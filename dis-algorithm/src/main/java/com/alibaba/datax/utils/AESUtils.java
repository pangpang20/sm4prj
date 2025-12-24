package com.alibaba.datax.utils;

import javax.crypto.KeyGenerator;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;



/**
 * AES加解密工具类
 * @author daizhong.liu
 * @date 2019年11月7日 下午3:32:42 
 * @desc
 */
public class AESUtils {
    /**
     * 算法名称
     */
    private static final String KEY_ALGORITHM = "AES";
    /**
     * 算法
     */
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    /**
     * 默认生成密钥位数，128位生成16进制密钥
     */
    private static final int DEFAULT_KEY_SIZE = 128;

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
    /**
     * 加密数据
     *
     * @param data 待加密内容
     * @param key  加密的密钥
     * @return 加密后的数据
     */
    public static String encrypt(String data, String key) throws Exception{
        try {
            SecureRandom sr = SecureRandomUtils.getInstance();
            // 获得密钥
            Key deskey = keyGenerator(key);
            // 实例化一个密码对象
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec spec = new IvParameterSpec(deskey.getEncoded());
            // 密码初始化
            cipher.init(Cipher.ENCRYPT_MODE, deskey, spec, sr);
            // 执行加密
            byte[] bytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            // 返回Base64编码后的字符串
            return new String(Base64Utils.encode(bytes), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("dsps.module.algorithm.service.0002", e); // 获取加密名称异常
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("dsps.module.algorithm.service.0003", e); // 未知编码格式
        } catch (InvalidKeyException e) {
            throw new RuntimeException("dsps.module.algorithm.service.0004", e); // 无效Key
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException("dsps.module.algorithm.service.0010", e); // 无效密码算法
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException("dsps.module.algorithm.service.0007", e); // 无效字节
        } catch (BadPaddingException e) {
            throw new RuntimeException("dsps.module.algorithm.service.0008", e); // 解析异常
        }
    }

    /**
     * 解密数据
     *
     * @param data 待解密的内容
     * @param key  解密的密钥
     * @return 解密后的文字
     */
    public static String decrypt(String data, String key) throws Exception{
        try {
            SecureRandom sr = SecureRandomUtils.getInstance();
            // 生成密钥
            Key kGen = keyGenerator(key);
            // 实例化密码对象
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec spec = new IvParameterSpec(kGen.getEncoded());
            // 初始化密码对象
            cipher.init(Cipher.DECRYPT_MODE, kGen, spec, sr);
            // 执行解密
            byte[] bytes = cipher.doFinal(Base64Utils.decode(data.getBytes(StandardCharsets.UTF_8)));
            // 返回解密后的字符串
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("dsps.module.algorithm.service.0002", e); // 获取加密名称异常
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("dsps.module.algorithm.service.0003", e); // 未知编码格式
        } catch (InvalidKeyException e) {
            throw new RuntimeException("dsps.module.algorithm.service.0004", e); // 无效Key
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException("dsps.module.algorithm.service.0010", e); // 无效密码算法
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException("dsps.module.algorithm.service.0007", e); // 无效字节
        } catch (BadPaddingException e) {
            throw new RuntimeException("dsps.module.algorithm.service.0008", e); // 解析异常
        }
    }

    /**
     * 获取密钥
     *
     * @param key 密钥字符串
     * @return 返回一个密钥
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private static Key keyGenerator(String key) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        KeyGenerator kGen = KeyGenerator.getInstance(KEY_ALGORITHM);
        kGen.init(128, new SecureRandom(hexString2Bytes(key)));
        SecretKey secretKey = kGen.generateKey();
        byte[] encoded = secretKey.getEncoded();
        return new SecretKeySpec(encoded, KEY_ALGORITHM);
    }


    /**
     * 从十六进制字符串到字节数组转化
     *
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
     *
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
