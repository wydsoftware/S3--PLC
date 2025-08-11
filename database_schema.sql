-- 汇川S3 PLC数据采集系统数据库设计
-- 数据库类型：SQLite

-- 1. 设备数据表（存储最新数据，每个设备只保留一条记录）
CREATE TABLE IF NOT EXISTS device_data (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_name VARCHAR(50) NOT NULL UNIQUE,  -- 设备名称（如：AOI-01, CCM08-01等）
    device_address VARCHAR(10) NOT NULL,      -- PLC地址（如：D802, D804等）
    current_value REAL NOT NULL,              -- 当前读取值
    last_update_time DATETIME NOT NULL,       -- 最后更新时间
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(device_name)
);

-- 2. 数据读取日志表（记录当天所有读取操作）
CREATE TABLE IF NOT EXISTS read_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_name VARCHAR(50) NOT NULL,         -- 设备名称
    device_address VARCHAR(10) NOT NULL,      -- PLC地址
    read_value REAL NOT NULL,                 -- 读取到的值
    read_time DATETIME NOT NULL,              -- 读取时间
    status VARCHAR(20) DEFAULT 'SUCCESS',     -- 读取状态（SUCCESS/ERROR）
    error_message TEXT,                       -- 错误信息（如果有）
    created_date DATE DEFAULT (DATE('now'))   -- 创建日期（用于按天清理日志）
);

-- 3. 系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    config_key VARCHAR(50) NOT NULL UNIQUE,
    config_value VARCHAR(200) NOT NULL,
    description TEXT,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 4. 设备配置表（预定义48个点位）
CREATE TABLE IF NOT EXISTS device_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_name VARCHAR(50) NOT NULL UNIQUE,
    device_address VARCHAR(10) NOT NULL,
    address_type VARCHAR(10) DEFAULT 'D',     -- 地址类型（D寄存器）
    address_number INTEGER NOT NULL,          -- 地址编号
    data_type VARCHAR(20) DEFAULT 'INT16',    -- 数据类型
    is_enabled BOOLEAN DEFAULT 1,            -- 是否启用
    description TEXT,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 插入默认系统配置
INSERT OR REPLACE INTO system_config (config_key, config_value, description) VALUES
('plc_ip_address', '192.168.1.2', 'PLC IP地址'),
('plc_port', '502', 'PLC Modbus TCP端口'),
('read_interval_seconds', '5', '数据读取间隔（秒）'),
('log_retention_days', '30', '日志保留天数'),
('auto_start_service', '1', '开机自动启动服务');

-- 插入48个预定义设备点位
INSERT OR REPLACE INTO device_config (device_name, device_address, address_number, description) VALUES
-- AOI系列
('AOI-01', '802', 802, 'AOI设备01'),
('AOI-02', '804', 804, 'AOI设备02'),

-- CCM08系列
('CCM08-01', '806', 806, 'CCM08设备01'),
('CCM08-02', '808', 808, 'CCM08设备02'),
('CCM08-03', '810', 810, 'CCM08设备03'),
('CCM08-04', '812', 812, 'CCM08设备04'),
('CCM08-05', '814', 814, 'CCM08设备05'),
('CCM08-06', '816', 816, 'CCM08设备06'),
('CCM08-07', '818', 818, 'CCM08设备07'),
('CCM08-08', '820', 820, 'CCM08设备08'),
('CCM08-09', '822', 822, 'CCM08设备09'),
('CCM08-10', '824', 824, 'CCM08设备10'),
('CCM08-11', '826', 826, 'CCM08设备11'),
('CCM08-12', '828', 828, 'CCM08设备12'),
('CCM08-13', '830', 830, 'CCM08设备13'),
('CCM08-14', '832', 832, 'CCM08设备14'),
('CCM08-15', '834', 834, 'CCM08设备15'),
('CCM08-16', '836', 836, 'CCM08设备16'),
('CCM08-17', '838', 838, 'CCM08设备17'),
('CCM08-18', '840', 840, 'CCM08设备18'),
('CCM08-19', '842', 842, 'CCM08设备19'),
('CCM08-20', '844', 844, 'CCM08设备20'),
('CCM08-21', '846', 846, 'CCM08设备21'),
('CCM08-22', '848', 848, 'CCM08设备22'),
('CCM08-23', '850', 850, 'CCM08设备23'),
('CCM08-24', '852', 852, 'CCM08设备24'),

-- CCM23系列
('CCM23-01', '854', 854, 'CCM23设备01'),
('CCM23-02', '856', 856, 'CCM23设备02'),
('CCM23-03', '858', 858, 'CCM23设备03'),
('CCM23-04', '860', 860, 'CCM23设备04'),
('CCM23-05', '862', 862, 'CCM23设备05'),
('CCM23-06', '864', 864, 'CCM23设备06'),

-- CNC系列
('CNC-01', '866', 866, 'CNC设备01'),
('CNC-02', '868', 868, 'CNC设备02'),
('CNC-03', '870', 870, 'CNC设备03'),
('CNC-04', '872', 872, 'CNC设备04'),
('CNC-05', '874', 874, 'CNC设备05'),
('CNC-06', '876', 876, 'CNC设备06'),
('CNC-07', '878', 878, 'CNC设备07'),
('CNC-08', '880', 880, 'CNC设备08'),
('CNC-09', '882', 882, 'CNC设备09'),
('CNC-10', '884', 884, 'CNC设备10'),
('CNC-11', '886', 886, 'CNC设备11'),
('CNC-12', '888', 888, 'CNC设备12'),
('CNC-13', '890', 890, 'CNC设备13'),
('CNC-14', '892', 892, 'CNC设备14'),
('CNC-15', '894', 894, 'CNC设备15'),
('CNC-16', '896', 896, 'CNC设备16');

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_device_data_name ON device_data(device_name);
CREATE INDEX IF NOT EXISTS idx_read_log_time ON read_log(read_time);
CREATE INDEX IF NOT EXISTS idx_read_log_date ON read_log(created_date);
CREATE INDEX IF NOT EXISTS idx_system_config_key ON system_config(config_key);

-- 创建视图：当前所有设备状态
CREATE VIEW IF NOT EXISTS v_current_device_status AS
SELECT 
    dc.device_name,
    dc.device_address,
    dc.address_number,
    COALESCE(dd.current_value, 0) as current_value,
    dd.last_update_time,
    dc.is_enabled,
    dc.description
FROM device_config dc
LEFT JOIN device_data dd ON dc.device_name = dd.device_name
WHERE dc.is_enabled = 1
ORDER BY dc.address_number;