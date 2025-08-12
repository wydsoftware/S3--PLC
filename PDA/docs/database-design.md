# 移动端派工系统数据库设计

## 数据库概述

### 数据库信息
- **数据库类型**: MySQL
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci
- **存储引擎**: InnoDB

## 数据表设计

### 1. 用户表 (users)
用户认证和权限管理

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    real_name VARCHAR(100) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    role ENUM('admin', 'operator', 'viewer') DEFAULT 'operator' COMMENT '角色',
    status ENUM('active', 'inactive') DEFAULT 'active' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_at TIMESTAMP NULL COMMENT '最后登录时间'
) COMMENT='用户表';
```

### 2. 设备信息表 (equipment)
设备基础信息管理

```sql
CREATE TABLE equipment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    equipment_code VARCHAR(20) NOT NULL UNIQUE COMMENT '设备编号',
    equipment_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    equipment_type ENUM('AOI', 'CNC', 'CCM08', 'CCM23') NOT NULL COMMENT '设备类型',
    plc_address VARCHAR(20) NOT NULL COMMENT 'PLC点位地址',
    status ENUM('idle', 'running', 'maintenance', 'fault') DEFAULT 'idle' COMMENT '设备状态',
    location VARCHAR(100) COMMENT '设备位置',
    specifications TEXT COMMENT '设备规格',
    purchase_date DATE COMMENT '采购日期',
    warranty_date DATE COMMENT '保修期至',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='设备信息表';
```

### 3. 派工单表 (work_orders)
派工单主表

```sql
CREATE TABLE work_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '派工单ID',
    work_order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '派工单号',
    equipment_id BIGINT NOT NULL COMMENT '设备ID',
    equipment_code VARCHAR(20) NOT NULL COMMENT '设备编号',
    planned_quantity INT NOT NULL COMMENT '预计生产数量',
    actual_quantity INT DEFAULT 0 COMMENT '实际生产数量',
    status ENUM('pending', 'running', 'completed', 'cancelled') DEFAULT 'pending' COMMENT '状态',
    start_time TIMESTAMP NULL COMMENT '开始生产时间',
    end_time TIMESTAMP NULL COMMENT '结束生产时间',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
) COMMENT='派工单表';
```

### 4. 生产记录表 (production_records)
生产过程记录

```sql
CREATE TABLE production_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    work_order_id BIGINT NOT NULL COMMENT '派工单ID',
    work_order_no VARCHAR(50) NOT NULL COMMENT '派工单号',
    equipment_id BIGINT NOT NULL COMMENT '设备ID',
    equipment_code VARCHAR(20) NOT NULL COMMENT '设备编号',
    planned_quantity INT NOT NULL COMMENT '预计生产数量',
    actual_quantity INT NOT NULL COMMENT '实际生产数量',
    start_time TIMESTAMP NOT NULL COMMENT '开始时间',
    end_time TIMESTAMP NOT NULL COMMENT '结束时间',
    duration_minutes INT COMMENT '生产时长(分钟)',
    completion_rate DECIMAL(5,2) COMMENT '完成率(%)',
    efficiency_rate DECIMAL(5,2) COMMENT '效率(%)',
    status ENUM('completed', 'stopped', 'cancelled') NOT NULL COMMENT '记录状态',
    operator_id BIGINT COMMENT '操作员ID',
    notes TEXT COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (work_order_id) REFERENCES work_orders(id),
    FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    FOREIGN KEY (operator_id) REFERENCES users(id)
) COMMENT='生产记录表';
```

### 5. 维修记录表 (maintenance_records)
设备维修记录

```sql
CREATE TABLE maintenance_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '维修记录ID',
    maintenance_no VARCHAR(50) NOT NULL UNIQUE COMMENT '维修单号',
    equipment_id BIGINT NOT NULL COMMENT '设备ID',
    equipment_code VARCHAR(20) NOT NULL COMMENT '设备编号',
    fault_description TEXT NOT NULL COMMENT '故障描述',
    maintenance_type ENUM('preventive', 'corrective', 'emergency') NOT NULL COMMENT '维修类型',
    priority ENUM('low', 'medium', 'high', 'urgent') DEFAULT 'medium' COMMENT '优先级',
    status ENUM('pending', 'in_progress', 'completed', 'cancelled') DEFAULT 'pending' COMMENT '状态',
    reported_by BIGINT NOT NULL COMMENT '报修人ID',
    assigned_to BIGINT COMMENT '维修人员ID',
    start_time TIMESTAMP NULL COMMENT '开始维修时间',
    end_time TIMESTAMP NULL COMMENT '完成维修时间',
    duration_minutes INT COMMENT '维修时长(分钟)',
    maintenance_cost DECIMAL(10,2) COMMENT '维修费用',
    parts_used TEXT COMMENT '使用配件',
    solution_description TEXT COMMENT '解决方案描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    FOREIGN KEY (reported_by) REFERENCES users(id),
    FOREIGN KEY (assigned_to) REFERENCES users(id)
) COMMENT='维修记录表';
```

### 6. 生产统计表 (production_statistics)
生产统计数据

```sql
CREATE TABLE production_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    statistics_date DATE NOT NULL COMMENT '统计日期',
    equipment_id BIGINT NOT NULL COMMENT '设备ID',
    equipment_code VARCHAR(20) NOT NULL COMMENT '设备编号',
    equipment_type ENUM('AOI', 'CNC', 'CCM08', 'CCM23') NOT NULL COMMENT '设备类型',
    total_work_orders INT DEFAULT 0 COMMENT '总派工单数',
    completed_work_orders INT DEFAULT 0 COMMENT '完成派工单数',
    total_planned_quantity INT DEFAULT 0 COMMENT '总计划产量',
    total_actual_quantity INT DEFAULT 0 COMMENT '总实际产量',
    total_runtime_minutes INT DEFAULT 0 COMMENT '总运行时间(分钟)',
    total_downtime_minutes INT DEFAULT 0 COMMENT '总停机时间(分钟)',
    utilization_rate DECIMAL(5,2) COMMENT '设备利用率(%)',
    efficiency_rate DECIMAL(5,2) COMMENT '生产效率(%)',
    completion_rate DECIMAL(5,2) COMMENT '完成率(%)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    UNIQUE KEY uk_date_equipment (statistics_date, equipment_id)
) COMMENT='生产统计表';
```

### 7. 系统配置表 (system_config)
系统配置信息

```sql
CREATE TABLE system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type ENUM('string', 'number', 'boolean', 'json') DEFAULT 'string' COMMENT '配置类型',
    description VARCHAR(255) COMMENT '配置描述',
    is_system BOOLEAN DEFAULT FALSE COMMENT '是否系统配置',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='系统配置表';
```

### 8. 操作日志表 (operation_logs)
系统操作日志

```sql
CREATE TABLE operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_module VARCHAR(50) NOT NULL COMMENT '操作模块',
    operation_description TEXT COMMENT '操作描述',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(255) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    execution_time INT COMMENT '执行时间(毫秒)',
    status ENUM('success', 'failure') DEFAULT 'success' COMMENT '执行状态',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id)
) COMMENT='操作日志表';
```

## 初始化数据脚本

### 设备基础数据
```sql
-- AOI设备 (2台)
INSERT INTO equipment (equipment_code, equipment_name, equipment_type, plc_address) VALUES 
('AOI-01', 'AOI检测设备01', 'AOI', 'D1002'),
('AOI-02', 'AOI检测设备02', 'AOI', 'D1004');

-- CNC设备 (16台)
INSERT INTO equipment (equipment_code, equipment_name, equipment_type, plc_address) VALUES 
('CNC-01', 'CNC加工中心01', 'CNC', 'D1066'),
('CNC-02', 'CNC加工中心02', 'CNC', 'D1068'),
('CNC-03', 'CNC加工中心03', 'CNC', 'D1070'),
('CNC-04', 'CNC加工中心04', 'CNC', 'D1072'),
('CNC-05', 'CNC加工中心05', 'CNC', 'D1074'),
('CNC-06', 'CNC加工中心06', 'CNC', 'D1076'),
('CNC-07', 'CNC加工中心07', 'CNC', 'D1078'),
('CNC-08', 'CNC加工中心08', 'CNC', 'D1080'),
('CNC-09', 'CNC加工中心09', 'CNC', 'D1082'),
('CNC-10', 'CNC加工中心10', 'CNC', 'D1084'),
('CNC-11', 'CNC加工中心11', 'CNC', 'D1086'),
('CNC-12', 'CNC加工中心12', 'CNC', 'D1088'),
('CNC-13', 'CNC加工中心13', 'CNC', 'D1090'),
('CNC-14', 'CNC加工中心14', 'CNC', 'D1092'),
('CNC-15', 'CNC加工中心15', 'CNC', 'D1094'),
('CNC-16', 'CNC加工中心16', 'CNC', 'D1096');

-- CCM08设备 (24台)
INSERT INTO equipment (equipment_code, equipment_name, equipment_type, plc_address) VALUES 
('CCM08-01', 'CCM08设备01', 'CCM08', 'D1006'),
('CCM08-02', 'CCM08设备02', 'CCM08', 'D1008'),
('CCM08-03', 'CCM08设备03', 'CCM08', 'D1010'),
('CCM08-04', 'CCM08设备04', 'CCM08', 'D1012'),
('CCM08-05', 'CCM08设备05', 'CCM08', 'D1014'),
('CCM08-06', 'CCM08设备06', 'CCM08', 'D1016'),
('CCM08-07', 'CCM08设备07', 'CCM08', 'D1018'),
('CCM08-08', 'CCM08设备08', 'CCM08', 'D1020'),
('CCM08-09', 'CCM08设备09', 'CCM08', 'D1022'),
('CCM08-10', 'CCM08设备10', 'CCM08', 'D1024'),
('CCM08-11', 'CCM08设备11', 'CCM08', 'D1026'),
('CCM08-12', 'CCM08设备12', 'CCM08', 'D1028'),
('CCM08-13', 'CCM08设备13', 'CCM08', 'D1030'),
('CCM08-14', 'CCM08设备14', 'CCM08', 'D1032'),
('CCM08-15', 'CCM08设备15', 'CCM08', 'D1034'),
('CCM08-16', 'CCM08设备16', 'CCM08', 'D1036'),
('CCM08-17', 'CCM08设备17', 'CCM08', 'D1038'),
('CCM08-18', 'CCM08设备18', 'CCM08', 'D1040'),
('CCM08-19', 'CCM08设备19', 'CCM08', 'D1042'),
('CCM08-20', 'CCM08设备20', 'CCM08', 'D1044'),
('CCM08-21', 'CCM08设备21', 'CCM08', 'D1046'),
('CCM08-22', 'CCM08设备22', 'CCM08', 'D1048'),
('CCM08-23', 'CCM08设备23', 'CCM08', 'D1050'),
('CCM08-24', 'CCM08设备24', 'CCM08', 'D1052');

-- CCM23设备 (6台)
INSERT INTO equipment (equipment_code, equipment_name, equipment_type, plc_address) VALUES 
('CCM23-01', 'CCM23设备01', 'CCM23', 'D1054'),
('CCM23-02', 'CCM23设备02', 'CCM23', 'D1056'),
('CCM23-03', 'CCM23设备03', 'CCM23', 'D1058'),
('CCM23-04', 'CCM23设备04', 'CCM23', 'D1060'),
('CCM23-05', 'CCM23设备05', 'CCM23', 'D1062'),
('CCM23-06', 'CCM23设备06', 'CCM23', 'D1064');

-- 默认管理员用户 (密码: 123456)
INSERT INTO users (username, password, real_name, role, status) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXISwKhUOKmyqjNNHPjsOLB7Nqm', '系统管理员', 'admin', 'active');

-- 系统配置
INSERT INTO system_config (config_key, config_value, config_type, description, is_system) VALUES 
('plc.ip.address', '192.168.1.100', 'string', 'PLC IP地址', true),
('plc.port', '502', 'number', 'PLC通信端口', true),
('plc.timeout', '5000', 'number', 'PLC通信超时时间(毫秒)', true),
('plc.retry.count', '3', 'number', 'PLC通信重试次数', true);
```

## 索引优化

```sql
-- 性能优化索引
CREATE INDEX idx_work_orders_equipment ON work_orders(equipment_id);
CREATE INDEX idx_work_orders_status ON work_orders(status);
CREATE INDEX idx_work_orders_created_at ON work_orders(created_at);
CREATE INDEX idx_production_records_work_order ON production_records(work_order_id);
CREATE INDEX idx_production_records_equipment ON production_records(equipment_id);
CREATE INDEX idx_maintenance_records_equipment ON maintenance_records(equipment_id);
CREATE INDEX idx_maintenance_records_status ON maintenance_records(status);
CREATE INDEX idx_operation_logs_user ON operation_logs(user_id);
CREATE INDEX idx_operation_logs_date ON operation_logs(created_at);
```