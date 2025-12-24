#include <stdio.h>
#include <string.h>
#include "sm4_wrapper.h"

/**
 * SM4加密使用示例
 * 演示如何在C程序中使用SM4加密功能
 */

int main() {
    printf("==============================================\n");
    printf("SM4 Encryption Example - C调用Java加密库\n");
    printf("==============================================\n\n");
    
    // 步骤1: 初始化SM4环境
    printf("步骤1: 初始化SM4环境...\n");
    if (sm4_init() != 0) {
        fprintf(stderr, "错误: 初始化失败！\n");
        fprintf(stderr, "请确保:\n");
        fprintf(stderr, "  1. JAVA_HOME环境变量已设置\n");
        fprintf(stderr, "  2. dis-algorithm jar包已编译\n");
        fprintf(stderr, "  3. PATH包含JVM动态库路径\n");
        return 1;
    }
    printf("✓ 初始化成功！\n\n");
    
    // 步骤2: 生成加密密钥
    printf("步骤2: 生成SM4密钥...\n");
    char *key = sm4_new_key();
    if (key == NULL) {
        fprintf(stderr, "错误: 密钥生成失败！\n");
        sm4_cleanup();
        return 1;
    }
    printf("✓ 密钥生成成功: %s\n", key);
    printf("  (这是一个32位16进制字符串，表示128位密钥)\n\n");
    
    // 步骤3: 加密数据
    printf("步骤3: 加密数据...\n");
    const char *sensitive_data = "这是敏感数据: 身份证号123456789012345678";
    printf("  原始数据: %s\n", sensitive_data);
    
    char *encrypted = sm4_encrypt(sensitive_data, key);
    if (encrypted == NULL) {
        fprintf(stderr, "错误: 加密失败！\n");
        sm4_cleanup();
        return 1;
    }
    printf("✓ 加密成功！\n");
    printf("  加密数据: %s\n", encrypted);
    printf("  (加密后的数据是16进制格式)\n\n");
    
    // 步骤4: 解密数据
    printf("步骤4: 解密数据...\n");
    char *decrypted = sm4_decrypt(encrypted, key);
    if (decrypted == NULL) {
        fprintf(stderr, "错误: 解密失败！\n");
        sm4_cleanup();
        return 1;
    }
    printf("✓ 解密成功！\n");
    printf("  解密数据: %s\n", decrypted);
    
    // 步骤5: 验证结果
    printf("\n步骤5: 验证加解密结果...\n");
    if (strcmp(sensitive_data, decrypted) == 0) {
        printf("✓ 验证成功！原始数据与解密数据完全一致！\n");
    } else {
        printf("✗ 验证失败！数据不一致！\n");
    }
    
    // 步骤6: 演示多次加密（相同明文加密结果不同）
    printf("\n步骤6: 演示SM4-GCM模式特性...\n");
    printf("  (相同的明文每次加密结果都不同，但都能正确解密)\n");
    
    for (int i = 1; i <= 3; i++) {
        char *enc = sm4_encrypt("测试数据", key);
        printf("  第%d次加密: %s\n", i, enc);
    }
    
    // 清理环境
    printf("\n清理环境...\n");
    sm4_cleanup();
    printf("✓ 程序执行完成！\n");
    
    printf("\n==============================================\n");
    printf("提示: 这个密钥可以保存下来，用于后续的加解密操作\n");
    printf("==============================================\n");
    
    return 0;
}
