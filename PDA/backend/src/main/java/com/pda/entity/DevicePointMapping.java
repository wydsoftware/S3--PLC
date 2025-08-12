package com.pda.entity;

import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * 设备点位映射实体类
 * 用于存储设备与PLC点位的映射关系
 */
@Entity
@Table(name = "device_point_mappings", indexes = {
    @Index(name = "idx_device_id", columnList = "device_id"),
    @Index(name = "idx_device_code", columnList = "device_code"),
    @Index(name = "idx_point_type", columnList = "point_type")
})
public class DevicePointMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联设备ID
     */
    @NotNull(message = "设备ID不能为空")
    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    /**
     * 设备编号
     */
    @NotBlank(message = "设备编号不能为空")
    @Size(max = 50, message = "设备编号长度不能超过50个字符")
    @Column(name = "device_code", nullable = false, length = 50)
    private String deviceCode;

    /**
     * 设备类型
     */
    @NotNull(message = "设备类型不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 20)
    private DeviceType deviceType;

    /**
     * 点位类型（RESET-清零点位，DATA-数据点位）
     */
    @NotNull(message = "点位类型不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "point_type", nullable = false, length = 20)
    private PointType pointType;

    /**
     * PLC地址
     */
    @NotBlank(message = "PLC地址不能为空")
    @Size(max = 50, message = "PLC地址长度不能超过50个字符")
    @Column(name = "plc_address", nullable = false, length = 50)
    private String plcAddress;

    /**
     * 数据类型（BOOL, INT, DINT, REAL等）
     */
    @NotBlank(message = "数据类型不能为空")
    @Size(max = 20, message = "数据类型长度不能超过20个字符")
    @Column(name = "data_type", nullable = false, length = 20)
    private String dataType = "BOOL";

    /**
     * 读写权限（READ, write, read_write）
     */
    @NotBlank(message = "读写权限不能为空")
    @Size(max = 20, message = "读写权限长度不能超过20个字符")
    @Column(name = "access_type", nullable = false, length = 20)
    private String accessType = "read_write";

    /**
     * 点位描述
     */
    @Size(max = 200, message = "点位描述长度不能超过200个字符")
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 排序序号
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

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
     * 点位类型枚举
     */
    public enum PointType {
        RESET("清零点位", "用于清零设备计数器的点位"),
        DATA("数据点位", "用于读取设备生产数据的点位"),
        STATUS("状态点位", "用于读取设备状态的点位"),
        CONTROL("控制点位", "用于控制设备运行的点位");

        private final String displayName;
        private final String description;

        PointType(String displayName, String description) {
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
         * 根据显示名称获取点位类型
         */
        public static PointType getByDisplayName(String displayName) {
            for (PointType type : values()) {
                if (type.getDisplayName().equals(displayName)) {
                    return type;
                }
            }
            return null;
        }

        /**
         * 检查是否为清零点位
         */
        public boolean isResetPoint() {
            return this == RESET;
        }

        /**
         * 检查是否为数据点位
         */
        public boolean isDataPoint() {
            return this == DATA;
        }

        /**
         * 检查是否为状态点位
         */
        public boolean isStatusPoint() {
            return this == STATUS;
        }

        /**
         * 检查是否为控制点位
         */
        public boolean isControlPoint() {
            return this == CONTROL;
        }
    }

    // 构造函数
    public DevicePointMapping() {}

    public DevicePointMapping(Long deviceId, String deviceCode, DeviceType deviceType, 
                             PointType pointType, String plcAddress) {
        this.deviceId = deviceId;
        this.deviceCode = deviceCode;
        this.deviceType = deviceType;
        this.pointType = pointType;
        this.plcAddress = plcAddress;
    }

    public DevicePointMapping(Long deviceId, String deviceCode, DeviceType deviceType, 
                             PointType pointType, String plcAddress, String dataType, String accessType) {
        this.deviceId = deviceId;
        this.deviceCode = deviceCode;
        this.deviceType = deviceType;
        this.pointType = pointType;
        this.plcAddress = plcAddress;
        this.dataType = dataType;
        this.accessType = accessType;
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

    public PointType getPointType() {
        return pointType;
    }

    public void setPointType(PointType pointType) {
        this.pointType = pointType;
    }

    public String getPlcAddress() {
        return plcAddress;
    }

    public void setPlcAddress(String plcAddress) {
        this.plcAddress = plcAddress;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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
     * 检查是否启用
     */
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    /**
     * 检查是否可读
     */
    public boolean isReadable() {
        return "read".equals(accessType) || "read_write".equals(accessType);
    }

    /**
     * 检查是否可写
     */
    public boolean isWritable() {
        return "write".equals(accessType) || "read_write".equals(accessType);
    }

    /**
     * 检查是否为布尔类型
     */
    public boolean isBooleanType() {
        return "BOOL".equalsIgnoreCase(dataType);
    }

    /**
     * 检查是否为整数类型
     */
    public boolean isIntegerType() {
        return "INT".equalsIgnoreCase(dataType) || "DINT".equalsIgnoreCase(dataType);
    }

    /**
     * 检查是否为实数类型
     */
    public boolean isRealType() {
        return "REAL".equalsIgnoreCase(dataType);
    }

    /**
     * 获取完整的点位标识
     */
    public String getPointIdentifier() {
        return deviceCode + "_" + pointType.name() + "_" + plcAddress;
    }

    /**
     * 获取点位类型显示名称
     */
    public String getPointTypeDisplayName() {
        return pointType != null ? pointType.getDisplayName() : "";
    }

    /**
     * 获取设备类型显示名称
     */
    public String getDeviceTypeDisplayName() {
        return deviceType != null ? deviceType.getDisplayName() : "";
    }

    /**
     * 获取访问类型显示名称
     */
    public String getAccessTypeDisplayName() {
        switch (accessType) {
            case "read":
                return "只读";
            case "write":
                return "只写";
            case "read_write":
                return "读写";
            default:
                return "未知";
        }
    }

    @Override
    public String toString() {
        return "DevicePointMapping{" +
                "id=" + id +
                ", deviceCode='" + deviceCode + '\'' +
                ", deviceType=" + deviceType +
                ", pointType=" + pointType +
                ", plcAddress='" + plcAddress + '\'' +
                ", dataType='" + dataType + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}