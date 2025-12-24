#!/bin/bash
# SM4 JNI 编译和运行脚本 (Linux/macOS)

echo "========================================"
echo "SM4 JNI Build Script for Linux/macOS"
echo "========================================"

# 检查JAVA_HOME
if [ -z "$JAVA_HOME" ]; then
    echo "错误: JAVA_HOME 环境变量未设置！"
    echo "请设置 JAVA_HOME 指向JDK安装目录"
    echo "例如: export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64"
    exit 1
fi

echo "JAVA_HOME: $JAVA_HOME"

# 检查dis-algorithm jar包
if [ ! -f "../dis-algorithm/target/dis-algorithm-1.0.0.0.jar" ]; then
    echo "警告: dis-algorithm jar包不存在，尝试编译..."
    cd ../dis-algorithm
    mvn clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "错误: dis-algorithm 编译失败！"
        exit 1
    fi
    cd ../sm4-jni
    echo "dis-algorithm 编译成功！"
fi

# 使用Linux Makefile
echo ""
echo "开始编译SM4 JNI..."
make -f Makefile.linux clean
make -f Makefile.linux

if [ $? -ne 0 ]; then
    echo "错误: 编译失败！"
    exit 1
fi

echo ""
echo "========================================"
echo "编译成功！"
echo "========================================"
echo ""
echo "生成的文件:"
ls -lh libsm4_jni.* test_sm4 simple_example 2>/dev/null
echo ""
echo "运行测试: ./build.sh test"
echo "运行示例: ./build.sh example"
echo ""

# 根据参数运行
if [ "$1" == "test" ]; then
    echo "运行测试..."
    export LD_LIBRARY_PATH=$JAVA_HOME/lib/server:$LD_LIBRARY_PATH
    export CLASSPATH=../dis-algorithm/target/dis-algorithm-1.0.0.0.jar:$CLASSPATH
    ./test_sm4
elif [ "$1" == "example" ]; then
    echo "运行示例..."
    export LD_LIBRARY_PATH=$JAVA_HOME/lib/server:$LD_LIBRARY_PATH
    export CLASSPATH=../dis-algorithm/target/dis-algorithm-1.0.0.0.jar:$CLASSPATH
    ./simple_example
fi
