using System;
using System.Threading;
using System.Threading.Tasks;
using NModbus;
using System.Net.Sockets;
using System.Collections.Generic;
using System.Linq;

namespace PLCCNCTest
{
    /// <summary>
    /// PLC CNC测试程序
    /// 功能：每1秒随机选择CNC01-16中的一个设备，将其点位值加1
    /// 用于测试大屏和采集程序的数据更新功能
    /// </summary>
    class Program
    {
        // PLC连接配置
        private const string PLC_HOST = "192.168.1.2";
        private const int PLC_PORT = 502;
        
        // CNC设备地址映射 (实际PLC D寄存器地址)
        private static readonly Dictionary<string, ushort> CNC_ADDRESSES = new Dictionary<string, ushort>
        {
            { "CNC01", 866 },  // CNC-01 对应 D866
            { "CNC02", 868 },  // CNC-02 对应 D868
            { "CNC03", 870 },  // CNC-03 对应 D870
            { "CNC04", 872 },  // CNC-04 对应 D872
            { "CNC05", 874 },  // CNC-05 对应 D874
            { "CNC06", 876 },  // CNC-06 对应 D876
            { "CNC07", 878 },  // CNC-07 对应 D878
            { "CNC08", 880 },  // CNC-08 对应 D880
            { "CNC09", 882 },  // CNC-09 对应 D882
            { "CNC10", 884 },  // CNC-10 对应 D884
            { "CNC11", 886 },  // CNC-11 对应 D886
            { "CNC12", 888 },  // CNC-12 对应 D888
            { "CNC13", 890 },  // CNC-13 对应 D890
            { "CNC14", 892 },  // CNC-14 对应 D892
            { "CNC15", 894 },  // CNC-15 对应 D894
            { "CNC16", 896 }   // CNC-16 对应 D896
        };
        
        private static TcpClient? _tcpClient;
        private static IModbusMaster? _master;
        private static bool _isRunning = false;
        private static readonly Random _random = new Random();
        
        static async Task Main(string[] args)
        {
            Console.WriteLine("=== PLC CNC 模拟测试程序 ===");
            Console.WriteLine("按 Ctrl+C 停止测试");
            
            // 设置Ctrl+C事件处理
            Console.CancelKeyPress += (sender, e) =>
            {
                e.Cancel = true;
                _isRunning = false;
                Console.WriteLine("\n收到停止信号，正在停止测试...");
            };
            
            try
            {
                if (await ConnectToPLCAsync())
                {
                    await StartSimulationAsync();
                }
                else
                {
                    Console.WriteLine("无法连接到PLC，测试终止");
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"程序运行时发生错误: {ex.Message}");
            }
            finally
            {
                DisconnectFromPLC();
                Console.WriteLine("=== PLC CNC 模拟测试结束 ===");
            }
        }
        
        /// <summary>
        /// 连接到PLC
        /// </summary>
        private static async Task<bool> ConnectToPLCAsync()
        {
            try
            {
                Console.WriteLine($"正在连接到PLC: {PLC_HOST}:{PLC_PORT}");
                
                _tcpClient = new TcpClient();
                await _tcpClient.ConnectAsync(PLC_HOST, PLC_PORT);
                
                var factory = new ModbusFactory();
                _master = factory.CreateMaster(_tcpClient);
                
                Console.WriteLine("✓ 成功连接到PLC");
                return true;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"✗ 连接PLC失败: {ex.Message}");
                return false;
            }
        }
        
        /// <summary>
        /// 断开PLC连接
        /// </summary>
        private static void DisconnectFromPLC()
        {
            try
            {
                _master?.Dispose();
                _tcpClient?.Close();
                Console.WriteLine("PLC连接已关闭");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"关闭PLC连接时发生错误: {ex.Message}");
            }
        }
        
        /// <summary>
        /// 开始模拟测试
        /// </summary>
        private static async Task StartSimulationAsync()
        {
            _isRunning = true;
            int successCount = 0;
            int totalCount = 0;
            
            Console.WriteLine("开始模拟CNC数据更新...");
            
            while (_isRunning)
            {
                try
                {
                    totalCount++;
                    
                    if (await SimulateCNCDataAsync())
                    {
                        successCount++;
                    }
                    
                    // 每10次显示统计信息
                    if (totalCount % 10 == 0)
                    {
                        double successRate = (double)successCount / totalCount * 100;
                        Console.WriteLine($"统计: 总计 {totalCount} 次，成功 {successCount} 次，成功率 {successRate:F1}%");
                    }
                    
                    // 等待1秒
                    await Task.Delay(1000);
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"模拟过程中发生错误: {ex.Message}");
                    
                    // 尝试重新连接
                    if (!await ReconnectToPLCAsync())
                    {
                        Console.WriteLine("重连失败，停止测试");
                        break;
                    }
                }
            }
            
            double finalSuccessRate = totalCount > 0 ? (double)successCount / totalCount * 100 : 0;
            Console.WriteLine($"\n最终统计: 总计 {totalCount} 次，成功 {successCount} 次，成功率 {finalSuccessRate:F1}%");
        }
        
        /// <summary>
        /// 模拟CNC数据更新
        /// </summary>
        private static async Task<bool> SimulateCNCDataAsync()
        {
            try
            {
                // 随机选择一个CNC设备
                var cncDevices = CNC_ADDRESSES.Keys.ToArray();
                string selectedCNC = cncDevices[_random.Next(cncDevices.Length)];
                ushort address = CNC_ADDRESSES[selectedCNC];
                
                // 读取当前值
                ushort[] currentValues = await _master!.ReadHoldingRegistersAsync(1, address, 1);
                ushort currentValue = currentValues[0];
                
                // 增加1
                ushort newValue = (ushort)(currentValue + 1);
                
                // 写入新值
                await _master.WriteSingleRegisterAsync(1, address, newValue);
                
                Console.WriteLine($"✓ {selectedCNC} (地址: {address}): {currentValue} → {newValue}");
                return true;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"✗ 模拟数据更新失败: {ex.Message}");
                return false;
            }
        }
        
        /// <summary>
        /// 重新连接到PLC
        /// </summary>
        private static async Task<bool> ReconnectToPLCAsync()
        {
            Console.WriteLine("尝试重新连接到PLC...");
            
            DisconnectFromPLC();
            await Task.Delay(2000); // 等待2秒后重连
            
            return await ConnectToPLCAsync();
        }
    }
}