package com.adanac.framework.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adanac.framework.constant.UniconfigConstant;
import com.adanac.framework.statistics.VersionStatistics;
import com.adanac.framework.util.EncryptUtil;
import com.adanac.framework.util.HttpClient;
import com.adanac.framework.util.ParamUtil;
import com.adanac.framework.util.StringUtils;
import com.adanac.framework.zookeeper.ZooKeeperClient;
import com.adanac.framework.zookeeper.ZooKeeperClient.ZooKeeperConnectionException;
import com.adanac.framework.zookeeper.ZooKeeperNode;

/**
 * 統一配置客户端
 * @author adanac
 * @version 1.0
 */
public class UniconfigClientImpl implements UniconfigClient {

	// 日志
	private static Logger logger = LoggerFactory.getLogger(UniconfigClientImpl.class);

	private static final ConcurrentHashMap<String, UniconfigClient> INSTANCES = new ConcurrentHashMap<String, UniconfigClient>();

	private ConcurrentHashMap<String, UniconfigNode> uniconfigNodes = new ConcurrentHashMap<String, UniconfigNode>();

	private final Object MUX = new Object();

	private static final String PATH_SEPARATOR = "/";

	private static final String CONFIG_FILE = "/uniconfig.properties";

	private static final String GLOBAL_PATH = "/global";

	private static final String ROOT = "/bn";

	// 当前环境
	private Environment environment;

	// zk客户端
	private ZooKeeperClient zooKeeperClient;

	// 配置对象反序列化
	private ValueNodeDeserializer valueNodeDeserializer = new ValueNodeDeserializer();

	/**
	 * app code key
	 */
	public String appCode = "";

	/**
	 * secret key
	 */
	private String secretKey = "";

	/**
	 * uniconfig server(http url)
	 */
	private String uniconfigServer = "";

	private String zkServer = "";

	public static UniconfigClient getInstance() throws UniconfigException {
		UniconfigClient client = INSTANCES.get(CONFIG_FILE);
		if (client != null) {
			return client;
		}
		synchronized (INSTANCES) {
			client = INSTANCES.get(CONFIG_FILE);
			if (client == null) {
				client = new UniconfigClientImpl(CONFIG_FILE);
				INSTANCES.put(CONFIG_FILE, client);
			}
			return client;
		}
	}

	// 创建客户端
	private UniconfigClientImpl(String scmConfigFile) throws UniconfigException {
		// 读取zk server配置
		InputStream inputStream = UniconfigClientImpl.class.getResourceAsStream(scmConfigFile);
		if (inputStream != null) {
			try {
				Properties properties = new Properties();
				properties.load(inputStream);
				zkServer = (String) properties.get("zkServer");
				this.appCode = (String) properties.get("appCode");
				this.secretKey = (String) properties.get("secretKey");
				this.uniconfigServer = (String) properties.get("uniconfigServer");
				if (StringUtils.isEmpty(zkServer) || StringUtils.isEmpty(secretKey) || StringUtils.isEmpty(appCode)
						|| StringUtils.isEmpty(uniconfigServer)) {
					throw new UniconfigException(
							"load scm config error,zkServer or appCode" + " or scmServer or secretKey is null ");
				}
				VersionStatistics.reportVersion(appCode, uniconfigServer, UniconfigClientImpl.class);
			} catch (Exception e) {
				throw new UniconfigException("load uniconfig config error", e);
			} finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error("close stream error", e);
				}
			}
		}
		// 根据url地址判断当前使用环境
		if (uniconfigServer.startsWith(UniconfigConstant.UNICONFIG_SERVER_DEV)) {
			this.environment = Environment.DEV;
		} else if (uniconfigServer.startsWith(UniconfigConstant.UNICONFIG_SERVER_SIT)) {
			this.environment = Environment.SIT;
		} else if (uniconfigServer.startsWith(UniconfigConstant.UNICONFIG_SERVER_PRE)) {
			this.environment = Environment.PRE;
		} else {
			this.environment = Environment.PRD;
		}
		// 创建zk客户端
		this.zooKeeperClient = ZooKeeperClient
				.getInstance(ZooKeeperClient.digestCredentials(appCode.trim(), secretKey.trim()), zkServer.trim());
	}

	@Override
	public UniconfigNode getConfig(final String path) {
		UniconfigNode scmNode = uniconfigNodes.get(path);
		if (scmNode != null) {
			return scmNode;
		}
		synchronized (MUX) {
			scmNode = uniconfigNodes.get(path);
			if (scmNode != null) {
				return scmNode;
			}
			ZooKeeperNode<String> zkNode = ZooKeeperNode.create(zooKeeperClient, getFullPath(appCode, path),
					valueNodeDeserializer);
			scmNode = new UniconfigNodeImpl(path, zkNode);
			uniconfigNodes.put(path, scmNode);
			return scmNode;
		}
	}

	public static String getFullPath(String appCode, String path) {

		String fullPath;
		/*
		 * if (path.indexOf(PATH_SEPARATOR) == 0) { path =
		 * StringUtils.substringAfter(path, PATH_SEPARATOR); } if
		 * (path.contains(GLOBAL_PATH.substring(1))) { fullPath = ROOT +
		 * PATH_SEPARATOR + path; } else { fullPath = ROOT + PATH_SEPARATOR +
		 * appCode + PATH_SEPARATOR + path; }
		 */

		if (path.contains(GLOBAL_PATH.substring(1))) {
			fullPath = ROOT + path;
		} else {
			fullPath = PATH_SEPARATOR + UniconfigConstant.ROOT_NODE + PATH_SEPARATOR + appCode + PATH_SEPARATOR + path;
		}
		return fullPath;
	}

	// 获取远程操作path,对应数据库中path
	public static String getFullOperationPath(String appCode, String path) {
		String fullPath;
		/*
		 * if (path.indexOf(PATH_SEPARATOR) == 0) { path =
		 * StringUtils.substringAfter(path, PATH_SEPARATOR); } if
		 * (path.contains(GLOBAL_PATH.substring(1))) { fullPath = DB_ROOT +
		 * PATH_SEPARATOR + path; } else { fullPath = DB_ROOT + PATH_SEPARATOR +
		 * appCode + PATH_SEPARATOR + path; }
		 */
		fullPath = PATH_SEPARATOR + UniconfigConstant.ROOT_NODE + PATH_SEPARATOR + appCode + PATH_SEPARATOR + path;
		return fullPath;
	}

	@Override
	public void destroy() {
		for (UniconfigNode scmNode : uniconfigNodes.values()) {
			try {
				scmNode.destroy();
			} catch (Throwable ex) {
				// do nothing
			}
		}
	}

	// 创建配置
	@Override
	public void createConfig(String user, String path, String value) throws UniconfigException {
		remoteOperationConfig(user, getFullOperationPath(appCode, path), value, UniconfigConstant.C);
	}

	// 更新配置
	@Override
	public void updateConfig(String user, String path, String value) throws UniconfigException {
		try {
			if (!StringUtils.isEmpty(value)) {
				value = URLEncoder.encode(value, "UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		remoteOperationConfig(user, getFullOperationPath(appCode, path), value, UniconfigConstant.U);
	}

	// 删除配置
	@Override
	public void deleteConfig(String user, String path) throws UniconfigException {
		remoteOperationConfig(user, getFullOperationPath(appCode, path), "", UniconfigConstant.D);
	}

	// 远程操作服务端配置
	public void remoteOperationConfig(String user, String path, String config, String operationType)
			throws UniconfigException {
		Map<String, Object> map = new HashMap<String, Object>();
		// 应用ID
		map.put("appCode", appCode);
		map.put("path", path);
		map.put("config", config);
		map.put("operationType", operationType);
		map.put("user", user);
		String result = sendDataByPost(uniconfigServer + "/operationConfig.htm", map);
		// 处理内容
		String[] resultsStrings = result.split(",");
		// 远程操作失败抛出异常
		if ("failure".equals(resultsStrings[0])) {
			throw new UniconfigException(resultsStrings[1]);
		}
	}

	/**
	 * 功能描述: send data by post<br>
	 * 通过post方式提交数据
	 *
	 * @param url     request uri
	 * @param dataMap post data map
	 * @return result
	 * @see [相关类/方法](可选)
	 * @since [产品/模块版本](可选)
	 */
	protected String sendDataByPost(String url, Map<String, Object> dataMap) {
		// 请求url不能为空
		if (url == null || url.length() == 0) {
			throw new IllegalArgumentException("url is empty!");
		}
		// url地址
		// 加入时间戳
		dataMap.put("timeStamp", System.currentTimeMillis());
		try {
			// 从map中提取参数内容
			String digestStr = ParamUtil.getParamFromMap(dataMap);
			// 数据加密
			String mac = EncryptUtil.encryptHMAC(digestStr, this.secretKey);
			// 从map中获取加密参数
			String paramStr = ParamUtil.getEncodedParamFromMap(dataMap);
			// 添加加密数据
			String result = HttpClient.sendByPost(url, paramStr + "&mac=" + mac, 30000, 3, 2000);
			// 结果显示消息ID或者处理成功or失败
			logger.info("Result from SCM server:" + result);
			// 返回处理结果至调用方
			return result;
		} catch (Exception e) {
			// 发送数据异常
			throw new UniconfigException("Exception occur when send data to uniconfig server.", e);
		}
	}

	public ZooKeeperClient getZooKeeperClient() {
		return zooKeeperClient;
	}

	@Override
	public String getAppCode() {
		return appCode;
	}

	@Override
	public String getSecureKey(String appCode) {
		byte[] secureKey;
		try {
			secureKey = zooKeeperClient.get().getData(ROOT + PATH_SEPARATOR + appCode, false, null);
			return new String(secureKey);
		} catch (KeeperException e) {
			throw new UniconfigException(e.getMessage(), e);
		} catch (InterruptedException e) {
			throw new UniconfigException(e.getMessage(), e);
		} catch (ZooKeeperConnectionException e) {
			throw new UniconfigException(e.getMessage(), e);
		}
	}

	public String getSecretKey() {
		return secretKey;
	}

	public String getZkServer() {
		return zkServer;
	}

	@Override
	public UniconfigNode getGlobalConfig(String path) throws UniconfigException {
		if (path.indexOf(PATH_SEPARATOR) == 0) {
			path = StringUtils.substringAfter(path, PATH_SEPARATOR);
		}
		return getConfig(GLOBAL_PATH + PATH_SEPARATOR + path);
	}

	@Override
	public Environment getEnvironment() {
		return environment;
	}

	public static void main(String[] args)

	{
		// UniconfigClientImpl
		UniconfigClient uniconfigClient = UniconfigClientImpl.getInstance();
		/*
		 * globalWarnConfigNode =
		 * uniconfigClient.getGlobalConfig(GLOBAL_WARNING_CONFIG_PATH);
		 * globalWarnConfigNode.sync();
		 */

		UniconfigNode redisConfigNode;
		redisConfigNode = uniconfigClient.getConfig("redis");
		redisConfigNode.sync();

		String redisConfig = redisConfigNode.getValue();
		System.out.println(redisConfig);
		if (redisConfig == null || "".equals(redisConfig.trim())) {
			try {
				throw new Exception("can't find redis config or config content is empty.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
