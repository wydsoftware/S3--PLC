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
            
            // 检查device_config表
            Console.WriteLine("=== Device Config ===");
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