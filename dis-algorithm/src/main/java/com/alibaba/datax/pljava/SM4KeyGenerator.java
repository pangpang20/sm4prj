package com.alibaba.datax.pljava;

import com.alibaba.datax.utils.DataConvertUtil;
import com.alibaba.datax.utils.SM4Utils;

/**
 * OpenGauss PL/Java SM4密钥生成函数
 * 
 * 使用方式:
 * 1. 部署jar包到OpenGauss
 * 2. 创建函数:
 * CREATE OR REPLACE FUNCTION sm4_generate_key()
 * RETURNS text
 * AS 'com.alibaba.datax.pljava.SM4KeyGenerator.generateKey'
 * LANGUAGE java;
 * 
 * 3. 调用:
 * SELECT sm4_generate_key();
 * 
 * @author generated
 * @date 2024-12-24
 */
public class SM4KeyGenerator {

    /**
     * 生成SM4密钥（16进制格式）
     * 
     * @return 32位16进制字符串密钥（对应128位密钥）
     * @throws Exception 生成失败时抛出异常
     */
    public static String generateKey() throws Exception {
        try {
            byte[] keyBytes = SM4Utils.generateKey();
            return DataConvertUtil.toHexString(keyBytes);
        } catch (Exception e) {
            throw new Exception("SM4密钥生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成SM4密钥（Base64格式）
     * 
     * @return Base64格式的密钥
     * @throws Exception 生成失败时抛出异常
     */
    public static String generateKeyBase64() throws Exception {
        try {
            byte[] keyBytes = SM4Utils.generateKey();
            return com.alibaba.datax.utils.Base64Utils.encodeToString(keyBytes);
        } catch (Exception e) {
            throw new Exception("SM4密钥生成失败: " + e.getMessage(), e);
        }
    }
}
