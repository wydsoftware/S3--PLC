package com.pda.enums;

/**
 * 维修状态枚举
 */
public enum MaintenanceStatus {
    /**
     * 已报修 - 维修请求已提交
     */
    REPORTED("已报修", "维修请求已提交，等待处理", "#6c757d"),
    
    /**
     * 已分配 - 维修任务已分配给维修人员
     */
    ASSIGNED("已分配", "维修任务已分配给维修人员", "#17a2b8"),
    
    /**
     * 进行中 - 维修工作正在进行
     */
    IN_PROGRESS("进行中", "维修工作正在进行中", "#007bff"),
    
    /**
     * 等待零件 - 等待零件到货
     */
    WAITING_PARTS("等待零件", "等待维修零件到货", "#ffc107"),
    
    /**
     * 暂停 - 维修工作暂停
     */
    PAUSED("暂停", "维修工作因故暂停", "#fd7e14"),
    
    /**
     * 已完成 - 维修工作已完成
     */
    COMPLETED("已完成", "维修工作已完成，设备可正常使用", "#28a745"),
    
    /**
     * 已取消 - 维修请求被取消
     */
    CANCELLED("已取消", "维修请求被取消", "#dc3545"),
    
    /**
     * 验收中 - 维修完成后等待验收
     */
    UNDER_REVIEW("验收中", "维修完成后等待质量验收", "#6f42c1"),
    
    /**
     * 返工 - 维修质量不合格，需要返工
     */
    REWORK("返工", "维修质量不合格，需要重新维修", "#e83e8c");

    private final String displayName;
    private final String description;
    private final String color; // 用于前端显示的颜色

    MaintenanceStatus(String displayName, String description, String color) {
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
    public static MaintenanceStatus fromDisplayName(String displayName) {
        for (MaintenanceStatus status : values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的维修状态: " + displayName);
    }

    /**
     * 检查是否为活跃状态（正在处理中）
     */
    public boolean isActive() {
        return this == ASSIGNED || this == IN_PROGRESS || this == WAITING_PARTS || 
               this == PAUSED || this == UNDER_REVIEW || this == REWORK;
    }

    /**
     * 检查是否为完成状态
     */
    public boolean isFinished() {
        return this == COMPLETED || this == CANCELLED;
    }

    /**
     * 检查是否可以开始维修
     */
    public boolean canStart() {
        return this == REPORTED || this == ASSIGNED || this == PAUSED || this == REWORK;
    }

    /**
     * 检查是否可以暂停
     */
    public boolean canPause() {
        return this == IN_PROGRESS;
    }

    /**
     * 检查是否可以完成
     */
    public boolean canComplete() {
        return this == IN_PROGRESS || this == UNDER_REVIEW;
    }

    /**
     * 检查是否可以取消
     */
    public boolean canCancel() {
        return this == REPORTED || this == ASSIGNED || this == PAUSED;
    }

    /**
     * 检查是否可以分配
     */
    public boolean canAssign() {
        return this == REPORTED;
    }

    /**
     * 检查是否可以验收
     */
    public boolean canReview() {
        return this == IN_PROGRESS;
    }

    /**
     * 检查是否可以返工
     */
    public boolean canRework() {
        return this == UNDER_REVIEW;
    }

    /**
     * 获取可以转换到的状态列表
     */
    public MaintenanceStatus[] getValidTransitions() {
        switch (this) {
            case REPORTED:
                return new MaintenanceStatus[]{ASSIGNED, CANCELLED};
            case ASSIGNED:
                return new MaintenanceStatus[]{IN_PROGRESS, CANCELLED};
            case IN_PROGRESS:
                return new MaintenanceStatus[]{WAITING_PARTS, PAUSED, COMPLETED, UNDER_REVIEW};
            case WAITING_PARTS:
                return new MaintenanceStatus[]{IN_PROGRESS, PAUSED, CANCELLED};
            case PAUSED:
                return new MaintenanceStatus[]{IN_PROGRESS, CANCELLED};
            case UNDER_REVIEW:
                return new MaintenanceStatus[]{COMPLETED, REWORK};
            case REWORK:
                return new MaintenanceStatus[]{IN_PROGRESS};
            case COMPLETED:
            case CANCELLED:
                return new MaintenanceStatus[]{}; // 终态，不能再转换
            default:
                return new MaintenanceStatus[]{};
        }
    }

    /**
     * 检查是否可以转换到指定状态
     */
    public boolean canTransitionTo(MaintenanceStatus targetStatus) {
        MaintenanceStatus[] validTransitions = getValidTransitions();
        for (MaintenanceStatus status : validTransitions) {
            if (status == targetStatus) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取状态优先级（用于排序，数字越小优先级越高）
     */
    public int getPriority() {
        switch (this) {
            case IN_PROGRESS:
                return 1;
            case REWORK:
                return 2;
            case WAITING_PARTS:
                return 3;
            case ASSIGNED:
                return 4;
            case UNDER_REVIEW:
                return 5;
            case REPORTED:
                return 6;
            case PAUSED:
                return 7;
            case COMPLETED:
                return 8;
            case CANCELLED:
                return 9;
            default:
                return 99;
        }
    }

    /**
     * 检查设备是否因此状态而不可用
     */
    public boolean makesDeviceUnavailable() {
        return this == IN_PROGRESS || this == WAITING_PARTS || this == UNDER_REVIEW || this == REWORK;
    }
}