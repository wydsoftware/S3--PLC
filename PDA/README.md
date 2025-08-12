# 移动端派工系统

## 项目概述

本项目是一个基于Java后端和移动端前端的派工管理系统，用于管理工厂设备的生产派工、状态监控和维修管理。

## 技术栈

### 后端
- Java (Spring Boot)
- MySQL 数据库
- TCP/IP 通信 (与PLC通信)

### 前端
- 移动端Web应用
- 工业风格UI设计
- 响应式布局

## 系统架构

```
移动端 (HTML5/CSS3/JavaScript)
    ↓ HTTP/HTTPS
后端服务 (Spring Boot)
    ↓ JDBC
MySQL 数据库
    ↓ TCP/IP
PLC 设备
```

## 功能模块

### 移动端功能
1. 设备派工
2. 设备停止生产
3. 设备报修
4. 设备状态查询
5. PLC IP设定

### 后台管理功能
1. 角色管理
2. 生产记录查询
3. 报表管理

## 项目结构

```
PDA/
├── docs/                    # 文档目录
│   ├── requirements.md      # 需求文档
│   ├── database-design.md   # 数据库设计
│   ├── api-design.md        # 接口设计
│   ├── user-manual.md       # 操作手册
│   └── deployment.md        # 部署文档
├── backend/                 # 后端代码
│   ├── src/
│   ├── pom.xml
│   └── application.yml
├── frontend/                # 前端代码
│   ├── mobile/              # 移动端
│   └── admin/               # 后台管理
└── database/                # 数据库脚本
    ├── schema.sql
    └── init-data.sql
```

## 开发环境要求

- JDK 8+
- MySQL 5.7+
- Maven 3.6+
- 现代浏览器支持

## 快速开始

详细的安装和配置说明请参考 `docs/deployment.md`