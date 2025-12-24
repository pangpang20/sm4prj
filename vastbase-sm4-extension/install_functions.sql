-- VastBase SM4 扩展 - 手动安装脚本
-- 用法: vsql -d vastbase -f install_functions.sql

-- ================================================================
-- SM4 密钥生成函数
-- ================================================================
CREATE OR REPLACE FUNCTION sm4_generate_key()
RETURNS text
AS '$libdir/vastbase_sm4', 'sm4_generate_key_pg'
LANGUAGE C STRICT VOLATILE;

COMMENT ON FUNCTION sm4_generate_key() IS 
'生成SM4加密密钥（16进制格式，32位字符）';


-- ================================================================
-- SM4 加密函数（16进制格式）
-- ================================================================
CREATE OR REPLACE FUNCTION sm4_encrypt(plain_text text, hex_key text)
RETURNS text
AS '$libdir/vastbase_sm4', 'sm4_encrypt_pg'
LANGUAGE C STRICT IMMUTABLE;

COMMENT ON FUNCTION sm4_encrypt(text, text) IS 
'SM4加密函数
参数1: 待加密的明文
参数2: 16进制密钥（32位字符）
返回: 16进制格式的密文';


-- ================================================================
-- SM4 解密函数（16进制格式）
-- ================================================================
CREATE OR REPLACE FUNCTION sm4_decrypt(cipher_text text, hex_key text)
RETURNS text
AS '$libdir/vastbase_sm4', 'sm4_decrypt_pg'
LANGUAGE C STRICT IMMUTABLE;

COMMENT ON FUNCTION sm4_decrypt(text, text) IS 
'SM4解密函数
参数1: 16进制格式的密文
参数2: 16进制密钥（32位字符）
返回: 解密后的明文';


-- ================================================================
-- SM4 加密函数（Base64格式）
-- ================================================================
CREATE OR REPLACE FUNCTION sm4_encrypt_base64(plain_text text, base64_key text)
RETURNS text
AS '$libdir/vastbase_sm4', 'sm4_encrypt_base64_pg'
LANGUAGE C STRICT IMMUTABLE;

COMMENT ON FUNCTION sm4_encrypt_base64(text, text) IS 
'SM4加密函数（Base64格式）
参数1: 待加密的明文
参数2: Base64格式的密钥
返回: Base64格式的密文';


-- ================================================================
-- SM4 解密函数（Base64格式）
-- ================================================================
CREATE OR REPLACE FUNCTION sm4_decrypt_base64(cipher_text text, base64_key text)
RETURNS text
AS '$libdir/vastbase_sm4', 'sm4_decrypt_base64_pg'
LANGUAGE C STRICT IMMUTABLE;

COMMENT ON FUNCTION sm4_decrypt_base64(text, text) IS 
'SM4解密函数（Base64格式）
参数1: Base64格式的密文
参数2: Base64格式的密钥
返回: 解密后的明文';


-- ================================================================
-- 便捷视图：显示扩展版本和状态
-- ================================================================
CREATE OR REPLACE VIEW sm4_extension_info AS
SELECT 
    'vastbase_sm4' as extension_name,
    '1.0' as version,
    'SM4 encryption via Java JNI' as description,
    current_timestamp as query_time;

COMMENT ON VIEW sm4_extension_info IS 'SM4扩展信息视图';

-- 完成提示
SELECT 'SM4 extension installed successfully!' as status;
