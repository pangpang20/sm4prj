-- ================================================================
-- VastBase 备选部署方案 - 当PL/Java不可用时
-- ================================================================

-- 方案A: 检查并安装PL/Java扩展
-- ================================================================

-- 1. 查看可用扩展
SELECT name, default_version, installed_version, comment 
FROM pg_available_extensions 
WHERE name LIKE '%java%' OR name LIKE '%pljava%';

-- 2. 如果pljava可用，则安装
-- CREATE EXTENSION IF NOT EXISTS pljava;

-- 3. 验证pljava schema是否存在
-- SELECT nspname FROM pg_namespace WHERE nspname = 'sqlj';


-- 方案B: 使用自定义C扩展或外部程序（需要额外开发）
-- ================================================================
-- 此方案需要：
-- 1. 编写C语言扩展调用Java代码
-- 2. 或使用外部程序(如Python/Go)提供HTTP API
-- 3. 在数据库中通过HTTP客户端扩展调用


-- 方案C: 应用层加解密（推荐的临时方案）
-- ================================================================
-- 说明：
-- 如果数据库不支持Java UDF，建议在应用层实现加解密
-- 使用dis-algorithm-1.0.0.0.jar中的类：
-- - com.alibaba.datax.utils.SM4Utils
-- - com.alibaba.datax.pljava.SM4Encrypt
-- - com.alibaba.datax.pljava.SM4Decrypt

-- 应用层使用示例（Java代码）：
-- import com.alibaba.datax.utils.SM4Utils;
-- 
-- // 生成密钥
-- String key = DataConvertUtil.toHexString(SM4Utils.generateKey());
-- 
-- // 加密
-- String encrypted = SM4Utils.encryptGcmKeyHexResultHex(key, "敏感数据");
-- 
-- // 解密
-- String decrypted = SM4Utils.decryptGcmKeyHexValueHex(key, encrypted);


-- 方案D: 检查VastBase是否支持其他Java调用方式
-- ================================================================

-- 检查是否有Java相关配置
SHOW all WHERE name LIKE '%java%';

-- 检查是否有其他UDF支持
SELECT proname, prolang::regproc 
FROM pg_proc 
WHERE proname LIKE '%java%' 
LIMIT 10;

-- 查看VastBase版本和支持的特性
SELECT version();


-- ================================================================
-- 如果PL/Java可用，则继续以下步骤
-- ================================================================

-- 设置classpath（假设pljava已安装）
-- SELECT sqlj.set_classpath('public', 'dis_algorithm');

-- 安装jar包
-- SELECT sqlj.install_jar('file:///home/vastbase/dis-algorithm-1.0.0.0.jar', 'dis_algorithm', true);

-- 创建函数
-- CREATE OR REPLACE FUNCTION sm4_encrypt(text, text)
-- RETURNS text
-- AS 'com.alibaba.datax.pljava.SM4Encrypt.encrypt'
-- LANGUAGE java
-- IMMUTABLE STRICT;

-- CREATE OR REPLACE FUNCTION sm4_decrypt(text, text)
-- RETURNS text
-- AS 'com.alibaba.datax.pljava.SM4Decrypt.decrypt'
-- LANGUAGE java
-- IMMUTABLE STRICT;

-- CREATE OR REPLACE FUNCTION sm4_generate_key()
-- RETURNS text
-- AS 'com.alibaba.datax.pljava.SM4KeyGenerator.generateKey'
-- LANGUAGE java
-- VOLATILE;


-- ================================================================
-- 故障排查命令
-- ================================================================

-- 1. 检查shared_preload_libraries配置
SHOW shared_preload_libraries;

-- 2. 检查dynamic_library_path
SHOW dynamic_library_path;

-- 3. 查看pg_config（如果有权限）
-- 通过shell执行: pg_config --sharedir

-- 4. 检查PostgreSQL/VastBase的lib目录下是否有pljava相关文件
-- 通过shell执行: ls -la $PGHOME/lib/*java*

-- 5. 检查是否需要重启数据库来加载pljava
-- 如果修改了shared_preload_libraries，需要重启数据库
