package com.alibaba.datax.utils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Pattern;
/**
 * 数字工具类
 * @author liudaizhong.liu
 * @date 2019年11月6日 下午4:21:25
 * @desc
 */
public class AqdNumberUtils extends org.apache.commons.lang3.math.NumberUtils {
	/**
	 * @Fields REG_NUMBER : 正则-数字
	 */
	private static final String REG_NUMBER = "^([-]|[+])?\\d+([.]\\d+)?$";

	/**
	 * @Fields REG_INTEGER : 正则-整数
	 */
	private static final String REG_INTEGER = "^([-]|[+])?\\d+$";

	/**
	 * @Fields REG_DECIMAL : 正则-小数
	 */
	private static final String REG_DECIMAL = "^([-]|[+])?\\d+[.]\\d+$";

	/**
	 * @Fields REG_POSITIVE : 正则-正数
	 */
	private static final String REG_POSITIVE = "^[+]?\\d+([.]\\d+)?$";

	/**
	 * @Fields REG_POSIT_INT : 正则-正整数
	 */
	private static final String REG_POSIT_INT = "^[+]?\\d+$";

	/**
	 * @Description 判断当前值是否为数字
	 * @param obj 需判断的值
	 * @return true：是、false：否
	 */
	public static boolean isDigits(Object obj) {
		if (null == obj)
			return false;
		return isDigits(obj.toString());
	}

	/**
	 * @Description 判断字符串是否为整数
	 * @param str 需判断的字符串
	 * @return true：是、false：否
	 */
	public static boolean isInteger(String str) {
		if (StringUtils.isEmpty(str))
			return false;
		return Pattern.matches(REG_INTEGER, str);
	}

	/**
	 * @Description 判断字符串是否为小数
	 * @param str 需判断的字符串
	 * @return true：是、false：否
	 */
	public static boolean isDecimal(String str) {
		if (StringUtils.isEmpty(str))
			return false;
		return Pattern.matches(REG_DECIMAL, str);
	}

	/**
	 * @Description 判断字符串是否为正数
	 * @param str 需判断的字符串
	 * @return true：是、false：否
	 */
	public static boolean isPositive(String str) {
		if (StringUtils.isEmpty(str))
			return false;
		return Pattern.matches(REG_POSITIVE, str);
	}

	/**
	 * @Description 判断字符串是否为正整数
	 * @param str 需判断的字符串
	 * @return true：是、false：否
	 */
	public static boolean isPositInt(String str) {
		if (StringUtils.isEmpty(str))
			return false;
		return Pattern.matches(REG_POSIT_INT, str);
	}

	/**
	 * @Description 将数字格式化输出
	 * @param str       需要格式化的值
	 * @param precision 精度（小数点后的位数）（默认2位）
	 * @return 格式化后的数字字符串
	 */
	public static String format(String str, Integer precision) {
		Double number = 0.0;
		if (isNumber(str))
			number = Double.valueOf(str);
		precision = (Objects.equals(precision, null) || precision.intValue() < 0) ? Integer.valueOf(2) : precision;
		BigDecimal bigDecimal = new BigDecimal(number);
		return bigDecimal.setScale(precision, BigDecimal.ROUND_HALF_UP).toPlainString();
	}

	/**
	 * @Description 将数字格式化输出（保留小数点后2位）
	 * @param str 需要格式化的值
	 * @return 格式化后的数字字符串
	 */
	public static String format(String str) {
		return format(str, 2);
	}
 
}
