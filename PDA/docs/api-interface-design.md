# 移动端派工系统API接口设计

## 接口概述

### 基础信息
- **基础URL**: `http://localhost:8080/api`
- **数据格式**: JSON
- **字符编码**: UTF-8
- **认证方式**: JWT Token

### 统一响应格式
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {},
    "timestamp": "2024-01-01T12:00:00Z"
}
```

### 状态码说明
- `200`: 成功
- `400`: 请求参数错误
- `401`: 未授权
- `403`: 权限不足
- `404`: 资源不存在
- `500`: 服务器内部错误

## 1. 认证接口

### 1.1 用户登录
**接口地址**: `POST /auth/login`

**请求参数**:
```json
{
    "username": "admin",
    "password": "123456"
}
```

**响应数据**:
```json
{
    "code": 200,
    "message": "登录成功",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "user": {
            "id": 1,
            "username": "admin",
            "realName": "系统管理员",
            "role": "admin"
        }
    }
}
```

### 1.2 用户登出
**接口地址**: `POST /auth/logout`

**请求头**:
```
Authorization: Bearer {token}
```

**响应数据**:
```json
{
    "code": 200,
    "message": "登出成功"
}
```

### 1.3 刷新Token
**接口地址**: `POST /auth/refresh`

**请求头**:
```
Authorization: Bearer {token}
```

**响应数据**:
```json
{
    "code": 200,
    "message": "Token刷新成功",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
}
```

## 2. 设备管理接口

### 2.1 获取设备列表
**接口地址**: `GET /equipment/list`

**请求参数**:
```
equipmentType: string (可选) - 设备类型 (AOI/CNC/CCM08/CCM23)
status: string (可选) - 设备状态 (idle/running/maintenance/fault)
page: int (可选) - 页码，默认1
size: int (可选) - 每页大小，默认20
```

**响应数据**:
```json
{
    "code": 200,
    "message": "获取成功",
    "data": {
        "list": [
            {
                "id": 1,
                "equipmentCode": "CNC-01",
                "equipmentName": "CNC加工中心01",
                "equipmentType": "CNC",
                "status": "idle",
                "plcAddress": "D1066",
                "location": "车间A区",
                "currentWorkOrder": null
            }
        ],
        "total": 48,
        "page": 1,
        "size": 20
    }
}
```

### 2.2 根据设备类型获取设备
**接口地址**: `GET /equipment/by-type/{equipmentType}`

**路径参数**:
- `equipmentType`: 设备类型 (AOI/CNC/CCM08/CCM23)

**响应数据**:
```json
{
    "code": 200,
    "message": "获取成功",
    "data": [
        {
            "id": 1,
            "equipmentCode": "CNC-01",
            "equipmentName": "CNC加工中心01",
            "status": "idle",
            "plcAddress": "D1066"
        }
    ]
}
```

### 2.3 获取设备详情
**接口地址**: `GET /equipment/{id}`

**路径参数**:
- `id`: 设备ID

**响应数据**:
```json
{
    "code": 200,
    "message": "获取成功",
    "data": {
        "id": 1,
        "equipmentCode": "CNC-01",
        "equipmentName": "CNC加工中心01",
        "equipmentType": "CNC",
        "status": "idle",
        "plcAddress": "D1066",
        "location": "车间A区",
        "specifications": "规格说明",
        "currentWorkOrder": null,
        "lastMaintenanceDate": "2024-01-01"
    }
}
```

### 2.4 更新设备状态
**接口地址**: `PUT /equipment/{id}/status`

**路径参数**:
- `id`: 设备ID

**请求参数**:
```json
{
    "status": "maintenance"
}
```

**响应数据**:
```json
{
    "code": 200,
    "message": "状态更新成功"
}
```

## 3. 派工单管理接口

### 3.1 创建派工单
**接口地址**: `POST /work-orders`

**请求参数**:
```json
{
    "workOrderNo": "WO20240101001",
    "equipmentId": 1,
    "plannedQuantity": 1000
}
```

**响应数据**:
```json
{
    "code": 200,
    "message": "派工单创建成功",
    "data": {
        "id": 1,
        "workOrderNo": "WO20240101001",
        "equipmentCode": "CNC-01",
        "plannedQuantity": 1000,
        "status": "pending",
        "createdAt": "2024-01-01T12:00:00Z"
    }
}
```

### 3.2 开始生产
**接口地址**: `POST /work-orders/{id}/start`

**路径参数**:
- `id`: 派工单ID

**响应数据**:
```json
{
    "code": 200,
    "message": "生产开始成功",
    "data": {
        "id": 1,
        "workOrderNo": "WO20240101001",
        "status": "running",
        "startTime": "2024-01-01T12:00:00Z"
    }
}
```

### 3.3 停止生产
**接口地址**: `POST /work-orders/{id}/stop`

**路径参数**:
- `id`: 派工单ID

**请求参数**:
```json
{
    "actualQuantity": 800,
    "notes": "提前停止原因"
}
```

**响应数据**:
```json
{
    "code": 200,
    "message": "生产停止成功",
    "data": {
        "id": 1,
        "workOrderNo": "WO20240101001",
        "status": "completed",
        "endTime": "2024-01-01T18:00:00Z",
        "actualQuantity": 800,
        "completionRate": 80.0
    }
}
```

### 3.4 获取正在生产的派工单
**接口地址**: `GET /work-orders/running`

**响应数据**:
```json
{
    "code": 200,
    "message": "获取成功",
    "data": [
        {
            "id": 1,
            "workOrderNo": "WO20240101001",
            "equipmentCode": "CNC-01",
            "equipmentName": "CNC加工中心01",
            "plannedQuantity": 1000,
            "actualQuantity": 600,
            "completionRate": 60.0,
            "startTime": "2024-01-01T12:00:00Z",
            "duration": "6小时30分钟"
        }
    ]
}
```

### 3.5 派工单查询
**接口地址**: `GET /work-orders`

**请求参数**:
```
workOrderNo: string (可选) - 派工单号
equipmentCode: string (可选) - 设备编号
status: string (可选) - 状态
startDate: string (可选) - 开始日期
endDate: string (可选) - 结束日期
page: int (可选) - 页码
size: int (可选) - 每页大小
```

**响应数据**:
```json
{
    "code": 200,
    "message": "查询成功",
    "data": {
        "list": [
            {
                "id": 1,
                "workOrderNo": "WO20240101001",
                "equipmentCode": "CNC-01",
                "plannedQuantity": 1000,
                "actualQuantity": 800,
                "status": "completed",
                "startTime": "2024-01-01T12:00:00Z",
                "endTime": "2024-01-01T18:00:00Z",
                "completionRate": 80.0
            }
        ],
        "total": 100,
        "page": 1,
        "size": 20
    }
}
```

## 4. 维修管理接口

### 4.1 设备报修
**接口地址**: `POST /maintenance`

**请求参数**:
```json
{
    "equipmentId": 1,
    "faultDescription": "设备异常噪音",
    "maintenanceType": "corrective",
    "priority": "medium"
}
```

**响应数据**:
```json
{
    "code": 200,
    "message": "报修成功",
    "data": {
        "id": 1,
        "maintenanceNo": "MR20240101001",
        "equipmentCode": "CNC-01",
        "status": "pending",
        "createdAt": "2024-01-01T12:00:00Z"
    }
}
```

### 4.2 开始维修
**接口地址**: `POST /maintenance/{id}/start`

**路径参数**:
- `id`: 维修记录ID

**请求参数**:
```json
{
    "assignedTo": 2
}
```

**响应数据**:
```json
{
    "code": 200,
    "message": "维修开始",
    "data": {
        "id": 1,
        "status": "in_progress",
        "startTime": "2024-01-01T14:00:00Z"
    }
}
```

### 4.3 完成维修
**接口地址**: `POST /maintenance/{id}/complete`

**路径参数**:
- `id`: 维修记录ID

**请求参数**:
```json
{
    "solutionDescription": "更换轴承",
    "partsUsed": "轴承x2",
    "maintenanceCost": 500.00
}
```

**响应数据**:
```json
{
    "code": 200,
    "message": "维修完成",
    "data": {
        "id": 1,
        "status": "completed",
        "endTime": "2024-01-01T16:00:00Z",
        "durationMinutes": 120
    }
}
```

### 4.4 获取维修记录
**接口地址**: `GET /maintenance`

**请求参数**:
```
equipmentCode: string (可选) - 设备编号
status: string (可选) - 维修状态
startDate: string (可选) - 开始日期
endDate: string (可选) - 结束日期
page: int (可选) - 页码
size: int (可选) - 每页大小
```

**响应数据**:
```json
{
    "code": 200,
    "message": "获取成功",
    "data": {
        "list": [
            {
                "id": 1,
                "maintenanceNo": "MR20240101001",
                "equipmentCode": "CNC-01",
                "faultDescription": "设备异常噪音",
                "status": "completed",
                "priority": "medium",
                "reportedBy": "操作员A",
                "assignedTo": "维修员B",
                "startTime": "2024-01-01T14:00:00Z",
                "endTime": "2024-01-01T16:00:00Z",
                "durationMinutes": 120
            }
        ],
        "total": 50,
        "page": 1,
        "size": 20
    }
}
```

## 5. PLC通信接口

### 5.1 PLC连接测试
**接口地址**: `POST /plc/test-connection`

**请求参数**:
```json
{
    "ipAddress": "192.168.1.100",
    "port": 502
}
```

**响应数据**:
```json
{
    "code": 200,
    "message": "连接成功",
    "data": {
        "connected": true,
        "responseTime": 50
    }
}
```

### 5.2 设备清零操作
**接口地址**: `POST /plc/clear-equipment`

**请求参数**:
```json
{
    "equipmentCode": "CNC-01",
    "plcAddress": "D1066"
}
```

**响应数据**:
```json
{
    "code": 200,
    "message": "清零操作成功",
    "data": {
        "equipmentCode": "CNC-01",
        "cleared": true,
        "verificationPassed": true
    }
}
```

### 5.3 读取设备数据
**接口地址**: `GET /plc/equipment-data/{equipmentCode}`

**路径参数**:
- `equipmentCode`: 设备编号

**响应数据**:
```json
{
    "code": 200,
    "message": "读取成功",
    "data": {
        "equipmentCode": "CNC-01",
        "currentValue": 0,
        "isCleared": true,
        "lastUpdateTime": "2024-01-01T12:00:00Z"
    }
}
```

## 6. 系统配置接口

### 6.1 获取PLC配置
**接口地址**: `GET /config/plc`

**响应数据**:
```json
{
    "code": 200,
    "message": "获取成功",
    "data": {
        "ipAddress": "192.168.1.100",
        "port": 502,
        "timeout": 5000,
        "retryCount": 3
    }
}
```

### 6.2 更新PLC配置
**接口地址**: `PUT /config/plc`

**请求参数**:
```json
{
    "ipAddress": "192.168.1.101",
    "port": 502,
    "timeout": 5000,
    "retryCount": 3
}
```

**响应数据**:
```json
{
    "code": 200,
    "message": "配置更新成功"
}
```

### 6.3 获取系统配置
**接口地址**: `GET /config/system`

**响应数据**:
```json
{
    "code": 200,
    "message": "获取成功",
    "data": {
        "workOrderPrefix": "WO",
        "maintenancePrefix": "MR",
        "systemName": "移动端派工系统",
        "version": "1.0.0"
    }
}
```

## 7. 统计报表接口

### 7.1 生产统计
**接口地址**: `GET /statistics/production`

**请求参数**:
```
startDate: string - 开始日期 (YYYY-MM-DD)
endDate: string - 结束日期 (YYYY-MM-DD)
equipmentType: string (可选) - 设备类型
equipmentCode: string (可选) - 设备编号
```

**响应数据**:
```json
{
    "code": 200,
    "message": "获取成功",
    "data": {
        "totalWorkOrders": 100,
        "completedWorkOrders": 85,
        "totalPlannedQuantity": 50000,
        "totalActualQuantity": 42500,
        "averageCompletionRate": 85.0,
        "equipmentUtilization": 78.5,
        "dailyStatistics": [
            {
                "date": "2024-01-01",
                "workOrders": 10,
                "plannedQuantity": 5000,
                "actualQuantity": 4200,
                "completionRate": 84.0
            }
        ]
    }
}
```

### 7.2 设备效率统计
**接口地址**: `GET /statistics/equipment-efficiency`

**请求参数**:
```
startDate: string - 开始日期
endDate: string - 结束日期
equipmentType: string (可选) - 设备类型
```

**响应数据**:
```json
{
    "code": 200,
    "message": "获取成功",
    "data": [
        {
            "equipmentCode": "CNC-01",
            "equipmentName": "CNC加工中心01",
            "totalWorkOrders": 15,
            "completedWorkOrders": 13,
            "totalRuntime": 480,
            "utilizationRate": 85.7,
            "efficiencyRate": 92.3,
            "averageCompletionRate": 88.5
        }
    ]
}
```

### 7.3 维修统计
**接口地址**: `GET /statistics/maintenance`

**请求参数**:
```
startDate: string - 开始日期
endDate: string - 结束日期
equipmentType: string (可选) - 设备类型
```

**响应数据**:
```json
{
    "code": 200,
    "message": "获取成功",
    "data": {
        "totalMaintenanceRecords": 25,
        "completedMaintenanceRecords": 20,
        "averageMaintenanceTime": 120,
        "totalMaintenanceCost": 15000.00,
        "equipmentFaultRate": {
            "CNC": 12.5,
            "CCM08": 8.3,
            "CCM23": 16.7,
            "AOI": 5.0
        },
        "monthlyTrend": [
            {
                "month": "2024-01",
                "maintenanceCount": 8,
                "averageDuration": 115,
                "totalCost": 6000.00
            }
        ]
    }
}
```

## 8. 用户管理接口 (后台Web)

### 8.1 获取用户列表
**接口地址**: `GET /users`

**请求参数**:
```
username: string (可选) - 用户名
role: string (可选) - 角色
status: string (可选) - 状态
page: int (可选) - 页码
size: int (可选) - 每页大小
```

**响应数据**:
```json
{
    "code": 200,
    "message": "获取成功",
    "data": {
        "list": [
            {
                "id": 1,
                "username": "admin",
                "realName": "系统管理员",
                "role": "admin",
                "status": "active",
                "createdAt": "2024-01-01T12:00:00Z",
                "lastLoginAt": "2024-01-01T12:00:00Z"
            }
        ],
        "total": 10,
        "page": 1,
        "size": 20
    }
}
```

### 8.2 创建用户
**接口地址**: `POST /users`

**请求参数**:
```json
{
    "username": "operator01",
    "password": "123456",
    "realName": "操作员01",
    "phone": "13800138000",
    "email": "operator01@example.com",
    "role": "operator"
}
```

**响应数据**:
```json
{
    "code": 200,
    "message": "用户创建成功",
    "data": {
        "id": 2,
        "username": "operator01",
        "realName": "操作员01",
        "role": "operator",
        "status": "active"
    }
}
```

### 8.3 更新用户
**接口地址**: `PUT /users/{id}`

**路径参数**:
- `id`: 用户ID

**请求参数**:
```json
{
    "realName": "操作员01-更新",
    "phone": "13800138001",
    "email": "operator01_new@example.com",
    "role": "operator",
    "status": "active"
}
```

**响应数据**:
```json
{
    "code": 200,
    "message": "用户更新成功"
}
```

### 8.4 删除用户
**接口地址**: `DELETE /users/{id}`

**路径参数**:
- `id`: 用户ID

**响应数据**:
```json
{
    "code": 200,
    "message": "用户删除成功"
}
```

### 8.5 重置用户密码
**接口地址**: `POST /users/{id}/reset-password`

**路径参数**:
- `id`: 用户ID

**请求参数**:
```json
{
    "newPassword": "123456"
}
```

**响应数据**:
```json
{
    "code": 200,
    "message": "密码重置成功"
}
```

## 9. 错误处理

### 9.1 参数验证错误
```json
{
    "code": 400,
    "message": "参数验证失败",
    "data": {
        "errors": [
            {
                "field": "username",
                "message": "用户名不能为空"
            },
            {
                "field": "password",
                "message": "密码长度不能少于6位"
            }
        ]
    }
}
```

### 9.2 业务逻辑错误
```json
{
    "code": 400,
    "message": "设备正在维修中，无法安排生产",
    "data": {
        "equipmentCode": "CNC-01",
        "currentStatus": "maintenance"
    }
}
```

### 9.3 PLC通信错误
```json
{
    "code": 500,
    "message": "PLC通信失败",
    "data": {
        "equipmentCode": "CNC-01",
        "plcAddress": "D1066",
        "errorDetail": "连接超时",
        "retryCount": 3
    }
}
```

## 10. 接口安全

### 10.1 JWT Token格式
```
Header: {
    "alg": "HS256",
    "typ": "JWT"
}

Payload: {
    "sub": "1",
    "username": "admin",
    "role": "admin",
    "iat": 1640995200,
    "exp": 1641081600
}
```

### 10.2 权限控制
- **admin**: 所有接口访问权限
- **operator**: 设备操作、派工单管理、维修管理权限
- **viewer**: 只读权限，查询接口访问权限

### 10.3 接口限流
- 登录接口: 每分钟最多5次尝试
- PLC通信接口: 每秒最多10次请求
- 其他接口: 每秒最多100次请求

## 11. 接口测试

### 11.1 Postman集合
提供完整的Postman测试集合，包含所有接口的示例请求。

### 11.2 自动化测试
使用JUnit和MockMvc进行接口自动化测试，覆盖率达到90%以上。

### 11.3 性能测试
使用JMeter进行性能测试，确保接口响应时间在100ms以内。