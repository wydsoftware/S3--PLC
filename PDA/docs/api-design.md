# API接口设计文档

## 1. 接口概述

本系统采用RESTful API设计风格，所有接口返回JSON格式数据。

### 1.1 基础信息
- 基础URL: `http://localhost:8080/api`
- 数据格式: JSON
- 字符编码: UTF-8
- 请求方式: GET, POST, PUT, DELETE

### 1.2 统一响应格式

```json
{
    "code": 200,
    "message": "success",
    "data": {},
    "timestamp": "2025-01-08T10:30:00"
}
```

### 1.3 状态码说明
- 200: 成功
- 400: 请求参数错误
- 401: 未授权
- 403: 禁止访问
- 404: 资源不存在
- 500: 服务器内部错误

## 2. 移动端API接口

### 2.1 用户认证接口

#### 2.1.1 用户登录

**接口地址:** `POST /auth/login`

**请求参数:**
```json
{
    "username": "admin",
    "password": "123456"
}
```

**响应数据:**
```json
{
    "code": 200,
    "message": "登录成功",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "user": {
            "id": 1,
            "username": "admin",
            "role": "admin"
        }
    },
    "timestamp": "2025-01-08T10:30:00"
}
```

#### 2.1.2 用户登出

**接口地址:** `POST /auth/logout`

**请求头:** `Authorization: Bearer {token}`

**响应数据:**
```json
{
    "code": 200,
    "message": "登出成功",
    "data": null,
    "timestamp": "2025-01-08T10:30:00"
}
```

### 2.2 设备管理接口

#### 2.2.1 获取设备列表

**接口地址:** `GET /devices`

**请求参数:**
- `type` (可选): 设备类型 (AOI/CNC/CCM08/CCM23)
- `status` (可选): 设备状态 (idle/working/maintenance)

**响应数据:**
```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "id": 1,
            "deviceCode": "CNC-01",
            "deviceName": "CNC数控设备01",
            "deviceType": "CNC",
            "status": "idle",
            "currentWorkOrder": null
        }
    ],
    "timestamp": "2025-01-08T10:30:00"
}
```

#### 2.2.2 获取设备类型列表

**接口地址:** `GET /devices/types`

**响应数据:**
```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "type": "CNC",
            "devices": ["CNC-01", "CNC-02", "...", "CNC-16"]
        },
        {
            "type": "CCM08",
            "devices": ["CCM08-01", "CCM08-02", "...", "CCM08-24"]
        },
        {
            "type": "CCM23",
            "devices": ["CCM23-01", "CCM23-02", "...", "CCM23-06"]
        }
    ],
    "timestamp": "2025-01-08T10:30:00"
}
```

### 2.3 派工管理接口

#### 2.3.1 创建派工单

**接口地址:** `POST /work-orders`

**请求参数:**
```json
{
    "orderNo": "WO20250108001",
    "deviceCode": "CNC-01",
    "plannedQuantity": 1000
}
```

**响应数据:**
```json
{
    "code": 200,
    "message": "派工单创建成功",
    "data": {
        "id": 1,
        "orderNo": "WO20250108001",
        "deviceCode": "CNC-01",
        "plannedQuantity": 1000,
        "status": "pending",
        "createdTime": "2025-01-08T10:30:00"
    },
    "timestamp": "2025-01-08T10:30:00"
}
```

#### 2.3.2 开始生产

**接口地址:** `POST /work-orders/{id}/start`

**响应数据:**
```json
{
    "code": 200,
    "message": "生产开始成功",
    "data": {
        "id": 1,
        "orderNo": "WO20250108001",
        "deviceCode": "CNC-01",
        "status": "working",
        "startTime": "2025-01-08T10:30:00"
    },
    "timestamp": "2025-01-08T10:30:00"
}
```

#### 2.3.3 停止生产

**接口地址:** `POST /work-orders/{id}/stop`

**请求参数:**
```json
{
    "actualQuantity": 850
}
```

**响应数据:**
```json
{
    "code": 200,
    "message": "生产停止成功",
    "data": {
        "id": 1,
        "orderNo": "WO20250108001",
        "deviceCode": "CNC-01",
        "status": "stopped",
        "endTime": "2025-01-08T18:30:00",
        "actualQuantity": 850,
        "completionRate": 85.0
    },
    "timestamp": "2025-01-08T18:30:00"
}
```

#### 2.3.4 获取正在生产的派工单

**接口地址:** `GET /work-orders/working`

**响应数据:**
```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "id": 1,
            "orderNo": "WO20250108001",
            "deviceCode": "CNC-01",
            "deviceName": "CNC数控设备01",
            "plannedQuantity": 1000,
            "actualQuantity": 650,
            "completionRate": 65.0,
            "startTime": "2025-01-08T10:30:00",
            "duration": "8小时15分钟"
        }
    ],
    "timestamp": "2025-01-08T18:45:00"
}
```

### 2.4 设备维修接口

#### 2.4.1 设备报修

**接口地址:** `POST /maintenance`

**请求参数:**
```json
{
    "deviceCode": "CNC-01",
    "faultDescription": "设备异常停机，需要检修"
}
```

**响应数据:**
```json
{
    "code": 200,
    "message": "报修成功",
    "data": {
        "id": 1,
        "deviceCode": "CNC-01",
        "faultDescription": "设备异常停机，需要检修",
        "status": "pending",
        "startTime": "2025-01-08T10:30:00"
    },
    "timestamp": "2025-01-08T10:30:00"
}
```

#### 2.4.2 设备修复完成

**接口地址:** `POST /maintenance/{id}/complete`

**响应数据:**
```json
{
    "code": 200,
    "message": "设备修复完成",
    "data": {
        "id": 1,
        "deviceCode": "CNC-01",
        "status": "completed",
        "endTime": "2025-01-08T14:30:00"
    },
    "timestamp": "2025-01-08T14:30:00"
}
```

### 2.5 PLC配置接口

#### 2.5.1 获取PLC配置

**接口地址:** `GET /plc-config`

**响应数据:**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "plcIp": "192.168.1.100",
        "plcPort": 502,
        "timeoutSeconds": 5
    },
    "timestamp": "2025-01-08T10:30:00"
}
```

#### 2.5.2 更新PLC配置

**接口地址:** `PUT /plc-config`

**请求参数:**
```json
{
    "plcIp": "192.168.1.100",
    "plcPort": 502,
    "timeoutSeconds": 5
}
```

**响应数据:**
```json
{
    "code": 200,
    "message": "PLC配置更新成功",
    "data": {
        "id": 1,
        "plcIp": "192.168.1.100",
        "plcPort": 502,
        "timeoutSeconds": 5
    },
    "timestamp": "2025-01-08T10:30:00"
}
```

#### 2.5.3 测试PLC连接

**接口地址:** `POST /plc-config/test`

**响应数据:**
```json
{
    "code": 200,
    "message": "PLC连接测试成功",
    "data": {
        "connected": true,
        "responseTime": 150
    },
    "timestamp": "2025-01-08T10:30:00"
}
```

## 3. 后台管理API接口

### 3.1 用户管理接口

#### 3.1.1 获取用户列表

**接口地址:** `GET /admin/users`

**请求参数:**
- `page` (可选): 页码，默认1
- `size` (可选): 每页大小，默认10

**响应数据:**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "content": [
            {
                "id": 1,
                "username": "admin",
                "role": "admin",
                "status": 1,
                "createdTime": "2025-01-08T10:30:00"
            }
        ],
        "totalElements": 1,
        "totalPages": 1,
        "currentPage": 1
    },
    "timestamp": "2025-01-08T10:30:00"
}
```

#### 3.1.2 创建用户

**接口地址:** `POST /admin/users`

**请求参数:**
```json
{
    "username": "user001",
    "password": "123456",
    "role": "user"
}
```

**响应数据:**
```json
{
    "code": 200,
    "message": "用户创建成功",
    "data": {
        "id": 2,
        "username": "user001",
        "role": "user",
        "status": 1,
        "createdTime": "2025-01-08T10:30:00"
    },
    "timestamp": "2025-01-08T10:30:00"
}
```

### 3.2 生产记录查询接口

#### 3.2.1 查询生产记录

**接口地址:** `GET /admin/production-records`

**请求参数:**
- `startDate` (可选): 开始日期 (yyyy-MM-dd)
- `endDate` (可选): 结束日期 (yyyy-MM-dd)
- `orderNo` (可选): 派工单号
- `deviceCode` (可选): 设备编号
- `page` (可选): 页码，默认1
- `size` (可选): 每页大小，默认10

**响应数据:**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "content": [
            {
                "id": 1,
                "orderNo": "WO20250108001",
                "deviceCode": "CNC-01",
                "deviceType": "CNC",
                "plannedQuantity": 1000,
                "actualQuantity": 850,
                "completionRate": 85.0,
                "startTime": "2025-01-08T10:30:00",
                "endTime": "2025-01-08T18:30:00",
                "durationMinutes": 480
            }
        ],
        "totalElements": 1,
        "totalPages": 1,
        "currentPage": 1
    },
    "timestamp": "2025-01-08T10:30:00"
}
```

### 3.3 报表统计接口

#### 3.3.1 月度生产统计

**接口地址:** `GET /admin/reports/monthly`

**请求参数:**
- `year`: 年份
- `month`: 月份
- `deviceType` (可选): 设备类型
- `deviceCode` (可选): 设备编号

**响应数据:**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "period": "2025-01",
        "totalProduction": 25000,
        "deviceStats": [
            {
                "deviceCode": "CNC-01",
                "deviceType": "CNC",
                "totalQuantity": 5000,
                "workingHours": 160,
                "utilizationRate": 80.0
            }
        ]
    },
    "timestamp": "2025-01-08T10:30:00"
}
```

#### 3.3.2 设备利用率统计

**接口地址:** `GET /admin/reports/utilization`

**请求参数:**
- `startDate`: 开始日期
- `endDate`: 结束日期
- `deviceType` (可选): 设备类型

**响应数据:**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "period": "2025-01-01 ~ 2025-01-31",
        "overallUtilization": 75.5,
        "deviceUtilization": [
            {
                "deviceCode": "CNC-01",
                "utilizationRate": 80.0,
                "workingHours": 160,
                "totalHours": 200
            }
        ]
    },
    "timestamp": "2025-01-08T10:30:00"
}
```

## 4. PLC通信接口

### 4.1 PLC数据写入

**内部接口:** `POST /plc/write`

**请求参数:**
```json
{
    "deviceCode": "CNC-01",
    "address": "D1066",
    "value": 1
}
```

### 4.2 PLC数据读取

**内部接口:** `GET /plc/read`

**请求参数:**
- `deviceCode`: 设备编号
- `address`: PLC地址

**响应数据:**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "deviceCode": "CNC-01",
        "address": "D1066",
        "value": 0
    },
    "timestamp": "2025-01-08T10:30:00"
}
```

## 5. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 1001 | 用户名或密码错误 |
| 1002 | 用户已被禁用 |
| 1003 | Token无效或已过期 |
| 2001 | 设备不存在 |
| 2002 | 设备正在维修中 |
| 2003 | 设备已被占用 |
| 3001 | 派工单号已存在 |
| 3002 | 派工单不存在 |
| 3003 | 派工单状态不允许此操作 |
| 4001 | PLC连接失败 |
| 4002 | PLC通信超时 |
| 4003 | PLC数据写入失败 |
| 5001 | 参数验证失败 |
| 5002 | 数据库操作失败 |