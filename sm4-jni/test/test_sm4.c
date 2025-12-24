#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "sm4_wrapper.h"
#include "sm4_jni.h"

void test_basic_encryption() {
    printf("\n=== Test 1: Basic Encryption/Decryption ===\n");
    
    // 生成密钥
    char *key = sm4_new_key();
    if (key == NULL) {
        printf("FAILED: Could not generate key\n");
        return;
    }
    printf("Generated Key: %s\n", key);
    
    // 测试加密
    const char *original = "Hello SM4 from C!";
    printf("Original Text: %s\n", original);
    
    char *encrypted = sm4_encrypt(original, key);
    if (encrypted == NULL) {
        printf("FAILED: Encryption failed\n");
        return;
    }
    printf("Encrypted: %s\n", encrypted);
    
    // 测试解密
    char *decrypted = sm4_decrypt(encrypted, key);
    if (decrypted == NULL) {
        printf("FAILED: Decryption failed\n");
        return;
    }
    printf("Decrypted: %s\n", decrypted);
    
    // 验证结果
    if (strcmp(original, decrypted) == 0) {
        printf("PASSED: Encryption/Decryption successful!\n");
    } else {
        printf("FAILED: Decrypted text doesn't match original\n");
    }
}

void test_chinese_text() {
    printf("\n=== Test 2: Chinese Text Encryption ===\n");
    
    char *key = sm4_new_key();
    const char *chinese = "中文加密测试";
    printf("Original: %s\n", chinese);
    
    char *encrypted = sm4_encrypt(chinese, key);
    if (encrypted == NULL) {
        printf("FAILED: Encryption failed\n");
        return;
    }
    printf("Encrypted: %s\n", encrypted);
    
    char *decrypted = sm4_decrypt(encrypted, key);
    if (decrypted == NULL) {
        printf("FAILED: Decryption failed\n");
        return;
    }
    printf("Decrypted: %s\n", decrypted);
    
    if (strcmp(chinese, decrypted) == 0) {
        printf("PASSED: Chinese text encryption successful!\n");
    } else {
        printf("FAILED: Decrypted text doesn't match\n");
    }
}

void test_long_text() {
    printf("\n=== Test 3: Long Text Encryption ===\n");
    
    char *key = sm4_new_key();
    const char *long_text = "This is a longer text to test SM4 encryption. "
                           "It contains multiple sentences and should be encrypted "
                           "and decrypted correctly. SM4 is a Chinese national standard "
                           "block cipher algorithm used for data encryption.";
    
    printf("Original length: %zu bytes\n", strlen(long_text));
    
    char *encrypted = sm4_encrypt(long_text, key);
    if (encrypted == NULL) {
        printf("FAILED: Encryption failed\n");
        return;
    }
    printf("Encrypted length: %zu bytes\n", strlen(encrypted));
    
    char *decrypted = sm4_decrypt(encrypted, key);
    if (decrypted == NULL) {
        printf("FAILED: Decryption failed\n");
        return;
    }
    
    if (strcmp(long_text, decrypted) == 0) {
        printf("PASSED: Long text encryption successful!\n");
    } else {
        printf("FAILED: Decrypted text doesn't match\n");
    }
}

void test_special_characters() {
    printf("\n=== Test 4: Special Characters ===\n");
    
    char *key = sm4_new_key();
    const char *special = "!@#$%^&*()_+-={}[]|\\:\";<>?,./";
    printf("Original: %s\n", special);
    
    char *encrypted = sm4_encrypt(special, key);
    if (encrypted == NULL) {
        printf("FAILED: Encryption failed\n");
        return;
    }
    
    char *decrypted = sm4_decrypt(encrypted, key);
    if (decrypted == NULL) {
        printf("FAILED: Decryption failed\n");
        return;
    }
    
    if (strcmp(special, decrypted) == 0) {
        printf("PASSED: Special characters encryption successful!\n");
    } else {
        printf("FAILED: Decrypted text doesn't match\n");
    }
}

void test_empty_and_null() {
    printf("\n=== Test 5: Edge Cases (Empty/NULL) ===\n");
    
    char *key = sm4_new_key();
    
    // 空字符串
    const char *empty = "";
    char *encrypted = sm4_encrypt(empty, key);
    if (encrypted != NULL) {
        char *decrypted = sm4_decrypt(encrypted, key);
        if (decrypted != NULL && strcmp(empty, decrypted) == 0) {
            printf("PASSED: Empty string handled correctly\n");
        } else {
            printf("FAILED: Empty string decryption failed\n");
        }
    } else {
        printf("FAILED: Empty string encryption failed\n");
    }
}

void test_low_level_api() {
    printf("\n=== Test 6: Low-level API Test ===\n");
    
    char key[64];
    char cipher[4096];
    char plain[4096];
    
    // 生成密钥
    if (sm4_generate_key_hex(key, sizeof(key)) != 0) {
        printf("FAILED: Key generation failed: %s\n", sm4_get_last_error());
        return;
    }
    printf("Generated key: %s\n", key);
    
    // 加密
    const char *text = "Low-level API test";
    if (sm4_encrypt_hex(text, key, cipher, sizeof(cipher)) != 0) {
        printf("FAILED: Encryption failed: %s\n", sm4_get_last_error());
        return;
    }
    printf("Encrypted: %s\n", cipher);
    
    // 解密
    if (sm4_decrypt_hex(cipher, key, plain, sizeof(plain)) != 0) {
        printf("FAILED: Decryption failed: %s\n", sm4_get_last_error());
        return;
    }
    printf("Decrypted: %s\n", plain);
    
    if (strcmp(text, plain) == 0) {
        printf("PASSED: Low-level API works correctly!\n");
    } else {
        printf("FAILED: Text mismatch\n");
    }
}

int main(int argc, char *argv[]) {
    printf("========================================\n");
    printf("SM4 JNI C Wrapper Test Suite\n");
    printf("========================================\n");
    
    // 初始化环境
    if (sm4_init() != 0) {
        fprintf(stderr, "Failed to initialize SM4 environment\n");
        return 1;
    }
    printf("SM4 environment initialized successfully\n");
    
    // 运行测试
    test_basic_encryption();
    test_chinese_text();
    test_long_text();
    test_special_characters();
    test_empty_and_null();
    test_low_level_api();
    
    // 清理
    sm4_cleanup();
    printf("\n========================================\n");
    printf("All tests completed!\n");
    printf("========================================\n");
    
    return 0;
}
