package com.pda.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 报警记录实体类
 */
@Entity
@Table(name = "alarm_records", indexes = {
    @Index(name = "idx_device_id", columnList = "device_id"),
    @Index(name = "idx_alarm_time", columnList = "alarm_time"),
    @Index(name = "idx_alarm_type", columnList = "alarm_type"),
    @Index(name = "idx_alarm_level", columnList = "alarm_level"),
    @Index(name = "idx_status", columnList = "status")
})
public class AlarmRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联设备ID
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
     * 报警类型
     */
    @NotNull(message = "报警类型不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "alarm_type", nullable = false, length = 30)
    private AlarmType alarmType;

    /**
     * 报警级别
     */
    @NotNull(message = "报警级别不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "alarm_level", nullable = false, length = 20)
    private AlarmLevel alarmLevel;

    /**
     * 报警标题
     */
    @NotBlank(message = "报警标题不能为空")
    @Size(max = 200, message = "报警标题长度不能超过200个字符")
    @Column(name = "alarm_title", nullable = false, length = 200)
    private String alarmTitle;

    /**
     * 报警内容
     */
    @NotBlank(message = "报警内容不能为空")
    @Column(name = "alarm_content", nullable = false, columnDefinition = "TEXT")
    private String alarmContent;

    /**
     * 报警时间
     */
    @NotNull(message = "报警时间不能为空")
    @Column(name = "alarm_time", nullable = false)
    private LocalDateTime alarmTime;

    /**
     * 报警状态
     */
    @NotNull(message = "报警状态不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AlarmStatus status;

    /**
     * 确认时间
     */
    @Column(name = "ack_time")
    private LocalDateTime ackTime;

    /**
     * 确认人ID
     */
    @Column(name = "ack_user_id")
    private Long ackUserId;

    /**
     * 确认人姓名
     */
    @Size(max = 50, message = "确认人姓名长度不能超过50个字符")
    @Column(name = "ack_username", length = 50)
    private String ackUsername;

    /**
     * 处理时间
     */
    @Column(name = "resolve_time")
    private LocalDateTime resolveTime;

    /**
     * 处理人ID
     */
    @Column(name = "resolve_user_id")
    private Long resolveUserId;

    /**
     * 处理人姓名
     */
    @Size(max = 50, message = "处理人姓名长度不能超过50个字符")
    @Column(name = "resolve_username", length = 50)
    private String resolveUsername;

    /**
     * 处理方案
     */
    @Column(name = "resolve_solution", columnDefinition = "TEXT")
    private String resolveSolution;

    /**
     * 处理结果
     */
    @Column(name = "resolve_result", columnDefinition = "TEXT")
    private String resolveResult;

    /**
     * 报警源（SYSTEM-系统，DEVICE-设备，USER-用户）
     */
    @Size(max = 20, message = "报警源长度不能超过20个字符")
    @Column(name = "alarm_source", length = 20)
    private String alarmSource = "SYSTEM";

    /**
     * 是否需要确认
     */
    @Column(name = "require_ack", nullable = false)
    private Boolean requireAck = true;

    /**
     * 是否自动清除
     */
    @Column(name = "auto_clear", nullable = false)
    private Boolean autoClear = false;

    /**
     * 重复次数
     */
    @Min(value = 1, message = "重复次数不能小于1")
    @Column(name = "repeat_count", nullable = false)
    private Integer repeatCount = 1;

    /**
     * 最后重复时间
     */
    @Column(name = "last_repeat_time")
    private LocalDateTime lastRepeatTime;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 与设备实体的多对一关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", insertable = false, updatable = false)
    private Device device;

    /**
     * 报警类型枚举
     */
    public enum AlarmType {
        DEVICE_OFFLINE("设备离线", "设备连接断开或无响应"),
        DEVICE_FAULT("设备故障", "设备运行异常或故障"),
        PRODUCTION_ABNORMAL("生产异常", "生产数据异常或超出阈值"),
        QUALITY_ISSUE("质量问题", "产品质量不合格"),
        MAINTENANCE_DUE("维修到期", "设备需要维修保养"),
        SYSTEM_ERROR("系统错误", "系统运行错误"),
        DATA_ABNORMAL("数据异常", "数据采集或处理异常"),
        COMMUNICATION_ERROR("通信错误", "PLC通信异常"),
        THRESHOLD_EXCEEDED("阈值超限", "监控指标超出设定阈值"),
        SECURITY_ALERT("安全警报", "安全相关的警报");

        private final String displayName;
        private final String description;

        AlarmType(String displayName, String description) {
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
         * 根据显示名称获取报警类型
         */
        public static AlarmType getByDisplayName(String displayName) {
            for (AlarmType type : values()) {
                if (type.getDisplayName().equals(displayName)) {
                    return type;
                }
            }
            return null;
        }

        /**
         * 检查是否为设备相关报警
         */
        public boolean isDeviceRelated() {
            return this == DEVICE_OFFLINE || this == DEVICE_FAULT || this == MAINTENANCE_DUE;
        }

        /**
         * 检查是否为生产相关报警
         */
        public boolean isProductionRelated() {
            return this == PRODUCTION_ABNORMAL || this == QUALITY_ISSUE;
        }

        /**
         * 检查是否为系统相关报警
         */
        public boolean isSystemRelated() {
            return this == SYSTEM_ERROR || this == DATA_ABNORMAL || this == COMMUNICATION_ERROR;
        }
    }

    /**
     * 报警级别枚举
     */
    public enum AlarmLevel {
        LOW("低级", "一般性提醒", "#28a745", 1),
        MEDIUM("中级", "需要关注的问题", "#ffc107", 2),
        HIGH("高级", "重要问题，需要及时处理", "#fd7e14", 3),
        CRITICAL("紧急", "严重问题，需要立即处理", "#dc3545", 4);

        private final String displayName;
        private final String description;
        private final String color;
        private final int priority;

        AlarmLevel(String displayName, String description, String color, int priority) {
            this.displayName = displayName;
            this.description = description;
            this.color = color;
            this.priority = priority;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public String getColor() {
            return color;
        }

        public int getPriority() {
            return priority;
        }

        /**
         * 根据显示名称获取报警级别
         */
        public static AlarmLevel getByDisplayName(String displayName) {
            for (AlarmLevel level : values()) {
                if (level.getDisplayName().equals(displayName)) {
                    return level;
                }
            }
            return null;
        }

        /**
         * 检查是否为高优先级
         */
        public boolean isHighPriority() {
            return this == HIGH || this == CRITICAL;
        }

        /**
         * 检查是否为紧急级别
         */
        public boolean isCritical() {
            return this == CRITICAL;
        }
    }

    /**
     * 报警状态枚举
     */
    public enum AlarmStatus {
        ACTIVE("活跃", "报警处于活跃状态", "#dc3545"),
        ACKNOWLEDGED("已确认", "报警已被确认", "#ffc107"),
        RESOLVED("已处理", "报警已被处理", "#28a745"),
        CLEARED("已清除", "报警已被清除", "#6c757d"),
        SUPPRESSED("已抑制", "报警被抑制", "#17a2b8");

        private final String displayName;
        private final String description;
        private final String color;

        AlarmStatus(String displayName, String description, String color) {
            this.displayName = displayName;
            this.description = description;
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public String getColor() {
            return color;
        }

        /**
         * 根据显示名称获取报警状态
         */
        public static AlarmStatus getByDisplayName(String displayName) {
            for (AlarmStatus status : values()) {
                if (status.getDisplayName().equals(displayName)) {
                    return status;
                }
            }
            return null;
        }

        /**
         * 检查是否为活跃状态
         */
        public boolean isActive() {
            return this == ACTIVE;
        }

        /**
         * 检查是否已确认
         */
        public boolean isAcknowledged() {
            return this == ACKNOWLEDGED;
        }

        /**
         * 检查是否已处理
         */
        public boolean isResolved() {
            return this == RESOLVED;
        }

        /**
         * 检查是否已结束
         */
        public boolean isFinished() {
            return this == RESOLVED || this == CLEARED;
        }
    }

    // 构造函数
    public AlarmRecord() {
        this.alarmTime = LocalDateTime.now();
        this.status = AlarmStatus.ACTIVE;
    }

    public AlarmRecord(AlarmType alarmType, AlarmLevel alarmLevel, String alarmTitle, String alarmContent) {
        this();
        this.alarmType = alarmType;
        this.alarmLevel = alarmLevel;
        this.alarmTitle = alarmTitle;
        this.alarmContent = alarmContent;
    }

    public AlarmRecord(Long deviceId, String deviceCode, DeviceType deviceType, 
                      AlarmType alarmType, AlarmLevel alarmLevel, String alarmTitle, String alarmContent) {
        this(alarmType, alarmLevel, alarmTitle, alarmContent);
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

    public AlarmType getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(AlarmType alarmType) {
        this.alarmType = alarmType;
    }

    public AlarmLevel getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(AlarmLevel alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public String getAlarmTitle() {
        return alarmTitle;
    }

    public void setAlarmTitle(String alarmTitle) {
        this.alarmTitle = alarmTitle;
    }

    public String getAlarmContent() {
        return alarmContent;
    }

    public void setAlarmContent(String alarmContent) {
        this.alarmContent = alarmContent;
    }

    public LocalDateTime getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(LocalDateTime alarmTime) {
        this.alarmTime = alarmTime;
    }

    public AlarmStatus getStatus() {
        return status;
    }

    public void setStatus(AlarmStatus status) {
        this.status = status;
    }

    public LocalDateTime getAckTime() {
        return ackTime;
    }

    public void setAckTime(LocalDateTime ackTime) {
        this.ackTime = ackTime;
    }

    public Long getAckUserId() {
        return ackUserId;
    }

    public void setAckUserId(Long ackUserId) {
        this.ackUserId = ackUserId;
    }

    public String getAckUsername() {
        return ackUsername;
    }

    public void setAckUsername(String ackUsername) {
        this.ackUsername = ackUsername;
    }

    public LocalDateTime getResolveTime() {
        return resolveTime;
    }

    public void setResolveTime(LocalDateTime resolveTime) {
        this.resolveTime = resolveTime;
    }

    public Long getResolveUserId() {
        return resolveUserId;
    }

    public void setResolveUserId(Long resolveUserId) {
        this.resolveUserId = resolveUserId;
    }

    public String getResolveUsername() {
        return resolveUsername;
    }

    public void setResolveUsername(String resolveUsername) {
        this.resolveUsername = resolveUsername;
    }

    public String getResolveSolution() {
        return resolveSolution;
    }

    public void setResolveSolution(String resolveSolution) {
        this.resolveSolution = resolveSolution;
    }

    public String getResolveResult() {
        return resolveResult;
    }

    public void setResolveResult(String resolveResult) {
        this.resolveResult = resolveResult;
    }

    public String getAlarmSource() {
        return alarmSource;
    }

    public void setAlarmSource(String alarmSource) {
        this.alarmSource = alarmSource;
    }

    public Boolean getRequireAck() {
        return requireAck;
    }

    public void setRequireAck(Boolean requireAck) {
        this.requireAck = requireAck;
    }

    public Boolean getAutoClear() {
        return autoClear;
    }

    public void setAutoClear(Boolean autoClear) {
        this.autoClear = autoClear;
    }

    public Integer getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(Integer repeatCount) {
        this.repeatCount = repeatCount;
    }

    public LocalDateTime getLastRepeatTime() {
        return lastRepeatTime;
    }

    public void setLastRepeatTime(LocalDateTime lastRepeatTime) {
        this.lastRepeatTime = lastRepeatTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    /**
     * 确认报警
     */
    public void acknowledge(Long userId, String username) {
        if (requireAck && status == AlarmStatus.ACTIVE) {
            this.status = AlarmStatus.ACKNOWLEDGED;
            this.ackTime = LocalDateTime.now();
            this.ackUserId = userId;
            this.ackUsername = username;
        }
    }

    /**
     * 处理报警
     */
    public void resolve(Long userId, String username, String solution, String result) {
        this.status = AlarmStatus.RESOLVED;
        this.resolveTime = LocalDateTime.now();
        this.resolveUserId = userId;
        this.resolveUsername = username;
        this.resolveSolution = solution;
        this.resolveResult = result;
    }

    /**
     * 清除报警
     */
    public void clear() {
        this.status = AlarmStatus.CLEARED;
    }

    /**
     * 抑制报警
     */
    public void suppress() {
        this.status = AlarmStatus.SUPPRESSED;
    }

    /**
     * 增加重复次数
     */
    public void incrementRepeatCount() {
        this.repeatCount++;
        this.lastRepeatTime = LocalDateTime.now();
    }

    /**
     * 获取报警类型显示名称
     */
    public String getAlarmTypeDisplayName() {
        return alarmType != null ? alarmType.getDisplayName() : "";
    }

    /**
     * 获取报警级别显示名称
     */
    public String getAlarmLevelDisplayName() {
        return alarmLevel != null ? alarmLevel.getDisplayName() : "";
    }

    /**
     * 获取报警状态显示名称
     */
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }

    /**
     * 获取报警级别颜色
     */
    public String getAlarmLevelColor() {
        return alarmLevel != null ? alarmLevel.getColor() : "#6c757d";
    }

    /**
     * 获取报警状态颜色
     */
    public String getStatusColor() {
        return status != null ? status.getColor() : "#6c757d";
    }

    /**
     * 检查是否需要确认
     */
    public boolean needsAcknowledgment() {
        return requireAck && status == AlarmStatus.ACTIVE;
    }

    /**
     * 检查是否已确认
     */
    public boolean isAcknowledged() {
        return status != null && status.isAcknowledged();
    }

    /**
     * 检查是否已处理
     */
    public boolean isResolved() {
        return status != null && status.isResolved();
    }

    /**
     * 检查是否为高优先级
     */
    public boolean isHighPriority() {
        return alarmLevel != null && alarmLevel.isHighPriority();
    }

    /**
     * 检查是否为紧急级别
     */
    public boolean isCritical() {
        return alarmLevel != null && alarmLevel.isCritical();
    }

    /**
     * 创建设备离线报警
     */
    public static AlarmRecord createDeviceOfflineAlarm(Long deviceId, String deviceCode, DeviceType deviceType) {
        return new AlarmRecord(deviceId, deviceCode, deviceType, 
                AlarmType.DEVICE_OFFLINE, AlarmLevel.HIGH, 
                "设备离线报警", "设备 " + deviceCode + " 连接断开或无响应");
    }

    /**
     * 创建设备故障报警
     */
    public static AlarmRecord createDeviceFaultAlarm(Long deviceId, String deviceCode, DeviceType deviceType, String faultDesc) {
        return new AlarmRecord(deviceId, deviceCode, deviceType, 
                AlarmType.DEVICE_FAULT, AlarmLevel.CRITICAL, 
                "设备故障报警", "设备 " + deviceCode + " 发生故障：" + faultDesc);
    }

    /**
     * 创建生产异常报警
     */
    public static AlarmRecord createProductionAbnormalAlarm(Long deviceId, String deviceCode, DeviceType deviceType, String abnormalDesc) {
        return new AlarmRecord(deviceId, deviceCode, deviceType, 
                AlarmType.PRODUCTION_ABNORMAL, AlarmLevel.MEDIUM, 
                "生产异常报警", "设备 " + deviceCode + " 生产异常：" + abnormalDesc);
    }

    @Override
    public String toString() {
        return "AlarmRecord{" +
                "id=" + id +
                ", deviceCode='" + deviceCode + '\'' +
                ", alarmType=" + alarmType +
                ", alarmLevel=" + alarmLevel +
                ", alarmTitle='" + alarmTitle + '\'' +
                ", status=" + status +
                ", alarmTime=" + alarmTime +
                '}';
    }
}