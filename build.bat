@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo 汇川S3 PLC数据采集程序 构建脚本
echo ========================================
echo.

:: 设置变量
set PROJECT_NAME=S3PLCDataCollector
set OUTPUT_DIR=bin\Release\net6.0-windows
set PUBLISH_DIR=publish
set BUILD_CONFIG=Release

echo 项目名称: %PROJECT_NAME%
echo 构建配置: %BUILD_CONFIG%
echo 输出目录: %OUTPUT_DIR%
echo 发布目录: %PUBLISH_DIR%
echo.

:: 检查.NET SDK
echo 检查.NET SDK...
dotnet --version >nul 2>&1
if %errorLevel% neq 0 (
    echo 错误：未找到.NET SDK！
    echo 请安装.NET 6.0 SDK或更高版本
    echo 下载地址: https://dotnet.microsoft.com/download
    echo.
    pause
    exit /b 1
)

echo .NET SDK版本:
dotnet --version
echo.

:: 检查项目文件
if not exist "%PROJECT_NAME%.csproj" (
    echo 错误：找不到项目文件 %PROJECT_NAME%.csproj
    echo 请确保在项目根目录下运行此脚本
    echo.
    pause
    exit /b 1
)

:: 清理之前的构建
echo 清理之前的构建...
if exist "bin" rmdir /s /q "bin" >nul 2>&1
if exist "obj" rmdir /s /q "obj" >nul 2>&1
if exist "%PUBLISH_DIR%" rmdir /s /q "%PUBLISH_DIR%" >nul 2>&1
echo 清理完成
echo.

:: 还原NuGet包
echo 还原NuGet包...
dotnet restore
if %errorLevel% neq 0 (
    echo 错误：NuGet包还原失败！
    echo.
    pause
    exit /b 1
)
echo NuGet包还原成功
echo.

:: 构建项目
echo 构建项目...
dotnet build --configuration %BUILD_CONFIG% --no-restore
if %errorLevel% neq 0 (
    echo 错误：项目构建失败！
    echo.
    pause
    exit /b 1
)
echo 项目构建成功
echo.

:: 发布项目（自包含）
echo 发布项目（自包含部署）...
dotnet publish --configuration %BUILD_CONFIG% --output "%PUBLISH_DIR%" --self-contained true --runtime win-x64 --no-restore
if %errorLevel% neq 0 (
    echo 错误：项目发布失败！
    echo.
    pause
    exit /b 1
)
echo 项目发布成功
echo.

:: 复制配置文件和脚本
echo 复制配置文件和脚本...

:: 复制数据库架构文件
if exist "database_schema.sql" (
    copy "database_schema.sql" "%PUBLISH_DIR%\" >nul
    echo 已复制: database_schema.sql
)

:: 复制安装脚本
if exist "install_service.bat" (
    copy "install_service.bat" "%PUBLISH_DIR%\" >nul
    echo 已复制: install_service.bat
)

if exist "uninstall_service.bat" (
    copy "uninstall_service.bat" "%PUBLISH_DIR%\" >nul
    echo 已复制: uninstall_service.bat
)

:: 复制图标文件
if exist "icon.svg" (
    copy "icon.svg" "%PUBLISH_DIR%\" >nul
    echo 已复制: icon.svg
)

:: 创建启动脚本
echo 创建启动脚本...
echo @echo off > "%PUBLISH_DIR%\start.bat"
echo cd /d "%%~dp0" >> "%PUBLISH_DIR%\start.bat"
echo "%PROJECT_NAME%.exe" >> "%PUBLISH_DIR%\start.bat"
echo 已创建: start.bat

:: 创建服务模式启动脚本
echo @echo off > "%PUBLISH_DIR%\start_service.bat"
echo cd /d "%%~dp0" >> "%PUBLISH_DIR%\start_service.bat"
echo "%PROJECT_NAME%.exe" --service >> "%PUBLISH_DIR%\start_service.bat"
echo 已创建: start_service.bat

echo 文件复制完成
echo.

:: 显示构建结果
echo ========================================
echo 构建完成！
echo ========================================
echo.
echo 发布目录: %PUBLISH_DIR%
echo.
echo 主要文件:
dir "%PUBLISH_DIR%\%PROJECT_NAME%.exe" 2>nul | find "%PROJECT_NAME%.exe"
dir "%PUBLISH_DIR%\appsettings.json" 2>nul | find "appsettings.json"
dir "%PUBLISH_DIR%\database_schema.sql" 2>nul | find "database_schema.sql"
echo.
echo 部署说明:
echo 1. 将 %PUBLISH_DIR% 目录复制到目标计算机
echo 2. 运行 start.bat 启动GUI模式
echo 3. 以管理员身份运行 install_service.bat 安装Windows服务
echo 4. 使用 uninstall_service.bat 卸载Windows服务
echo.
echo 注意事项:
echo 1. 目标计算机无需安装.NET运行时（自包含部署）
echo 2. 首次运行会自动创建数据库和配置文件
echo 3. 确保目标计算机防火墙允许程序访问网络
echo 4. 建议在安装服务前先测试GUI模式
echo.

:: 询问是否打开发布目录
set /p open_dir=是否打开发布目录？(Y/N): 
if /i "%open_dir%"=="Y" (
    explorer "%PUBLISH_DIR%"
)

echo.
pause