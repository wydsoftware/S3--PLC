package com.pda.enums;

/**
 * 设备状态枚举
 */
public enum DeviceStatus {
    /**
     * 闲置状态 - 设备空闲，可以安排生产
     */
    IDLE("闲置", "设备空闲，可以安排生产任务", "#28a745"),
    
    /**
     * 工作状态 - 设备正在生产
     */
    WORKING("生产中", "设备正在执行生产任务", "#007bff"),
    
    /**
     * 维修状态 - 设备正在维修，不可用
     */
    MAINTENANCE("维修中", "设备正在维修，暂时不可用", "#ffc107"),
    
    /**
     * 故障状态 - 设备出现故障
     */
    FAULT("故障", "设备出现故障，需要维修", "#dc3545"),
    
    /**
     * 离线状态 - 设备离线或通信中断
     */
    OFFLINE("离线", "设备离线或通信中断", "#6c757d"),
    
    /**
     * 停用状态 - 设备被管理员停用
     */
    DISABLED("停用", "设备被管理员停用，不参与生产", "#343a40");

    private final String displayName;
    private final String description;
    private final String color; // 用于前端显示的颜色

    DeviceStatus(String displayName, String description, String color) {
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
     * 根据显示名称获取状态
     */
    public static DeviceStatus fromDisplayName(String displayName) {
        for (DeviceStatus status : values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的设备状态: " + displayName);
    }

    /**
     * 检查设备是否可用于生产
     */
    public boolean isAvailableForProduction() {
        return this == IDLE;
    }

    /**
     * 检查设备是否正在工作
     */
    public boolean isWorking() {
        return this == WORKING;
    }

    /**
     * 检查设备是否需要维修
     */
    public boolean needsMaintenance() {
        return this == MAINTENANCE || this == FAULT;
    }

    /**
     * 检查设备是否在线
     */
    public boolean isOnline() {
        return this != OFFLINE;
    }

    /**
     * 检查设备是否启用
     */
    public boolean isEnabled() {
        return this != DISABLED;
    }

    /**
     * 检查设备是否处于正常状态（非故障、非离线）
     */
    public boolean isNormal() {
        return this != FAULT && this != OFFLINE && this != DISABLED;
    }

    /**
     * 获取可以转换到的状态列表
     */
    public DeviceStatus[] getValidTransitions() {
        switch (this) {
            case IDLE:
                return new DeviceStatus[]{WORKING, MAINTENANCE, FAULT, OFFLINE, DISABLED};
            case WORKING:
                return new DeviceStatus[]{IDLE, FAULT, OFFLINE};
            case MAINTENANCE:
                return new DeviceStatus[]{IDLE, FAULT, OFFLINE};
            case FAULT:
                return new DeviceStatus[]{MAINTENANCE, OFFLINE, DISABLED};
            case OFFLINE:
                return new DeviceStatus[]{IDLE, WORKING, MAINTENANCE, FAULT, DISABLED};
            case DISABLED:
                return new DeviceStatus[]{IDLE, OFFLINE};
            default:
                return new DeviceStatus[]{};
        }
    }

    /**
     * 检查是否可以转换到指定状态
     */
    public boolean canTransitionTo(DeviceStatus targetStatus) {
        DeviceStatus[] validTransitions = getValidTransitions();
        for (DeviceStatus status : validTransitions) {
            if (status == targetStatus) {
                return true;
            }
        }
        return false;
    }
}