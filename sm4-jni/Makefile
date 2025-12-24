# SM4 JNI Makefile for Windows (MinGW)
# 需要先设置JAVA_HOME环境变量

CC = gcc
JAVA_HOME ?= C:/Program Files/Java/jdk-11
TARGET = sm4_jni.dll
JAVA_JAR = ../dis-algorithm/target/dis-algorithm-1.0.0.0.jar

# 源文件
SOURCES = src/sm4_jni.c src/sm4_wrapper.c
OBJECTS = $(SOURCES:.c=.o)

# 包含路径
INCLUDES = -I"$(JAVA_HOME)/include" \
           -I"$(JAVA_HOME)/include/win32" \
           -Iinclude

# 链接库
LIBS = -L"$(JAVA_HOME)/lib" -ljvm

# 编译选项
CFLAGS = -Wall -O2 -fPIC $(INCLUDES)
LDFLAGS = -shared $(LIBS)

# 默认目标
all: $(TARGET) test_sm4 simple_example

# 生成动态库
$(TARGET): $(OBJECTS)
	$(CC) $(LDFLAGS) -o $@ $^

# 编译目标文件
%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

# 编译测试程序
test_sm4: test/test_sm4.c $(OBJECTS)
	$(CC) $(CFLAGS) -o test_sm4.exe test/test_sm4.c src/sm4_wrapper.c src/sm4_jni.c $(LIBS)

# 编译示例程序
simple_example: examples/simple_example.c $(OBJECTS)
	$(CC) $(CFLAGS) -o simple_example.exe examples/simple_example.c src/sm4_wrapper.c src/sm4_jni.c $(LIBS)

# 清理
clean:
	del /Q src\*.o $(TARGET) test_sm4.exe simple_example.exe 2>nul || echo Cleaned

# 运行测试
test: test_sm4
	set PATH=$(JAVA_HOME)\bin;$(JAVA_HOME)\jre\bin\server;%PATH% && \
	set CLASSPATH=$(JAVA_JAR);%CLASSPATH% && \
	test_sm4.exe

# 运行示例
run-example: simple_example
	set PATH=$(JAVA_HOME)\bin;$(JAVA_HOME)\jre\bin\server;%PATH% && \
	set CLASSPATH=$(JAVA_JAR);%CLASSPATH% && \
	simple_example.exe

.PHONY: all clean test run-example
