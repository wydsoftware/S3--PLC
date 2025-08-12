# 移动端派工系统环境配置指南

## 文档概述

本文档详细描述移动端派工系统的环境配置要求，包括开发环境、测试环境和生产环境的搭建步骤，以及系统部署和配置的详细说明。

## 系统架构概述

```
┌─────────────────────────────────────────────────────────┐
│                    系统架构图                           │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────┐    HTTP/HTTPS    ┌─────────────────┐   │
│  │   手机端    │ ←──────────────→ │   后端服务器    │   │
│  │  (Android/  │                  │   (Spring Boot) │   │
│  │   iOS App)  │                  │                 │   │
│  └─────────────┘                  └─────────────────┘   │
│                                             │           │
│  ┌─────────────┐    HTTP/HTTPS              │           │
│  │  Web管理端  │ ←──────────────────────────┘           │
│  │  (浏览器)   │                                        │
│  └─────────────┘                                        │
│                                             │           │
│                                    ┌─────────────────┐   │
│                                    │   MySQL数据库   │   │
│                                    │                 │   │
│                                    └─────────────────┘   │
│                                             │           │
│  ┌─────────────┐      TCP/IP                │           │
│  │  PLC设备    │ ←──────────────────────────┘           │
│  │  (Modbus)   │                                        │
│  └─────────────┘                                        │
└─────────────────────────────────────────────────────────┘
```

## 第一部分：环境要求

### 1. 硬件要求

#### 1.1 服务器硬件要求

**最低配置**:
- **CPU**: 2核心 2.0GHz
- **内存**: 4GB RAM
- **存储**: 100GB 可用空间
- **网络**: 100Mbps 网络接口

**推荐配置**:
- **CPU**: 4核心 2.4GHz 或更高
- **内存**: 8GB RAM 或更高
- **存储**: 500GB SSD 或更高
- **网络**: 1Gbps 网络接口
- **备份**: 独立备份存储设备

**生产环境配置**:
- **CPU**: 8核心 3.0GHz 或更高
- **内存**: 16GB RAM 或更高
- **存储**: 1TB SSD RAID配置
- **网络**: 双网卡冗余配置
- **UPS**: 不间断电源保护

#### 1.2 网络要求

**IP地址规划**:
- **服务器网段**: 192.168.1.0/24
  - 应用服务器: 192.168.1.10
  - 数据库服务器: 192.168.1.11
  - 备份服务器: 192.168.1.12
- **办公网段**: 192.168.10.0/24
  - 管理电脑: 192.168.10.100-199
- **生产网段**: 192.168.100.0/24
  - PLC设备: 192.168.100.1-48
  - 工业交换机: 192.168.100.254

**端口要求**:
- **HTTP**: 80 (可选)
- **HTTPS**: 443 (推荐)
- **MySQL**: 3306
- **Modbus TCP**: 502
- **SSH**: 22 (管理用)
- **应用端口**: 8080 (可配置)

### 2. 软件要求

#### 2.1 操作系统要求

**服务器操作系统**:
- **推荐**: Ubuntu 20.04 LTS 或 CentOS 8
- **支持**: Windows Server 2019/2022
- **最低**: Ubuntu 18.04 或 CentOS 7

**客户端要求**:
- **手机端**: Android 6.0+ 或 iOS 10.0+
- **Web端**: Chrome 70+, Firefox 65+, Safari 12+, Edge 79+

#### 2.2 Java环境要求

**Java版本**:
- **JDK版本**: 与现有系统兼容的JDK版本
- **推荐版本**: OpenJDK 11 或 Oracle JDK 11
- **兼容版本**: JDK 8, JDK 17

**JVM参数建议**:
```bash
# 生产环境JVM参数
-Xms2g
-Xmx4g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/app/
-Dfile.encoding=UTF-8
-Duser.timezone=Asia/Shanghai
```

#### 2.3 数据库要求

**MySQL版本**:
- **使用版本**: 与现有系统相同的MySQL版本
- **推荐版本**: MySQL 8.0.x
- **支持版本**: MySQL 5.7.x

**数据库配置要求**:
```ini
# MySQL配置参数 (my.cnf)
[mysqld]
# 基础配置
port = 3306
bind-address = 0.0.0.0
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# 内存配置
innodb_buffer_pool_size = 2G
innodb_log_file_size = 256M
innodb_log_buffer_size = 16M

# 连接配置
max_connections = 200
max_connect_errors = 10000
wait_timeout = 28800
interactive_timeout = 28800

# 性能配置
innodb_flush_log_at_trx_commit = 2
sync_binlog = 0
query_cache_type = 1
query_cache_size = 128M
```

## 第二部分：开发环境搭建

### 1. Java开发环境

#### 1.1 JDK安装

**Windows安装**:
1. 检查现有系统的JDK版本
2. 如果需要安装，下载对应版本的JDK
3. 配置环境变量:
   - JAVA_HOME: JDK安装路径
   - PATH: 添加 %JAVA_HOME%\bin

**验证安装**:
```cmd
java -version
javac -version
```

#### 1.2 Maven安装

**Windows安装**:
1. 下载Apache Maven
2. 解压到指定目录
3. 配置环境变量:
   - MAVEN_HOME: Maven安装路径
   - PATH: 添加 %MAVEN_HOME%\bin

**Maven配置文件 (settings.xml)**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
  
  <!-- 本地仓库路径 -->
  <localRepository>C:/Users/{username}/.m2/repository</localRepository>
  
  <!-- 镜像配置 -->
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Central</name>
      <url>https://maven.aliyun.com/repository/central</url>
    </mirror>
  </mirrors>
  
  <!-- 配置文件 -->
  <profiles>
    <profile>
      <id>jdk-11</id>
      <activation>
        <activeByDefault>true</activeByDefault>
        <jdk>11</jdk>
      </activation>
      <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <maven.compiler.compilerVersion>11</maven.compiler.compilerVersion>
      </properties>
    </profile>
  </profiles>
</settings>
```

### 2. 数据库环境搭建

#### 2.1 MySQL配置

**使用现有MySQL实例**:
1. 连接到现有MySQL服务器
2. 创建PDA系统专用数据库
3. 创建专用用户和权限

**创建数据库和用户**:
```sql
-- 连接到MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE pda_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER 'pda_user'@'%' IDENTIFIED BY 'Pda@2024#Secure';

-- 授权
GRANT ALL PRIVILEGES ON pda_system.* TO 'pda_user'@'%';
FLUSH PRIVILEGES;

-- 验证
SHOW DATABASES;
SELECT User, Host FROM mysql.user WHERE User = 'pda_user';
```

#### 2.2 数据库初始化

**导入数据库结构**:
```cmd
# 导入数据库脚本
mysql -u pda_user -p pda_system < database/init.sql
mysql -u pda_user -p pda_system < database/data.sql

# 验证导入
mysql -u pda_user -p -e "USE pda_system; SHOW TABLES;"
```

### 3. 开发工具配置

#### 3.1 IDE配置

**IntelliJ IDEA配置**:
1. **项目导入**:
   - File → Open → 选择项目根目录
   - 等待Maven依赖下载完成

2. **JDK配置**:
   - File → Project Structure → Project
   - 设置Project SDK为对应JDK版本
   - 设置Project language level

3. **数据库连接配置**:
   - View → Tool Windows → Database
   - 添加MySQL数据源
   - 配置连接参数

**Eclipse配置**:
1. **项目导入**:
   - File → Import → Existing Maven Projects
   - 选择项目根目录

2. **JDK配置**:
   - Window → Preferences → Java → Installed JREs
   - 添加对应JDK版本

#### 3.2 版本控制配置

**Git配置**:
```cmd
# 全局配置
git config --global user.name "Your Name"
git config --global user.email "your.email@company.com"
git config --global core.autocrlf true
git config --global init.defaultBranch main

# 项目初始化
cd C:\Users\10256\Desktop\S3 0811\PDA
git init
git add .
git commit -m "Initial commit"
```

**.gitignore文件**:
```gitignore
# Java
*.class
*.jar
*.war
*.ear
*.nar
hs_err_pid*

# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties
.mvn/wrapper/maven-wrapper.jar

# IDE
.idea/
*.iws
*.iml
*.ipr
.vscode/
.eclipse/
.metadata/
.recommenders/

# 系统文件
.DS_Store
Thumbs.db

# 日志文件
*.log
logs/

# 配置文件
application-local.yml
application-dev.yml
*.env

# 临时文件
*.tmp
*.temp
*.swp
*.swo
*~

# 数据库
*.db
*.sqlite
*.sqlite3

# 备份文件
*.bak
*.backup
```

## 第三部分：项目结构和配置

### 1. 项目目录结构

```
PDA/
├── docs/                           # 文档目录
│   ├── mobile-dispatch-requirements.md
│   ├── database-design.md
│   ├── api-interface-design.md
│   ├── business-process-design.md
│   ├── operation-manual.md
│   └── environment-setup.md
├── backend/                        # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── pda/
│   │   │   │           ├── PdaApplication.java
│   │   │   │           ├── config/
│   │   │   │           ├── controller/
│   │   │   │           ├── service/
│   │   │   │           ├── repository/
│   │   │   │           ├── entity/
│   │   │   │           ├── dto/
│   │   │   │           └── util/
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       ├── application-prod.yml
│   │   │       └── logback-spring.xml
│   │   └── test/
│   ├── pom.xml
│   └── README.md
├── mobile/                         # 移动端项目
│   ├── android/                    # Android项目
│   └── ios/                        # iOS项目
├── web/                           # Web管理端项目
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── README.md
├── database/                      # 数据库脚本
│   ├── init.sql
│   ├── data.sql
│   └── migration/
├── scripts/                       # 部署脚本
│   ├── build.sh
│   ├── deploy.sh
│   ├── backup.sh
│   └── restore.sh
├── config/                        # 配置文件
│   ├── nginx.conf
│   ├── docker-compose.yml
│   └── systemd/
└── README.md
```

### 2. 应用配置文件

#### 2.1 Spring Boot配置

**application.yml**:
```yaml
# 通用配置
spring:
  application:
    name: pda-system
  profiles:
    active: dev
  
  # 数据源配置
  datasource:
    url: jdbc:mysql://localhost:3306/pda_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: pda_user
    password: Pda@2024#Secure
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  # JPA配置
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
  
  # Web配置
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher

# 服务器配置
server:
  port: 8080
  servlet:
    context-path: /api

# 日志配置
logging:
  level:
    com.pda: DEBUG
    org.springframework: INFO
    org.hibernate: INFO
    root: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# 应用配置
app:
  # PLC配置
  plc:
    default-ip: 192.168.100.1
    default-port: 502
    connection-timeout: 5000
    read-timeout: 3000
    retry-count: 3
  
  # 安全配置
  security:
    jwt:
      secret: pda-system-jwt-secret-key-2024
      expiration: 86400000  # 24小时
    cors:
      allowed-origins: "*"
      allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
      allowed-headers: "*"
  
  # 文件上传配置
  upload:
    path: ./uploads
    max-file-size: 10MB
    max-request-size: 50MB
```

**application-dev.yml**:
```yaml
# 开发环境配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pda_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: pda_user
    password: Pda@2024#Secure
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

logging:
  level:
    com.pda: DEBUG
    org.springframework: DEBUG
    org.hibernate: DEBUG
    root: INFO

app:
  plc:
    default-ip: 192.168.100.1
```

**application-prod.yml**:
```yaml
# 生产环境配置
spring:
  datasource:
    url: jdbc:mysql://192.168.1.11:3306/pda_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: pda_user
    password: ${DB_PASSWORD:Pda@2024#Secure}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

server:
  port: 8080

logging:
  level:
    com.pda: INFO
    org.springframework: WARN
    org.hibernate: WARN
    root: INFO
  file:
    name: ./logs/application.log
    max-size: 100MB
    max-history: 30

app:
  plc:
    default-ip: 192.168.100.1
  security:
    jwt:
      secret: ${JWT_SECRET:pda-system-jwt-secret-key-2024}
```

#### 2.2 Maven配置

**pom.xml**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
        <relativePath/>
    </parent>
    
    <groupId>com.pda</groupId>
    <artifactId>pda-system</artifactId>
    <version>1.0.0</version>
    <name>PDA Mobile Dispatch System</name>
    <description>移动端派工系统</description>
    
    <properties>
        <java.version>11</java.version>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.11.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Modbus TCP通信 -->
        <dependency>
            <groupId>com.digitalpetri.modbus</groupId>
            <artifactId>modbus-master-tcp</artifactId>
            <version>1.2.0</version>
        </dependency>
        
        <!-- 工具类 -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
        </dependency>
        
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        
        <!-- 测试依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>
            
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.22.2</version>
                <configuration>
                    <skipTests>false</skipTests>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## 第四部分：部署和运维

### 1. 构建和打包

#### 1.1 构建脚本

**build.bat** (Windows):
```batch
@echo off
echo 开始构建PDA系统...

:: 清理旧的构建文件
echo 清理旧文件...
call mvn clean

:: 运行测试
echo 运行测试...
call mvn test

:: 打包应用
echo 打包应用...
call mvn package -DskipTests

:: 检查打包结果
if exist "target\pda-system-1.0.0.jar" (
    echo 构建成功！
    echo 应用包: target\pda-system-1.0.0.jar
    dir target\pda-system-1.0.0.jar
) else (
    echo 构建失败！
    exit /b 1
)

echo 构建完成。
pause
```

#### 1.2 部署脚本

**deploy.bat** (Windows):
```batch
@echo off
set APP_NAME=pda-system
set APP_VERSION=1.0.0
set APP_JAR=%APP_NAME%-%APP_VERSION%.jar
set APP_HOME=C:\PDA\app

echo 开始部署 %APP_NAME%...

:: 停止现有应用
echo 停止现有应用...
taskkill /f /im java.exe 2>nul

:: 创建应用目录
if not exist "%APP_HOME%" mkdir "%APP_HOME%"
if not exist "%APP_HOME%\logs" mkdir "%APP_HOME%\logs"
if not exist "%APP_HOME%\config" mkdir "%APP_HOME%\config"
if not exist "%APP_HOME%\backup" mkdir "%APP_HOME%\backup"

:: 备份现有版本
if exist "%APP_HOME%\%APP_JAR%" (
    echo 备份现有版本...
    copy "%APP_HOME%\%APP_JAR%" "%APP_HOME%\backup\%APP_JAR%.%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
)

:: 复制新版本
echo 复制新版本...
copy "target\%APP_JAR%" "%APP_HOME%\"

:: 复制配置文件
echo 复制配置文件...
copy "src\main\resources\application-prod.yml" "%APP_HOME%\config\application.yml"

:: 启动应用
echo 启动应用...
cd /d "%APP_HOME%"
start "PDA System" java -jar -Xms1g -Xmx2g -Dspring.config.location=file:./config/application.yml %APP_JAR%

echo 部署完成！
echo 应用正在启动，请稍等...
timeout /t 10

echo 检查应用状态...
curl -s http://localhost:8080/api/actuator/health

pause
```

### 2. 监控和日志

#### 2.1 应用监控

**健康检查脚本 (health_check.bat)**:
```batch
@echo off
set APP_URL=http://localhost:8080/api/actuator/health
set LOG_FILE=C:\PDA\logs\health_check.log

echo %date% %time% - 开始健康检查 >> %LOG_FILE%

curl -s -o nul -w "%%{http_code}" %APP_URL% > temp_status.txt
set /p HTTP_STATUS=<temp_status.txt
del temp_status.txt

if "%HTTP_STATUS%"=="200" (
    echo %date% %time% - 应用状态正常 >> %LOG_FILE%
) else (
    echo %date% %time% - 应用状态异常，HTTP状态码: %HTTP_STATUS% >> %LOG_FILE%
    :: 发送告警邮件或其他通知
)
```

#### 2.2 日志管理

**日志清理脚本 (cleanup_logs.bat)**:
```batch
@echo off
set LOG_DIR=C:\PDA\logs
set RETENTION_DAYS=30

echo 开始清理超过 %RETENTION_DAYS% 天的日志文件...

:: 清理应用日志
forfiles /p "%LOG_DIR%" /s /m *.log /d -%RETENTION_DAYS% /c "cmd /c del @path" 2>nul

:: 清理GC日志
forfiles /p "%LOG_DIR%" /s /m gc*.log /d -%RETENTION_DAYS% /c "cmd /c del @path" 2>nul

echo 日志清理完成。
```

### 3. 备份和恢复

#### 3.1 数据库备份

**数据库备份脚本 (backup_database.bat)**:
```batch
@echo off
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=pda_system
set DB_USER=pda_user
set DB_PASSWORD=Pda@2024#Secure
set BACKUP_DIR=C:\PDA\backup\database
set BACKUP_FILE=%BACKUP_DIR%\pda_system_%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%.sql

:: 创建备份目录
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

echo 开始备份数据库...
echo 备份文件: %BACKUP_FILE%

:: 执行备份
mysqldump -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% --single-transaction --routines --triggers --events %DB_NAME% > "%BACKUP_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo 数据库备份成功完成！
    echo 备份文件: %BACKUP_FILE%
    dir "%BACKUP_FILE%"
) else (
    echo 数据库备份失败！
    exit /b 1
)

:: 清理过期备份（保留30天）
forfiles /p "%BACKUP_DIR%" /m pda_system_*.sql /d -30 /c "cmd /c del @path" 2>nul

echo 数据库备份完成。
```

#### 3.2 完整系统备份

**系统备份脚本 (full_backup.bat)**:
```batch
@echo off
set APP_HOME=C:\PDA
set BACKUP_ROOT=C:\Backup\PDA
set BACKUP_DATE=%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set BACKUP_DIR=%BACKUP_ROOT%\%BACKUP_DATE%

echo 开始完整系统备份...
echo 备份目录: %BACKUP_DIR%

:: 创建备份目录
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

:: 停止应用服务
echo 停止应用服务...
taskkill /f /im java.exe 2>nul

:: 备份应用文件
echo 备份应用文件...
xcopy "%APP_HOME%\app" "%BACKUP_DIR%\app\" /E /I /Y
xcopy "%APP_HOME%\config" "%BACKUP_DIR%\config\" /E /I /Y
xcopy "%APP_HOME%\scripts" "%BACKUP_DIR%\scripts\" /E /I /Y

:: 备份数据库
echo 备份数据库...
call backup_database.bat
copy "%APP_HOME%\backup\database\pda_system_*.sql" "%BACKUP_DIR%\"

:: 备份日志文件
echo 备份日志文件...
xcopy "%APP_HOME%\logs" "%BACKUP_DIR%\logs\" /E /I /Y

:: 启动应用服务
echo 启动应用服务...
cd /d "%APP_HOME%\app"
start "PDA System" java -jar -Xms1g -Xmx2g pda-system-1.0.0.jar

:: 压缩备份
echo 压缩备份文件...
cd /d "%BACKUP_ROOT%"
tar -czf "pda_system_full_%BACKUP_DATE%.tar.gz" "%BACKUP_DATE%"
rmdir /s /q "%BACKUP_DATE%"

:: 清理过期备份
forfiles /p "%BACKUP_ROOT%" /m pda_system_full_*.tar.gz /d -7 /c "cmd /c del @path" 2>nul

echo 完整备份完成。
echo 备份文件: pda_system_full_%BACKUP_DATE%.tar.gz
```

## 第五部分：安全和性能配置

### 1. 安全配置

#### 1.1 数据库安全

**数据库用户权限配置**:
```sql
-- 创建只读用户（用于报表查询）
CREATE USER 'pda_readonly'@'%' IDENTIFIED BY 'PdaReadOnly@2024#';
GRANT SELECT ON pda_system.* TO 'pda_readonly'@'%';

-- 创建备份用户
CREATE USER 'pda_backup'@'localhost' IDENTIFIED BY 'PdaBackup@2024#';
GRANT SELECT, LOCK TABLES, SHOW VIEW, EVENT, TRIGGER ON pda_system.* TO 'pda_backup'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;
```

#### 1.2 应用安全配置

**安全配置类示例**:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### 2. 性能优化

#### 2.1 JVM性能调优

**生产环境JVM参数**:
```batch
:: JVM内存配置
-Xms2g
-Xmx4g
-XX:MetaspaceSize=256m
-XX:MaxMetaspaceSize=512m

:: 垃圾收集器配置
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m

:: GC日志配置
-Xloggc:C:\PDA\logs\gc\gc.log
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+UseGCLogFileRotation
-XX:NumberOfGCLogFiles=10
-XX:GCLogFileSize=10M

:: 其他优化参数
-Dfile.encoding=UTF-8
-Duser.timezone=Asia/Shanghai
-Djava.awt.headless=true
```

#### 2.2 数据库性能优化

**MySQL性能配置**:
```ini
# MySQL性能优化配置
[mysqld]
# 连接配置
max_connections = 200
max_connect_errors = 10000
wait_timeout = 28800
interactive_timeout = 28800

# 缓存配置
query_cache_type = 1
query_cache_size = 128M
query_cache_limit = 2M

# InnoDB配置
innodb_buffer_pool_size = 2G
innodb_log_file_size = 256M
innodb_log_buffer_size = 16M
innodb_flush_log_at_trx_commit = 2
innodb_file_per_table = 1

# 临时表配置
tmp_table_size = 64M
max_heap_table_size = 64M

# 慢查询日志
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
```

## 第六部分：故障排除和维护

### 1. 常见问题排除

#### 1.1 应用启动问题

**检查清单**:
1. **Java版本检查**:
   ```cmd
   java -version
   ```

2. **端口占用检查**:
   ```cmd
   netstat -ano | findstr :8080
   ```

3. **数据库连接检查**:
   ```cmd
   mysql -h localhost -u pda_user -p pda_system -e "SELECT 1;"
   ```

4. **配置文件检查**:
   - 检查application.yml语法
   - 验证数据库连接参数
   - 确认文件路径正确

#### 1.2 性能问题排除

**性能监控命令**:
```cmd
:: 查看Java进程
jps -v

:: 查看JVM内存使用
jstat -gc [pid] 5s

:: 生成堆转储
jmap -dump:format=b,file=heapdump.hprof [pid]

:: 查看线程状态
jstack [pid]
```

### 2. 维护任务

#### 2.1 定期维护任务

**维护脚本 (maintenance.bat)**:
```batch
@echo off
echo 开始系统维护任务...

:: 清理临时文件
echo 清理临时文件...
del /q /s C:\PDA\temp\*.*

:: 清理过期日志
echo 清理过期日志...
call cleanup_logs.bat

:: 数据库优化
echo 优化数据库...
mysql -u pda_user -p -e "USE pda_system; OPTIMIZE TABLE users, devices, work_orders, production_records;"

:: 检查磁盘空间
echo 检查磁盘空间...
dir C:\ | findstr "bytes free"

:: 备份数据库
echo 执行数据库备份...
call backup_database.bat

echo 系统维护任务完成。
```

#### 2.2 监控脚本

**系统监控脚本 (monitor.bat)**:
```batch
@echo off
set LOG_FILE=C:\PDA\logs\monitor.log

echo %date% %time% - 开始系统监控 >> %LOG_FILE%

:: 检查应用状态
curl -s -o nul -w "%%{http_code}" http://localhost:8080/api/actuator/health > temp_status.txt
set /p HTTP_STATUS=<temp_status.txt
del temp_status.txt

if "%HTTP_STATUS%"=="200" (
    echo %date% %time% - 应用状态: 正常 >> %LOG_FILE%
) else (
    echo %date% %time% - 应用状态: 异常 (HTTP %HTTP_STATUS%) >> %LOG_FILE%
)

:: 检查数据库连接
mysql -h localhost -u pda_user -p%DB_PASSWORD% -e "SELECT 1;" > nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo %date% %time% - 数据库状态: 正常 >> %LOG_FILE%
) else (
    echo %date% %time% - 数据库状态: 异常 >> %LOG_FILE%
)

:: 检查磁盘空间
for /f "tokens=3" %%a in ('dir C:\ ^| findstr "bytes free"') do (
    echo %date% %time% - 磁盘剩余空间: %%a bytes >> %LOG_FILE%
)

echo %date% %time% - 系统监控完成 >> %LOG_FILE%
```

## 总结

本环境配置指南涵盖了移动端派工系统从开发到生产的完整环境搭建过程，包括：

1. **环境要求**: 硬件、软件、网络配置要求
2. **开发环境**: JDK、Maven、数据库、IDE配置
3. **项目结构**: 标准的项目目录结构和配置文件
4. **部署运维**: 构建、部署、监控、备份脚本
5. **安全性能**: 安全配置和性能优化建议
6. **故障排除**: 常见问题解决方案和维护任务

通过遵循本指南，可以确保PDA移动端派工系统在各种环境中稳定、安全、高效地运行。建议在实施过程中根据实际情况调整相关配置参数。