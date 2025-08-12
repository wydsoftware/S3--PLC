package com.pda.repository;

import com.pda.entity.MaintenanceRecord;
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
 * 维修记录数据访问层接口
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {

    /**
     * 根据设备ID查找维修记录列表
     */
    List<MaintenanceRecord> findByDeviceId(Long deviceId);

    /**
     * 根据设备编号查找维修记录列表
     */
    List<MaintenanceRecord> findByDeviceCode(String deviceCode);

    /**
     * 根据状态查找维修记录列表
     */
    List<MaintenanceRecord> findByStatus(String status);

    /**
     * 根据设备ID和状态查找维修记录列表
     */
    List<MaintenanceRecord> findByDeviceIdAndStatus(Long deviceId, String status);

    /**
     * 根据设备编号和状态查找维修记录列表
     */
    List<MaintenanceRecord> findByDeviceCodeAndStatus(String deviceCode, String status);

    /**
     * 查找已报修的维修记录
     */
    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.status = 'reported'")
    List<MaintenanceRecord> findReportedRecords();

    /**
     * 查找正在维修的记录
     */
    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.status = 'in_progress'")
    List<MaintenanceRecord> findInProgressRecords();

    /**
     * 查找已完成的维修记录
     */
    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.status = 'completed'")
    List<MaintenanceRecord> findCompletedRecords();

    /**
     * 根据设备ID查找正在进行的维修记录
     */
    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.deviceId = :deviceId AND mr.status IN ('reported', 'in_progress')")
    List<MaintenanceRecord> findActiveMaintenanceByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据设备编号查找正在进行的维修记录
     */
    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.deviceCode = :deviceCode AND mr.status IN ('reported', 'in_progress')")
    List<MaintenanceRecord> findActiveMaintenanceByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 检查设备是否有正在进行的维修
     */
    @Query("SELECT COUNT(mr) > 0 FROM MaintenanceRecord mr WHERE mr.deviceId = :deviceId AND mr.status IN ('reported', 'in_progress')")
    boolean hasActiveMaintenanceForDevice(@Param("deviceId") Long deviceId);

    /**
     * 根据维修人员查找维修记录
     */
    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.repairPerson = :repairPerson")
    List<MaintenanceRecord> findByRepairPerson(@Param("repairPerson") String repairPerson);

    /**
     * 根据报修人查找维修记录
     */
    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.reportedBy = :reportedBy")
    List<MaintenanceRecord> findByReportedBy(@Param("reportedBy") String reportedBy);

    /**
     * 分页查询维修记录
     */
    @Query("SELECT mr FROM MaintenanceRecord mr WHERE (:deviceCode IS NULL OR mr.deviceCode LIKE %:deviceCode%) " +
           "AND (:status IS NULL OR mr.status = :status) " +
           "AND (:repairPerson IS NULL OR mr.repairPerson LIKE %:repairPerson%) " +
           "AND (:startTime IS NULL OR mr.reportTime >= :startTime) " +
           "AND (:endTime IS NULL OR mr.reportTime <= :endTime) " +
           "ORDER BY mr.reportTime DESC")
    Page<MaintenanceRecord> findMaintenanceRecordsWithConditions(@Param("deviceCode") String deviceCode,
                                                                @Param("status") String status,
                                                                @Param("repairPerson") String repairPerson,
                                                                @Param("startTime") LocalDateTime startTime,
                                                                @Param("endTime") LocalDateTime endTime,
                                                                Pageable pageable);

    /**
     * 查找指定时间范围内的维修记录
     */
    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.reportTime BETWEEN :startTime AND :endTime ORDER BY mr.reportTime DESC")
    List<MaintenanceRecord> findMaintenanceRecordsBetween(@Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查找指定时间范围内已完成的维修记录
     */
    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.repairTime BETWEEN :startTime AND :endTime " +
           "AND mr.status = 'completed' ORDER BY mr.repairTime DESC")
    List<MaintenanceRecord> findCompletedMaintenanceRecordsBetween(@Param("startTime") LocalDateTime startTime,
                                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 统计维修记录总数
     */
    @Query("SELECT COUNT(mr) FROM MaintenanceRecord mr")
    long countAllMaintenanceRecords();

    /**
     * 根据状态统计维修记录数量
     */
    @Query("SELECT COUNT(mr) FROM MaintenanceRecord mr WHERE mr.status = :status")
    long countByStatus(@Param("status") String status);

    /**
     * 统计已报修的记录数量
     */
    @Query("SELECT COUNT(mr) FROM MaintenanceRecord mr WHERE mr.status = 'reported'")
    long countReportedRecords();

    /**
     * 统计正在维修的记录数量
     */
    @Query("SELECT COUNT(mr) FROM MaintenanceRecord mr WHERE mr.status = 'in_progress'")
    long countInProgressRecords();

    /**
     * 统计已完成的维修记录数量
     */
    @Query("SELECT COUNT(mr) FROM MaintenanceRecord mr WHERE mr.status = 'completed'")
    long countCompletedRecords();

    /**
     * 根据设备类型统计维修记录数量
     */
    @Query("SELECT d.deviceType, COUNT(mr) FROM MaintenanceRecord mr " +
           "JOIN Device d ON mr.deviceId = d.id " +
           "GROUP BY d.deviceType")
    List<Object[]> countMaintenanceRecordsByDeviceType();

    /**
     * 根据设备编号统计维修记录数量
     */
    @Query("SELECT mr.deviceCode, COUNT(mr) FROM MaintenanceRecord mr GROUP BY mr.deviceCode")
    List<Object[]> countMaintenanceRecordsByDeviceCode();

    /**
     * 更新维修记录状态
     */
    @Modifying
    @Query("UPDATE MaintenanceRecord mr SET mr.status = :status, mr.updatedTime = CURRENT_TIMESTAMP WHERE mr.id = :id")
    int updateMaintenanceRecordStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 开始维修
     */
    @Modifying
    @Query("UPDATE MaintenanceRecord mr SET mr.status = 'in_progress', mr.repairStartTime = :repairStartTime, " +
           "mr.repairPerson = :repairPerson, mr.updatedTime = CURRENT_TIMESTAMP WHERE mr.id = :id")
    int startRepair(@Param("id") Long id, @Param("repairStartTime") LocalDateTime repairStartTime, @Param("repairPerson") String repairPerson);

    /**
     * 完成维修
     */
    @Modifying
    @Query("UPDATE MaintenanceRecord mr SET mr.status = 'completed', mr.repairTime = :repairTime, " +
           "mr.repairPerson = :repairPerson, mr.repairDescription = :repairDescription, mr.updatedTime = CURRENT_TIMESTAMP WHERE mr.id = :id")
    int completeRepair(@Param("id") Long id, @Param("repairTime") LocalDateTime repairTime, 
                      @Param("repairPerson") String repairPerson, @Param("repairDescription") String repairDescription);

    /**
     * 取消维修
     */
    @Modifying
    @Query("UPDATE MaintenanceRecord mr SET mr.status = 'cancelled', mr.updatedTime = CURRENT_TIMESTAMP WHERE mr.id = :id")
    int cancelMaintenance(@Param("id") Long id);

    /**
     * 批量更新维修记录状态
     */
    @Modifying
    @Query("UPDATE MaintenanceRecord mr SET mr.status = :status, mr.updatedTime = CURRENT_TIMESTAMP WHERE mr.id IN :ids")
    int updateMaintenanceRecordStatusBatch(@Param("ids") List<Long> ids, @Param("status") String status);

    /**
     * 获取维修记录状态统计
     */
    @Query("SELECT mr.status, COUNT(mr) FROM MaintenanceRecord mr GROUP BY mr.status")
    List<Object[]> getMaintenanceRecordStatusStatistics();

    /**
     * 获取维修记录创建时间统计（按日）
     */
    @Query("SELECT DATE(mr.reportTime) as date, COUNT(mr) FROM MaintenanceRecord mr " +
           "WHERE mr.reportTime >= :startTime " +
           "GROUP BY DATE(mr.reportTime) " +
           "ORDER BY date DESC")
    List<Object[]> getMaintenanceRecordCreationStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取维修统计（按日）
     */
    @Query("SELECT DATE(mr.reportTime) as date, " +
           "COUNT(mr) as reportCount, " +
           "SUM(CASE WHEN mr.status = 'completed' THEN 1 ELSE 0 END) as completedCount, " +
           "AVG(CASE WHEN mr.repairTime IS NOT NULL AND mr.reportTime IS NOT NULL " +
           "THEN TIMESTAMPDIFF(HOUR, mr.reportTime, mr.repairTime) ELSE NULL END) as avgRepairHours " +
           "FROM MaintenanceRecord mr " +
           "WHERE mr.reportTime >= :startTime " +
           "GROUP BY DATE(mr.reportTime) " +
           "ORDER BY date DESC")
    List<Object[]> getMaintenanceStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取设备维修统计
     */
    @Query("SELECT mr.deviceCode, " +
           "COUNT(mr) as reportCount, " +
           "SUM(CASE WHEN mr.status = 'completed' THEN 1 ELSE 0 END) as completedCount, " +
           "AVG(CASE WHEN mr.repairTime IS NOT NULL AND mr.reportTime IS NOT NULL " +
           "THEN TIMESTAMPDIFF(HOUR, mr.reportTime, mr.repairTime) ELSE NULL END) as avgRepairHours, " +
           "SUM(CASE WHEN mr.repairTime IS NOT NULL AND mr.reportTime IS NOT NULL " +
           "THEN TIMESTAMPDIFF(HOUR, mr.reportTime, mr.repairTime) ELSE 0 END) as totalDowntimeHours " +
           "FROM MaintenanceRecord mr " +
           "WHERE mr.reportTime >= :startTime " +
           "GROUP BY mr.deviceCode " +
           "ORDER BY mr.deviceCode")
    List<Object[]> getDeviceMaintenanceStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 搜索维修记录
     */
    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.deviceCode LIKE %:keyword% " +
           "OR mr.faultDescription LIKE %:keyword% OR mr.repairDescription LIKE %:keyword%")
    List<MaintenanceRecord> searchMaintenanceRecordsByKeyword(@Param("keyword") String keyword);

    /**
     * 分页搜索维修记录
     */
    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.deviceCode LIKE %:keyword% " +
           "OR mr.faultDescription LIKE %:keyword% OR mr.repairDescription LIKE %:keyword% ORDER BY mr.reportTime DESC")
    Page<MaintenanceRecord> searchMaintenanceRecordsByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 获取最近的维修记录
     */
    @Query("SELECT mr FROM MaintenanceRecord mr ORDER BY mr.reportTime DESC")
    List<MaintenanceRecord> findRecentMaintenanceRecords(Pageable pageable);

    /**
     * 获取正在维修的记录详情（包含设备信息）
     */
    @Query("SELECT mr, d FROM MaintenanceRecord mr JOIN Device d ON mr.deviceId = d.id WHERE mr.status IN ('reported', 'in_progress') ORDER BY mr.reportTime ASC")
    List<Object[]> findActiveMaintenanceRecordsWithDevice();

    /**
     * 获取维修人员工作统计
     */
    @Query("SELECT mr.repairPerson, " +
           "COUNT(mr) as repairCount, " +
           "AVG(CASE WHEN mr.repairTime IS NOT NULL AND mr.repairStartTime IS NOT NULL " +
           "THEN TIMESTAMPDIFF(HOUR, mr.repairStartTime, mr.repairTime) ELSE NULL END) as avgRepairHours " +
           "FROM MaintenanceRecord mr " +
           "WHERE mr.repairPerson IS NOT NULL AND mr.status = 'completed' " +
           "AND mr.repairTime >= :startTime " +
           "GROUP BY mr.repairPerson " +
           "ORDER BY repairCount DESC")
    List<Object[]> getRepairPersonStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取月度维修报表数据
     */
    @Query(value = "SELECT " +
                   "DATE_FORMAT(mr.report_time, '%Y-%m') as month, " +
                   "mr.device_code, " +
                   "d.device_type, " +
                   "COUNT(mr.id) as report_count, " +
                   "SUM(CASE WHEN mr.status = 'completed' THEN 1 ELSE 0 END) as completed_count, " +
                   "ROUND(AVG(CASE WHEN mr.repair_time IS NOT NULL AND mr.report_time IS NOT NULL " +
                   "THEN TIMESTAMPDIFF(HOUR, mr.report_time, mr.repair_time) ELSE NULL END), 2) as avg_repair_hours, " +
                   "SUM(CASE WHEN mr.repair_time IS NOT NULL AND mr.report_time IS NOT NULL " +
                   "THEN TIMESTAMPDIFF(HOUR, mr.report_time, mr.repair_time) ELSE 0 END) as total_downtime_hours " +
                   "FROM maintenance_records mr " +
                   "JOIN devices d ON mr.device_id = d.id " +
                   "WHERE mr.report_time >= :startTime " +
                   "AND mr.report_time < :endTime " +
                   "GROUP BY DATE_FORMAT(mr.report_time, '%Y-%m'), mr.device_code, d.device_type " +
                   "ORDER BY month DESC, mr.device_code", nativeQuery = true)
    List<Object[]> getMonthlyMaintenanceReport(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 获取设备故障频率统计
     */
    @Query("SELECT mr.deviceCode, d.deviceType, COUNT(mr) as faultCount, " +
           "ROUND(COUNT(mr) * 30.0 / DATEDIFF(CURRENT_DATE, :startDate), 2) as faultFrequency " +
           "FROM MaintenanceRecord mr " +
           "JOIN Device d ON mr.deviceId = d.id " +
           "WHERE mr.reportTime >= :startTime " +
           "GROUP BY mr.deviceCode, d.deviceType " +
           "ORDER BY faultCount DESC")
    List<Object[]> getDeviceFaultFrequencyStatistics(@Param("startTime") LocalDateTime startTime, @Param("startDate") java.sql.Date startDate);
}