package com.alibaba.datax.utils;

/**
 * @Description: 银行卡号工具类（Luhn算法）：银行卡号由最多19位数字组成，前6位是发卡行识别码、7~18位由发卡行自定义、19位是校验码
 * ISO/IEC 7812-1:2017:8位是发卡行识别码、9-18位由发卡行自定义、19位是校验码
 * @Title: BankCardUtils
 * @Package: com.audaque.cloud.dsps.desensitization.utils
 * @author: huafu.su
 * @Date: 2024/6/6 11:08
 */
public class BankCardUtils {

	/**
	 * @param cardNo 银行卡号
	 * @return java.lang.String
	 * @Description: 根据银行卡号前18位，生成校验码返回信用代码
	 * @Title: BankCardUtils
	 * @Package: com.audaque.cloud.dsps.desensitization.utils
	 * @author: huafu.su
	 * @Date: 2024/6/6 11:37
	 */
	public static String generateBankCardCode(String cardNo) {
		if (cardNo == null || cardNo.isEmpty()) {
			throw new RuntimeException("dsps.module.algorithm.service.0014");
		}
		// 6位IIN+最多12位自定义数字+1位校验数字、注意ISO/IEC 7812-1:2017中重新定义8位IIN+最多10位自定义数字+1位校验数字
		// 这里为了兼容2017之前的版本，使用8~19位数字校验
		if (!cardNo.matches("^\\d{8,19}$")) {
			throw new RuntimeException("数据格式不正确");
		}
		String id = cardNo.substring(0, cardNo.length() - 1);
		int sum = 0;
		boolean isSecondDigit = false;
		final String substr = id + "0";
		// 从右向左遍历，但不包括假设的校验位
		for (int i = substr.length() - 1; i >= 0; i--) {
			int digit = Character.getNumericValue(substr.charAt(i));
			if (isSecondDigit) {
				digit *= 2;
				if (digit > 9) {
					digit -= 9; // 如果乘以2的结果大于9，则减去9
				}
			}
			sum += digit;
			isSecondDigit = !isSecondDigit; // 切换双倍计算标志
		}
		// 找到需要加在末尾使得总和能被10整除的数，即校验码
		int checkDigit = (10 - (sum % 10)) % 10;
		return id + checkDigit;
	}

	/**
	 * @param cardNo 银行卡号
	 * @return boolean true:通过
	 * @Description: 验证银行卡号是否合法
	 * @Title: BankCardUtils
	 * @Package: com.audaque.cloud.dsps.desensitization.utils
	 * @author: huafu.su
	 * @Date: 2024/6/6 11:35
	 */
	public static boolean checkBankCard(String cardNo) {
		if (cardNo == null || cardNo.isEmpty()) {
			throw new RuntimeException("dsps.module.algorithm.service.0014");
		}
		// 6位IIN+最多12位自定义数字+1位校验数字、注意ISO/IEC 7812-1:2017中重新定义8位IIN+最多10位自定义数字+1位校验数字
		// 这里为了兼容2017之前的版本，使用8~19位数字校验
		if (!cardNo.matches("^\\d{8,19}$")) {
			throw new RuntimeException("数据格式不正确");
		}
		int sum = 0;
		boolean isSecondDigit = false;
		// 从右向左遍历
		for (int i = cardNo.length() - 1; i >= 0; i--) {
			int digit = Character.getNumericValue(cardNo.charAt(i));
			if (isSecondDigit) {
				digit *= 2;
				if (digit > 9) {
					digit -= 9; // 如果乘以2的结果大于9，则减去9
				}
			}
			sum += digit;
			isSecondDigit = !isSecondDigit; // 切换双倍计算标志
		}
		// 校验和必须能被10整除
		return sum % 10 == 0;
	}

}
