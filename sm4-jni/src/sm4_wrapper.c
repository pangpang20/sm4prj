#include "sm4_jni.h"
#include <stdio.h>
#include <string.h>

/**
 * SM4 C语言封装层
 * 提供更友好的C API接口
 */

// 全局初始化标志
static int initialized = 0;

/**
 * 简化的初始化函数 - 自动查找jar包
 */
int sm4_init() {
    if (initialized) {
        return 0;
    }
    
    // 默认jar路径（相对于项目根目录）
    const char *jar_path = "../dis-algorithm/target/dis-algorithm-1.0.0.0.jar";
    
    if (sm4_jni_init(jar_path) != 0) {
        fprintf(stderr, "SM4 initialization failed: %s\n", sm4_get_last_error());
        return -1;
    }
    
    initialized = 1;
    return 0;
}

/**
 * 清理资源
 */
void sm4_cleanup() {
    if (initialized) {
        sm4_jni_destroy();
        initialized = 0;
    }
}

/**
 * 简化的密钥生成
 */
char* sm4_new_key() {
    static char key[64];
    
    if (!initialized && sm4_init() != 0) {
        return NULL;
    }
    
    if (sm4_generate_key_hex(key, sizeof(key)) != 0) {
        fprintf(stderr, "Key generation failed: %s\n", sm4_get_last_error());
        return NULL;
    }
    
    return key;
}

/**
 * 简化的加密接口
 */
char* sm4_encrypt(const char *text, const char *key) {
    static char cipher[4096];
    
    if (!initialized && sm4_init() != 0) {
        return NULL;
    }
    
    if (sm4_encrypt_hex(text, key, cipher, sizeof(cipher)) != 0) {
        fprintf(stderr, "Encryption failed: %s\n", sm4_get_last_error());
        return NULL;
    }
    
    return cipher;
}

/**
 * 简化的解密接口
 */
char* sm4_decrypt(const char *cipher, const char *key) {
    static char plain[4096];
    
    if (!initialized && sm4_init() != 0) {
        return NULL;
    }
    
    if (sm4_decrypt_hex(cipher, key, plain, sizeof(plain)) != 0) {
        fprintf(stderr, "Decryption failed: %s\n", sm4_get_last_error());
        return NULL;
    }
    
    return plain;
}
