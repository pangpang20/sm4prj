# SM4 JNI - C语言调用Java SM4加密库

这个项目通过JNI (Java Native Interface) 实现了从C语言调用已有的Java SM4加密解密功能。

## 项目结构

```
sm4-jni/
├── include/              # 头文件
│   ├── sm4_jni.h        # JNI底层接口
│   └── sm4_wrapper.h    # 简化封装接口
├── src/                 # 源代码
│   ├── sm4_jni.c        # JNI实现
│   └── sm4_wrapper.c    # 简化封装实现
├── test/                # 测试代码
│   └── test_sm4.c       # 测试用例
├── Makefile             # 构建文件
└── README.md            # 本文件
```

## 依赖要求

1. **JDK**: Java Development Kit 8 或更高版本
2. **编译器**: GCC (MinGW for Windows)
3. **Java库**: dis-algorithm-1.0.0.0.jar

## 编译步骤

### 1. 确保dis-algorithm已编译

首先需要编译Java项目生成jar包：

```bash
cd ../dis-algorithm
mvn clean package
```

### 2. 设置JAVA_HOME环境变量

```bash
# Windows (cmd)
set JAVA_HOME=C:\Program Files\Java\jdk-11

# Windows (PowerShell)
$env:JAVA_HOME="C:\Program Files\Java\jdk-11"

# Git Bash
export JAVA_HOME="/c/Program Files/Java/jdk-11"
```

### 3. 编译C项目

```bash
cd sm4-jni
make
```

这将生成：
- `sm4_jni.dll` - JNI动态库
- `test_sm4.exe` - 测试程序

## 使用方法

### 方法一：使用简化API（推荐）

```c
#include "sm4_wrapper.h"
#include <stdio.h>

int main() {
    // 初始化（自动查找jar包）
    if (sm4_init() != 0) {
        fprintf(stderr, "初始化失败\n");
        return 1;
    }
    
    // 生成密钥
    char *key = sm4_new_key();
    printf("密钥: %s\n", key);
    
    // 加密
    const char *text = "Hello SM4!";
    char *encrypted = sm4_encrypt(text, key);
    printf("密文: %s\n", encrypted);
    
    // 解密
    char *decrypted = sm4_decrypt(encrypted, key);
    printf("明文: %s\n", decrypted);
    
    // 清理
    sm4_cleanup();
    return 0;
}
```

### 方法二：使用底层API

```c
#include "sm4_jni.h"
#include <stdio.h>

int main() {
    // 初始化JVM
    const char *jar_path = "../dis-algorithm/target/dis-algorithm-1.0.0.0.jar";
    if (sm4_jni_init(jar_path) != 0) {
        fprintf(stderr, "初始化失败: %s\n", sm4_get_last_error());
        return 1;
    }
    
    // 生成密钥
    char key[64];
    sm4_generate_key_hex(key, sizeof(key));
    
    // 加密
    char cipher[4096];
    const char *text = "Hello SM4!";
    if (sm4_encrypt_hex(text, key, cipher, sizeof(cipher)) != 0) {
        fprintf(stderr, "加密失败: %s\n", sm4_get_last_error());
    }
    
    // 解密
    char plain[4096];
    if (sm4_decrypt_hex(cipher, key, plain, sizeof(plain)) != 0) {
        fprintf(stderr, "解密失败: %s\n", sm4_get_last_error());
    }
    
    // 销毁JVM
    sm4_jni_destroy();
    return 0;
}
```

## API说明

### 简化API (sm4_wrapper.h)

- `int sm4_init()` - 初始化环境
- `void sm4_cleanup()` - 清理环境
- `char* sm4_new_key()` - 生成密钥
- `char* sm4_encrypt(text, key)` - 加密
- `char* sm4_decrypt(cipher, key)` - 解密

### 底层API (sm4_jni.h)

- `sm4_jni_init(jar_path)` - 初始化JVM
- `sm4_jni_destroy()` - 销毁JVM
- `sm4_generate_key_hex(key_out, size)` - 生成16进制密钥
- `sm4_encrypt_hex(plain, key, cipher_out, size)` - 16进制加密
- `sm4_decrypt_hex(cipher, key, plain_out, size)` - 16进制解密
- `sm4_encrypt_base64(plain, key, cipher_out, size)` - Base64加密
- `sm4_decrypt_base64(cipher, key, plain_out, size)` - Base64解密
- `sm4_get_last_error()` - 获取错误信息

## 运行测试

```bash
make test
```

测试包括：
1. 基本加密/解密
2. 中文文本加密
3. 长文本加密
4. 特殊字符处理
5. 边界情况测试
6. 底层API测试

## 注意事项

1. **路径问题**: 确保jar包路径正确
2. **内存管理**: 简化API使用静态缓冲区，注意线程安全
3. **错误处理**: 总是检查返回值，使用`sm4_get_last_error()`获取详细错误
4. **JVM生命周期**: 一个进程只能创建一次JVM
5. **环境变量**: 运行时需要设置正确的PATH和CLASSPATH

## 环境变量配置

运行时需要以下环境变量：

```bash
# Windows
set PATH=%JAVA_HOME%\bin;%JAVA_HOME%\jre\bin\server;%PATH%
set CLASSPATH=..\dis-algorithm\target\dis-algorithm-1.0.0.0.jar;%CLASSPATH%

# Linux/Mac
export LD_LIBRARY_PATH=$JAVA_HOME/lib/server:$LD_LIBRARY_PATH
export CLASSPATH=../dis-algorithm/target/dis-algorithm-1.0.0.0.jar:$CLASSPATH
```

## 常见问题

### Q: 提示找不到jvm.dll
**A**: 设置PATH包含`%JAVA_HOME%\bin`和`%JAVA_HOME%\jre\bin\server`

### Q: 提示找不到Java类
**A**: 确保dis-algorithm已编译，jar包路径正确

### Q: 加密结果每次都不同
**A**: 这是正常的，SM4-GCM模式使用随机IV，相同明文加密结果不同但都能正确解密

### Q: 编译错误找不到jni.h
**A**: 检查JAVA_HOME是否正确设置，Makefile中的include路径是否正确

## 性能考虑

- JVM初始化有开销（约几百毫秒），建议在程序启动时初始化一次
- 每次调用都会经过JNI边界，对于大量小数据加密考虑批量处理
- 简化API使用静态缓冲区，不是线程安全的

## 许可证

与dis-algorithm项目保持一致
