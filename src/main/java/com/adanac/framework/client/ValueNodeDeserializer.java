package com.adanac.framework.client;

import com.adanac.framework.zookeeper.NodeDeserializer;

/**
 * 配置对象反序列化
 * @author adanac
 * @version 1.0
 */
public class ValueNodeDeserializer implements NodeDeserializer<String> {

	@Override
	public String deserialize(byte[] data) {
		return data == null ? null : new String(data);
	}

}
