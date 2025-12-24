#include "sm4_jni_wrapper.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* PostgreSQL不需要包含postgres.h，因为这个模块被vastbase_sm4.c调用 */

/* 简单的日志输出（写到stderr，会在VastBase日志中显示） */
#define JNI_LOG(fmt, ...) fprintf(stderr, "[SM4_JNI] " fmt "\n", ##__VA_ARGS__); fflush(stderr)

/* 全局JVM变量 */
static JavaVM *g_jvm = NULL;
static char g_error_msg[512] = {0};

/* 缓存的Java类和方法 */
static jclass g_sm4_utils_class = NULL;
static jclass g_sm4_keygen_class = NULL;
static jmethodID g_method_encrypt_hex = NULL;
static jmethodID g_method_decrypt_hex = NULL;
static jmethodID g_method_encrypt_base64 = NULL;
static jmethodID g_method_decrypt_base64 = NULL;
static jmethodID g_method_generate_key = NULL;

/* 设置错误信息 */
static void set_error(const char *msg) {
    snprintf(g_error_msg, sizeof(g_error_msg), "%s", msg);
}

/* 检查并清理JNI异常 */
static int check_and_clear_exception(JNIEnv *env) {
    if (env->ExceptionCheck()) {
        jthrowable exception = env->ExceptionOccurred();
        env->ExceptionClear();
        
        /* 尝试获取异常消息 */
        jclass throwable_class = env->FindClass("java/lang/Throwable");
        if (throwable_class != NULL) {
            jmethodID getMessage = env->GetMethodID(throwable_class,
                                                        "getMessage", "()Ljava/lang/String;");
            if (getMessage != NULL) {
                jstring msg = (jstring)env->CallObjectMethod(exception, getMessage);
                if (msg != NULL) {
                    const char *msg_str = env->GetStringUTFChars(msg, NULL);
                    set_error(msg_str);
                    env->ReleaseStringUTFChars(msg, msg_str);
                    env->DeleteLocalRef(msg);
                }
            }
            env->DeleteLocalRef(throwable_class);
        }
        
        if (exception != NULL) {
            env->DeleteLocalRef(exception);
        }
        
        return -1;
    }
    return 0;
}

/* 初始化JVM（如果需要） */
int sm4_jni_init_if_needed(const char *jar_path) {
    JavaVMInitArgs vm_args;
    JavaVMOption options[4];
    JNIEnv *env = NULL;
    jint result;
    char classpath[1024];
    jsize nVMs = 0;
    
    JNI_LOG("init_if_needed called, jar_path=%s", jar_path);
    
    /* 如果已经初始化，直接返回 */
    if (g_jvm != NULL) {
        JNI_LOG("JVM already initialized, returning");
        return 0;
    }
    
    /* 首先检查是否已经有JVM在运行 */
    JNI_LOG("Checking for existing JVM...");
    result = JNI_GetCreatedJavaVMs(&g_jvm, 1, &nVMs);
    JNI_LOG("JNI_GetCreatedJavaVMs returned %d, nVMs=%d", result, (int)nVMs);
    
    if (result == JNI_OK && nVMs > 0 && g_jvm != NULL) {
        /* 已有JVM，尝试附加到它 */
        JNI_LOG("Found existing JVM, attempting to attach...");
        result = g_jvm->AttachCurrentThread((void**)&env, NULL);
        if (result != JNI_OK) {
            snprintf(g_error_msg, sizeof(g_error_msg),
                    "Failed to attach to existing JVM, error code: %d", result);
            JNI_LOG("AttachCurrentThread failed: %d", result);
            g_jvm = NULL;
            return -1;
        }
        JNI_LOG("Attached to existing JVM successfully");
        /* 注意：附加到现有JVM时，classpath可能不包含我们的JAR */
        /* 需要通过URLClassLoader动态加载 - 这里简化处理，先尝试直接查找类 */
    } else {
        /* 没有现有JVM，创建新的 */
        JNI_LOG("No existing JVM found, creating new one...");
        g_jvm = NULL;
        
        /* 设置classpath */
        snprintf(classpath, sizeof(classpath), "-Djava.class.path=%s", jar_path);
        JNI_LOG("Classpath: %s", classpath);
        
        options[0].optionString = classpath;
        options[1].optionString = (char*)"-Djava.security.egd=file:/dev/urandom";
        options[2].optionString = (char*)"-Xmx256m"; /* 限制内存使用 */
        options[3].optionString = (char*)"-Xcheck:jni"; /* JNI检查模式，帮助调试 */
        
        vm_args.version = JNI_VERSION_1_8;
        vm_args.nOptions = 4;
        vm_args.options = options;
        vm_args.ignoreUnrecognized = JNI_TRUE; /* 忽略不认识的选项 */
        
        /* 创建JVM */
        JNI_LOG("Calling JNI_CreateJavaVM...");
        result = JNI_CreateJavaVM(&g_jvm, (void**)&env, &vm_args);
        JNI_LOG("JNI_CreateJavaVM returned %d", result);
        
        if (result != JNI_OK) {
            snprintf(g_error_msg, sizeof(g_error_msg), 
                    "Failed to create JVM, error code: %d", result);
            g_jvm = NULL;
            return -1;
        }
        JNI_LOG("JVM created successfully");
    }
    
    /* 查找Java类 */
    JNI_LOG("Looking for SM4Utils class...");
    g_sm4_utils_class = env->FindClass("com/alibaba/datax/utils/SM4Utils");
    if (g_sm4_utils_class == NULL || check_and_clear_exception(env) != 0) {
        set_error("Failed to find SM4Utils class");
        JNI_LOG("ERROR: Failed to find SM4Utils class");
        return -1;
    }
    g_sm4_utils_class = (jclass)env->NewGlobalRef(g_sm4_utils_class);
    JNI_LOG("SM4Utils class found");
    
    JNI_LOG("Looking for SM4KeyGenerator class...");
    g_sm4_keygen_class = env->FindClass("com/alibaba/datax/pljava/SM4KeyGenerator");
    if (g_sm4_keygen_class == NULL || check_and_clear_exception(env) != 0) {
        set_error("Failed to find SM4KeyGenerator class");
        JNI_LOG("ERROR: Failed to find SM4KeyGenerator class");
        return -1;
    }
    g_sm4_keygen_class = (jclass)env->NewGlobalRef(g_sm4_keygen_class);
    JNI_LOG("SM4KeyGenerator class found");
    
    /* 获取方法ID */
    JNI_LOG("Looking for methods...");
    g_method_encrypt_hex = env->GetStaticMethodID(g_sm4_utils_class,
        "encryptGcmKeyHexResultHex", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    if (g_method_encrypt_hex == NULL || check_and_clear_exception(env) != 0) {
        set_error("Failed to find encryptGcmKeyHexResultHex method");
        JNI_LOG("ERROR: Failed to find encryptGcmKeyHexResultHex method");
        return -1;
    }
    
    g_method_decrypt_hex = env->GetStaticMethodID(g_sm4_utils_class,
        "decryptGcmKeyHexValueHex", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    if (g_method_decrypt_hex == NULL || check_and_clear_exception(env) != 0) {
        set_error("Failed to find decryptGcmKeyHexValueHex method");
        JNI_LOG("ERROR: Failed to find decryptGcmKeyHexValueHex method");
        return -1;
    }
    
    g_method_encrypt_base64 = env->GetStaticMethodID(g_sm4_utils_class,
        "encryptKey64Result64", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    if (g_method_encrypt_base64 == NULL || check_and_clear_exception(env) != 0) {
        set_error("Failed to find encryptKey64Result64 method");
        JNI_LOG("ERROR: Failed to find encryptKey64Result64 method");
        return -1;
    }
    
    g_method_decrypt_base64 = env->GetStaticMethodID(g_sm4_utils_class,
        "decryptKeyBase64Value64", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    if (g_method_decrypt_base64 == NULL || check_and_clear_exception(env) != 0) {
        set_error("Failed to find decryptKeyBase64Value64 method");
        JNI_LOG("ERROR: Failed to find decryptKeyBase64Value64 method");
        return -1;
    }
    
    g_method_generate_key = env->GetStaticMethodID(g_sm4_keygen_class,
        "generateKey", "()Ljava/lang/String;");
    if (g_method_generate_key == NULL || check_and_clear_exception(env) != 0) {
        set_error("Failed to find generateKey method");
        JNI_LOG("ERROR: Failed to find generateKey method");
        return -1;
    }
    
    JNI_LOG("All methods found, initialization complete");
    return 0;
}

/* 获取JNI环境 */
JNIEnv* sm4_jni_get_env(void) {
    JNIEnv *env = NULL;
    if (g_jvm == NULL) {
        return NULL;
    }
    
    jint result = g_jvm->GetEnv((void**)&env, JNI_VERSION_1_8);
    if (result == JNI_EDETACHED) {
        /* 当前线程未附加到JVM，需要附加 */
        result = g_jvm->AttachCurrentThread((void**)&env, NULL);
        if (result != JNI_OK) {
            return NULL;
        }
    }
    
    return env;
}

/* SM4密钥生成 */
int sm4_jni_generate_key(char *key_out, int key_size) {
    JNIEnv *env = sm4_jni_get_env();
    jstring result;
    const char *key_str;
    
    JNI_LOG("generate_key called");
    
    if (env == NULL) {
        set_error("JVM not initialized or thread not attached");
        JNI_LOG("ERROR: env is NULL");
        return -1;
    }
    
    if (key_size < 33) {
        set_error("Key buffer too small");
        JNI_LOG("ERROR: key buffer too small");
        return -1;
    }
    
    JNI_LOG("Calling Java generateKey method...");
    result = (jstring)env->CallStaticObjectMethod(g_sm4_keygen_class, 
                                                       g_method_generate_key);
    JNI_LOG("Java method returned");
    
    if (check_and_clear_exception(env) != 0 || result == NULL) {
        if (result == NULL) set_error("Failed to generate key");
        JNI_LOG("ERROR: generateKey failed or returned null");
        return -1;
    }
    
    key_str = env->GetStringUTFChars(result, NULL);
    JNI_LOG("Got key string: %s", key_str);
    strncpy(key_out, key_str, key_size - 1);
    key_out[key_size - 1] = '\0';
    env->ReleaseStringUTFChars(result, key_str);
    env->DeleteLocalRef(result);
    
    JNI_LOG("generate_key completed successfully");
    return 0;
}

/* SM4加密 - 16进制 */
int sm4_jni_encrypt_hex(const char *plain_text, const char *hex_key,
                        char *cipher_out, int cipher_size) {
    JNIEnv *env = sm4_jni_get_env();
    jstring j_key, j_plain, result;
    const char *cipher_str;
    
    if (env == NULL) {
        set_error("JVM not initialized or thread not attached");
        return -1;
    }
    
    j_key = env->NewStringUTF(hex_key);
    j_plain = env->NewStringUTF(plain_text);
    
    result = (jstring)env->CallStaticObjectMethod(g_sm4_utils_class,
                                                       g_method_encrypt_hex, j_key, j_plain);
    
    env->DeleteLocalRef(j_key);
    env->DeleteLocalRef(j_plain);
    
    if (check_and_clear_exception(env) != 0 || result == NULL) {
        if (result == NULL) set_error("Encryption failed");
        return -1;
    }
    
    cipher_str = env->GetStringUTFChars(result, NULL);
    if (strlen(cipher_str) >= (size_t)cipher_size) {
        set_error("Cipher buffer too small");
        env->ReleaseStringUTFChars(result, cipher_str);
        env->DeleteLocalRef(result);
        return -1;
    }
    
    strcpy(cipher_out, cipher_str);
    env->ReleaseStringUTFChars(result, cipher_str);
    env->DeleteLocalRef(result);
    
    return 0;
}

/* SM4解密 - 16进制 */
int sm4_jni_decrypt_hex(const char *cipher_text, const char *hex_key,
                        char *plain_out, int plain_size) {
    JNIEnv *env = sm4_jni_get_env();
    jstring j_key, j_cipher, result;
    const char *plain_str;
    
    if (env == NULL) {
        set_error("JVM not initialized or thread not attached");
        return -1;
    }
    
    j_key = env->NewStringUTF(hex_key);
    j_cipher = env->NewStringUTF(cipher_text);
    
    result = (jstring)env->CallStaticObjectMethod(g_sm4_utils_class,
                                                       g_method_decrypt_hex, j_key, j_cipher);
    
    env->DeleteLocalRef(j_key);
    env->DeleteLocalRef(j_cipher);
    
    if (check_and_clear_exception(env) != 0 || result == NULL) {
        if (result == NULL) set_error("Decryption failed");
        return -1;
    }
    
    plain_str = env->GetStringUTFChars(result, NULL);
    if (strlen(plain_str) >= (size_t)plain_size) {
        set_error("Plain buffer too small");
        env->ReleaseStringUTFChars(result, plain_str);
        env->DeleteLocalRef(result);
        return -1;
    }
    
    strcpy(plain_out, plain_str);
    env->ReleaseStringUTFChars(result, plain_str);
    env->DeleteLocalRef(result);
    
    return 0;
}

/* SM4加密 - Base64 */
int sm4_jni_encrypt_base64(const char *plain_text, const char *base64_key,
                           char *cipher_out, int cipher_size) {
    JNIEnv *env = sm4_jni_get_env();
    jstring j_key, j_plain, result;
    const char *cipher_str;
    
    if (env == NULL) {
        set_error("JVM not initialized or thread not attached");
        return -1;
    }
    
    j_key = env->NewStringUTF(base64_key);
    j_plain = env->NewStringUTF(plain_text);
    
    result = (jstring)env->CallStaticObjectMethod(g_sm4_utils_class,
                                                       g_method_encrypt_base64, j_key, j_plain);
    
    env->DeleteLocalRef(j_key);
    env->DeleteLocalRef(j_plain);
    
    if (check_and_clear_exception(env) != 0 || result == NULL) {
        if (result == NULL) set_error("Encryption failed");
        return -1;
    }
    
    cipher_str = env->GetStringUTFChars(result, NULL);
    if (strlen(cipher_str) >= (size_t)cipher_size) {
        set_error("Cipher buffer too small");
        env->ReleaseStringUTFChars(result, cipher_str);
        env->DeleteLocalRef(result);
        return -1;
    }
    
    strcpy(cipher_out, cipher_str);
    env->ReleaseStringUTFChars(result, cipher_str);
    env->DeleteLocalRef(result);
    
    return 0;
}

/* SM4解密 - Base64 */
int sm4_jni_decrypt_base64(const char *cipher_text, const char *base64_key,
                           char *plain_out, int plain_size) {
    JNIEnv *env = sm4_jni_get_env();
    jstring j_key, j_cipher, result;
    const char *plain_str;
    
    if (env == NULL) {
        set_error("JVM not initialized or thread not attached");
        return -1;
    }
    
    j_key = env->NewStringUTF(base64_key);
    j_cipher = env->NewStringUTF(cipher_text);
    
    result = (jstring)env->CallStaticObjectMethod(g_sm4_utils_class,
                                                       g_method_decrypt_base64, j_key, j_cipher);
    
    env->DeleteLocalRef(j_key);
    env->DeleteLocalRef(j_cipher);
    
    if (check_and_clear_exception(env) != 0 || result == NULL) {
        if (result == NULL) set_error("Decryption failed");
        return -1;
    }
    
    plain_str = env->GetStringUTFChars(result, NULL);
    if (strlen(plain_str) >= (size_t)plain_size) {
        set_error("Plain buffer too small");
        env->ReleaseStringUTFChars(result, plain_str);
        env->DeleteLocalRef(result);
        return -1;
    }
    
    strcpy(plain_out, plain_str);
    env->ReleaseStringUTFChars(result, plain_str);
    env->DeleteLocalRef(result);
    
    return 0;
}

/* 获取错误信息 */
const char* sm4_jni_get_error(void) {
    return g_error_msg;
}

/* 清理JVM */
void sm4_jni_cleanup(void) {
    if (g_jvm != NULL) {
        JNIEnv *env = sm4_jni_get_env();
        if (env != NULL && g_sm4_utils_class != NULL) {
            env->DeleteGlobalRef(g_sm4_utils_class);
            g_sm4_utils_class = NULL;
        }
        if (env != NULL && g_sm4_keygen_class != NULL) {
            env->DeleteGlobalRef(g_sm4_keygen_class);
            g_sm4_keygen_class = NULL;
        }
        g_jvm->DestroyJavaVM();
        g_jvm = NULL;
    }
}
