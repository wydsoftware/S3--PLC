package com.pda.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 数据统计实体类
 */
@Entity
@Table(name = "data_statistics", indexes = {
    @Index(name = "idx_stat_date", columnList = "stat_date"),
    @Index(name = "idx_stat_type", columnList = "stat_type"),
    @Index(name = "idx_device_id", columnList = "device_id"),
    @Index(name = "idx_device_type", columnList = "device_type")
})
public class DataStatistics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 统计日期
     */
    @NotNull(message = "统计日期不能为空")
    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    /**
     * 统计类型
     */
    @NotNull(message = "统计类型不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "stat_type", nullable = false, length = 20)
    private StatType statType;

    /**
     * 设备ID（可选，用于设备级统计）
     */
    @Column(name = "device_id")
    private Long deviceId;

    /**
     * 设备编号
     */
    @Size(max = 50, message = "设备编号长度不能超过50个字符")
    @Column(name = "device_code", length = 50)
    private String deviceCode;

    /**
     * 设备类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 20)
    private DeviceType deviceType;

    /**
     * 总生产数量
     */
    @Min(value = 0, message = "总生产数量不能小于0")
    @Column(name = "total_production", nullable = false)
    private Integer totalProduction = 0;

    /**
     * 合格数量
     */
    @Min(value = 0, message = "合格数量不能小于0")
    @Column(name = "qualified_count", nullable = false)
    private Integer qualifiedCount = 0;

    /**
     * 不合格数量
     */
    @Min(value = 0, message = "不合格数量不能小于0")
    @Column(name = "defective_count", nullable = false)
    private Integer defectiveCount = 0;

    /**
     * 合格率（百分比）
     */
    @DecimalMin(value = "0.00", message = "合格率不能小于0")
    @DecimalMax(value = "100.00", message = "合格率不能大于100")
    @Column(name = "qualified_rate", precision = 5, scale = 2)
    private BigDecimal qualifiedRate;

    /**
     * 设备运行时间（分钟）
     */
    @Min(value = 0, message = "设备运行时间不能小于0")
    @Column(name = "running_time", nullable = false)
    private Integer runningTime = 0;

    /**
     * 设备停机时间（分钟）
     */
    @Min(value = 0, message = "设备停机时间不能小于0")
    @Column(name = "downtime", nullable = false)
    private Integer downtime = 0;

    /**
     * 设备利用率（百分比）
     */
    @DecimalMin(value = "0.00", message = "设备利用率不能小于0")
    @DecimalMax(value = "100.00", message = "设备利用率不能大于100")
    @Column(name = "utilization_rate", precision = 5, scale = 2)
    private BigDecimal utilizationRate;

    /**
     * 完成派工单数量
     */
    @Min(value = 0, message = "完成派工单数量不能小于0")
    @Column(name = "completed_orders", nullable = false)
    private Integer completedOrders = 0;

    /**
     * 进行中派工单数量
     */
    @Min(value = 0, message = "进行中派工单数量不能小于0")
    @Column(name = "ongoing_orders", nullable = false)
    private Integer ongoingOrders = 0;

    /**
     * 维修次数
     */
    @Min(value = 0, message = "维修次数不能小于0")
    @Column(name = "maintenance_count", nullable = false)
    private Integer maintenanceCount = 0;

    /**
     * 故障次数
     */
    @Min(value = 0, message = "故障次数不能小于0")
    @Column(name = "failure_count", nullable = false)
    private Integer failureCount = 0;

    /**
     * 平均生产效率（件/小时）
     */
    @DecimalMin(value = "0.00", message = "平均生产效率不能小于0")
    @Column(name = "avg_efficiency", precision = 8, scale = 2)
    private BigDecimal avgEfficiency;

    /**
     * 最后更新时间
     */
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 统计类型枚举
     */
    public enum StatType {
        DAILY("日统计", "按日统计数据"),
        WEEKLY("周统计", "按周统计数据"),
        MONTHLY("月统计", "按月统计数据"),
        YEARLY("年统计", "按年统计数据"),
        DEVICE_DAILY("设备日统计", "按设备按日统计数据"),
        DEVICE_MONTHLY("设备月统计", "按设备按月统计数据"),
        TYPE_DAILY("类型日统计", "按设备类型按日统计数据"),
        TYPE_MONTHLY("类型月统计", "按设备类型按月统计数据");

        private final String displayName;
        private final String description;

        StatType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        /**
         * 根据显示名称获取统计类型
         */
        public static StatType getByDisplayName(String displayName) {
            for (StatType type : values()) {
                if (type.getDisplayName().equals(displayName)) {
                    return type;
                }
            }
            return null;
        }

        /**
         * 检查是否为设备级统计
         */
        public boolean isDeviceLevel() {
            return this == DEVICE_DAILY || this == DEVICE_MONTHLY;
        }

        /**
         * 检查是否为类型级统计
         */
        public boolean isTypeLevel() {
            return this == TYPE_DAILY || this == TYPE_MONTHLY;
        }

        /**
         * 检查是否为全局统计
         */
        public boolean isGlobalLevel() {
            return this == DAILY || this == WEEKLY || this == MONTHLY || this == YEARLY;
        }

        /**
         * 检查是否为日统计
         */
        public boolean isDailyType() {
            return this == DAILY || this == DEVICE_DAILY || this == TYPE_DAILY;
        }

        /**
         * 检查是否为月统计
         */
        public boolean isMonthlyType() {
            return this == MONTHLY || this == DEVICE_MONTHLY || this == TYPE_MONTHLY;
        }
    }

    // 构造函数
    public DataStatistics() {
        this.lastUpdated = LocalDateTime.now();
    }

    public DataStatistics(LocalDate statDate, StatType statType) {
        this();
        this.statDate = statDate;
        this.statType = statType;
    }

    public DataStatistics(LocalDate statDate, StatType statType, Long deviceId, String deviceCode, DeviceType deviceType) {
        this(statDate, statType);
        this.deviceId = deviceId;
        this.deviceCode = deviceCode;
        this.deviceType = deviceType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getStatDate() {
        return statDate;
    }

    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }

    public StatType getStatType() {
        return statType;
    }

    public void setStatType(StatType statType) {
        this.statType = statType;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public Integer getTotalProduction() {
        return totalProduction;
    }

    public void setTotalProduction(Integer totalProduction) {
        this.totalProduction = totalProduction;
    }

    public Integer getQualifiedCount() {
        return qualifiedCount;
    }

    public void setQualifiedCount(Integer qualifiedCount) {
        this.qualifiedCount = qualifiedCount;
    }

    public Integer getDefectiveCount() {
        return defectiveCount;
    }

    public void setDefectiveCount(Integer defectiveCount) {
        this.defectiveCount = defectiveCount;
    }

    public BigDecimal getQualifiedRate() {
        return qualifiedRate;
    }

    public void setQualifiedRate(BigDecimal qualifiedRate) {
        this.qualifiedRate = qualifiedRate;
    }

    public Integer getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(Integer runningTime) {
        this.runningTime = runningTime;
    }

    public Integer getDowntime() {
        return downtime;
    }

    public void setDowntime(Integer downtime) {
        this.downtime = downtime;
    }

    public BigDecimal getUtilizationRate() {
        return utilizationRate;
    }

    public void setUtilizationRate(BigDecimal utilizationRate) {
        this.utilizationRate = utilizationRate;
    }

    public Integer getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(Integer completedOrders) {
        this.completedOrders = completedOrders;
    }

    public Integer getOngoingOrders() {
        return ongoingOrders;
    }

    public void setOngoingOrders(Integer ongoingOrders) {
        this.ongoingOrders = ongoingOrders;
    }

    public Integer getMaintenanceCount() {
        return maintenanceCount;
    }

    public void setMaintenanceCount(Integer maintenanceCount) {
        this.maintenanceCount = maintenanceCount;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    public BigDecimal getAvgEfficiency() {
        return avgEfficiency;
    }

    public void setAvgEfficiency(BigDecimal avgEfficiency) {
        this.avgEfficiency = avgEfficiency;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 计算合格率
     */
    public void calculateQualifiedRate() {
        if (totalProduction != null && totalProduction > 0) {
            BigDecimal rate = new BigDecimal(qualifiedCount != null ? qualifiedCount : 0)
                    .multiply(new BigDecimal("100"))
                    .divide(new BigDecimal(totalProduction), 2, BigDecimal.ROUND_HALF_UP);
            this.qualifiedRate = rate;
        } else {
            this.qualifiedRate = BigDecimal.ZERO;
        }
    }

    /**
     * 计算设备利用率
     */
    public void calculateUtilizationRate() {
        int totalTime = (runningTime != null ? runningTime : 0) + (downtime != null ? downtime : 0);
        if (totalTime > 0) {
            BigDecimal rate = new BigDecimal(runningTime != null ? runningTime : 0)
                    .multiply(new BigDecimal("100"))
                    .divide(new BigDecimal(totalTime), 2, BigDecimal.ROUND_HALF_UP);
            this.utilizationRate = rate;
        } else {
            this.utilizationRate = BigDecimal.ZERO;
        }
    }

    /**
     * 计算平均生产效率
     */
    public void calculateAvgEfficiency() {
        if (runningTime != null && runningTime > 0 && totalProduction != null && totalProduction > 0) {
            // 转换为小时
            BigDecimal hours = new BigDecimal(runningTime).divide(new BigDecimal("60"), 2, BigDecimal.ROUND_HALF_UP);
            if (hours.compareTo(BigDecimal.ZERO) > 0) {
                this.avgEfficiency = new BigDecimal(totalProduction)
                        .divide(hours, 2, BigDecimal.ROUND_HALF_UP);
            } else {
                this.avgEfficiency = BigDecimal.ZERO;
            }
        } else {
            this.avgEfficiency = BigDecimal.ZERO;
        }
    }

    /**
     * 更新统计数据
     */
    public void updateStatistics() {
        calculateQualifiedRate();
        calculateUtilizationRate();
        calculateAvgEfficiency();
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 获取统计类型显示名称
     */
    public String getStatTypeDisplayName() {
        return statType != null ? statType.getDisplayName() : "";
    }

    /**
     * 获取设备类型显示名称
     */
    public String getDeviceTypeDisplayName() {
        return deviceType != null ? deviceType.getDisplayName() : "";
    }

    /**
     * 检查是否有生产数据
     */
    public boolean hasProductionData() {
        return totalProduction != null && totalProduction > 0;
    }

    /**
     * 检查是否有运行时间数据
     */
    public boolean hasRuntimeData() {
        return runningTime != null && runningTime > 0;
    }

    /**
     * 获取不合格率
     */
    public BigDecimal getDefectiveRate() {
        if (totalProduction != null && totalProduction > 0) {
            return new BigDecimal(defectiveCount != null ? defectiveCount : 0)
                    .multiply(new BigDecimal("100"))
                    .divide(new BigDecimal(totalProduction), 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 获取停机率
     */
    public BigDecimal getDowntimeRate() {
        int totalTime = (runningTime != null ? runningTime : 0) + (downtime != null ? downtime : 0);
        if (totalTime > 0) {
            return new BigDecimal(downtime != null ? downtime : 0)
                    .multiply(new BigDecimal("100"))
                    .divide(new BigDecimal(totalTime), 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "DataStatistics{" +
                "id=" + id +
                ", statDate=" + statDate +
                ", statType=" + statType +
                ", deviceCode='" + deviceCode + '\'' +
                ", totalProduction=" + totalProduction +
                ", qualifiedRate=" + qualifiedRate +
                ", utilizationRate=" + utilizationRate +
                '}';
    }
}