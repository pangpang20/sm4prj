package com.alibaba.datax.utils;



import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;


/** 
 * 3-DES加密工具类
* @author liudaizhong.liu
* @date 2019年11月7日 下午3:37:28 
* @desc 
*/
public class ThreeDESUtils {
	/**
         * 算法名称
     */
	private static final String KEY_ALGORITHM = "desede";
    /**
         * 算法名称/加密模式/填充方式
     * 
        String source = "abcdefgh"; // 无填充情况下，长度必须为8的倍数
        String key = "6C4E60E55552386C759569836DC0F83869836DC0F838C0F7";// 长度必须大于等于48
        String encryptData = ThreeDESUtil.tDesEncryptCBC(source, key);
        String decryptData = ThreeDESUtil.tDesDecryptCBC(encryptData, key);
     */
//    public static final String CIPHER_CBC_ALGORITHM = "desede/CBC/NoPadding";
    /**
     * '使用ECB填充模式，可以不用指定8位的数据源加密
     */
	private static String CIPHER_ECB_ALGORITHM = "";
    
    static{
		//Base64.encodeBytes("DESede/ECB/PKCS5Padding".getBytes());
		CIPHER_ECB_ALGORITHM = new String(Base64Utils.decode("REVTZWRlL0VDQi9QS0NTNVBhZGRpbmc=".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}
    
    
    /**
     * IvParameterSpec参数
     */
	private static final byte[] KEY_IV = {1, 2, 3, 4, 5, 6, 7, 8};
    
    /**
     * CBC加密
     * @param data 明文
     * @param key 密钥
     * @return Base64编码的密文
     */
//    public static String tDesEncryptCBC(String data, String key) throws Exception{
//        try {
//            // 添加一个安全提供者
//            Security.addProvider(new BouncyCastleProvider());
//            // 获得密钥
//            Key desKey = keyGenerator(key);
//            // 获取密码实例
//            Cipher cipher = Cipher.getInstance("desede/CBC/NoPadding");
//            IvParameterSpec ips = new IvParameterSpec(KEY_IV);
//            // 初始化密码
//            cipher.init(Cipher.ENCRYPT_MODE, desKey, ips);
//            // 执行加密
//            byte[] bytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
//            return new String(Base64Utils.encode(bytes), StandardCharsets.UTF_8);
//        } catch (InvalidKeyException e) {
//            throw ExceptionFactory.buildExceptionByI18nKey("dsps.module.algorithm.service.0004",e); // 无效KEY
//        } catch (NoSuchAlgorithmException e) {
//            throw ExceptionFactory.buildExceptionByI18nKey("dsps.module.algorithm.service.0006",e); // 无效算法名称
//        } catch (InvalidKeySpecException e) {
//            throw ExceptionFactory.buildExceptionByI18nKey("dsps.module.algorithm.service.0005",e); // 无效KeySpec
//        } catch (NoSuchPaddingException e) {
//            throw ExceptionFactory.buildExceptionByI18nKey("dsps.module.algorithm.service.0006",e); // 无效算法名称
//        } catch (IllegalBlockSizeException e) {
//            throw ExceptionFactory.buildExceptionByI18nKey("dsps.module.algorithm.service.0007",e); // 无效字节
//        } catch (BadPaddingException e) {
//            throw ExceptionFactory.buildExceptionByI18nKey("dsps.module.algorithm.service.0008",e); // 解析异常
//        } catch (InvalidAlgorithmParameterException e) {
//            throw ExceptionFactory.buildExceptionByI18nKey("摘要参数异常",e); // 摘要参数异常
//        }
//    }
    
    /**
     * CBC解密
     * @param data Base64编码的密文
     * @param key 密钥
     * @return
     */
//    public static String tDesDecryptCBC(String data, String key) throws Exception{
//        try {
//            Key desKey = keyGenerator(key);
//            Cipher cipher = Cipher.getInstance(CIPHER_CBC_ALGORITHM);
//            IvParameterSpec ips = new IvParameterSpec(KEY_IV);
//            cipher.init(Cipher.DECRYPT_MODE, desKey, ips);
//            byte[] bytes = cipher.doFinal(Base64.decode(data));
//            return new String(bytes, StandardCharsets.UTF_8);
//        } catch (InvalidKeyException e) {
//            throw ExceptionFactory.buildExceptionByI18nKey("dsps.module.algorithm.service.0004",e); // 无效KEY
//        } catch (NoSuchAlgorithmException e) {
//            throw ExceptionFactory.buildExceptionByI18nKey("dsps.module.algorithm.service.0006",e); // 无效算法名称
//        } catch (InvalidKeySpecException e) {
//            throw ExceptionFactory.buildExceptionByI18nKey("dsps.module.algorithm.service.0005",e); // 无效KeySpec
//        } catch (NoSuchPaddingException e) {
//            throw ExceptionFactory.buildExceptionByI18nKey("dsps.module.algorithm.service.0006",e); // 无效算法名称
//        } catch (IllegalBlockSizeException e) {
//            throw ExceptionFactory.buildExceptionByI18nKey("dsps.module.algorithm.service.0007",e); // 无效字节
//        } catch (BadPaddingException e) {
//            throw ExceptionFactory.buildExceptionByI18nKey("dsps.module.algorithm.service.0008",e); // 解析异常
//        } catch (InvalidAlgorithmParameterException e) {
//            throw ExceptionFactory.buildExceptionByI18nKey("摘要参数异常",e); // 摘要参数异常
//        }
//    }
    /**
     * ECB加密
     * @param data 明文
     * @param key 密钥
     * @return Base64编码的密文
     */
    public static String tDesEncryptECB(String data, String key) throws Exception{
    	try {
    		// 添加一个安全提供者
    		Security.addProvider(new BouncyCastleProvider());
    		// 获得密钥
    		Key desKey = keyGenerator(key);
    		// 获取密码实例
    		Cipher cipher = Cipher.getInstance(CIPHER_ECB_ALGORITHM);
    		// 初始化密码
    		cipher.init(Cipher.ENCRYPT_MODE, desKey);
    		// 执行加密
    		byte[] bytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
			return new String(Base64Utils.encode(bytes), StandardCharsets.UTF_8);
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
     * CBC解密
     * @param data Base64编码的密文
     * @param key 密钥
     * @return
     */
    public static String tDesDecryptECB(String data, String key) throws Exception{
    	try {
    		Key desKey = keyGenerator(key);
    		Cipher cipher = Cipher.getInstance(CIPHER_ECB_ALGORITHM);
    		cipher.init(Cipher.DECRYPT_MODE, desKey);
			byte[] bytes = cipher.doFinal(Base64Utils.decode(data.getBytes(StandardCharsets.UTF_8)));
    		return new String(bytes, StandardCharsets.UTF_8);
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
     * @throws InvalidKeyException 无效的key
     * @throws NoSuchAlgorithmException 算法名称未发现
     * @throws InvalidKeySpecException 无效的KeySpec
     */
    private static Key keyGenerator(String key) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] input = hexString2Bytes(key);
        DESedeKeySpec desKey = new DESedeKeySpec(input);
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
        byte[] b = new byte[key.length()/2];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            char c0 = key.charAt(j++);
            char c1 = key.charAt(j++);
            // c0做b[i]的高字节，c1做低字节
            b[i] = (byte) ((parse(c0)<<4)|parse(c1));
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
