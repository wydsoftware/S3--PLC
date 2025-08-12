package com.pda.repository;

import com.pda.entity.PlcConfig;
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
 * PLC配置数据访问层接口
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
@Repository
public interface PlcConfigRepository extends JpaRepository<PlcConfig, Long> {

    /**
     * 根据配置名称查找PLC配置
     */
    Optional<PlcConfig> findByConfigName(String configName);

    /**
     * 检查配置名称是否存在
     */
    boolean existsByConfigName(String configName);

    /**
     * 根据PLC IP查找配置
     */
    List<PlcConfig> findByPlcIp(String plcIp);

    /**
     * 根据PLC IP和端口查找配置
     */
    Optional<PlcConfig> findByPlcIpAndPlcPort(String plcIp, Integer plcPort);

    /**
     * 检查PLC IP和端口是否存在
     */
    boolean existsByPlcIpAndPlcPort(String plcIp, Integer plcPort);

    /**
     * 查找启用的PLC配置
     */
    @Query("SELECT pc FROM PlcConfig pc WHERE pc.isActive = true")
    List<PlcConfig> findActiveConfigs();

    /**
     * 查找禁用的PLC配置
     */
    @Query("SELECT pc FROM PlcConfig pc WHERE pc.isActive = false")
    List<PlcConfig> findInactiveConfigs();

    /**
     * 查找默认PLC配置
     */
    @Query("SELECT pc FROM PlcConfig pc WHERE pc.isDefault = true AND pc.isActive = true")
    Optional<PlcConfig> findDefaultConfig();

    /**
     * 查找所有默认配置（包括禁用的）
     */
    @Query("SELECT pc FROM PlcConfig pc WHERE pc.isDefault = true")
    List<PlcConfig> findAllDefaultConfigs();

    /**
     * 根据配置名称模糊查询
     */
    @Query("SELECT pc FROM PlcConfig pc WHERE pc.configName LIKE %:configName%")
    List<PlcConfig> findByConfigNameLike(@Param("configName") String configName);

    /**
     * 分页查询PLC配置
     */
    @Query("SELECT pc FROM PlcConfig pc WHERE (:configName IS NULL OR pc.configName LIKE %:configName%) " +
           "AND (:plcIp IS NULL OR pc.plcIp LIKE %:plcIp%) " +
           "AND (:isActive IS NULL OR pc.isActive = :isActive) " +
           "ORDER BY pc.isDefault DESC, pc.createdTime DESC")
    Page<PlcConfig> findPlcConfigsWithConditions(@Param("configName") String configName,
                                                 @Param("plcIp") String plcIp,
                                                 @Param("isActive") Boolean isActive,
                                                 Pageable pageable);

    /**
     * 查找指定时间范围内创建的PLC配置
     */
    @Query("SELECT pc FROM PlcConfig pc WHERE pc.createdTime BETWEEN :startTime AND :endTime ORDER BY pc.createdTime DESC")
    List<PlcConfig> findPlcConfigsCreatedBetween(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 统计PLC配置总数
     */
    @Query("SELECT COUNT(pc) FROM PlcConfig pc")
    long countAllPlcConfigs();

    /**
     * 统计启用的PLC配置数量
     */
    @Query("SELECT COUNT(pc) FROM PlcConfig pc WHERE pc.isActive = true")
    long countActiveConfigs();

    /**
     * 统计禁用的PLC配置数量
     */
    @Query("SELECT COUNT(pc) FROM PlcConfig pc WHERE pc.isActive = false")
    long countInactiveConfigs();

    /**
     * 统计默认配置数量
     */
    @Query("SELECT COUNT(pc) FROM PlcConfig pc WHERE pc.isDefault = true")
    long countDefaultConfigs();

    /**
     * 更新PLC配置状态
     */
    @Modifying
    @Query("UPDATE PlcConfig pc SET pc.isActive = :isActive, pc.updatedTime = CURRENT_TIMESTAMP WHERE pc.id = :id")
    int updatePlcConfigStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);

    /**
     * 启用PLC配置
     */
    @Modifying
    @Query("UPDATE PlcConfig pc SET pc.isActive = true, pc.updatedTime = CURRENT_TIMESTAMP WHERE pc.id = :id")
    int enablePlcConfig(@Param("id") Long id);

    /**
     * 禁用PLC配置
     */
    @Modifying
    @Query("UPDATE PlcConfig pc SET pc.isActive = false, pc.updatedTime = CURRENT_TIMESTAMP WHERE pc.id = :id")
    int disablePlcConfig(@Param("id") Long id);

    /**
     * 设置为默认配置
     */
    @Modifying
    @Query("UPDATE PlcConfig pc SET pc.isDefault = true, pc.isActive = true, pc.updatedTime = CURRENT_TIMESTAMP WHERE pc.id = :id")
    int setAsDefaultConfig(@Param("id") Long id);

    /**
     * 取消默认配置
     */
    @Modifying
    @Query("UPDATE PlcConfig pc SET pc.isDefault = false, pc.updatedTime = CURRENT_TIMESTAMP WHERE pc.id = :id")
    int unsetDefaultConfig(@Param("id") Long id);

    /**
     * 取消所有默认配置
     */
    @Modifying
    @Query("UPDATE PlcConfig pc SET pc.isDefault = false, pc.updatedTime = CURRENT_TIMESTAMP WHERE pc.isDefault = true")
    int unsetAllDefaultConfigs();

    /**
     * 批量更新PLC配置状态
     */
    @Modifying
    @Query("UPDATE PlcConfig pc SET pc.isActive = :isActive, pc.updatedTime = CURRENT_TIMESTAMP WHERE pc.id IN :ids")
    int updatePlcConfigStatusBatch(@Param("ids") List<Long> ids, @Param("isActive") Boolean isActive);

    /**
     * 批量删除PLC配置
     */
    @Modifying
    @Query("DELETE FROM PlcConfig pc WHERE pc.id IN :ids AND pc.isDefault = false")
    int deletePlcConfigsBatch(@Param("ids") List<Long> ids);

    /**
     * 更新PLC配置连接参数
     */
    @Modifying
    @Query("UPDATE PlcConfig pc SET pc.plcIp = :plcIp, pc.plcPort = :plcPort, " +
           "pc.connectTimeout = :connectTimeout, pc.readTimeout = :readTimeout, " +
           "pc.retryCount = :retryCount, pc.retryInterval = :retryInterval, " +
           "pc.updatedTime = CURRENT_TIMESTAMP WHERE pc.id = :id")
    int updatePlcConfigConnection(@Param("id") Long id,
                                 @Param("plcIp") String plcIp,
                                 @Param("plcPort") Integer plcPort,
                                 @Param("connectTimeout") Integer connectTimeout,
                                 @Param("readTimeout") Integer readTimeout,
                                 @Param("retryCount") Integer retryCount,
                                 @Param("retryInterval") Integer retryInterval);

    /**
     * 检查除指定ID外配置名称是否存在
     */
    @Query("SELECT COUNT(pc) > 0 FROM PlcConfig pc WHERE pc.configName = :configName AND pc.id != :id")
    boolean existsByConfigNameAndIdNot(@Param("configName") String configName, @Param("id") Long id);

    /**
     * 检查除指定ID外PLC IP和端口是否存在
     */
    @Query("SELECT COUNT(pc) > 0 FROM PlcConfig pc WHERE pc.plcIp = :plcIp AND pc.plcPort = :plcPort AND pc.id != :id")
    boolean existsByPlcIpAndPlcPortAndIdNot(@Param("plcIp") String plcIp, @Param("plcPort") Integer plcPort, @Param("id") Long id);

    /**
     * 获取PLC配置状态统计
     */
    @Query("SELECT pc.isActive, COUNT(pc) FROM PlcConfig pc GROUP BY pc.isActive")
    List<Object[]> getPlcConfigStatusStatistics();

    /**
     * 获取PLC配置创建时间统计（按月）
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', pc.createdTime, '%Y-%m') as month, COUNT(pc) " +
           "FROM PlcConfig pc " +
           "WHERE pc.createdTime >= :startTime " +
           "GROUP BY FUNCTION('DATE_FORMAT', pc.createdTime, '%Y-%m') " +
           "ORDER BY month DESC")
    List<Object[]> getPlcConfigCreationStatistics(@Param("startTime") LocalDateTime startTime);

    /**
     * 搜索PLC配置
     */
    @Query("SELECT pc FROM PlcConfig pc WHERE pc.configName LIKE %:keyword% " +
           "OR pc.plcIp LIKE %:keyword% OR pc.description LIKE %:keyword%")
    List<PlcConfig> searchPlcConfigsByKeyword(@Param("keyword") String keyword);

    /**
     * 分页搜索PLC配置
     */
    @Query("SELECT pc FROM PlcConfig pc WHERE pc.configName LIKE %:keyword% " +
           "OR pc.plcIp LIKE %:keyword% OR pc.description LIKE %:keyword% ORDER BY pc.createdTime DESC")
    Page<PlcConfig> searchPlcConfigsByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 获取最近的PLC配置
     */
    @Query("SELECT pc FROM PlcConfig pc ORDER BY pc.createdTime DESC")
    List<PlcConfig> findRecentPlcConfigs(Pageable pageable);

    /**
     * 获取最近更新的PLC配置
     */
    @Query("SELECT pc FROM PlcConfig pc ORDER BY pc.updatedTime DESC")
    List<PlcConfig> findRecentlyUpdatedPlcConfigs(Pageable pageable);

    /**
     * 检查是否存在启用的默认配置
     */
    @Query("SELECT COUNT(pc) > 0 FROM PlcConfig pc WHERE pc.isDefault = true AND pc.isActive = true")
    boolean hasActiveDefaultConfig();

    /**
     * 获取可用的PLC配置列表（启用且有效的配置）
     */
    @Query("SELECT pc FROM PlcConfig pc WHERE pc.isActive = true " +
           "AND pc.plcIp IS NOT NULL AND pc.plcPort IS NOT NULL " +
           "ORDER BY pc.isDefault DESC, pc.configName ASC")
    List<PlcConfig> findAvailableConfigs();

    /**
     * 根据创建人查找PLC配置
     */
    @Query("SELECT pc FROM PlcConfig pc WHERE pc.createdBy = :createdBy ORDER BY pc.createdTime DESC")
    List<PlcConfig> findByCreatedBy(@Param("createdBy") String createdBy);

    /**
     * 获取PLC IP使用统计
     */
    @Query("SELECT pc.plcIp, COUNT(pc) FROM PlcConfig pc GROUP BY pc.plcIp ORDER BY COUNT(pc) DESC")
    List<Object[]> getPlcIpUsageStatistics();

    /**
     * 获取PLC端口使用统计
     */
    @Query("SELECT pc.plcPort, COUNT(pc) FROM PlcConfig pc GROUP BY pc.plcPort ORDER BY COUNT(pc) DESC")
    List<Object[]> getPlcPortUsageStatistics();

    /**
     * 查找超时配置相同的PLC配置
     */
    @Query("SELECT pc FROM PlcConfig pc WHERE pc.connectTimeout = :connectTimeout " +
           "AND pc.readTimeout = :readTimeout AND pc.retryCount = :retryCount")
    List<PlcConfig> findByTimeoutSettings(@Param("connectTimeout") Integer connectTimeout,
                                         @Param("readTimeout") Integer readTimeout,
                                         @Param("retryCount") Integer retryCount);

    /**
     * 获取配置使用频率统计（基于创建时间）
     */
    @Query("SELECT pc.configName, pc.plcIp, pc.plcPort, pc.isActive, pc.isDefault, pc.createdTime " +
           "FROM PlcConfig pc ORDER BY pc.createdTime DESC")
    List<Object[]> getConfigUsageReport();
}