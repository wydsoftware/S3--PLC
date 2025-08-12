package com.pda.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 系统配置实体类
 */
@Entity
@Table(name = "system_configs", indexes = {
    @Index(name = "idx_config_key", columnList = "config_key", unique = true),
    @Index(name = "idx_category", columnList = "category")
})
public class SystemConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 配置键
     */
    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键长度不能超过100个字符")
    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    /**
     * 配置值
     */
    @Column(name = "config_value", length = 2000)
    private String configValue;

    /**
     * 配置名称
     */
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 200, message = "配置名称长度不能超过200个字符")
    @Column(name = "config_name", nullable = false, length = 200)
    private String configName;

    /**
     * 配置描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 配置分类
     */
    @NotBlank(message = "配置分类不能为空")
    @Size(max = 50, message = "配置分类长度不能超过50个字符")
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    /**
     * 数据类型（string, number, boolean, json）
     */
    @Column(name = "data_type", length = 20)
    private String dataType = "string";

    /**
     * 默认值
     */
    @Column(name = "default_value", length = 2000)
    private String defaultValue;

    /**
     * 是否必需
     */
    @Column(name = "required", nullable = false)
    private Boolean required = false;

    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 是否只读
     */
    @Column(name = "readonly", nullable = false)
    private Boolean readonly = false;

    /**
     * 排序序号
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * 验证规则（正则表达式）
     */
    @Column(name = "validation_rule", length = 500)
    private String validationRule;

    /**
     * 可选值列表（JSON格式）
     */
    @Column(name = "options", length = 1000)
    private String options;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    // 构造函数
    public SystemConfig() {}

    public SystemConfig(String configKey, String configValue, String configName, String category) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.configName = configName;
        this.category = category;
    }

    public SystemConfig(String configKey, String configValue, String configName, String category, String dataType) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.configName = configName;
        this.category = category;
        this.dataType = dataType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getValidationRule() {
        return validationRule;
    }

    public void setValidationRule(String validationRule) {
        this.validationRule = validationRule;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取配置值作为字符串
     */
    public String getValueAsString() {
        return configValue != null ? configValue : defaultValue;
    }

    /**
     * 获取配置值作为整数
     */
    public Integer getValueAsInteger() {
        String value = getValueAsString();
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取配置值作为长整数
     */
    public Long getValueAsLong() {
        String value = getValueAsString();
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取配置值作为布尔值
     */
    public Boolean getValueAsBoolean() {
        String value = getValueAsString();
        if (value != null && !value.trim().isEmpty()) {
            return Boolean.parseBoolean(value.trim());
        }
        return false;
    }

    /**
     * 获取配置值作为双精度浮点数
     */
    public Double getValueAsDouble() {
        String value = getValueAsString();
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 检查配置是否有效
     */
    public boolean isValid() {
        if (!enabled) {
            return false;
        }
        if (required && (configValue == null || configValue.trim().isEmpty())) {
            return false;
        }
        // 可以添加更多验证逻辑
        return true;
    }

    @Override
    public String toString() {
        return "SystemConfig{" +
                "id=" + id +
                ", configKey='" + configKey + '\'' +
                ", configName='" + configName + '\'' +
                ", category='" + category + '\'' +
                ", dataType='" + dataType + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}