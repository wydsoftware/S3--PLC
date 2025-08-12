using System;
using Microsoft.Data.Sqlite;

class Program
{
    static void Main()
    {
        var connectionString = "Data Source=plc_data.db;Cache=Shared";
        
        try
        {
            using var connection = new SqliteConnection(connectionString);
            connection.Open();
            
            // 统计总设备配置数量
            Console.WriteLine("=== 设备配置统计 ===");
            using var totalCmd = new SqliteCommand("SELECT COUNT(*) as total FROM device_config WHERE is_enabled = 1", connection);
            var totalCount = Convert.ToInt32(totalCmd.ExecuteScalar());
            Console.WriteLine($"启用的设备总数: {totalCount}");
            
            // 按设备类型统计
            Console.WriteLine("\n=== 按设备类型统计 ===");
            var deviceTypes = new[] { "AOI", "CCM08", "CCM23", "CNC" };
            
            foreach (var type in deviceTypes)
            {
                using var typeCmd = new SqliteCommand($"SELECT COUNT(*) FROM device_config WHERE device_name LIKE '{type}%' AND is_enabled = 1", connection);
                var typeCount = Convert.ToInt32(typeCmd.ExecuteScalar());
                Console.WriteLine($"{type}设备数量: {typeCount}");
            }
            
            // 统计有数据的设备数量
            Console.WriteLine("\n=== 数据统计 ===");
            using var dataCountCmd = new SqliteCommand("SELECT COUNT(*) FROM device_data", connection);
            var dataCount = Convert.ToInt32(dataCountCmd.ExecuteScalar());
            Console.WriteLine($"有数据的设备数量: {dataCount}");
            
            // 检查数据值分布
            Console.WriteLine("\n=== 数据值分布 ===");
            using var valueCmd = new SqliteCommand("SELECT current_value, COUNT(*) as count FROM device_data GROUP BY current_value ORDER BY current_value", connection);
            using var valueReader = valueCmd.ExecuteReader();
            
            while (valueReader.Read())
            {
                Console.WriteLine($"值 {valueReader["current_value"]}: {valueReader["count"]} 个设备");
            }
            
            // 检查device_config表前5个设备
            Console.WriteLine("\n=== Device Config (前5个) ===");
            using var configCmd = new SqliteCommand("SELECT device_name, device_address, address_number, is_enabled FROM device_config LIMIT 5", connection);
            using var configReader = configCmd.ExecuteReader();
            while (configReader.Read())
            {
                Console.WriteLine($"{configReader["device_name"]}: {configReader["device_address"]} ({configReader["address_number"]}) - Enabled: {configReader["is_enabled"]}");
            }
            
            // 检查device_data表
            Console.WriteLine("\n=== Device Data ===");
            using var dataCmd = new SqliteCommand("SELECT device_name, device_address, current_value, last_update_time FROM device_data LIMIT 10", connection);
            using var dataReader = dataCmd.ExecuteReader();
            
            int count = 0;
            while (dataReader.Read())
            {
                Console.WriteLine($"{dataReader["device_name"]}: {dataReader["device_address"]} = {dataReader["current_value"]} at {dataReader["last_update_time"]}");
                count++;
            }
            
            if (count == 0)
            {
                Console.WriteLine("No data found in device_data table.");
            }
            
            // 检查read_log表
            Console.WriteLine("\n=== Read Log (Latest 5) ===");
            using var logCmd = new SqliteCommand("SELECT device_name, device_address, read_value, read_time, status FROM read_log ORDER BY read_time DESC LIMIT 5", connection);
            using var logReader = logCmd.ExecuteReader();
            
            int logCount = 0;
            while (logReader.Read())
            {
                Console.WriteLine($"{logReader["device_name"]}: {logReader["device_address"]} = {logReader["read_value"]} at {logReader["read_time"]} ({logReader["status"]})");
                logCount++;
            }
            
            if (logCount == 0)
            {
                Console.WriteLine("No data found in read_log table.");
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error: {ex.Message}");
        }
        
        Console.WriteLine("\nPress any key to exit...");
        Console.ReadKey();
    }
}