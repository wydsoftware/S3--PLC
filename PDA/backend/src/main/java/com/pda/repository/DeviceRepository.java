package com.pda.repository;

import com.pda.entity.Device;
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
 * 设备数据访问层接口
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    /**
     * 根据设备编号查找设备
     */
    Optional<Device> findByDeviceCode(String deviceCode);

    /**
     * 检查设备编号是否存在
     */
    boolean existsByDeviceCode(String deviceCode);

    /**
     * 根据设备类型查找设备列表
     */
    List<Device> findByDeviceType(String deviceType);

    /**
     * 根据设备状态查找设备列表
     */
    List<Device> findByStatus(String status);

    /**
     * 根据设备类型和状态查找设备列表
     */
    List<Device> findByDeviceTypeAndStatus(String deviceType, String status);

    /**
     * 查找可用于生产的设备（空闲状态）
     */
    @Query("SELECT d FROM Device d WHERE d.status = 'idle'")
    List<Device> findAvailableDevices();

    /**
     * 根据设备类型查找可用设备
     */
    @Query("SELECT d FROM Device d WHERE d.deviceType = :deviceType AND d.status = 'idle'")
    List<Device> findAvailableDevicesByType(@Param("deviceType") String deviceType);

    /**
     * 查找正在生产的设备
     */
    @Query("SELECT d FROM Device d WHERE d.status = 'working'")
    List<Device> findWorkingDevices();

    /**
     * 查找维修中的设备
     */
    @Query("SELECT d FROM Device d WHERE d.status = 'maintenance'")
    List<Device> findMaintenanceDevices();

    /**
     * 根据设备名称模糊查询
     */
    @Query("SELECT d FROM Device d WHERE d.deviceName LIKE %:deviceName%")
    List<Device> findByDeviceNameLike(@Param("deviceName") String deviceName);

    /**
     * 根据设备编号模糊查询
     */
    @Query("SELECT d FROM Device d WHERE d.deviceCode LIKE %:deviceCode%")
    List<Device> findByDeviceCodeLike(@Param("deviceCode") String deviceCode);

    /**
     * 分页查询设备
     */
    @Query("SELECT d FROM Device d WHERE (:deviceCode IS NULL OR d.deviceCode LIKE %:deviceCode%) " +
           "AND (:deviceType IS NULL OR d.deviceType = :deviceType) " +
           "AND (:status IS NULL OR d.status = :status) " +
           "ORDER BY d.deviceCode ASC")
    Page<Device> findDevicesWithConditions(@Param("deviceCode") String deviceCode,
                                          @Param("deviceType") String deviceType,
                                          @Param("status") String status,
                                          Pageable pageable);

    /**
     * 统计设备总数
     */
    @Query("SELECT COUNT(d) FROM Device d")
    long countAllDevices();

    /**
     * 根据设备类型统计设备数量
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.deviceType = :deviceType")
    long countByDeviceType(@Param("deviceType") String deviceType);

    /**
     * 根据设备状态统计设备数量
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = :status")
    long countByStatus(@Param("status") String status);

    /**
     * 统计可用设备数量
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = 'idle'")
    long countAvailableDevices();

    /**
     * 统计正在生产的设备数量
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = 'working'")
    long countWorkingDevices();

    /**
     * 统计维修中的设备数量
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = 'maintenance'")
    long countMaintenanceDevices();

    /**
     * 根据PLC地址查找设备
     */
    @Query("SELECT d FROM Device d WHERE d.plcAddress = :plcAddress")
    List<Device> findByPlcAddress(@Param("plcAddress") String plcAddress);

    /**
     * 检查PLC地址是否存在
     */
    @Query("SELECT COUNT(d) > 0 FROM Device d WHERE d.plcAddress = :plcAddress")
    boolean existsByPlcAddress(@Param("plcAddress") String plcAddress);

    /**
     * 查找指定时间范围内创建的设备
     */
    @Query("SELECT d FROM Device d WHERE d.createdTime BETWEEN :startTime AND :endTime ORDER BY d.createdTime DESC")
    List<Device> findDevicesCreatedBetween(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 批量更新设备状态
     */
    @Modifying
    @Query("UPDATE Device d SET d.status = :status, d.updatedTime = CURRENT_TIMESTAMP WHERE d.id IN :ids")
    int updateDeviceStatusBatch(@Param("ids") List<Long> ids, @Param("status") String status);

    /**
     * 更新设备状态
     */
    @Modifying
    @Query("UPDATE Device d SET d.status = :status, d.updatedTime = CURRENT_TIMESTAMP WHERE d.id = :id")
    int updateDeviceStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 根据设备编号更新设备状态
     */
    @Modifying
    @Query("UPDATE Device d SET d.status = :status, d.updatedTime = CURRENT_TIMESTAMP WHERE d.deviceCode = :deviceCode")
    int updateDeviceStatusByCode(@Param("deviceCode") String deviceCode, @Param("status") String status);

    /**
     * 设置设备为工作状态
     */
    @Modifying
    @Query("UPDATE Device d SET d.status = 'working', d.updatedTime = CURRENT_TIMESTAMP WHERE d.id = :id")
    int setDeviceWorking(@Param("id") Long id);

    /**
     * 设置设备为空闲状态
     */
    @Modifying
    @Query("UPDATE Device d SET d.status = 'idle', d.updatedTime = CURRENT_TIMESTAMP WHERE d.id = :id")
    int setDeviceIdle(@Param("id") Long id);

    /**
     * 设置设备为维修状态
     */
    @Modifying
    @Query("UPDATE Device d SET d.status = 'maintenance', d.updatedTime = CURRENT_TIMESTAMP WHERE d.id = :id")
    int setDeviceMaintenance(@Param("id") Long id);

    /**
     * 检查除指定ID外设备编号是否存在
     */
    @Query("SELECT COUNT(d) > 0 FROM Device d WHERE d.deviceCode = :deviceCode AND d.id != :id")
    boolean existsByDeviceCodeAndIdNot(@Param("deviceCode") String deviceCode, @Param("id") Long id);

    /**
     * 检查除指定ID外PLC地址是否存在
     */
    @Query("SELECT COUNT(d) > 0 FROM Device d WHERE d.plcAddress = :plcAddress AND d.id != :id")
    boolean existsByPlcAddressAndIdNot(@Param("plcAddress") String plcAddress, @Param("id") Long id);

    /**
     * 获取设备类型统计
     */
    @Query("SELECT d.deviceType, COUNT(d) FROM Device d GROUP BY d.deviceType")
    List<Object[]> getDeviceTypeStatistics();

    /**
     * 获取设备状态统计
     */
    @Query("SELECT d.status, COUNT(d) FROM Device d GROUP BY d.status")
    List<Object[]> getDeviceStatusStatistics();

    /**
     * 获取设备类型和状态统计
     */
    @Query("SELECT d.deviceType, d.status, COUNT(d) FROM Device d GROUP BY d.deviceType, d.status")
    List<Object[]> getDeviceTypeStatusStatistics();

    /**
     * 搜索设备
     */
    @Query("SELECT d FROM Device d WHERE d.deviceCode LIKE %:keyword% OR d.deviceName LIKE %:keyword%")
    List<Device> searchDevicesByKeyword(@Param("keyword") String keyword);

    /**
     * 分页搜索设备
     */
    @Query("SELECT d FROM Device d WHERE d.deviceCode LIKE %:keyword% OR d.deviceName LIKE %:keyword% ORDER BY d.deviceCode ASC")
    Page<Device> searchDevicesByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据设备类型获取设备编号列表
     */
    @Query("SELECT d.deviceCode FROM Device d WHERE d.deviceType = :deviceType ORDER BY d.deviceCode ASC")
    List<String> getDeviceCodesByType(@Param("deviceType") String deviceType);

    /**
     * 根据设备类型获取可用设备编号列表
     */
    @Query("SELECT d.deviceCode FROM Device d WHERE d.deviceType = :deviceType AND d.status = 'idle' ORDER BY d.deviceCode ASC")
    List<String> getAvailableDeviceCodesByType(@Param("deviceType") String deviceType);

    /**
     * 获取所有设备编号列表
     */
    @Query("SELECT d.deviceCode FROM Device d ORDER BY d.deviceCode ASC")
    List<String> getAllDeviceCodes();

    /**
     * 获取所有可用设备编号列表
     */
    @Query("SELECT d.deviceCode FROM Device d WHERE d.status = 'idle' ORDER BY d.deviceCode ASC")
    List<String> getAllAvailableDeviceCodes();

    /**
     * 根据设备类型和编号范围查找设备
     */
    @Query("SELECT d FROM Device d WHERE d.deviceType = :deviceType " +
           "AND d.deviceCode >= :startCode AND d.deviceCode <= :endCode " +
           "ORDER BY d.deviceCode ASC")
    List<Device> findDevicesByTypeAndCodeRange(@Param("deviceType") String deviceType,
                                              @Param("startCode") String startCode,
                                              @Param("endCode") String endCode);

    /**
     * 获取设备利用率统计（最近30天）
     */
    @Query(value = "SELECT d.device_code, d.device_type, " +
                   "COUNT(wo.id) as work_order_count, " +
                   "COALESCE(SUM(TIMESTAMPDIFF(MINUTE, wo.start_time, wo.end_time)), 0) as total_minutes " +
                   "FROM devices d " +
                   "LEFT JOIN work_orders wo ON d.id = wo.device_id " +
                   "AND wo.start_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                   "GROUP BY d.id, d.device_code, d.device_type " +
                   "ORDER BY d.device_code", nativeQuery = true)
    List<Object[]> getDeviceUtilizationStatistics();
}