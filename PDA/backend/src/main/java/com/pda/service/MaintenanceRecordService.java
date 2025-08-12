package com.pda.service;

import com.pda.entity.MaintenanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 维修记录服务接口
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
public interface MaintenanceRecordService {

    /**
     * 保存维修记录
     * 
     * @param maintenanceRecord 维修记录
     * @return 保存后的维修记录
     */
    MaintenanceRecord saveMaintenanceRecord(MaintenanceRecord maintenanceRecord);

    /**
     * 根据ID查找维修记录
     * 
     * @param id 维修记录ID
     * @return 维修记录
     */
    Optional<MaintenanceRecord> findById(Long id);

    /**
     * 根据维修单号查找维修记录
     * 
     * @param maintenanceCode 维修单号
     * @return 维修记录
     */
    Optional<MaintenanceRecord> findByMaintenanceCode(String maintenanceCode);

    /**
     * 检查维修单号是否存在
     * 
     * @param maintenanceCode 维修单号
     * @return 是否存在
     */
    boolean existsByMaintenanceCode(String maintenanceCode);

    /**
     * 创建维修记录
     * 
     * @param deviceId 设备ID
     * @param faultDescription 故障描述
     * @param reportedBy 报修人
     * @return 维修记录
     */
    MaintenanceRecord createMaintenanceRecord(Long deviceId, String faultDescription, String reportedBy);

    /**
     * 更新维修记录
     * 
     * @param id 维修记录ID
     * @param faultDescription 故障描述
     * @param repairDescription 维修描述
     * @param status 状态
     * @return 更新后的维修记录
     */
    MaintenanceRecord updateMaintenanceRecord(Long id, String faultDescription, String repairDescription, String status);

    /**
     * 删除维修记录
     * 
     * @param id 维修记录ID
     * @return 是否删除成功
     */
    boolean deleteMaintenanceRecord(Long id);

    /**
     * 开始维修
     * 
     * @param maintenanceId 维修记录ID
     * @param repairedBy 维修人员
     * @return 是否成功
     */
    boolean startMaintenance(Long maintenanceId, String repairedBy);

    /**
     * 开始维修
     * 
     * @param maintenanceCode 维修单号
     * @param repairedBy 维修人员
     * @return 是否成功
     */
    boolean startMaintenance(String maintenanceCode, String repairedBy);

    /**
     * 完成维修
     * 
     * @param maintenanceId 维修记录ID
     * @param repairDescription 维修描述
     * @return 是否成功
     */
    boolean completeMaintenance(Long maintenanceId, String repairDescription);

    /**
     * 完成维修
     * 
     * @param maintenanceCode 维修单号
     * @param repairDescription 维修描述
     * @return 是否成功
     */
    boolean completeMaintenance(String maintenanceCode, String repairDescription);

    /**
     * 取消维修
     * 
     * @param maintenanceId 维修记录ID
     * @param cancelReason 取消原因
     * @return 是否成功
     */
    boolean cancelMaintenance(Long maintenanceId, String cancelReason);

    /**
     * 获取所有维修记录
     * 
     * @return 维修记录列表
     */
    List<MaintenanceRecord> findAllMaintenanceRecords();

    /**
     * 根据状态查找维修记录
     * 
     * @param status 状态
     * @return 维修记录列表
     */
    List<MaintenanceRecord> findByStatus(String status);

    /**
     * 根据设备ID查找维修记录
     * 
     * @param deviceId 设备ID
     * @return 维修记录列表
     */
    List<MaintenanceRecord> findByDeviceId(Long deviceId);

    /**
     * 根据设备编号查找维修记录
     * 
     * @param deviceCode 设备编号
     * @return 维修记录列表
     */
    List<MaintenanceRecord> findByDeviceCode(String deviceCode);

    /**
     * 获取待处理的维修记录
     * 
     * @return 维修记录列表
     */
    List<MaintenanceRecord> getPendingMaintenanceRecords();

    /**
     * 获取进行中的维修记录
     * 
     * @return 维修记录列表
     */
    List<MaintenanceRecord> getInProgressMaintenanceRecords();

    /**
     * 获取已完成的维修记录
     * 
     * @return 维修记录列表
     */
    List<MaintenanceRecord> getCompletedMaintenanceRecords();

    /**
     * 获取进行中的维修记录及设备信息
     * 
     * @return 维修记录及设备信息列表
     */
    List<Map<String, Object>> getInProgressMaintenanceRecordsWithDeviceInfo();

    /**
     * 条件查询维修记录
     * 
     * @param maintenanceCode 维修单号
     * @param deviceCode 设备编号
     * @param status 状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 维修记录分页结果
     */
    Page<MaintenanceRecord> findMaintenanceRecordsWithConditions(String maintenanceCode, String deviceCode, 
                                                                String status, LocalDateTime startTime, 
                                                                LocalDateTime endTime, Pageable pageable);

    /**
     * 搜索维修记录
     * 
     * @param keyword 关键词
     * @return 维修记录列表
     */
    List<MaintenanceRecord> searchMaintenanceRecords(String keyword);

    /**
     * 搜索维修记录（分页）
     * 
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 维修记录分页结果
     */
    Page<MaintenanceRecord> searchMaintenanceRecords(String keyword, Pageable pageable);

    /**
     * 检查设备是否有进行中的维修记录
     * 
     * @param deviceId 设备ID
     * @return 是否有进行中的维修记录
     */
    boolean hasActiveMaintenanceForDevice(Long deviceId);

    /**
     * 检查设备是否有进行中的维修记录
     * 
     * @param deviceCode 设备编号
     * @return 是否有进行中的维修记录
     */
    boolean hasActiveMaintenanceForDevice(String deviceCode);

    /**
     * 获取设备的进行中维修记录
     * 
     * @param deviceId 设备ID
     * @return 维修记录
     */
    Optional<MaintenanceRecord> getActiveMaintenanceForDevice(Long deviceId);

    /**
     * 获取设备的进行中维修记录
     * 
     * @param deviceCode 设备编号
     * @return 维修记录
     */
    Optional<MaintenanceRecord> getActiveMaintenanceForDevice(String deviceCode);

    /**
     * 批量更新维修记录状态
     * 
     * @param maintenanceIds 维修记录ID列表
     * @param status 状态
     * @return 更新数量
     */
    int batchUpdateMaintenanceStatus(List<Long> maintenanceIds, String status);

    /**
     * 批量开始维修
     * 
     * @param maintenanceIds 维修记录ID列表
     * @param repairedBy 维修人员
     * @return 更新数量
     */
    int batchStartMaintenance(List<Long> maintenanceIds, String repairedBy);

    /**
     * 批量完成维修
     * 
     * @param maintenanceIds 维修记录ID列表
     * @return 更新数量
     */
    int batchCompleteMaintenance(List<Long> maintenanceIds);

    /**
     * 获取维修记录统计信息
     * 
     * @return 统计信息
     */
    Map<String, Long> getMaintenanceStatistics();

    /**
     * 获取维修记录状态分布
     * 
     * @return 状态分布
     */
    Map<String, Long> getMaintenanceStatusDistribution();

    /**
     * 获取设备类型维修统计
     * 
     * @return 设备类型维修统计
     */
    Map<String, Long> getDeviceTypeMaintenanceStatistics();

    /**
     * 获取月度维修报表
     * 
     * @param months 月数
     * @return 月度维修报表
     */
    List<Map<String, Object>> getMonthlyMaintenanceReport(int months);

    /**
     * 获取日维修统计
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日维修统计
     */
    List<Map<String, Object>> getDailyMaintenanceStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取设备维修统计
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 设备维修统计
     */
    List<Map<String, Object>> getDeviceMaintenanceStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取设备故障率统计
     * 
     * @param days 天数
     * @return 设备故障率统计
     */
    Map<String, Double> getDeviceFailureRateStatistics(int days);

    /**
     * 获取最近的维修记录
     * 
     * @param limit 数量限制
     * @return 维修记录列表
     */
    List<MaintenanceRecord> getRecentMaintenanceRecords(int limit);

    /**
     * 根据日期范围查找维修记录
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 维修记录列表
     */
    List<MaintenanceRecord> findMaintenanceRecordsByDateRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取维修记录创建时间统计
     * 
     * @param days 天数
     * @return 创建时间统计
     */
    Map<String, Long> getMaintenanceCreationTimeStatistics(int days);

    /**
     * 验证维修单号格式
     * 
     * @param maintenanceCode 维修单号
     * @return 是否格式正确
     */
    boolean validateMaintenanceCodeFormat(String maintenanceCode);

    /**
     * 生成维修单号
     * 
     * @return 维修单号
     */
    String generateMaintenanceCode();

    /**
     * 检查维修单号冲突
     * 
     * @param maintenanceCode 维修单号
     * @param excludeId 排除的ID
     * @return 是否冲突
     */
    boolean checkMaintenanceCodeConflict(String maintenanceCode, Long excludeId);

    /**
     * 验证设备是否可以报修
     * 
     * @param deviceId 设备ID
     * @return 是否可以报修
     */
    boolean validateDeviceForMaintenance(Long deviceId);

    /**
     * 验证设备是否可以报修
     * 
     * @param deviceCode 设备编号
     * @return 是否可以报修
     */
    boolean validateDeviceForMaintenance(String deviceCode);

    /**
     * 获取维修记录详细信息
     * 
     * @param maintenanceId 维修记录ID
     * @return 详细信息
     */
    Map<String, Object> getMaintenanceDetailInfo(Long maintenanceId);

    /**
     * 获取维修进度信息
     * 
     * @param maintenanceId 维修记录ID
     * @return 进度信息
     */
    Map<String, Object> getMaintenanceProgress(Long maintenanceId);

    /**
     * 计算维修持续时间
     * 
     * @param maintenanceId 维修记录ID
     * @return 持续时间（分钟）
     */
    Long calculateMaintenanceDuration(Long maintenanceId);

    /**
     * 获取维修效率统计
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 维修效率统计
     */
    Map<String, Object> getMaintenanceEfficiencyStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取设备维修排名
     * 
     * @param days 天数
     * @return 设备维修排名
     */
    List<Map<String, Object>> getDeviceMaintenanceRanking(int days);

    /**
     * 获取维修状态列表
     * 
     * @return 维修状态列表
     */
    List<String> getMaintenanceStatuses();

    /**
     * 重置所有维修记录状态
     * 
     * @return 重置数量
     */
    int resetAllMaintenanceStatus();

    /**
     * 获取超时维修记录
     * 
     * @param hours 超时小时数
     * @return 超时维修记录列表
     */
    List<MaintenanceRecord> getOverdueMaintenanceRecords(int hours);

    /**
     * 获取长时间维修记录
     * 
     * @param hours 小时数
     * @return 长时间维修记录列表
     */
    List<MaintenanceRecord> getLongRunningMaintenanceRecords(int hours);

    /**
     * 获取维修类型统计
     * 
     * @return 维修类型统计
     */
    Map<String, Long> getMaintenanceTypeStatistics();

    /**
     * 获取维修人员统计
     * 
     * @return 维修人员统计
     */
    Map<String, Long> getMaintenancePersonnelStatistics();

    /**
     * 获取设备平均维修时间
     * 
     * @param deviceId 设备ID
     * @return 平均维修时间（分钟）
     */
    Double getAverageMaintenanceTime(Long deviceId);

    /**
     * 获取设备维修历史
     * 
     * @param deviceId 设备ID
     * @param limit 数量限制
     * @return 维修历史列表
     */
    List<MaintenanceRecord> getDeviceMaintenanceHistory(Long deviceId, int limit);
}