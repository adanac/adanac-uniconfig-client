package com.adanac.framework.uniconfig.client;

/**
 * 节点配置
 * @author adanac
 * @version 1.0
 */
public interface UniconfigNode {
	public String getValue();

	public void sync() throws UniconfigException;

	public void monitor(UniconfigListener scmListener);

	public void destroy();
}
