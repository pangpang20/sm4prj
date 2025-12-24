package com.alibaba.datax.pljava;

import com.alibaba.datax.utils.SM4Utils;

/**
 * OpenGauss PL/Java SM4解密函数
 * 
 * 使用方式:
 * 1. 部署jar包到OpenGauss
 * 2. 创建函数:
 * CREATE OR REPLACE FUNCTION sm4_decrypt(text, text)
 * RETURNS text
 * AS 'com.alibaba.datax.pljava.SM4Decrypt.decrypt'
 * LANGUAGE java;
 * 
 * 3. 调用:
 * SELECT sm4_decrypt('16进制密文', '16进制密钥');
 * 
 * @author generated
 * @date 2024-12-24
 */
public class SM4Decrypt {

    /**
     * SM4解密 - 使用16进制密钥和密文
     * 
     * @param cipherText 16进制格式的密文
     * @param hexKey     16进制格式的密钥（32位16进制字符串，对应128位密钥）
     * @return 解密后的明文
     * @throws Exception 解密失败时抛出异常
     */
    public static String decrypt(String cipherText, String hexKey) throws Exception {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }

        if (hexKey == null || hexKey.isEmpty()) {
            throw new IllegalArgumentException("密钥不能为空");
        }

        try {
            return SM4Utils.decryptGcmKeyHexValueHex(hexKey, cipherText);
        } catch (Exception e) {
            throw new Exception("SM4解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * SM4解密 - 使用Base64密钥和密文（备用方法）
     * 
     * @param cipherText Base64格式的密文
     * @param base64Key  Base64格式的密钥
     * @return 解密后的明文
     * @throws Exception 解密失败时抛出异常
     */
    public static String decryptBase64(String cipherText, String base64Key) throws Exception {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }

        if (base64Key == null || base64Key.isEmpty()) {
            throw new IllegalArgumentException("密钥不能为空");
        }

        try {
            return SM4Utils.decryptKeyBase64Value64(base64Key, cipherText);
        } catch (Exception e) {
            throw new Exception("SM4解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量解密 - 用于批量处理
     * 
     * @param cipherTexts 待解密的密文数组
     * @param hexKey      16进制格式的密钥
     * @return 解密后的明文数组
     * @throws Exception 解密失败时抛出异常
     */
    public static String[] decryptBatch(String[] cipherTexts, String hexKey) throws Exception {
        if (cipherTexts == null || cipherTexts.length == 0) {
            return cipherTexts;
        }

        if (hexKey == null || hexKey.isEmpty()) {
            throw new IllegalArgumentException("密钥不能为空");
        }

        String[] results = new String[cipherTexts.length];
        for (int i = 0; i < cipherTexts.length; i++) {
            results[i] = decrypt(cipherTexts[i], hexKey);
        }

        return results;
    }
}
