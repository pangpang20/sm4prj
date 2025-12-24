-- VastBase SM4 扩展 - 卸载脚本
-- 用法: vsql -d vastbase -f uninstall_functions.sql

-- 删除视图
DROP VIEW IF EXISTS sm4_extension_info;

-- 删除函数
DROP FUNCTION IF EXISTS sm4_generate_key();
DROP FUNCTION IF EXISTS sm4_encrypt(text, text);
DROP FUNCTION IF EXISTS sm4_decrypt(text, text);
DROP FUNCTION IF EXISTS sm4_encrypt_base64(text, text);
DROP FUNCTION IF EXISTS sm4_decrypt_base64(text, text);

-- 完成提示
SELECT 'SM4 extension uninstalled successfully!' as status;
