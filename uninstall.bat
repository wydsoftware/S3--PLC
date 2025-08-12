@echo off
chcp 65001 >nul
echo ========================================
echo    S3 PLC数据采集系统 卸载程序
echo ========================================
echo.

:: 检查管理员权限
net session >nul 2>&1
if %errorLevel% == 0 (
    echo [√] 管理员权限检查通过
) else (
    echo [×] 需要管理员权限运行此卸载程序
    echo 请右键点击此文件，选择"以管理员身份运行"
    pause
    exit /b 1
)

set INSTALL_DIR=C:\Program Files\S3PLCDataCollector

echo [*] 准备卸载 S3 PLC数据采集系统...
echo [*] 安装目录: %INSTALL_DIR%
echo.

:: 确认卸载
set /p confirm=确定要卸载吗？这将删除所有程序文件（数据库文件将保留）[Y/N]: 
if /i not "%confirm%"=="Y" (
    echo 取消卸载
    pause
    exit /b 0
)

:: 停止并删除服务
echo [*] 停止并删除Windows服务...
sc query "S3PLCDataCollector" >nul 2>&1
if %errorLevel% == 0 (
    sc stop "S3PLCDataCollector" >nul 2>&1
    timeout /t 3 >nul
    sc delete "S3PLCDataCollector" >nul 2>&1
    echo [√] Windows服务已删除
) else (
    echo [*] 未找到Windows服务
)

:: 结束进程
echo [*] 结束相关进程...
taskkill /f /im "S3PLCDataCollector.exe" >nul 2>&1
echo [√] 进程已结束

:: 删除桌面快捷方式
echo [*] 删除桌面快捷方式...
if exist "%USERPROFILE%\Desktop\S3 PLC数据采集系统.lnk" (
    del "%USERPROFILE%\Desktop\S3 PLC数据采集系统.lnk" >nul 2>&1
    echo [√] 桌面快捷方式已删除
) else (
    echo [*] 未找到桌面快捷方式
)

:: 备份数据库文件
echo [*] 备份数据库文件...
if exist "%INSTALL_DIR%\plc_data.db" (
    if not exist "%USERPROFILE%\Documents\S3PLCBackup" mkdir "%USERPROFILE%\Documents\S3PLCBackup"
    copy "%INSTALL_DIR%\plc_data.db" "%USERPROFILE%\Documents\S3PLCBackup\plc_data_backup_%date:~0,4%%date:~5,2%%date:~8,2%.db" >nul 2>&1
    echo [√] 数据库已备份到: %USERPROFILE%\Documents\S3PLCBackup\
)

:: 删除程序文件
echo [*] 删除程序文件...
if exist "%INSTALL_DIR%" (
    rmdir /s /q "%INSTALL_DIR%" >nul 2>&1
    if exist "%INSTALL_DIR%" (
        echo [!] 部分文件可能正在使用中，无法完全删除
        echo [*] 请重启计算机后手动删除: %INSTALL_DIR%
    ) else (
        echo [√] 程序文件删除成功
    )
) else (
    echo [*] 程序目录不存在
)

echo.
echo ========================================
echo           卸载完成！
echo ========================================
echo.
echo 数据库备份位置: %USERPROFILE%\Documents\S3PLCBackup\
echo.
echo 如需重新安装，请运行 install.bat
echo.
echo 按任意键退出...
pause >nul