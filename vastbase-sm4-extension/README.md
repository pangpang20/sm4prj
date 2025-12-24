# VastBase SM4 Extension

VastBase/PostgreSQL的SM4国密加密扩展，通过JNI调用Java实现的SM4加解密算法。

## 项目说明

此扩展为VastBase数据库提供SM4国密加密功能，采用C语言开发，通过JNI技术调用已有的Java SM4实现（dis-algorithm项目）。

## 功能特性

✅ **SM4密钥生成** - `sm4_generate_key()`  
✅ **SM4加密** - `sm4_encrypt(plain_text, hex_key)`  
✅ **SM4解密** - `sm4_decrypt(cipher_text, hex_key)`  
✅ **Base64格式** - `sm4_encrypt_base64()` / `sm4_decrypt_base64()`  
✅ **GCM加密模式** - 采用SM4-GCM模式，提供认证加密  

## 系统要求

- **VastBase** 或 **PostgreSQL** 9.1+
- **JDK** 8 或更高版本
- **GCC** 编译器
- **dis-algorithm** jar包已编译

## 项目结构

```
vastbase-sm4-extension/
├── Makefile                      # PostgreSQL扩展构建文件
├── vastbase_sm4.control          # 扩展控制文件
├── vastbase_sm4--1.0.sql         # SQL安装脚本
├── src/
│   ├── vastbase_sm4.c           # PostgreSQL扩展主文件
│   ├── sm4_jni_wrapper.h        # JNI包装层头文件
│   └── sm4_jni_wrapper.c        # JNI包装层实现
├── docs/
│   ├── INSTALL.md               # 安装指南
│   └── USAGE.md                 # 使用手册
└── README.md                     # 本文件
```

## 快速开始

### 1. 编译dis-algorithm

```bash
cd ../dis-algorithm
mvn clean package
```

### 2. 配置环境变量

```bash
# 设置JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

# 设置pg_config路径（如果不在PATH中）
export PG_CONFIG=/usr/pgsql-13/bin/pg_config
```

### 3. 编译扩展

```bash
cd vastbase-sm4-extension
make
```

### 4. 安装扩展

```bash
# 需要PostgreSQL/VastBase管理员权限
sudo make install
```

### 5. 在数据库中创建扩展

```sql
-- 连接到目标数据库
\c your_database

-- 创建扩展
CREATE EXTENSION vastbase_sm4;

-- 验证安装
SELECT * FROM sm4_extension_info;
```

## 使用示例

### 生成密钥

```sql
SELECT sm4_generate_key();
-- 返回: a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef12345
```

### 加密数据

```sql
-- 生成并保存密钥
\set key `SELECT sm4_generate_key()`

-- 加密敏感数据
SELECT sm4_encrypt('身份证号: 110101199001011234', :'key');
-- 返回: 16进制密文
```

### 解密数据

```sql
-- 使用相同密钥解密
SELECT sm4_decrypt('加密后的16进制密文', :'key');
-- 返回: 身份证号: 110101199001011234
```

### 实际应用场景

```sql
-- 创建带加密字段的表
CREATE TABLE user_info (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50),
    id_card_encrypted TEXT,  -- 加密后的身份证号
    phone_encrypted TEXT,    -- 加密后的手机号
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 生成并记录密钥（生产环境应安全存储）
SELECT sm4_generate_key() AS encryption_key \gset

-- 插入加密数据
INSERT INTO user_info (username, id_card_encrypted, phone_encrypted)
VALUES (
    'zhangsan',
    sm4_encrypt('110101199001011234', :'encryption_key'),
    sm4_encrypt('13800138000', :'encryption_key')
);

-- 查询并解密
SELECT 
    username,
    sm4_decrypt(id_card_encrypted, :'encryption_key') AS id_card,
    sm4_decrypt(phone_encrypted, :'encryption_key') AS phone
FROM user_info;
```

## 函数说明

### sm4_generate_key()

生成SM4加密密钥（16进制格式）

- **返回**: TEXT - 32位16进制字符串（表示128位密钥）
- **示例**: `SELECT sm4_generate_key();`

### sm4_encrypt(plain_text TEXT, hex_key TEXT)

SM4加密函数

- **参数1**: 待加密的明文
- **参数2**: 16进制密钥（32位字符）
- **返回**: 16进制格式的密文
- **示例**: `SELECT sm4_encrypt('敏感数据', 'abc123...');`

### sm4_decrypt(cipher_text TEXT, hex_key TEXT)

SM4解密函数

- **参数1**: 16进制格式的密文
- **参数2**: 16进制密钥（32位字符）
- **返回**: 解密后的明文
- **示例**: `SELECT sm4_decrypt('加密数据', 'abc123...');`

### sm4_encrypt_base64(plain_text TEXT, base64_key TEXT)

SM4加密函数（Base64格式）

- **参数1**: 待加密的明文
- **参数2**: Base64格式的密钥
- **返回**: Base64格式的密文

### sm4_decrypt_base64(cipher_text TEXT, base64_key TEXT)

SM4解密函数（Base64格式）

- **参数1**: Base64格式的密文
- **参数2**: Base64格式的密钥
- **返回**: 解密后的明文

## 配置说明

### 修改jar包路径

默认jar包路径为 `../dis-algorithm/target/dis-algorithm-1.0.0.0.jar`

如需修改，编辑 `src/vastbase_sm4.c` 文件中的 `jar_path` 变量：

```c
static char *jar_path = "/path/to/your/dis-algorithm-1.0.0.0.jar";
```

### 运行时环境

确保运行时环境变量包含JVM库路径：

```bash
# Linux
export LD_LIBRARY_PATH=$JAVA_HOME/lib/server:$LD_LIBRARY_PATH

# macOS
export DYLD_LIBRARY_PATH=$JAVA_HOME/lib/server:$DYLD_LIBRARY_PATH
```

可以在 `postgresql.conf` 中配置：

```ini
# 添加到 postgresql.conf
env = 'LD_LIBRARY_PATH=/usr/lib/jvm/java-11-openjdk-amd64/lib/server'
```

## 性能考虑

1. **JVM初始化**: 首次调用函数时会初始化JVM（约100-200ms），后续调用无此开销
2. **JNI调用开销**: 每次加密/解密约0.1-1ms
3. **适用场景**: 适合低频加密操作，不适合大批量实时加密

## 常见问题

### Q: 编译时找不到postgres.h

**A**: 确保安装了PostgreSQL开发包

```bash
# CentOS/RHEL
yum install postgresql-devel

# Ubuntu/Debian
apt-get install postgresql-server-dev-13
```

### Q: 运行时报错: cannot load library

**A**: 检查以下几点：
1. JVM库路径是否在 LD_LIBRARY_PATH 中
2. dis-algorithm jar包路径是否正确
3. JAVA_HOME 是否正确设置

### Q: 相同明文加密结果每次都不同？

**A**: 这是正常的！SM4-GCM模式使用随机IV，相同明文的加密结果会不同，但都能正确解密。

### Q: VastBase与PostgreSQL的兼容性？

**A**: VastBase基于PostgreSQL，此扩展完全兼容两者。

## 卸载扩展

```sql
-- 在数据库中删除扩展
DROP EXTENSION vastbase_sm4;
```

```bash
# 从系统中卸载
sudo make uninstall
```

## 安全建议

⚠️ **密钥管理**
- 密钥应安全存储，不要硬编码在应用中
- 建议使用专门的密钥管理系统（KMS）
- 定期轮换密钥

⚠️ **权限控制**
- 限制函数使用权限
- 记录加密/解密操作日志

⚠️ **生产环境**
- 充分测试后再部署
- 监控JVM内存使用情况

## 许可证

与dis-algorithm项目保持一致

## 技术支持

如有问题，请参考：
- [安装指南](docs/INSTALL.md)
- [使用手册](docs/USAGE.md)
- dis-algorithm项目文档
