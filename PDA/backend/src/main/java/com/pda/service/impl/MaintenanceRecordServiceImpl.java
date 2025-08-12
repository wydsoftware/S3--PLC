package com.pda.service.impl;

import com.pda.entity.Device;
import com.pda.entity.MaintenanceRecord;
import com.pda.repository.DeviceRepository;
import com.pda.repository.MaintenanceRecordRepository;
import com.pda.service.DeviceService;
import com.pda.service.MaintenanceRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 维修记录服务实现类
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceRecordServiceImpl implements MaintenanceRecordService {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;
    
    // 维修单号格式验证正则表达式
    private static final Pattern MAINTENANCE_CODE_PATTERN = Pattern.compile("^MR\\d{8}-\\d{3}$");
    
    // 维修状态列表
    private static final List<String> MAINTENANCE_STATUSES = Arrays.asList("pending", "in_progress", "completed", "cancelled");

    @Override
    @Transactional
    public MaintenanceRecord saveMaintenanceRecord(MaintenanceRecord maintenanceRecord) {
        if (maintenanceRecord.getId() == null) {
            maintenanceRecord.setCreatedTime(LocalDateTime.now());
        }
        maintenanceRecord.setUpdatedTime(LocalDateTime.now());
        return maintenanceRecordRepository.save(maintenanceRecord);
    }

    @Override
    public Optional<MaintenanceRecord> findById(Long id) {
        return maintenanceRecordRepository.findById(id);
    }

    @Override
    public Optional<MaintenanceRecord> findByMaintenanceCode(String maintenanceCode) {
        return maintenanceRecordRepository.findByMaintenanceCode(maintenanceCode);
    }

    @Override
    public boolean existsByMaintenanceCode(String maintenanceCode) {
        return maintenanceRecordRepository.existsByMaintenanceCode(maintenanceCode);
    }

    @Override
    @Transactional
    public MaintenanceRecord createMaintenanceRecord(Long deviceId, String faultDescription, String reportedBy) {
        if (!StringUtils.hasText(faultDescription)) {
            throw new IllegalArgumentException("故障描述不能为空");
        }
        
        if (!StringUtils.hasText(reportedBy)) {
            throw new IllegalArgumentException("报修人不能为空");
        }
        
        if (!validateDeviceForMaintenance(deviceId)) {
            throw new IllegalArgumentException("设备不可报修");
        }
        
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + deviceId));
        
        MaintenanceRecord maintenanceRecord = new MaintenanceRecord();
        maintenanceRecord.setMaintenanceCode(generateMaintenanceCode());
        maintenanceRecord.setDeviceId(deviceId);
        maintenanceRecord.setDeviceCode(device.getDeviceCode());
        maintenanceRecord.setFaultDescription(faultDescription);
        maintenanceRecord.setReportedBy(reportedBy);
        maintenanceRecord.setStatus("pending");
        
        MaintenanceRecord savedRecord = saveMaintenanceRecord(maintenanceRecord);
        
        // 设置设备为维修状态
        deviceService.setDeviceMaintenance(deviceId);
        
        return savedRecord;
    }

    @Override
    @Transactional
    public MaintenanceRecord updateMaintenanceRecord(Long id, String faultDescription, String repairDescription, String status) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("维修记录不存在: " + id));
        
        if (StringUtils.hasText(faultDescription)) {
            maintenanceRecord.setFaultDescription(faultDescription);
        }
        
        if (StringUtils.hasText(repairDescription)) {
            maintenanceRecord.setRepairDescription(repairDescription);
        }
        
        if (StringUtils.hasText(status) && MAINTENANCE_STATUSES.contains(status)) {
            maintenanceRecord.setStatus(status);
        }
        
        maintenanceRecord.setUpdatedTime(LocalDateTime.now());
        return maintenanceRecordRepository.save(maintenanceRecord);
    }

    @Override
    @Transactional
    public boolean deleteMaintenanceRecord(Long id) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findById(id).orElse(null);
        if (maintenanceRecord == null) {
            return false;
        }
        
        // 只能删除待处理或已完成的维修记录
        if ("in_progress".equals(maintenanceRecord.getStatus())) {
            throw new IllegalStateException("不能删除正在进行的维修记录");
        }
        
        maintenanceRecordRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public boolean startMaintenance(Long maintenanceId, String repairedBy) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findById(maintenanceId).orElse(null);
        if (maintenanceRecord == null) {
            return false;
        }
        
        return startMaintenanceInternal(maintenanceRecord, repairedBy);
    }

    @Override
    @Transactional
    public boolean startMaintenance(String maintenanceCode, String repairedBy) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findByMaintenanceCode(maintenanceCode).orElse(null);
        if (maintenanceRecord == null) {
            return false;
        }
        
        return startMaintenanceInternal(maintenanceRecord, repairedBy);
    }
    
    private boolean startMaintenanceInternal(MaintenanceRecord maintenanceRecord, String repairedBy) {
        // 检查维修记录状态
        if (!"pending".equals(maintenanceRecord.getStatus())) {
            throw new IllegalStateException("只能开始待处理状态的维修记录");
        }
        
        if (!StringUtils.hasText(repairedBy)) {
            throw new IllegalArgumentException("维修人员不能为空");
        }
        
        // 更新维修记录状态
        maintenanceRecord.setStatus("in_progress");
        maintenanceRecord.setRepairedBy(repairedBy);
        maintenanceRecord.setStartTime(LocalDateTime.now());
        maintenanceRecord.setUpdatedTime(LocalDateTime.now());
        maintenanceRecordRepository.save(maintenanceRecord);
        
        return true;
    }

    @Override
    @Transactional
    public boolean completeMaintenance(Long maintenanceId, String repairDescription) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findById(maintenanceId).orElse(null);
        if (maintenanceRecord == null) {
            return false;
        }
        
        return completeMaintenanceInternal(maintenanceRecord, repairDescription);
    }

    @Override
    @Transactional
    public boolean completeMaintenance(String maintenanceCode, String repairDescription) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findByMaintenanceCode(maintenanceCode).orElse(null);
        if (maintenanceRecord == null) {
            return false;
        }
        
        return completeMaintenanceInternal(maintenanceRecord, repairDescription);
    }
    
    private boolean completeMaintenanceInternal(MaintenanceRecord maintenanceRecord, String repairDescription) {
        // 检查维修记录状态
        if (!"in_progress".equals(maintenanceRecord.getStatus())) {
            throw new IllegalStateException("只能完成正在进行的维修记录");
        }
        
        if (StringUtils.hasText(repairDescription)) {
            maintenanceRecord.setRepairDescription(repairDescription);
        }
        
        // 更新维修记录状态
        maintenanceRecord.setStatus("completed");
        maintenanceRecord.setEndTime(LocalDateTime.now());
        maintenanceRecord.setUpdatedTime(LocalDateTime.now());
        maintenanceRecordRepository.save(maintenanceRecord);
        
        // 设置设备为空闲状态
        deviceService.setDeviceIdle(maintenanceRecord.getDeviceId());
        
        return true;
    }

    @Override
    @Transactional
    public boolean cancelMaintenance(Long maintenanceId, String cancelReason) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findById(maintenanceId).orElse(null);
        if (maintenanceRecord == null) {
            return false;
        }
        
        // 检查维修记录状态
        if ("completed".equals(maintenanceRecord.getStatus()) || "cancelled".equals(maintenanceRecord.getStatus())) {
            throw new IllegalStateException("不能取消已完成或已取消的维修记录");
        }
        
        // 更新维修记录状态
        maintenanceRecord.setStatus("cancelled");
        maintenanceRecord.setRepairDescription(cancelReason);
        maintenanceRecord.setEndTime(LocalDateTime.now());
        maintenanceRecord.setUpdatedTime(LocalDateTime.now());
        maintenanceRecordRepository.save(maintenanceRecord);
        
        // 设置设备为空闲状态
        deviceService.setDeviceIdle(maintenanceRecord.getDeviceId());
        
        return true;
    }

    @Override
    public List<MaintenanceRecord> findAllMaintenanceRecords() {
        return maintenanceRecordRepository.findAll();
    }

    @Override
    public List<MaintenanceRecord> findByStatus(String status) {
        return maintenanceRecordRepository.findByStatus(status);
    }

    @Override
    public List<MaintenanceRecord> findByDeviceId(Long deviceId) {
        return maintenanceRecordRepository.findByDeviceId(deviceId);
    }

    @Override
    public List<MaintenanceRecord> findByDeviceCode(String deviceCode) {
        return maintenanceRecordRepository.findByDeviceCode(deviceCode);
    }

    @Override
    public List<MaintenanceRecord> getPendingMaintenanceRecords() {
        return maintenanceRecordRepository.findByStatus("pending");
    }

    @Override
    public List<MaintenanceRecord> getInProgressMaintenanceRecords() {
        return maintenanceRecordRepository.findByStatus("in_progress");
    }

    @Override
    public List<MaintenanceRecord> getCompletedMaintenanceRecords() {
        return maintenanceRecordRepository.findByStatus("completed");
    }

    @Override
    public List<Map<String, Object>> getInProgressMaintenanceRecordsWithDeviceInfo() {
        List<Object[]> results = maintenanceRecordRepository.findInProgressMaintenanceRecordsWithDeviceInfo();
        return results.stream()
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("maintenanceId", result[0]);
                    item.put("maintenanceCode", result[1]);
                    item.put("deviceCode", result[2]);
                    item.put("deviceName", result[3]);
                    item.put("deviceType", result[4]);
                    item.put("faultDescription", result[5]);
                    item.put("reportedBy", result[6]);
                    item.put("repairedBy", result[7]);
                    item.put("startTime", result[8]);
                    
                    // 计算维修持续时间
                    LocalDateTime startTime = (LocalDateTime) result[8];
                    if (startTime != null) {
                        long duration = ChronoUnit.MINUTES.between(startTime, LocalDateTime.now());
                        item.put("duration", duration);
                    } else {
                        item.put("duration", 0L);
                    }
                    
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<MaintenanceRecord> findMaintenanceRecordsWithConditions(String maintenanceCode, String deviceCode, 
                                                                       String status, LocalDateTime startTime, 
                                                                       LocalDateTime endTime, Pageable pageable) {
        return maintenanceRecordRepository.findMaintenanceRecordsWithConditions(maintenanceCode, deviceCode, status, startTime, endTime, pageable);
    }

    @Override
    public List<MaintenanceRecord> searchMaintenanceRecords(String keyword) {
        return maintenanceRecordRepository.searchMaintenanceRecordsByKeyword(keyword);
    }

    @Override
    public Page<MaintenanceRecord> searchMaintenanceRecords(String keyword, Pageable pageable) {
        return maintenanceRecordRepository.searchMaintenanceRecordsByKeyword(keyword, pageable);
    }

    @Override
    public boolean hasActiveMaintenanceForDevice(Long deviceId) {
        return maintenanceRecordRepository.hasActiveMaintenanceForDevice(deviceId);
    }

    @Override
    public boolean hasActiveMaintenanceForDevice(String deviceCode) {
        return maintenanceRecordRepository.hasActiveMaintenanceForDeviceCode(deviceCode);
    }

    @Override
    public Optional<MaintenanceRecord> getActiveMaintenanceForDevice(Long deviceId) {
        return maintenanceRecordRepository.findActiveMaintenanceByDeviceId(deviceId);
    }

    @Override
    public Optional<MaintenanceRecord> getActiveMaintenanceForDevice(String deviceCode) {
        return maintenanceRecordRepository.findActiveMaintenanceByDeviceCode(deviceCode);
    }

    @Override
    @Transactional
    public int batchUpdateMaintenanceStatus(List<Long> maintenanceIds, String status) {
        if (!MAINTENANCE_STATUSES.contains(status)) {
            throw new IllegalArgumentException("不支持的维修状态: " + status);
        }
        return maintenanceRecordRepository.batchUpdateMaintenanceStatus(maintenanceIds, status);
    }

    @Override
    @Transactional
    public int batchStartMaintenance(List<Long> maintenanceIds, String repairedBy) {
        if (!StringUtils.hasText(repairedBy)) {
            throw new IllegalArgumentException("维修人员不能为空");
        }
        return maintenanceRecordRepository.batchStartMaintenance(maintenanceIds, repairedBy);
    }

    @Override
    @Transactional
    public int batchCompleteMaintenance(List<Long> maintenanceIds) {
        return maintenanceRecordRepository.batchCompleteMaintenance(maintenanceIds);
    }

    @Override
    public Map<String, Long> getMaintenanceStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", maintenanceRecordRepository.countAllMaintenanceRecords());
        stats.put("pending", maintenanceRecordRepository.countByStatus("pending"));
        stats.put("inProgress", maintenanceRecordRepository.countByStatus("in_progress"));
        stats.put("completed", maintenanceRecordRepository.countByStatus("completed"));
        stats.put("cancelled", maintenanceRecordRepository.countByStatus("cancelled"));
        return stats;
    }

    @Override
    public Map<String, Long> getMaintenanceStatusDistribution() {
        List<Object[]> results = maintenanceRecordRepository.getMaintenanceStatusDistribution();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Override
    public Map<String, Long> getDeviceTypeMaintenanceStatistics() {
        List<Object[]> results = maintenanceRecordRepository.getDeviceTypeMaintenanceStatistics();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Override
    public List<Map<String, Object>> getMonthlyMaintenanceReport(int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        List<Object[]> results = maintenanceRecordRepository.getMonthlyMaintenanceReport(startDate);
        
        return results.stream()
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("month", result[0]);
                    item.put("totalRecords", result[1]);
                    item.put("completedRecords", result[2]);
                    item.put("averageDuration", result[3]);
                    
                    // 计算完成率
                    Long totalRecords = (Long) result[1];
                    Long completedRecords = (Long) result[2];
                    if (totalRecords != null && totalRecords > 0 && completedRecords != null) {
                        double completionRate = (double) completedRecords / totalRecords * 100;
                        item.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
                    } else {
                        item.put("completionRate", 0.0);
                    }
                    
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getDailyMaintenanceStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = maintenanceRecordRepository.getDailyMaintenanceStatistics(startDate, endDate);
        
        return results.stream()
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", result[0]);
                    item.put("totalRecords", result[1]);
                    item.put("completedRecords", result[2]);
                    item.put("averageDuration", result[3]);
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getDeviceMaintenanceStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = maintenanceRecordRepository.getDeviceMaintenanceStatistics(startDate, endDate);
        
        return results.stream()
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("deviceCode", result[0]);
                    item.put("deviceType", result[1]);
                    item.put("totalRecords", result[2]);
                    item.put("completedRecords", result[3]);
                    item.put("averageDuration", result[4]);
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Double> getDeviceFailureRateStatistics(int days) {
        List<Object[]> results = maintenanceRecordRepository.getDeviceFailureRateStatistics(days);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> ((Number) result[1]).doubleValue()
                ));
    }

    @Override
    public List<MaintenanceRecord> getRecentMaintenanceRecords(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return maintenanceRecordRepository.findRecentMaintenanceRecords(pageable);
    }

    @Override
    public List<MaintenanceRecord> findMaintenanceRecordsByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        return maintenanceRecordRepository.findMaintenanceRecordsByCreatedTimeBetween(startTime, endTime);
    }

    @Override
    public Map<String, Long> getMaintenanceCreationTimeStatistics(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        List<Object[]> results = maintenanceRecordRepository.getMaintenanceCreationTimeStatistics(startTime);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1],
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    @Override
    public boolean validateMaintenanceCodeFormat(String maintenanceCode) {
        return StringUtils.hasText(maintenanceCode) && MAINTENANCE_CODE_PATTERN.matcher(maintenanceCode).matches();
    }

    @Override
    public String generateMaintenanceCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 查找当天的最大序号
        String prefix = "MR" + dateStr + "-";
        List<String> todayCodes = maintenanceRecordRepository.findMaintenanceCodesByPrefix(prefix);
        
        int maxSequence = 0;
        for (String code : todayCodes) {
            String sequencePart = code.substring(prefix.length());
            try {
                int sequence = Integer.parseInt(sequencePart);
                maxSequence = Math.max(maxSequence, sequence);
            } catch (NumberFormatException e) {
                // 忽略格式错误的编号
            }
        }
        
        return String.format("%s%03d", prefix, maxSequence + 1);
    }

    @Override
    public boolean checkMaintenanceCodeConflict(String maintenanceCode, Long excludeId) {
        return maintenanceRecordRepository.checkMaintenanceCodeConflict(maintenanceCode, excludeId);
    }

    @Override
    public boolean validateDeviceForMaintenance(Long deviceId) {
        // 检查设备是否存在
        if (!deviceRepository.existsById(deviceId)) {
            return false;
        }
        
        // 检查设备是否已有进行中的维修记录
        return !hasActiveMaintenanceForDevice(deviceId);
    }

    @Override
    public boolean validateDeviceForMaintenance(String deviceCode) {
        // 检查设备是否存在
        if (!deviceRepository.existsByDeviceCode(deviceCode)) {
            return false;
        }
        
        // 检查设备是否已有进行中的维修记录
        return !hasActiveMaintenanceForDevice(deviceCode);
    }

    @Override
    public Map<String, Object> getMaintenanceDetailInfo(Long maintenanceId) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findById(maintenanceId).orElse(null);
        if (maintenanceRecord == null) {
            return null;
        }
        
        Map<String, Object> info = new HashMap<>();
        info.put("maintenanceRecord", maintenanceRecord);
        
        // 添加设备信息
        deviceRepository.findById(maintenanceRecord.getDeviceId())
                .ifPresent(device -> info.put("device", device));
        
        // 添加进度信息
        info.put("progress", getMaintenanceProgress(maintenanceId));
        
        return info;
    }

    @Override
    public Map<String, Object> getMaintenanceProgress(Long maintenanceId) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findById(maintenanceId).orElse(null);
        if (maintenanceRecord == null) {
            return null;
        }
        
        Map<String, Object> progress = new HashMap<>();
        progress.put("maintenanceDuration", calculateMaintenanceDuration(maintenanceId));
        progress.put("status", maintenanceRecord.getStatus());
        progress.put("reportedBy", maintenanceRecord.getReportedBy());
        progress.put("repairedBy", maintenanceRecord.getRepairedBy());
        progress.put("faultDescription", maintenanceRecord.getFaultDescription());
        progress.put("repairDescription", maintenanceRecord.getRepairDescription());
        
        return progress;
    }

    @Override
    public Long calculateMaintenanceDuration(Long maintenanceId) {
        MaintenanceRecord maintenanceRecord = maintenanceRecordRepository.findById(maintenanceId).orElse(null);
        if (maintenanceRecord == null || maintenanceRecord.getStartTime() == null) {
            return 0L;
        }
        
        LocalDateTime endTime = maintenanceRecord.getEndTime() != null ? maintenanceRecord.getEndTime() : LocalDateTime.now();
        return ChronoUnit.MINUTES.between(maintenanceRecord.getStartTime(), endTime);
    }

    @Override
    public Map<String, Object> getMaintenanceEfficiencyStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        List<MaintenanceRecord> maintenanceRecords = maintenanceRecordRepository.findMaintenanceRecordsByCreatedTimeBetween(startDate, endDate);
        
        long totalRecords = maintenanceRecords.size();
        long completedRecords = maintenanceRecords.stream()
                .filter(mr -> "completed".equals(mr.getStatus()))
                .count();
        
        // 计算平均维修时间
        double averageDuration = maintenanceRecords.stream()
                .filter(mr -> "completed".equals(mr.getStatus()) && mr.getStartTime() != null && mr.getEndTime() != null)
                .mapToLong(mr -> ChronoUnit.MINUTES.between(mr.getStartTime(), mr.getEndTime()))
                .average()
                .orElse(0.0);
        
        stats.put("totalRecords", totalRecords);
        stats.put("completedRecords", completedRecords);
        stats.put("completionRate", totalRecords > 0 ? (double) completedRecords / totalRecords * 100 : 0.0);
        stats.put("averageDuration", Math.round(averageDuration * 100.0) / 100.0);
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getDeviceMaintenanceRanking(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Object[]> results = maintenanceRecordRepository.getDeviceMaintenanceStatistics(startDate, LocalDateTime.now());
        
        return results.stream()
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("deviceCode", result[0]);
                    item.put("deviceType", result[1]);
                    item.put("totalRecords", result[2]);
                    item.put("completedRecords", result[3]);
                    item.put("averageDuration", result[4]);
                    return item;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("totalRecords"), (Long) a.get("totalRecords")))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getMaintenanceStatuses() {
        return new ArrayList<>(MAINTENANCE_STATUSES);
    }

    @Override
    @Transactional
    public int resetAllMaintenanceStatus() {
        // 将所有进行中的维修记录设置为已完成
        return maintenanceRecordRepository.batchUpdateMaintenanceStatus(
                maintenanceRecordRepository.findByStatus("in_progress").stream()
                        .map(MaintenanceRecord::getId)
                        .collect(Collectors.toList()),
                "completed"
        );
    }

    @Override
    public List<MaintenanceRecord> getOverdueMaintenanceRecords(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        return maintenanceRecordRepository.findOverdueMaintenanceRecords(cutoffTime);
    }

    @Override
    public List<MaintenanceRecord> getLongRunningMaintenanceRecords(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        return maintenanceRecordRepository.findLongRunningMaintenanceRecords(cutoffTime);
    }

    @Override
    public Map<String, Long> getMaintenanceTypeStatistics() {
        // 根据故障描述关键词统计维修类型
        List<MaintenanceRecord> records = maintenanceRecordRepository.findAll();
        Map<String, Long> typeStats = new HashMap<>();
        
        for (MaintenanceRecord record : records) {
            String faultDescription = record.getFaultDescription();
            if (faultDescription != null) {
                String type = categorizeMaintenanceType(faultDescription);
                typeStats.put(type, typeStats.getOrDefault(type, 0L) + 1);
            }
        }
        
        return typeStats;
    }
    
    private String categorizeMaintenanceType(String faultDescription) {
        String description = faultDescription.toLowerCase();
        if (description.contains("电机") || description.contains("motor")) {
            return "电机故障";
        } else if (description.contains("传感器") || description.contains("sensor")) {
            return "传感器故障";
        } else if (description.contains("软件") || description.contains("software")) {
            return "软件故障";
        } else if (description.contains("机械") || description.contains("mechanical")) {
            return "机械故障";
        } else if (description.contains("电气") || description.contains("electrical")) {
            return "电气故障";
        } else {
            return "其他故障";
        }
    }

    @Override
    public Map<String, Long> getMaintenancePersonnelStatistics() {
        List<Object[]> results = maintenanceRecordRepository.getMaintenancePersonnelStatistics();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Override
    public Double getAverageMaintenanceTime(Long deviceId) {
        List<MaintenanceRecord> records = maintenanceRecordRepository.findByDeviceIdAndStatus(deviceId, "completed");
        
        return records.stream()
                .filter(record -> record.getStartTime() != null && record.getEndTime() != null)
                .mapToLong(record -> ChronoUnit.MINUTES.between(record.getStartTime(), record.getEndTime()))
                .average()
                .orElse(0.0);
    }

    @Override
    public List<MaintenanceRecord> getDeviceMaintenanceHistory(Long deviceId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return maintenanceRecordRepository.findByDeviceIdOrderByCreatedTimeDesc(deviceId, pageable);
    }
}