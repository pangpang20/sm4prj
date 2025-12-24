package com.alibaba.datax.utils;

import org.bouncycastle.jcajce.provider.digest.SHA3;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 加密工具类
 * @author liudaizhong.liu
 * @date 2019年11月5日 下午7:09:02
 * @desc
 */
public class EncryptionUtils {
	// 用于加密的字符
	private static final char md5String[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	/** 
     * MAC算法可选以下多种算法 
     * <pre> 
     * HmacMD5  
     * HmacSHA1  
     * HmacSHA256  
     * HmacSHA384  
     * HmacSHA512 
     * </pre> 
	 */
	private static final String KEY_MAC = "HmacSHA256";
	/**
	 * 生成盐
	 * @author daizhong.liu
	 * @date 2019年11月5日 下午7:11:16
	 * @desc
	 * @return
	 */
	public static String buildSalt() {
		String model = "abcdefghijklmnopqrstuvwxyz1234567890";
		StringBuilder salt = new StringBuilder();
		char[] m = model.toCharArray();
		SecureRandom random = null;
		try {
			random = SecureRandomUtils.getInstance();
		} catch (Exception e) {
//            throw new AudaqueException("dgp.module.core.0036");
		}
		for (int i = 0; i < 6; i++) {
			Double index = random.nextDouble() * 36;
			char c = m[index.intValue()];
			salt = salt.append(c);
		}
		return salt.toString();
	}

	/**
	 * 数据加密成16位MD5
	 * @author daizhong.liu
	 * @date 2019年11月6日 上午9:34:35
	 * @desc
	 * @param content 加密内容
	 * @param salt    盐
	 * @return
	 */
	public static final String MD5Salt(String content, String salt) {
		content = content + salt;
		try {
			return encryptByAlgorithm(content,"MD5");
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * 使用sha-256加密
	 * @author daizhong.liu
	 * @date 2019年11月6日 上午11:14:55 
	 * @desc 
	 * @param content 加密内容
	 * @param salt 盐
	 * @return
	 */
	public static final String SHA1Salt(String content, String salt) {
		content = content + salt;
		try {
			return encryptByAlgorithm(content,"SHA1");
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @param content 加密内容
	 * @param salt    盐
	 * @return java.lang.String
	 * @Description: 使用sha3-256加密比sha-256慢很多
	 * @Title: EncryptionUtils
	 * @Package: com.audaque.cloud.dsps.desensitization.utils
	 * @author: huafu.su
	 * @Date: 2024/4/9 9:19
	 */
	public static final String SHA3_256Salt(String content, String salt) {
		content = content + salt;
		try {
			SHA3.Digest256 digest = new SHA3.Digest256();
			digest.update(content.getBytes(StandardCharsets.UTF_8));
			return byte2HexString(digest.digest());
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * 使用sha-256加密
	 * @author daizhong.liu
	 * @date 2019年11月6日 上午11:06:17 
	 * @desc 
	 * @param content 加密内容
	 * @param salt 盐 
	 * @return
	 */
	public static final String SHA256Salt(String content, String salt) {
		content = content + salt;
		try {
			return encryptByAlgorithm(content,"SHA-256");
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * HMAC算法加密，默认使用HmacSHA256，可以选择【HmacMD5、HmacSHA1、HmacSHA256、HmacSHA384、HmacSHA512】
	 * @author daizhong.liu
	 * @date 2019年11月6日 上午10:57:21 
	 * @desc 
	 * @param content 加密内容
	 * @param key 密钥(盐)
	 * @return
	 */
	public static final String HMACSalt(String content, String key) {
		byte[] result = null;  
        try {  
            //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称    
			SecretKeySpec signinKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), KEY_MAC);
            //生成一个指定 Mac 算法 的 Mac 对象    
            Mac mac = Mac.getInstance(KEY_MAC);  
            //用给定密钥初始化 Mac 对象    
            mac.init(signinKey);  
            //完成 Mac 操作     
			byte[] rawHmac = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
			result = Base64Utils.encode(rawHmac);
        } catch (NoSuchAlgorithmException e) {  
            System.err.println(e.getMessage());  
        } catch (InvalidKeyException e) {  
            System.err.println(e.getMessage());
		}
		if (null != result) {
			return new String(result, StandardCharsets.UTF_8);
        } else {  
            return null;  
        }
	}
	
	/**
	 * 将密文转换成十六进制的字符串形式
	 * @author daizhong.liu
	 * @date 2019年11月6日 上午9:55:12 
	 * @desc 
	 * @param bytes
	 * @return
	 */
	private static String byte2HexString(byte[] bytes) {
		int j = bytes.length;
		char str[] = new char[j * 2];
		int k = 0;
		for (int i = 0; i < j; i++) { // i = 0
			byte byte0 = bytes[i]; // 95
			str[k++] = md5String[byte0 >>> 4 & 0xf]; // 5
			str[k++] = md5String[byte0 & 0xf]; // F
		}
		// 返回经过加密后的字符串
		return new String(str);
	}
	/**
	 * 根据不同的算法转换成不同的加密方式
	 * @author daizhong.liu
	 * @date 2019年11月6日 上午9:58:29 
	 * @desc 
	 * @param content 加密内容(原始内容+盐[可选])
	 * @param algorithm 加密算法：MD5、SHA-1、SHA-256
	 * @return
	 */
	private static String encryptByAlgorithm(String content,String algorithm) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(algorithm);
			messageDigest.update(content.getBytes(StandardCharsets.UTF_8));
			return byte2HexString(messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.fillInStackTrace());
		}
	}
	
}
