package com.adanac.framework.uniconfig.bean;

import java.io.Serializable;

public class UniconfigNodeImpl implements UniconfigNode, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2512978033877076290L;

	/**
	 * 节点内容
	 */
	private String value;

	/**
	 * 节点类型
	 */
	private String type;

	public String getValue() {
		return (value == null) ? EMPTY_STR : value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return (type == null) ? EMPTY_STR : type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
