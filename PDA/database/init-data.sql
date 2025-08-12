-- 移动端派工系统初始化数据脚本
-- 数据库: pda_system
-- 版本: 1.0
-- 创建时间: 2025-01-08

USE pda_system;

-- 插入默认用户数据
INSERT INTO users (username, password, role, status) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb0VeCdiNOyWoVlYlS.9FHK.WSk9.g2qI2q4HOpjS', 'admin', 1), -- 密码: 123456
('user', '$2a$10$N.zmdr9k7uOCQb0VeCdiNOyWoVlYlS.9FHK.WSk9.g2qI2q4HOpjS', 'user', 1);   -- 密码: 123456

-- 插入48台设备数据
-- AOI设备 (2台)
INSERT INTO devices (device_code, device_name, device_type, plc_address, status) VALUES 
('AOI-01', 'AOI检测设备01', 'AOI', 'D1002', 'idle'),
('AOI-02', 'AOI检测设备02', 'AOI', 'D1004', 'idle');

-- CCM08设备 (24台)
INSERT INTO devices (device_code, device_name, device_type, plc_address, status) VALUES 
('CCM08-01', 'CCM08生产设备01', 'CCM08', 'D1006', 'idle'),
('CCM08-02', 'CCM08生产设备02', 'CCM08', 'D1008', 'idle'),
('CCM08-03', 'CCM08生产设备03', 'CCM08', 'D1010', 'idle'),
('CCM08-04', 'CCM08生产设备04', 'CCM08', 'D1012', 'idle'),
('CCM08-05', 'CCM08生产设备05', 'CCM08', 'D1014', 'idle'),
('CCM08-06', 'CCM08生产设备06', 'CCM08', 'D1016', 'idle'),
('CCM08-07', 'CCM08生产设备07', 'CCM08', 'D1018', 'idle'),
('CCM08-08', 'CCM08生产设备08', 'CCM08', 'D1020', 'idle'),
('CCM08-09', 'CCM08生产设备09', 'CCM08', 'D1022', 'idle'),
('CCM08-10', 'CCM08生产设备10', 'CCM08', 'D1024', 'idle'),
('CCM08-11', 'CCM08生产设备11', 'CCM08', 'D1026', 'idle'),
('CCM08-12', 'CCM08生产设备12', 'CCM08', 'D1028', 'idle'),
('CCM08-13', 'CCM08生产设备13', 'CCM08', 'D1030', 'idle'),
('CCM08-14', 'CCM08生产设备14', 'CCM08', 'D1032', 'idle'),
('CCM08-15', 'CCM08生产设备15', 'CCM08', 'D1034', 'idle'),
('CCM08-16', 'CCM08生产设备16', 'CCM08', 'D1036', 'idle'),
('CCM08-17', 'CCM08生产设备17', 'CCM08', 'D1038', 'idle'),
('CCM08-18', 'CCM08生产设备18', 'CCM08', 'D1040', 'idle'),
('CCM08-19', 'CCM08生产设备19', 'CCM08', 'D1042', 'idle'),
('CCM08-20', 'CCM08生产设备20', 'CCM08', 'D1044', 'idle'),
('CCM08-21', 'CCM08生产设备21', 'CCM08', 'D1046', 'idle'),
('CCM08-22', 'CCM08生产设备22', 'CCM08', 'D1048', 'idle'),
('CCM08-23', 'CCM08生产设备23', 'CCM08', 'D1050', 'idle'),
('CCM08-24', 'CCM08生产设备24', 'CCM08', 'D1052', 'idle');

-- CCM23设备 (6台)
INSERT INTO devices (device_code, device_name, device_type, plc_address, status) VALUES 
('CCM23-01', 'CCM23生产设备01', 'CCM23', 'D1054', 'idle'),
('CCM23-02', 'CCM23生产设备02', 'CCM23', 'D1056', 'idle'),
('CCM23-03', 'CCM23生产设备03', 'CCM23', 'D1058', 'idle'),
('CCM23-04', 'CCM23生产设备04', 'CCM23', 'D1060', 'idle'),
('CCM23-05', 'CCM23生产设备05', 'CCM23', 'D1062', 'idle'),
('CCM23-06', 'CCM23生产设备06', 'CCM23', 'D1064', 'idle');

-- CNC设备 (16台)
INSERT INTO devices (device_code, device_name, device_type, plc_address, status) VALUES 
('CNC-01', 'CNC加工设备01', 'CNC', 'D1066', 'idle'),
('CNC-02', 'CNC加工设备02', 'CNC', 'D1068', 'idle'),
('CNC-03', 'CNC加工设备03', 'CNC', 'D1070', 'idle'),
('CNC-04', 'CNC加工设备04', 'CNC', 'D1072', 'idle'),
('CNC-05', 'CNC加工设备05', 'CNC', 'D1074', 'idle'),
('CNC-06', 'CNC加工设备06', 'CNC', 'D1076', 'idle'),
('CNC-07', 'CNC加工设备07', 'CNC', 'D1078', 'idle'),
('CNC-08', 'CNC加工设备08', 'CNC', 'D1080', 'idle'),
('CNC-09', 'CNC加工设备09', 'CNC', 'D1082', 'idle'),
('CNC-10', 'CNC加工设备10', 'CNC', 'D1084', 'idle'),
('CNC-11', 'CNC加工设备11', 'CNC', 'D1086', 'idle'),
('CNC-12', 'CNC加工设备12', 'CNC', 'D1088', 'idle'),
('CNC-13', 'CNC加工设备13', 'CNC', 'D1090', 'idle'),
('CNC-14', 'CNC加工设备14', 'CNC', 'D1092', 'idle'),
('CNC-15', 'CNC加工设备15', 'CNC', 'D1094', 'idle'),
('CNC-16', 'CNC加工设备16', 'CNC', 'D1096', 'idle');

-- 插入默认PLC配置
INSERT INTO plc_config (config_name, plc_ip, plc_port, timeout_seconds, is_active) VALUES 
('默认PLC配置', '192.168.1.100', 502, 5, 1);

-- 插入系统初始化日志
INSERT INTO system_logs (username, action, module, description, ip_address) VALUES 
('system', '系统初始化', 'SYSTEM', '移动端派工系统数据库初始化完成', '127.0.0.1');

-- 验证数据插入
SELECT '用户数据' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT '设备数据' as table_name, COUNT(*) as count FROM devices
UNION ALL
SELECT 'PLC配置' as table_name, COUNT(*) as count FROM plc_config
UNION ALL
SELECT '系统日志' as table_name, COUNT(*) as count FROM system_logs;

-- 按设备类型统计
SELECT device_type, COUNT(*) as device_count 
FROM devices 
GROUP BY device_type 
ORDER BY device_type;