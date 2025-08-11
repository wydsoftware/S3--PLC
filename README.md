# S3 PLC数据采集系统

## 项目简介

这是一个专为汇川S3系列PLC设计的数据采集与监控系统，支持实时数据采集、数据存储和系统托盘运行。

## 主要功能

- **实时数据采集**: 支持汇川S3系列PLC的Modbus TCP通信
- **数据存储**: 使用SQLite数据库存储采集数据和配置信息
- **系统服务**: 可作为Windows服务运行，支持开机自启
- **系统托盘**: 提供系统托盘界面，方便监控和管理
- **WPF界面**: 现代化的用户界面，支持实时数据显示

## 系统架构

### 核心组件

- **S3PLCDataCollector**: 主程序，包含WPF界面和数据采集逻辑
- **DatabaseService**: 数据库服务，负责数据存储和查询
- **PLCService**: PLC通信服务，处理Modbus TCP通信
- **DataCollectionService**: 数据采集服务，协调各组件工作
- **WindowsServiceHost**: Windows服务宿主

### 辅助工具

- **db_init**: 数据库初始化工具
- **db_check**: 数据库检查工具
- **plc_test**: PLC连接测试工具
- **test_console**: 控制台测试程序

## 数据库结构

### device_config 表
存储设备配置信息，包括48个预定义的数据点位配置。

### device_data 表
存储实时采集的设备数据。

### read_log 表
存储数据读取日志。

### system_config 表
存储系统配置参数。

## 快速开始

### 环境要求

- .NET 6.0 或更高版本
- Windows 10/11 或 Windows Server 2016+
- 汇川S3系列PLC（支持Modbus TCP）

### 安装步骤

1. **编译项目**
   ```bash
   dotnet build S3PLCDataCollector.csproj
   ```

2. **初始化数据库**
   ```bash
   dotnet run --project db_init
   ```

3. **配置连接**
   编辑 `appsettings.json` 文件，设置PLC IP地址和端口

4. **运行程序**
   ```bash
   dotnet run
   ```

### 服务安装

以管理员身份运行 `install_service.bat` 安装服务，运行 `uninstall_service.bat` 卸载服务。

## 技术栈

- **.NET 6.0**: 主要开发框架
- **WPF**: 用户界面框架
- **SQLite**: 数据存储
- **Modbus TCP**: PLC通信协议
- **Windows Services**: 系统服务

## 许可证

本项目采用 MIT 许可证。

## 联系方式

如有问题或建议，请联系：1025646629@qq.com

## 功能特性

### 核心功能
- **自动数据采集**：定时从汇川S3 PLC读取48个点位数据
- **实时数据存储**：将采集数据保存到SQLite数据库，只保留最新数据
- **完整日志记录**：记录当天所有读取操作的详细日志
- **双模式运行**：支持GUI界面模式和Windows服务模式
- **配置管理**：可配置PLC IP地址、读取间隔等参数

### 界面功能
- **实时数据展示**：表格形式显示所有点位的当前值和状态
- **控制面板**：提供启动/暂停、配置修改、手动刷新等操作
- **系统日志**：实时显示程序运行日志和异常信息
- **系统托盘**：支持最小化到系统托盘运行
- **连接状态**：实时显示PLC连接状态和数据采集状态

### 服务功能
- **Windows服务**：可安装为Windows服务，开机自启动
- **服务管理**：提供安装、卸载、启动、停止服务的脚本
- **后台运行**：服务模式下无界面后台运行
- **自动恢复**：服务异常时自动重启

## 技术架构

### 开发环境
- **框架**：.NET 6.0
- **界面**：WPF (Windows Presentation Foundation)
- **数据库**：SQLite
- **通信协议**：Modbus TCP
- **依赖注入**：Microsoft.Extensions.Hosting
- **日志记录**：Microsoft.Extensions.Logging

### 主要组件
- **PLCService**：负责与PLC的Modbus TCP通信
- **DatabaseService**：负责数据库操作和数据持久化
- **DataCollectionService**：后台数据采集服务
- **WindowsServiceHost**：Windows服务宿主
- **MainWindow**：WPF主界面

## 预定义点位

系统预配置了48个数据点位，涵盖多种设备类型：

| 设备类型 | 数量 | 地址范围 | 说明 |
|---------|------|----------|------|
| AOI | 2 | D802-D804 | 模拟输出接口 |
| CCM08 | 24 | D806-D852 | 控制模块08系列 |
| CCM23 | 6 | D854-D864 | 控制模块23系列 |
| CNC | 16 | D866-D896 | 数控系统 |

所有点位数据类型均为16位整数(Int16)，通过Modbus TCP协议的D寄存器读取。

## 数据库设计

### 表结构

#### device_data（设备数据表）
- `id`：主键，自增
- `device_name`：设备名称
- `device_address`：设备地址
- `current_value`：当前值
- `last_update_time`：最后更新时间
- `is_online`：在线状态

#### read_log（读取日志表）
- `id`：主键，自增
- `device_name`：设备名称
- `device_address`：设备地址
- `read_value`：读取值
- `read_time`：读取时间
- `is_success`：读取是否成功
- `error_message`：错误信息

#### device_config（设备配置表）
- `id`：主键，自增
- `device_name`：设备名称
- `device_address`：设备地址
- `data_type`：数据类型
- `description`：设备描述
- `is_enabled`：是否启用

#### system_config（系统配置表）
- `id`：主键，自增
- `config_key`：配置键
- `config_value`：配置值
- `description`：配置描述
- `last_update_time`：最后更新时间

## 安装部署

### 系统要求
- **操作系统**：Windows 10/11 或 Windows Server 2016+
- **网络**：能够访问PLC的TCP/IP网络
- **权限**：安装Windows服务需要管理员权限

### 构建程序

1. **安装.NET 6.0 SDK**
   ```bash
   # 下载并安装.NET 6.0 SDK
   # https://dotnet.microsoft.com/download/dotnet/6.0
   ```

2. **构建项目**
   ```bash
   # 运行构建脚本
   build.bat
   ```

3. **发布文件**
   构建完成后，所有文件将位于 `publish` 目录中。

### 部署步骤

1. **复制文件**
   将 `publish` 目录复制到目标计算机的合适位置（如 `C:\Program Files\S3PLCDataCollector`）

2. **配置参数**
   编辑 `appsettings.json` 文件，修改PLC IP地址等配置：
   ```json
   {
     "PLC": {
       "IPAddress": "192.168.1.2",
       "Port": 502,
       "ReadInterval": 5000
     }
   }
   ```

3. **测试运行**
   双击 `start.bat` 或直接运行 `S3PLCDataCollector.exe` 测试程序功能

4. **安装服务**（可选）
   以管理员身份运行 `install_service.bat` 安装Windows服务

## 使用说明

### GUI模式

1. **启动程序**
   - 双击 `S3PLCDataCollector.exe` 或 `start.bat`
   - 程序将显示主界面

2. **配置连接**
   - 在控制面板中设置PLC IP地址和端口
   - 设置数据读取间隔（秒）
   - 点击"应用配置"保存设置

3. **开始采集**
   - 点击"启动采集"按钮开始数据采集
   - 观察连接状态和设备数据表格
   - 查看系统日志了解运行状态

4. **监控数据**
   - 设备数据表格实时显示所有点位信息
   - 包括设备名称、地址、当前值、更新时间、连接状态
   - 可手动点击"刷新数据"更新显示

### 服务模式

1. **安装服务**
   ```bash
   # 以管理员身份运行
   install_service.bat
   ```

2. **管理服务**
   ```bash
   # 启动服务
   sc start S3PLCDataCollectorService
   
   # 停止服务
   sc stop S3PLCDataCollectorService
   
   # 查看状态
   sc query S3PLCDataCollectorService
   ```

3. **卸载服务**
   ```bash
   # 以管理员身份运行
   uninstall_service.bat
   ```

## 配置说明

### appsettings.json 配置文件

```json
{
  "PLC": {
    "IPAddress": "192.168.1.2",    // PLC IP地址
    "Port": 502,                    // Modbus TCP端口
    "ReadInterval": 5000,           // 读取间隔(毫秒)
    "Timeout": 3000,                // 连接超时(毫秒)
    "RetryCount": 3                 // 重试次数
  },
  "Database": {
    "ConnectionString": "Data Source=plc_data.db",  // 数据库连接字符串
    "LogRetentionDays": 30,         // 日志保留天数
    "EnableBackup": true,           // 启用备份
    "BackupPath": "Backup"          // 备份路径
  },
  "WindowsService": {
    "ServiceName": "S3PLCDataCollectorService",
    "DisplayName": "汇川S3 PLC数据采集服务",
    "Description": "自动采集汇川S3 PLC数据并保存到数据库",
    "AutoStart": true               // 开机自启动
  },
  "UI": {
    "RefreshInterval": 1000,        // 界面刷新间隔(毫秒)
    "MaxDisplayRows": 1000,         // 最大显示行数
    "AutoScrollLog": true,          // 自动滚动日志
    "ShowInTray": true              // 显示系统托盘图标
  },
  "Logging": {
    "LogLevel": {
      "Default": "Information"
    },
    "FilePath": "Logs/app.log",     // 日志文件路径
    "MaxFileSize": 10485760         // 最大文件大小(字节)
  }
}
```

## 故障排除

### 常见问题

1. **无法连接PLC**
   - 检查网络连接和PLC IP地址
   - 确认PLC的Modbus TCP功能已启用
   - 检查防火墙设置
   - 验证端口502是否被占用

2. **数据读取失败**
   - 检查PLC中D寄存器地址是否正确
   - 确认PLC程序正在运行
   - 检查Modbus地址映射

3. **服务安装失败**
   - 确保以管理员身份运行安装脚本
   - 检查可执行文件路径是否正确
   - 查看Windows事件日志获取详细错误信息

4. **数据库错误**
   - 检查数据库文件权限
   - 确保磁盘空间充足
   - 查看程序日志了解具体错误

### 日志文件位置
- **程序日志**：`Logs/app.log`
- **Windows事件日志**：Windows日志 → 应用程序
- **服务日志**：通过Windows服务管理器查看

### 调试模式
在 `appsettings.json` 中设置日志级别为 `Debug` 可获取更详细的调试信息：
```json
{
  "Logging": {
    "LogLevel": {
      "Default": "Debug"
    }
  }
}
```

## 维护说明

### 定期维护
- **数据库清理**：程序会自动清理超过保留期的日志数据
- **日志文件**：定期检查和清理日志文件
- **备份数据**：定期备份数据库文件
- **更新配置**：根据需要调整采集间隔和其他参数

### 性能优化
- **读取间隔**：根据实际需求调整数据读取频率
- **批量读取**：程序自动优化相邻地址的批量读取
- **内存管理**：程序自动管理内存使用，避免内存泄漏

## 技术支持

如遇到技术问题，请提供以下信息：
1. 程序版本和运行环境
2. 详细的错误描述和重现步骤
3. 相关的日志文件内容
4. PLC型号和网络配置信息

## 版本历史

### v1.0.0
- 初始版本发布
- 支持汇川S3 PLC数据采集
- 实现GUI界面和Windows服务模式
- 提供完整的配置和管理功能

---

**注意**：本程序专为汇川S3 PLC设计，使用前请确保PLC支持Modbus TCP协议并正确配置网络参数。