#!/bin/bash
#
# VastBase SM4 Extension - 快速安装脚本
# 适用于 Linux 环境
#

set -e

echo "========================================"
echo "VastBase SM4 Extension Quick Start"
echo "========================================"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查函数
check_command() {
    if command -v $1 &> /dev/null; then
        echo -e "${GREEN}✓${NC} $1 已安装"
        return 0
    else
        echo -e "${RED}✗${NC} $1 未安装"
        return 1
    fi
}

# 步骤1: 检查依赖
echo "步骤1: 检查系统依赖..."
echo "-----------------------------"

ALL_OK=true

if ! check_command java; then
    echo "  请安装JDK: sudo yum install java-11-openjdk"
    ALL_OK=false
fi

if ! check_command javac; then
    echo "  请安装JDK开发包: sudo yum install java-11-openjdk-devel"
    ALL_OK=false
fi

if ! check_command mvn; then
    echo "  请安装Maven: sudo yum install maven"
    ALL_OK=false
fi

if ! check_command pg_config; then
    echo "  请安装PostgreSQL开发包: sudo yum install postgresql-devel"
    ALL_OK=false
fi

if ! check_command gcc; then
    echo "  请安装GCC: sudo yum install gcc"
    ALL_OK=false
fi

if [ "$ALL_OK" = false ]; then
    echo ""
    echo -e "${RED}错误: 缺少必要的依赖，请先安装${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}所有依赖检查通过！${NC}"
echo ""

# 步骤2: 检查环境变量
echo "步骤2: 检查环境变量..."
echo "-----------------------------"

if [ -z "$JAVA_HOME" ]; then
    echo -e "${YELLOW}警告: JAVA_HOME 未设置，尝试自动检测...${NC}"
    JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
    export JAVA_HOME
    echo "  设置 JAVA_HOME=$JAVA_HOME"
fi

echo -e "${GREEN}✓${NC} JAVA_HOME=$JAVA_HOME"
echo -e "${GREEN}✓${NC} PG_CONFIG=$(which pg_config)"
echo ""

# 步骤3: 编译dis-algorithm
echo "步骤3: 编译dis-algorithm..."
echo "-----------------------------"

if [ -f "../dis-algorithm/target/dis-algorithm-1.0.0.0.jar" ]; then
    echo -e "${GREEN}✓${NC} dis-algorithm jar包已存在"
else
    echo "  编译dis-algorithm..."
    cd ../dis-algorithm
    mvn clean package -DskipTests
    cd ../vastbase-sm4-extension
    echo -e "${GREEN}✓${NC} dis-algorithm 编译完成"
fi
echo ""

# 步骤4: 编译扩展
echo "步骤4: 编译SM4扩展..."
echo "-----------------------------"

make clean
make

if [ -f "vastbase_sm4.so" ]; then
    echo -e "${GREEN}✓${NC} 扩展编译成功"
else
    echo -e "${RED}✗${NC} 编译失败，请检查错误信息"
    exit 1
fi
echo ""

# 步骤5: 提示安装
echo "步骤5: 安装扩展..."
echo "-----------------------------"
echo ""
echo "编译完成！现在需要管理员权限安装扩展。"
echo ""
echo "请执行以下命令："
echo ""
echo -e "${YELLOW}  sudo make install${NC}"
echo ""
echo "然后在数据库中执行："
echo ""
echo -e "${YELLOW}  CREATE EXTENSION vastbase_sm4;${NC}"
echo ""
echo "========================================"
echo "配置提示"
echo "========================================"
echo ""
echo "1. 确保数据库能找到JVM库："
echo "   编辑 postgresql.conf 添加:"
echo "   env = 'LD_LIBRARY_PATH=$JAVA_HOME/lib/server'"
echo ""
echo "2. 重启数据库："
echo "   sudo systemctl restart postgresql"
echo ""
echo "3. 测试安装："
echo "   SELECT sm4_generate_key();"
echo ""
echo -e "${GREEN}祝您使用愉快！${NC}"
