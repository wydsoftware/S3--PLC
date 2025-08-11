@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo 汇川S3 PLC数据采集服务 卸载脚本
echo ========================================
echo.

:: 检查管理员权限
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo 错误：需要管理员权限才能卸载Windows服务！
    echo 请右键点击此脚本，选择"以管理员身份运行"
    echo.
    pause
    exit /b 1
)

:: 设置变量
set SERVICE_NAME=S3PLCDataCollectorService
set SERVICE_DISPLAY_NAME=汇川S3 PLC数据采集服务

echo 服务名称: %SERVICE_NAME%
echo 显示名称: %SERVICE_DISPLAY_NAME%
echo.

:: 检查服务是否存在
sc query "%SERVICE_NAME%" >nul 2>&1
if %errorLevel% neq 0 (
    echo 服务不存在或已被卸载
    echo.
    pause
    exit /b 0
)

:: 显示当前服务状态
echo 当前服务状态:
sc query "%SERVICE_NAME%"
echo.

:: 确认卸载
set /p confirm=确定要卸载服务吗？(Y/N): 
if /i not "%confirm%"=="Y" (
    echo 取消卸载操作
    echo.
    pause
    exit /b 0
)

echo.
echo 开始卸载服务...
echo.

:: 停止服务
echo 正在停止服务...
sc stop "%SERVICE_NAME%"

if %errorLevel% equ 0 (
    echo 服务停止成功
    
    :: 等待服务完全停止
    echo 等待服务完全停止...
    timeout /t 5 /nobreak >nul
    
    :: 再次检查服务状态
    for /f "tokens=4" %%i in ('sc query "%SERVICE_NAME%" ^| find "STATE"') do (
        if "%%i"=="STOPPED" (
            echo 服务已完全停止
        ) else (
            echo 警告：服务可能未完全停止，状态: %%i
            echo 等待更长时间...
            timeout /t 5 /nobreak >nul
        )
    )
else (
    echo 警告：停止服务失败或服务已停止
    echo 错误代码: %errorLevel%
    echo 继续尝试删除服务...
)

echo.

:: 删除服务
echo 正在删除服务...
sc delete "%SERVICE_NAME%"

if %errorLevel% equ 0 (
    echo 服务删除成功！
    echo.
    
    :: 等待服务注册表项完全删除
    echo 等待系统更新服务注册表...
    timeout /t 3 /nobreak >nul
    
    :: 验证服务是否已删除
    sc query "%SERVICE_NAME%" >nul 2>&1
    if %errorLevel% neq 0 (
        echo 验证：服务已完全卸载
    ) else (
        echo 警告：服务可能仍在系统中，请重启计算机后再次检查
    )
else (
    echo 错误：删除服务失败！
    echo 错误代码: %errorLevel%
    echo.
    echo 可能的原因:
    echo 1. 服务仍在运行中
    echo 2. 服务被其他程序占用
    echo 3. 权限不足
    echo.
    echo 建议:
    echo 1. 重启计算机后再次尝试
    echo 2. 使用任务管理器结束相关进程
    echo 3. 检查服务依赖关系
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo 卸载完成！
echo ========================================
echo.
echo 服务 "%SERVICE_DISPLAY_NAME%" 已成功卸载
echo.
echo 注意事项:
echo 1. 程序文件和配置文件仍保留在磁盘上
echo 2. 数据库文件和日志文件未被删除
echo 3. 如需完全清理，请手动删除程序目录
echo.
echo 如需重新安装服务，请运行 install_service.bat
echo.

pause