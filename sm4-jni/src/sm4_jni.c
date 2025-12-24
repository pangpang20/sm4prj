#include "sm4_jni.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// 全局JVM变量
static JavaVM *jvm = NULL;
static JNIEnv *env = NULL;
static char last_error[512] = {0};

// 缓存的Java类和方法ID
static jclass sm4_utils_class = NULL;
static jclass sm4_key_gen_class = NULL;
static jmethodID method_encrypt_hex = NULL;
static jmethodID method_decrypt_hex = NULL;
static jmethodID method_encrypt_base64 = NULL;
static jmethodID method_decrypt_base64 = NULL;
static jmethodID method_generate_key = NULL;

/**
 * 设置错误信息
 */
static void set_error(const char *error) {
    strncpy(last_error, error, sizeof(last_error) - 1);
    last_error[sizeof(last_error) - 1] = '\0';
}

/**
 * 检查并处理JNI异常
 */
static int check_exception(JNIEnv *env) {
    if ((*env)->ExceptionCheck(env)) {
        jthrowable exception = (*env)->ExceptionOccurred(env);
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        
        // 获取异常消息
        jclass throwable_class = (*env)->FindClass(env, "java/lang/Throwable");
        jmethodID getMessage = (*env)->GetMethodID(env, throwable_class, 
                                                    "getMessage", "()Ljava/lang/String;");
        jstring message = (jstring)(*env)->CallObjectMethod(env, exception, getMessage);
        
        if (message != NULL) {
            const char *msg_str = (*env)->GetStringUTFChars(env, message, NULL);
            set_error(msg_str);
            (*env)->ReleaseStringUTFChars(env, message, msg_str);
        } else {
            set_error("Unknown Java exception");
        }
        
        return -1;
    }
    return 0;
}

/**
 * 初始化JVM环境
 */
int sm4_jni_init(const char *jar_path) {
    JavaVMInitArgs vm_args;
    JavaVMOption options[3];
    jint result;
    
    if (jvm != NULL) {
        set_error("JVM already initialized");
        return 0; // 已经初始化
    }
    
    // 设置classpath
    char classpath[1024];
    snprintf(classpath, sizeof(classpath), "-Djava.class.path=%s", jar_path);
    
    options[0].optionString = classpath;
    options[1].optionString = "-Djava.security.egd=file:/dev/urandom";
    options[2].optionString = "-Xmx512m";
    
    vm_args.version = JNI_VERSION_1_8;
    vm_args.nOptions = 3;
    vm_args.options = options;
    vm_args.ignoreUnrecognized = JNI_FALSE;
    
    // 创建JVM
    result = JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args);
    if (result != JNI_OK) {
        snprintf(last_error, sizeof(last_error), "Failed to create JVM, error code: %d", result);
        return -1;
    }
    
    // 查找Java类
    sm4_utils_class = (*env)->FindClass(env, "com/alibaba/datax/utils/SM4Utils");
    if (sm4_utils_class == NULL || check_exception(env) != 0) {
        set_error("Failed to find SM4Utils class");
        sm4_jni_destroy();
        return -1;
    }
    sm4_utils_class = (*env)->NewGlobalRef(env, sm4_utils_class);
    
    sm4_key_gen_class = (*env)->FindClass(env, "com/alibaba/datax/pljava/SM4KeyGenerator");
    if (sm4_key_gen_class == NULL || check_exception(env) != 0) {
        set_error("Failed to find SM4KeyGenerator class");
        sm4_jni_destroy();
        return -1;
    }
    sm4_key_gen_class = (*env)->NewGlobalRef(env, sm4_key_gen_class);
    
    // 获取方法ID
    method_encrypt_hex = (*env)->GetStaticMethodID(env, sm4_utils_class, 
        "encryptGcmKeyHexResultHex", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    if (method_encrypt_hex == NULL || check_exception(env) != 0) {
        set_error("Failed to get encryptGcmKeyHexResultHex method");
        sm4_jni_destroy();
        return -1;
    }
    
    method_decrypt_hex = (*env)->GetStaticMethodID(env, sm4_utils_class,
        "decryptGcmKeyHexValueHex", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    if (method_decrypt_hex == NULL || check_exception(env) != 0) {
        set_error("Failed to get decryptGcmKeyHexValueHex method");
        sm4_jni_destroy();
        return -1;
    }
    
    method_encrypt_base64 = (*env)->GetStaticMethodID(env, sm4_utils_class,
        "encryptKey64Result64", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    if (method_encrypt_base64 == NULL || check_exception(env) != 0) {
        set_error("Failed to get encryptKey64Result64 method");
        sm4_jni_destroy();
        return -1;
    }
    
    method_decrypt_base64 = (*env)->GetStaticMethodID(env, sm4_utils_class,
        "decryptKeyBase64Value64", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    if (method_decrypt_base64 == NULL || check_exception(env) != 0) {
        set_error("Failed to get decryptKeyBase64Value64 method");
        sm4_jni_destroy();
        return -1;
    }
    
    method_generate_key = (*env)->GetStaticMethodID(env, sm4_key_gen_class,
        "generateKey", "()Ljava/lang/String;");
    if (method_generate_key == NULL || check_exception(env) != 0) {
        set_error("Failed to get generateKey method");
        sm4_jni_destroy();
        return -1;
    }
    
    printf("SM4 JNI initialized successfully\n");
    return 0;
}

/**
 * 销毁JVM环境
 */
void sm4_jni_destroy() {
    if (env != NULL) {
        if (sm4_utils_class != NULL) {
            (*env)->DeleteGlobalRef(env, sm4_utils_class);
            sm4_utils_class = NULL;
        }
        if (sm4_key_gen_class != NULL) {
            (*env)->DeleteGlobalRef(env, sm4_key_gen_class);
            sm4_key_gen_class = NULL;
        }
    }
    
    if (jvm != NULL) {
        (*jvm)->DestroyJavaVM(jvm);
        jvm = NULL;
        env = NULL;
    }
}

/**
 * SM4密钥生成
 */
int sm4_generate_key_hex(char *key_out, int key_size) {
    if (jvm == NULL || env == NULL) {
        set_error("JVM not initialized");
        return -1;
    }
    
    if (key_size < 33) {
        set_error("Key buffer too small, need at least 33 bytes");
        return -1;
    }
    
    // 调用Java方法
    jstring result = (jstring)(*env)->CallStaticObjectMethod(env, sm4_key_gen_class, method_generate_key);
    if (check_exception(env) != 0) {
        return -1;
    }
    
    if (result == NULL) {
        set_error("Failed to generate key");
        return -1;
    }
    
    // 转换结果
    const char *key_str = (*env)->GetStringUTFChars(env, result, NULL);
    strncpy(key_out, key_str, key_size - 1);
    key_out[key_size - 1] = '\0';
    (*env)->ReleaseStringUTFChars(env, result, key_str);
    
    return 0;
}

/**
 * SM4加密 - 16进制
 */
int sm4_encrypt_hex(const char *plain_text, const char *hex_key, 
                    char *cipher_out, int cipher_size) {
    if (jvm == NULL || env == NULL) {
        set_error("JVM not initialized");
        return -1;
    }
    
    // 创建Java字符串
    jstring j_key = (*env)->NewStringUTF(env, hex_key);
    jstring j_plain = (*env)->NewStringUTF(env, plain_text);
    
    // 调用Java方法
    jstring result = (jstring)(*env)->CallStaticObjectMethod(env, sm4_utils_class, 
                                                               method_encrypt_hex, j_key, j_plain);
    if (check_exception(env) != 0) {
        (*env)->DeleteLocalRef(env, j_key);
        (*env)->DeleteLocalRef(env, j_plain);
        return -1;
    }
    
    if (result == NULL) {
        set_error("Encryption failed");
        (*env)->DeleteLocalRef(env, j_key);
        (*env)->DeleteLocalRef(env, j_plain);
        return -1;
    }
    
    // 转换结果
    const char *cipher_str = (*env)->GetStringUTFChars(env, result, NULL);
    if (strlen(cipher_str) >= cipher_size) {
        set_error("Cipher buffer too small");
        (*env)->ReleaseStringUTFChars(env, result, cipher_str);
        (*env)->DeleteLocalRef(env, j_key);
        (*env)->DeleteLocalRef(env, j_plain);
        (*env)->DeleteLocalRef(env, result);
        return -1;
    }
    
    strcpy(cipher_out, cipher_str);
    (*env)->ReleaseStringUTFChars(env, result, cipher_str);
    
    // 清理
    (*env)->DeleteLocalRef(env, j_key);
    (*env)->DeleteLocalRef(env, j_plain);
    (*env)->DeleteLocalRef(env, result);
    
    return 0;
}

/**
 * SM4解密 - 16进制
 */
int sm4_decrypt_hex(const char *cipher_text, const char *hex_key,
                    char *plain_out, int plain_size) {
    if (jvm == NULL || env == NULL) {
        set_error("JVM not initialized");
        return -1;
    }
    
    // 创建Java字符串
    jstring j_key = (*env)->NewStringUTF(env, hex_key);
    jstring j_cipher = (*env)->NewStringUTF(env, cipher_text);
    
    // 调用Java方法
    jstring result = (jstring)(*env)->CallStaticObjectMethod(env, sm4_utils_class,
                                                               method_decrypt_hex, j_key, j_cipher);
    if (check_exception(env) != 0) {
        (*env)->DeleteLocalRef(env, j_key);
        (*env)->DeleteLocalRef(env, j_cipher);
        return -1;
    }
    
    if (result == NULL) {
        set_error("Decryption failed");
        (*env)->DeleteLocalRef(env, j_key);
        (*env)->DeleteLocalRef(env, j_cipher);
        return -1;
    }
    
    // 转换结果
    const char *plain_str = (*env)->GetStringUTFChars(env, result, NULL);
    if (strlen(plain_str) >= plain_size) {
        set_error("Plain buffer too small");
        (*env)->ReleaseStringUTFChars(env, result, plain_str);
        (*env)->DeleteLocalRef(env, j_key);
        (*env)->DeleteLocalRef(env, j_cipher);
        (*env)->DeleteLocalRef(env, result);
        return -1;
    }
    
    strcpy(plain_out, plain_str);
    (*env)->ReleaseStringUTFChars(env, result, plain_str);
    
    // 清理
    (*env)->DeleteLocalRef(env, j_key);
    (*env)->DeleteLocalRef(env, j_cipher);
    (*env)->DeleteLocalRef(env, result);
    
    return 0;
}

/**
 * SM4加密 - Base64
 */
int sm4_encrypt_base64(const char *plain_text, const char *base64_key,
                       char *cipher_out, int cipher_size) {
    if (jvm == NULL || env == NULL) {
        set_error("JVM not initialized");
        return -1;
    }
    
    jstring j_key = (*env)->NewStringUTF(env, base64_key);
    jstring j_plain = (*env)->NewStringUTF(env, plain_text);
    
    jstring result = (jstring)(*env)->CallStaticObjectMethod(env, sm4_utils_class,
                                                               method_encrypt_base64, j_key, j_plain);
    if (check_exception(env) != 0) {
        (*env)->DeleteLocalRef(env, j_key);
        (*env)->DeleteLocalRef(env, j_plain);
        return -1;
    }
    
    if (result == NULL) {
        set_error("Encryption failed");
        (*env)->DeleteLocalRef(env, j_key);
        (*env)->DeleteLocalRef(env, j_plain);
        return -1;
    }
    
    const char *cipher_str = (*env)->GetStringUTFChars(env, result, NULL);
    if (strlen(cipher_str) >= cipher_size) {
        set_error("Cipher buffer too small");
        (*env)->ReleaseStringUTFChars(env, result, cipher_str);
        (*env)->DeleteLocalRef(env, j_key);
        (*env)->DeleteLocalRef(env, j_plain);
        (*env)->DeleteLocalRef(env, result);
        return -1;
    }
    
    strcpy(cipher_out, cipher_str);
    (*env)->ReleaseStringUTFChars(env, result, cipher_str);
    
    (*env)->DeleteLocalRef(env, j_key);
    (*env)->DeleteLocalRef(env, j_plain);
    (*env)->DeleteLocalRef(env, result);
    
    return 0;
}

/**
 * SM4解密 - Base64
 */
int sm4_decrypt_base64(const char *cipher_text, const char *base64_key,
                       char *plain_out, int plain_size) {
    if (jvm == NULL || env == NULL) {
        set_error("JVM not initialized");
        return -1;
    }
    
    jstring j_key = (*env)->NewStringUTF(env, base64_key);
    jstring j_cipher = (*env)->NewStringUTF(env, cipher_text);
    
    jstring result = (jstring)(*env)->CallStaticObjectMethod(env, sm4_utils_class,
                                                               method_decrypt_base64, j_key, j_cipher);
    if (check_exception(env) != 0) {
        (*env)->DeleteLocalRef(env, j_key);
        (*env)->DeleteLocalRef(env, j_cipher);
        return -1;
    }
    
    if (result == NULL) {
        set_error("Decryption failed");
        (*env)->DeleteLocalRef(env, j_key);
        (*env)->DeleteLocalRef(env, j_cipher);
        return -1;
    }
    
    const char *plain_str = (*env)->GetStringUTFChars(env, result, NULL);
    if (strlen(plain_str) >= plain_size) {
        set_error("Plain buffer too small");
        (*env)->ReleaseStringUTFChars(env, result, plain_str);
        (*env)->DeleteLocalRef(env, j_key);
        (*env)->DeleteLocalRef(env, j_cipher);
        (*env)->DeleteLocalRef(env, result);
        return -1;
    }
    
    strcpy(plain_out, plain_str);
    (*env)->ReleaseStringUTFChars(env, result, plain_str);
    
    (*env)->DeleteLocalRef(env, j_key);
    (*env)->DeleteLocalRef(env, j_cipher);
    (*env)->DeleteLocalRef(env, result);
    
    return 0;
}

/**
 * 获取最后的错误信息
 */
const char* sm4_get_last_error() {
    return last_error;
}
