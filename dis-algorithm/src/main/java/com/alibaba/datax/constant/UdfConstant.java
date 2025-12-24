package com.alibaba.datax.constant;
/** 
 * @author liudaizhong.liu
 * @date 2020年5月29日 下午6:45:45 
 * @desc 
 */
public class UdfConstant {
	/**
	 * SM4加密方法名
	 */
	public static final String FUNCTION_SM4_ENCRYPT = "DQMS_SM4_ENCRYPT";
	/**
	 * SM4解密方法名
	 */
	public static final String FUNCTION_SM4_DECRYPT = "DQMS_SM4_DECRYPT";
	/**
	 * DES加密方法名
	 */
	public static final String FUNCTION_DES_ENCRYPT = "DQMS_DES_ENCRYPT";
	/**
	 * DES解密方法名
	 */
	public static final String FUNCTION_DES_DECRYPT = "DQMS_DES_DECRYPT";
	/**
	 *  数据脱敏
	 */
	public static final String FUNCTION_DESENSITIZATION = "DQMS_DESENSITIZATION";
	
	/**
	 * 质量稽查正则规则
	 */
	public static final String FUNCTION_REGEX = "DQMS_REGEX";
	
	/**
	 * 元规则
	 */
	public static final String FUNCTION_META_RULE = "DQMS_META_RULE";
	
	/**
	 * 异常规则
	 */
	public static final String FUNCITON_EXCEPTION_RULE = "DQMS_EXCEPTION_RULE";
	
	/**
	 * 符合函数返回值
	 */
	public static final String PASS = "1";
	
	/**
	 * 不符合函数值返回
	 */
	public static final String NO_PASS = "0";
}
