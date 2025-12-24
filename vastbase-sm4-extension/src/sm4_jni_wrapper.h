#ifndef SM4_JNI_WRAPPER_H
#define SM4_JNI_WRAPPER_H

#include <jni.h>

/**
 * JNI包装层 - 用于PostgreSQL C扩展调用Java SM4实现
 * 这是一个简化版本，专门为PostgreSQL扩展设计
 */

/**
 * 初始化JVM（如果尚未初始化）
 * 注意：在PostgreSQL环境中，JVM应该只初始化一次
 * @param jar_path dis-algorithm jar包路径
 * @return 0成功，-1失败
 */
int sm4_jni_init_if_needed(const char *jar_path);

/**
 * 获取JNI环境
 * 用于在PostgreSQL后端进程中获取当前线程的JNI环境
 * @return JNI环境指针，失败返回NULL
 */
JNIEnv* sm4_jni_get_env(void);

/**
 * SM4密钥生成
 * @param key_out 输出缓冲区（至少33字节）
 * @param key_size 缓冲区大小
 * @return 0成功，-1失败
 */
int sm4_jni_generate_key(char *key_out, int key_size);

/**
 * SM4加密 - 16进制
 * @param plain_text 明文
 * @param hex_key 16进制密钥
 * @param cipher_out 密文输出缓冲区
 * @param cipher_size 缓冲区大小
 * @return 0成功，-1失败
 */
int sm4_jni_encrypt_hex(const char *plain_text, const char *hex_key,
                        char *cipher_out, int cipher_size);

/**
 * SM4解密 - 16进制
 * @param cipher_text 16进制密文
 * @param hex_key 16进制密钥
 * @param plain_out 明文输出缓冲区
 * @param plain_size 缓冲区大小
 * @return 0成功，-1失败
 */
int sm4_jni_decrypt_hex(const char *cipher_text, const char *hex_key,
                        char *plain_out, int plain_size);

/**
 * SM4加密 - Base64
 * @param plain_text 明文
 * @param base64_key Base64密钥
 * @param cipher_out 密文输出缓冲区
 * @param cipher_size 缓冲区大小
 * @return 0成功，-1失败
 */
int sm4_jni_encrypt_base64(const char *plain_text, const char *base64_key,
                           char *cipher_out, int cipher_size);

/**
 * SM4解密 - Base64
 * @param cipher_text Base64密文
 * @param base64_key Base64密钥
 * @param plain_out 明文输出缓冲区
 * @param plain_size 缓冲区大小
 * @return 0成功，-1失败
 */
int sm4_jni_decrypt_base64(const char *cipher_text, const char *base64_key,
                           char *plain_out, int plain_size);

/**
 * 获取最后的错误信息
 * @return 错误信息字符串
 */
const char* sm4_jni_get_error(void);

/**
 * 清理JVM（谨慎使用）
 * 注意：在PostgreSQL中通常不需要调用，因为JVM应该在进程生命周期内保持
 */
void sm4_jni_cleanup(void);

#endif /* SM4_JNI_WRAPPER_H */
