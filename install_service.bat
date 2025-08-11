@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo 汇川S3 PLC数据采集服务 安装脚本
echo ========================================
echo.

:: 检查管理员权限
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo 错误：需要管理员权限才能安装Windows服务！
    echo 请右键点击此脚本，选择"以管理员身份运行"
    echo.
    pause
    exit /b 1
)

:: 设置变量
set SERVICE_NAME=S3PLCDataCollectorService
set SERVICE_DISPLAY_NAME=汇川S3 PLC数据采集服务
set SERVICE_DESCRIPTION=自动采集汇川S3 PLC数据并保存到数据库
set EXE_PATH=%~dp0S3PLCDataCollector.exe

echo 当前目录: %~dp0
echo 可执行文件路径: %EXE_PATH%
echo.

:: 检查可执行文件是否存在
if not exist "%EXE_PATH%" (
    echo 错误：找不到可执行文件 S3PLCDataCollector.exe
    echo 请确保此脚本与程序文件在同一目录下
    echo.
    pause
    exit /b 1
)

:: 检查服务是否已存在
sc query "%SERVICE_NAME%" >nul 2>&1
if %errorLevel% equ 0 (
    echo 检测到服务已存在，正在卸载旧服务...
    
    :: 停止服务
    echo 正在停止服务...
    sc stop "%SERVICE_NAME%" >nul 2>&1
    timeout /t 3 /nobreak >nul
    
    :: 删除服务
    echo 正在删除旧服务...
    sc delete "%SERVICE_NAME%" >nul 2>&1
    if %errorLevel% neq 0 (
        echo 警告：删除旧服务失败，继续安装新服务
    ) else (
        echo 旧服务删除成功
    )
    
    :: 等待服务完全删除
    timeout /t 2 /nobreak >nul
    echo.
)

:: 安装新服务
echo 正在安装Windows服务...
sc create "%SERVICE_NAME%" binPath= "\"%EXE_PATH%\" --service" DisplayName= "%SERVICE_DISPLAY_NAME%" start= auto depend= Tcpip

if %errorLevel% neq 0 (
    echo 错误：服务安装失败！
    echo 错误代码: %errorLevel%
    echo.
    pause
    exit /b 1
)

echo 服务安装成功！
echo.

:: 设置服务描述
echo 正在设置服务描述...
sc description "%SERVICE_NAME%" "%SERVICE_DESCRIPTION%"

:: 设置服务恢复选项
echo 正在配置服务恢复选项...
sc failure "%SERVICE_NAME%" reset= 86400 actions= restart/5000/restart/10000/restart/20000

:: 启动服务
echo 正在启动服务...
sc start "%SERVICE_NAME%"

if %errorLevel% neq 0 (
    echo 警告：服务启动失败！
    echo 错误代码: %errorLevel%
    echo 请检查程序配置或手动启动服务
    echo.
) else (
    echo 服务启动成功！
    echo.
)

:: 显示服务状态
echo 当前服务状态:
sc query "%SERVICE_NAME%"
echo.

echo ========================================
echo 安装完成！
echo ========================================
echo.
echo 服务名称: %SERVICE_NAME%
echo 显示名称: %SERVICE_DISPLAY_NAME%
echo 可执行文件: %EXE_PATH%
echo.
echo 您可以通过以下方式管理服务:
echo 1. 使用 services.msc 图形界面
echo 2. 使用命令行:
echo    启动服务: sc start "%SERVICE_NAME%"
echo    停止服务: sc stop "%SERVICE_NAME%"
echo    查看状态: sc query "%SERVICE_NAME%"
echo    卸载服务: sc delete "%SERVICE_NAME%"
echo.
echo 注意：卸载服务前请先停止服务！
echo.

pause