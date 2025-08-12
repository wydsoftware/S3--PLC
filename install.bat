@echo off
chcp 65001 >nul
echo ========================================
echo    S3 PLC数据采集系统 安装程序
echo ========================================
echo.

:: 检查管理员权限
net session >nul 2>&1
if %errorLevel% == 0 (
    echo [√] 管理员权限检查通过
) else (
    echo [×] 需要管理员权限运行此安装程序
    echo 请右键点击此文件，选择"以管理员身份运行"
    pause
    exit /b 1
)

:: 设置安装目录
set INSTALL_DIR=C:\Program Files\S3PLCDataCollector
echo [*] 安装目录: %INSTALL_DIR%

:: 创建安装目录
if not exist "%INSTALL_DIR%" (
    mkdir "%INSTALL_DIR%"
    echo [√] 创建安装目录成功
) else (
    echo [*] 安装目录已存在
)

:: 停止现有服务（如果存在）
echo [*] 检查现有服务...
sc query "S3PLCDataCollector" >nul 2>&1
if %errorLevel% == 0 (
    echo [*] 停止现有服务...
    sc stop "S3PLCDataCollector" >nul 2>&1
    timeout /t 3 >nul
    echo [*] 删除现有服务...
    sc delete "S3PLCDataCollector" >nul 2>&1
    echo [√] 现有服务已清理
)

:: 复制主程序文件
echo [*] 复制主程序文件...
xcopy /E /I /Y "S3PLCDataCollector\*" "%INSTALL_DIR%" >nul
if %errorLevel% == 0 (
    echo [√] 主程序文件复制成功
) else (
    echo [×] 主程序文件复制失败
    pause
    exit /b 1
)

:: 复制辅助工具
echo [*] 复制辅助工具...
if not exist "%INSTALL_DIR%\tools" mkdir "%INSTALL_DIR%\tools"
xcopy /E /I /Y "db_init\*" "%INSTALL_DIR%\tools\db_init\" >nul
xcopy /E /I /Y "db_check\*" "%INSTALL_DIR%\tools\db_check\" >nul
echo [√] 辅助工具复制成功

:: 初始化数据库
echo [*] 初始化数据库...
cd /d "%INSTALL_DIR%"
"%INSTALL_DIR%\tools\db_init\init_db.exe" >nul 2>&1
if %errorLevel% == 0 (
    echo [√] 数据库初始化成功
) else (
    echo [!] 数据库初始化可能失败，请稍后手动运行
)

:: 创建服务安装脚本
echo [*] 创建服务管理脚本...
echo @echo off > "%INSTALL_DIR%\install_service.bat"
echo sc create "S3PLCDataCollector" binPath= ""%INSTALL_DIR%\S3PLCDataCollector.exe" --service" start= auto >> "%INSTALL_DIR%\install_service.bat"
echo sc description "S3PLCDataCollector" "S3 PLC数据采集系统服务" >> "%INSTALL_DIR%\install_service.bat"
echo sc start "S3PLCDataCollector" >> "%INSTALL_DIR%\install_service.bat"
echo echo 服务安装完成 >> "%INSTALL_DIR%\install_service.bat"
echo pause >> "%INSTALL_DIR%\install_service.bat"

echo @echo off > "%INSTALL_DIR%\uninstall_service.bat"
echo sc stop "S3PLCDataCollector" >> "%INSTALL_DIR%\uninstall_service.bat"
echo sc delete "S3PLCDataCollector" >> "%INSTALL_DIR%\uninstall_service.bat"
echo echo 服务卸载完成 >> "%INSTALL_DIR%\uninstall_service.bat"
echo pause >> "%INSTALL_DIR%\uninstall_service.bat"

:: 创建启动脚本
echo @echo off > "%INSTALL_DIR%\启动程序.bat"
echo cd /d "%INSTALL_DIR%" >> "%INSTALL_DIR%\启动程序.bat"
echo start "" "S3PLCDataCollector.exe" >> "%INSTALL_DIR%\启动程序.bat"

:: 创建桌面快捷方式
echo [*] 创建桌面快捷方式...
powershell -Command "$WshShell = New-Object -comObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%USERPROFILE%\Desktop\S3 PLC数据采集系统.lnk'); $Shortcut.TargetPath = '%INSTALL_DIR%\S3PLCDataCollector.exe'; $Shortcut.WorkingDirectory = '%INSTALL_DIR%'; $Shortcut.Description = 'S3 PLC数据采集系统'; $Shortcut.Save()" >nul 2>&1
if %errorLevel% == 0 (
    echo [√] 桌面快捷方式创建成功
) else (
    echo [!] 桌面快捷方式创建失败
)

:: 安装并启动服务
echo [*] 安装Windows服务...
sc create "S3PLCDataCollector" binPath= "\"%INSTALL_DIR%\S3PLCDataCollector.exe\" --service" start= auto >nul 2>&1
if %errorLevel% == 0 (
    sc description "S3PLCDataCollector" "S3 PLC数据采集系统服务" >nul 2>&1
    echo [√] Windows服务安装成功
    
    echo [*] 启动服务...
    sc start "S3PLCDataCollector" >nul 2>&1
    if %errorLevel% == 0 (
        echo [√] 服务启动成功
    ) else (
        echo [!] 服务启动失败，请稍后手动启动
    )
) else (
    echo [!] Windows服务安装失败
)

echo.
echo ========================================
echo           安装完成！
echo ========================================
echo.
echo 安装位置: %INSTALL_DIR%
echo 桌面快捷方式: S3 PLC数据采集系统
echo.
echo 服务管理:
echo   - 安装服务: install_service.bat
echo   - 卸载服务: uninstall_service.bat
echo   - 直接启动: 启动程序.bat
echo.
echo 配置文件: %INSTALL_DIR%\appsettings.json
echo 数据库文件: %INSTALL_DIR%\plc_data.db
echo.
echo 按任意键退出...
pause >nul