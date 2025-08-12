package com.pda.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 设备维修记录实体类
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "maintenance_records", indexes = {
    @Index(name = "idx_device_id", columnList = "device_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_report_time", columnList = "report_time"),
    @Index(name = "idx_repair_time", columnList = "repair_time")
})
public class MaintenanceRecord {

    /**
     * 维修记录ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

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
     * 故障描述
     */
    @Size(max = 500, message = "故障描述长度不能超过500个字符")
    @Column(name = "fault_description", length = 500)
    private String faultDescription;

    /**
     * 维修状态
     */
    @Column(name = "status", length = 20)
    private String status = "reported";

    /**
     * 报修时间
     */
    @Column(name = "report_time")
    private LocalDateTime reportTime;

    /**
     * 维修开始时间
     */
    @Column(name = "repair_start_time")
    private LocalDateTime repairStartTime;

    /**
     * 维修完成时间
     */
    @Column(name = "repair_time")
    private LocalDateTime repairTime;

    /**
     * 维修人员
     */
    @Column(name = "repair_person", length = 50)
    private String repairPerson;

    /**
     * 维修说明
     */
    @Size(max = 500, message = "维修说明长度不能超过500个字符")
    @Column(name = "repair_description", length = 500)
    private String repairDescription;

    /**
     * 报修人
     */
    @Column(name = "reported_by", length = 50)
    private String reportedBy;

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
     * 维修状态枚举
     */
    public enum MaintenanceStatus {
        REPORTED("reported", "已报修", "#ffc107"),
        IN_PROGRESS("in_progress", "维修中", "#007bff"),
        COMPLETED("completed", "已修复", "#28a745"),
        CANCELLED("cancelled", "已取消", "#6c757d");

        private final String code;
        private final String displayName;
        private final String color;

        MaintenanceStatus(String code, String displayName, String color) {
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

        public static MaintenanceStatus fromCode(String code) {
            for (MaintenanceStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return REPORTED; // 默认返回已报修状态
        }
    }

    /**
     * 检查维修记录是否可以开始维修
     */
    public boolean canStartRepair() {
        return MaintenanceStatus.REPORTED.getCode().equals(this.status);
    }

    /**
     * 检查维修记录是否正在维修
     */
    public boolean isInProgress() {
        return MaintenanceStatus.IN_PROGRESS.getCode().equals(this.status);
    }

    /**
     * 检查维修记录是否已完成
     */
    public boolean isCompleted() {
        return MaintenanceStatus.COMPLETED.getCode().equals(this.status);
    }

    /**
     * 检查维修记录是否可以完成
     */
    public boolean canComplete() {
        return MaintenanceStatus.IN_PROGRESS.getCode().equals(this.status) ||
               MaintenanceStatus.REPORTED.getCode().equals(this.status);
    }

    /**
     * 获取状态显示名称
     */
    public String getStatusDisplayName() {
        return MaintenanceStatus.fromCode(this.status).getDisplayName();
    }

    /**
     * 获取状态颜色
     */
    public String getStatusColor() {
        return MaintenanceStatus.fromCode(this.status).getColor();
    }

    /**
     * 计算维修时长（分钟）
     */
    public Long getRepairDurationMinutes() {
        if (this.repairStartTime == null) {
            return 0L;
        }
        
        LocalDateTime endTime = this.repairTime != null ? this.repairTime : LocalDateTime.now();
        return ChronoUnit.MINUTES.between(this.repairStartTime, endTime);
    }

    /**
     * 计算维修时长（小时）
     */
    public Double getRepairDurationHours() {
        Long minutes = getRepairDurationMinutes();
        return minutes != null ? Math.round(minutes / 60.0 * 100.0) / 100.0 : 0.0;
    }

    /**
     * 计算总停机时长（分钟）
     */
    public Long getTotalDowntimeMinutes() {
        if (this.reportTime == null) {
            return 0L;
        }
        
        LocalDateTime endTime = this.repairTime != null ? this.repairTime : LocalDateTime.now();
        return ChronoUnit.MINUTES.between(this.reportTime, endTime);
    }

    /**
     * 计算总停机时长（小时）
     */
    public Double getTotalDowntimeHours() {
        Long minutes = getTotalDowntimeMinutes();
        return minutes != null ? Math.round(minutes / 60.0 * 100.0) / 100.0 : 0.0;
    }

    /**
     * 获取维修时长描述
     */
    public String getRepairDurationDescription() {
        Long minutes = getRepairDurationMinutes();
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
     * 获取停机时长描述
     */
    public String getDowntimeDescription() {
        Long minutes = getTotalDowntimeMinutes();
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
     * 开始维修
     */
    public void startRepair(String repairPerson) {
        this.status = MaintenanceStatus.IN_PROGRESS.getCode();
        this.repairStartTime = LocalDateTime.now();
        this.repairPerson = repairPerson;
    }

    /**
     * 完成维修
     */
    public void completeRepair(String repairPerson, String repairDescription) {
        this.status = MaintenanceStatus.COMPLETED.getCode();
        this.repairTime = LocalDateTime.now();
        this.repairPerson = repairPerson;
        this.repairDescription = repairDescription;
        
        // 如果没有设置维修开始时间，则设置为当前时间
        if (this.repairStartTime == null) {
            this.repairStartTime = this.repairTime;
        }
    }

    /**
     * 取消维修
     */
    public void cancelMaintenance() {
        this.status = MaintenanceStatus.CANCELLED.getCode();
    }

    /**
     * 创建维修记录
     */
    public static MaintenanceRecord createMaintenanceRecord(Long deviceId, String deviceCode, 
                                                           String faultDescription, String reportedBy) {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setDeviceId(deviceId);
        record.setDeviceCode(deviceCode);
        record.setFaultDescription(faultDescription);
        record.setReportedBy(reportedBy);
        record.setReportTime(LocalDateTime.now());
        record.setStatus(MaintenanceStatus.REPORTED.getCode());
        return record;
    }
}