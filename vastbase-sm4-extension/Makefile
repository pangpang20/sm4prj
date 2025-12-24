# VastBase/PostgreSQL SM4 Extension Makefile

# 扩展名称
EXTENSION = vastbase_sm4
# 扩展数据文件（SQL安装脚本）
DATA = vastbase_sm4--1.0.sql
# 模块名称（编译后的共享库名）
MODULE_big = vastbase_sm4
# 源文件
OBJS = src/vastbase_sm4.o src/sm4_jni_wrapper.o

# PostgreSQL扩展构建系统
PG_CONFIG ?= pg_config
PGXS := $(shell $(PG_CONFIG) --pgxs)

# JDK配置
# 自动检测操作系统和JDK路径
UNAME_S := $(shell uname -s)

# 默认JAVA_HOME（如果环境变量未设置）
ifndef JAVA_HOME
	$(error JAVA_HOME is not set. Please export JAVA_HOME explicitly.)
endif

# JAR路径配置 - 从环境变量UDF_JAR读取，否则使用默认值
UDF_JAR ?= /home/vastbase/dis-algorithm-1.0.0.0.jar

# 强制使用C++11标准（GCC 4.8.5不支持C++14）
override CXXFLAGS := $(filter-out -std=c++14,$(CXXFLAGS)) -std=c++11

# 根据操作系统配置JNI路径
ifeq ($(UNAME_S),Linux)
    # Linux系统
    PG_CPPFLAGS += -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/linux"
    SHLIB_LINK += -L"$(JAVA_HOME)/lib/server" -L"$(JAVA_HOME)/jre/lib/amd64/server" -ljvm
    # 添加rpath，运行时能找到libjvm.so
    SHLIB_LINK += -Wl,-rpath,"$(JAVA_HOME)/lib/server" -Wl,-rpath,"$(JAVA_HOME)/jre/lib/amd64/server"
else ifeq ($(UNAME_S),Darwin)
    # macOS系统
    PG_CPPFLAGS += -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/darwin"
    SHLIB_LINK += -L"$(JAVA_HOME)/lib/server" -ljvm
    SHLIB_LINK += -Wl,-rpath,"$(JAVA_HOME)/lib/server"
else
    # Windows系统
    PG_CPPFLAGS += -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/win32" -DWIN32
    SHLIB_LINK += -L"$(JAVA_HOME)/lib" -ljvm
endif

# 包含PostgreSQL扩展构建系统
include $(PGXS)

# 必须在include $(PGXS)之后再次覆盖C++标准
# 移除PGXS可能设置的-std=c++14，强制使用-std=c++11
override CXXFLAGS := $(filter-out -std=c++14,$(CXXFLAGS)) -std=c++11
override CFLAGS := $(filter-out -std=c++14,$(CFLAGS))

# 必须在include $(PGXS)之后再次覆盖JNI路径
# 因为VastBase的PGXS会在CPPFLAGS中硬编码huaweijdk路径
# 我们需要强制覆盖编译规则

# 自定义编译规则，强制使用我们的JAVA_HOME
# VastBase需要用g++编译（支持C++特性），JNI代码使用C++风格API
# 添加 -fvisibility=default 确保函数符号可见
src/%.o: src/%.c
	@echo "Compiling $< with JAVA_HOME=$(JAVA_HOME)"
	g++ -std=c++11 -D_GLIBCXX_USE_CXX11_ABI=0 -fvisibility=default \
		-I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/linux" \
		-I/home/vastbase/vasthome/include/postgresql/server \
		-I/home/vastbase/vasthome/include/postgresql/internal \
		-D_GNU_SOURCE -Wall -fPIC -c -o $@ $<

# 自定义目标：显示配置信息
.PHONY: show-config
show-config:
	@echo "===================================="
	@echo "VastBase SM4 Extension Build Config"
	@echo "===================================="
	@echo "[编译时配置]"
	@echo "PG_CONFIG: $(PG_CONFIG)"
	@echo "PGXS: $(PGXS)"
	@echo "JAVA_HOME: $(JAVA_HOME)"
	@echo "Extension: $(EXTENSION)"
	@echo "Module: $(MODULE_big)"
	@echo ""
	@echo "[运行时配置 - 启动数据库前设置]"
	@echo "UDF_JAR: $(UDF_JAR)"
	@echo "  示例: export UDF_JAR=/path/to/dis-algorithm-1.0.0.0.jar"
	@echo ""
	@echo "LD_LIBRARY_PATH: 必须包含libjvm.so路径"
	@echo "  示例: export LD_LIBRARY_PATH=\$$JAVA_HOME/lib/server:\$$LD_LIBRARY_PATH"
	@echo "===================================="
