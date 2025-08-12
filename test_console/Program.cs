using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Microsoft.Data.Sqlite;
using Modbus.Device;
using System.Net.Sockets;
using System.Threading;

namespace TestConsole
{
    class Program
    {
        private static bool _isRunning = true;
        private static TcpClient? _tcpClient;
        private static ModbusIpMaster? _master;
        
        static async Task Main(string[] args)
        {
            Console.WriteLine("启动S3 PLC数据采集服务...");
            Console.WriteLine("按 Ctrl+C 停止服务");
            
            // 处理Ctrl+C退出
            Console.CancelKeyPress += (sender, e) => {
                e.Cancel = true;
                _isRunning = false;
                Console.WriteLine("\n正在停止数据采集服务...");
            };
            
            try
            {
                // 连接到PLC
                await ConnectToPLCAsync();
                
                // 开始数据采集循环
                await StartDataCollectionAsync();
            }
            catch (Exception ex)
            {
                Console.WriteLine($"服务启动失败: {ex.Message}");
            }
            finally
            {
                _tcpClient?.Close();
                Console.WriteLine("数据采集服务已停止");
            }
        }
        
        private static async Task ConnectToPLCAsync()
        {
            _tcpClient = new TcpClient();
            await _tcpClient.ConnectAsync("192.168.1.2", 502);
            _master = ModbusIpMaster.CreateIp(_tcpClient);
            Console.WriteLine("连接到PLC成功！");
        }
        
        private static async Task StartDataCollectionAsync()
        {
            var connectionString = "Data Source=../plc_data.db";
            
            // CNC设备地址映射 - 与数据库schema和PLC测试程序保持一致
            var cncDevices = new Dictionary<string, int>
            {
                { "CNC-01", 866 }, { "CNC-02", 868 }, { "CNC-03", 870 }, { "CNC-04", 872 },
                { "CNC-05", 874 }, { "CNC-06", 876 }, { "CNC-07", 878 }, { "CNC-08", 880 },
                { "CNC-09", 882 }, { "CNC-10", 884 }, { "CNC-11", 886 }, { "CNC-12", 888 },
                { "CNC-13", 890 }, { "CNC-14", 892 }, { "CNC-15", 894 }, { "CNC-16", 896 }
            };
            
            while (_isRunning)
            {
                try
                {
                    Console.WriteLine($"[{DateTime.Now:yyyy-MM-dd HH:mm:ss}] 开始数据采集...");
                    
                    using var connection = new SqliteConnection(connectionString);
                    connection.Open();
                    
                    int successCount = 0;
                    int totalCount = cncDevices.Count;
                    
                    foreach (var device in cncDevices)
                    {
                        try
                        {
                            // 读取PLC数据
                            var values = _master.ReadHoldingRegisters(1, (ushort)device.Value, 1);
                            var value = values[0];
                            
                            // 更新数据库 - 使用正确的表结构
                            var updateSql = @"
                                INSERT OR REPLACE INTO device_data 
                                (device_name, device_address, current_value, last_update_time, created_time) 
                                VALUES (@deviceName, @deviceAddress, @currentValue, @lastUpdateTime, @createdTime)";
                            
                            using var command = new SqliteCommand(updateSql, connection);
                            command.Parameters.AddWithValue("@deviceName", device.Key);
                            command.Parameters.AddWithValue("@deviceAddress", $"D{device.Value}");
                            command.Parameters.AddWithValue("@currentValue", (double)value);
                            command.Parameters.AddWithValue("@lastUpdateTime", DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss"));
                            command.Parameters.AddWithValue("@createdTime", DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss"));
                            
                            command.ExecuteNonQuery();
                            successCount++;
                            
                            Console.WriteLine($"  {device.Key} (D{device.Value}) = {value}");
                        }
                        catch (Exception ex)
                        {
                            Console.WriteLine($"  {device.Key} 读取失败: {ex.Message}");
                        }
                    }
                    
                    Console.WriteLine($"数据采集完成: {successCount}/{totalCount} 设备成功更新");
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"数据采集异常: {ex.Message}");
                    
                    // 尝试重新连接PLC
                    try
                    {
                        _tcpClient?.Close();
                        await Task.Delay(2000);
                        await ConnectToPLCAsync();
                    }
                    catch (Exception reconnectEx)
                    {
                        Console.WriteLine($"PLC重连失败: {reconnectEx.Message}");
                    }
                }
                
                // 等待5秒后进行下一次采集
                for (int i = 0; i < 50 && _isRunning; i++)
                {
                    await Task.Delay(100);
                }
            }
        }
    }
}