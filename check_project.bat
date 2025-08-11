@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo 汇川S3 PLC数据采集程序 项目检查
echo ========================================
echo.

:: 检查.NET SDK
echo 检查.NET SDK安装状态...
dotnet --version >nul 2>&1
if %errorLevel% neq 0 (
    echo [警告] 未检测到.NET SDK！
    echo.
    echo 请按照以下步骤安装.NET 6.0 SDK:
    echo 1. 访问: https://dotnet.microsoft.com/download/dotnet/6.0
    echo 2. 下载并安装 .NET 6.0 SDK (x64)
    echo 3. 重启命令提示符或PowerShell
    echo 4. 运行 'dotnet --version' 验证安装
    echo.
) else (
    echo [成功] .NET SDK已安装
    echo 版本: 
    dotnet --version
    echo.
)

:: 检查项目文件
echo 检查项目文件结构...
echo.

set "files_to_check=S3PLCDataCollector.csproj appsettings.json database_schema.sql Program.cs App.xaml App.xaml.cs"
set "missing_files="
set "found_files="

for %%f in (%files_to_check%) do (
    if exist "%%f" (
        echo [✓] %%f
        set "found_files=!found_files! %%f"
    ) else (
        echo [✗] %%f (缺失)
        set "missing_files=!missing_files! %%f"
    )
)

echo.

:: 检查源代码目录
echo 检查源代码目录...
if exist "Models" (
    echo [✓] Models 目录
    dir /b "Models\*.cs" 2>nul | find /c ".cs" >nul && echo     - 包含 C# 文件
) else (
    echo [✗] Models 目录 (缺失)
)

if exist "Services" (
    echo [✓] Services 目录
    dir /b "Services\*.cs" 2>nul | find /c ".cs" >nul && echo     - 包含 C# 文件
) else (
    echo [✗] Services 目录 (缺失)
)

echo.

:: 检查脚本文件
echo 检查管理脚本...
if exist "build.bat" echo [✓] build.bat
if exist "install_service.bat" echo [✓] install_service.bat
if exist "uninstall_service.bat" echo [✓] uninstall_service.bat

echo.

:: 统计结果
echo ========================================
echo 检查结果汇总
echo ========================================

if "%missing_files%"=="" (
    echo [成功] 所有核心文件都存在
) else (
    echo [警告] 以下文件缺失:%missing_files%
)

echo.
echo 项目文件总数:
dir /b *.cs *.xaml *.json *.sql *.bat 2>nul | find /c "." 

echo.
echo 下一步操作:
if "%missing_files%"=="" (
    dotnet --version >nul 2>&1
    if !errorLevel! equ 0 (
        echo 1. 运行 'dotnet restore' 恢复NuGet包
        echo 2. 运行 'dotnet build' 编译项目
        echo 3. 运行 'build.bat' 构建发布版本
    ) else (
        echo 1. 安装.NET 6.0 SDK
        echo 2. 运行 'dotnet restore' 恢复NuGet包
        echo 3. 运行 'dotnet build' 编译项目
    )
) else (
    echo 1. 检查并补充缺失的文件
    echo 2. 安装.NET 6.0 SDK（如果未安装）
    echo 3. 重新运行此检查脚本
)

echo.
pause