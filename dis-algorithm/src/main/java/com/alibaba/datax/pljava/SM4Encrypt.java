package com.alibaba.datax.pljava;

import com.alibaba.datax.utils.SM4Utils;

/**
 * OpenGauss PL/Java SM4加密函数
 * 
 * 使用方式:
 * 1. 部署jar包到OpenGauss
 * 2. 创建函数:
 * CREATE OR REPLACE FUNCTION sm4_encrypt(text, text)
 * RETURNS text
 * AS 'com.alibaba.datax.pljava.SM4Encrypt.encrypt'
 * LANGUAGE java;
 * 
 * 3. 调用:
 * SELECT sm4_encrypt('明文数据', '16进制密钥');
 * 
 * @author generated
 * @date 2024-12-24
 */
public class SM4Encrypt {

    /**
     * SM4加密 - 使用16进制密钥和结果
     * 
     * @param plainText 待加密的明文
     * @param hexKey    16进制格式的密钥（32位16进制字符串，对应128位密钥）
     * @return 16进制格式的密文
     * @throws Exception 加密失败时抛出异常
     */
    public static String encrypt(String plainText, String hexKey) throws Exception {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        if (hexKey == null || hexKey.isEmpty()) {
            throw new IllegalArgumentException("密钥不能为空");
        }

        try {
            return SM4Utils.encryptGcmKeyHexResultHex(hexKey, plainText);
        } catch (Exception e) {
            throw new Exception("SM4加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * SM4加密 - 使用Base64密钥和结果（备用方法）
     * 
     * @param plainText 待加密的明文
     * @param base64Key Base64格式的密钥
     * @return Base64格式的密文
     * @throws Exception 加密失败时抛出异常
     */
    public static String encryptBase64(String plainText, String base64Key) throws Exception {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        if (base64Key == null || base64Key.isEmpty()) {
            throw new IllegalArgumentException("密钥不能为空");
        }

        try {
            return SM4Utils.encryptKey64Result64(base64Key, plainText);
        } catch (Exception e) {
            throw new Exception("SM4加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量加密 - 用于批量处理
     * 
     * @param plainTexts 待加密的明文数组
     * @param hexKey     16进制格式的密钥
     * @return 加密后的密文数组
     * @throws Exception 加密失败时抛出异常
     */
    public static String[] encryptBatch(String[] plainTexts, String hexKey) throws Exception {
        if (plainTexts == null || plainTexts.length == 0) {
            return plainTexts;
        }

        if (hexKey == null || hexKey.isEmpty()) {
            throw new IllegalArgumentException("密钥不能为空");
        }

        String[] results = new String[plainTexts.length];
        for (int i = 0; i < plainTexts.length; i++) {
            results[i] = encrypt(plainTexts[i], hexKey);
        }

        return results;
    }
}
