#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
PLC CNC测试脚本
功能：每1秒随机选择CNC01-16中的一个设备，将其点位值加1
用于测试大屏和采集程序的数据更新功能
"""

import time
import random
from pymodbus.client.sync import ModbusTcpClient
import logging

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# PLC连接配置
PLC_HOST = '192.168.1.100'
PLC_PORT = 502

# CNC设备地址映射 (Modbus地址)
CNC_ADDRESSES = {
    'CNC01': 0,
    'CNC02': 1,
    'CNC03': 2,
    'CNC04': 3,
    'CNC05': 4,
    'CNC06': 5,
    'CNC07': 6,
    'CNC08': 7,
    'CNC09': 8,
    'CNC10': 9,
    'CNC11': 10,
    'CNC12': 11,
    'CNC13': 12,
    'CNC14': 13,
    'CNC15': 14,
    'CNC16': 15
}

class PLCCNCTester:
    def __init__(self):
        self.client = None
        self.is_running = False
        
    def connect_to_plc(self):
        """连接到PLC"""
        try:
            self.client = ModbusTcpClient(PLC_HOST, port=PLC_PORT)
            if self.client.connect():
                logger.info(f"成功连接到PLC: {PLC_HOST}:{PLC_PORT}")
                return True
            else:
                logger.error(f"无法连接到PLC: {PLC_HOST}:{PLC_PORT}")
                return False
        except Exception as e:
            logger.error(f"连接PLC时发生错误: {e}")
            return False
    
    def read_register(self, address):
        """读取寄存器值"""
        try:
            result = self.client.read_holding_registers(address, 1)
            if result.isError():
                logger.error(f"读取地址 {address} 失败: {result}")
                return None
            return result.registers[0]
        except Exception as e:
            logger.error(f"读取寄存器 {address} 时发生错误: {e}")
            return None
    
    def write_register(self, address, value):
        """写入寄存器值"""
        try:
            result = self.client.write_register(address, value)
            if result.isError():
                logger.error(f"写入地址 {address} 值 {value} 失败: {result}")
                return False
            return True
        except Exception as e:
            logger.error(f"写入寄存器 {address} 时发生错误: {e}")
            return False
    
    def simulate_cnc_data(self):
        """模拟CNC数据更新"""
        # 随机选择一个CNC设备
        cnc_name = random.choice(list(CNC_ADDRESSES.keys()))
        address = CNC_ADDRESSES[cnc_name]
        
        # 读取当前值
        current_value = self.read_register(address)
        if current_value is None:
            logger.warning(f"无法读取 {cnc_name} (地址: {address}) 的当前值")
            return False
        
        # 增加1
        new_value = current_value + 1
        
        # 写入新值
        if self.write_register(address, new_value):
            logger.info(f"✓ {cnc_name} (地址: {address}): {current_value} → {new_value}")
            return True
        else:
            logger.error(f"✗ 更新 {cnc_name} (地址: {address}) 失败")
            return False
    
    def start_simulation(self):
        """开始模拟测试"""
        logger.info("=== PLC CNC 模拟测试开始 ===")
        logger.info("按 Ctrl+C 停止测试")
        
        if not self.connect_to_plc():
            logger.error("无法连接到PLC，测试终止")
            return
        
        self.is_running = True
        success_count = 0
        total_count = 0
        
        try:
            while self.is_running:
                total_count += 1
                if self.simulate_cnc_data():
                    success_count += 1
                
                # 显示统计信息
                if total_count % 10 == 0:
                    success_rate = (success_count / total_count) * 100
                    logger.info(f"统计: 总计 {total_count} 次，成功 {success_count} 次，成功率 {success_rate:.1f}%")
                
                # 等待1秒
                time.sleep(1)
                
        except KeyboardInterrupt:
            logger.info("\n收到停止信号，正在停止测试...")
        except Exception as e:
            logger.error(f"测试过程中发生错误: {e}")
        finally:
            self.stop_simulation()
    
    def stop_simulation(self):
        """停止模拟测试"""
        self.is_running = False
        if self.client:
            self.client.close()
            logger.info("PLC连接已关闭")
        logger.info("=== PLC CNC 模拟测试结束 ===")

def main():
    """主函数"""
    tester = PLCCNCTester()
    tester.start_simulation()

if __name__ == "__main__":
    main()