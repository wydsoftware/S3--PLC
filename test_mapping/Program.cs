using System;
using System.Threading.Tasks;
using Microsoft.Data.Sqlite;
using Modbus.Device;
using System.Net.Sockets;

class Program
{
    static async Task Main(string[] args)
    {
        Console.WriteLine("测试修复后的地址映射...");
        
        // 连接到PLC
        var tcpClient = new TcpClient();
        await tcpClient.ConnectAsync("192.168.1.2", 502);
        var master = ModbusIpMaster.CreateIp(tcpClient);
        
        Console.WriteLine("连接到PLC成功！");
        
        // 测试读取几个关键地址
        var testAddresses = new[] { 802, 804, 806, 808, 810 };
        
        Console.WriteLine("\n=== 使用修复后的地址映射（D地址直接映射）===");
        foreach (var addr in testAddresses)
        {
            try
            {
                var values = master.ReadHoldingRegisters(1, (ushort)addr, 1);
                Console.WriteLine($"D{addr} -> Modbus{addr} = {values[0]}");
                
                // 如果读取到非零值，写入数据库进行测试
                if (values[0] != 0)
                {
                    await WriteToDatabase($"TEST-D{addr}", addr, values[0]);
                    Console.WriteLine($"  -> 已写入数据库: {values[0]}");
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"D{addr} 读取失败: {ex.Message}");
            }
        }
        
        tcpClient.Close();
        Console.WriteLine("\n测试完成。按任意键退出...");
        Console.ReadKey();
    }
    
    static async Task WriteToDatabase(string deviceName, int address, int value)
    {
        var connectionString = "Data Source=plc_data.db;Cache=Shared";
        using var connection = new SqliteConnection(connectionString);
        await connection.OpenAsync();
        
        var sql = @"INSERT INTO device_data (device_name, address, value, timestamp, status) 
                   VALUES (@deviceName, @address, @value, @timestamp, 'SUCCESS')";
        
        using var command = new SqliteCommand(sql, connection);
        command.Parameters.AddWithValue("@deviceName", deviceName);
        command.Parameters.AddWithValue("@address", address);
        command.Parameters.AddWithValue("@value", value);
        command.Parameters.AddWithValue("@timestamp", DateTime.Now);
        
        await command.ExecuteNonQueryAsync();
    }
}