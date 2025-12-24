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
JAVA_HOME ?= C:/Program Files/Java/jdk-11
JAVA_JAR = ../dis-algorithm/target/dis-algorithm-1.0.0.0.jar

# 包含JNI头文件
PG_CPPFLAGS = -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/win32"
# 链接JVM库
SHLIB_LINK = -L"$(JAVA_HOME)/lib" -ljvm

# Windows特殊处理
ifeq ($(OS),Windows_NT)
    # Windows下需要添加jvm.dll的路径到运行时搜索路径
    PG_CPPFLAGS += -DWIN32
endif

# 包含PostgreSQL扩展构建系统
include $(PGXS)

# 自定义目标：在安装前显示配置信息
.PHONY: show-config
show-config:
	@echo "===================================="
	@echo "VastBase SM4 Extension Build Config"
	@echo "===================================="
	@echo "PG_CONFIG: $(PG_CONFIG)"
	@echo "PGXS: $(PGXS)"
	@echo "JAVA_HOME: $(JAVA_HOME)"
	@echo "JAVA_JAR: $(JAVA_JAR)"
	@echo "Extension: $(EXTENSION)"
	@echo "Module: $(MODULE_big)"
	@echo "===================================="
