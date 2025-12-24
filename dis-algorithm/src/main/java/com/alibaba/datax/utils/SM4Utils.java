package com.alibaba.datax.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;


/**
 * SM4国密加解密工具类
 * @author liudaizhong.liu
 * @date 2019年11月7日 下午3:54:16
 * @desc
 */
public class SM4Utils {
    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String ALGORITHM_NAME = "SM4";
    // 加密算法/分组加密模式/分组填充方式
    // PKCS5Padding-以8个字节为一组进行分组加密
    // 定义分组加密模式使用：PKCS5Padding
    private static final String ALGORITHM_NAME_GCM_PADDING = "SM4/GCM/NoPadding";
    // 128-32位16进制；256-64位16进制
    private static final int DEFAULT_KEY_SIZE = 128;

    public static final int DEFAULT_DATA_BLOCK_SIZE =  1024;

    public static final int ENCRYPT_DATA_BLOCK_SIZE = 2080;

    /**
     * @Description: 自动生成128位密钥
     * @Title: SM4Utils
     * @Package: com.audaque.cloud.dsps.desensitization.utils
     * @author: huafu.su
     * @Date: 2024/4/9 11:50
     * @return byte[]
     */
    public static byte[] generateKey() throws Exception {
        return generateKey(DEFAULT_KEY_SIZE);
    }

    /**
     * @param keySize 密钥位数
     * @Description: 生成密钥
     * @Title: SM4Utils
     * @Package: com.audaque.cloud.dsps.desensitization.utils
     * @author: huafu.su
     * @Date: 2024/4/9 11:51
     * @return byte[]
     */
    private static byte[] generateKey(int keySize) throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM_NAME, BouncyCastleProvider.PROVIDER_NAME);
        SecureRandom random = SecureRandomUtils.getInstance();
        kg.init(keySize, random);
        return kg.generateKey().getEncoded();
    }


    /**
     * @param hexKey  16进制密钥（忽略大小写）
     * @param paramStr  待加密字符串
     * @Description: sm4加密 加密模式：GCM; 密文长度不固定，会随着被加密字符串长度的变化而变化
     * @Title: SM4Utils
     * @Package: com.audaque.cloud.dsps.desensitization.utils
     * @author: huafu.su
     * @Date: 2024/4/9 11:53
     * @return java.lang.String 返回16进制的加密字符串
     */
    public static String encryptGcmKeyHexResultHex(String hexKey, String paramStr) throws Exception {
        String cipherText;
        // 16进制字符串-->byte[]
        byte[] keyData = DataConvertUtil.fromHexString(hexKey);
        // String-->byte[]
        byte[] srcData = paramStr.getBytes(StandardCharsets.UTF_8);
        // 加密后的数组
        byte[] cipherArray = encrypt_Gcm_Padding(keyData, srcData);
        // byte[]-->hexString
        cipherText = DataConvertUtil.toHexString(cipherArray);
        return cipherText;
    }

    public static byte[] encryptGcmKeyHex(String hexKey, byte[] srcData) throws Exception {
        String cipherText;
        // 16进制字符串-->byte[]
        byte[] keyData = DataConvertUtil.fromHexString(hexKey);
        // String-->byte[]
        //byte[] srcData = paramStr.getBytes(StandardCharsets.UTF_8);
        // 加密后的数组
        return encrypt_Gcm_Padding(keyData, srcData);
    }



    public static String encryptKey64Result64(String base64Key, String paramStr) throws Exception {
        String cipherText;
        // Base64String-->byte[]
        byte[] keyData = Base64Utils.decodeFromString(base64Key);
        // String-->byte[]
        byte[] srcData = paramStr.getBytes(StandardCharsets.UTF_8);
        // 加密后的数组
        byte[] cipherArray = encrypt_Gcm_Padding(keyData, srcData);
        // byte[]-->Base64String
        cipherText = Base64Utils.encodeToString(cipherArray);
        return cipherText;
    }


    /**
     * @param key 加密密钥
     * @param data 待加密数据
     * @Description: 加密模式之GCM
     * @Title: SM4Utils
     * @Package: com.audaque.cloud.dsps.desensitization.utils
     * @author: huafu.su
     * @Date: 2024/4/9 11:54
     * @return byte[]
     */
    private static byte[] encrypt_Gcm_Padding(byte[] key, byte[] data) throws Exception {
        Cipher cipher = generateCbcCipher(ALGORITHM_NAME_GCM_PADDING, Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    /**
     * @param hexKey  16进制密钥
     * @param cipherText 16进制的加密字符串（忽略大小写）
     * @Description: sm4解密;解密模式：采用GCM;
     * @Title: SM4Utils
     * @Package: com.audaque.cloud.dsps.desensitization.utils
     * @author: huafu.su
     * @Date: 2024/4/9 11:57
     * @return java.lang.String 解密后的字符串
     */
    public static String decryptGcmKeyHexValueHex(String hexKey, String cipherText) throws Exception {
        // 用于接收解密后的字符串
        String decryptStr;
        // hexString-->byte[]
        byte[] keyData = DataConvertUtil.fromHexString(hexKey);
        // hexString-->byte[]
        byte[] cipherData = DataConvertUtil.fromHexString(cipherText);
        // 解密
        byte[] srcData = decrypt_Gcm_Padding(keyData, cipherData);
        // byte[]-->String
        decryptStr = new String(srcData, StandardCharsets.UTF_8);
        return decryptStr;
    }

    public static String decryptKeyBase64Value64(String base64Key, String cipherText) throws Exception {
        // 用于接收解密后的字符串
        String decryptStr;
        // hexString-->byte[]
        //byte[] keyData = DataConvertUtil.fromHexString(hexKey);
        byte[] keyData = Base64Utils.decodeFromString(base64Key);
        // hexString-->byte[]
        //byte[] cipherData = DataConvertUtil.fromHexString(cipherText);
        byte[] cipherData = Base64Utils.decodeFromString(cipherText);
        // 解密
        byte[] srcData = decrypt_Gcm_Padding(keyData, cipherData);
        // byte[]-->String
        decryptStr = new String(srcData, StandardCharsets.UTF_8);
        return decryptStr;
    }

    public static byte[] decryptKeyHex(String hexKey, byte[] cipherData) throws Exception {
        // hexString-->byte[]
        byte[] keyData = DataConvertUtil.fromHexString(hexKey);
        // 解密
        return decrypt_Gcm_Padding(keyData, cipherData);
    }

    /**
     * @param key 密钥
     * @param cipherText 等待解密数据
     * @Description: 解密
     * @Title: SM4Utils
     * @Package: com.audaque.cloud.dsps.desensitization.utils
     * @author: huafu.su
     * @Date: 2024/4/9 11:56
     * @return byte[]
     */
    private static byte[] decrypt_Gcm_Padding(byte[] key, byte[] cipherText) throws Exception {
        Cipher cipher = generateCbcCipher(ALGORITHM_NAME_GCM_PADDING, Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(cipherText);
    }


    private static Cipher generateCbcCipher(String algorithmName, int mode, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
        Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
        cipher.init(mode, sm4Key, generateIV(key));
        return cipher;
    }

    //生成iv
    private static AlgorithmParameters generateIV(byte[] key) throws Exception {
        //iv 为一个 16 字节的数组，这里采用和 iOS 端一样的构造方法，数据全为0
//        byte[] iv = new byte[16];
//        Arrays.fill(iv, (byte) 0x00);
        AlgorithmParameters params = AlgorithmParameters.getInstance(ALGORITHM_NAME);
        params.init(new IvParameterSpec(key));
        return params;
    }

}
