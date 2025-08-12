-- 移动端派工系统数据库初始化脚本
-- 数据库: pda_system
-- 版本: 1.0
-- 创建时间: 2025-01-08

-- 创建数据库
CREATE DATABASE IF NOT EXISTS pda_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pda_system;

-- 1. 用户表
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    role VARCHAR(20) DEFAULT 'user' COMMENT '角色(admin/user)',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    status TINYINT DEFAULT 1 COMMENT '状态(1:启用 0:禁用)'
) COMMENT='用户表';

-- 2. 设备信息表
CREATE TABLE devices (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    device_code VARCHAR(20) NOT NULL UNIQUE COMMENT '设备编号',
    device_name VARCHAR(50) NOT NULL COMMENT '设备名称',
    device_type VARCHAR(20) NOT NULL COMMENT '设备类型(AOI/CNC/CCM08/CCM23)',
    plc_address VARCHAR(50) COMMENT 'PLC地址映射',
    status VARCHAR(20) DEFAULT 'idle' COMMENT '设备状态(idle:闲置/working:工作中/maintenance:维修中)',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='设备信息表';

-- 3. 派工单表
CREATE TABLE work_orders (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '派工单ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '派工单号',
    device_id INT NOT NULL COMMENT '设备ID',
    device_code VARCHAR(20) NOT NULL COMMENT '设备编号',
    planned_quantity INT NOT NULL COMMENT '预计生产数量',
    actual_quantity INT DEFAULT 0 COMMENT '实际生产数量',
    start_time TIMESTAMP NULL COMMENT '开始生产时间',
    end_time TIMESTAMP NULL COMMENT '结束生产时间',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态(pending:待开始/working:生产中/completed:已完成/stopped:已停止)',
    created_by VARCHAR(50) COMMENT '创建人',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (device_id) REFERENCES devices(id)
) COMMENT='派工单表';

-- 4. 生产记录表
CREATE TABLE production_records (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    work_order_id INT NOT NULL COMMENT '派工单ID',
    order_no VARCHAR(50) NOT NULL COMMENT '派工单号',
    device_id INT NOT NULL COMMENT '设备ID',
    device_code VARCHAR(20) NOT NULL COMMENT '设备编号',
    device_type VARCHAR(20) NOT NULL COMMENT '设备类型',
    planned_quantity INT NOT NULL COMMENT '预计生产数量',
    actual_quantity INT NOT NULL COMMENT '实际生产数量',
    completion_rate DECIMAL(5,2) COMMENT '完成率(%)',
    start_time TIMESTAMP NOT NULL COMMENT '开始时间',
    end_time TIMESTAMP NOT NULL COMMENT '结束时间',
    duration_minutes INT COMMENT '生产时长(分钟)',
    operator VARCHAR(50) COMMENT '操作员',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (work_order_id) REFERENCES work_orders(id),
    FOREIGN KEY (device_id) REFERENCES devices(id)
) COMMENT='生产记录表';

-- 5. 设备维修记录表
CREATE TABLE maintenance_records (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '维修记录ID',
    device_id INT NOT NULL COMMENT '设备ID',
    device_code VARCHAR(20) NOT NULL COMMENT '设备编号',
    fault_description TEXT COMMENT '故障描述',
    maintenance_type VARCHAR(20) DEFAULT 'repair' COMMENT '维修类型(repair:报修/fixed:已修复)',
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '报修时间',
    end_time TIMESTAMP NULL COMMENT '修复时间',
    operator VARCHAR(50) COMMENT '操作员',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态(pending:待维修/completed:已完成)',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (device_id) REFERENCES devices(id)
) COMMENT='设备维修记录表';

-- 6. PLC配置表
CREATE TABLE plc_config (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_name VARCHAR(50) NOT NULL COMMENT '配置名称',
    plc_ip VARCHAR(15) NOT NULL COMMENT 'PLC IP地址',
    plc_port INT DEFAULT 502 COMMENT 'PLC端口',
    timeout_seconds INT DEFAULT 5 COMMENT '连接超时时间(秒)',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用(1:启用 0:禁用)',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='PLC配置表';

-- 7. 系统日志表
CREATE TABLE system_logs (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id INT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    action VARCHAR(100) NOT NULL COMMENT '操作动作',
    module VARCHAR(50) COMMENT '操作模块',
    description TEXT COMMENT '操作描述',
    ip_address VARCHAR(15) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id)
) COMMENT='系统日志表';

-- 创建索引
-- 设备表索引
CREATE INDEX idx_devices_type ON devices(device_type);
CREATE INDEX idx_devices_status ON devices(status);

-- 派工单表索引
CREATE INDEX idx_work_orders_device ON work_orders(device_id);
CREATE INDEX idx_work_orders_status ON work_orders(status);
CREATE INDEX idx_work_orders_time ON work_orders(start_time, end_time);

-- 生产记录表索引
CREATE INDEX idx_production_device ON production_records(device_id);
CREATE INDEX idx_production_time ON production_records(start_time, end_time);
CREATE INDEX idx_production_order ON production_records(order_no);

-- 维修记录表索引
CREATE INDEX idx_maintenance_device ON maintenance_records(device_id);
CREATE INDEX idx_maintenance_status ON maintenance_records(status);
CREATE INDEX idx_maintenance_time ON maintenance_records(start_time, end_time);

-- 系统日志表索引
CREATE INDEX idx_logs_user ON system_logs(user_id);
CREATE INDEX idx_logs_time ON system_logs(created_time);
CREATE INDEX idx_logs_action ON system_logs(action);