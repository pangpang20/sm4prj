-- ================================================================
-- OpenGauss PL/Java SM4 加解密函数部署脚本
-- ================================================================
-- 前置条件:
-- 1. OpenGauss已安装PL/Java扩展
-- 2. 已将dis-algorithm-1.0.0.0.jar上传到数据库服务器
-- 3. 已将jar包添加到sqlj.classpath
-- ================================================================

-- 设置jar包路径 (根据实际路径修改)
-- SELECT sqlj.install_jar('file:///path/to/dis-algorithm-1.0.0.0.jar', 'dis_algorithm', true);
-- SELECT sqlj.set_classpath('public', 'dis_algorithm');

-- ================================================================
-- 1. 创建SM4加密函数 (16进制格式)
-- ================================================================
CREATE OR REPLACE FUNCTION sm4_encrypt(text, text)
RETURNS text
AS 'com.alibaba.datax.pljava.SM4Encrypt.encrypt'
LANGUAGE java
IMMUTABLE STRICT;

COMMENT ON FUNCTION sm4_encrypt(text, text) IS 
'SM4加密函数 - 参数: (明文, 16进制密钥), 返回: 16进制密文';

-- ================================================================
-- 2. 创建SM4解密函数 (16进制格式)
-- ================================================================
CREATE OR REPLACE FUNCTION sm4_decrypt(text, text)
RETURNS text
AS 'com.alibaba.datax.pljava.SM4Decrypt.decrypt'
LANGUAGE java
IMMUTABLE STRICT;

COMMENT ON FUNCTION sm4_decrypt(text, text) IS 
'SM4解密函数 - 参数: (16进制密文, 16进制密钥), 返回: 明文';

-- ================================================================
-- 3. 创建SM4加密函数 (Base64格式)
-- ================================================================
CREATE OR REPLACE FUNCTION sm4_encrypt_base64(text, text)
RETURNS text
AS 'com.alibaba.datax.pljava.SM4Encrypt.encryptBase64'
LANGUAGE java
IMMUTABLE STRICT;

COMMENT ON FUNCTION sm4_encrypt_base64(text, text) IS 
'SM4加密函数(Base64) - 参数: (明文, Base64密钥), 返回: Base64密文';

-- ================================================================
-- 4. 创建SM4解密函数 (Base64格式)
-- ================================================================
CREATE OR REPLACE FUNCTION sm4_decrypt_base64(text, text)
RETURNS text
AS 'com.alibaba.datax.pljava.SM4Decrypt.decryptBase64'
LANGUAGE java
IMMUTABLE STRICT;

COMMENT ON FUNCTION sm4_decrypt_base64(text, text) IS 
'SM4解密函数(Base64) - 参数: (Base64密文, Base64密钥), 返回: 明文';

-- ================================================================
-- 5. 创建SM4密钥生成函数 (16进制格式)
-- ================================================================
CREATE OR REPLACE FUNCTION sm4_generate_key()
RETURNS text
AS 'com.alibaba.datax.pljava.SM4KeyGenerator.generateKey'
LANGUAGE java
VOLATILE;

COMMENT ON FUNCTION sm4_generate_key() IS 
'SM4密钥生成函数 - 返回: 32位16进制密钥字符串';

-- ================================================================
-- 6. 创建SM4密钥生成函数 (Base64格式)
-- ================================================================
CREATE OR REPLACE FUNCTION sm4_generate_key_base64()
RETURNS text
AS 'com.alibaba.datax.pljava.SM4KeyGenerator.generateKeyBase64'
LANGUAGE java
VOLATILE;

COMMENT ON FUNCTION sm4_generate_key_base64() IS 
'SM4密钥生成函数(Base64) - 返回: Base64格式密钥';

-- ================================================================
-- 使用示例
-- ================================================================

-- 示例1: 生成密钥
-- SELECT sm4_generate_key();
-- 输出示例: 'a1b2c3d4e5f67890a1b2c3d4e5f67890'

-- 示例2: 加密数据
-- SELECT sm4_encrypt('敏感数据', 'a1b2c3d4e5f67890a1b2c3d4e5f67890');

-- 示例3: 解密数据
-- SELECT sm4_decrypt('加密后的16进制字符串', 'a1b2c3d4e5f67890a1b2c3d4e5f67890');

-- 示例4: 完整的加解密测试
-- WITH test_data AS (
--     SELECT sm4_generate_key() as key_value
-- ),
-- encrypted AS (
--     SELECT key_value, 
--            sm4_encrypt('测试数据123', key_value) as cipher_text
--     FROM test_data
-- )
-- SELECT key_value,
--        cipher_text,
--        sm4_decrypt(cipher_text, key_value) as decrypted_text
-- FROM encrypted;

-- 示例5: 表中数据加密
-- UPDATE sensitive_table 
-- SET encrypted_column = sm4_encrypt(plain_column, '你的密钥')
-- WHERE encrypted_column IS NULL;

-- 示例6: 创建带加密的视图
-- CREATE VIEW encrypted_user_view AS
-- SELECT 
--     user_id,
--     username,
--     sm4_encrypt(id_card, '固定密钥或从配置表获取') as encrypted_id_card,
--     sm4_encrypt(phone, '固定密钥或从配置表获取') as encrypted_phone
-- FROM user_table;
