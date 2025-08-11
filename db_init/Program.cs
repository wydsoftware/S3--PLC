using Microsoft.Data.Sqlite;
using System;
using System.IO;

class Program
{
    static void Main()
    {
        try
        {
            var connectionString = "Data Source=plc_data.db;Cache=Shared";
            
            // 读取SQL脚本
            var sqlScript = File.ReadAllText("database_schema.sql");
            
            // 创建数据库连接并执行脚本
            using var connection = new SqliteConnection(connectionString);
            connection.Open();
            
            using var command = new SqliteCommand(sqlScript, connection);
            command.ExecuteNonQuery();
            
            Console.WriteLine("数据库初始化成功！");
            
            // 验证表是否创建成功
            using var checkCommand = new SqliteCommand("SELECT COUNT(*) FROM device_config", connection);
            var count = checkCommand.ExecuteScalar();
            Console.WriteLine($"设备配置表中有 {count} 条记录");
        }
        catch (Exception ex)
        {
            Console.WriteLine($"数据库初始化失败: {ex.Message}");
            Console.WriteLine($"详细错误: {ex}");
        }
        
        Console.WriteLine("按任意键退出...");
        Console.ReadKey();
    }
}