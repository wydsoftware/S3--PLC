package com.pda.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 设备实体类
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "devices", indexes = {
    @Index(name = "idx_device_code", columnList = "device_code"),
    @Index(name = "idx_device_type", columnList = "device_type"),
    @Index(name = "idx_status", columnList = "status")
})
public class Device {

    /**
     * 设备ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 设备编号
     */
    @NotBlank(message = "设备编号不能为空")
    @Size(max = 20, message = "设备编号长度不能超过20个字符")
    @Column(name = "device_code", nullable = false, unique = true, length = 20)
    private String deviceCode;

    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空")
    @Size(max = 50, message = "设备名称长度不能超过50个字符")
    @Column(name = "device_name", nullable = false, length = 50)
    private String deviceName;

    /**
     * 设备类型
     */
    @NotBlank(message = "设备类型不能为空")
    @Column(name = "device_type", nullable = false, length = 20)
    private String deviceType;

    /**
     * PLC地址映射
     */
    @Column(name = "plc_address", length = 50)
    private String plcAddress;

    /**
     * 设备状态
     */
    @Column(name = "status", length = 20)
    private String status = "idle";

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
     * 设备类型枚举
     */
    public enum DeviceType {
        AOI("AOI", "AOI检测设备", 2),
        CNC("CNC", "CNC加工设备", 16),
        CCM08("CCM08", "CCM08生产设备", 24),
        CCM23("CCM23", "CCM23生产设备", 6);

        private final String code;
        private final String displayName;
        private final int maxCount;

        DeviceType(String code, String displayName, int maxCount) {
            this.code = code;
            this.displayName = displayName;
            this.maxCount = maxCount;
        }

        public String getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getMaxCount() {
            return maxCount;
        }

        public static DeviceType fromCode(String code) {
            for (DeviceType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("未知的设备类型: " + code);
        }

        /**
         * 验证设备编号是否符合类型规范
         */
        public boolean isValidDeviceCode(String deviceCode) {
            if (deviceCode == null || !deviceCode.startsWith(this.code + "-")) {
                return false;
            }
            
            try {
                String numberPart = deviceCode.substring(this.code.length() + 1);
                int number = Integer.parseInt(numberPart);
                return number >= 1 && number <= this.maxCount;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    /**
     * 设备状态枚举
     */
    public enum DeviceStatus {
        IDLE("idle", "闲置", "#28a745"),
        WORKING("working", "生产中", "#007bff"),
        MAINTENANCE("maintenance", "维修中", "#dc3545");

        private final String code;
        private final String displayName;
        private final String color;

        DeviceStatus(String code, String displayName, String color) {
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

        public static DeviceStatus fromCode(String code) {
            for (DeviceStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return IDLE; // 默认返回闲置状态
        }
    }

    /**
     * 检查设备是否可用于派工
     */
    public boolean isAvailableForWork() {
        return DeviceStatus.IDLE.getCode().equals(this.status);
    }

    /**
     * 检查设备是否正在工作
     */
    public boolean isWorking() {
        return DeviceStatus.WORKING.getCode().equals(this.status);
    }

    /**
     * 检查设备是否在维修中
     */
    public boolean isUnderMaintenance() {
        return DeviceStatus.MAINTENANCE.getCode().equals(this.status);
    }

    /**
     * 获取设备类型显示名称
     */
    public String getDeviceTypeDisplayName() {
        try {
            return DeviceType.fromCode(this.deviceType).getDisplayName();
        } catch (IllegalArgumentException e) {
            return this.deviceType;
        }
    }

    /**
     * 获取设备状态显示名称
     */
    public String getStatusDisplayName() {
        return DeviceStatus.fromCode(this.status).getDisplayName();
    }

    /**
     * 获取设备状态颜色
     */
    public String getStatusColor() {
        return DeviceStatus.fromCode(this.status).getColor();
    }

    /**
     * 验证设备编号格式
     */
    public boolean isValidDeviceCode() {
        try {
            DeviceType type = DeviceType.fromCode(this.deviceType);
            return type.isValidDeviceCode(this.deviceCode);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 获取设备编号中的数字部分
     */
    public Integer getDeviceNumber() {
        if (this.deviceCode == null) {
            return null;
        }
        
        try {
            String[] parts = this.deviceCode.split("-");
            if (parts.length == 2) {
                return Integer.parseInt(parts[1]);
            }
        } catch (NumberFormatException e) {
            // 忽略解析错误
        }
        
        return null;
    }
}