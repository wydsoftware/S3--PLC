package com.pda.enums;

/**
 * 维修类型枚举
 */
public enum MaintenanceType {
    /**
     * 故障维修 - 设备出现故障后的维修
     */
    BREAKDOWN("故障维修", "设备出现故障后的紧急维修", 1),
    
    /**
     * 预防性维修 - 定期预防性维护
     */
    PREVENTIVE("预防性维修", "定期进行的预防性维护保养", 3),
    
    /**
     * 计划维修 - 计划内的维修保养
     */
    PLANNED("计划维修", "按计划进行的维修保养工作", 2),
    
    /**
     * 改进维修 - 设备改进升级
     */
    IMPROVEMENT("改进维修", "对设备进行改进升级的维修", 4),
    
    /**
     * 应急维修 - 紧急情况下的维修
     */
    EMERGENCY("应急维修", "紧急情况下的快速维修", 1),
    
    /**
     * 日常保养 - 日常维护保养
     */
    ROUTINE("日常保养", "日常的维护保养工作", 5);

    private final String displayName;
    private final String description;
    private final int priority; // 优先级，数字越小优先级越高

    MaintenanceType(String displayName, String description, int priority) {
        this.displayName = displayName;
        this.description = description;
        this.priority = priority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    /**
     * 根据显示名称获取维修类型
     */
    public static MaintenanceType fromDisplayName(String displayName) {
        for (MaintenanceType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的维修类型: " + displayName);
    }

    /**
     * 检查是否为紧急维修
     */
    public boolean isUrgent() {
        return this == BREAKDOWN || this == EMERGENCY;
    }

    /**
     * 检查是否为计划维修
     */
    public boolean isPlanned() {
        return this == PLANNED || this == PREVENTIVE || this == ROUTINE;
    }

    /**
     * 检查是否为故障维修
     */
    public boolean isBreakdown() {
        return this == BREAKDOWN;
    }

    /**
     * 检查是否为预防性维修
     */
    public boolean isPreventive() {
        return this == PREVENTIVE || this == ROUTINE;
    }

    /**
     * 获取建议的响应时间（小时）
     */
    public int getSuggestedResponseHours() {
        switch (this) {
            case EMERGENCY:
                return 1; // 1小时内响应
            case BREAKDOWN:
                return 4; // 4小时内响应
            case PLANNED:
                return 24; // 24小时内响应
            case PREVENTIVE:
                return 48; // 48小时内响应
            case IMPROVEMENT:
                return 72; // 72小时内响应
            case ROUTINE:
                return 168; // 1周内响应
            default:
                return 24;
        }
    }

    /**
     * 获取建议的完成时间（小时）
     */
    public int getSuggestedCompletionHours() {
        switch (this) {
            case EMERGENCY:
                return 4; // 4小时内完成
            case BREAKDOWN:
                return 8; // 8小时内完成
            case PLANNED:
                return 24; // 24小时内完成
            case PREVENTIVE:
                return 48; // 48小时内完成
            case IMPROVEMENT:
                return 168; // 1周内完成
            case ROUTINE:
                return 4; // 4小时内完成
            default:
                return 24;
        }
    }

    /**
     * 获取颜色代码（用于前端显示）
     */
    public String getColor() {
        switch (this) {
            case EMERGENCY:
                return "#dc3545"; // 红色
            case BREAKDOWN:
                return "#fd7e14"; // 橙色
            case PLANNED:
                return "#007bff"; // 蓝色
            case PREVENTIVE:
                return "#28a745"; // 绿色
            case IMPROVEMENT:
                return "#6f42c1"; // 紫色
            case ROUTINE:
                return "#20c997"; // 青色
            default:
                return "#6c757d"; // 灰色
        }
    }
}