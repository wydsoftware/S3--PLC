using Microsoft.Extensions.Logging;
using S3PLCDataCollector.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Net.Sockets;
using Modbus.Device;

namespace S3PLCDataCollector.Services
{
    /// <summary>
    /// PLC通信服务类
    /// </summary>
    public class PLCService : IDisposable
    {
        private readonly ILogger<PLCService> _logger;
        private IModbusMaster? _modbusClient;
        private TcpClient? _tcpClient;
        private string _ipAddress = "192.168.1.2";
        private int _port = 502;
        private int _connectionTimeout = 3000;
        private bool _isConnected = false;
        private readonly object _lockObject = new object();
        
        public PLCService(ILogger<PLCService> logger)
        {
            _logger = logger;
        }
        
        /// <summary>
        /// 连接状态
        /// </summary>
        public bool IsConnected 
        { 
            get 
            { 
                lock (_lockObject)
                {
                    return _isConnected && _tcpClient?.Connected == true;
                }
            } 
        }
        
        /// <summary>
        /// 配置PLC连接参数
        /// </summary>
        public void Configure(string ipAddress, int port = 502, int connectionTimeout = 3000)
        {
            lock (_lockObject)
            {
                _ipAddress = ipAddress;
                _port = port;
                _connectionTimeout = connectionTimeout;
                
                _logger.LogInformation("PLC连接参数已更新: IP={IpAddress}, Port={Port}", ipAddress, port);
            }
        }
        
        /// <summary>
        /// 连接到PLC
        /// </summary>
        public async Task<bool> ConnectAsync()
        {
            try
            {
                lock (_lockObject)
                {
                    // 如果已连接，先断开
                    if (_tcpClient?.Connected == true)
                    {
                        _tcpClient.Close();
                    }
                    
                    // 创建新的TCP客户端
                    _tcpClient?.Dispose();
                    _tcpClient = new TcpClient();
                    _tcpClient.ReceiveTimeout = _connectionTimeout;
                    _tcpClient.SendTimeout = _connectionTimeout;
                }
                
                // 异步连接
                await _tcpClient.ConnectAsync(_ipAddress, _port);
                
                lock (_lockObject)
                {
                    _modbusClient = ModbusIpMaster.CreateIp(_tcpClient);
                    _isConnected = _tcpClient.Connected;
                }
                
                if (_isConnected)
                {
                    _logger.LogInformation("成功连接到PLC: {IpAddress}:{Port}", _ipAddress, _port);
                    return true;
                }
                else
                {
                    _logger.LogWarning("连接PLC失败: {IpAddress}:{Port}", _ipAddress, _port);
                    return false;
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "连接PLC时发生异常: {IpAddress}:{Port}", _ipAddress, _port);
                lock (_lockObject)
                {
                    _isConnected = false;
                }
                return false;
            }
        }
        
        /// <summary>
        /// 断开PLC连接
        /// </summary>
        public void Disconnect()
        {
            try
            {
                lock (_lockObject)
                {
                    if (_tcpClient?.Connected == true)
                    {
                        _tcpClient.Close();
                        _logger.LogInformation("已断开PLC连接");
                    }
                    _isConnected = false;
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "断开PLC连接时发生异常");
            }
        }
        
        /// <summary>
        /// 读取单个D寄存器
        /// </summary>
        public async Task<int?> ReadDRegisterAsync(int address)
        {
            try
            {
                if (!IsConnected)
                {
                    _logger.LogWarning("PLC未连接，无法读取D{Address}", address);
                    return null;
                }
                
                ushort[] result = await Task.Run(() =>
                {
                    lock (_lockObject)
                    {
                        // 汇川PLC D寄存器地址映射：D802直接对应Modbus地址802
                        // 根据测试结果，正确的映射方式是D地址直接对应Modbus地址
                        var modbusAddress = (ushort)address;
                        _logger.LogDebug("读取D{Address}，Modbus地址: {ModbusAddress}", address, modbusAddress);
                        return _modbusClient?.ReadHoldingRegisters(1, modbusAddress, 1) ?? new ushort[0];
                    }
                });
                
                if (result.Length > 0)
                {
                    return result[0];
                }
                
                _logger.LogWarning("读取D{Address}失败：返回数据为空", address);
                return null;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "读取D{Address}时发生异常", address);
                return null;
            }
        }
        
        /// <summary>
        /// 批量读取D寄存器
        /// </summary>
        public async Task<Dictionary<int, int>> ReadDRegistersAsync(List<int> addresses)
        {
            var results = new Dictionary<int, int>();
            
            if (!IsConnected)
            {
                _logger.LogWarning("PLC未连接，无法批量读取寄存器");
                return results;
            }
            
            try
            {
                // 简化实现：逐个读取
                foreach (var address in addresses)
                {
                    var value = await ReadDRegisterAsync(address);
                    if (value.HasValue)
                    {
                        results[address] = value.Value;
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "批量读取寄存器时发生异常");
            }
            
            return results;
        }
        
        /// <summary>
        /// 写入单个D寄存器
        /// </summary>
        public async Task<bool> WriteDRegisterAsync(int address, int value)
        {
            try
            {
                if (!IsConnected)
                {
                    _logger.LogWarning("PLC未连接，无法写入D{Address}", address);
                    return false;
                }
                
                await Task.Run(() =>
                {
                    lock (_lockObject)
                    {
                        _modbusClient?.WriteSingleRegister(1, (ushort)address, (ushort)value);
                    }
                });
                
                _logger.LogDebug("成功写入D{Address} = {Value}", address, value);
                return true;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "写入D{Address}时发生异常", address);
                return false;
            }
        }
        
        /// <summary>
        /// 测试连接
        /// </summary>
        public async Task<bool> TestConnectionAsync()
        {
            try
            {
                if (!IsConnected)
                {
                    return await ConnectAsync();
                }
                
                // 尝试读取一个寄存器来测试连接
                var result = await ReadDRegisterAsync(1);
                return result.HasValue;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "测试连接时发生异常");
                return false;
            }
        }
        
        /// <summary>
        /// 获取连接状态信息
        /// </summary>
        public string GetConnectionStatusInfo()
        {
            if (_tcpClient?.Connected == true)
            {
                return $"已连接到 {_ipAddress}:{_port}";
            }
            return "未连接";
        }
        
        /// <summary>
        /// 读取多个D寄存器（连续地址）
        /// </summary>
        public async Task<ushort[]?> ReadDRegisterAsync(int startAddress, int count)
        {
            try
            {
                if (!IsConnected)
                {
                    _logger.LogWarning("PLC未连接，无法读取D{StartAddress}-D{EndAddress}", startAddress, startAddress + count - 1);
                    return null;
                }
                
                ushort[] result = await Task.Run(() =>
                {
                    lock (_lockObject)
                    {
                        var modbusAddress = (ushort)(startAddress - 1);
                        _logger.LogDebug("批量读取D{StartAddress}-D{EndAddress}，Modbus地址: {ModbusAddress}-{EndModbusAddress}", 
                            startAddress, startAddress + count - 1, modbusAddress, modbusAddress + count - 1);
                        return _modbusClient?.ReadHoldingRegisters(1, modbusAddress, (ushort)count) ?? new ushort[0];
                    }
                });
                
                return result;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "读取D{StartAddress}-D{EndAddress}时发生异常", startAddress, startAddress + count - 1);
                return null;
            }
        }
        
        /// <summary>
        /// 读取所有设备数据
        /// </summary>
        public async Task<List<DeviceData>> ReadAllDeviceDataAsync(List<DeviceConfig> deviceConfigs)
        {
            var result = new List<DeviceData>();
            
            if (!IsConnected)
            {
                _logger.LogWarning("PLC未连接，无法读取设备数据");
                return result;
            }

            foreach (var config in deviceConfigs)
            {
                try
                {
                    var value = await ReadDRegisterAsync(config.AddressNumber);
                    if (value.HasValue)
                    {
                        var deviceData = new DeviceData
                        {
                            DeviceName = config.DeviceName,
                            DeviceAddress = config.DeviceAddress,
                            CurrentValue = value.Value,
                            LastUpdateTime = DateTime.Now,
                            CreatedTime = DateTime.Now
                        };
                        result.Add(deviceData);
                    }
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "读取设备 {DeviceName} 数据失败", config.DeviceName);
                }
            }

            return result;
        }
        
        /// <summary>
        /// 释放资源
        /// </summary>
        public void Dispose()
        {
            try
            {
                Disconnect();
                _modbusClient?.Dispose();
                _tcpClient?.Dispose();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "释放PLC服务资源时发生异常");
            }
        }
    }
}