@echo off
chcp 65001 >nul
echo ===========================================
echo           PLC CNC 模拟测试程序
echo ===========================================
echo.
echo 功能说明：
echo - 每1秒随机选择CNC01-16中的一个设备
echo - 将选中设备的点位值加1
echo - 实际写入到PLC中进行测试
echo.
echo 按任意键开始测试，按Ctrl+C停止测试
pause >nul
echo.
echo 正在启动测试程序...
echo.
dotnet run
echo.
echo 测试程序已结束
pause