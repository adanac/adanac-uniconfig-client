package com.adanac.framework.client;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adanac.framework.constant.UniconfigConstant;
import com.adanac.framework.util.StringUtils;
import com.adanac.framework.zookeeper.DataException;
import com.adanac.framework.zookeeper.DataListener;
import com.adanac.framework.zookeeper.ZooKeeperNode;

public class UniconfigNodeImpl implements UniconfigNode {
	private static Logger logger = LoggerFactory.getLogger(UniconfigNodeImpl.class);

	private static final String PATH_SEPARATOR = "/";

	private String path;

	private ZooKeeperNode<String> zkNode;

	private volatile String value;

	public UniconfigNodeImpl(String path, ZooKeeperNode<String> zkNode) {
		this.path = path;
		this.zkNode = zkNode;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void sync() throws UniconfigException {
		try {
			zkNode.sync(false);
			value = zkNode.getData();
		} catch (DataException ex) {
			logger.warn("Can not get config content of " + path + ",will try to load from local file.");
			value = loadFromLocalFileCache(path);
		}
	}

	@Override
	public void monitor(final UniconfigListener scmListener) {
		zkNode.monitor(value, new DataListener<String>() {
			@Override
			public void dataChanged(String oldData, String newData) {
				String oldValue = value;
				value = newData;
				saveToLocalFileCache(path, value);
				scmListener.execute(oldValue, newData);
			}
		});
	}

	@Override
	public void destroy() {
		zkNode.destroy();
	}

	// 从本地缓存文件加载,如果文件不存在则抛异常
	private String loadFromLocalFileCache(String path) throws UniconfigException {
		final File file = new File(getLocalFilePath(path));
		if (!file.exists())
			throw new UniconfigException("LocalFile is not exist!");
		FileInputStream in = null;
		final int size = 512;
		StringBuilder sb = new StringBuilder(size);
		try {
			in = new FileInputStream(file);
			final int length = 8192;
			byte[] data = new byte[length];
			int n = -1;
			while ((n = in.read(data)) != -1) {
				sb.append(new String(data, 0, n, UniconfigConstant.ENCODE));
			}
			return sb.toString();
		} catch (IOException e) {
			throw new UniconfigException("get localFile config error,path= " + path, e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}
	}

	// 保存到本地缓存
	private void saveToLocalFileCache(String path, String value) {
		File file;
		FileOutputStream out = null;
		PrintWriter writer = null;
		try {
			file = new File(getLocalFilePath(path));
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (RuntimeException e) {
					logger.error(e.getMessage(), e);
				}
			}
			out = new FileOutputStream(file);
			BufferedOutputStream stream = new BufferedOutputStream(out);
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream, UniconfigConstant.ENCODE)));
			writer.write(value == null ? "" : value);
			writer.flush();
		} catch (IOException e) {
			logger.error("save localFile config error, path= " + path, e);
		} finally {
			if (writer != null) {
				writer.close();
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * 获取本地文件路径
	 */
	private String getLocalFilePath(String path) {
		if (path.contains(PATH_SEPARATOR)) {
			path = StringUtils.substringAfterLast(path, PATH_SEPARATOR);
		}
		logger.info("LocalFilePath ==========" + UniconfigConstant.FILE_PATH + PATH_SEPARATOR + path);
		return UniconfigConstant.FILE_PATH + PATH_SEPARATOR + StringFilter(path);
	}

	// 过滤特殊字符
	public static String StringFilter(String str) throws PatternSyntaxException {
		Pattern p = Pattern.compile(UniconfigConstant.REGEX);
		Matcher m = p.matcher(str);
		return m.replaceAll("").trim();
	}

}
