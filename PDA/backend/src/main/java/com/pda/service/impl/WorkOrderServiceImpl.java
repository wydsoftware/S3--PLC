package com.pda.service.impl;

import com.pda.entity.Device;
import com.pda.entity.WorkOrder;
import com.pda.repository.DeviceRepository;
import com.pda.repository.WorkOrderRepository;
import com.pda.service.DeviceService;
import com.pda.service.WorkOrderService;
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
 * 派工单服务实现类
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;
    
    // 派工单号格式验证正则表达式
    private static final Pattern WORK_ORDER_CODE_PATTERN = Pattern.compile("^WO\\d{8}-\\d{3}$");
    
    // 派工单状态列表
    private static final List<String> WORK_ORDER_STATUSES = Arrays.asList("pending", "in_progress", "completed", "cancelled");

    @Override
    @Transactional
    public WorkOrder saveWorkOrder(WorkOrder workOrder) {
        if (workOrder.getId() == null) {
            workOrder.setCreatedTime(LocalDateTime.now());
        }
        workOrder.setUpdatedTime(LocalDateTime.now());
        return workOrderRepository.save(workOrder);
    }

    @Override
    public Optional<WorkOrder> findById(Long id) {
        return workOrderRepository.findById(id);
    }

    @Override
    public Optional<WorkOrder> findByWorkOrderCode(String workOrderCode) {
        return workOrderRepository.findByWorkOrderCode(workOrderCode);
    }

    @Override
    public boolean existsByWorkOrderCode(String workOrderCode) {
        return workOrderRepository.existsByWorkOrderCode(workOrderCode);
    }

    @Override
    @Transactional
    public WorkOrder createWorkOrder(String workOrderCode, Long deviceId, Integer plannedQuantity) {
        if (existsByWorkOrderCode(workOrderCode)) {
            throw new IllegalArgumentException("派工单号已存在: " + workOrderCode);
        }
        
        if (!validateWorkOrderCodeFormat(workOrderCode)) {
            throw new IllegalArgumentException("派工单号格式不正确: " + workOrderCode);
        }
        
        if (!validateDeviceForProduction(deviceId)) {
            throw new IllegalArgumentException("设备不可用于生产");
        }
        
        if (plannedQuantity == null || plannedQuantity <= 0) {
            throw new IllegalArgumentException("计划产量必须大于0");
        }
        
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + deviceId));
        
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWorkOrderCode(workOrderCode);
        workOrder.setDeviceId(deviceId);
        workOrder.setDeviceCode(device.getDeviceCode());
        workOrder.setPlannedQuantity(plannedQuantity);
        workOrder.setActualQuantity(0);
        workOrder.setStatus("pending");
        
        return saveWorkOrder(workOrder);
    }

    @Override
    @Transactional
    public WorkOrder updateWorkOrder(Long id, Integer plannedQuantity, String status) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("派工单不存在: " + id));
        
        if (plannedQuantity != null && plannedQuantity > 0) {
            workOrder.setPlannedQuantity(plannedQuantity);
        }
        
        if (StringUtils.hasText(status) && WORK_ORDER_STATUSES.contains(status)) {
            workOrder.setStatus(status);
        }
        
        workOrder.setUpdatedTime(LocalDateTime.now());
        return workOrderRepository.save(workOrder);
    }

    @Override
    @Transactional
    public boolean deleteWorkOrder(Long id) {
        WorkOrder workOrder = workOrderRepository.findById(id).orElse(null);
        if (workOrder == null) {
            return false;
        }
        
        // 只能删除待开始或已完成的派工单
        if ("in_progress".equals(workOrder.getStatus())) {
            throw new IllegalStateException("不能删除正在进行的派工单");
        }
        
        workOrderRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public boolean startProduction(Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId).orElse(null);
        if (workOrder == null) {
            return false;
        }
        
        return startProductionInternal(workOrder);
    }

    @Override
    @Transactional
    public boolean startProduction(String workOrderCode) {
        WorkOrder workOrder = workOrderRepository.findByWorkOrderCode(workOrderCode).orElse(null);
        if (workOrder == null) {
            return false;
        }
        
        return startProductionInternal(workOrder);
    }
    
    private boolean startProductionInternal(WorkOrder workOrder) {
        // 检查派工单状态
        if (!"pending".equals(workOrder.getStatus())) {
            throw new IllegalStateException("只能开始待开始状态的派工单");
        }
        
        // 检查设备是否可用
        if (!validateDeviceForProduction(workOrder.getDeviceId())) {
            throw new IllegalStateException("设备不可用于生产");
        }
        
        // 更新派工单状态
        workOrder.setStatus("in_progress");
        workOrder.setStartTime(LocalDateTime.now());
        workOrder.setUpdatedTime(LocalDateTime.now());
        workOrderRepository.save(workOrder);
        
        // 更新设备状态为工作中
        deviceService.setDeviceWorking(workOrder.getDeviceId());
        
        return true;
    }

    @Override
    @Transactional
    public boolean stopProduction(Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId).orElse(null);
        if (workOrder == null) {
            return false;
        }
        
        return stopProductionInternal(workOrder);
    }

    @Override
    @Transactional
    public boolean stopProduction(String workOrderCode) {
        WorkOrder workOrder = workOrderRepository.findByWorkOrderCode(workOrderCode).orElse(null);
        if (workOrder == null) {
            return false;
        }
        
        return stopProductionInternal(workOrder);
    }
    
    private boolean stopProductionInternal(WorkOrder workOrder) {
        // 检查派工单状态
        if (!"in_progress".equals(workOrder.getStatus())) {
            throw new IllegalStateException("只能停止正在进行的派工单");
        }
        
        // 更新派工单状态
        workOrder.setStatus("completed");
        workOrder.setEndTime(LocalDateTime.now());
        workOrder.setUpdatedTime(LocalDateTime.now());
        workOrderRepository.save(workOrder);
        
        // 更新设备状态为空闲
        deviceService.setDeviceIdle(workOrder.getDeviceId());
        
        return true;
    }

    @Override
    @Transactional
    public boolean completeProduction(Long workOrderId, Integer actualQuantity) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId).orElse(null);
        if (workOrder == null) {
            return false;
        }
        
        return completeProductionInternal(workOrder, actualQuantity);
    }

    @Override
    @Transactional
    public boolean completeProduction(String workOrderCode, Integer actualQuantity) {
        WorkOrder workOrder = workOrderRepository.findByWorkOrderCode(workOrderCode).orElse(null);
        if (workOrder == null) {
            return false;
        }
        
        return completeProductionInternal(workOrder, actualQuantity);
    }
    
    private boolean completeProductionInternal(WorkOrder workOrder, Integer actualQuantity) {
        // 检查派工单状态
        if (!"in_progress".equals(workOrder.getStatus())) {
            throw new IllegalStateException("只能完成正在进行的派工单");
        }
        
        if (actualQuantity != null && actualQuantity >= 0) {
            workOrder.setActualQuantity(actualQuantity);
        }
        
        // 更新派工单状态
        workOrder.setStatus("completed");
        workOrder.setEndTime(LocalDateTime.now());
        workOrder.setUpdatedTime(LocalDateTime.now());
        workOrderRepository.save(workOrder);
        
        // 更新设备状态为空闲
        deviceService.setDeviceIdle(workOrder.getDeviceId());
        
        return true;
    }

    @Override
    @Transactional
    public boolean updateActualQuantity(Long workOrderId, Integer actualQuantity) {
        if (actualQuantity == null || actualQuantity < 0) {
            return false;
        }
        
        return workOrderRepository.updateActualQuantity(workOrderId, actualQuantity) > 0;
    }

    @Override
    public List<WorkOrder> findAllWorkOrders() {
        return workOrderRepository.findAll();
    }

    @Override
    public List<WorkOrder> findByStatus(String status) {
        return workOrderRepository.findByStatus(status);
    }

    @Override
    public List<WorkOrder> findByDeviceId(Long deviceId) {
        return workOrderRepository.findByDeviceId(deviceId);
    }

    @Override
    public List<WorkOrder> findByDeviceCode(String deviceCode) {
        return workOrderRepository.findByDeviceCode(deviceCode);
    }

    @Override
    public List<WorkOrder> getPendingWorkOrders() {
        return workOrderRepository.findByStatus("pending");
    }

    @Override
    public List<WorkOrder> getInProgressWorkOrders() {
        return workOrderRepository.findByStatus("in_progress");
    }

    @Override
    public List<WorkOrder> getCompletedWorkOrders() {
        return workOrderRepository.findByStatus("completed");
    }

    @Override
    public List<Map<String, Object>> getInProgressWorkOrdersWithDeviceInfo() {
        List<Object[]> results = workOrderRepository.findInProgressWorkOrdersWithDeviceInfo();
        return results.stream()
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("workOrderId", result[0]);
                    item.put("workOrderCode", result[1]);
                    item.put("deviceCode", result[2]);
                    item.put("deviceName", result[3]);
                    item.put("deviceType", result[4]);
                    item.put("plannedQuantity", result[5]);
                    item.put("actualQuantity", result[6]);
                    item.put("startTime", result[7]);
                    
                    // 计算完成率
                    Integer planned = (Integer) result[5];
                    Integer actual = (Integer) result[6];
                    if (planned != null && planned > 0 && actual != null) {
                        double completionRate = (double) actual / planned * 100;
                        item.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
                    } else {
                        item.put("completionRate", 0.0);
                    }
                    
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<WorkOrder> findWorkOrdersWithConditions(String workOrderCode, String deviceCode, 
                                                       String status, LocalDateTime startTime, 
                                                       LocalDateTime endTime, Pageable pageable) {
        return workOrderRepository.findWorkOrdersWithConditions(workOrderCode, deviceCode, status, startTime, endTime, pageable);
    }

    @Override
    public List<WorkOrder> searchWorkOrders(String keyword) {
        return workOrderRepository.searchWorkOrdersByKeyword(keyword);
    }

    @Override
    public Page<WorkOrder> searchWorkOrders(String keyword, Pageable pageable) {
        return workOrderRepository.searchWorkOrdersByKeyword(keyword, pageable);
    }

    @Override
    public boolean hasActiveWorkOrderForDevice(Long deviceId) {
        return workOrderRepository.hasActiveWorkOrderForDevice(deviceId);
    }

    @Override
    public boolean hasActiveWorkOrderForDevice(String deviceCode) {
        return workOrderRepository.hasActiveWorkOrderForDeviceCode(deviceCode);
    }

    @Override
    public Optional<WorkOrder> getActiveWorkOrderForDevice(Long deviceId) {
        return workOrderRepository.findActiveWorkOrderByDeviceId(deviceId);
    }

    @Override
    public Optional<WorkOrder> getActiveWorkOrderForDevice(String deviceCode) {
        return workOrderRepository.findActiveWorkOrderByDeviceCode(deviceCode);
    }

    @Override
    @Transactional
    public int batchUpdateWorkOrderStatus(List<Long> workOrderIds, String status) {
        if (!WORK_ORDER_STATUSES.contains(status)) {
            throw new IllegalArgumentException("不支持的派工单状态: " + status);
        }
        return workOrderRepository.batchUpdateWorkOrderStatus(workOrderIds, status);
    }

    @Override
    @Transactional
    public int batchStartProduction(List<Long> workOrderIds) {
        return workOrderRepository.batchStartProduction(workOrderIds);
    }

    @Override
    @Transactional
    public int batchStopProduction(List<Long> workOrderIds) {
        return workOrderRepository.batchStopProduction(workOrderIds);
    }

    @Override
    public Map<String, Long> getWorkOrderStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", workOrderRepository.countAllWorkOrders());
        stats.put("pending", workOrderRepository.countByStatus("pending"));
        stats.put("inProgress", workOrderRepository.countByStatus("in_progress"));
        stats.put("completed", workOrderRepository.countByStatus("completed"));
        stats.put("cancelled", workOrderRepository.countByStatus("cancelled"));
        return stats;
    }

    @Override
    public Map<String, Long> getWorkOrderStatusDistribution() {
        List<Object[]> results = workOrderRepository.getWorkOrderStatusDistribution();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Override
    public Map<String, Long> getDeviceTypeProductionStatistics() {
        List<Object[]> results = workOrderRepository.getDeviceTypeProductionStatistics();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Override
    public List<Map<String, Object>> getMonthlyProductionReport(int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        List<Object[]> results = workOrderRepository.getMonthlyProductionReport(startDate);
        
        return results.stream()
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("month", result[0]);
                    item.put("totalOrders", result[1]);
                    item.put("completedOrders", result[2]);
                    item.put("totalPlanned", result[3]);
                    item.put("totalActual", result[4]);
                    
                    // 计算完成率
                    Long totalPlanned = (Long) result[3];
                    Long totalActual = (Long) result[4];
                    if (totalPlanned != null && totalPlanned > 0 && totalActual != null) {
                        double completionRate = (double) totalActual / totalPlanned * 100;
                        item.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
                    } else {
                        item.put("completionRate", 0.0);
                    }
                    
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getDailyProductionStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = workOrderRepository.getDailyProductionStatistics(startDate, endDate);
        
        return results.stream()
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", result[0]);
                    item.put("totalOrders", result[1]);
                    item.put("completedOrders", result[2]);
                    item.put("totalPlanned", result[3]);
                    item.put("totalActual", result[4]);
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getDeviceProductionStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = workOrderRepository.getDeviceProductionStatistics(startDate, endDate);
        
        return results.stream()
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("deviceCode", result[0]);
                    item.put("deviceType", result[1]);
                    item.put("totalOrders", result[2]);
                    item.put("completedOrders", result[3]);
                    item.put("totalPlanned", result[4]);
                    item.put("totalActual", result[5]);
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Double> getDeviceUtilizationStatistics(int days) {
        List<Object[]> results = workOrderRepository.getDeviceUtilizationStatistics(days);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> ((Number) result[1]).doubleValue()
                ));
    }

    @Override
    public List<WorkOrder> getRecentWorkOrders(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return workOrderRepository.findRecentWorkOrders(pageable);
    }

    @Override
    public List<WorkOrder> findWorkOrdersByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        return workOrderRepository.findWorkOrdersByCreatedTimeBetween(startTime, endTime);
    }

    @Override
    public Map<String, Long> getWorkOrderCreationTimeStatistics(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        List<Object[]> results = workOrderRepository.getWorkOrderCreationTimeStatistics(startTime);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1],
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    @Override
    public boolean validateWorkOrderCodeFormat(String workOrderCode) {
        return StringUtils.hasText(workOrderCode) && WORK_ORDER_CODE_PATTERN.matcher(workOrderCode).matches();
    }

    @Override
    public String generateWorkOrderCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 查找当天的最大序号
        String prefix = "WO" + dateStr + "-";
        List<String> todayCodes = workOrderRepository.findWorkOrderCodesByPrefix(prefix);
        
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
    public boolean checkWorkOrderCodeConflict(String workOrderCode, Long excludeId) {
        return workOrderRepository.checkWorkOrderCodeConflict(workOrderCode, excludeId);
    }

    @Override
    public boolean validateDeviceForProduction(Long deviceId) {
        return deviceService.isDeviceAvailableForProduction(deviceId);
    }

    @Override
    public boolean validateDeviceForProduction(String deviceCode) {
        return deviceService.isDeviceAvailableForProduction(deviceCode);
    }

    @Override
    public Map<String, Object> getWorkOrderDetailInfo(Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId).orElse(null);
        if (workOrder == null) {
            return null;
        }
        
        Map<String, Object> info = new HashMap<>();
        info.put("workOrder", workOrder);
        
        // 添加设备信息
        deviceRepository.findById(workOrder.getDeviceId())
                .ifPresent(device -> info.put("device", device));
        
        // 添加进度信息
        info.put("progress", getWorkOrderProgress(workOrderId));
        
        return info;
    }

    @Override
    public Map<String, Object> getWorkOrderProgress(Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId).orElse(null);
        if (workOrder == null) {
            return null;
        }
        
        Map<String, Object> progress = new HashMap<>();
        progress.put("completionRate", calculateCompletionRate(workOrderId));
        progress.put("productionDuration", calculateProductionDuration(workOrderId));
        progress.put("status", workOrder.getStatus());
        progress.put("plannedQuantity", workOrder.getPlannedQuantity());
        progress.put("actualQuantity", workOrder.getActualQuantity());
        
        return progress;
    }

    @Override
    public Double calculateCompletionRate(Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId).orElse(null);
        if (workOrder == null || workOrder.getPlannedQuantity() == null || workOrder.getPlannedQuantity() == 0) {
            return 0.0;
        }
        
        Integer actualQuantity = workOrder.getActualQuantity() != null ? workOrder.getActualQuantity() : 0;
        return (double) actualQuantity / workOrder.getPlannedQuantity() * 100;
    }

    @Override
    public Long calculateProductionDuration(Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId).orElse(null);
        if (workOrder == null || workOrder.getStartTime() == null) {
            return 0L;
        }
        
        LocalDateTime endTime = workOrder.getEndTime() != null ? workOrder.getEndTime() : LocalDateTime.now();
        return ChronoUnit.MINUTES.between(workOrder.getStartTime(), endTime);
    }

    @Override
    public Map<String, Object> getProductionEfficiencyStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        List<WorkOrder> workOrders = workOrderRepository.findWorkOrdersByCreatedTimeBetween(startDate, endDate);
        
        long totalOrders = workOrders.size();
        long completedOrders = workOrders.stream()
                .filter(wo -> "completed".equals(wo.getStatus()))
                .count();
        
        int totalPlanned = workOrders.stream()
                .mapToInt(wo -> wo.getPlannedQuantity() != null ? wo.getPlannedQuantity() : 0)
                .sum();
        
        int totalActual = workOrders.stream()
                .mapToInt(wo -> wo.getActualQuantity() != null ? wo.getActualQuantity() : 0)
                .sum();
        
        stats.put("totalOrders", totalOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("completionRate", totalOrders > 0 ? (double) completedOrders / totalOrders * 100 : 0.0);
        stats.put("totalPlanned", totalPlanned);
        stats.put("totalActual", totalActual);
        stats.put("productionRate", totalPlanned > 0 ? (double) totalActual / totalPlanned * 100 : 0.0);
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getDeviceProductionRanking(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Object[]> results = workOrderRepository.getDeviceProductionStatistics(startDate, LocalDateTime.now());
        
        return results.stream()
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("deviceCode", result[0]);
                    item.put("deviceType", result[1]);
                    item.put("totalOrders", result[2]);
                    item.put("completedOrders", result[3]);
                    item.put("totalPlanned", result[4]);
                    item.put("totalActual", result[5]);
                    return item;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("totalActual"), (Long) a.get("totalActual")))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getWorkOrderStatuses() {
        return new ArrayList<>(WORK_ORDER_STATUSES);
    }

    @Override
    @Transactional
    public int resetAllWorkOrdersStatus() {
        // 将所有进行中的派工单设置为已完成
        return workOrderRepository.batchUpdateWorkOrderStatus(
                workOrderRepository.findByStatus("in_progress").stream()
                        .map(WorkOrder::getId)
                        .collect(Collectors.toList()),
                "completed"
        );
    }

    @Override
    public List<WorkOrder> getOverdueWorkOrders(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        return workOrderRepository.findOverdueWorkOrders(cutoffTime);
    }

    @Override
    public List<WorkOrder> getLongRunningWorkOrders(int hours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        return workOrderRepository.findLongRunningWorkOrders(cutoffTime);
    }

    @Override
    @Transactional
    public boolean syncActualQuantityFromPLC(Long workOrderId) {
        // TODO: 实现PLC数据同步逻辑
        // 这里应该调用PLC服务获取实际产量数据
        log.info("同步PLC数据 - 派工单ID: {}", workOrderId);
        return true;
    }

    @Override
    @Transactional
    public int batchSyncActualQuantityFromPLC() {
        // TODO: 实现批量PLC数据同步逻辑
        List<WorkOrder> inProgressOrders = getInProgressWorkOrders();
        int syncCount = 0;
        
        for (WorkOrder workOrder : inProgressOrders) {
            if (syncActualQuantityFromPLC(workOrder.getId())) {
                syncCount++;
            }
        }
        
        log.info("批量同步PLC数据完成，同步数量: {}", syncCount);
        return syncCount;
    }
}