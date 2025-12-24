# 快速修复编译错误

## 问题分析

编译错误 `fatal error: jni.h: No such file or directory` 的原因：

1. **Makefile中配置的是Windows路径** (`include/win32`)，而您在Linux环境
2. **需要使用Linux路径** (`include/linux`)

## 解决方案（三选一）

### 方案1: 最简单 - 直接设置环境变量

```bash
# 设置JAVA_HOME为HuaweiJDK路径
export JAVA_HOME=/home/vastbase/binarylibs/platform/huaweijdk8/x86_64/jdk

# 验证路径
ls -l $JAVA_HOME/include/jni.h
ls -l $JAVA_HOME/include/linux/jni_md.h

# 编译
make clean
make JAVA_HOME=$JAVA_HOME
```

### 方案2: 使用配置脚本

```bash
# 运行配置脚本
source config.sh

# 编译
make clean
make
```

### 方案3: 使用自动修复脚本（推荐）

```bash
# 添加执行权限
chmod +x fix-compile.sh

# 运行修复脚本
./fix-compile.sh

# 按照提示执行export命令，然后编译
make clean
make
```

## 验证JDK环境

在编译前，先验证JDK环境：

```bash
# 设置JAVA_HOME
export JAVA_HOME=/home/vastbase/binarylibs/platform/huaweijdk8/x86_64/jdk

# 检查JNI头文件
echo "检查 jni.h:"
ls -l $JAVA_HOME/include/jni.h

echo "检查 jni_md.h (Linux):"
ls -l $JAVA_HOME/include/linux/jni_md.h

echo "检查 libjvm.so:"
find $JAVA_HOME -name "libjvm.so"
```

## 如果JDK结构不同

HuaweiJDK可能有不同的目录结构，检查实际路径：

```bash
# 查找jni头文件位置
find /home/vastbase/binarylibs/platform/huaweijdk8 -name "jni.h"
find /home/vastbase/binarylibs/platform/huaweijdk8 -name "jni_md.h"
find /home/vastbase/binarylibs/platform/huaweijdk8 -name "libjvm.so"
```

然后根据实际路径修改 `config.sh` 中的 `JAVA_HOME`。

## 编译成功后

安装扩展：

```bash
# 安装到VastBase
make install

# 配置运行时库路径
export LD_LIBRARY_PATH=$JAVA_HOME/jre/lib/amd64/server:$LD_LIBRARY_PATH

# 或者找到libjvm.so的实际位置
export LD_LIBRARY_PATH=$(dirname $(find $JAVA_HOME -name "libjvm.so" | head -1)):$LD_LIBRARY_PATH

# 在数据库中创建扩展
vsql -d your_database -c "CREATE EXTENSION vastbase_sm4;"
```

## 常见问题

### Q: 编译时提示找不到 pg_config
**A**: 确保VastBase的bin目录在PATH中：
```bash
export PATH=/home/vastbase/vasthome/bin:$PATH
```

### Q: 运行时提示找不到 libjvm.so
**A**: 设置LD_LIBRARY_PATH或在postgresql.conf中配置：
```bash
export LD_LIBRARY_PATH=$JAVA_HOME/jre/lib/amd64/server:$LD_LIBRARY_PATH
```

### Q: CREATE EXTENSION 失败
**A**: 检查jar包路径，修改 `src/vastbase_sm4.c` 中的 `jar_path` 变量为绝对路径
