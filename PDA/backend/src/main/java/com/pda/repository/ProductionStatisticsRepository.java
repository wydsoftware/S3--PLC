package com.pda.repository;

import com.pda.entity.ProductionStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 生产统计数据访问层接口
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
@Repository
public interface ProductionStatisticsRepository extends JpaRepository<ProductionStatistics, Long> {

    /**
     * 根据统计日期和统计类型查找统计记录
     */
    Optional<ProductionStatistics> findByStatDateAndStatType(LocalDate statDate, String statType);

    /**
     * 根据统计日期、统计类型和设备编号查找统计记录
     */
    Optional<ProductionStatistics> findByStatDateAndStatTypeAndDeviceCode(LocalDate statDate, String statType, String deviceCode);

    /**
     * 根据统计日期、统计类型和设备类型查找统计记录
     */
    Optional<ProductionStatistics> findByStatDateAndStatTypeAndDeviceType(LocalDate statDate, String statType, String deviceType);

    /**
     * 根据统计类型查找统计记录列表
     */
    List<ProductionStatistics> findByStatType(String statType);

    /**
     * 根据设备类型查找统计记录列表
     */
    List<ProductionStatistics> findByDeviceType(String deviceType);

    /**
     * 根据设备编号查找统计记录列表
     */
    List<ProductionStatistics> findByDeviceCode(String deviceCode);

    /**
     * 根据统计类型和设备类型查找统计记录列表
     */
    List<ProductionStatistics> findByStatTypeAndDeviceType(String statType, String deviceType);

    /**
     * 根据统计类型和设备编号查找统计记录列表
     */
    List<ProductionStatistics> findByStatTypeAndDeviceCode(String statType, String deviceCode);

    /**
     * 查找日统计记录
     */
    @Query("SELECT ps FROM ProductionStatistics ps WHERE ps.statType = 'daily' ORDER BY ps.statDate DESC")
    List<ProductionStatistics> findDailyStatistics();

    /**
     * 查找月统计记录
     */
    @Query("SELECT ps FROM ProductionStatistics ps WHERE ps.statType = 'monthly' ORDER BY ps.statDate DESC")
    List<ProductionStatistics> findMonthlyStatistics();

    /**
     * 查找设备日统计记录
     */
    @Query("SELECT ps FROM ProductionStatistics ps WHERE ps.statType = 'device_daily' ORDER BY ps.statDate DESC, ps.deviceCode ASC")
    List<ProductionStatistics> findDeviceDailyStatistics();

    /**
     * 查找设备月统计记录
     */
    @Query("SELECT ps FROM ProductionStatistics ps WHERE ps.statType = 'device_monthly' ORDER BY ps.statDate DESC, ps.deviceCode ASC")
    List<ProductionStatistics> findDeviceMonthlyStatistics();

    /**
     * 根据日期范围查找统计记录
     */
    @Query("SELECT ps FROM ProductionStatistics ps WHERE ps.statDate BETWEEN :startDate AND :endDate ORDER BY ps.statDate DESC")
    List<ProductionStatistics> findStatisticsBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据日期范围和统计类型查找统计记录
     */
    @Query("SELECT ps FROM ProductionStatistics ps WHERE ps.statDate BETWEEN :startDate AND :endDate " +
           "AND ps.statType = :statType ORDER BY ps.statDate DESC")
    List<ProductionStatistics> findStatisticsBetweenAndStatType(@Param("startDate") LocalDate startDate, 
                                                               @Param("endDate") LocalDate endDate, 
                                                               @Param("statType") String statType);

    /**
     * 根据日期范围和设备类型查找统计记录
     */
    @Query("SELECT ps FROM ProductionStatistics ps WHERE ps.statDate BETWEEN :startDate AND :endDate " +
           "AND ps.deviceType = :deviceType ORDER BY ps.statDate DESC")
    List<ProductionStatistics> findStatisticsBetweenAndDeviceType(@Param("startDate") LocalDate startDate, 
                                                                 @Param("endDate") LocalDate endDate, 
                                                                 @Param("deviceType") String deviceType);

    /**
     * 根据日期范围和设备编号查找统计记录
     */
    @Query("SELECT ps FROM ProductionStatistics ps WHERE ps.statDate BETWEEN :startDate AND :endDate " +
           "AND ps.deviceCode = :deviceCode ORDER BY ps.statDate DESC")
    List<ProductionStatistics> findStatisticsBetweenAndDeviceCode(@Param("startDate") LocalDate startDate, 
                                                                 @Param("endDate") LocalDate endDate, 
                                                                 @Param("deviceCode") String deviceCode);

    /**
     * 分页查询生产统计
     */
    @Query("SELECT ps FROM ProductionStatistics ps WHERE (:statType IS NULL OR ps.statType = :statType) " +
           "AND (:deviceType IS NULL OR ps.deviceType = :deviceType) " +
           "AND (:deviceCode IS NULL OR ps.deviceCode LIKE %:deviceCode%) " +
           "AND (:startDate IS NULL OR ps.statDate >= :startDate) " +
           "AND (:endDate IS NULL OR ps.statDate <= :endDate) " +
           "ORDER BY ps.statDate DESC, ps.deviceCode ASC")
    Page<ProductionStatistics> findStatisticsWithConditions(@Param("statType") String statType,
                                                           @Param("deviceType") String deviceType,
                                                           @Param("deviceCode") String deviceCode,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate,
                                                           Pageable pageable);

    /**
     * 统计记录总数
     */
    @Query("SELECT COUNT(ps) FROM ProductionStatistics ps")
    long countAllStatistics();

    /**
     * 根据统计类型统计记录数量
     */
    @Query("SELECT COUNT(ps) FROM ProductionStatistics ps WHERE ps.statType = :statType")
    long countByStatType(@Param("statType") String statType);

    /**
     * 根据设备类型统计记录数量
     */
    @Query("SELECT COUNT(ps) FROM ProductionStatistics ps WHERE ps.deviceType = :deviceType")
    long countByDeviceType(@Param("deviceType") String deviceType);

    /**
     * 获取统计类型分布
     */
    @Query("SELECT ps.statType, COUNT(ps) FROM ProductionStatistics ps GROUP BY ps.statType")
    List<Object[]> getStatTypeDistribution();

    /**
     * 获取设备类型统计分布
     */
    @Query("SELECT ps.deviceType, COUNT(ps) FROM ProductionStatistics ps WHERE ps.deviceType IS NOT NULL GROUP BY ps.deviceType")
    List<Object[]> getDeviceTypeStatisticsDistribution();

    /**
     * 获取月度生产汇总
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', ps.statDate, '%Y-%m') as month, " +
           "SUM(ps.workOrderCount) as totalOrders, " +
           "SUM(ps.plannedQuantity) as totalPlanned, " +
           "SUM(ps.actualQuantity) as totalActual, " +
           "AVG(ps.completionRate) as avgCompletionRate, " +
           "SUM(ps.productionMinutes) as totalProductionMinutes " +
           "FROM ProductionStatistics ps " +
           "WHERE ps.statType = 'daily' AND ps.statDate >= :startDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', ps.statDate, '%Y-%m') " +
           "ORDER BY month DESC")
    List<Object[]> getMonthlyProductionSummary(@Param("startDate") LocalDate startDate);

    /**
     * 获取设备生产效率排名
     */
    @Query("SELECT ps.deviceCode, ps.deviceType, " +
           "AVG(ps.productionEfficiency) as avgEfficiency, " +
           "AVG(ps.utilizationRate) as avgUtilization, " +
           "SUM(ps.actualQuantity) as totalProduction " +
           "FROM ProductionStatistics ps " +
           "WHERE ps.statType = 'device_daily' AND ps.statDate >= :startDate " +
           "GROUP BY ps.deviceCode, ps.deviceType " +
           "ORDER BY avgEfficiency DESC")
    List<Object[]> getDeviceEfficiencyRanking(@Param("startDate") LocalDate startDate);

    /**
     * 获取设备类型生产统计
     */
    @Query("SELECT ps.deviceType, " +
           "SUM(ps.workOrderCount) as totalOrders, " +
           "SUM(ps.plannedQuantity) as totalPlanned, " +
           "SUM(ps.actualQuantity) as totalActual, " +
           "AVG(ps.completionRate) as avgCompletionRate, " +
           "AVG(ps.productionEfficiency) as avgEfficiency " +
           "FROM ProductionStatistics ps " +
           "WHERE ps.statType = 'device_daily' AND ps.statDate >= :startDate " +
           "GROUP BY ps.deviceType " +
           "ORDER BY totalActual DESC")
    List<Object[]> getDeviceTypeProductionStatistics(@Param("startDate") LocalDate startDate);

    /**
     * 更新统计数据
     */
    @Modifying
    @Query("UPDATE ProductionStatistics ps SET " +
           "ps.workOrderCount = :workOrderCount, " +
           "ps.plannedQuantity = :plannedQuantity, " +
           "ps.actualQuantity = :actualQuantity, " +
           "ps.completionRate = :completionRate, " +
           "ps.productionMinutes = :productionMinutes, " +
           "ps.downtimeMinutes = :downtimeMinutes, " +
           "ps.maintenanceCount = :maintenanceCount, " +
           "ps.utilizationRate = :utilizationRate, " +
           "ps.productionEfficiency = :productionEfficiency, " +
           "ps.updatedTime = CURRENT_TIMESTAMP " +
           "WHERE ps.id = :id")
    int updateStatistics(@Param("id") Long id,
                        @Param("workOrderCount") Integer workOrderCount,
                        @Param("plannedQuantity") Integer plannedQuantity,
                        @Param("actualQuantity") Integer actualQuantity,
                        @Param("completionRate") Double completionRate,
                        @Param("productionMinutes") Long productionMinutes,
                        @Param("downtimeMinutes") Long downtimeMinutes,
                        @Param("maintenanceCount") Integer maintenanceCount,
                        @Param("utilizationRate") Double utilizationRate,
                        @Param("productionEfficiency") Double productionEfficiency);

    /**
     * 批量删除统计记录
     */
    @Modifying
    @Query("DELETE FROM ProductionStatistics ps WHERE ps.id IN :ids")
    int deleteStatisticsBatch(@Param("ids") List<Long> ids);

    /**
     * 删除指定日期之前的统计记录
     */
    @Modifying
    @Query("DELETE FROM ProductionStatistics ps WHERE ps.statDate < :beforeDate")
    int deleteStatisticsBeforeDate(@Param("beforeDate") LocalDate beforeDate);

    /**
     * 搜索统计记录
     */
    @Query("SELECT ps FROM ProductionStatistics ps WHERE ps.deviceCode LIKE %:keyword% OR ps.deviceType LIKE %:keyword%")
    List<ProductionStatistics> searchStatisticsByKeyword(@Param("keyword") String keyword);

    /**
     * 分页搜索统计记录
     */
    @Query("SELECT ps FROM ProductionStatistics ps WHERE ps.deviceCode LIKE %:keyword% OR ps.deviceType LIKE %:keyword% ORDER BY ps.statDate DESC")
    Page<ProductionStatistics> searchStatisticsByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 获取最近的统计记录
     */
    @Query("SELECT ps FROM ProductionStatistics ps ORDER BY ps.statDate DESC, ps.createdTime DESC")
    List<ProductionStatistics> findRecentStatistics(Pageable pageable);

    /**
     * 获取生产趋势数据
     */
    @Query("SELECT ps.statDate, " +
           "SUM(ps.actualQuantity) as dailyProduction, " +
           "AVG(ps.completionRate) as avgCompletionRate, " +
           "AVG(ps.productionEfficiency) as avgEfficiency " +
           "FROM ProductionStatistics ps " +
           "WHERE ps.statType = 'daily' AND ps.statDate BETWEEN :startDate AND :endDate " +
           "GROUP BY ps.statDate " +
           "ORDER BY ps.statDate ASC")
    List<Object[]> getProductionTrendData(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 获取设备利用率趋势
     */
    @Query("SELECT ps.statDate, ps.deviceCode, ps.utilizationRate, ps.productionEfficiency " +
           "FROM ProductionStatistics ps " +
           "WHERE ps.statType = 'device_daily' AND ps.deviceCode = :deviceCode " +
           "AND ps.statDate BETWEEN :startDate AND :endDate " +
           "ORDER BY ps.statDate ASC")
    List<Object[]> getDeviceUtilizationTrend(@Param("deviceCode") String deviceCode, 
                                            @Param("startDate") LocalDate startDate, 
                                            @Param("endDate") LocalDate endDate);

    /**
     * 获取设备类型对比数据
     */
    @Query("SELECT ps.deviceType, ps.statDate, " +
           "SUM(ps.actualQuantity) as typeProduction, " +
           "AVG(ps.completionRate) as avgCompletionRate " +
           "FROM ProductionStatistics ps " +
           "WHERE ps.statType = 'device_daily' AND ps.statDate BETWEEN :startDate AND :endDate " +
           "GROUP BY ps.deviceType, ps.statDate " +
           "ORDER BY ps.statDate ASC, ps.deviceType ASC")
    List<Object[]> getDeviceTypeComparisonData(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 检查统计记录是否存在
     */
    @Query("SELECT COUNT(ps) > 0 FROM ProductionStatistics ps WHERE ps.statDate = :statDate " +
           "AND ps.statType = :statType AND (:deviceCode IS NULL OR ps.deviceCode = :deviceCode)")
    boolean existsStatistics(@Param("statDate") LocalDate statDate, 
                           @Param("statType") String statType, 
                           @Param("deviceCode") String deviceCode);

    /**
     * 获取生产报表数据
     */
    @Query(value = "SELECT " +
                   "DATE_FORMAT(ps.stat_date, '%Y-%m') as month, " +
                   "ps.device_type, " +
                   "ps.device_code, " +
                   "SUM(ps.work_order_count) as total_orders, " +
                   "SUM(ps.planned_quantity) as total_planned, " +
                   "SUM(ps.actual_quantity) as total_actual, " +
                   "ROUND(AVG(ps.completion_rate), 2) as avg_completion_rate, " +
                   "SUM(ps.production_minutes) as total_production_minutes, " +
                   "SUM(ps.downtime_minutes) as total_downtime_minutes, " +
                   "ROUND(AVG(ps.utilization_rate), 2) as avg_utilization_rate, " +
                   "ROUND(AVG(ps.production_efficiency), 2) as avg_efficiency " +
                   "FROM production_statistics ps " +
                   "WHERE ps.stat_type = 'device_daily' " +
                   "AND ps.stat_date >= :startDate " +
                   "AND ps.stat_date < :endDate " +
                   "AND (:deviceType IS NULL OR ps.device_type = :deviceType) " +
                   "AND (:deviceCode IS NULL OR ps.device_code = :deviceCode) " +
                   "GROUP BY DATE_FORMAT(ps.stat_date, '%Y-%m'), ps.device_type, ps.device_code " +
                   "ORDER BY month DESC, ps.device_type, ps.device_code", nativeQuery = true)
    List<Object[]> getProductionReportData(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("deviceType") String deviceType,
                                          @Param("deviceCode") String deviceCode);
}