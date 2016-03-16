package com.adanac.framework.uniconfig.constant;

/**
 * 常量类
 * @author adanac
 * @version 1.0
 */
public class UniconfigConstant {
	/**
	 * 操作类型 创建
	 */
	public static final String C = "create";
	/**
	 * 操作类型 修改
	 */
	public static final String U = "update";
	/**
	 * 操作类型 删除
	 */
	public static final String D = "delete";
	/**
	 * uniconfig dev环境地址
	 */
	public static final String UNICONFIG_SERVER_DEV = "http://scmdev";
	/**
	 * uniconfig sit环境地址
	 */
	public static final String UNICONFIG_SERVER_SIT = "http://scmsit";
	/**
	 * uniconfig pre环境地址
	 */
	public static final String UNICONFIG_SERVER_PRE = "http://scmpre";
	/**
	 * 文件操作编码
	 */
	public static final String ENCODE = "UTF-8";
	/**
	 * 本地配置存储路径
	 */
	public static final String FILE_PATH = System.getProperty("user.dir");
	/**
	 * 特殊字符
	 */
	public static final String REGEX = "[`~!@#$%^&*()+=|{}':;',//[//].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";

	/**
	 * 根节点
	 */
	public static final String ROOT_NODE = "adanac";
}
