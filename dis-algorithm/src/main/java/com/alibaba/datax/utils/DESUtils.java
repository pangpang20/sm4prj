package com.alibaba.datax.utils;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;


/**
 * DES加解密工具类
 * @author liudaizhong.liu
 * @date 2019年11月7日 下午3:19:47
 * @desc
 */
public class DESUtils {
	/**
	 * 算法名称
	 */
	private static final String KEY_ALGORITHM = "DES";
	/**
         *  算法名称/加密模式/填充方式
     * DES的四种工作模式：ECB(电子密码本)、CBC(加密分组链接)、
     * CFB(加密反馈模式)、OFB(输出反馈)
          * 当前无填充的情况：
          *      加密数据必须为8的倍数，密钥输入必须为16的倍数
          *  使用
        String source = "abcdefgh";// 为8位的倍数
        String key = "A1B2C3D4E5F60708";// 为16位的倍数
        
        String encryptData = DESUtil.encrypt(source, key);

        String decryptData = DESUtil.decrypt(encryptData, key);
       */
//	public static final String CIPHER_ALGORITHM_DES_ECB_NOPADDING = "DES/ECB/NoPadding";
	/**
     * DES/CBC/PKCS5Padding
     * PKCS5Padding填充时:
         *      加密数据无位数控制，密钥输入必须为16的倍数
         *  使用
        String source = "abcdefgh";// 无需控制位数
        String key = "A1B2C3D4E5F60708";// 为16位的倍数
        
        String encryptData = DESUtil.encrypt(source, key);

        String decryptData = DESUtil.decrypt(encryptData, key);

     */
	private static String CIPHER_ALGORITHM_DES_ECB_PKCS5_PADDING = "";

	static{
		//Base64.encodeBytes("DES/ECB/PKCS5Padding".getBytes());
		CIPHER_ALGORITHM_DES_ECB_PKCS5_PADDING = new String(Base64Utils.decode("REVTL0VDQi9QS0NTNVBhZGRpbmc=".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}
	 
	/**
	 * 加密数据
	 *
	 * @param data 待加密的数据--8位
	 * @param key  密钥--16位
	 * @return 加密后的数据
	 */
	public static String encrypt(String data, String key) throws Exception{
		try {
			// 获得一个密钥
			Key deskey = keyGenerator(key);
			// 实例化一个Cipher(密码)对象，用于完成加密操作
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_DES_ECB_PKCS5_PADDING);
			SecureRandom random = SecureRandomUtils.getInstance();
			// 初始化Cipher对象，设置为加密模式
			cipher.init(Cipher.ENCRYPT_MODE, deskey, random);
			byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
			// 执行加密操作
			byte[] results = cipher.doFinal(bytes);
			return new String(Base64Utils.encode(results), StandardCharsets.UTF_8);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("dsps.module.algorithm.service.0004", e); // 无效KEY
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("dsps.module.algorithm.service.0006", e); // 无效算法名称
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException("dsps.module.algorithm.service.0005", e); // 无效KeySpec
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException("dsps.module.algorithm.service.0006", e); // 无效算法名称
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException("dsps.module.algorithm.service.0007", e); // 无效字节
		} catch (BadPaddingException e) {
			throw new RuntimeException("dsps.module.algorithm.service.0008", e); // 解析异常
		}
	}

	/**
	 * 解密数据
	 * @param data 待解密数据
	 * @param key  密钥
	 * @return 解密后的数据
	 */
	public static String decrypt(String data, String key)  throws Exception{
		try {
			Key desKey = keyGenerator(key);
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_DES_ECB_PKCS5_PADDING);
			// 初始化Cipher对象，设置为解密模式
			cipher.init(Cipher.DECRYPT_MODE, desKey);
			// 执行解密操作
			byte[] decBytes = cipher.doFinal(Base64Utils.decode(data.getBytes(StandardCharsets.UTF_8)));
			return new String(decBytes, StandardCharsets.UTF_8);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("dsps.module.algorithm.service.0004", e); // 无效KEY
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("dsps.module.algorithm.service.0006", e); // 无效算法名称
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException("dsps.module.algorithm.service.0005", e); // 无效KeySpec
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException("dsps.module.algorithm.service.0006", e); // 无效算法名称
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException("dsps.module.algorithm.service.0007", e); // 无效字节
		} catch (BadPaddingException e) {
			throw new RuntimeException("dsps.module.algorithm.service.0008", e); // 解析异常
		}
	}

	/**
	 * 生成密钥key对象
	 * @param key 密钥字符串
	 * @return 密钥对象
	 * @throws InvalidKeyException      无效的key
	 * @throws NoSuchAlgorithmException 算法名称未发现
	 * @throws InvalidKeySpecException  无效的KeySpec
	 */
	private static Key keyGenerator(String key)
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] input = hexString2Bytes(key);
		DESKeySpec desKey = new DESKeySpec(input);
		// 创建一个密钥工厂，然后用它把DESKeySpec转化
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
		// 获得一个密钥
		SecretKey secretKey = keyFactory.generateSecret(desKey);
		return secretKey;
	}

	/**
	 * 从十六进制字符串到字节数组转化
	 * @param key 密钥
	 */
	private static byte[] hexString2Bytes(String key) {
		byte[] b = new byte[key.length() / 2];
		int j = 0;
		for (int i = 0; i < b.length; i++) {
			char c0 = key.charAt(j++);
			char c1 = key.charAt(j++);
			// c0做b[i]的高字节，c1做低字节
			b[i] = (byte) ((parse(c0) << 4) | parse(c1));
		}
		return b;
	}

	/**
	 * 将字符转换为int值
	 * @param c 要转化的字符
	 * @return ASCII码值
	 */
	private static int parse(char c) {
		if (c >= 'a') {
			return (c - 'a' + 10) & 0x0f;
		}
		if (c >= 'A') {
			return (c - 'A' + 10) & 0x0f;
		}
		return (c - '0') & 0x0f;
	}
	

}
