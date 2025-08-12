const http = require('http');
const url = require('url');
const path = require('path');
const fs = require('fs');
const sqlite3 = require('sqlite3').verbose();

// 数据库路径
const DB_PATH = path.join(__dirname, '..', 'plc_data.db');

// 创建HTTP服务器
const server = http.createServer((req, res) => {
    const parsedUrl = url.parse(req.url, true);
    const pathname = parsedUrl.pathname;
    
    // 设置CORS头
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
    
    if (req.method === 'OPTIONS') {
        res.writeHead(200);
        res.end();
        return;
    }
    
    // API路由
    if (pathname === '/api/cnc-devices') {
        getCNCDevicesData(req, res);
    } else if (pathname === '/api/ccm08-devices') {
        getCCM08DevicesData(req, res);
    } else if (pathname === '/api/ccm23-devices') {
        getCCM23DevicesData(req, res);
    } else if (pathname === '/api/device-status') {
        getDeviceStatus(req, res);
    } else if (pathname === '/') {
        // 提供HTML文件
        serveFile(res, path.join(__dirname, 'cnc-dashboard.html'), 'text/html');
    } else if (pathname === '/ccm08') {
        // 提供CCM08页面
        serveFile(res, path.join(__dirname, 'ccm08-dashboard.html'), 'text/html');
    } else if (pathname === '/ccm23') {
        // 提供CCM23页面
        serveFile(res, path.join(__dirname, 'ccm23-dashboard.html'), 'text/html');
    } else {
        res.writeHead(404, {'Content-Type': 'application/json'});
        res.end(JSON.stringify({error: 'Not Found'}));
    }
});

// 获取CNC设备数据
function getCNCDevicesData(req, res) {
    const db = new sqlite3.Database(DB_PATH, sqlite3.OPEN_READONLY, (err) => {
        if (err) {
            console.error('数据库连接失败:', err.message);
            res.writeHead(500, {'Content-Type': 'application/json'});
            res.end(JSON.stringify({error: '数据库连接失败'}));
            return;
        }
    });
    
    // 查询CNC设备数据
    const sql = `
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
        WHERE dc.device_name LIKE 'CNC-%' AND dc.is_enabled = 1
        ORDER BY dc.address_number
    `;
    
    db.all(sql, [], (err, rows) => {
        if (err) {
            console.error('查询失败:', err.message);
            res.writeHead(500, {'Content-Type': 'application/json'});
            res.end(JSON.stringify({error: '查询失败'}));
            db.close();
            return;
        }
        
        // 转换数据格式
        const devices = rows.map(row => {
            // 根据当前值判断设备状态
            let status = 'offline';
            if (row.current_value > 0) {
                status = 'running';
            } else if (row.last_update_time) {
                const lastUpdate = new Date(row.last_update_time);
                const now = new Date();
                const diffMinutes = (now - lastUpdate) / (1000 * 60);
                if (diffMinutes < 10) {
                    status = 'standby';
                } else {
                    status = 'offline';
                }
            }
            
            return {
                id: row.device_name,
                name: row.device_name,
                status: status,
                count: Math.floor(row.current_value || 0),
                address: row.device_address,
                lastUpdate: row.last_update_time || null,
                workOrder: `WO${String(row.address_number).slice(-3)}`,
                startTime: row.last_update_time ? new Date(row.last_update_time).toLocaleTimeString('zh-CN', {hour12: false}) : '--:--:--'
            };
        });
        
        // 计算统计数据
        const stats = {
            total: devices.length,
            running: devices.filter(d => d.status === 'running').length,
            standby: devices.filter(d => d.status === 'standby').length,
            offline: devices.filter(d => d.status === 'offline').length,
            totalCount: devices.reduce((sum, d) => sum + d.count, 0)
        };
        
        const response = {
            success: true,
            timestamp: new Date().toISOString(),
            stats: stats,
            devices: devices
        };
        
        res.writeHead(200, {'Content-Type': 'application/json'});
        res.end(JSON.stringify(response, null, 2));
        
        db.close();
    });
}

// 获取CCM08设备数据
function getCCM08DevicesData(req, res) {
    const db = new sqlite3.Database(DB_PATH, sqlite3.OPEN_READONLY, (err) => {
        if (err) {
            console.error('数据库连接失败:', err.message);
            res.writeHead(500, {'Content-Type': 'application/json'});
            res.end(JSON.stringify({error: '数据库连接失败'}));
            return;
        }
    });
    
    // 查询CCM08设备数据
    const sql = `
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
        WHERE dc.device_name LIKE 'CCM08-%' AND dc.is_enabled = 1
        ORDER BY dc.address_number
    `;
    
    db.all(sql, [], (err, rows) => {
        if (err) {
            console.error('查询失败:', err.message);
            res.writeHead(500, {'Content-Type': 'application/json'});
            res.end(JSON.stringify({error: '查询失败'}));
            db.close();
            return;
        }
        
        // 转换数据格式
        const devices = rows.map(row => {
            // 根据当前值判断设备状态
            let status = 'offline';
            if (row.current_value > 0) {
                status = 'running';
            } else if (row.last_update_time) {
                const lastUpdate = new Date(row.last_update_time);
                const now = new Date();
                const diffMinutes = (now - lastUpdate) / (1000 * 60);
                if (diffMinutes < 10) {
                    status = 'standby';
                } else {
                    status = 'offline';
                }
            }
            
            return {
                id: row.device_name,
                name: row.device_name,
                status: status,
                count: Math.floor(row.current_value || 0),
                address: row.device_address,
                lastUpdate: row.last_update_time || null,
                workOrder: `WO2025010${String(row.address_number).slice(-2)}`,
                startTime: row.last_update_time ? new Date(row.last_update_time).toLocaleTimeString('zh-CN', {hour12: false}) : '--:--:--'
            };
        });
        
        // 计算统计数据
        const stats = {
            total: devices.length,
            running: devices.filter(d => d.status === 'running').length,
            standby: devices.filter(d => d.status === 'standby').length,
            offline: devices.filter(d => d.status === 'offline').length,
            totalCount: devices.reduce((sum, d) => sum + d.count, 0)
        };
        
        const response = {
            success: true,
            timestamp: new Date().toISOString(),
            stats: stats,
            devices: devices
        };
        
        res.writeHead(200, {'Content-Type': 'application/json'});
        res.end(JSON.stringify(response, null, 2));
        
        db.close();
    });
}

// 获取CCM23设备数据
function getCCM23DevicesData(req, res) {
    const db = new sqlite3.Database(DB_PATH, sqlite3.OPEN_READONLY, (err) => {
        if (err) {
            console.error('数据库连接失败:', err.message);
            res.writeHead(500, {'Content-Type': 'application/json'});
            res.end(JSON.stringify({error: '数据库连接失败'}));
            return;
        }
    });
    
    // 查询CCM23设备数据
    const sql = `
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
        WHERE dc.device_name LIKE 'CCM23-%' AND dc.is_enabled = 1
        ORDER BY dc.address_number
    `;
    
    db.all(sql, [], (err, rows) => {
        if (err) {
            console.error('查询失败:', err.message);
            res.writeHead(500, {'Content-Type': 'application/json'});
            res.end(JSON.stringify({error: '查询失败'}));
            db.close();
            return;
        }
        
        // 转换数据格式
        const devices = rows.map(row => {
            // 根据当前值判断设备状态
            let status = 'offline';
            if (row.current_value > 0) {
                status = 'running';
            } else if (row.last_update_time) {
                const lastUpdate = new Date(row.last_update_time);
                const now = new Date();
                const diffMinutes = (now - lastUpdate) / (1000 * 60);
                if (diffMinutes < 10) {
                    status = 'standby';
                } else {
                    status = 'offline';
                }
            }
            
            return {
                id: row.device_name,
                name: row.device_name,
                status: status,
                count: Math.floor(row.current_value || 0),
                address: row.device_address,
                lastUpdate: row.last_update_time || null,
                workOrder: `WO2025010${String(200 + parseInt(row.device_name.split('-')[1]) || 0)}`,
                startTime: row.last_update_time ? new Date(row.last_update_time).toLocaleTimeString('zh-CN', {hour12: false}) : '--:--:--'
            };
        });
        
        // 计算统计数据
        const stats = {
            total: devices.length,
            running: devices.filter(d => d.status === 'running').length,
            standby: devices.filter(d => d.status === 'standby').length,
            offline: devices.filter(d => d.status === 'offline').length,
            totalCount: devices.reduce((sum, d) => sum + d.count, 0)
        };
        
        const response = {
            success: true,
            timestamp: new Date().toISOString(),
            stats: stats,
            devices: devices
        };
        
        res.writeHead(200, {'Content-Type': 'application/json'});
        res.end(JSON.stringify(response, null, 2));
        
        db.close();
    });
}

// 获取所有设备状态
function getDeviceStatus(req, res) {
    const db = new sqlite3.Database(DB_PATH, sqlite3.OPEN_READONLY, (err) => {
        if (err) {
            console.error('数据库连接失败:', err.message);
            res.writeHead(500, {'Content-Type': 'application/json'});
            res.end(JSON.stringify({error: '数据库连接失败'}));
            return;
        }
    });
    
    const sql = `
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
        ORDER BY dc.address_number
    `;
    
    db.all(sql, [], (err, rows) => {
        if (err) {
            console.error('查询失败:', err.message);
            res.writeHead(500, {'Content-Type': 'application/json'});
            res.end(JSON.stringify({error: '查询失败'}));
            db.close();
            return;
        }
        
        const response = {
            success: true,
            timestamp: new Date().toISOString(),
            devices: rows
        };
        
        res.writeHead(200, {'Content-Type': 'application/json'});
        res.end(JSON.stringify(response, null, 2));
        
        db.close();
    });
}

// 提供静态文件
function serveFile(res, filePath, contentType) {
    fs.readFile(filePath, (err, data) => {
        if (err) {
            res.writeHead(404, {'Content-Type': 'text/plain'});
            res.end('File not found');
            return;
        }
        
        res.writeHead(200, {'Content-Type': contentType});
        res.end(data);
    });
}

// 启动服务器
const PORT = 3000;
server.listen(PORT, () => {
    console.log(`S3 PLC数据API服务器已启动`);
    console.log(`服务地址: http://localhost:${PORT}`);
    console.log(`API接口:`);
    console.log(`  - GET /api/cnc-devices - 获取CNC设备数据`);
    console.log(`  - GET /api/ccm08-devices - 获取CCM08设备数据`);
    console.log(`  - GET /api/ccm23-devices - 获取CCM23设备数据`);
    console.log(`  - GET /api/device-status - 获取所有设备状态`);
    console.log(`监控页面:`);
    console.log(`  - GET / - CNC监控大屏`);
    console.log(`  - GET /ccm08 - CCM08监控大屏`);
    console.log(`  - GET /ccm23 - CCM23监控大屏`);
    console.log(`数据库路径: ${DB_PATH}`);
    
    // 检查数据库文件是否存在
    if (!fs.existsSync(DB_PATH)) {
        console.warn(`警告: 数据库文件不存在: ${DB_PATH}`);
        console.warn(`请确保S3 PLC数据采集服务正在运行并已创建数据库`);
    }
});

// 优雅关闭
process.on('SIGINT', () => {
    console.log('\n正在关闭服务器...');
    server.close(() => {
        console.log('服务器已关闭');
        process.exit(0);
    });
});