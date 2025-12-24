#!/bin/bash
#
# VastBase SM4 Extension - 编译问题诊断和修复脚本
#

echo "========================================"
echo "VastBase SM4 扩展编译问题诊断"
echo "========================================"
echo ""

# 1. 检测JDK路径
echo "步骤1: 检测JDK路径..."
echo "-----------------------------"

# VastBase常见的JDK路径
POSSIBLE_JDK_PATHS=(
    "/opt/jdk11"
    "/opt/jdk"
    "/home/vastbase/binarylibs/platform/huaweijdk8/x86_64/jdk"
    "/home/vastbase/binarylibs/platform/huaweijdk/x86_64/jdk"
    "/usr/lib/jvm/java-11-openjdk-amd64"
    "/usr/lib/jvm/java-11-openjdk"
    "/usr/lib/jvm/java-1.8.0-openjdk"
    "$JAVA_HOME"
)

# 如果环境变量已设置，优先使用
if [ -n "$JAVA_HOME" ] && [ -d "$JAVA_HOME" ]; then
    echo "✓ 使用环境变量中的JAVA_HOME: $JAVA_HOME"
else
    # 尝试在常见路径中查找
    FOUND=false
    for path in "${POSSIBLE_JDK_PATHS[@]}"; do
        if [ -d "$path" ] && [ -f "$path/include/jni.h" ]; then
            export JAVA_HOME="$path"
            echo "✓ 检测到JDK: $JAVA_HOME"
            FOUND=true
            break
        fi
    done
    
    if [ "$FOUND" = false ]; then
        echo "✗ 无法自动检测JDK路径"
        echo ""
        echo "请手动设置JAVA_HOME，例如:"
        echo "  export JAVA_HOME=/home/vastbase/binarylibs/platform/huaweijdk8/x86_64/jdk"
        echo ""
        echo "或者查找JDK位置:"
        echo "  find /home/vastbase -name 'jni.h' 2>/dev/null | head -5"
        exit 1
    fi
fi

echo ""

# 2. 验证JDK目录结构
echo "步骤2: 验证JDK目录结构..."
echo "-----------------------------"

echo "JAVA_HOME: $JAVA_HOME"

# 检查jni.h
if [ -f "$JAVA_HOME/include/jni.h" ]; then
    echo "✓ jni.h: $JAVA_HOME/include/jni.h"
else
    echo "✗ 找不到 jni.h"
    exit 1
fi

# 检查jni_md.h (Linux)
if [ -f "$JAVA_HOME/include/linux/jni_md.h" ]; then
    echo "✓ jni_md.h: $JAVA_HOME/include/linux/jni_md.h"
else
    echo "✗ 找不到 jni_md.h"
    echo "  可能的位置:"
    find "$JAVA_HOME" -name "jni_md.h" 2>/dev/null || echo "  未找到"
    exit 1
fi

# 检查libjvm.so
JVM_SO=$(find "$JAVA_HOME" -name "libjvm.so" 2>/dev/null | head -1)
if [ -n "$JVM_SO" ]; then
    echo "✓ libjvm.so: $JVM_SO"
    JVM_DIR=$(dirname "$JVM_SO")
    export LD_LIBRARY_PATH="$JVM_DIR:$LD_LIBRARY_PATH"
else
    echo "✗ 找不到 libjvm.so"
    exit 1
fi

echo ""

# 3. 更新Makefile
echo "步骤3: 配置Makefile..."
echo "-----------------------------"

# 创建临时Makefile.local覆盖JAVA_HOME
cat > Makefile.local << EOF
# 自动生成的配置文件
# 由 fix-compile.sh 创建

# 覆盖JAVA_HOME
JAVA_HOME := $JAVA_HOME

# 添加JNI包含路径
PG_CPPFLAGS += -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/linux"

# 添加JVM链接库
SHLIB_LINK += -L"$JVM_DIR" -ljvm -Wl,-rpath,"$JVM_DIR"

EOF

echo "✓ 已创建 Makefile.local"
echo ""

# 4. 修改主Makefile以包含local配置
if ! grep -q "Makefile.local" Makefile; then
    echo "# 包含本地配置（如果存在）" >> Makefile
    echo "-include Makefile.local" >> Makefile
    echo "✓ 已更新 Makefile"
fi

echo ""

# 5. 显示配置摘要
echo "步骤4: 配置摘要"
echo "-----------------------------"
echo "JAVA_HOME=$JAVA_HOME"
echo "LD_LIBRARY_PATH=$LD_LIBRARY_PATH"
echo ""

# 6. 提示下一步操作
echo "========================================"
echo "配置完成！现在请执行:"
echo "========================================"
echo ""
echo "  export JAVA_HOME=$JAVA_HOME"
echo "  export LD_LIBRARY_PATH=$LD_LIBRARY_PATH"
echo ""
echo "  make clean"
echo "  make"
echo ""
echo "或者直接运行:"
echo ""
echo "  source <(./fix-compile.sh) && make clean && make"
echo ""
