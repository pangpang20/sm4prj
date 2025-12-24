#ifndef SM4_WRAPPER_H
#define SM4_WRAPPER_H

/**
 * SM4 简化封装接口
 * 提供更简单易用的C API
 */

/**
 * 初始化SM4环境（自动查找jar包）
 * @return 0成功，-1失败
 */
int sm4_init();

/**
 * 清理SM4环境
 */
void sm4_cleanup();

/**
 * 生成新的SM4密钥
 * @return 密钥字符串（静态缓冲区），失败返回NULL
 */
char* sm4_new_key();

/**
 * 加密文本
 * @param text 明文
 * @param key 密钥（16进制格式）
 * @return 密文（静态缓冲区），失败返回NULL
 */
char* sm4_encrypt(const char *text, const char *key);

/**
 * 解密文本
 * @param cipher 密文（16进制格式）
 * @param key 密钥（16进制格式）
 * @return 明文（静态缓冲区），失败返回NULL
 */
char* sm4_decrypt(const char *cipher, const char *key);

#endif /* SM4_WRAPPER_H */
