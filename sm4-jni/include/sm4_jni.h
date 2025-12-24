#ifndef SM4_JNI_H
#define SM4_JNI_H

#include <jni.h>

/**
 * 初始化JVM环境
 * @param jar_path dis-algorithm jar包路径
 * @return 0成功，-1失败
 */
int sm4_jni_init(const char *jar_path);

/**
 * 销毁JVM环境
 */
void sm4_jni_destroy();

/**
 * SM4密钥生成（16进制格式）
 * @param key_out 输出缓冲区，至少33字节（32位hex + '\0'）
 * @param key_size 输出缓冲区大小
 * @return 0成功，-1失败
 */
int sm4_generate_key_hex(char *key_out, int key_size);

/**
 * SM4加密 - 16进制密钥和结果
 * @param plain_text 明文
 * @param hex_key 16进制密钥（32位字符）
 * @param cipher_out 密文输出缓冲区
 * @param cipher_size 输出缓冲区大小
 * @return 0成功，-1失败
 */
int sm4_encrypt_hex(const char *plain_text, const char *hex_key, 
                    char *cipher_out, int cipher_size);

/**
 * SM4解密 - 16进制密钥和密文
 * @param cipher_text 16进制密文
 * @param hex_key 16进制密钥（32位字符）
 * @param plain_out 明文输出缓冲区
 * @param plain_size 输出缓冲区大小
 * @return 0成功，-1失败
 */
int sm4_decrypt_hex(const char *cipher_text, const char *hex_key,
                    char *plain_out, int plain_size);

/**
 * SM4加密 - Base64密钥和结果
 * @param plain_text 明文
 * @param base64_key Base64密钥
 * @param cipher_out Base64密文输出缓冲区
 * @param cipher_size 输出缓冲区大小
 * @return 0成功，-1失败
 */
int sm4_encrypt_base64(const char *plain_text, const char *base64_key,
                       char *cipher_out, int cipher_size);

/**
 * SM4解密 - Base64密钥和密文
 * @param cipher_text Base64密文
 * @param base64_key Base64密钥
 * @param plain_out 明文输出缓冲区
 * @param plain_size 输出缓冲区大小
 * @return 0成功，-1失败
 */
int sm4_decrypt_base64(const char *cipher_text, const char *base64_key,
                       char *plain_out, int plain_size);

/**
 * 获取最后的错误信息
 * @return 错误信息字符串
 */
const char* sm4_get_last_error();

#endif /* SM4_JNI_H */
