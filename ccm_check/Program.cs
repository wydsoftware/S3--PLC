using System;
using System.Data.SQLite;
using System.IO;

class Program
{
    static void Main()
    {
        string dbPath = "../plc_data.db";
        
        if (!File.Exists(dbPath))
        {
            Console.WriteLine($"数据库文件不存在: {dbPath}");
            return;
        }
        
        using (var connection = new SQLiteConnection($"Data Source={dbPath}"))
        {
            connection.Open();
            
            Console.WriteLine("=== CCM08和CCM23设备数据详情 ===");
            
            string sql = @"
                SELECT 
                    dc.device_name,
                    dc.device_address,
                    dc.address_number,
                    COALESCE(dd.current_value, 0) as current_value,
                    dd.last_update_time,
                    dc.is_enabled
                FROM device_config dc
                LEFT JOIN device_data dd ON dc.device_name = dd.device_name
                WHERE (dc.device_name LIKE 'CCM08-%' OR dc.device_name LIKE 'CCM23-%') 
                  AND dc.is_enabled = 1
                ORDER BY dc.device_name";
            
            using (var command = new SQLiteCommand(sql, connection))
            using (var reader = command.ExecuteReader())
            {
                int ccm08Count = 0, ccm23Count = 0;
                int ccm08Total = 0, ccm23Total = 0;
                
                while (reader.Read())
                {
                    string deviceName = reader["device_name"]?.ToString() ?? "";
                    string deviceAddress = reader["device_address"]?.ToString() ?? "";
                    int currentValue = reader["current_value"] != DBNull.Value ? Convert.ToInt32(reader["current_value"]) : 0;
                    string lastUpdate = reader.IsDBNull("last_update_time") ? "无数据" : reader["last_update_time"]?.ToString() ?? "";
                    
                    Console.WriteLine($"{deviceName}: 地址={deviceAddress}, 值={currentValue}, 更新时间={lastUpdate}");
                    
                    if (deviceName.StartsWith("CCM08"))
                    {
                        ccm08Count++;
                        ccm08Total += currentValue;
                    }
                    else if (deviceName.StartsWith("CCM23"))
                    {
                        ccm23Count++;
                        ccm23Total += currentValue;
                    }
                }
                
                Console.WriteLine($"\n=== 统计结果 ===");
                Console.WriteLine($"CCM08设备: {ccm08Count}个, 总计值: {ccm08Total}");
                Console.WriteLine($"CCM23设备: {ccm23Count}个, 总计值: {ccm23Total}");
            }
        }
        
        Console.WriteLine("\n按任意键退出...");
        Console.ReadKey();
    }
}