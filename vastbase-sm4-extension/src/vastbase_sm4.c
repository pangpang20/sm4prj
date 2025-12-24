/*
 * VastBase SM4 Extension - PostgreSQL C Extension
 * 
 * 这个扩展通过JNI调用Java实现的SM4国密加密算法
 * 提供数据库级别的加密/解密函数
 */

#include "postgres.h"
#include "fmgr.h"
#include "utils/builtins.h"
#include "sm4_jni_wrapper.h"
#include <stdlib.h>  /* for getenv */

#ifdef PG_MODULE_MAGIC
PG_MODULE_MAGIC;
#endif

/* jar包路径配置 - 优先从环境变量UDF_JAR读取，否则使用默认值 */
static const char *default_jar_path = "/home/vastbase/dis-algorithm-1.0.0.0.jar";
static bool jvm_initialized = false;

/* 获取JAR路径 */
static const char* get_jar_path(void) {
    const char *env_path = getenv("UDF_JAR");
    if (env_path != NULL && env_path[0] != '\0') {
        return env_path;
    }
    return default_jar_path;
}

/*
 * 初始化JVM（延迟初始化）
 */
static void ensure_jvm_initialized(void) {
    if (!jvm_initialized) {
        const char *jar_path = get_jar_path();
        int init_result;
        
        elog(NOTICE, "SM4 JVM initializing with JAR: %s", jar_path);
        
        /* 调用JNI初始化 */
        init_result = sm4_jni_init_if_needed(jar_path);
        
        if (init_result != 0) {
            ereport(ERROR,
                    (errcode(ERRCODE_INTERNAL_ERROR),
                     errmsg("Failed to initialize SM4 JNI: %s", sm4_jni_get_error())));
        }
        
        jvm_initialized = true;
        elog(NOTICE, "SM4 JVM initialized successfully");
    }
}

/*
 * SM4密钥生成函数
 */
PG_FUNCTION_INFO_V1(sm4_generate_key_pg);

/* 使用asm强制C符号名，避免C++名称修饰 */
Datum sm4_generate_key_pg(PG_FUNCTION_ARGS) asm("sm4_generate_key_pg");

Datum
sm4_generate_key_pg(PG_FUNCTION_ARGS)
{
    char key_buffer[64];
    text *result;
    int gen_result;
    
    elog(NOTICE, "SM4 generate_key: starting");
    
    /* 确保JVM已初始化 */
    ensure_jvm_initialized();
    
    elog(NOTICE, "SM4 generate_key: JVM ready, calling JNI");
    
    /* 调用JNI生成密钥 */
    gen_result = sm4_jni_generate_key(key_buffer, sizeof(key_buffer));
    
    elog(NOTICE, "SM4 generate_key: JNI returned %d", gen_result);
    
    if (gen_result != 0) {
        ereport(ERROR,
                (errcode(ERRCODE_INTERNAL_ERROR),
                 errmsg("SM4 key generation failed: %s", sm4_jni_get_error())));
    }
    
    /* 转换为PostgreSQL text类型 */
    result = cstring_to_text(key_buffer);
    
    elog(NOTICE, "SM4 generate_key: success");
    
    PG_RETURN_TEXT_P(result);
}

/*
 * SM4加密函数（16进制）
 */
PG_FUNCTION_INFO_V1(sm4_encrypt_pg);

Datum sm4_encrypt_pg(PG_FUNCTION_ARGS) asm("sm4_encrypt_pg");

Datum
sm4_encrypt_pg(PG_FUNCTION_ARGS)
{
    text *plain_text_arg;
    text *key_arg;
    char *plain_text;
    char *hex_key;
    char cipher_buffer[8192];
    text *result;
    
    /* 获取参数 */
    plain_text_arg = PG_GETARG_TEXT_PP(0);
    key_arg = PG_GETARG_TEXT_PP(1);
    
    /* 转换为C字符串 */
    plain_text = text_to_cstring(plain_text_arg);
    hex_key = text_to_cstring(key_arg);
    
    /* 确保JVM已初始化 */
    ensure_jvm_initialized();
    
    /* 调用JNI加密 */
    if (sm4_jni_encrypt_hex(plain_text, hex_key, cipher_buffer, sizeof(cipher_buffer)) != 0) {
        pfree(plain_text);
        pfree(hex_key);
        ereport(ERROR,
                (errcode(ERRCODE_INTERNAL_ERROR),
                 errmsg("SM4 encryption failed: %s", sm4_jni_get_error())));
    }
    
    /* 转换为PostgreSQL text类型 */
    result = cstring_to_text(cipher_buffer);
    
    /* 释放临时内存 */
    pfree(plain_text);
    pfree(hex_key);
    
    PG_RETURN_TEXT_P(result);
}

/*
 * SM4解密函数（16进制）
 */
PG_FUNCTION_INFO_V1(sm4_decrypt_pg);

Datum sm4_decrypt_pg(PG_FUNCTION_ARGS) asm("sm4_decrypt_pg");

Datum
sm4_decrypt_pg(PG_FUNCTION_ARGS)
{
    text *cipher_text_arg;
    text *key_arg;
    char *cipher_text;
    char *hex_key;
    char plain_buffer[8192];
    text *result;
    
    /* 获取参数 */
    cipher_text_arg = PG_GETARG_TEXT_PP(0);
    key_arg = PG_GETARG_TEXT_PP(1);
    
    /* 转换为C字符串 */
    cipher_text = text_to_cstring(cipher_text_arg);
    hex_key = text_to_cstring(key_arg);
    
    /* 确保JVM已初始化 */
    ensure_jvm_initialized();
    
    /* 调用JNI解密 */
    if (sm4_jni_decrypt_hex(cipher_text, hex_key, plain_buffer, sizeof(plain_buffer)) != 0) {
        pfree(cipher_text);
        pfree(hex_key);
        ereport(ERROR,
                (errcode(ERRCODE_INTERNAL_ERROR),
                 errmsg("SM4 decryption failed: %s", sm4_jni_get_error())));
    }
    
    /* 转换为PostgreSQL text类型 */
    result = cstring_to_text(plain_buffer);
    
    /* 释放临时内存 */
    pfree(cipher_text);
    pfree(hex_key);
    
    PG_RETURN_TEXT_P(result);
}

/*
 * SM4加密函数（Base64）
 */
PG_FUNCTION_INFO_V1(sm4_encrypt_base64_pg);

Datum sm4_encrypt_base64_pg(PG_FUNCTION_ARGS) asm("sm4_encrypt_base64_pg");

Datum
sm4_encrypt_base64_pg(PG_FUNCTION_ARGS)
{
    text *plain_text_arg;
    text *key_arg;
    char *plain_text;
    char *base64_key;
    char cipher_buffer[8192];
    text *result;
    
    /* 获取参数 */
    plain_text_arg = PG_GETARG_TEXT_PP(0);
    key_arg = PG_GETARG_TEXT_PP(1);
    
    /* 转换为C字符串 */
    plain_text = text_to_cstring(plain_text_arg);
    base64_key = text_to_cstring(key_arg);
    
    /* 确保JVM已初始化 */
    ensure_jvm_initialized();
    
    /* 调用JNI加密 */
    if (sm4_jni_encrypt_base64(plain_text, base64_key, cipher_buffer, sizeof(cipher_buffer)) != 0) {
        pfree(plain_text);
        pfree(base64_key);
        ereport(ERROR,
                (errcode(ERRCODE_INTERNAL_ERROR),
                 errmsg("SM4 encryption (base64) failed: %s", sm4_jni_get_error())));
    }
    
    /* 转换为PostgreSQL text类型 */
    result = cstring_to_text(cipher_buffer);
    
    /* 释放临时内存 */
    pfree(plain_text);
    pfree(base64_key);
    
    PG_RETURN_TEXT_P(result);
}

/*
 * SM4解密函数（Base64）
 */
PG_FUNCTION_INFO_V1(sm4_decrypt_base64_pg);

Datum sm4_decrypt_base64_pg(PG_FUNCTION_ARGS) asm("sm4_decrypt_base64_pg");

Datum
sm4_decrypt_base64_pg(PG_FUNCTION_ARGS)
{
    text *cipher_text_arg;
    text *key_arg;
    char *cipher_text;
    char *base64_key;
    char plain_buffer[8192];
    text *result;
    
    /* 获取参数 */
    cipher_text_arg = PG_GETARG_TEXT_PP(0);
    key_arg = PG_GETARG_TEXT_PP(1);
    
    /* 转换为C字符串 */
    cipher_text = text_to_cstring(cipher_text_arg);
    base64_key = text_to_cstring(key_arg);
    
    /* 确保JVM已初始化 */
    ensure_jvm_initialized();
    
    /* 调用JNI解密 */
    if (sm4_jni_decrypt_base64(cipher_text, base64_key, plain_buffer, sizeof(plain_buffer)) != 0) {
        pfree(cipher_text);
        pfree(base64_key);
        ereport(ERROR,
                (errcode(ERRCODE_INTERNAL_ERROR),
                 errmsg("SM4 decryption (base64) failed: %s", sm4_jni_get_error())));
    }
    
    /* 转换为PostgreSQL text类型 */
    result = cstring_to_text(plain_buffer);
    
    /* 释放临时内存 */
    pfree(cipher_text);
    pfree(base64_key);
    
    PG_RETURN_TEXT_P(result);
}
