# 移动端派工系统业务流程文档

## 1. 业务流程概述

### 1.1 系统业务范围
移动端派工系统主要管理48台工业设备的生产调度，包括：
- **AOI设备**: 2台 (AOI-01, AOI-02)
- **CCM08设备**: 24台 (CCM08-01 ~ CCM08-24)
- **CCM23设备**: 6台 (CCM23-01 ~ CCM23-06)
- **CNC设备**: 16台 (CNC-01 ~ CNC-16)

### 1.2 核心业务流程
1. **设备派工流程** - 创建和分配生产任务
2. **生产执行流程** - 监控和管理生产过程
3. **生产停止流程** - 结束生产并记录数据
4. **设备维修流程** - 处理设备故障和维修
5. **状态监控流程** - 实时监控设备状态
6. **数据统计流程** - 生成生产报表和统计

## 2. 设备派工业务流程

### 2.1 流程图
```
开始 → 用户登录 → 选择设备派工 → 填写派工信息 → 选择设备 → 系统验证 → PLC通信检查 → 创建派工单 → 更新设备状态 → 记录日志 → 结束
```

### 2.2 详细流程步骤

#### 步骤1: 用户身份验证
- **输入**: 用户名、密码
- **验证**: 检查用户凭据
- **输出**: 登录成功/失败
- **异常处理**: 登录失败返回错误信息

#### 步骤2: 派工信息录入
- **必填字段**:
  - 派工单号（唯一性校验）
  - 预计生产数量（正整数）
  - 设备类型（CNC/CCM08/CCM23）
  - 设备编号（从可用设备中选择）
- **验证规则**:
  - 派工单号不能重复
  - 生产数量必须大于0
  - 设备必须处于闲置状态

#### 步骤3: 设备可用性检查
```sql
-- 检查设备状态
SELECT status FROM devices WHERE device_code = ?
-- status必须为'idle'才能派工
```

#### 步骤4: PLC通信验证
```java
// PLC通信流程
1. 连接PLC设备 (IP从plc_config表获取)
2. 对设备清零点位写入1
3. 等待1秒
4. 查询设备数据点位
5. 验证返回值是否为0
6. 最多重试3次
```

#### 步骤5: 派工单创建
```sql
-- 创建派工单记录
INSERT INTO work_orders (
    order_no, device_id, device_code, planned_quantity, 
    status, created_by, created_time
) VALUES (?, ?, ?, ?, 'pending', ?, NOW());

-- 更新设备状态
UPDATE devices SET status = 'working' WHERE device_code = ?;
```

#### 步骤6: 开始生产
```sql
-- 更新派工单状态
UPDATE work_orders SET 
    status = 'working', 
    start_time = NOW() 
WHERE order_no = ?;
```

### 2.3 业务规则

#### 派工约束条件
1. **设备状态约束**: 只有闲置状态的设备才能接受派工
2. **派工单唯一性**: 派工单号在系统中必须唯一
3. **PLC通信约束**: 必须通过PLC验证才能开始生产
4. **维修状态约束**: 维修中的设备不能接受派工

#### 数据完整性规则
1. 派工单与设备的关联关系必须有效
2. 生产数量必须为正整数
3. 时间戳必须符合逻辑顺序
4. 操作人员信息必须记录

## 3. 生产停止业务流程

### 3.1 流程图
```
开始 → 查看生产中设备 → 选择派工单 → 确认停止 → 计算生产数据 → 更新记录 → 释放设备 → 生成报告 → 结束
```

### 3.2 详细流程步骤

#### 步骤1: 查询生产中的派工单
```sql
-- 获取所有生产中的派工单
SELECT wo.*, d.device_name, 
       TIMESTAMPDIFF(MINUTE, wo.start_time, NOW()) as duration_minutes,
       ROUND((wo.actual_quantity / wo.planned_quantity) * 100, 2) as completion_rate
FROM work_orders wo
JOIN devices d ON wo.device_id = d.id
WHERE wo.status = 'working'
ORDER BY wo.start_time;
```

#### 步骤2: 生产数据计算
```java
// 计算生产统计数据
public ProductionSummary calculateProductionData(WorkOrder workOrder) {
    LocalDateTime startTime = workOrder.getStartTime();
    LocalDateTime endTime = LocalDateTime.now();
    
    // 计算生产时长（分钟）
    long durationMinutes = Duration.between(startTime, endTime).toMinutes();
    
    // 计算完成率
    double completionRate = (double) workOrder.getActualQuantity() / workOrder.getPlannedQuantity() * 100;
    
    // 计算生产效率（件/小时）
    double efficiency = workOrder.getActualQuantity() / (durationMinutes / 60.0);
    
    return new ProductionSummary(durationMinutes, completionRate, efficiency);
}
```

#### 步骤3: 生产记录保存
```sql
-- 创建生产记录
INSERT INTO production_records (
    work_order_id, order_no, device_id, device_code, device_type,
    planned_quantity, actual_quantity, completion_rate,
    start_time, end_time, duration_minutes, operator
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

-- 更新派工单状态
UPDATE work_orders SET 
    status = 'completed',
    end_time = NOW(),
    actual_quantity = ?
WHERE id = ?;

-- 释放设备
UPDATE devices SET status = 'idle' WHERE id = ?;
```

### 3.3 业务规则

#### 停止生产条件
1. 只有状态为'working'的派工单才能停止
2. 必须记录实际生产数量
3. 必须计算完成率和生产时长
4. 设备状态必须更新为闲置

#### 数据一致性保证
1. 使用数据库事务确保数据一致性
2. 生产记录与派工单数据必须匹配
3. 时间计算必须准确
4. 设备状态变更必须同步

## 4. 设备维修业务流程

### 4.1 设备报修流程
```
开始 → 选择设备 → 填写故障信息 → 检查设备状态 → 停止当前生产 → 更新设备状态 → 创建维修记录 → 通知维修人员 → 结束
```

### 4.2 设备修复流程
```
开始 → 查看维修记录 → 选择待修复设备 → 确认修复完成 → 更新维修记录 → 恢复设备状态 → 记录修复信息 → 结束
```

### 4.3 详细业务逻辑

#### 报修处理逻辑
```java
public void reportMaintenance(String deviceCode, String faultDescription) {
    // 1. 检查设备当前状态
    Device device = deviceRepository.findByDeviceCode(deviceCode);
    
    // 2. 如果设备正在生产，先停止生产
    if ("working".equals(device.getStatus())) {
        WorkOrder activeOrder = workOrderRepository.findActiveByDeviceCode(deviceCode);
        if (activeOrder != null) {
            stopProduction(activeOrder.getOrderNo());
        }
    }
    
    // 3. 更新设备状态为维修中
    device.setStatus("maintenance");
    deviceRepository.save(device);
    
    // 4. 创建维修记录
    MaintenanceRecord record = new MaintenanceRecord();
    record.setDeviceId(device.getId());
    record.setDeviceCode(deviceCode);
    record.setFaultDescription(faultDescription);
    record.setMaintenanceType("repair");
    record.setStatus("pending");
    record.setStartTime(LocalDateTime.now());
    maintenanceRepository.save(record);
    
    // 5. 记录系统日志
    logService.log("MAINTENANCE", "设备报修", deviceCode + " 报修: " + faultDescription);
}
```

#### 修复完成逻辑
```java
public void completeMaintenance(Long maintenanceId) {
    // 1. 获取维修记录
    MaintenanceRecord record = maintenanceRepository.findById(maintenanceId);
    
    // 2. 更新维修记录
    record.setStatus("completed");
    record.setEndTime(LocalDateTime.now());
    record.setMaintenanceType("fixed");
    maintenanceRepository.save(record);
    
    // 3. 恢复设备状态
    Device device = deviceRepository.findById(record.getDeviceId());
    device.setStatus("idle");
    deviceRepository.save(device);
    
    // 4. 记录系统日志
    logService.log("MAINTENANCE", "设备修复", record.getDeviceCode() + " 修复完成");
}
```

## 5. 设备状态监控流程

### 5.1 状态查询流程
```
开始 → 用户请求 → 查询所有设备 → 获取实时状态 → 关联派工信息 → 格式化显示 → 返回结果 → 结束
```

### 5.2 状态分类定义

#### 设备状态枚举
```java
public enum DeviceStatus {
    IDLE("idle", "闲置", "#28a745"),           // 绿色
    WORKING("working", "生产中", "#007bff"),    // 蓝色
    MAINTENANCE("maintenance", "维修中", "#dc3545"); // 红色
    
    private String code;
    private String displayName;
    private String color;
}
```

#### 状态查询SQL
```sql
-- 获取设备状态统计
SELECT 
    d.device_code,
    d.device_name,
    d.device_type,
    d.status,
    wo.order_no,
    wo.planned_quantity,
    wo.actual_quantity,
    wo.start_time,
    CASE 
        WHEN d.status = 'working' THEN wo.order_no
        WHEN d.status = 'maintenance' THEN '维修中'
        ELSE '闲置'
    END as status_display
FROM devices d
LEFT JOIN work_orders wo ON d.id = wo.device_id AND wo.status = 'working'
ORDER BY d.device_type, d.device_code;
```

### 5.3 实时数据更新机制

#### 定时刷新策略
```javascript
// 前端自动刷新机制
setInterval(function() {
    refreshDeviceStatus();
}, 30000); // 每30秒刷新一次

function refreshDeviceStatus() {
    fetch('/api/devices/status')
        .then(response => response.json())
        .then(data => {
            updateDeviceDisplay(data);
        })
        .catch(error => {
            console.error('状态刷新失败:', error);
        });
}
```

## 6. PLC通信业务流程

### 6.1 PLC连接管理流程
```
开始 → 获取PLC配置 → 建立TCP连接 → 验证连接 → 保持连接池 → 处理通信请求 → 异常处理 → 结束
```

### 6.2 设备点位映射

#### 点位地址映射表
```java
public class PLCAddressMapping {
    // 设备清零点位映射
    private static final Map<String, String> RESET_ADDRESS_MAP = Map.of(
        "AOI-01", "D1002",
        "AOI-02", "D1004",
        "CCM08-01", "D1006",
        "CCM08-02", "D1008",
        // ... 其他设备映射
        "CNC-16", "D1096"
    );
    
    // 设备状态点位映射（读取用）
    private static final Map<String, String> STATUS_ADDRESS_MAP = Map.of(
        "AOI-01", "D1102",
        "AOI-02", "D1104",
        // ... 对应的状态读取地址
    );
}
```

### 6.3 PLC通信协议

#### 通信时序图
```
系统 → PLC: 连接请求
PLC → 系统: 连接确认
系统 → PLC: 写入清零点位(值=1)
PLC → 系统: 写入确认
系统: 等待1秒
系统 → PLC: 读取状态点位
PLC → 系统: 返回状态值
系统: 判断状态值是否为0
如果不为0: 重复读取(最多3次)
如果为0: 通信成功
```

#### 通信异常处理
```java
public class PLCCommunicationHandler {
    private static final int MAX_RETRY_TIMES = 3;
    private static final int RETRY_INTERVAL = 1000; // 1秒
    
    public boolean verifyDeviceReady(String deviceCode) {
        String resetAddress = getResetAddress(deviceCode);
        String statusAddress = getStatusAddress(deviceCode);
        
        try {
            // 1. 写入清零点位
            writeDataPoint(resetAddress, 1);
            
            // 2. 等待1秒
            Thread.sleep(1000);
            
            // 3. 读取状态点位，最多重试3次
            for (int i = 0; i < MAX_RETRY_TIMES; i++) {
                int statusValue = readDataPoint(statusAddress);
                if (statusValue == 0) {
                    return true; // 设备就绪
                }
                Thread.sleep(RETRY_INTERVAL);
            }
            
            return false; // 设备未就绪
            
        } catch (Exception e) {
            logger.error("PLC通信异常: " + e.getMessage());
            return false;
        }
    }
}
```

## 7. 数据统计业务流程

### 7.1 生产报表生成流程
```
开始 → 接收查询条件 → 验证参数 → 查询生产数据 → 数据聚合计算 → 生成图表数据 → 格式化输出 → 返回报表 → 结束
```

### 7.2 统计维度定义

#### 时间维度统计
```sql
-- 按月统计生产数量
SELECT 
    DATE_FORMAT(start_time, '%Y-%m') as month,
    device_type,
    COUNT(*) as order_count,
    SUM(planned_quantity) as total_planned,
    SUM(actual_quantity) as total_actual,
    AVG(completion_rate) as avg_completion_rate,
    SUM(duration_minutes) as total_duration
FROM production_records
WHERE start_time >= ? AND start_time <= ?
GROUP BY DATE_FORMAT(start_time, '%Y-%m'), device_type
ORDER BY month, device_type;
```

#### 设备维度统计
```sql
-- 按设备统计生产效率
SELECT 
    device_code,
    device_type,
    COUNT(*) as production_times,
    SUM(actual_quantity) as total_production,
    AVG(completion_rate) as avg_completion_rate,
    SUM(duration_minutes) / 60.0 as total_hours,
    SUM(actual_quantity) / (SUM(duration_minutes) / 60.0) as efficiency_per_hour
FROM production_records
WHERE start_time >= ? AND start_time <= ?
GROUP BY device_code, device_type
ORDER BY efficiency_per_hour DESC;
```

### 7.3 报表数据处理

#### 数据聚合服务
```java
@Service
public class ReportService {
    
    public ProductionReport generateMonthlyReport(LocalDate startDate, LocalDate endDate, String deviceType) {
        // 1. 查询基础数据
        List<ProductionRecord> records = productionRepository.findByDateRangeAndDeviceType(
            startDate.atStartOfDay(), 
            endDate.atTime(23, 59, 59), 
            deviceType
        );
        
        // 2. 数据分组聚合
        Map<String, List<ProductionRecord>> groupedByMonth = records.stream()
            .collect(Collectors.groupingBy(
                record -> record.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            ));
        
        // 3. 计算统计指标
        List<MonthlyStatistics> monthlyStats = groupedByMonth.entrySet().stream()
            .map(entry -> calculateMonthlyStatistics(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(MonthlyStatistics::getMonth))
            .collect(Collectors.toList());
        
        // 4. 生成报表对象
        return new ProductionReport(monthlyStats, calculateSummary(records));
    }
    
    private MonthlyStatistics calculateMonthlyStatistics(String month, List<ProductionRecord> records) {
        int totalOrders = records.size();
        int totalPlanned = records.stream().mapToInt(ProductionRecord::getPlannedQuantity).sum();
        int totalActual = records.stream().mapToInt(ProductionRecord::getActualQuantity).sum();
        double avgCompletionRate = records.stream().mapToDouble(ProductionRecord::getCompletionRate).average().orElse(0.0);
        long totalDuration = records.stream().mapToLong(ProductionRecord::getDurationMinutes).sum();
        
        return new MonthlyStatistics(month, totalOrders, totalPlanned, totalActual, avgCompletionRate, totalDuration);
    }
}
```

## 8. 异常处理业务流程

### 8.1 系统异常分类

#### 业务异常
1. **派工异常**: 设备不可用、派工单重复等
2. **生产异常**: PLC通信失败、设备故障等
3. **数据异常**: 数据不一致、约束违反等

#### 技术异常
1. **网络异常**: PLC连接失败、数据库连接失败等
2. **系统异常**: 内存不足、磁盘空间不足等
3. **安全异常**: 认证失败、权限不足等

### 8.2 异常处理策略

#### 业务异常处理
```java
@ControllerAdvice
public class BusinessExceptionHandler {
    
    @ExceptionHandler(DeviceNotAvailableException.class)
    public ResponseEntity<ApiResponse> handleDeviceNotAvailable(DeviceNotAvailableException e) {
        // 记录异常日志
        logger.warn("设备不可用: {}", e.getMessage());
        
        // 返回用户友好的错误信息
        return ResponseEntity.badRequest().body(
            ApiResponse.error("DEVICE_NOT_AVAILABLE", "设备当前不可用，请选择其他设备")
        );
    }
    
    @ExceptionHandler(PLCCommunicationException.class)
    public ResponseEntity<ApiResponse> handlePLCCommunication(PLCCommunicationException e) {
        // 记录异常日志
        logger.error("PLC通信异常: {}", e.getMessage(), e);
        
        // 返回错误信息
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            ApiResponse.error("PLC_COMMUNICATION_ERROR", "设备通信失败，请检查网络连接")
        );
    }
}
```

#### 事务回滚处理
```java
@Service
@Transactional
public class WorkOrderService {
    
    public void createWorkOrder(WorkOrderRequest request) {
        try {
            // 1. 验证设备可用性
            validateDeviceAvailability(request.getDeviceCode());
            
            // 2. PLC通信验证
            if (!plcService.verifyDeviceReady(request.getDeviceCode())) {
                throw new PLCCommunicationException("设备PLC通信验证失败");
            }
            
            // 3. 创建派工单
            WorkOrder workOrder = createWorkOrderEntity(request);
            workOrderRepository.save(workOrder);
            
            // 4. 更新设备状态
            updateDeviceStatus(request.getDeviceCode(), DeviceStatus.WORKING);
            
            // 5. 记录操作日志
            logService.log("WORK_ORDER", "创建派工单", workOrder.getOrderNo());
            
        } catch (Exception e) {
            // 事务自动回滚
            logger.error("创建派工单失败: {}", e.getMessage(), e);
            throw e;
        }
    }
}
```

## 9. 安全控制业务流程

### 9.1 用户认证流程
```
开始 → 用户输入凭据 → 验证用户名密码 → 生成JWT Token → 设置会话 → 返回认证结果 → 结束
```

### 9.2 权限控制流程
```
请求到达 → 提取Token → 验证Token有效性 → 解析用户信息 → 检查操作权限 → 允许/拒绝访问 → 记录访问日志
```

### 9.3 安全策略实现

#### JWT认证实现
```java
@Service
public class AuthenticationService {
    
    public AuthenticationResponse authenticate(LoginRequest request) {
        // 1. 验证用户凭据
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new AuthenticationException("用户不存在"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("密码错误");
        }
        
        // 2. 检查用户状态
        if (user.getStatus() != 1) {
            throw new AuthenticationException("用户已被禁用");
        }
        
        // 3. 生成JWT Token
        String token = jwtTokenProvider.generateToken(user);
        
        // 4. 记录登录日志
        logService.log("AUTH", "用户登录", user.getUsername());
        
        return new AuthenticationResponse(token, user.getRole());
    }
}
```

#### 操作权限控制
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
@PostMapping("/work-orders")
public ResponseEntity<ApiResponse> createWorkOrder(@RequestBody WorkOrderRequest request) {
    // 业务逻辑处理
}

@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/reports")
public ResponseEntity<ApiResponse> generateReport(@RequestParam Map<String, Object> params) {
    // 报表生成逻辑
}
```

## 10. 数据一致性保证

### 10.1 事务管理策略

#### 分布式事务处理
```java
@Service
public class ProductionTransactionService {
    
    @Transactional(rollbackFor = Exception.class)
    public void stopProductionWithTransaction(String orderNo) {
        try {
            // 1. 查询派工单
            WorkOrder workOrder = workOrderRepository.findByOrderNo(orderNo);
            
            // 2. 计算生产数据
            ProductionSummary summary = calculateProductionSummary(workOrder);
            
            // 3. 创建生产记录
            ProductionRecord record = createProductionRecord(workOrder, summary);
            productionRepository.save(record);
            
            // 4. 更新派工单状态
            workOrder.setStatus(WorkOrderStatus.COMPLETED);
            workOrder.setEndTime(LocalDateTime.now());
            workOrderRepository.save(workOrder);
            
            // 5. 释放设备
            Device device = deviceRepository.findById(workOrder.getDeviceId());
            device.setStatus(DeviceStatus.IDLE);
            deviceRepository.save(device);
            
            // 6. 记录操作日志
            logService.log("PRODUCTION", "停止生产", orderNo);
            
        } catch (Exception e) {
            logger.error("停止生产事务失败: {}", e.getMessage(), e);
            throw new TransactionException("停止生产失败: " + e.getMessage());
        }
    }
}
```

### 10.2 数据校验规则

#### 业务数据校验
```java
@Component
public class DataValidationService {
    
    public void validateWorkOrderData(WorkOrderRequest request) {
        // 1. 派工单号唯一性校验
        if (workOrderRepository.existsByOrderNo(request.getOrderNo())) {
            throw new ValidationException("派工单号已存在");
        }
        
        // 2. 生产数量校验
        if (request.getPlannedQuantity() <= 0) {
            throw new ValidationException("生产数量必须大于0");
        }
        
        // 3. 设备状态校验
        Device device = deviceRepository.findByDeviceCode(request.getDeviceCode());
        if (!DeviceStatus.IDLE.equals(device.getStatus())) {
            throw new ValidationException("设备当前不可用");
        }
        
        // 4. 设备类型匹配校验
        if (!isValidDeviceTypeMatch(request.getDeviceType(), request.getDeviceCode())) {
            throw new ValidationException("设备类型与设备编号不匹配");
        }
    }
}
```

---

**版本信息**：v1.0  
**更新日期**：2025-01-08  
**适用范围**：移动端派工系统业务分析和开发参考