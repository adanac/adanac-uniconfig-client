package com.adanac.framework.client;

/**
 * 統一配置客户端
 * @author adanac
 * @version 1.0
 */
public interface UniconfigClient {
	/**
	* 获取配置
	*/
	public UniconfigNode getConfig(String path);

	/**
	 * 创建配置
	 */
	public void createConfig(String user, String path, String value) throws UniconfigException;

	/**
	 * 修改配置
	 */
	public void updateConfig(String user, String path, String value) throws UniconfigException;

	/**
	 * 删除配置
	 */
	public void deleteConfig(String user, String path) throws UniconfigException;

	/**
	 * 销毁SCMClient对象，释放资源
	 */
	public void destroy();

	/**
	 * 获取项目AppCode
	 */
	public String getAppCode();

	/**
	 * 获取项目私钥
	 */
	public String getSecureKey(String appCode);

	/**
	 * 获取全局配置
	 */
	public UniconfigNode getGlobalConfig(String path) throws UniconfigException;

	/**
	 * 判断环境
	 */
	public Environment getEnvironment();

	public enum Environment {
		DEV, SIT, PRE, PRD
	}
}
