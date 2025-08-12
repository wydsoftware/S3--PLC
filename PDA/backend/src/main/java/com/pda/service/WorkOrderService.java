package com.pda.service;

import com.pda.entity.WorkOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 派工单服务接口
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
public interface WorkOrderService {

    /**
     * 保存派工单
     */
    WorkOrder saveWorkOrder(WorkOrder workOrder);

    /**
     * 根据ID查找派工单
     */
    Optional<WorkOrder> findById(Long id);

    /**
     * 根据派工单号查找派工单
     */
    Optional<WorkOrder> findByWorkOrderCode(String workOrderCode);

    /**
     * 检查派工单号是否存在
     */
    boolean existsByWorkOrderCode(String workOrderCode);

    /**
     * 创建新派工单
     */
    WorkOrder createWorkOrder(String workOrderCode, Long deviceId, Integer plannedQuantity);

    /**
     * 更新派工单信息
     */
    WorkOrder updateWorkOrder(Long id, Integer plannedQuantity, String status);

    /**
     * 删除派工单
     */
    boolean deleteWorkOrder(Long id);

    /**
     * 开始生产
     */
    boolean startProduction(Long workOrderId);

    /**
     * 开始生产（根据派工单号）
     */
    boolean startProduction(String workOrderCode);

    /**
     * 停止生产
     */
    boolean stopProduction(Long workOrderId);

    /**
     * 停止生产（根据派工单号）
     */
    boolean stopProduction(String workOrderCode);

    /**
     * 完成生产
     */
    boolean completeProduction(Long workOrderId, Integer actualQuantity);

    /**
     * 完成生产（根据派工单号）
     */
    boolean completeProduction(String workOrderCode, Integer actualQuantity);

    /**
     * 更新实际产量
     */
    boolean updateActualQuantity(Long workOrderId, Integer actualQuantity);

    /**
     * 获取所有派工单
     */
    List<WorkOrder> findAllWorkOrders();

    /**
     * 根据状态查找派工单
     */
    List<WorkOrder> findByStatus(String status);

    /**
     * 根据设备ID查找派工单
     */
    List<WorkOrder> findByDeviceId(Long deviceId);

    /**
     * 根据设备编号查找派工单
     */
    List<WorkOrder> findByDeviceCode(String deviceCode);

    /**
     * 获取待开始的派工单
     */
    List<WorkOrder> getPendingWorkOrders();

    /**
     * 获取生产中的派工单
     */
    List<WorkOrder> getInProgressWorkOrders();

    /**
     * 获取已完成的派工单
     */
    List<WorkOrder> getCompletedWorkOrders();

    /**
     * 获取正在生产的派工单详情（包含设备信息）
     */
    List<Map<String, Object>> getInProgressWorkOrdersWithDeviceInfo();

    /**
     * 分页查询派工单
     */
    Page<WorkOrder> findWorkOrdersWithConditions(String workOrderCode, String deviceCode, 
                                                 String status, LocalDateTime startTime, 
                                                 LocalDateTime endTime, Pageable pageable);

    /**
     * 搜索派工单
     */
    List<WorkOrder> searchWorkOrders(String keyword);

    /**
     * 分页搜索派工单
     */
    Page<WorkOrder> searchWorkOrders(String keyword, Pageable pageable);

    /**
     * 检查设备是否有进行中的派工单
     */
    boolean hasActiveWorkOrderForDevice(Long deviceId);

    /**
     * 检查设备是否有进行中的派工单（根据设备编号）
     */
    boolean hasActiveWorkOrderForDevice(String deviceCode);

    /**
     * 获取设备当前的派工单
     */
    Optional<WorkOrder> getActiveWorkOrderForDevice(Long deviceId);

    /**
     * 获取设备当前的派工单（根据设备编号）
     */
    Optional<WorkOrder> getActiveWorkOrderForDevice(String deviceCode);

    /**
     * 批量更新派工单状态
     */
    int batchUpdateWorkOrderStatus(List<Long> workOrderIds, String status);

    /**
     * 批量开始生产
     */
    int batchStartProduction(List<Long> workOrderIds);

    /**
     * 批量停止生产
     */
    int batchStopProduction(List<Long> workOrderIds);

    /**
     * 获取派工单统计信息
     */
    Map<String, Long> getWorkOrderStatistics();

    /**
     * 获取派工单状态分布
     */
    Map<String, Long> getWorkOrderStatusDistribution();

    /**
     * 获取设备类型生产统计
     */
    Map<String, Long> getDeviceTypeProductionStatistics();

    /**
     * 获取月度生产报表数据
     */
    List<Map<String, Object>> getMonthlyProductionReport(int months);

    /**
     * 获取生产统计（按日）
     */
    List<Map<String, Object>> getDailyProductionStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取生产统计（按设备）
     */
    List<Map<String, Object>> getDeviceProductionStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取设备利用率统计
     */
    Map<String, Double> getDeviceUtilizationStatistics(int days);

    /**
     * 获取最近的派工单
     */
    List<WorkOrder> getRecentWorkOrders(int limit);

    /**
     * 根据时间范围查询派工单
     */
    List<WorkOrder> findWorkOrdersByDateRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取派工单创建时间统计
     */
    Map<String, Long> getWorkOrderCreationTimeStatistics(int days);

    /**
     * 验证派工单号格式
     */
    boolean validateWorkOrderCodeFormat(String workOrderCode);

    /**
     * 生成派工单号
     */
    String generateWorkOrderCode();

    /**
     * 检查派工单号冲突（排除指定ID）
     */
    boolean checkWorkOrderCodeConflict(String workOrderCode, Long excludeId);

    /**
     * 验证设备是否可用于生产
     */
    boolean validateDeviceForProduction(Long deviceId);

    /**
     * 验证设备是否可用于生产（根据设备编号）
     */
    boolean validateDeviceForProduction(String deviceCode);

    /**
     * 获取派工单详细信息（包含设备信息）
     */
    Map<String, Object> getWorkOrderDetailInfo(Long workOrderId);

    /**
     * 获取派工单生产进度
     */
    Map<String, Object> getWorkOrderProgress(Long workOrderId);

    /**
     * 计算派工单完成率
     */
    Double calculateCompletionRate(Long workOrderId);

    /**
     * 计算派工单生产时长（分钟）
     */
    Long calculateProductionDuration(Long workOrderId);

    /**
     * 获取生产效率统计
     */
    Map<String, Object> getProductionEfficiencyStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取设备生产排名
     */
    List<Map<String, Object>> getDeviceProductionRanking(int days);

    /**
     * 获取派工单状态列表
     */
    List<String> getWorkOrderStatuses();

    /**
     * 重置所有派工单状态
     */
    int resetAllWorkOrdersStatus();

    /**
     * 获取超时派工单列表
     */
    List<WorkOrder> getOverdueWorkOrders(int hours);

    /**
     * 获取长时间运行的派工单
     */
    List<WorkOrder> getLongRunningWorkOrders(int hours);

    /**
     * 同步PLC数据更新实际产量
     */
    boolean syncActualQuantityFromPLC(Long workOrderId);

    /**
     * 批量同步PLC数据
     */
    int batchSyncActualQuantityFromPLC();
}