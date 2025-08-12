package com.pda.service;

import com.pda.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 设备服务接口
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
public interface DeviceService {

    /**
     * 保存设备
     */
    Device saveDevice(Device device);

    /**
     * 根据ID查找设备
     */
    Optional<Device> findById(Long id);

    /**
     * 根据设备编号查找设备
     */
    Optional<Device> findByDeviceCode(String deviceCode);

    /**
     * 检查设备编号是否存在
     */
    boolean existsByDeviceCode(String deviceCode);

    /**
     * 检查PLC地址是否存在
     */
    boolean existsByPlcAddress(String plcAddress);

    /**
     * 创建新设备
     */
    Device createDevice(String deviceCode, String deviceName, String deviceType, String plcAddress);

    /**
     * 更新设备信息
     */
    Device updateDevice(Long id, String deviceName, String deviceType, String plcAddress, String status);

    /**
     * 删除设备
     */
    boolean deleteDevice(Long id);

    /**
     * 获取所有设备
     */
    List<Device> findAllDevices();

    /**
     * 根据设备类型查找设备
     */
    List<Device> findByDeviceType(String deviceType);

    /**
     * 根据状态查找设备
     */
    List<Device> findByStatus(String status);

    /**
     * 根据设备类型和状态查找设备
     */
    List<Device> findByDeviceTypeAndStatus(String deviceType, String status);

    /**
     * 获取空闲设备列表
     */
    List<Device> getIdleDevices();

    /**
     * 获取工作中设备列表
     */
    List<Device> getWorkingDevices();

    /**
     * 获取维修中设备列表
     */
    List<Device> getMaintenanceDevices();

    /**
     * 获取可用设备列表（空闲状态）
     */
    List<Device> getAvailableDevices();

    /**
     * 根据设备类型获取可用设备
     */
    List<Device> getAvailableDevicesByType(String deviceType);

    /**
     * 分页查询设备
     */
    Page<Device> findDevicesWithConditions(String deviceCode, String deviceType, String status, Pageable pageable);

    /**
     * 搜索设备
     */
    List<Device> searchDevices(String keyword);

    /**
     * 分页搜索设备
     */
    Page<Device> searchDevices(String keyword, Pageable pageable);

    /**
     * 更新设备状态
     */
    boolean updateDeviceStatus(Long id, String status);

    /**
     * 设置设备为工作状态
     */
    boolean setDeviceWorking(Long id);

    /**
     * 设置设备为空闲状态
     */
    boolean setDeviceIdle(Long id);

    /**
     * 设置设备为维修状态
     */
    boolean setDeviceMaintenance(Long id);

    /**
     * 批量更新设备状态
     */
    int batchUpdateDeviceStatus(List<Long> deviceIds, String status);

    /**
     * 获取设备统计信息
     */
    Map<String, Long> getDeviceStatistics();

    /**
     * 获取设备类型分布
     */
    Map<String, Long> getDeviceTypeDistribution();

    /**
     * 获取设备状态分布
     */
    Map<String, Long> getDeviceStatusDistribution();

    /**
     * 获取设备类型和状态组合统计
     */
    Map<String, Map<String, Long>> getDeviceTypeStatusStatistics();

    /**
     * 检查设备是否可用于生产
     */
    boolean isDeviceAvailableForProduction(Long deviceId);

    /**
     * 检查设备是否可用于生产（根据设备编号）
     */
    boolean isDeviceAvailableForProduction(String deviceCode);

    /**
     * 检查设备是否正在生产
     */
    boolean isDeviceWorking(Long deviceId);

    /**
     * 检查设备是否正在维修
     */
    boolean isDeviceInMaintenance(Long deviceId);

    /**
     * 获取设备编号列表
     */
    List<String> getAllDeviceCodes();

    /**
     * 根据设备类型获取设备编号列表
     */
    List<String> getDeviceCodesByType(String deviceType);

    /**
     * 获取可用设备编号列表
     */
    List<String> getAvailableDeviceCodes();

    /**
     * 根据设备类型获取可用设备编号列表
     */
    List<String> getAvailableDeviceCodesByType(String deviceType);

    /**
     * 获取设备利用率统计
     */
    Map<String, Double> getDeviceUtilizationStatistics(int days);

    /**
     * 获取设备生产效率排名
     */
    List<Map<String, Object>> getDeviceEfficiencyRanking(int days);

    /**
     * 验证设备编号格式
     */
    boolean validateDeviceCodeFormat(String deviceCode);

    /**
     * 验证PLC地址格式
     */
    boolean validatePlcAddressFormat(String plcAddress);

    /**
     * 生成设备编号
     */
    String generateDeviceCode(String deviceType);

    /**
     * 初始化默认设备
     */
    void initializeDefaultDevices();

    /**
     * 获取设备详细信息（包含生产状态）
     */
    Map<String, Object> getDeviceDetailInfo(Long deviceId);

    /**
     * 获取所有设备的状态概览
     */
    List<Map<String, Object>> getAllDevicesStatusOverview();

    /**
     * 根据设备类型获取设备状态概览
     */
    List<Map<String, Object>> getDevicesStatusOverviewByType(String deviceType);

    /**
     * 检查设备编号冲突（排除指定ID）
     */
    boolean checkDeviceCodeConflict(String deviceCode, Long excludeId);

    /**
     * 检查PLC地址冲突（排除指定ID）
     */
    boolean checkPlcAddressConflict(String plcAddress, Long excludeId);

    /**
     * 获取设备类型列表
     */
    List<String> getDeviceTypes();

    /**
     * 获取设备状态列表
     */
    List<String> getDeviceStatuses();

    /**
     * 重置所有设备状态为空闲
     */
    int resetAllDevicesToIdle();

    /**
     * 获取设备运行时长统计
     */
    Map<String, Long> getDeviceRunningTimeStatistics(String deviceCode, int days);
}