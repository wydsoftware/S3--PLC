@echo off
chcp 65001 >nul
echo ========================================
echo    创建 S3 PLC数据采集系统 安装包
echo ========================================
echo.

:: 清理旧的安装包目录
if exist "installer" (
    echo [*] 清理旧的安装包目录...
    rmdir /s /q "installer" >nul 2>&1
)

:: 创建安装包目录结构
echo [*] 创建安装包目录结构...
mkdir "installer" >nul 2>&1
mkdir "installer\S3PLCDataCollector" >nul 2>&1
mkdir "installer\db_init" >nul 2>&1
mkdir "installer\db_check" >nul 2>&1

:: 发布主程序（自包含）
echo [*] 发布主程序（自包含部署）...
dotnet publish S3PLCDataCollector.csproj -c Release -r win-x64 --self-contained true -o ./installer/S3PLCDataCollector >nul 2>&1
if %errorLevel% == 0 (
    echo [√] 主程序发布成功
) else (
    echo [×] 主程序发布失败
    pause
    exit /b 1
)

:: 发布数据库初始化工具
echo [*] 发布数据库初始化工具...
dotnet publish db_init/init_db.csproj -c Release -r win-x64 --self-contained true -o ./installer/db_init >nul 2>&1
if %errorLevel% == 0 (
    echo [√] 数据库初始化工具发布成功
) else (
    echo [×] 数据库初始化工具发布失败
    pause
    exit /b 1
)

:: 发布数据库检查工具
echo [*] 发布数据库检查工具...
dotnet publish db_check/check_db.csproj -c Release -r win-x64 --self-contained true -o ./installer/db_check >nul 2>&1
if %errorLevel% == 0 (
    echo [√] 数据库检查工具发布成功
) else (
    echo [×] 数据库检查工具发布失败
    pause
    exit /b 1
)

:: 复制安装脚本
echo [*] 复制安装脚本...
copy "install.bat" "installer\install.bat" >nul 2>&1
copy "uninstall.bat" "installer\uninstall.bat" >nul 2>&1
copy "安装说明.txt" "installer\安装说明.txt" >nul 2>&1

:: 复制项目文档
echo [*] 复制项目文档...
copy "README.md" "installer\README.md" >nul 2>&1

:: 创建版本信息文件
echo [*] 创建版本信息文件...
echo S3 PLC数据采集系统 v1.0.0 > "installer\version.txt"
echo 构建时间: %date% %time% >> "installer\version.txt"
echo 目标平台: Windows x64 >> "installer\version.txt"
echo .NET版本: 9.0 >> "installer\version.txt"
echo 部署类型: 自包含部署 >> "installer\version.txt"

echo.
echo ========================================
echo         安装包创建完成！
echo ========================================
echo.
echo 安装包位置: %cd%\installer\
echo.
echo 安装包内容:
echo   - install.bat        (安装程序)
echo   - uninstall.bat      (卸载程序)
echo   - 安装说明.txt        (安装说明)
echo   - S3PLCDataCollector\ (主程序目录)
echo   - db_init\           (数据库初始化工具)
echo   - db_check\          (数据库检查工具)
echo   - README.md          (项目说明)
echo   - version.txt        (版本信息)
echo.
echo 使用方法:
echo 1. 将整个 installer 目录复制到目标计算机
echo 2. 以管理员身份运行 install.bat
echo 3. 按照提示完成安装
echo.
echo 按任意键退出...
pause >nul