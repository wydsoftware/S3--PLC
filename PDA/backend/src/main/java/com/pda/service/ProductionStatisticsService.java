package com.pda.service;

import com.pda.entity.ProductionStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 生产统计服务接口
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
public interface ProductionStatisticsService {

    /**
     * 保存生产统计记录
     * 
     * @param productionStatistics 生产统计记录
     * @return 保存后的生产统计记录
     */
    ProductionStatistics saveProductionStatistics(ProductionStatistics productionStatistics);

    /**
     * 根据ID查找生产统计记录
     * 
     * @param id 生产统计记录ID
     * @return 生产统计记录
     */
    Optional<ProductionStatistics> findById(Long id);

    /**
     * 创建生产统计记录
     * 
     * @param statisticsDate 统计日期
     * @param statisticsType 统计类型
     * @param deviceCode 设备编号
     * @param deviceType 设备类型
     * @param productionQuantity 生产数量
     * @return 生产统计记录
     */
    ProductionStatistics createProductionStatistics(LocalDate statisticsDate, String statisticsType, 
                                                   String deviceCode, String deviceType, Integer productionQuantity);

    /**
     * 更新生产统计记录
     * 
     * @param id 生产统计记录ID
     * @param productionQuantity 生产数量
     * @param workingHours 工作时长
     * @param efficiency 效率
     * @return 更新后的生产统计记录
     */
    ProductionStatistics updateProductionStatistics(Long id, Integer productionQuantity, Double workingHours, Double efficiency);

    /**
     * 删除生产统计记录
     * 
     * @param id 生产统计记录ID
     * @return 是否删除成功
     */
    boolean deleteProductionStatistics(Long id);

    /**
     * 获取所有生产统计记录
     * 
     * @return 生产统计记录列表
     */
    List<ProductionStatistics> findAllProductionStatistics();

    /**
     * 根据统计日期查找生产统计记录
     * 
     * @param statisticsDate 统计日期
     * @return 生产统计记录列表
     */
    List<ProductionStatistics> findByStatisticsDate(LocalDate statisticsDate);

    /**
     * 根据统计类型查找生产统计记录
     * 
     * @param statisticsType 统计类型
     * @return 生产统计记录列表
     */
    List<ProductionStatistics> findByStatisticsType(String statisticsType);

    /**
     * 根据设备编号查找生产统计记录
     * 
     * @param deviceCode 设备编号
     * @return 生产统计记录列表
     */
    List<ProductionStatistics> findByDeviceCode(String deviceCode);

    /**
     * 根据设备类型查找生产统计记录
     * 
     * @param deviceType 设备类型
     * @return 生产统计记录列表
     */
    List<ProductionStatistics> findByDeviceType(String deviceType);

    /**
     * 根据日期范围查找生产统计记录
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 生产统计记录列表
     */
    List<ProductionStatistics> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 条件查询生产统计记录
     * 
     * @param statisticsDate 统计日期
     * @param statisticsType 统计类型
     * @param deviceCode 设备编号
     * @param deviceType 设备类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageable 分页参数
     * @return 生产统计记录分页结果
     */
    Page<ProductionStatistics> findProductionStatisticsWithConditions(LocalDate statisticsDate, String statisticsType, 
                                                                      String deviceCode, String deviceType, 
                                                                      LocalDate startDate, LocalDate endDate, 
                                                                      Pageable pageable);

    /**
     * 搜索生产统计记录
     * 
     * @param keyword 关键词
     * @return 生产统计记录列表
     */
    List<ProductionStatistics> searchProductionStatistics(String keyword);

    /**
     * 搜索生产统计记录（分页）
     * 
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 生产统计记录分页结果
     */
    Page<ProductionStatistics> searchProductionStatistics(String keyword, Pageable pageable);

    /**
     * 检查生产统计记录是否存在
     * 
     * @param statisticsDate 统计日期
     * @param statisticsType 统计类型
     * @param deviceCode 设备编号
     * @return 是否存在
     */
    boolean existsProductionStatistics(LocalDate statisticsDate, String statisticsType, String deviceCode);

    /**
     * 批量更新生产统计记录
     * 
     * @param statisticsIds 生产统计记录ID列表
     * @param productionQuantity 生产数量
     * @return 更新数量
     */
    int batchUpdateProductionQuantity(List<Long> statisticsIds, Integer productionQuantity);

    /**
     * 批量删除生产统计记录
     * 
     * @param statisticsIds 生产统计记录ID列表
     * @return 删除数量
     */
    int batchDeleteProductionStatistics(List<Long> statisticsIds);

    /**
     * 获取生产统计汇总信息
     * 
     * @return 汇总信息
     */
    Map<String, Object> getProductionStatisticsSummary();

    /**
     * 获取统计类型分布
     * 
     * @return 统计类型分布
     */
    Map<String, Long> getStatisticsTypeDistribution();

    /**
     * 获取设备类型生产统计
     * 
     * @return 设备类型生产统计
     */
    Map<String, Long> getDeviceTypeProductionStatistics();

    /**
     * 获取月度生产汇总
     * 
     * @param months 月数
     * @return 月度生产汇总
     */
    List<Map<String, Object>> getMonthlyProductionSummary(int months);

    /**
     * 获取设备效率排名
     * 
     * @param days 天数
     * @return 设备效率排名
     */
    List<Map<String, Object>> getDeviceEfficiencyRanking(int days);

    /**
     * 获取设备类型对比数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 设备类型对比数据
     */
    List<Map<String, Object>> getDeviceTypeComparisonData(LocalDate startDate, LocalDate endDate);

    /**
     * 获取生产趋势数据
     * 
     * @param days 天数
     * @return 生产趋势数据
     */
    List<Map<String, Object>> getProductionTrendData(int days);

    /**
     * 获取设备利用率趋势
     * 
     * @param deviceCode 设备编号
     * @param days 天数
     * @return 设备利用率趋势
     */
    List<Map<String, Object>> getDeviceUtilizationTrend(String deviceCode, int days);

    /**
     * 获取设备类型利用率对比
     * 
     * @param days 天数
     * @return 设备类型利用率对比
     */
    List<Map<String, Object>> getDeviceTypeUtilizationComparison(int days);

    /**
     * 获取日生产统计
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日生产统计
     */
    List<Map<String, Object>> getDailyProductionStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取设备生产统计
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 设备生产统计
     */
    List<Map<String, Object>> getDeviceProductionStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取最近的生产统计记录
     * 
     * @param limit 数量限制
     * @return 生产统计记录列表
     */
    List<ProductionStatistics> getRecentProductionStatistics(int limit);

    /**
     * 获取生产统计创建时间统计
     * 
     * @param days 天数
     * @return 创建时间统计
     */
    Map<String, Long> getProductionStatisticsCreationTimeStatistics(int days);

    /**
     * 获取统计类型列表
     * 
     * @return 统计类型列表
     */
    List<String> getStatisticsTypes();

    /**
     * 获取设备类型列表
     * 
     * @return 设备类型列表
     */
    List<String> getDeviceTypes();

    /**
     * 计算设备效率
     * 
     * @param deviceCode 设备编号
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 设备效率
     */
    Double calculateDeviceEfficiency(String deviceCode, LocalDate startDate, LocalDate endDate);

    /**
     * 计算设备利用率
     * 
     * @param deviceCode 设备编号
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 设备利用率
     */
    Double calculateDeviceUtilization(String deviceCode, LocalDate startDate, LocalDate endDate);

    /**
     * 获取生产报表数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param deviceType 设备类型
     * @param deviceCode 设备编号
     * @return 生产报表数据
     */
    Map<String, Object> getProductionReportData(LocalDate startDate, LocalDate endDate, String deviceType, String deviceCode);

    /**
     * 获取设备生产能力分析
     * 
     * @param deviceCode 设备编号
     * @param days 天数
     * @return 生产能力分析
     */
    Map<String, Object> getDeviceProductionCapacityAnalysis(String deviceCode, int days);

    /**
     * 获取生产异常统计
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 生产异常统计
     */
    Map<String, Object> getProductionAnomalyStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取设备性能对比
     * 
     * @param deviceCodes 设备编号列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 设备性能对比
     */
    List<Map<String, Object>> getDevicePerformanceComparison(List<String> deviceCodes, LocalDate startDate, LocalDate endDate);

    /**
     * 生成生产统计报告
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param reportType 报告类型
     * @return 生产统计报告
     */
    Map<String, Object> generateProductionStatisticsReport(LocalDate startDate, LocalDate endDate, String reportType);

    /**
     * 同步生产数据
     * 
     * @param statisticsDate 统计日期
     * @return 同步数量
     */
    int syncProductionData(LocalDate statisticsDate);

    /**
     * 批量同步生产数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 同步数量
     */
    int batchSyncProductionData(LocalDate startDate, LocalDate endDate);

    /**
     * 重新计算统计数据
     * 
     * @param statisticsDate 统计日期
     * @return 重新计算数量
     */
    int recalculateStatistics(LocalDate statisticsDate);

    /**
     * 获取生产预测数据
     * 
     * @param deviceCode 设备编号
     * @param days 预测天数
     * @return 生产预测数据
     */
    List<Map<String, Object>> getProductionForecastData(String deviceCode, int days);

    /**
     * 获取设备维护建议
     * 
     * @param deviceCode 设备编号
     * @return 维护建议
     */
    Map<String, Object> getDeviceMaintenanceSuggestions(String deviceCode);

    /**
     * 获取生产质量统计
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 生产质量统计
     */
    Map<String, Object> getProductionQualityStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取设备停机时间统计
     * 
     * @param deviceCode 设备编号
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 停机时间统计
     */
    Map<String, Object> getDeviceDowntimeStatistics(String deviceCode, LocalDate startDate, LocalDate endDate);

    /**
     * 获取生产成本分析
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 生产成本分析
     */
    Map<String, Object> getProductionCostAnalysis(LocalDate startDate, LocalDate endDate);

    /**
     * 获取设备ROI分析
     * 
     * @param deviceCode 设备编号
     * @param months 月数
     * @return ROI分析
     */
    Map<String, Object> getDeviceROIAnalysis(String deviceCode, int months);
}