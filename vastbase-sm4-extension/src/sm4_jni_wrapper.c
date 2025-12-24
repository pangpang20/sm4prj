#include "sm4_jni_wrapper.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* PostgreSQL不需要包含postgres.h，因为这个模块被vastbase_sm4.c调用 */

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
    if ((*env)->ExceptionCheck(env)) {
        jthrowable exception = (*env)->ExceptionOccurred(env);
        (*env)->ExceptionClear(env);
        
        /* 尝试获取异常消息 */
        jclass throwable_class = (*env)->FindClass(env, "java/lang/Throwable");
        if (throwable_class != NULL) {
            jmethodID getMessage = (*env)->GetMethodID(env, throwable_class,
                                                        "getMessage", "()Ljava/lang/String;");
            if (getMessage != NULL) {
                jstring msg = (jstring)(*env)->CallObjectMethod(env, exception, getMessage);
                if (msg != NULL) {
                    const char *msg_str = (*env)->GetStringUTFChars(env, msg, NULL);
                    set_error(msg_str);
                    (*env)->ReleaseStringUTFChars(env, msg, msg_str);
                    (*env)->DeleteLocalRef(env, msg);
                }
            }
            (*env)->DeleteLocalRef(env, throwable_class);
        }
        
        if (exception != NULL) {
            (*env)->DeleteLocalRef(env, exception);
        }
        
        return -1;
    }
    return 0;
}

/* 初始化JVM（如果需要） */
int sm4_jni_init_if_needed(const char *jar_path) {
    JavaVMInitArgs vm_args;
    JavaVMOption options[3];
    JNIEnv *env = NULL;
    jint result;
    char classpath[1024];
    
    /* 如果已经初始化，直接返回 */
    if (g_jvm != NULL) {
        return 0;
    }
    
    /* 设置classpath */
    snprintf(classpath, sizeof(classpath), "-Djava.class.path=%s", jar_path);
    
    options[0].optionString = classpath;
    options[1].optionString = "-Djava.security.egd=file:/dev/urandom";
    options[2].optionString = "-Xmx256m"; /* 限制内存使用 */
    
    vm_args.version = JNI_VERSION_1_8;
    vm_args.nOptions = 3;
    vm_args.options = options;
    vm_args.ignoreUnrecognized = JNI_FALSE;
    
    /* 创建JVM */
    result = JNI_CreateJavaVM(&g_jvm, (void**)&env, &vm_args);
    if (result != JNI_OK) {
        snprintf(g_error_msg, sizeof(g_error_msg), 
                "Failed to create JVM, error code: %d", result);
        return -1;
    }
    
    /* 查找Java类 */
    g_sm4_utils_class = (*env)->FindClass(env, "com/alibaba/datax/utils/SM4Utils");
    if (g_sm4_utils_class == NULL || check_and_clear_exception(env) != 0) {
        set_error("Failed to find SM4Utils class");
        return -1;
    }
    g_sm4_utils_class = (*env)->NewGlobalRef(env, g_sm4_utils_class);
    
    g_sm4_keygen_class = (*env)->FindClass(env, "com/alibaba/datax/pljava/SM4KeyGenerator");
    if (g_sm4_keygen_class == NULL || check_and_clear_exception(env) != 0) {
        set_error("Failed to find SM4KeyGenerator class");
        return -1;
    }
    g_sm4_keygen_class = (*env)->NewGlobalRef(env, g_sm4_keygen_class);
    
    /* 获取方法ID */
    g_method_encrypt_hex = (*env)->GetStaticMethodID(env, g_sm4_utils_class,
        "encryptGcmKeyHexResultHex", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    if (g_method_encrypt_hex == NULL || check_and_clear_exception(env) != 0) {
        set_error("Failed to find encryptGcmKeyHexResultHex method");
        return -1;
    }
    
    g_method_decrypt_hex = (*env)->GetStaticMethodID(env, g_sm4_utils_class,
        "decryptGcmKeyHexValueHex", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    if (g_method_decrypt_hex == NULL || check_and_clear_exception(env) != 0) {
        set_error("Failed to find decryptGcmKeyHexValueHex method");
        return -1;
    }
    
    g_method_encrypt_base64 = (*env)->GetStaticMethodID(env, g_sm4_utils_class,
        "encryptKey64Result64", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    if (g_method_encrypt_base64 == NULL || check_and_clear_exception(env) != 0) {
        set_error("Failed to find encryptKey64Result64 method");
        return -1;
    }
    
    g_method_decrypt_base64 = (*env)->GetStaticMethodID(env, g_sm4_utils_class,
        "decryptKeyBase64Value64", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    if (g_method_decrypt_base64 == NULL || check_and_clear_exception(env) != 0) {
        set_error("Failed to find decryptKeyBase64Value64 method");
        return -1;
    }
    
    g_method_generate_key = (*env)->GetStaticMethodID(env, g_sm4_keygen_class,
        "generateKey", "()Ljava/lang/String;");
    if (g_method_generate_key == NULL || check_and_clear_exception(env) != 0) {
        set_error("Failed to find generateKey method");
        return -1;
    }
    
    return 0;
}

/* 获取JNI环境 */
JNIEnv* sm4_jni_get_env(void) {
    JNIEnv *env = NULL;
    if (g_jvm == NULL) {
        return NULL;
    }
    
    jint result = (*g_jvm)->GetEnv(g_jvm, (void**)&env, JNI_VERSION_1_8);
    if (result == JNI_EDETACHED) {
        /* 当前线程未附加到JVM，需要附加 */
        result = (*g_jvm)->AttachCurrentThread(g_jvm, (void**)&env, NULL);
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
    
    if (env == NULL) {
        set_error("JVM not initialized or thread not attached");
        return -1;
    }
    
    if (key_size < 33) {
        set_error("Key buffer too small");
        return -1;
    }
    
    result = (jstring)(*env)->CallStaticObjectMethod(env, g_sm4_keygen_class, 
                                                       g_method_generate_key);
    if (check_and_clear_exception(env) != 0 || result == NULL) {
        if (result == NULL) set_error("Failed to generate key");
        return -1;
    }
    
    key_str = (*env)->GetStringUTFChars(env, result, NULL);
    strncpy(key_out, key_str, key_size - 1);
    key_out[key_size - 1] = '\0';
    (*env)->ReleaseStringUTFChars(env, result, key_str);
    (*env)->DeleteLocalRef(env, result);
    
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
    
    j_key = (*env)->NewStringUTF(env, hex_key);
    j_plain = (*env)->NewStringUTF(env, plain_text);
    
    result = (jstring)(*env)->CallStaticObjectMethod(env, g_sm4_utils_class,
                                                       g_method_encrypt_hex, j_key, j_plain);
    
    (*env)->DeleteLocalRef(env, j_key);
    (*env)->DeleteLocalRef(env, j_plain);
    
    if (check_and_clear_exception(env) != 0 || result == NULL) {
        if (result == NULL) set_error("Encryption failed");
        return -1;
    }
    
    cipher_str = (*env)->GetStringUTFChars(env, result, NULL);
    if (strlen(cipher_str) >= cipher_size) {
        set_error("Cipher buffer too small");
        (*env)->ReleaseStringUTFChars(env, result, cipher_str);
        (*env)->DeleteLocalRef(env, result);
        return -1;
    }
    
    strcpy(cipher_out, cipher_str);
    (*env)->ReleaseStringUTFChars(env, result, cipher_str);
    (*env)->DeleteLocalRef(env, result);
    
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
    
    j_key = (*env)->NewStringUTF(env, hex_key);
    j_cipher = (*env)->NewStringUTF(env, cipher_text);
    
    result = (jstring)(*env)->CallStaticObjectMethod(env, g_sm4_utils_class,
                                                       g_method_decrypt_hex, j_key, j_cipher);
    
    (*env)->DeleteLocalRef(env, j_key);
    (*env)->DeleteLocalRef(env, j_cipher);
    
    if (check_and_clear_exception(env) != 0 || result == NULL) {
        if (result == NULL) set_error("Decryption failed");
        return -1;
    }
    
    plain_str = (*env)->GetStringUTFChars(env, result, NULL);
    if (strlen(plain_str) >= plain_size) {
        set_error("Plain buffer too small");
        (*env)->ReleaseStringUTFChars(env, result, plain_str);
        (*env)->DeleteLocalRef(env, result);
        return -1;
    }
    
    strcpy(plain_out, plain_str);
    (*env)->ReleaseStringUTFChars(env, result, plain_str);
    (*env)->DeleteLocalRef(env, result);
    
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
    
    j_key = (*env)->NewStringUTF(env, base64_key);
    j_plain = (*env)->NewStringUTF(env, plain_text);
    
    result = (jstring)(*env)->CallStaticObjectMethod(env, g_sm4_utils_class,
                                                       g_method_encrypt_base64, j_key, j_plain);
    
    (*env)->DeleteLocalRef(env, j_key);
    (*env)->DeleteLocalRef(env, j_plain);
    
    if (check_and_clear_exception(env) != 0 || result == NULL) {
        if (result == NULL) set_error("Encryption failed");
        return -1;
    }
    
    cipher_str = (*env)->GetStringUTFChars(env, result, NULL);
    if (strlen(cipher_str) >= cipher_size) {
        set_error("Cipher buffer too small");
        (*env)->ReleaseStringUTFChars(env, result, cipher_str);
        (*env)->DeleteLocalRef(env, result);
        return -1;
    }
    
    strcpy(cipher_out, cipher_str);
    (*env)->ReleaseStringUTFChars(env, result, cipher_str);
    (*env)->DeleteLocalRef(env, result);
    
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
    
    j_key = (*env)->NewStringUTF(env, base64_key);
    j_cipher = (*env)->NewStringUTF(env, cipher_text);
    
    result = (jstring)(*env)->CallStaticObjectMethod(env, g_sm4_utils_class,
                                                       g_method_decrypt_base64, j_key, j_cipher);
    
    (*env)->DeleteLocalRef(env, j_key);
    (*env)->DeleteLocalRef(env, j_cipher);
    
    if (check_and_clear_exception(env) != 0 || result == NULL) {
        if (result == NULL) set_error("Decryption failed");
        return -1;
    }
    
    plain_str = (*env)->GetStringUTFChars(env, result, NULL);
    if (strlen(plain_str) >= plain_size) {
        set_error("Plain buffer too small");
        (*env)->ReleaseStringUTFChars(env, result, plain_str);
        (*env)->DeleteLocalRef(env, result);
        return -1;
    }
    
    strcpy(plain_out, plain_str);
    (*env)->ReleaseStringUTFChars(env, result, plain_str);
    (*env)->DeleteLocalRef(env, result);
    
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
            (*env)->DeleteGlobalRef(env, g_sm4_utils_class);
            g_sm4_utils_class = NULL;
        }
        if (env != NULL && g_sm4_keygen_class != NULL) {
            (*env)->DeleteGlobalRef(env, g_sm4_keygen_class);
            g_sm4_keygen_class = NULL;
        }
        (*g_jvm)->DestroyJavaVM(g_jvm);
        g_jvm = NULL;
    }
}
