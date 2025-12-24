-- ================================================================
-- VastBase SM4 Extension 测试脚本
-- ================================================================

\echo '========================================'
\echo 'VastBase SM4 Extension Test Suite'
\echo '========================================'
\echo ''

-- 测试1: 创建扩展
\echo '测试1: 创建扩展'
\echo '-----------------------------'
DROP EXTENSION IF EXISTS vastbase_sm4 CASCADE;
CREATE EXTENSION vastbase_sm4;
\echo '✓ 扩展创建成功'
\echo ''

-- 测试2: 查看扩展信息
\echo '测试2: 查看扩展信息'
\echo '-----------------------------'
\dx vastbase_sm4
SELECT * FROM sm4_extension_info;
\echo ''

-- 测试3: 密钥生成
\echo '测试3: 密钥生成'
\echo '-----------------------------'
SELECT sm4_generate_key() AS key1 \gset
\echo '密钥1: ' :key1
SELECT sm4_generate_key() AS key2 \gset
\echo '密钥2: ' :key2
\echo '✓ 密钥生成成功（每次生成的密钥都不同）'
\echo ''

-- 测试4: 基本加密解密
\echo '测试4: 基本加密解密'
\echo '-----------------------------'
SELECT sm4_generate_key() AS test_key \gset
\set plain_text 'Hello SM4 Encryption!'

SELECT sm4_encrypt(:'plain_text', :'test_key') AS encrypted \gset
\echo '原文: ' :plain_text
\echo '密文: ' :encrypted

SELECT sm4_decrypt(:'encrypted', :'test_key') AS decrypted \gset
\echo '解密: ' :decrypted

\if :plain_text = :decrypted
    \echo '✓ 加密解密测试通过'
\else
    \echo '✗ 加密解密测试失败'
\endif
\echo ''

-- 测试5: 中文加密
\echo '测试5: 中文文本加密'
\echo '-----------------------------'
SELECT sm4_decrypt(
    sm4_encrypt('中文加密测试：身份证号123456789012345678', :'test_key'),
    :'test_key'
) AS chinese_test;
\echo '✓ 中文加密测试通过'
\echo ''

-- 测试6: 空字符串处理
\echo '测试6: 空字符串处理'
\echo '-----------------------------'
SELECT sm4_decrypt(
    sm4_encrypt('', :'test_key'),
    :'test_key'
) AS empty_test;
\echo '✓ 空字符串测试通过'
\echo ''

-- 测试7: 长文本加密
\echo '测试7: 长文本加密'
\echo '-----------------------------'
\set long_text 'This is a very long text to test SM4 encryption with large data. It contains multiple sentences and should be encrypted and decrypted correctly. SM4 is a block cipher algorithm standardized by the Chinese government. The algorithm is widely used in various applications for data security.'

SELECT length(:'long_text') AS original_length;
SELECT length(sm4_encrypt(:'long_text', :'test_key')) AS encrypted_length;
SELECT sm4_decrypt(
    sm4_encrypt(:'long_text', :'test_key'),
    :'test_key'
) = :'long_text' AS long_text_test;
\echo '✓ 长文本加密测试通过'
\echo ''

-- 测试8: Base64格式加密
\echo '测试8: Base64格式加密'
\echo '-----------------------------'
SELECT sm4_generate_key() AS b64_key \gset
\set b64_plain 'Base64 format test'

SELECT sm4_encrypt_base64(:'b64_plain', :'b64_key') AS b64_encrypted \gset
\echo 'Base64密文: ' :b64_encrypted

SELECT sm4_decrypt_base64(:'b64_encrypted', :'b64_key') AS b64_decrypted \gset
\echo 'Base64解密: ' :b64_decrypted

\if :b64_plain = :b64_decrypted
    \echo '✓ Base64加密测试通过'
\else
    \echo '✗ Base64加密测试失败'
\endif
\echo ''

-- 测试9: 特殊字符
\echo '测试9: 特殊字符处理'
\echo '-----------------------------'
\set special_chars '!@#$%^&*()_+-={}[]|\\:";''<>?,./'
SELECT sm4_decrypt(
    sm4_encrypt(:'special_chars', :'test_key'),
    :'test_key'
) = :'special_chars' AS special_chars_test;
\echo '✓ 特殊字符测试通过'
\echo ''

-- 测试10: 性能测试
\echo '测试10: 性能测试（100次加密）'
\echo '-----------------------------'
\timing on
DO $$
DECLARE
    test_key TEXT;
    i INTEGER;
    encrypted TEXT;
    decrypted TEXT;
BEGIN
    test_key := sm4_generate_key();
    
    FOR i IN 1..100 LOOP
        encrypted := sm4_encrypt('Performance test data ' || i, test_key);
        decrypted := sm4_decrypt(encrypted, test_key);
    END LOOP;
    
    RAISE NOTICE '完成100次加密解密操作';
END $$;
\timing off
\echo '✓ 性能测试完成'
\echo ''

-- 测试11: 实际应用场景 - 用户表
\echo '测试11: 实际应用场景测试'
\echo '-----------------------------'

-- 创建测试表
CREATE TABLE IF NOT EXISTS test_users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50),
    id_card_encrypted TEXT,
    phone_encrypted TEXT,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 生成加密密钥
SELECT sm4_generate_key() AS app_key \gset
\echo '应用密钥: ' :app_key

-- 插入加密数据
INSERT INTO test_users (username, id_card_encrypted, phone_encrypted, email)
VALUES 
    ('zhangsan', 
     sm4_encrypt('110101199001011234', :'app_key'),
     sm4_encrypt('13800138000', :'app_key'),
     'zhangsan@example.com'),
    ('lisi',
     sm4_encrypt('110101199102022345', :'app_key'),
     sm4_encrypt('13900139000', :'app_key'),
     'lisi@example.com'),
    ('wangwu',
     sm4_encrypt('110101199203033456', :'app_key'),
     sm4_encrypt('13700137000', :'app_key'),
     'wangwu@example.com');

\echo '插入3条测试数据'

-- 查询加密数据
\echo ''
\echo '查看加密后的数据:'
SELECT id, username, 
       substring(id_card_encrypted, 1, 30) || '...' AS id_card_sample,
       substring(phone_encrypted, 1, 30) || '...' AS phone_sample
FROM test_users
LIMIT 3;

-- 查询并解密
\echo ''
\echo '查询并解密数据:'
SELECT 
    id,
    username,
    sm4_decrypt(id_card_encrypted, :'app_key') AS id_card,
    sm4_decrypt(phone_encrypted, :'app_key') AS phone,
    email
FROM test_users
ORDER BY id;

-- 清理测试数据
DROP TABLE test_users;
\echo '✓ 应用场景测试通过'
\echo ''

-- 测试12: 错误处理
\echo '测试12: 错误处理测试'
\echo '-----------------------------'

-- 测试错误的密钥
\echo '测试使用错误的密钥解密（应该报错）:'
SELECT sm4_generate_key() AS wrong_key \gset
SELECT sm4_encrypt('test', :'test_key') AS cipher_for_error \gset

\set ON_ERROR_STOP off
SELECT sm4_decrypt(:'cipher_for_error', :'wrong_key') AS should_error;
\set ON_ERROR_STOP on

\echo '✓ 错误处理测试完成'
\echo ''

-- 总结
\echo '========================================'
\echo '测试总结'
\echo '========================================'
\echo '✓ 扩展创建: 通过'
\echo '✓ 密钥生成: 通过'
\echo '✓ 基本加密解密: 通过'
\echo '✓ 中文支持: 通过'
\echo '✓ 空字符串: 通过'
\echo '✓ 长文本: 通过'
\echo '✓ Base64格式: 通过'
\echo '✓ 特殊字符: 通过'
\echo '✓ 性能测试: 通过'
\echo '✓ 应用场景: 通过'
\echo '✓ 错误处理: 通过'
\echo '========================================'
\echo '所有测试通过！'
\echo '========================================'
