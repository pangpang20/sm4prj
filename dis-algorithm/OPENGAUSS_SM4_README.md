# OpenGauss PL/Java SM4 加解密使用指南

## 📋 目录
- [功能概述](#功能概述)
- [技术架构](#技术架构)
- [部署步骤](#部署步骤)
- [使用示例](#使用示例)
- [注意事项](#注意事项)
- [故障排查](#故障排查)

---

## 功能概述

基于国密SM4算法的OpenGauss数据库PL/Java加解密函数，提供以下功能：

✅ **SM4加密** - 支持16进制和Base64两种格式  
✅ **SM4解密** - 支持16进制和Base64两种格式  
✅ **密钥生成** - 自动生成128位安全密钥  
✅ **批量处理** - 支持数组批量加解密  

### 加密模式
- **算法**: SM4 (国密对称加密算法)
- **模式**: GCM (Galois/Counter Mode)
- **填充**: NoPadding
- **密钥长度**: 128位

---

## 技术架构

```
OpenGauss 数据库
    ↓ 
PL/Java 扩展
    ↓
dis-algorithm.jar (本项目)
    ↓
SM4Utils (com.alibaba.datax.utils)
    ↓
BouncyCastle 加密库
```

**依赖组件**:
- OpenGauss 3.x+
- PL/Java 扩展
- BouncyCastle 1.69

---

## 部署步骤

### 第一步: 编译项目

```bash
cd dis-algorithm
mvn clean package
```

生成文件: `target/dis-algorithm-1.0.0.0.jar`

### 第二步: 上传jar包到数据库服务器

```bash
# 将jar包上传到OpenGauss服务器
scp target/dis-algorithm-1.0.0.0.jar user@opengauss-server:/opt/opengauss/jars/
```

### 第三步: 在OpenGauss中安装jar包

```sql
-- 1. 安装jar包到数据库
SELECT sqlj.install_jar('file:///opt/opengauss/jars/dis-algorithm-1.0.0.0.jar', 'dis_algorithm', true);

-- 2. 设置classpath
SELECT sqlj.set_classpath('public', 'dis_algorithm');

-- 3. 验证安装
SELECT jarname, jarowner FROM sqlj.jar_repository;
```

### 第四步: 创建函数

执行SQL脚本创建所有函数:

```bash
gsql -d your_database -f src/main/resources/opengauss-sm4-deploy.sql
```

或手动创建核心函数:

```sql
-- 创建加密函数
CREATE OR REPLACE FUNCTION sm4_encrypt(text, text)
RETURNS text
AS 'com.alibaba.datax.pljava.SM4Encrypt.encrypt'
LANGUAGE java
IMMUTABLE STRICT;

-- 创建解密函数
CREATE OR REPLACE FUNCTION sm4_decrypt(text, text)
RETURNS text
AS 'com.alibaba.datax.pljava.SM4Decrypt.decrypt'
LANGUAGE java
IMMUTABLE STRICT;

-- 创建密钥生成函数
CREATE OR REPLACE FUNCTION sm4_generate_key()
RETURNS text
AS 'com.alibaba.datax.pljava.SM4KeyGenerator.generateKey'
LANGUAGE java
VOLATILE;
```

---

## 使用示例

### 示例1: 生成密钥

```sql
-- 生成16进制格式密钥
SELECT sm4_generate_key();
-- 输出: 'a1b2c3d4e5f6789012345678abcdef01'

-- 生成Base64格式密钥
SELECT sm4_generate_key_base64();
-- 输出: 'obPDxOX2eJASNFZ4q83vAQ=='
```

### 示例2: 简单加解密

```sql
-- 定义测试密钥
SET @test_key = 'a1b2c3d4e5f6789012345678abcdef01';

-- 加密
SELECT sm4_encrypt('身份证号:110101199001011234', @test_key);
-- 输出: '4f8a2b...(密文)'

-- 解密
SELECT sm4_decrypt('4f8a2b...', @test_key);
-- 输出: '身份证号:110101199001011234'
```

### 示例3: 完整测试

```sql
-- 生成密钥、加密、解密一体化测试
WITH test_data AS (
    SELECT sm4_generate_key() as key_value
),
encrypted AS (
    SELECT 
        key_value, 
        '敏感数据123' as original_text,
        sm4_encrypt('敏感数据123', key_value) as cipher_text
    FROM test_data
)
SELECT 
    key_value,
    original_text,
    cipher_text,
    sm4_decrypt(cipher_text, key_value) as decrypted_text,
    -- 验证加解密一致性
    CASE 
        WHEN sm4_decrypt(cipher_text, key_value) = original_text 
        THEN '✓ 测试通过' 
        ELSE '✗ 测试失败' 
    END as test_result
FROM encrypted;
```

### 示例4: 批量加密表数据

```sql
-- 场景: 对用户表中的身份证和手机号进行加密

-- 准备: 创建密钥表
CREATE TABLE encryption_keys (
    key_name VARCHAR(50) PRIMARY KEY,
    key_value TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 生成并存储密钥
INSERT INTO encryption_keys (key_name, key_value)
VALUES ('user_sensitive_data', sm4_generate_key());

-- 批量加密用户敏感数据
UPDATE user_table u
SET 
    id_card_encrypted = sm4_encrypt(id_card, (SELECT key_value FROM encryption_keys WHERE key_name = 'user_sensitive_data')),
    phone_encrypted = sm4_encrypt(phone, (SELECT key_value FROM encryption_keys WHERE key_name = 'user_sensitive_data'))
WHERE id_card_encrypted IS NULL;
```

### 示例5: 创建加密视图

```sql
-- 创建自动解密的视图
CREATE VIEW user_decrypted_view AS
SELECT 
    user_id,
    username,
    sm4_decrypt(id_card_encrypted, k.key_value) as id_card,
    sm4_decrypt(phone_encrypted, k.key_value) as phone,
    email
FROM user_table u
CROSS JOIN encryption_keys k
WHERE k.key_name = 'user_sensitive_data';

-- 查询时自动解密
SELECT * FROM user_decrypted_view WHERE user_id = 1001;
```

### 示例6: 使用Base64格式

```sql
-- 适用于需要Base64格式的场景
WITH base64_test AS (
    SELECT sm4_generate_key_base64() as key_value
)
SELECT 
    key_value,
    sm4_encrypt_base64('测试数据', key_value) as encrypted,
    sm4_decrypt_base64(
        sm4_encrypt_base64('测试数据', key_value), 
        key_value
    ) as decrypted
FROM base64_test;
```

### 示例7: 函数封装

```sql
-- 创建业务函数，隐藏密钥细节
CREATE OR REPLACE FUNCTION encrypt_id_card(plain_text TEXT)
RETURNS TEXT AS $$
DECLARE
    secret_key TEXT;
BEGIN
    -- 从配置表或密钥管理系统获取密钥
    SELECT key_value INTO secret_key 
    FROM encryption_keys 
    WHERE key_name = 'id_card_key';
    
    RETURN sm4_encrypt(plain_text, secret_key);
END;
$$ LANGUAGE plpgsql;

-- 使用业务函数
SELECT encrypt_id_card('110101199001011234');
```

---

## 注意事项

### 1. 密钥管理 ⚠️

**重要**: 密钥安全是加密系统的核心

- ✅ **推荐**: 使用专业的密钥管理系统 (KMS)
- ✅ **推荐**: 定期轮换密钥
- ✅ **推荐**: 密钥表设置严格的访问权限
- ❌ **禁止**: 在代码中硬编码密钥
- ❌ **禁止**: 将密钥存储在日志中

```sql
-- 设置密钥表权限示例
REVOKE ALL ON encryption_keys FROM PUBLIC;
GRANT SELECT ON encryption_keys TO app_user;
GRANT ALL ON encryption_keys TO key_admin;
```

### 2. 性能考虑

- 加解密操作会增加CPU开销，建议在应用层面考虑缓存策略
- 批量操作时建议分批处理，避免长事务
- 对于频繁查询的场景，考虑使用物化视图

### 3. 数据格式

- **16进制格式**: 密文长度较长，但兼容性好
- **Base64格式**: 密文长度较短，适合存储空间敏感场景

### 4. NULL值处理

函数使用 `STRICT` 修饰符，NULL值会直接返回NULL而不执行加密

```sql
SELECT sm4_encrypt(NULL, 'key'); -- 返回 NULL
```

### 5. 加密模式

使用GCM模式，每次加密同样的明文会产生不同的密文（包含IV），这提高了安全性

---

## 故障排查

### 问题1: 函数创建失败

**错误**: `ERROR: could not load library`

**解决**:
1. 检查jar包路径是否正确
2. 确认PL/Java扩展已安装
3. 验证classpath配置

```sql
-- 查看当前classpath
SELECT sqlj.get_classpath('public');

-- 重新设置classpath
SELECT sqlj.set_classpath('public', 'dis_algorithm');
```

### 问题2: 加密失败

**错误**: `SM4加密失败: Invalid key length`

**原因**: 密钥长度不正确

**解决**: 确保密钥是32位16进制字符串（对应128位）

```sql
-- 正确的密钥格式
SELECT length(sm4_generate_key()); -- 应该返回 32
```

### 问题3: 解密失败

**错误**: `SM4解密失败`

**可能原因**:
1. 密钥不匹配
2. 密文被篡改
3. 密文格式错误

**排查**:
```sql
-- 验证密钥
SELECT key_value FROM encryption_keys WHERE key_name = 'xxx';

-- 验证密文格式
SELECT length(cipher_text), substr(cipher_text, 1, 10) FROM your_table;
```

### 问题4: 权限问题

**错误**: `permission denied`

**解决**:
```sql
-- 授予函数执行权限
GRANT EXECUTE ON FUNCTION sm4_encrypt(text, text) TO your_user;
GRANT EXECUTE ON FUNCTION sm4_decrypt(text, text) TO your_user;
```

---

## 函数列表

| 函数名 | 参数 | 返回值 | 说明 |
|--------|------|--------|------|
| `sm4_encrypt` | (明文, 16进制密钥) | 16进制密文 | SM4加密 |
| `sm4_decrypt` | (16进制密文, 16进制密钥) | 明文 | SM4解密 |
| `sm4_encrypt_base64` | (明文, Base64密钥) | Base64密文 | SM4加密(Base64) |
| `sm4_decrypt_base64` | (Base64密文, Base64密钥) | 明文 | SM4解密(Base64) |
| `sm4_generate_key` | 无 | 16进制密钥 | 生成密钥 |
| `sm4_generate_key_base64` | 无 | Base64密钥 | 生成密钥(Base64) |

---

## 相关文件

- **Java源码**: `src/main/java/com/alibaba/datax/pljava/`
  - `SM4Encrypt.java` - 加密函数
  - `SM4Decrypt.java` - 解密函数
  - `SM4KeyGenerator.java` - 密钥生成
  
- **SQL脚本**: `src/main/resources/opengauss-sm4-deploy.sql`

- **核心工具**: `src/main/java/com/alibaba/datax/utils/SM4Utils.java`

---

## 技术支持

如有问题，请检查:
1. OpenGauss版本兼容性
2. PL/Java安装状态
3. jar包版本匹配
4. 日志文件 (`$PGDATA/log/`)

---

**版本**: 1.0.0  
**更新日期**: 2024-12-24  
**兼容**: OpenGauss 3.x+
