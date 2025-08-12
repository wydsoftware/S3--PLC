# 移动端派工系统架构设计文档

## 1. 系统架构概述

### 1.1 整体架构
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   移动端前端    │    │   后端服务      │    │   数据库层      │    │   PLC设备层     │
│  (Mobile Web)   │◄──►│ (Spring Boot)   │◄──►│    (MySQL)      │    │   (TCP/IP)      │
└─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 1.2 技术栈选择
- **前端**: HTML5 + CSS3 + JavaScript (响应式设计)
- **后端**: Java Spring Boot 2.x
- **数据库**: MySQL 8.0
- **通信协议**: HTTP/HTTPS + TCP/IP (PLC通信)
- **部署**: 内网部署，支持移动设备访问

## 2. 系统分层架构

### 2.1 表现层 (Presentation Layer)
```
移动端界面
├── 登录页面
├── 主菜单页面
├── 设备派工页面
├── 停止生产页面
├── 设备报修页面
├── 状态查询页面
└── PLC配置页面

后台管理界面
├── 角色管理
├── 生产记录查询
└── 报表统计
```

### 2.2 业务逻辑层 (Business Logic Layer)
```
Controller层
├── AuthController (认证控制器)
├── DeviceController (设备管理控制器)
├── WorkOrderController (派工单控制器)
├── MaintenanceController (维修管理控制器)
├── PLCController (PLC通信控制器)
└── ReportController (报表控制器)

Service层
├── UserService (用户服务)
├── DeviceService (设备服务)
├── WorkOrderService (派工服务)
├── ProductionService (生产服务)
├── MaintenanceService (维修服务)
├── PLCService (PLC通信服务)
└── ReportService (报表服务)
```

### 2.3 数据访问层 (Data Access Layer)
```
Repository层
├── UserRepository
├── DeviceRepository
├── WorkOrderRepository
├── ProductionRecordRepository
├── MaintenanceRecordRepository
├── PLCConfigRepository
└── SystemLogRepository
```

### 2.4 数据持久层 (Data Persistence Layer)
```
MySQL数据库
├── users (用户表)
├── devices (设备表)
├── work_orders (派工单表)
├── production_records (生产记录表)
├── maintenance_records (维修记录表)
├── plc_config (PLC配置表)
└── system_logs (系统日志表)
```

## 3. 核心组件设计

### 3.1 PLC通信组件
```java
@Component
public class PLCCommunicationService {
    // TCP连接管理
    private Socket plcSocket;
    
    // 连接PLC
    public boolean connectPLC(String ip, int port);
    
    // 写入数据点位
    public boolean writeDataPoint(String address, int value);
    
    // 读取数据点位
    public int readDataPoint(String address);
    
    // 设备清零操作
    public boolean resetDevice(String deviceCode);
    
    // 验证设备状态
    public boolean verifyDeviceStatus(String deviceCode);
}
```

### 3.2 设备状态管理组件
```java
@Service
public class DeviceStatusService {
    // 更新设备状态
    public void updateDeviceStatus(String deviceCode, DeviceStatus status);
    
    // 检查设备可用性
    public boolean isDeviceAvailable(String deviceCode);
    
    // 获取设备当前状态
    public DeviceStatus getDeviceStatus(String deviceCode);
    
    // 设备状态变更通知
    public void notifyStatusChange(String deviceCode, DeviceStatus oldStatus, DeviceStatus newStatus);
}
```

### 3.3 派工流程管理组件
```java
@Service
public class WorkOrderFlowService {
    // 创建派工单
    public WorkOrder createWorkOrder(WorkOrderRequest request);
    
    // 开始生产
    public boolean startProduction(String orderNo);
    
    // 停止生产
    public ProductionRecord stopProduction(String orderNo);
    
    // 验证派工条件
    public ValidationResult validateWorkOrder(WorkOrderRequest request);
}
```

## 4. 数据流设计

### 4.1 设备派工流程
```
用户输入派工信息 → 系统验证 → PLC通信验证 → 创建派工单 → 更新设备状态 → 记录日志
```

### 4.2 生产停止流程
```
选择派工单 → 确认停止 → 计算生产数据 → 更新记录 → 释放设备 → 生成报告
```

### 4.3 设备报修流程
```
选择设备 → 填写故障信息 → 更新设备状态 → 创建维修记录 → 通知相关人员
```

## 5. 安全架构

### 5.1 认证授权
- JWT Token认证
- 角色基础访问控制(RBAC)
- 会话超时管理

### 5.2 数据安全
- 密码加密存储(BCrypt)
- SQL注入防护
- XSS攻击防护
- 敏感数据脱敏

### 5.3 通信安全
- HTTPS加密传输
- API接口限流
- 请求参数验证
- 异常统一处理

## 6. 性能优化

### 6.1 数据库优化
- 索引优化
- 查询优化
- 连接池配置
- 读写分离(可选)

### 6.2 应用优化
- 缓存策略(Redis可选)
- 异步处理
- 连接复用
- 资源压缩

### 6.3 前端优化
- 资源压缩
- 懒加载
- 本地缓存
- 响应式设计

## 7. 监控与日志

### 7.1 系统监控
- 应用性能监控
- 数据库性能监控
- PLC通信状态监控
- 设备状态实时监控

### 7.2 日志管理
- 操作日志记录
- 错误日志收集
- 性能日志分析
- 审计日志追踪

## 8. 部署架构

### 8.1 开发环境
```
开发机 → 本地MySQL → 模拟PLC
```

### 8.2 生产环境
```
应用服务器 → MySQL数据库 → 实际PLC设备
```

### 8.3 网络拓扑
```
移动设备 (WiFi) → 内网交换机 → 应用服务器 → 数据库服务器
                                    ↓
                              PLC设备网络
```

## 9. 扩展性设计

### 9.1 水平扩展
- 微服务架构准备
- 负载均衡支持
- 数据库分片准备

### 9.2 功能扩展
- 插件化架构
- 配置化管理
- 多租户支持

### 9.3 集成扩展
- ERP系统集成接口
- MES系统集成接口
- 第三方设备接入

## 10. 技术选型说明

### 10.1 为什么选择Spring Boot
- 快速开发和部署
- 丰富的生态系统
- 良好的社区支持
- 企业级应用成熟度高

### 10.2 为什么选择MySQL
- 成熟稳定的关系型数据库
- 良好的事务支持
- 丰富的工具生态
- 团队技术栈匹配

### 10.3 为什么选择移动Web
- 跨平台兼容性好
- 开发维护成本低
- 部署更新便捷
- 无需应用商店审核