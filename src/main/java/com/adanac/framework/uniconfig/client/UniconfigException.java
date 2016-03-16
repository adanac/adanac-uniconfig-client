package com.adanac.framework.uniconfig.client;

/**
 * 统一配置异常类
 * @author adanac
 * @version 1.0
 */
public class UniconfigException extends RuntimeException {
	private static final long serialVersionUID = -567786786121301727L;

	public UniconfigException(String message) {
		super(message);
	}

	public UniconfigException(String message, Throwable cause) {
		super(message, cause);
	}
}
