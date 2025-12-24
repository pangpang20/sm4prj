# VastBase SM4 Extension 安装指南

本文档详细说明如何在VastBase或PostgreSQL数据库中安装SM4加密扩展。

## 前置条件

### 1. 系统要求

- **操作系统**: Linux (推荐 CentOS 7+, Ubuntu 18.04+) 或 Windows
- **数据库**: VastBase 或 PostgreSQL 9.1+
- **JDK**: Java 8 或更高版本
- **编译工具**: GCC 4.8+ 或 MinGW (Windows)

### 2. 检查PostgreSQL开发环境

```bash
# 检查pg_config是否可用
which pg_config

# 查看PostgreSQL版本
pg_config --version

# 查看安装路径
pg_config --pgxs
```

如果 `pg_config` 不存在，需要安装PostgreSQL开发包：

**CentOS/RHEL:**
```bash
sudo yum install postgresql-devel
# 或 VastBase开发包
sudo yum install vastbase-devel
```

**Ubuntu/Debian:**
```bash
sudo apt-get install postgresql-server-dev-13
# 替换13为你的PostgreSQL版本
```

### 3. 检查JDK环境

```bash
# 检查Java版本
java -version
javac -version

# 检查JAVA_HOME
echo $JAVA_HOME

# 如果未设置，添加到 ~/.bashrc 或 ~/.bash_profile
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

## 安装步骤

### 步骤1: 编译dis-algorithm项目

SM4扩展依赖dis-algorithm项目提供的Java实现。

```bash
cd /path/to/putuo/dis-algorithm
mvn clean package

# 验证jar包生成
ls -lh target/dis-algorithm-1.0.0.0.jar
```

### 步骤2: 配置编译环境

```bash
cd /path/to/putuo/vastbase-sm4-extension

# 设置环境变量
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export PG_CONFIG=/usr/pgsql-13/bin/pg_config  # 根据实际路径调整

# 验证配置
make show-config
```

输出示例：
```
====================================
VastBase SM4 Extension Build Config
====================================
PG_CONFIG: /usr/pgsql-13/bin/pg_config
PGXS: /usr/pgsql-13/lib/pgxs/src/makefiles/pgxs.mk
JAVA_HOME: /usr/lib/jvm/java-11-openjdk-amd64
JAVA_JAR: ../dis-algorithm/target/dis-algorithm-1.0.0.0.jar
Extension: vastbase_sm4
Module: vastbase_sm4
====================================
```

### 步骤3: 编译扩展

```bash
# 清理之前的编译
make clean

# 编译
make

# 检查生成的文件
ls -lh vastbase_sm4.so  # Linux
# 或
ls -lh vastbase_sm4.dll # Windows
```

### 步骤4: 安装扩展到数据库

```bash
# 需要数据库管理员权限
sudo make install

# 或手动安装
sudo cp vastbase_sm4.so `pg_config --pkglibdir`
sudo cp vastbase_sm4.control `pg_config --sharedir`/extension/
sudo cp vastbase_sm4--1.0.sql `pg_config --sharedir`/extension/
```

### 步骤5: 配置数据库环境

编辑 `postgresql.conf` 或 `vastbase.conf`：

```ini
# 添加JVM库路径
# Linux
env = 'LD_LIBRARY_PATH=/usr/lib/jvm/java-11-openjdk-amd64/lib/server'

# 或使用 environment_variables (PostgreSQL 14+)
environment_variables = 'LD_LIBRARY_PATH=/usr/lib/jvm/java-11-openjdk-amd64/lib/server'
```

或者在系统级别配置：

```bash
# 编辑 /etc/ld.so.conf.d/jvm.conf
sudo echo "/usr/lib/jvm/java-11-openjdk-amd64/lib/server" > /etc/ld.so.conf.d/jvm.conf
sudo ldconfig
```

### 步骤6: 重启数据库

```bash
# PostgreSQL
sudo systemctl restart postgresql-13

# VastBase
sudo systemctl restart vastbase

# 或使用pg_ctl
pg_ctl restart -D /var/lib/pgsql/13/data
```

### 步骤7: 创建扩展

```sql
-- 连接到数据库
psql -U postgres -d your_database

-- 创建扩展
CREATE EXTENSION vastbase_sm4;

-- 验证安装
\dx vastbase_sm4

-- 查看扩展信息
SELECT * FROM sm4_extension_info;

-- 测试功能
SELECT sm4_generate_key();
```

## 验证安装

### 完整功能测试

```sql
-- 1. 生成密钥
SELECT sm4_generate_key() AS key \gset
\echo :key

-- 2. 加密测试
SELECT sm4_encrypt('Hello SM4!', :'key') AS encrypted \gset
\echo :encrypted

-- 3. 解密测试
SELECT sm4_decrypt(:'encrypted', :'key') AS decrypted;
-- 应该返回: Hello SM4!

-- 4. 中文测试
SELECT sm4_decrypt(
    sm4_encrypt('测试中文加密', :'key'),
    :'key'
) AS result;
-- 应该返回: 测试中文加密

-- 5. Base64格式测试
SELECT sm4_generate_key() AS b64key \gset
SELECT sm4_encrypt_base64('Base64 test', :'b64key') AS b64_encrypted \gset
SELECT sm4_decrypt_base64(:'b64_encrypted', :'b64key');
```

## 故障排查

### 问题1: CREATE EXTENSION 失败

**错误信息**: 
```
ERROR: could not load library "/usr/pgsql-13/lib/vastbase_sm4.so": 
libjvm.so: cannot open shared object file: No such file or directory
```

**解决方案**:
```bash
# 1. 检查JVM库文件
ls -l $JAVA_HOME/lib/server/libjvm.so

# 2. 添加到 LD_LIBRARY_PATH
export LD_LIBRARY_PATH=$JAVA_HOME/lib/server:$LD_LIBRARY_PATH

# 3. 重启数据库
sudo systemctl restart postgresql-13
```

### 问题2: 编译时找不到jni.h

**错误信息**:
```
fatal error: jni.h: No such file or directory
```

**解决方案**:
```bash
# 检查JNI头文件
ls $JAVA_HOME/include/jni.h

# 如果不存在，安装JDK开发包
sudo yum install java-11-openjdk-devel
```

### 问题3: SM4函数调用报错

**错误信息**:
```
ERROR: SM4 initialization failed: Failed to find SM4Utils class
```

**解决方案**:
```bash
# 1. 检查jar包路径
ls -l ../dis-algorithm/target/dis-algorithm-1.0.0.0.jar

# 2. 修改 src/vastbase_sm4.c 中的 jar_path
# 使用绝对路径
static char *jar_path = "/opt/putuo/dis-algorithm/target/dis-algorithm-1.0.0.0.jar";

# 3. 重新编译安装
make clean
make
sudo make install

# 4. 在数据库中重新创建扩展
DROP EXTENSION vastbase_sm4;
CREATE EXTENSION vastbase_sm4;
```

### 问题4: Windows环境编译问题

**解决方案**:
```cmd
REM 安装MinGW或使用Visual Studio
REM 设置环境变量
set JAVA_HOME=C:\Program Files\Java\jdk-11
set PG_CONFIG=C:\Program Files\PostgreSQL\13\bin\pg_config.exe

REM 编译（可能需要修改Makefile适配Windows）
mingw32-make
```

## 性能优化

### 1. 调整JVM内存

修改 `src/vastbase_sm4.c` 中的JVM参数：

```c
options[2].optionString = "-Xmx512m";  // 增加到512MB
```

### 2. 预热JVM

数据库启动后执行一次加密操作，预热JVM：

```sql
-- 在数据库启动脚本中添加
SELECT sm4_encrypt('warmup', sm4_generate_key());
```

### 3. 监控内存使用

```sql
-- 查看数据库进程内存
SELECT 
    pid,
    usename,
    application_name,
    pg_size_pretty(pg_backend_memory_contexts()) as memory_usage
FROM pg_stat_activity
WHERE backend_type = 'client backend';
```

## 权限配置

### 限制函数使用权限

```sql
-- 撤销public的执行权限
REVOKE EXECUTE ON FUNCTION sm4_encrypt(text, text) FROM PUBLIC;
REVOKE EXECUTE ON FUNCTION sm4_decrypt(text, text) FROM PUBLIC;
REVOKE EXECUTE ON FUNCTION sm4_generate_key() FROM PUBLIC;

-- 仅授权给特定角色
GRANT EXECUTE ON FUNCTION sm4_encrypt(text, text) TO app_role;
GRANT EXECUTE ON FUNCTION sm4_decrypt(text, text) TO app_role;
GRANT EXECUTE ON FUNCTION sm4_generate_key() TO admin_role;
```

### 审计日志

```sql
-- 创建审计触发器
CREATE TABLE sm4_audit_log (
    id SERIAL PRIMARY KEY,
    operation VARCHAR(20),
    username VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 记录加密操作（示例）
CREATE OR REPLACE FUNCTION log_sm4_operation()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO sm4_audit_log (operation, username)
    VALUES (TG_OP, current_user);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

## 卸载扩展

```sql
-- 1. 在数据库中删除扩展
DROP EXTENSION vastbase_sm4 CASCADE;
```

```bash
# 2. 从系统中卸载
sudo make uninstall

# 或手动删除
sudo rm `pg_config --pkglibdir`/vastbase_sm4.so
sudo rm `pg_config --sharedir`/extension/vastbase_sm4.control
sudo rm `pg_config --sharedir`/extension/vastbase_sm4--1.0.sql
```

## 升级扩展

将来如果有新版本：

```sql
-- 创建升级脚本 vastbase_sm4--1.0--1.1.sql
ALTER EXTENSION vastbase_sm4 UPDATE TO '1.1';
```

## 生产环境部署清单

- [ ] 编译dis-algorithm并验证
- [ ] 配置JAVA_HOME和LD_LIBRARY_PATH
- [ ] 编译并测试扩展功能
- [ ] 配置postgresql.conf环境变量
- [ ] 重启数据库服务
- [ ] 在测试库验证功能
- [ ] 配置函数权限
- [ ] 设置审计日志
- [ ] 监控JVM内存使用
- [ ] 准备密钥管理方案
- [ ] 文档化部署步骤
- [ ] 准备回滚方案

## 下一步

- 阅读 [使用手册](USAGE.md) 了解详细的函数使用方法
- 查看 [README.md](../README.md) 了解项目整体信息
