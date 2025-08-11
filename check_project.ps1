# 汇川S3 PLC数据采集程序 项目检查脚本
# PowerShell版本，避免编码问题

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "汇川S3 PLC数据采集程序 项目检查" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查.NET SDK
Write-Host "检查.NET SDK安装状态..." -ForegroundColor Yellow
try {
    $dotnetVersion = dotnet --version 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[成功] .NET SDK已安装" -ForegroundColor Green
        Write-Host "版本: $dotnetVersion" -ForegroundColor Green
    } else {
        throw "dotnet命令不可用"
    }
} catch {
    Write-Host "[警告] 未检测到.NET SDK！" -ForegroundColor Red
    Write-Host ""
    Write-Host "请按照以下步骤安装.NET 6.0 SDK:" -ForegroundColor Yellow
    Write-Host "1. 访问: https://dotnet.microsoft.com/download/dotnet/6.0" -ForegroundColor White
    Write-Host "2. 下载并安装 .NET 6.0 SDK (x64)" -ForegroundColor White
    Write-Host "3. 重启PowerShell" -ForegroundColor White
    Write-Host "4. 运行 'dotnet --version' 验证安装" -ForegroundColor White
}

Write-Host ""

# 检查项目文件
Write-Host "检查项目文件结构..." -ForegroundColor Yellow
Write-Host ""

$coreFiles = @(
    "S3PLCDataCollector.csproj",
    "appsettings.json",
    "database_schema.sql",
    "Program.cs",
    "App.xaml",
    "App.xaml.cs",
    "MainWindow.xaml",
    "MainWindow.xaml.cs"
)

$missingFiles = @()
$foundFiles = @()

foreach ($file in $coreFiles) {
    if (Test-Path $file) {
        Write-Host "[✓] $file" -ForegroundColor Green
        $foundFiles += $file
    } else {
        Write-Host "[✗] $file (缺失)" -ForegroundColor Red
        $missingFiles += $file
    }
}

Write-Host ""

# 检查源代码目录
Write-Host "检查源代码目录..." -ForegroundColor Yellow

if (Test-Path "Models") {
    $modelFiles = Get-ChildItem "Models\*.cs" -ErrorAction SilentlyContinue
    Write-Host "[✓] Models 目录" -ForegroundColor Green
    Write-Host "    - 包含 $($modelFiles.Count) 个 C# 文件" -ForegroundColor Gray
} else {
    Write-Host "[✗] Models 目录 (缺失)" -ForegroundColor Red
}

if (Test-Path "Services") {
    $serviceFiles = Get-ChildItem "Services\*.cs" -ErrorAction SilentlyContinue
    Write-Host "[✓] Services 目录" -ForegroundColor Green
    Write-Host "    - 包含 $($serviceFiles.Count) 个 C# 文件" -ForegroundColor Gray
} else {
    Write-Host "[✗] Services 目录 (缺失)" -ForegroundColor Red
}

Write-Host ""

# 检查脚本文件
Write-Host "检查管理脚本..." -ForegroundColor Yellow
$scriptFiles = @("build.bat", "install_service.bat", "uninstall_service.bat")
foreach ($script in $scriptFiles) {
    if (Test-Path $script) {
        Write-Host "[✓] $script" -ForegroundColor Green
    } else {
        Write-Host "[✗] $script (缺失)" -ForegroundColor Red
    }
}

Write-Host ""

# 统计结果
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "检查结果汇总" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if ($missingFiles.Count -eq 0) {
    Write-Host "[成功] 所有核心文件都存在" -ForegroundColor Green
} else {
    Write-Host "[警告] 以下文件缺失: $($missingFiles -join ', ')" -ForegroundColor Red
}

Write-Host ""

# 统计项目文件
$allFiles = Get-ChildItem -Filter "*.cs" -Recurse
$allFiles += Get-ChildItem -Filter "*.xaml"
$allFiles += Get-ChildItem -Filter "*.json"
$allFiles += Get-ChildItem -Filter "*.sql"
$allFiles += Get-ChildItem -Filter "*.bat"

Write-Host "项目文件总数: $($allFiles.Count)" -ForegroundColor Cyan
Write-Host "  - C# 文件: $((Get-ChildItem -Filter '*.cs' -Recurse).Count)" -ForegroundColor Gray
Write-Host "  - XAML 文件: $((Get-ChildItem -Filter '*.xaml').Count)" -ForegroundColor Gray
Write-Host "  - 配置文件: $((Get-ChildItem -Filter '*.json').Count + (Get-ChildItem -Filter '*.sql').Count)" -ForegroundColor Gray
Write-Host "  - 脚本文件: $((Get-ChildItem -Filter '*.bat').Count)" -ForegroundColor Gray

Write-Host ""
Write-Host "下一步操作:" -ForegroundColor Yellow

if ($missingFiles.Count -eq 0) {
    try {
        $dotnetVersion = dotnet --version 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "1. 运行 'dotnet restore' 恢复NuGet包" -ForegroundColor White
            Write-Host "2. 运行 'dotnet build' 编译项目" -ForegroundColor White
            Write-Host "3. 运行 '.\build.bat' 构建发布版本" -ForegroundColor White
        } else {
            throw "dotnet不可用"
        }
    } catch {
        Write-Host "1. 安装.NET 6.0 SDK" -ForegroundColor White
        Write-Host "2. 运行 'dotnet restore' 恢复NuGet包" -ForegroundColor White
        Write-Host "3. 运行 'dotnet build' 编译项目" -ForegroundColor White
    }
} else {
    Write-Host "1. 检查并补充缺失的文件" -ForegroundColor White
    Write-Host "2. 安装.NET 6.0 SDK（如果未安装）" -ForegroundColor White
    Write-Host "3. 重新运行此检查脚本" -ForegroundColor White
}

Write-Host ""
Write-Host "按任意键继续..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")