package com.adanac.framework.util;

import java.net.URLDecoder;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * HMAC安全编码组件
 * @author adanac
 * @version 1.0
 */
public class EncryptUtil {

	/**
	 * utf8
	 */
	public static final String ENCODER_UTF8 = "UTF-8";

	/**
	 * ISO-8859-1
	 */
	public static final String ENCODER_ISO8859 = "ISO-8859-1";

	/**
	 * HmacMD5
	 */
	public static final String KEY_MAC = "HmacMD5";

	/**
	 * 
	 * 功能描述: <br>
	 * 消息加密编码
	 * 
	 * @param data 数据
	 * @param key 私钥
	 */
	public static String encryptHMAC(String data, String key) throws Exception {
		String keyStr = URLDecoder.decode(key, ENCODER_UTF8);
		byte[] keyRaw = keyStr.getBytes(ENCODER_ISO8859);
		SecretKey secretKey = new SecretKeySpec(keyRaw, KEY_MAC);
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		mac.init(secretKey);
		byte[] raw = mac.doFinal(data.getBytes(ENCODER_UTF8));
		// return URLEncoder.encode(new String(raw, ENCODER_ISO8859),
		// ENCODER_UTF8);
		return toHexString(raw);
	}

	// 将加密后的byte数组转换为十六进制的字符串,否则的话生成的字符串会乱码
	public static String toHexString(byte[] byteStr) {
		StringBuilder strBuff = new StringBuilder();

		for (int i = 0; i < byteStr.length; i++) {
			if (Integer.toHexString(0xFF & byteStr[i]).length() == 1) {
				strBuff.append("0").append(Integer.toHexString(0xFF & byteStr[i]));
			} else {
				strBuff.append(Integer.toHexString(0xFF & byteStr[i]));
			}
		}
		return strBuff.toString();
	}

	public static void main(String[] args) throws Exception {
		String name = "test";
		String enName = encryptHMAC(name, "qwe");
		System.out.println(enName);

	}
}
