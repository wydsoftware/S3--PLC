using Microsoft.Data.Sqlite;
using Microsoft.Extensions.Logging;
using S3PLCDataCollector.Models;
using System;
using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;

namespace S3PLCDataCollector.Services
{
    /// <summary>
    /// 数据库服务类
    /// </summary>
    public class DatabaseService
    {
        private readonly string _connectionString;
        private readonly ILogger<DatabaseService> _logger;
        
        public DatabaseService(string connectionString, ILogger<DatabaseService> logger)
        {
            _connectionString = connectionString;
            _logger = logger;
        }
        
        /// <summary>
        /// 初始化数据库
        /// </summary>
        public async Task InitializeDatabaseAsync()
        {
            try
            {
                // 确保数据库目录存在
                var dbPath = GetDatabasePath();
                var dbDirectory = Path.GetDirectoryName(dbPath);
                if (!string.IsNullOrEmpty(dbDirectory) && !Directory.Exists(dbDirectory))
                {
                    Directory.CreateDirectory(dbDirectory);
                }
                
                // 读取并执行SQL脚本
                var sqlScript = await File.ReadAllTextAsync("database_schema.sql");
                await ExecuteNonQueryAsync(sqlScript);
                
                _logger.LogInformation("数据库初始化完成");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "数据库初始化失败");
                throw;
            }
        }
        
        /// <summary>
        /// 获取数据库文件路径
        /// </summary>
        private string GetDatabasePath()
        {
            var builder = new SqliteConnectionStringBuilder(_connectionString);
            return builder.DataSource;
        }
        
        /// <summary>
        /// 执行非查询SQL
        /// </summary>
        private async Task ExecuteNonQueryAsync(string sql)
        {
            using var connection = new SqliteConnection(_connectionString);
            await connection.OpenAsync();
            
            // 分割SQL脚本并逐个执行
            var lines = sql.Split(new[] { '\r', '\n' }, StringSplitOptions.RemoveEmptyEntries);
            var currentCommand = new System.Text.StringBuilder();
            
            foreach (var line in lines)
            {
                var trimmedLine = line.Trim();
                
                // 跳过空行和注释行
                if (string.IsNullOrEmpty(trimmedLine) || trimmedLine.StartsWith("--"))
                    continue;
                
                currentCommand.AppendLine(trimmedLine);
                
                // 如果行以分号结尾，执行当前命令
                if (trimmedLine.EndsWith(";"))
                {
                    var commandText = currentCommand.ToString().Trim();
                    if (!string.IsNullOrEmpty(commandText))
                    {
                        try
                        {
                            using var command = new SqliteCommand(commandText, connection);
                            await command.ExecuteNonQueryAsync();
                            _logger.LogDebug($"执行SQL命令成功: {commandText.Substring(0, Math.Min(50, commandText.Length))}...");
                        }
                        catch (Exception ex)
                        {
                            _logger.LogError(ex, $"执行SQL命令失败: {commandText.Substring(0, Math.Min(100, commandText.Length))}...");
                            throw;
                        }
                    }
                    currentCommand.Clear();
                }
            }
            
            // 执行最后一个命令（如果没有以分号结尾）
            var finalCommand = currentCommand.ToString().Trim();
            if (!string.IsNullOrEmpty(finalCommand))
            {
                try
                {
                    using var command = new SqliteCommand(finalCommand, connection);
                    await command.ExecuteNonQueryAsync();
                    _logger.LogDebug($"执行最终SQL命令成功: {finalCommand.Substring(0, Math.Min(50, finalCommand.Length))}...");
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, $"执行最终SQL命令失败: {finalCommand.Substring(0, Math.Min(100, finalCommand.Length))}...");
                    throw;
                }
            }
        }
        
        /// <summary>
        /// 更新或插入设备数据
        /// </summary>
        public async Task UpsertDeviceDataAsync(string deviceName, string deviceAddress, double value)
        {
            const string sql = @"
                INSERT OR REPLACE INTO device_data (device_name, device_address, current_value, last_update_time, created_time)
                VALUES (@deviceName, @deviceAddress, @value, @updateTime, 
                        COALESCE((SELECT created_time FROM device_data WHERE device_name = @deviceName), @updateTime))";
            
            try
            {
                using var connection = new SqliteConnection(_connectionString);
                await connection.OpenAsync();
                
                using var command = new SqliteCommand(sql, connection);
                command.Parameters.AddWithValue("@deviceName", deviceName);
                command.Parameters.AddWithValue("@deviceAddress", deviceAddress);
                command.Parameters.AddWithValue("@value", value);
                command.Parameters.AddWithValue("@updateTime", DateTime.Now);
                
                await command.ExecuteNonQueryAsync();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "更新设备数据失败: {DeviceName}", deviceName);
                throw;
            }
        }
        
        /// <summary>
        /// 插入读取日志
        /// </summary>
        public async Task InsertReadLogAsync(string deviceName, string deviceAddress, double value, string status = "SUCCESS", string? errorMessage = null)
        {
            const string sql = @"
                INSERT INTO read_log (device_name, device_address, read_value, read_time, status, error_message, created_date)
                VALUES (@deviceName, @deviceAddress, @value, @readTime, @status, @errorMessage, @createdDate)";
            
            try
            {
                using var connection = new SqliteConnection(_connectionString);
                await connection.OpenAsync();
                
                using var command = new SqliteCommand(sql, connection);
                command.Parameters.AddWithValue("@deviceName", deviceName);
                command.Parameters.AddWithValue("@deviceAddress", deviceAddress);
                command.Parameters.AddWithValue("@value", value);
                command.Parameters.AddWithValue("@readTime", DateTime.Now);
                command.Parameters.AddWithValue("@status", status);
                command.Parameters.AddWithValue("@errorMessage", errorMessage ?? (object)DBNull.Value);
                command.Parameters.AddWithValue("@createdDate", DateTime.Today);
                
                await command.ExecuteNonQueryAsync();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "插入读取日志失败: {DeviceName}", deviceName);
                // 日志插入失败不抛出异常，避免影响主流程
            }
        }
        
        /// <summary>
        /// 获取所有设备状态
        /// </summary>
        public async Task<List<DeviceStatus>> GetAllDeviceStatusAsync()
        {
            const string sql = "SELECT * FROM v_current_device_status ORDER BY address_number";
            var deviceStatusList = new List<DeviceStatus>();
            
            try
            {
                using var connection = new SqliteConnection(_connectionString);
                await connection.OpenAsync();
                
                using var command = new SqliteCommand(sql, connection);
                using var reader = await command.ExecuteReaderAsync();
                
                while (await reader.ReadAsync())
                {
                    deviceStatusList.Add(new DeviceStatus
                    {
                        DeviceName = reader.GetString(reader.GetOrdinal("device_name")),
                        DeviceAddress = reader.GetString(reader.GetOrdinal("device_address")),
                        AddressNumber = reader.GetInt32(reader.GetOrdinal("address_number")),
                        CurrentValue = reader.IsDBNull(reader.GetOrdinal("current_value")) ? 0 : reader.GetDouble(reader.GetOrdinal("current_value")),
                        LastUpdateTime = reader.IsDBNull(reader.GetOrdinal("last_update_time")) ? null : reader.GetDateTime(reader.GetOrdinal("last_update_time")),
                        IsEnabled = reader.GetBoolean(reader.GetOrdinal("is_enabled")),
                        Description = reader.IsDBNull(reader.GetOrdinal("description")) ? null : reader.GetString(reader.GetOrdinal("description"))
                    });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "获取设备状态失败");
                throw;
            }
            
            return deviceStatusList;
        }
        
        /// <summary>
        /// 获取启用的设备配置
        /// </summary>
        public async Task<List<DeviceConfig>> GetEnabledDeviceConfigsAsync()
        {
            const string sql = "SELECT * FROM device_config WHERE is_enabled = 1 ORDER BY address_number";
            var deviceConfigs = new List<DeviceConfig>();
            
            try
            {
                using var connection = new SqliteConnection(_connectionString);
                await connection.OpenAsync();
                
                using var command = new SqliteCommand(sql, connection);
                using var reader = await command.ExecuteReaderAsync();
                
                while (await reader.ReadAsync())
                {
                    deviceConfigs.Add(new DeviceConfig
                    {
                        Id = reader.GetInt32(reader.GetOrdinal("id")),
                        DeviceName = reader.GetString(reader.GetOrdinal("device_name")),
                        DeviceAddress = reader.GetString(reader.GetOrdinal("device_address")),
                        AddressType = reader.GetString(reader.GetOrdinal("address_type")),
                        AddressNumber = reader.GetInt32(reader.GetOrdinal("address_number")),
                        DataType = reader.GetString(reader.GetOrdinal("data_type")),
                        IsEnabled = reader.GetBoolean(reader.GetOrdinal("is_enabled")),
                        Description = reader.IsDBNull(reader.GetOrdinal("description")) ? null : reader.GetString(reader.GetOrdinal("description")),
                        CreatedTime = reader.GetDateTime(reader.GetOrdinal("created_time"))
                    });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "获取设备配置失败");
                throw;
            }
            
            return deviceConfigs;
        }
        
        /// <summary>
        /// 获取系统配置
        /// </summary>
        public async Task<string?> GetSystemConfigAsync(string configKey)
        {
            const string sql = "SELECT config_value FROM system_config WHERE config_key = @configKey";
            
            try
            {
                using var connection = new SqliteConnection(_connectionString);
                await connection.OpenAsync();
                
                using var command = new SqliteCommand(sql, connection);
                command.Parameters.AddWithValue("@configKey", configKey);
                
                var result = await command.ExecuteScalarAsync();
                return result?.ToString();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "获取系统配置失败: {ConfigKey}", configKey);
                return null;
            }
        }
        
        /// <summary>
        /// 更新系统配置
        /// </summary>
        public async Task UpdateSystemConfigAsync(string configKey, string configValue)
        {
            const string sql = @"
                INSERT OR REPLACE INTO system_config (config_key, config_value, updated_time)
                VALUES (@configKey, @configValue, @updatedTime)";
            
            try
            {
                using var connection = new SqliteConnection(_connectionString);
                await connection.OpenAsync();
                
                using var command = new SqliteCommand(sql, connection);
                command.Parameters.AddWithValue("@configKey", configKey);
                command.Parameters.AddWithValue("@configValue", configValue);
                command.Parameters.AddWithValue("@updatedTime", DateTime.Now);
                
                await command.ExecuteNonQueryAsync();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "更新系统配置失败: {ConfigKey}", configKey);
                throw;
            }
        }
        
        /// <summary>
        /// 清理过期日志
        /// </summary>
        public async Task CleanupOldLogsAsync(int retentionDays)
        {
            const string sql = "DELETE FROM read_log WHERE created_date < @cutoffDate";
            var cutoffDate = DateTime.Today.AddDays(-retentionDays);
            
            try
            {
                using var connection = new SqliteConnection(_connectionString);
                await connection.OpenAsync();
                
                using var command = new SqliteCommand(sql, connection);
                command.Parameters.AddWithValue("@cutoffDate", cutoffDate);
                
                var deletedRows = await command.ExecuteNonQueryAsync();
                if (deletedRows > 0)
                {
                    _logger.LogInformation("清理了 {DeletedRows} 条过期日志记录", deletedRows);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "清理过期日志失败");
            }
        }
        
        /// <summary>
        /// 获取今日读取日志
        /// </summary>
        public async Task<List<ReadLog>> GetTodayReadLogsAsync(int limit = 100)
        {
            const string sql = @"
                SELECT * FROM read_log 
                WHERE created_date = @today 
                ORDER BY read_time DESC 
                LIMIT @limit";
            
            var readLogs = new List<ReadLog>();
            
            try
            {
                using var connection = new SqliteConnection(_connectionString);
                await connection.OpenAsync();
                
                using var command = new SqliteCommand(sql, connection);
                command.Parameters.AddWithValue("@today", DateTime.Today);
                command.Parameters.AddWithValue("@limit", limit);
                
                using var reader = await command.ExecuteReaderAsync();
                
                while (await reader.ReadAsync())
                {
                    readLogs.Add(new ReadLog
                    {
                        Id = reader.GetInt32(reader.GetOrdinal("id")),
                        DeviceName = reader.GetString(reader.GetOrdinal("device_name")),
                        DeviceAddress = reader.GetString(reader.GetOrdinal("device_address")),
                        ReadValue = reader.GetDouble(reader.GetOrdinal("read_value")),
                        ReadTime = reader.GetDateTime(reader.GetOrdinal("read_time")),
                        Status = reader.GetString(reader.GetOrdinal("status")),
                        ErrorMessage = reader.IsDBNull(reader.GetOrdinal("error_message")) ? null : reader.GetString(reader.GetOrdinal("error_message")),
                        CreatedDate = reader.GetDateTime(reader.GetOrdinal("created_date"))
                    });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "获取今日读取日志失败");
                throw;
            }
            
            return readLogs;
        }
    }
}