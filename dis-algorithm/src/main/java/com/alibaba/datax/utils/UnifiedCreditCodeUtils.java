package com.alibaba.datax.utils;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.TreeBidiMap;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;

/**
 * 统一社会信用代码工具类
 * 社会统一信用代码由登记管理部门代码（1位）、机构类别代码（1位）、登记管理机关行政区划码（6位）、主体标识码（9位全国组织机构代码）、校验码（1位）五个部分组成。
 * @author liudaizhong.liu
 * @date 2019年11月5日 下午4:33:00
 * @desc
 */
public class UnifiedCreditCodeUtils {
	static String baseCode = "0123456789ABCDEFGHJKLMNPQRTUWXY";
	static char[] baseCodeArray = baseCode.toCharArray();
	static int[] wi = { 1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28 };

	/**
	 * 生成供较验使用的 Code Map
	 * @return BidiMap
	 */
	private static BidiMap<Character, Integer> generateCodes() {
		BidiMap<Character, Integer> codes = new TreeBidiMap<>();
		for (int i = 0; i < baseCode.length(); i++) {
			codes.put(baseCodeArray[i], i);
		}
		return codes;
	}

	/**
	 * 校验社会统一信用代码
	 * @param unifiedCreditCode 统一社会信息代码
	 * @return 符合: true
	 */
	public static boolean checkUnifiedCreditCode(String unifiedCreditCode) {
		if ((unifiedCreditCode.equals("")) || unifiedCreditCode.length() != 18) {
			return false;
		}
		boolean areaFlag = false;
		if (!IdentityCardUtils.isEmptyForAreaCodeMap()) {
			final String areaCodeStr = unifiedCreditCode.substring(2, 8);
			if (!IdentityCardUtils.containsKeyForAreaCodeMap(areaCodeStr)) {
				areaFlag = true;
			}
		}
		if (areaFlag) {
			throw new RuntimeException("dsps.module.algorithm.service.0034");
		}
		Map<Character, Integer> codes = generateCodes();
		int parityBit;
		try {
			parityBit = getParityBit(unifiedCreditCode, codes);
		} catch (RuntimeException e) {
			return false;
		}
		return parityBit == codes.get(unifiedCreditCode.charAt(unifiedCreditCode.length() - 1));
	}

	/**
	 * 获取较验码
	 * @param unifiedCreditCode 统一社会信息代码
	 * @param codes 带有映射关系的国家代码
	 * @return 获取较验位的值
	 */
	public static int getParityBit(String unifiedCreditCode, Map<Character, Integer> codes) {
		char[] businessCodeArray = unifiedCreditCode.toCharArray();
		int sum = 0;
		for (int i = 0; i < 17; i++) {
			char key = businessCodeArray[i];
			if (baseCode.indexOf(key) == -1) {
//				"第{0}位传入了非法的字符{1}"
				throw new RuntimeException("dsps.module.algorithm.service.0036" + i + 1 + key);
			}
			sum += (codes.get(key) * wi[i]);
		}
		int result = 31 - sum % 31;
		return result == 31 ? 0 : result;
	}
	/**
	 * 获取较验码，默认生成国家代码
	 * @param unifiedCreditCode 统一社会信用代码
	 * @return 获取较验位的值
	 */
	public static int getParityBit(String unifiedCreditCode) {
		char[] businessCodeArray = unifiedCreditCode.toCharArray();
		Map<Character, Integer> codes = generateCodes();
		int sum = 0;
		for (int i = 0; i < 17; i++) {
			char key = businessCodeArray[i];
			if (baseCode.indexOf(key) == -1) {
				throw new RuntimeException("dsps.module.algorithm.service.0036" + i + 1 + key);
			}
			sum += (codes.get(key) * wi[i]);
		}
		int result = 31 - sum % 31;
		return result == 31 ? 0 : result;
	}
	/**
	 * 根据统一社会信用代码前17位，生成校验码返回信用代码
	 * @author daizhong.liu
	 * @date 2019年11月5日 下午5:03:27 
	 * @desc 
	 * @param unifiedCreditCode 统一社会信用代码
	 * @return
	 */
	public static String generateUnifiedCreditCode(String unifiedCreditCode) {
		BidiMap<Character, Integer> codes = generateCodes();
		int creditCode = getParityBit(unifiedCreditCode.trim());
		String id = unifiedCreditCode.trim().substring(0, 17);
		return id+codes.getKey(creditCode);
	}

	/**
	 * 生成一个随机的统一社会信用代码
	 * @return 统一社会信用代码
	 */
	public static String generateOneUnifiedCreditCode() {
		//Random random = new Random();
		Random random = null;
		try {
			random = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
		}
		StringBuilder buf = new StringBuilder();
		if (random != null) {
			for (int i = 0; i < 17; ++i) {
				int num = random.nextInt(baseCode.length() - 1);
				buf.append(baseCode.charAt(num));
			} 
		}
		String code = buf.toString();
		String upperCode = code.toUpperCase();
		BidiMap<Character, Integer> codes = generateCodes();
		int parityBit = getParityBit(upperCode, codes);
		if (codes.getKey(parityBit) == null) {
			upperCode = generateOneUnifiedCreditCode();
		} else {
			upperCode = upperCode + codes.getKey(parityBit);
		}
		return upperCode;
	}
	 
}
