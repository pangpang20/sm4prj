# VastBase SM4 Extension - 环境配置
# 请根据您的实际环境修改这些变量

# 1. JDK路径配置
# 从编译错误中提取到的HuaweiJDK路径
export JAVA_HOME=/home/vastbase/binarylibs/platform/huaweijdk8/x86_64/jdk

# 2. 验证JDK路径
if [ ! -d "$JAVA_HOME" ]; then
    echo "错误: JAVA_HOME 路径不存在: $JAVA_HOME"
    echo "请检查并修改 config.sh 中的 JAVA_HOME 配置"
    return 1
fi

# 3. 检查JNI头文件
if [ ! -f "$JAVA_HOME/include/jni.h" ]; then
    echo "错误: 找不到 jni.h 文件"
    echo "检查路径: $JAVA_HOME/include/jni.h"
    return 1
fi

# 4. 配置库路径
# HuaweiJDK可能使用不同的目录结构
if [ -d "$JAVA_HOME/jre/lib/amd64/server" ]; then
    export LD_LIBRARY_PATH=$JAVA_HOME/jre/lib/amd64/server:$LD_LIBRARY_PATH
elif [ -d "$JAVA_HOME/lib/server" ]; then
    export LD_LIBRARY_PATH=$JAVA_HOME/lib/server:$LD_LIBRARY_PATH
elif [ -d "$JAVA_HOME/lib/amd64/server" ]; then
    export LD_LIBRARY_PATH=$JAVA_HOME/lib/amd64/server:$LD_LIBRARY_PATH
fi

# 5. dis-algorithm jar包路径
export SM4_JAR_PATH=$(cd "$(dirname "${BASH_SOURCE[0]}")/../dis-algorithm/target" && pwd)/dis-algorithm-1.0.0.0.jar

# 6. 显示配置信息
echo "========================================"
echo "VastBase SM4 Extension 环境配置"
echo "========================================"
echo "JAVA_HOME: $JAVA_HOME"
echo "LD_LIBRARY_PATH: $LD_LIBRARY_PATH"
echo "SM4_JAR_PATH: $SM4_JAR_PATH"
echo ""

# 7. 验证JNI头文件
if [ -f "$JAVA_HOME/include/linux/jni_md.h" ]; then
    echo "✓ JNI头文件: $JAVA_HOME/include/linux/jni_md.h"
else
    echo "✗ 警告: 找不到 jni_md.h"
fi

# 8. 验证libjvm.so
JVM_SO=$(find $JAVA_HOME -name "libjvm.so" 2>/dev/null | head -1)
if [ -n "$JVM_SO" ]; then
    echo "✓ JVM库: $JVM_SO"
else
    echo "✗ 警告: 找不到 libjvm.so"
fi

echo "========================================"
echo ""
echo "使用方法:"
echo "  source config.sh"
echo "  make clean"
echo "  make"
echo ""
