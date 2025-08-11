using System;

namespace S3PLCDataCollector.Models
{
    /// <summary>
    /// 设备数据实体类
    /// </summary>
    public class DeviceData
    {
        public int Id { get; set; }
        
        /// <summary>
        /// 设备名称
        /// </summary>
        public string DeviceName { get; set; } = string.Empty;
        
        /// <summary>
        /// 设备地址
        /// </summary>
        public string DeviceAddress { get; set; } = string.Empty;
        
        /// <summary>
        /// 当前值
        /// </summary>
        public double CurrentValue { get; set; }
        
        /// <summary>
        /// 最后更新时间
        /// </summary>
        public DateTime LastUpdateTime { get; set; }
        
        /// <summary>
        /// 创建时间
        /// </summary>
        public DateTime CreatedTime { get; set; }
    }
    
    /// <summary>
    /// 读取日志实体类
    /// </summary>
    public class ReadLog
    {
        public int Id { get; set; }
        
        /// <summary>
        /// 设备名称
        /// </summary>
        public string DeviceName { get; set; } = string.Empty;
        
        /// <summary>
        /// 设备地址
        /// </summary>
        public string DeviceAddress { get; set; } = string.Empty;
        
        /// <summary>
        /// 读取值
        /// </summary>
        public double ReadValue { get; set; }
        
        /// <summary>
        /// 读取时间
        /// </summary>
        public DateTime ReadTime { get; set; }
        
        /// <summary>
        /// 状态
        /// </summary>
        public string Status { get; set; } = "SUCCESS";
        
        /// <summary>
        /// 错误信息
        /// </summary>
        public string? ErrorMessage { get; set; }
        
        /// <summary>
        /// 创建日期
        /// </summary>
        public DateTime CreatedDate { get; set; }
    }
    
    /// <summary>
    /// 设备配置实体类
    /// </summary>
    public class DeviceConfig
    {
        public int Id { get; set; }
        
        /// <summary>
        /// 设备名称
        /// </summary>
        public string DeviceName { get; set; } = string.Empty;
        
        /// <summary>
        /// 设备地址
        /// </summary>
        public string DeviceAddress { get; set; } = string.Empty;
        
        /// <summary>
        /// 地址类型
        /// </summary>
        public string AddressType { get; set; } = "D";
        
        /// <summary>
        /// 地址编号
        /// </summary>
        public int AddressNumber { get; set; }
        
        /// <summary>
        /// 数据类型
        /// </summary>
        public string DataType { get; set; } = "INT16";
        
        /// <summary>
        /// 是否启用
        /// </summary>
        public bool IsEnabled { get; set; } = true;
        
        /// <summary>
        /// 描述
        /// </summary>
        public string? Description { get; set; }
        
        /// <summary>
        /// 创建时间
        /// </summary>
        public DateTime CreatedTime { get; set; }
    }
    
    /// <summary>
    /// 系统配置实体类
    /// </summary>
    public class SystemConfig
    {
        public int Id { get; set; }
        
        /// <summary>
        /// 配置键
        /// </summary>
        public string ConfigKey { get; set; } = string.Empty;
        
        /// <summary>
        /// 配置值
        /// </summary>
        public string ConfigValue { get; set; } = string.Empty;
        
        /// <summary>
        /// 描述
        /// </summary>
        public string? Description { get; set; }
        
        /// <summary>
        /// 更新时间
        /// </summary>
        public DateTime UpdatedTime { get; set; }
    }
    
    /// <summary>
    /// 设备状态视图实体类
    /// </summary>
    public class DeviceStatus
    {
        /// <summary>
        /// 设备名称
        /// </summary>
        public string DeviceName { get; set; } = string.Empty;
        
        /// <summary>
        /// 设备地址
        /// </summary>
        public string DeviceAddress { get; set; } = string.Empty;
        
        /// <summary>
        /// 地址编号
        /// </summary>
        public int AddressNumber { get; set; }
        
        /// <summary>
        /// 当前值
        /// </summary>
        public double CurrentValue { get; set; }
        
        /// <summary>
        /// 最后更新时间
        /// </summary>
        public DateTime? LastUpdateTime { get; set; }
        
        /// <summary>
        /// 是否启用
        /// </summary>
        public bool IsEnabled { get; set; }
        
        /// <summary>
        /// 描述
        /// </summary>
        public string? Description { get; set; }
        
        /// <summary>
        /// 连接状态
        /// </summary>
        public string ConnectionStatus { get; set; } = "未知";
    }
}