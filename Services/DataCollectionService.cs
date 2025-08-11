using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using S3PLCDataCollector.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace S3PLCDataCollector.Services
{
    /// <summary>
    /// 数据采集服务类
    /// </summary>
    public class DataCollectionService : BackgroundService
    {
        private readonly PLCService _plcService;
        private readonly DatabaseService _databaseService;
        private readonly ILogger<DataCollectionService> _logger;
        
        private int _readIntervalSeconds = 5;
        private bool _isRunning = false;
        private List<DeviceConfig> _deviceConfigs = new List<DeviceConfig>();
        private readonly object _lockObject = new object();
        
        // 事件：数据采集完成
        public event EventHandler<DataCollectionEventArgs>? DataCollected;
        
        // 事件：连接状态变化
        public event EventHandler<ConnectionStatusEventArgs>? ConnectionStatusChanged;
        
        public DataCollectionService(
            PLCService plcService,
            DatabaseService databaseService,
            ILogger<DataCollectionService> logger)
        {
            _plcService = plcService;
            _databaseService = databaseService;
            _logger = logger;
        }
        
        /// <summary>
        /// 是否正在运行
        /// </summary>
        public bool IsRunning 
        { 
            get 
            { 
                lock (_lockObject)
                {
                    return _isRunning;
                }
            } 
        }
        
        /// <summary>
        /// 读取间隔（秒）
        /// </summary>
        public int ReadIntervalSeconds
        {
            get { return _readIntervalSeconds; }
            set
            {
                if (value > 0)
                {
                    _readIntervalSeconds = value;
                    _logger.LogInformation("数据读取间隔已更新为 {Interval} 秒", value);
                }
            }
        }
        
        /// <summary>
        /// 初始化服务
        /// </summary>
        public async Task InitializeAsync()
        {
            try
            {
                // 加载设备配置
                await LoadDeviceConfigsAsync();
                
                // 从数据库加载系统配置
                await LoadSystemConfigsAsync();
                
                _logger.LogInformation("数据采集服务初始化完成，共加载 {Count} 个设备配置", _deviceConfigs.Count);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "数据采集服务初始化失败");
                throw;
            }
        }
        
        /// <summary>
        /// 加载设备配置
        /// </summary>
        private async Task LoadDeviceConfigsAsync()
        {
            try
            {
                _deviceConfigs = await _databaseService.GetEnabledDeviceConfigsAsync();
                _logger.LogInformation("已加载 {Count} 个启用的设备配置", _deviceConfigs.Count);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "加载设备配置失败");
                throw;
            }
        }
        
        /// <summary>
        /// 加载系统配置
        /// </summary>
        private async Task LoadSystemConfigsAsync()
        {
            try
            {
                // 加载PLC连接配置
                var plcIp = await _databaseService.GetSystemConfigAsync("plc_ip_address") ?? "192.168.1.2";
                var plcPortStr = await _databaseService.GetSystemConfigAsync("plc_port") ?? "502";
                var readIntervalStr = await _databaseService.GetSystemConfigAsync("read_interval_seconds") ?? "5";
                
                if (int.TryParse(plcPortStr, out int plcPort))
                {
                    _plcService.Configure(plcIp, plcPort);
                }
                
                if (int.TryParse(readIntervalStr, out int readInterval))
                {
                    ReadIntervalSeconds = readInterval;
                }
                
                _logger.LogInformation("系统配置加载完成: PLC={PlcIp}:{PlcPort}, 读取间隔={ReadInterval}秒", 
                    plcIp, plcPort, readInterval);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "加载系统配置失败");
            }
        }
        
        /// <summary>
        /// 更新PLC连接配置
        /// </summary>
        public async Task UpdatePLCConfigAsync(string ipAddress, int port = 502)
        {
            try
            {
                _plcService.Configure(ipAddress, port);
                
                // 保存到数据库
                await _databaseService.UpdateSystemConfigAsync("plc_ip_address", ipAddress);
                await _databaseService.UpdateSystemConfigAsync("plc_port", port.ToString());
                
                _logger.LogInformation("PLC配置已更新: {IpAddress}:{Port}", ipAddress, port);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "更新PLC配置失败");
                throw;
            }
        }
        
        /// <summary>
        /// 更新读取间隔
        /// </summary>
        public async Task UpdateReadIntervalAsync(int intervalSeconds)
        {
            try
            {
                ReadIntervalSeconds = intervalSeconds;
                
                // 保存到数据库
                await _databaseService.UpdateSystemConfigAsync("read_interval_seconds", intervalSeconds.ToString());
                
                _logger.LogInformation("读取间隔已更新为 {Interval} 秒", intervalSeconds);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "更新读取间隔失败");
                throw;
            }
        }
        
        /// <summary>
        /// 手动启动数据采集
        /// </summary>
        public async Task StartCollectionAsync()
        {
            lock (_lockObject)
            {
                _isRunning = true;
            }
            
            _logger.LogInformation("数据采集已手动启动");
            
            // 立即执行一次数据采集
            _ = Task.Run(async () => await CollectDataOnceAsync());
        }
        
        /// <summary>
        /// 手动停止数据采集
        /// </summary>
        public void StopCollection()
        {
            lock (_lockObject)
            {
                _isRunning = false;
            }
            
            _logger.LogInformation("数据采集已手动停止");
        }
        
        /// <summary>
        /// 后台服务执行方法
        /// </summary>
        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            _logger.LogInformation("数据采集后台服务已启动");
            
            // 初始化服务
            await InitializeAsync();
            
            // 自动启动采集
            lock (_lockObject)
            {
                _isRunning = true;
            }
            
            while (!stoppingToken.IsCancellationRequested)
            {
                try
                {
                    if (IsRunning)
                    {
                        await CollectDataOnceAsync();
                    }
                    
                    // 等待指定间隔
                    await Task.Delay(TimeSpan.FromSeconds(_readIntervalSeconds), stoppingToken);
                }
                catch (OperationCanceledException)
                {
                    // 正常取消，退出循环
                    break;
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "数据采集循环中发生异常");
                    
                    // 发生异常时等待一段时间再继续
                    await Task.Delay(TimeSpan.FromSeconds(10), stoppingToken);
                }
            }
            
            _logger.LogInformation("数据采集后台服务已停止");
        }
        
        /// <summary>
        /// 执行一次数据采集
        /// </summary>
        private async Task CollectDataOnceAsync()
        {
            var startTime = DateTime.Now;
            var collectedData = new Dictionary<string, double>();
            var errors = new List<string>();
            bool connectionSuccess = false;
            
            try
            {
                // 检查PLC连接
                if (!_plcService.IsConnected)
                {
                    _logger.LogDebug("PLC未连接，尝试重新连接...");
                    connectionSuccess = await _plcService.ConnectAsync();
                    
                    if (!connectionSuccess)
                    {
                        var errorMsg = "无法连接到PLC";
                        errors.Add(errorMsg);
                        _logger.LogWarning(errorMsg);
                        
                        // 触发连接状态变化事件
                        ConnectionStatusChanged?.Invoke(this, new ConnectionStatusEventArgs
                        {
                            IsConnected = false,
                            StatusMessage = _plcService.GetConnectionStatusInfo(),
                            Timestamp = DateTime.Now
                        });
                        
                        return;
                    }
                }
                else
                {
                    connectionSuccess = true;
                }
                
                // 触发连接状态变化事件
                ConnectionStatusChanged?.Invoke(this, new ConnectionStatusEventArgs
                {
                    IsConnected = true,
                    StatusMessage = _plcService.GetConnectionStatusInfo(),
                    Timestamp = DateTime.Now
                });
                
                // 读取所有设备数据
                var deviceDataList = await _plcService.ReadAllDeviceDataAsync(_deviceConfigs);
                
                // 转换为Dictionary格式
                foreach (var deviceData in deviceDataList)
                {
                    collectedData[deviceData.DeviceName] = deviceData.CurrentValue;
                }
                
                if (collectedData.Count == 0)
                {
                    var errorMsg = "未读取到任何设备数据";
                    errors.Add(errorMsg);
                    _logger.LogWarning(errorMsg);
                }
                else
                {
                    // 保存数据到数据库
                    await SaveCollectedDataAsync(collectedData);
                    
                    _logger.LogDebug("成功采集并保存 {Count} 个设备的数据", collectedData.Count);
                }
            }
            catch (Exception ex)
            {
                var errorMsg = $"数据采集过程中发生异常: {ex.Message}";
                errors.Add(errorMsg);
                _logger.LogError(ex, "数据采集过程中发生异常");
            }
            
            // 触发数据采集完成事件
            var eventArgs = new DataCollectionEventArgs
            {
                CollectedData = collectedData,
                Errors = errors,
                StartTime = startTime,
                EndTime = DateTime.Now,
                IsSuccess = errors.Count == 0 && collectedData.Count > 0,
                ConnectionStatus = connectionSuccess
            };
            
            DataCollected?.Invoke(this, eventArgs);
        }
        
        /// <summary>
        /// 保存采集的数据到数据库
        /// </summary>
        private async Task SaveCollectedDataAsync(Dictionary<string, double> collectedData)
        {
            var tasks = new List<Task>();
            
            foreach (var kvp in collectedData)
            {
                var deviceName = kvp.Key;
                var value = kvp.Value;
                
                // 查找设备配置
                var deviceConfig = _deviceConfigs.FirstOrDefault(d => d.DeviceName == deviceName);
                if (deviceConfig == null)
                {
                    _logger.LogWarning("未找到设备 {DeviceName} 的配置信息", deviceName);
                    continue;
                }
                
                // 更新设备数据
                tasks.Add(_databaseService.UpsertDeviceDataAsync(deviceName, deviceConfig.DeviceAddress, value));
                
                // 插入读取日志
                tasks.Add(_databaseService.InsertReadLogAsync(deviceName, deviceConfig.DeviceAddress, value));
            }
            
            // 并行执行所有数据库操作
            await Task.WhenAll(tasks);
        }
        
        /// <summary>
        /// 获取当前设备状态
        /// </summary>
        public async Task<List<DeviceStatus>> GetCurrentDeviceStatusAsync()
        {
            try
            {
                var deviceStatusList = await _databaseService.GetAllDeviceStatusAsync();
                
                // 更新连接状态信息
                foreach (var status in deviceStatusList)
                {
                    status.ConnectionStatus = _plcService.IsConnected ? "已连接" : "未连接";
                }
                
                return deviceStatusList;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "获取设备状态失败");
                return new List<DeviceStatus>();
            }
        }
        
        /// <summary>
        /// 清理过期日志
        /// </summary>
        public async Task CleanupOldLogsAsync()
        {
            try
            {
                var retentionDaysStr = await _databaseService.GetSystemConfigAsync("log_retention_days") ?? "30";
                if (int.TryParse(retentionDaysStr, out int retentionDays))
                {
                    await _databaseService.CleanupOldLogsAsync(retentionDays);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "清理过期日志失败");
            }
        }
        
        /// <summary>
        /// 释放资源
        /// </summary>
        public override void Dispose()
        {
            StopCollection();
            _plcService?.Dispose();
            base.Dispose();
        }
    }
    
    /// <summary>
    /// 数据采集事件参数
    /// </summary>
    public class DataCollectionEventArgs : EventArgs
    {
        public Dictionary<string, double> CollectedData { get; set; } = new Dictionary<string, double>();
        public List<string> Errors { get; set; } = new List<string>();
        public DateTime StartTime { get; set; }
        public DateTime EndTime { get; set; }
        public bool IsSuccess { get; set; }
        public bool ConnectionStatus { get; set; }
        
        public TimeSpan Duration => EndTime - StartTime;
    }
    
    /// <summary>
    /// 连接状态事件参数
    /// </summary>
    public class ConnectionStatusEventArgs : EventArgs
    {
        public bool IsConnected { get; set; }
        public string StatusMessage { get; set; } = string.Empty;
        public DateTime Timestamp { get; set; }
    }
}