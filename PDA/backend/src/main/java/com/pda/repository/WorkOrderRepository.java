package com.pda.repository;

import com.pda.entity.WorkOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 派工单数据访问层接口
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    /**
     * 根据派工单号查找派工单
     */
    Optional<WorkOrder> findByOrderNo(String orderNo);

    /**
     * 检查派工单号是否存在
     */
    boolean existsByOrderNo(String orderNo);

    /**
     * 根据设备ID查找派工单列表
     */
    List<WorkOrder> findByDeviceId(Long deviceId);

    /**
     * 根据设备编号查找派工单列表
     */
    List<WorkOrder> findByDeviceCode(String deviceCode);

    /**
     * 根据状态查找派工单列表
     */
    List<WorkOrder> findByStatus(String status);

    /**
     * 根据设备ID和状态查找派工单列表
     */
    List<WorkOrder> findByDeviceIdAndStatus(Long deviceId, String status);

    /**
     * 根据设备编号和状态查找派工单列表
     */
    List<WorkOrder> findByDeviceCodeAndStatus(String deviceCode, String status);

    /**
     * 查找正在生产的派工单
     */
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.status = 'working'")
    List<WorkOrder> findWorkingOrders();

    /**
     * 查找待开始的派工单
     */
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.status = 'pending'")
    List<WorkOrder> findPendingOrders();

    /**
     * 查找已完成的派工单
     */
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.status IN ('completed', 'stopped')")
    List<WorkOrder> findCompletedOrders();

    /**
     * 根据设备ID查找正在生产的派工单
     */
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.deviceId = :deviceId AND wo.status = 'working'")
    Optional<WorkOrder> findWorkingOrderByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据设备编号查找正在生产的派工单
     */
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.deviceCode = :deviceCode AND wo.status = 'working'")
    Optional<WorkOrder> findWorkingOrderByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 根据派工单号模糊查询
     */
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.orderNo LIKE %:orderNo%")
    List<WorkOrder> findByOrderNoLike(@Param("orderNo") String orderNo);

    /**
     * 分页查询派工单
     */
    @Query("SELECT wo FROM WorkOrder wo WHERE (:orderNo IS NULL OR wo.orderNo LIKE %:orderNo%) " +
           "AND (:deviceCode IS NULL OR wo.deviceCode LIKE %:deviceCode%) " +
           "AND (:status IS NULL OR wo.status = :status) " +
           "AND (:startTime IS NULL OR wo.startTime >= :startTime) " +
           "AND (:endTime IS NULL OR wo.startTime <= :endTime) " +
           "ORDER BY wo.createdTime DESC")
    Page<WorkOrder> findWorkOrdersWithConditions(@Param("orderNo") String orderNo,
                                                 @Param("deviceCode") String deviceCode,
                                                 @Param("status") String status,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 Pageable pageable);

    /**
     * 查找指定时间范围内的派工单
     */
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.startTime BETWEEN :startTime AND :endTime ORDER BY wo.startTime DESC")
    List<WorkOrder> findWorkOrdersBetween(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 查找指定时间范围内已完成的派工单
     */
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.endTime BETWEEN :startTime AND :endTime " +
           "AND wo.status IN ('completed', 'stopped') ORDER BY wo.endTime DESC")
    List<WorkOrder> findCompletedWorkOrdersBetween(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 统计派工单总数
     */
    @Query("SELECT COUNT(wo) FROM WorkOrder wo")
    long countAllWorkOrders();

    /**
     * 根据状态统计派工单数量
     */
    @Query("SELECT COUNT(wo) FROM WorkOrder wo WHERE wo.status = :status")
    long countByStatus(@Param("status") String status);

    /**
     * 统计正在生产的派工单数量
     */
    @Query("SELECT COUNT(wo) FROM WorkOrder wo WHERE wo.status = 'working'")
    long countWorkingOrders();

    /**
     * 统计待开始的派工单数量
     */
    @Query("SELECT COUNT(wo) FROM WorkOrder wo WHERE wo.status = 'pending'")
    long countPendingOrders();

    /**
     * 统计已完成的派工单数量
     */
    @Query("SELECT COUNT(wo) FROM WorkOrder wo WHERE wo.status IN ('completed', 'stopped')")
    long countCompletedOrders();

    /**
     * 根据设备类型统计派工单数量
     */
    @Query("SELECT d.deviceType, COUNT(wo) FROM WorkOrder wo " +
           "JOIN Device d ON wo.deviceId = d.id " +
           "GROUP BY d.deviceType")
    List<Object[]> countWorkOrdersByDeviceType();

    /**
     * 根据设备编号统计派工单数量
     */
    @Query("SELECT wo.deviceCode, COUNT(wo) FROM WorkOrder wo GROUP BY wo.deviceCode")
    List<Object[]> countWorkOrdersByDeviceCode();

    /**
     * 更新派工单状态
     */
    @Modifying
    @Query("UPDATE WorkOrder wo SET wo.status = :status, wo.updatedTime = CURRENT_TIMESTAMP WHERE wo.id = :id")
    int updateWorkOrderStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 开始生产
     */
    @Modifying
    @Query("UPDATE WorkOrder wo SET wo.status = 'working', wo.startTime = :startTime, wo.updatedTime = CURRENT_TIMESTAMP WHERE wo.id = :id")
    int startProduction(@Param("id") Long id, @Param("startTime") LocalDateTime startTime);

    /**
     * 停止生产
     */
    @Modifying
    @Query("UPDATE WorkOrder wo SET wo.status = 'stopped', wo.endTime = :endTime, wo.actualQuantity = :actualQuantity, wo.updatedTime = CURRENT_TIMESTAMP WHERE wo.id = :id")
    int stopProduction(@Param("id") Long id, @Param("endTime") LocalDateTime endTime, @Param("actualQuantity") Integer actualQuantity);

    /**
     * 完成生产
     */
    @Modifying
    @Query("UPDATE WorkOrder wo SET wo.status = 'completed', wo.endTime = :endTime, wo.actualQuantity = :actualQuantity, wo.updatedTime = CURRENT_TIMESTAMP WHERE wo.id = :id")
    int completeProduction(@Param("id") Long id, @Param("endTime") LocalDateTime endTime, @Param("actualQuantity") Integer actualQuantity);

    /**
     * 取消派工单
     */
    @Modifying
    @Query("UPDATE WorkOrder wo SET wo.status = 'cancelled', wo.updatedTime = CURRENT_TIMESTAMP WHERE wo.id = :id")
    int cancelWorkOrder(@Param("id") Long id);

    /**
     * 批量更新派工单状态
     */
    @Modifying
    @Query("UPDATE WorkOrder wo SET wo.status = :status, wo.updatedTime = CURRENT_TIMESTAMP WHERE wo.id IN :ids")
    int updateWorkOrderStatusBatch(@Param("ids") List<Long> ids, @Param("status") String status);

    /**
     * 获取派工单状态统计
     */
    @Query("SELECT wo.status, COUNT(wo) FROM WorkOrder wo GROUP BY wo.status")
    List<Object[]> getWorkOrderStatusStatistics();

    /**
     * 获取派工单创建时间统计（按日）
     */
    @Query("SELECT DATE(wo.createdTime) as date, COUNT(wo) FROM WorkOrder wo " +
           "WHERE wo.createdTime >= :startTime " +
           "GROUP BY DATE(wo.createdTime) " +
           "ORDER BY date DESC")
    List<Object[]> getWorkOrderCreationStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取生产统计（按日）
     */
    @Query("SELECT DATE(wo.startTime) as date, " +
           "COUNT(wo) as orderCount, " +
           "SUM(wo.plannedQuantity) as plannedTotal, " +
           "SUM(wo.actualQuantity) as actualTotal " +
           "FROM WorkOrder wo " +
           "WHERE wo.startTime >= :startTime AND wo.startTime IS NOT NULL " +
           "GROUP BY DATE(wo.startTime) " +
           "ORDER BY date DESC")
    List<Object[]> getProductionStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取设备生产统计
     */
    @Query("SELECT wo.deviceCode, " +
           "COUNT(wo) as orderCount, " +
           "SUM(wo.plannedQuantity) as plannedTotal, " +
           "SUM(wo.actualQuantity) as actualTotal, " +
           "AVG(TIMESTAMPDIFF(MINUTE, wo.startTime, wo.endTime)) as avgDuration " +
           "FROM WorkOrder wo " +
           "WHERE wo.startTime >= :startTime AND wo.startTime IS NOT NULL " +
           "GROUP BY wo.deviceCode " +
           "ORDER BY wo.deviceCode")
    List<Object[]> getDeviceProductionStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 搜索派工单
     */
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.orderNo LIKE %:keyword% OR wo.deviceCode LIKE %:keyword%")
    List<WorkOrder> searchWorkOrdersByKeyword(@Param("keyword") String keyword);

    /**
     * 分页搜索派工单
     */
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.orderNo LIKE %:keyword% OR wo.deviceCode LIKE %:keyword% ORDER BY wo.createdTime DESC")
    Page<WorkOrder> searchWorkOrdersByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 获取最近的派工单
     */
    @Query("SELECT wo FROM WorkOrder wo ORDER BY wo.createdTime DESC")
    List<WorkOrder> findRecentWorkOrders(Pageable pageable);

    /**
     * 获取正在生产的派工单详情（包含设备信息）
     */
    @Query("SELECT wo, d FROM WorkOrder wo JOIN Device d ON wo.deviceId = d.id WHERE wo.status = 'working' ORDER BY wo.startTime ASC")
    List<Object[]> findWorkingOrdersWithDevice();

    /**
     * 检查设备是否有正在进行的派工单
     */
    @Query("SELECT COUNT(wo) > 0 FROM WorkOrder wo WHERE wo.deviceId = :deviceId AND wo.status IN ('pending', 'working')")
    boolean hasActiveWorkOrderForDevice(@Param("deviceId") Long deviceId);

    /**
     * 检查派工单号是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(wo) > 0 FROM WorkOrder wo WHERE wo.orderNo = :orderNo AND wo.id != :id")
    boolean existsByOrderNoAndIdNot(@Param("orderNo") String orderNo, @Param("id") Long id);

    /**
     * 获取设备当前派工单
     */
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.deviceId = :deviceId AND wo.status IN ('pending', 'working') ORDER BY wo.createdTime DESC")
    List<WorkOrder> findActiveWorkOrdersByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 获取月度生产报表数据
     */
    @Query(value = "SELECT " +
                   "DATE_FORMAT(wo.start_time, '%Y-%m') as month, " +
                   "wo.device_code, " +
                   "d.device_type, " +
                   "COUNT(wo.id) as order_count, " +
                   "SUM(wo.planned_quantity) as planned_total, " +
                   "SUM(wo.actual_quantity) as actual_total, " +
                   "ROUND(AVG(wo.actual_quantity * 100.0 / wo.planned_quantity), 2) as avg_completion_rate, " +
                   "SUM(TIMESTAMPDIFF(MINUTE, wo.start_time, wo.end_time)) as total_minutes " +
                   "FROM work_orders wo " +
                   "JOIN devices d ON wo.device_id = d.id " +
                   "WHERE wo.start_time >= :startTime " +
                   "AND wo.start_time < :endTime " +
                   "AND wo.status IN ('completed', 'stopped') " +
                   "GROUP BY DATE_FORMAT(wo.start_time, '%Y-%m'), wo.device_code, d.device_type " +
                   "ORDER BY month DESC, wo.device_code", nativeQuery = true)
    List<Object[]> getMonthlyProductionReport(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);
}