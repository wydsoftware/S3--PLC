package com.pda.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * PLC配置实体类
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "plc_configs", indexes = {
    @Index(name = "idx_config_name", columnList = "config_name"),
    @Index(name = "idx_is_active", columnList = "is_active")
})
public class PlcConfig {

    /**
     * PLC配置ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 配置名称
     */
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 50, message = "配置名称长度不能超过50个字符")
    @Column(name = "config_name", nullable = false, length = 50)
    private String configName;

    /**
     * PLC IP地址
     */
    @NotBlank(message = "PLC IP地址不能为空")
    @Pattern(regexp = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$", 
             message = "PLC IP地址格式不正确")
    @Column(name = "plc_ip", nullable = false, length = 15)
    private String plcIp;

    /**
     * PLC端口
     */
    @Min(value = 1, message = "PLC端口必须大于0")
    @Max(value = 65535, message = "PLC端口不能超过65535")
    @Column(name = "plc_port", nullable = false)
    private Integer plcPort = 502;

    /**
     * 连接超时时间（毫秒）
     */
    @Min(value = 1000, message = "连接超时时间不能少于1000毫秒")
    @Max(value = 30000, message = "连接超时时间不能超过30000毫秒")
    @Column(name = "connect_timeout", nullable = false)
    private Integer connectTimeout = 5000;

    /**
     * 读取超时时间（毫秒）
     */
    @Min(value = 1000, message = "读取超时时间不能少于1000毫秒")
    @Max(value = 30000, message = "读取超时时间不能超过30000毫秒")
    @Column(name = "read_timeout", nullable = false)
    private Integer readTimeout = 3000;

    /**
     * 重试次数
     */
    @Min(value = 1, message = "重试次数不能少于1次")
    @Max(value = 10, message = "重试次数不能超过10次")
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 3;

    /**
     * 重试间隔（毫秒）
     */
    @Min(value = 100, message = "重试间隔不能少于100毫秒")
    @Max(value = 10000, message = "重试间隔不能超过10000毫秒")
    @Column(name = "retry_interval", nullable = false)
    private Integer retryInterval = 1000;

    /**
     * 是否启用
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * 是否为默认配置
     */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    /**
     * 描述
     */
    @Size(max = 200, message = "描述长度不能超过200个字符")
    @Column(name = "description", length = 200)
    private String description;

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
     * 检查配置是否可用
     */
    public boolean isAvailable() {
        return this.isActive != null && this.isActive;
    }

    /**
     * 检查是否为默认配置
     */
    public boolean isDefaultConfig() {
        return this.isDefault != null && this.isDefault;
    }

    /**
     * 获取连接地址
     */
    public String getConnectionAddress() {
        return String.format("%s:%d", this.plcIp, this.plcPort);
    }

    /**
     * 获取状态显示名称
     */
    public String getStatusDisplayName() {
        return isAvailable() ? "启用" : "禁用";
    }

    /**
     * 获取状态颜色
     */
    public String getStatusColor() {
        return isAvailable() ? "#28a745" : "#6c757d";
    }

    /**
     * 获取配置类型显示名称
     */
    public String getConfigTypeDisplayName() {
        return isDefaultConfig() ? "默认配置" : "自定义配置";
    }

    /**
     * 启用配置
     */
    public void enable() {
        this.isActive = true;
    }

    /**
     * 禁用配置
     */
    public void disable() {
        this.isActive = false;
    }

    /**
     * 设置为默认配置
     */
    public void setAsDefault() {
        this.isDefault = true;
        this.isActive = true; // 默认配置必须启用
    }

    /**
     * 取消默认配置
     */
    public void unsetDefault() {
        this.isDefault = false;
    }

    /**
     * 验证IP地址格式
     */
    public static boolean isValidIpAddress(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }
        
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        
        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 验证端口号
     */
    public static boolean isValidPort(Integer port) {
        return port != null && port > 0 && port <= 65535;
    }

    /**
     * 创建默认PLC配置
     */
    public static PlcConfig createDefaultConfig() {
        PlcConfig config = new PlcConfig();
        config.setConfigName("默认PLC配置");
        config.setPlcIp("192.168.1.100");
        config.setPlcPort(502);
        config.setConnectTimeout(5000);
        config.setReadTimeout(3000);
        config.setRetryCount(3);
        config.setRetryInterval(1000);
        config.setIsActive(true);
        config.setIsDefault(true);
        config.setDescription("系统默认PLC配置");
        return config;
    }

    /**
     * 创建自定义PLC配置
     */
    public static PlcConfig createCustomConfig(String configName, String plcIp, Integer plcPort, 
                                              String description, String createdBy) {
        PlcConfig config = new PlcConfig();
        config.setConfigName(configName);
        config.setPlcIp(plcIp);
        config.setPlcPort(plcPort != null ? plcPort : 502);
        config.setConnectTimeout(5000);
        config.setReadTimeout(3000);
        config.setRetryCount(3);
        config.setRetryInterval(1000);
        config.setIsActive(true);
        config.setIsDefault(false);
        config.setDescription(description);
        config.setCreatedBy(createdBy);
        return config;
    }

    /**
     * 测试连接配置
     */
    public String getTestConnectionInfo() {
        return String.format("PLC连接测试 - IP: %s, 端口: %d, 超时: %dms, 重试: %d次", 
            this.plcIp, this.plcPort, this.connectTimeout, this.retryCount);
    }
}