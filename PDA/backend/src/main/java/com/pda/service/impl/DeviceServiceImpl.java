package com.pda.service.impl;

import com.pda.entity.Device;
import com.pda.repository.DeviceRepository;
import com.pda.repository.WorkOrderRepository;
import com.pda.repository.MaintenanceRecordRepository;
import com.pda.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 设备服务实现类
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final WorkOrderRepository workOrderRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    
    // 设备编号格式验证正则表达式
    private static final Pattern DEVICE_CODE_PATTERN = Pattern.compile("^[A-Z]{2,6}\\d{2}$");
    
    // PLC地址格式验证正则表达式
    private static final Pattern PLC_ADDRESS_PATTERN = Pattern.compile("^D\\d{3,4}$");
    
    // 设备类型列表
    private static final List<String> DEVICE_TYPES = Arrays.asList("AOI", "CNC", "CCM08", "CCM23");
    
    // 设备状态列表
    private static final List<String> DEVICE_STATUSES = Arrays.asList("idle", "working", "maintenance");

    @Override
    @Transactional
    public Device saveDevice(Device device) {
        if (device.getId() == null) {
            device.setCreatedTime(LocalDateTime.now());
        }
        device.setUpdatedTime(LocalDateTime.now());
        return deviceRepository.save(device);
    }

    @Override
    public Optional<Device> findById(Long id) {
        return deviceRepository.findById(id);
    }

    @Override
    public Optional<Device> findByDeviceCode(String deviceCode) {
        return deviceRepository.findByDeviceCode(deviceCode);
    }

    @Override
    public boolean existsByDeviceCode(String deviceCode) {
        return deviceRepository.existsByDeviceCode(deviceCode);
    }

    @Override
    public boolean existsByPlcAddress(String plcAddress) {
        return deviceRepository.existsByPlcAddress(plcAddress);
    }

    @Override
    @Transactional
    public Device createDevice(String deviceCode, String deviceName, String deviceType, String plcAddress) {
        if (existsByDeviceCode(deviceCode)) {
            throw new IllegalArgumentException("设备编号已存在: " + deviceCode);
        }
        
        if (existsByPlcAddress(plcAddress)) {
            throw new IllegalArgumentException("PLC地址已存在: " + plcAddress);
        }
        
        if (!validateDeviceCodeFormat(deviceCode)) {
            throw new IllegalArgumentException("设备编号格式不正确: " + deviceCode);
        }
        
        if (!validatePlcAddressFormat(plcAddress)) {
            throw new IllegalArgumentException("PLC地址格式不正确: " + plcAddress);
        }
        
        if (!DEVICE_TYPES.contains(deviceType)) {
            throw new IllegalArgumentException("不支持的设备类型: " + deviceType);
        }
        
        Device device = new Device();
        device.setDeviceCode(deviceCode);
        device.setDeviceName(deviceName);
        device.setDeviceType(deviceType);
        device.setPlcAddress(plcAddress);
        device.setStatus("idle");
        
        return saveDevice(device);
    }

    @Override
    @Transactional
    public Device updateDevice(Long id, String deviceName, String deviceType, String plcAddress, String status) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + id));
        
        if (StringUtils.hasText(deviceName)) {
            device.setDeviceName(deviceName);
        }
        
        if (StringUtils.hasText(deviceType) && DEVICE_TYPES.contains(deviceType)) {
            device.setDeviceType(deviceType);
        }
        
        if (StringUtils.hasText(plcAddress)) {
            if (!validatePlcAddressFormat(plcAddress)) {
                throw new IllegalArgumentException("PLC地址格式不正确: " + plcAddress);
            }
            if (checkPlcAddressConflict(plcAddress, id)) {
                throw new IllegalArgumentException("PLC地址已存在: " + plcAddress);
            }
            device.setPlcAddress(plcAddress);
        }
        
        if (StringUtils.hasText(status) && DEVICE_STATUSES.contains(status)) {
            device.setStatus(status);
        }
        
        device.setUpdatedTime(LocalDateTime.now());
        return deviceRepository.save(device);
    }

    @Override
    @Transactional
    public boolean deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            return false;
        }
        
        // 检查设备是否有正在进行的派工单
        if (workOrderRepository.hasActiveWorkOrderForDevice(id)) {
            throw new IllegalStateException("设备有正在进行的派工单，不能删除");
        }
        
        // 检查设备是否有正在进行的维修
        if (maintenanceRecordRepository.hasActiveMaintenanceForDevice(id)) {
            throw new IllegalStateException("设备有正在进行的维修，不能删除");
        }
        
        deviceRepository.deleteById(id);
        return true;
    }

    @Override
    public List<Device> findAllDevices() {
        return deviceRepository.findAll();
    }

    @Override
    public List<Device> findByDeviceType(String deviceType) {
        return deviceRepository.findByDeviceType(deviceType);
    }

    @Override
    public List<Device> findByStatus(String status) {
        return deviceRepository.findByStatus(status);
    }

    @Override
    public List<Device> findByDeviceTypeAndStatus(String deviceType, String status) {
        return deviceRepository.findByDeviceTypeAndStatus(deviceType, status);
    }

    @Override
    public List<Device> getIdleDevices() {
        return deviceRepository.findByStatus("idle");
    }

    @Override
    public List<Device> getWorkingDevices() {
        return deviceRepository.findByStatus("working");
    }

    @Override
    public List<Device> getMaintenanceDevices() {
        return deviceRepository.findByStatus("maintenance");
    }

    @Override
    public List<Device> getAvailableDevices() {
        return getIdleDevices();
    }

    @Override
    public List<Device> getAvailableDevicesByType(String deviceType) {
        return deviceRepository.findByDeviceTypeAndStatus(deviceType, "idle");
    }

    @Override
    public Page<Device> findDevicesWithConditions(String deviceCode, String deviceType, String status, Pageable pageable) {
        return deviceRepository.findDevicesWithConditions(deviceCode, deviceType, status, pageable);
    }

    @Override
    public List<Device> searchDevices(String keyword) {
        return deviceRepository.searchDevicesByKeyword(keyword);
    }

    @Override
    public Page<Device> searchDevices(String keyword, Pageable pageable) {
        return deviceRepository.searchDevicesByKeyword(keyword, pageable);
    }

    @Override
    @Transactional
    public boolean updateDeviceStatus(Long id, String status) {
        if (!DEVICE_STATUSES.contains(status)) {
            throw new IllegalArgumentException("不支持的设备状态: " + status);
        }
        
        Device device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            return false;
        }
        
        // 状态转换验证
        if ("working".equals(status) && "maintenance".equals(device.getStatus())) {
            throw new IllegalStateException("维修中的设备不能直接设置为工作状态");
        }
        
        return deviceRepository.updateDeviceStatus(id, status) > 0;
    }

    @Override
    @Transactional
    public boolean setDeviceWorking(Long id) {
        return updateDeviceStatus(id, "working");
    }

    @Override
    @Transactional
    public boolean setDeviceIdle(Long id) {
        return updateDeviceStatus(id, "idle");
    }

    @Override
    @Transactional
    public boolean setDeviceMaintenance(Long id) {
        return updateDeviceStatus(id, "maintenance");
    }

    @Override
    @Transactional
    public int batchUpdateDeviceStatus(List<Long> deviceIds, String status) {
        if (!DEVICE_STATUSES.contains(status)) {
            throw new IllegalArgumentException("不支持的设备状态: " + status);
        }
        return deviceRepository.batchUpdateDeviceStatus(deviceIds, status);
    }

    @Override
    public Map<String, Long> getDeviceStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", deviceRepository.countAllDevices());
        stats.put("idle", deviceRepository.countByStatus("idle"));
        stats.put("working", deviceRepository.countByStatus("working"));
        stats.put("maintenance", deviceRepository.countByStatus("maintenance"));
        return stats;
    }

    @Override
    public Map<String, Long> getDeviceTypeDistribution() {
        List<Object[]> results = deviceRepository.getDeviceTypeDistribution();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Override
    public Map<String, Long> getDeviceStatusDistribution() {
        List<Object[]> results = deviceRepository.getDeviceStatusDistribution();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Override
    public Map<String, Map<String, Long>> getDeviceTypeStatusStatistics() {
        List<Object[]> results = deviceRepository.getDeviceTypeStatusStatistics();
        Map<String, Map<String, Long>> stats = new HashMap<>();
        
        for (Object[] result : results) {
            String deviceType = (String) result[0];
            String status = (String) result[1];
            Long count = (Long) result[2];
            
            stats.computeIfAbsent(deviceType, k -> new HashMap<>()).put(status, count);
        }
        
        return stats;
    }

    @Override
    public boolean isDeviceAvailableForProduction(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).orElse(null);
        return device != null && "idle".equals(device.getStatus());
    }

    @Override
    public boolean isDeviceAvailableForProduction(String deviceCode) {
        Device device = deviceRepository.findByDeviceCode(deviceCode).orElse(null);
        return device != null && "idle".equals(device.getStatus());
    }

    @Override
    public boolean isDeviceWorking(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).orElse(null);
        return device != null && "working".equals(device.getStatus());
    }

    @Override
    public boolean isDeviceInMaintenance(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).orElse(null);
        return device != null && "maintenance".equals(device.getStatus());
    }

    @Override
    public List<String> getAllDeviceCodes() {
        return deviceRepository.getAllDeviceCodes();
    }

    @Override
    public List<String> getDeviceCodesByType(String deviceType) {
        return deviceRepository.getDeviceCodesByType(deviceType);
    }

    @Override
    public List<String> getAvailableDeviceCodes() {
        return deviceRepository.getAvailableDeviceCodes();
    }

    @Override
    public List<String> getAvailableDeviceCodesByType(String deviceType) {
        return deviceRepository.getAvailableDeviceCodesByType(deviceType);
    }

    @Override
    public Map<String, Double> getDeviceUtilizationStatistics(int days) {
        List<Object[]> results = deviceRepository.getDeviceUtilizationStatistics(days);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> ((Number) result[1]).doubleValue()
                ));
    }

    @Override
    public List<Map<String, Object>> getDeviceEfficiencyRanking(int days) {
        List<Object[]> results = deviceRepository.getDeviceUtilizationStatistics(days);
        return results.stream()
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("deviceCode", result[0]);
                    item.put("utilization", result[1]);
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean validateDeviceCodeFormat(String deviceCode) {
        return StringUtils.hasText(deviceCode) && DEVICE_CODE_PATTERN.matcher(deviceCode).matches();
    }

    @Override
    public boolean validatePlcAddressFormat(String plcAddress) {
        return StringUtils.hasText(plcAddress) && PLC_ADDRESS_PATTERN.matcher(plcAddress).matches();
    }

    @Override
    public String generateDeviceCode(String deviceType) {
        if (!DEVICE_TYPES.contains(deviceType)) {
            throw new IllegalArgumentException("不支持的设备类型: " + deviceType);
        }
        
        // 查找该类型设备的最大编号
        List<String> existingCodes = getDeviceCodesByType(deviceType);
        int maxNumber = 0;
        
        for (String code : existingCodes) {
            String numberPart = code.substring(deviceType.length());
            try {
                int number = Integer.parseInt(numberPart);
                maxNumber = Math.max(maxNumber, number);
            } catch (NumberFormatException e) {
                // 忽略格式错误的编号
            }
        }
        
        return String.format("%s%02d", deviceType, maxNumber + 1);
    }

    @Override
    @PostConstruct
    @Transactional
    public void initializeDefaultDevices() {
        log.info("初始化默认设备...");
        
        // 初始化AOI设备
        initializeDevicesByType("AOI", 8, "D", 802);
        
        // 初始化CNC设备
        initializeDevicesByType("CNC", 16, "D", 866);
        
        // 初始化CCM08设备
        initializeDevicesByType("CCM08", 24, "D", 806);
        
        // 初始化CCM23设备（实际为CCM32）
        initializeDevicesByType("CCM23", 8, "D", 854);
        
        log.info("默认设备初始化完成");
    }
    
    private void initializeDevicesByType(String deviceType, int count, String plcPrefix, int startAddress) {
        for (int i = 1; i <= count; i++) {
            String deviceCode = String.format("%s%02d", deviceType, i);
            if (!existsByDeviceCode(deviceCode)) {
                String plcAddress = String.format("%s%d", plcPrefix, startAddress + (i - 1) * 2);
                
                Device device = new Device();
                device.setDeviceCode(deviceCode);
                device.setDeviceName(deviceType + "设备" + String.format("%02d", i));
                device.setDeviceType(deviceType);
                device.setPlcAddress(plcAddress);
                device.setStatus("idle");
                device.setCreatedTime(LocalDateTime.now());
                device.setUpdatedTime(LocalDateTime.now());
                
                deviceRepository.save(device);
            }
        }
    }

    @Override
    public Map<String, Object> getDeviceDetailInfo(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device == null) {
            return null;
        }
        
        Map<String, Object> info = new HashMap<>();
        info.put("device", device);
        info.put("hasActiveWorkOrder", workOrderRepository.hasActiveWorkOrderForDevice(deviceId));
        info.put("hasActiveMaintenance", maintenanceRecordRepository.hasActiveMaintenanceForDevice(deviceId));
        
        return info;
    }

    @Override
    public List<Map<String, Object>> getAllDevicesStatusOverview() {
        List<Device> devices = deviceRepository.findAll();
        return devices.stream()
                .map(this::buildDeviceStatusOverview)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getDevicesStatusOverviewByType(String deviceType) {
        List<Device> devices = deviceRepository.findByDeviceType(deviceType);
        return devices.stream()
                .map(this::buildDeviceStatusOverview)
                .collect(Collectors.toList());
    }
    
    private Map<String, Object> buildDeviceStatusOverview(Device device) {
        Map<String, Object> overview = new HashMap<>();
        overview.put("id", device.getId());
        overview.put("deviceCode", device.getDeviceCode());
        overview.put("deviceName", device.getDeviceName());
        overview.put("deviceType", device.getDeviceType());
        overview.put("status", device.getStatus());
        overview.put("plcAddress", device.getPlcAddress());
        
        // 添加业务状态信息
        if ("working".equals(device.getStatus())) {
            // 查找当前派工单信息
            workOrderRepository.findActiveWorkOrderByDeviceId(device.getId())
                    .ifPresent(workOrder -> {
                        overview.put("workOrderCode", workOrder.getWorkOrderCode());
                        overview.put("plannedQuantity", workOrder.getPlannedQuantity());
                        overview.put("actualQuantity", workOrder.getActualQuantity());
                        overview.put("startTime", workOrder.getStartTime());
                    });
        } else if ("maintenance".equals(device.getStatus())) {
            // 查找当前维修记录信息
            maintenanceRecordRepository.findActiveMaintenanceByDeviceId(device.getId())
                    .ifPresent(maintenance -> {
                        overview.put("maintenanceDescription", maintenance.getDescription());
                        overview.put("reportTime", maintenance.getReportTime());
                        overview.put("maintainer", maintenance.getMaintainer());
                    });
        }
        
        return overview;
    }

    @Override
    public boolean checkDeviceCodeConflict(String deviceCode, Long excludeId) {
        return deviceRepository.checkDeviceCodeConflict(deviceCode, excludeId);
    }

    @Override
    public boolean checkPlcAddressConflict(String plcAddress, Long excludeId) {
        return deviceRepository.checkPlcAddressConflict(plcAddress, excludeId);
    }

    @Override
    public List<String> getDeviceTypes() {
        return new ArrayList<>(DEVICE_TYPES);
    }

    @Override
    public List<String> getDeviceStatuses() {
        return new ArrayList<>(DEVICE_STATUSES);
    }

    @Override
    @Transactional
    public int resetAllDevicesToIdle() {
        return deviceRepository.batchSetDeviceStatus("idle");
    }

    @Override
    public Map<String, Long> getDeviceRunningTimeStatistics(String deviceCode, int days) {
        // 简单实现，实际项目中可能需要更复杂的统计逻辑
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalMinutes", 0L);
        stats.put("workingMinutes", 0L);
        stats.put("idleMinutes", 0L);
        stats.put("maintenanceMinutes", 0L);
        return stats;
    }
}