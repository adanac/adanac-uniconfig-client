package com.adanac.framework.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 请求参数工具类
 * @author adanac
 * @version 1.0
 */
public class ParamUtil {
	/**
	 * 基本数据类型
	 */
	@SuppressWarnings({ "rawtypes", "serial" })
	private static final Set<Class> BASEDATATYPE = new HashSet<Class>() {
		{
			add(short.class);
			add(int.class);
			add(long.class);
			add(float.class);
			add(double.class);
			add(boolean.class);
			add(char.class);
			add(Short.class);
			add(Integer.class);
			add(Long.class);
			add(Float.class);
			add(Double.class);
			add(Boolean.class);
			add(Character.class);
			add(String.class);
		}
	};

	/**
	 * 功能描述: <br>
	 * 将map参数集合拼装成请求字符串
	 *
	 * @param map 请求参数的map集合
	 * @return 请求数据字符串
	 */
	@SuppressWarnings("rawtypes")
	public static String getParamFromMap(Map<String, Object> map) {
		// 请求参数map不能为空
		if (map == null) {
			throw new IllegalArgumentException("map is null!");
		}
		// mapping列表
		List<Map.Entry<String, Object>> mappingList = new ArrayList<Map.Entry<String, Object>>(map.entrySet());
		// 根据key值排序
		Collections.sort(mappingList, new Comparator<Map.Entry<String, Object>>() {
			// 排序
			public int compare(Map.Entry<String, Object> entry1, Map.Entry<String, Object> entry2) {
				return entry1.getKey().compareTo(entry2.getKey());
			}
		});
		// 请求字符串
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, Object> entry : mappingList) {
			Object obj = entry.getValue();
			// 基本数据类型
			if (BASEDATATYPE.contains(obj.getClass())) { // String类型
				builder.append(entry.getKey()).append("=").append(obj).append("&");
				// 数组
			} else if (obj.getClass().isArray()) {
				// 计算数组长度
				int arrLength = Array.getLength(obj);
				// 数组长度需要大于0
				if (arrLength > 0) {
					for (int i = 0; i < arrLength; i++) {
						// 添加到请求url中
						builder.append(entry.getKey()).append("=").append(Array.get(obj, i)).append("&");
					}
				}
				// 集合
			} else if (obj instanceof Collection) {
				// 计算集合长度
				Collection collection = (Collection) obj;
				// 集合长度需要大于0
				if (collection.size() > 0) {
					for (Object o : collection) {
						// 添加到请求url中
						builder.append(entry.getKey()).append("=").append(o).append("&");
					}
				}
			} else {
				// 无法解析请求对象
				throw new RuntimeException("Can't parse complex Object:" + obj.getClass());
			}
		}
		// 删除最后一个连接符号
		builder.deleteCharAt(builder.lastIndexOf("&"));
		// 返回拼接完成的请求url
		return builder.toString();
	}

	/**
	 * 功能描述: <br>
	 * 获取编码后的参数
	 *
	 * @param map 参数map
	 * @return 编码后的参数值
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("rawtypes")
	public static String getEncodedParamFromMap(Map<String, Object> map) throws UnsupportedEncodingException {
		// 参数map不能为空
		if (map == null) {
			throw new IllegalArgumentException("map is null!");
		}
		// 编码后的map
		Map<String, Object> encodedMap = new HashMap<String, Object>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object obj = entry.getValue();
			// String类型
			if (obj instanceof String) {
				encodedMap.put(entry.getKey(), URLEncoder.encode((String) obj, "UTF-8"));
				// 数组
			} else if (obj.getClass().isArray()) {
				// 计算数组长度
				int arrLength = Array.getLength(obj);
				Object[] arr = new Object[arrLength];
				for (int i = 0; i < arrLength; i++) {
					// 数组中的数据是string需要编码
					if (Array.get(obj, i) instanceof String) {
						arr[i] = URLEncoder.encode((String) Array.get(obj, i), "UTF-8");
					} else {
						arr[i] = Array.get(obj, i);
					}
				}
				// 放入编码后的map
				encodedMap.put(entry.getKey(), arr);
				// 集合
			} else if (obj instanceof Collection) {
				Collection collection = (Collection) obj;
				// 计算集合长度
				int arrLength = collection.size();
				Object[] arr = new Object[arrLength];
				int i = 0;
				for (Object o : collection) {
					// 集合中的元素是stirng
					if (o instanceof String) {
						// utf8编码后放入
						arr[i] = URLEncoder.encode((String) o, "UTF-8");
					} else {
						arr[i] = o;
					}
					i++;
				}
				// 放入编码后的map
				encodedMap.put(entry.getKey(), arr);
			} else {
				encodedMap.put(entry.getKey(), obj);
			}
		}
		return getParamFromMap(encodedMap);
	}
}
