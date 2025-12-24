package com.alibaba.datax.utils;


import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

/** 
 * 密钥生成器
* @author liudaizhong.liu
* @date 2019年11月8日 下午3:34:49 
* @desc 
*/
public class KeyGenerator {

	/**所有字符名称*/
	private static final String ALLCHAR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    /**
     * '生成指定长度的密钥key
     * @author daizhong.liu
     * @date 2019年11月8日 下午3:26:48 
     * @desc 
     * @param length 密钥长度
     * @return
     */
    public static String generatorKey(int length) {
    	StringBuilder sb = new StringBuilder();
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
			random.setSeed(random.generateSeed(256));
			for (int i = 0; i < length; i++) {
				sb.append(ALLCHAR.charAt(random.nextInt(ALLCHAR.length())));
			}
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
		}
		return sb.toString();
    }
    /**
     *  3DES算法密钥(48位)
     * @author daizhong.liu
     * @date 2019年11月8日 下午3:27:48 
     * @desc 
     * @return
     */
    public static String generator3DESKey() {
    	return generatorKey(48);
    }
    /**
     * AES算法密钥(32位)
     * @author daizhong.liu
     * @date 2019年11月8日 下午3:38:22 
     * @desc 
     * @return
     */
    public static String generatorSM4Key() {
    	try {
			return DataConvertUtil.toHexString(SM4Utils.generateKey());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }
    /**
     * AES算法密钥(16位)
     * @author daizhong.liu
     * @date 2019年11月8日 下午3:38:22 
     * @desc 
     * @return
     */
    public static String generatorAESKey() {
    	return generatorKey(16);
    }
    /**
     * DES算法密钥(16位)
     * @author daizhong.liu
     * @date 2019年11月8日 下午3:38:22 
     * @desc 
     * @return
     */
    public static String generatorDESKey() {
    	return generatorKey(16);
    }
    
}
