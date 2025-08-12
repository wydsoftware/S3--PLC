package com.pda.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 生产统计实体类
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "production_statistics", indexes = {
    @Index(name = "idx_stat_date", columnList = "stat_date"),
    @Index(name = "idx_device_type", columnList = "device_type"),
    @Index(name = "idx_device_code", columnList = "device_code"),
    @Index(name = "idx_stat_type", columnList = "stat_type")
})
public class ProductionStatistics {

    /**
     * 统计记录ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
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
    @NotBlank(message = "统计类型不能为空")
    @Column(name = "stat_type", nullable = false, length = 20)
    private String statType;

    /**
     * 设备类型
     */
    @Column(name = "device_type", length = 20)
    private String deviceType;

    /**
     * 设备编号
     */
    @Column(name = "device_code", length = 20)
    private String deviceCode;

    /**
     * 派工单数量
     */
    @Min(value = 0, message = "派工单数量不能为负数")
    @Column(name = "work_order_count")
    private Integer workOrderCount = 0;

    /**
     * 计划生产数量
     */
    @Min(value = 0, message = "计划生产数量不能为负数")
    @Column(name = "planned_quantity")
    private Integer plannedQuantity = 0;

    /**
     * 实际生产数量
     */
    @Min(value = 0, message = "实际生产数量不能为负数")
    @Column(name = "actual_quantity")
    private Integer actualQuantity = 0;

    /**
     * 完成率
     */
    @Column(name = "completion_rate")
    private Double completionRate = 0.0;

    /**
     * 生产时长（分钟）
     */
    @Min(value = 0, message = "生产时长不能为负数")
    @Column(name = "production_minutes")
    private Long productionMinutes = 0L;

    /**
     * 停机时长（分钟）
     */
    @Min(value = 0, message = "停机时长不能为负数")
    @Column(name = "downtime_minutes")
    private Long downtimeMinutes = 0L;

    /**
     * 维修次数
     */
    @Min(value = 0, message = "维修次数不能为负数")
    @Column(name = "maintenance_count")
    private Integer maintenanceCount = 0;

    /**
     * 设备利用率
     */
    @Column(name = "utilization_rate")
    private Double utilizationRate = 0.0;

    /**
     * 生产效率（件/小时）
     */
    @Column(name = "production_efficiency")
    private Double productionEfficiency = 0.0;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    /**
     * 统计类型枚举
     */
    public enum StatType {
        DAILY("daily", "日统计"),
        MONTHLY("monthly", "月统计"),
        YEARLY("yearly", "年统计"),
        DEVICE_DAILY("device_daily", "设备日统计"),
        DEVICE_MONTHLY("device_monthly", "设备月统计"),
        TYPE_DAILY("type_daily", "设备类型日统计"),
        TYPE_MONTHLY("type_monthly", "设备类型月统计");

        private final String code;
        private final String displayName;

        StatType(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static StatType fromCode(String code) {
            for (StatType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return DAILY; // 默认返回日统计
        }
    }

    /**
     * 获取统计类型显示名称
     */
    public String getStatTypeDisplayName() {
        return StatType.fromCode(this.statType).getDisplayName();
    }

    /**
     * 获取格式化的统计日期
     */
    public String getFormattedStatDate() {
        if (this.statDate == null) {
            return "";
        }
        return this.statDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * 获取格式化的统计月份
     */
    public String getFormattedStatMonth() {
        if (this.statDate == null) {
            return "";
        }
        return this.statDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    /**
     * 获取格式化的统计年份
     */
    public String getFormattedStatYear() {
        if (this.statDate == null) {
            return "";
        }
        return this.statDate.format(DateTimeFormatter.ofPattern("yyyy"));
    }

    /**
     * 计算完成率
     */
    public void calculateCompletionRate() {
        if (this.plannedQuantity == null || this.plannedQuantity == 0) {
            this.completionRate = 0.0;
        } else {
            this.completionRate = Math.round((double) (this.actualQuantity != null ? this.actualQuantity : 0) 
                / this.plannedQuantity * 10000.0) / 100.0;
        }
    }

    /**
     * 计算设备利用率
     */
    public void calculateUtilizationRate() {
        long totalMinutes = (this.productionMinutes != null ? this.productionMinutes : 0L) + 
                           (this.downtimeMinutes != null ? this.downtimeMinutes : 0L);
        
        if (totalMinutes == 0) {
            this.utilizationRate = 0.0;
        } else {
            this.utilizationRate = Math.round((double) (this.productionMinutes != null ? this.productionMinutes : 0L) 
                / totalMinutes * 10000.0) / 100.0;
        }
    }

    /**
     * 计算生产效率
     */
    public void calculateProductionEfficiency() {
        if (this.productionMinutes == null || this.productionMinutes == 0) {
            this.productionEfficiency = 0.0;
        } else {
            double hours = this.productionMinutes / 60.0;
            this.productionEfficiency = Math.round((this.actualQuantity != null ? this.actualQuantity : 0) 
                / hours * 100.0) / 100.0;
        }
    }

    /**
     * 获取生产时长描述
     */
    public String getProductionTimeDescription() {
        if (this.productionMinutes == null || this.productionMinutes == 0) {
            return "0小时";
        }
        
        long hours = this.productionMinutes / 60;
        long minutes = this.productionMinutes % 60;
        
        if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes);
        } else {
            return String.format("%d分钟", minutes);
        }
    }

    /**
     * 获取停机时长描述
     */
    public String getDowntimeDescription() {
        if (this.downtimeMinutes == null || this.downtimeMinutes == 0) {
            return "0小时";
        }
        
        long hours = this.downtimeMinutes / 60;
        long minutes = this.downtimeMinutes % 60;
        
        if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes);
        } else {
            return String.format("%d分钟", minutes);
        }
    }

    /**
     * 获取完成率描述
     */
    public String getCompletionRateDescription() {
        return String.format("%.1f%%", this.completionRate != null ? this.completionRate : 0.0);
    }

    /**
     * 获取设备利用率描述
     */
    public String getUtilizationRateDescription() {
        return String.format("%.1f%%", this.utilizationRate != null ? this.utilizationRate : 0.0);
    }

    /**
     * 获取生产效率描述
     */
    public String getProductionEfficiencyDescription() {
        return String.format("%.1f件/小时", this.productionEfficiency != null ? this.productionEfficiency : 0.0);
    }

    /**
     * 更新统计数据
     */
    public void updateStatistics() {
        calculateCompletionRate();
        calculateUtilizationRate();
        calculateProductionEfficiency();
    }

    /**
     * 创建日统计记录
     */
    public static ProductionStatistics createDailyStats(LocalDate statDate) {
        ProductionStatistics stats = new ProductionStatistics();
        stats.setStatDate(statDate);
        stats.setStatType(StatType.DAILY.getCode());
        return stats;
    }

    /**
     * 创建设备日统计记录
     */
    public static ProductionStatistics createDeviceDailyStats(LocalDate statDate, String deviceType, String deviceCode) {
        ProductionStatistics stats = new ProductionStatistics();
        stats.setStatDate(statDate);
        stats.setStatType(StatType.DEVICE_DAILY.getCode());
        stats.setDeviceType(deviceType);
        stats.setDeviceCode(deviceCode);
        return stats;
    }

    /**
     * 创建月统计记录
     */
    public static ProductionStatistics createMonthlyStats(LocalDate statDate) {
        ProductionStatistics stats = new ProductionStatistics();
        stats.setStatDate(statDate.withDayOfMonth(1)); // 设置为月初
        stats.setStatType(StatType.MONTHLY.getCode());
        return stats;
    }

    /**
     * 创建设备月统计记录
     */
    public static ProductionStatistics createDeviceMonthlyStats(LocalDate statDate, String deviceType, String deviceCode) {
        ProductionStatistics stats = new ProductionStatistics();
        stats.setStatDate(statDate.withDayOfMonth(1)); // 设置为月初
        stats.setStatType(StatType.DEVICE_MONTHLY.getCode());
        stats.setDeviceType(deviceType);
        stats.setDeviceCode(deviceCode);
        return stats;
    }
}