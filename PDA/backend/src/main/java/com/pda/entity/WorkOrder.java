package com.pda.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 派工单实体类
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "work_orders", indexes = {
    @Index(name = "idx_order_no", columnList = "order_no"),
    @Index(name = "idx_device_id", columnList = "device_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_start_time", columnList = "start_time"),
    @Index(name = "idx_end_time", columnList = "end_time")
})
public class WorkOrder {

    /**
     * 派工单ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 派工单号
     */
    @NotBlank(message = "派工单号不能为空")
    @Size(max = 50, message = "派工单号长度不能超过50个字符")
    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    private String orderNo;

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    /**
     * 设备编号
     */
    @NotBlank(message = "设备编号不能为空")
    @Column(name = "device_code", nullable = false, length = 20)
    private String deviceCode;

    /**
     * 预计生产数量
     */
    @NotNull(message = "预计生产数量不能为空")
    @Min(value = 1, message = "预计生产数量必须大于0")
    @Column(name = "planned_quantity", nullable = false)
    private Integer plannedQuantity;

    /**
     * 实际生产数量
     */
    @Column(name = "actual_quantity")
    private Integer actualQuantity = 0;

    /**
     * 开始生产时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 结束生产时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 派工单状态
     */
    @Column(name = "status", length = 20)
    private String status = "pending";

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

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
     * 设备关联（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", insertable = false, updatable = false)
    private Device device;

    /**
     * 派工单状态枚举
     */
    public enum WorkOrderStatus {
        PENDING("pending", "待开始", "#ffc107"),
        WORKING("working", "生产中", "#007bff"),
        COMPLETED("completed", "已完成", "#28a745"),
        STOPPED("stopped", "已停止", "#6c757d"),
        CANCELLED("cancelled", "已取消", "#dc3545");

        private final String code;
        private final String displayName;
        private final String color;

        WorkOrderStatus(String code, String displayName, String color) {
            this.code = code;
            this.displayName = displayName;
            this.color = color;
        }

        public String getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getColor() {
            return color;
        }

        public static WorkOrderStatus fromCode(String code) {
            for (WorkOrderStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return PENDING; // 默认返回待开始状态
        }
    }

    /**
     * 检查派工单是否可以开始生产
     */
    public boolean canStart() {
        return WorkOrderStatus.PENDING.getCode().equals(this.status);
    }

    /**
     * 检查派工单是否正在生产
     */
    public boolean isWorking() {
        return WorkOrderStatus.WORKING.getCode().equals(this.status);
    }

    /**
     * 检查派工单是否已完成
     */
    public boolean isCompleted() {
        return WorkOrderStatus.COMPLETED.getCode().equals(this.status) ||
               WorkOrderStatus.STOPPED.getCode().equals(this.status);
    }

    /**
     * 检查派工单是否可以停止
     */
    public boolean canStop() {
        return WorkOrderStatus.WORKING.getCode().equals(this.status);
    }

    /**
     * 获取状态显示名称
     */
    public String getStatusDisplayName() {
        return WorkOrderStatus.fromCode(this.status).getDisplayName();
    }

    /**
     * 获取状态颜色
     */
    public String getStatusColor() {
        return WorkOrderStatus.fromCode(this.status).getColor();
    }

    /**
     * 计算完成率
     */
    public Double getCompletionRate() {
        if (this.plannedQuantity == null || this.plannedQuantity == 0) {
            return 0.0;
        }
        if (this.actualQuantity == null) {
            return 0.0;
        }
        return Math.round((double) this.actualQuantity / this.plannedQuantity * 10000.0) / 100.0;
    }

    /**
     * 计算生产时长（分钟）
     */
    public Long getDurationMinutes() {
        if (this.startTime == null) {
            return 0L;
        }
        
        LocalDateTime endTime = this.endTime != null ? this.endTime : LocalDateTime.now();
        return ChronoUnit.MINUTES.between(this.startTime, endTime);
    }

    /**
     * 计算生产时长（小时）
     */
    public Double getDurationHours() {
        Long minutes = getDurationMinutes();
        return minutes != null ? Math.round(minutes / 60.0 * 100.0) / 100.0 : 0.0;
    }

    /**
     * 计算生产效率（件/小时）
     */
    public Double getProductionEfficiency() {
        Double hours = getDurationHours();
        if (hours == null || hours == 0.0 || this.actualQuantity == null) {
            return 0.0;
        }
        return Math.round(this.actualQuantity / hours * 100.0) / 100.0;
    }

    /**
     * 获取生产进度描述
     */
    public String getProgressDescription() {
        if (this.actualQuantity == null || this.plannedQuantity == null) {
            return "0/0 (0%)"; 
        }
        return String.format("%d/%d (%.1f%%)", 
            this.actualQuantity, this.plannedQuantity, getCompletionRate());
    }

    /**
     * 获取时长描述
     */
    public String getDurationDescription() {
        Long minutes = getDurationMinutes();
        if (minutes == null || minutes == 0) {
            return "0分钟";
        }
        
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        
        if (hours > 0) {
            return String.format("%d小时%d分钟", hours, remainingMinutes);
        } else {
            return String.format("%d分钟", remainingMinutes);
        }
    }

    /**
     * 开始生产
     */
    public void startProduction() {
        this.status = WorkOrderStatus.WORKING.getCode();
        this.startTime = LocalDateTime.now();
    }

    /**
     * 停止生产
     */
    public void stopProduction(Integer actualQuantity) {
        this.status = WorkOrderStatus.STOPPED.getCode();
        this.endTime = LocalDateTime.now();
        this.actualQuantity = actualQuantity != null ? actualQuantity : 0;
    }

    /**
     * 完成生产
     */
    public void completeProduction(Integer actualQuantity) {
        this.status = WorkOrderStatus.COMPLETED.getCode();
        this.endTime = LocalDateTime.now();
        this.actualQuantity = actualQuantity != null ? actualQuantity : this.plannedQuantity;
    }

    /**
     * 取消派工单
     */
    public void cancelWorkOrder() {
        this.status = WorkOrderStatus.CANCELLED.getCode();
        if (this.startTime != null && this.endTime == null) {
            this.endTime = LocalDateTime.now();
        }
    }
}