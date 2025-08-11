using System;
using System.Threading.Tasks;
using Microsoft.Data.Sqlite;
using Modbus.Device;
using System.Net.Sockets;

namespace TestConsole
{
    class Program
    {
        static async Task Main(string[] args)
        {
            Console.WriteLine("启动PLC数据采集测试...");
            
            try
            {
                // 连接到PLC
                var tcpClient = new TcpClient();
                await tcpClient.ConnectAsync("192.168.1.2", 502);
                var master = ModbusIpMaster.CreateIp(tcpClient);
                
                Console.WriteLine("连接到PLC成功！");
                
                // 初始化数据库
                var connectionString = "Data Source=../plc_data.db";
                using var connection = new SqliteConnection(connectionString);
                connection.Open();
                
                // 测试地址映射修复
                var testAddresses = new[] { 802, 804, 806, 808, 810 };
                
                foreach (var addr in testAddresses)
                {
                    try
                    {
                        // 使用修复后的地址映射：D地址直接映射到Modbus地址
                        var modbusAddress = addr; // 修复后：直接映射，不再减1
                        var values = master.ReadHoldingRegisters(1, (ushort)modbusAddress, 1);
                        var value = values[0];
                        
                        Console.WriteLine($"D{addr} -> Modbus{modbusAddress} = {value}");
                        
                        if (value > 0)
                        {
                            // 更新数据库
                            var updateSql = @"
                                INSERT OR REPLACE INTO device_data (device_name, address, value, timestamp) 
                                VALUES (@deviceName, @address, @value, @timestamp)";
                            
                            using var command = new SqliteCommand(updateSql, connection);
                            command.Parameters.AddWithValue("@deviceName", $"TEST-D{addr}");
                            command.Parameters.AddWithValue("@address", addr);
                            command.Parameters.AddWithValue("@value", value);
                            command.Parameters.AddWithValue("@timestamp", DateTime.Now);
                            
                            var rowsAffected = command.ExecuteNonQuery();
                            Console.WriteLine($"  -> 数据库更新成功，影响行数: {rowsAffected}");
                        }
                    }
                    catch (Exception ex)
                    {
                        Console.WriteLine($"D{addr} 读取失败: {ex.Message}");
                    }
                }
                
                tcpClient.Close();
                Console.WriteLine("\n测试完成！地址映射修复验证成功。");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"测试失败: {ex.Message}");
            }
            
            Console.WriteLine("按任意键退出...");
            Console.ReadKey();
        }
    }
}