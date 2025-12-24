@echo off
REM SM4 JNI 编译和运行脚本 (Windows)

echo ========================================
echo SM4 JNI Build Script for Windows
echo ========================================

REM 检查JAVA_HOME
if "%JAVA_HOME%"=="" (
    echo 错误: JAVA_HOME 环境变量未设置！
    echo 请设置 JAVA_HOME 指向JDK安装目录
    echo 例如: set JAVA_HOME=C:\Program Files\Java\jdk-11
    exit /b 1
)

echo JAVA_HOME: %JAVA_HOME%

REM 检查dis-algorithm jar包
if not exist "..\dis-algorithm\target\dis-algorithm-1.0.0.0.jar" (
    echo 警告: dis-algorithm jar包不存在，尝试编译...
    cd ..\dis-algorithm
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo 错误: dis-algorithm 编译失败！
        exit /b 1
    )
    cd ..\sm4-jni
    echo dis-algorithm 编译成功！
)

REM 编译C项目
echo.
echo 开始编译SM4 JNI...
make clean
make

if errorlevel 1 (
    echo 错误: 编译失败！
    exit /b 1
)

echo.
echo ========================================
echo 编译成功！
echo ========================================
echo.
echo 生成的文件:
dir /b sm4_jni.dll test_sm4.exe 2>nul
echo.
echo 运行测试: build.bat test
echo 运行示例: build.bat example
echo.

if "%1"=="test" goto run_test
if "%1"=="example" goto run_example
goto end

:run_test
echo 运行测试...
set PATH=%JAVA_HOME%\bin;%JAVA_HOME%\jre\bin\server;%PATH%
set CLASSPATH=..\dis-algorithm\target\dis-algorithm-1.0.0.0.jar;%CLASSPATH%
test_sm4.exe
goto end

:run_example
echo 运行示例...
set PATH=%JAVA_HOME%\bin;%JAVA_HOME%\jre\bin\server;%PATH%
set CLASSPATH=..\dis-algorithm\target\dis-algorithm-1.0.0.0.jar;%CLASSPATH%
simple_example.exe
goto end

:end
