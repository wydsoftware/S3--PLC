#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
简单的Modbus TCP服务器模拟器
用于测试PLC数据采集系统
"""

import socket
import struct
import threading
import time
import random

class ModbusTCPSimulator:
    def __init__(self, host='0.0.0.0', port=502):
        self.host = host
        self.port = port
        self.running = False
        self.socket = None
        
        # 模拟寄存器数据 (地址: 值)
        self.holding_registers = {}
        
        # 初始化测试数据
        self.init_test_data()
        
    def init_test_data(self):
        """初始化测试数据"""
        # D802-D896 对应 Modbus地址 801-895
        addresses = [802, 804, 806, 808, 810, 812, 814, 816, 818, 820,
                    822, 824, 826, 828, 830, 832, 834, 836, 838, 840,
                    842, 844, 846, 848, 850, 852, 854, 856, 858, 860,
                    862, 864, 866, 868, 870, 872, 874, 876, 878, 880,
                    882, 884, 886, 888, 890, 892, 894, 896]
        
        for addr in addresses:
            # Modbus地址 = D地址 - 1
            modbus_addr = addr - 1
            # 生成随机测试数据
            self.holding_registers[modbus_addr] = random.randint(100, 999)
            
    def update_test_data(self):
        """更新测试数据（模拟实时变化）"""
        for addr in self.holding_registers:
            # 随机更新一些寄存器的值
            if random.random() < 0.3:  # 30%的概率更新
                self.holding_registers[addr] = random.randint(100, 999)
                
    def parse_modbus_request(self, data):
        """解析Modbus请求"""
        if len(data) < 12:
            return None
            
        # Modbus TCP ADU格式
        transaction_id = struct.unpack('>H', data[0:2])[0]
        protocol_id = struct.unpack('>H', data[2:4])[0]
        length = struct.unpack('>H', data[4:6])[0]
        unit_id = data[6]
        function_code = data[7]
        
        if function_code == 3:  # Read Holding Registers
            start_addr = struct.unpack('>H', data[8:10])[0]
            quantity = struct.unpack('>H', data[10:12])[0]
            return {
                'transaction_id': transaction_id,
                'protocol_id': protocol_id,
                'unit_id': unit_id,
                'function_code': function_code,
                'start_addr': start_addr,
                'quantity': quantity
            }
        return None
        
    def create_response(self, request):
        """创建Modbus响应"""
        if request['function_code'] == 3:  # Read Holding Registers
            start_addr = request['start_addr']
            quantity = request['quantity']
            
            # 读取寄存器数据
            register_data = []
            for i in range(quantity):
                addr = start_addr + i
                value = self.holding_registers.get(addr, 0)
                register_data.append(value)
                
            # 构建响应
            byte_count = quantity * 2
            response_data = struct.pack('B', byte_count)
            
            for value in register_data:
                response_data += struct.pack('>H', value)
                
            # Modbus TCP响应头
            response_length = 3 + byte_count
            response = struct.pack('>HHHBB', 
                                 request['transaction_id'],
                                 request['protocol_id'],
                                 response_length,
                                 request['unit_id'],
                                 request['function_code'])
            response += response_data
            
            return response
        return None
        
    def handle_client(self, client_socket, addr):
        """处理客户端连接"""
        print(f"客户端连接: {addr}")
        
        try:
            while self.running:
                data = client_socket.recv(1024)
                if not data:
                    break
                    
                request = self.parse_modbus_request(data)
                if request:
                    print(f"收到请求: 读取地址 {request['start_addr']}, 数量 {request['quantity']}")
                    response = self.create_response(request)
                    if response:
                        client_socket.send(response)
                        
        except Exception as e:
            print(f"处理客户端连接时出错: {e}")
        finally:
            client_socket.close()
            print(f"客户端断开: {addr}")
            
    def start(self):
        """启动模拟器"""
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        
        try:
            self.socket.bind((self.host, self.port))
            self.socket.listen(5)
            self.running = True
            
            print(f"Modbus TCP模拟器启动: {self.host}:{self.port}")
            print("等待客户端连接...")
            
            # 启动数据更新线程
            update_thread = threading.Thread(target=self.data_update_loop)
            update_thread.daemon = True
            update_thread.start()
            
            while self.running:
                try:
                    client_socket, addr = self.socket.accept()
                    client_thread = threading.Thread(
                        target=self.handle_client, 
                        args=(client_socket, addr)
                    )
                    client_thread.daemon = True
                    client_thread.start()
                except Exception as e:
                    if self.running:
                        print(f"接受连接时出错: {e}")
                        
        except Exception as e:
            print(f"启动模拟器时出错: {e}")
        finally:
            self.stop()
            
    def data_update_loop(self):
        """数据更新循环"""
        while self.running:
            time.sleep(2)  # 每2秒更新一次数据
            self.update_test_data()
            
    def stop(self):
        """停止模拟器"""
        self.running = False
        if self.socket:
            self.socket.close()
        print("Modbus TCP模拟器已停止")
        
if __name__ == '__main__':
    simulator = ModbusTCPSimulator(host='192.168.1.2', port=502)
    
    try:
        simulator.start()
    except KeyboardInterrupt:
        print("\n收到中断信号，正在停止...")
        simulator.stop()