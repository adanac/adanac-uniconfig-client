package com.adanac.framework.client;

/**
 * 统一配置监听接口
 * @author adanac
 * @version 1.0
 */
public interface UniconfigListener {
	/**
	 * 配置变更后处理
	 * @param oldValue old data
	 * @param newValue new data
	 */
	void execute(String oldValue, String newValue);
}
