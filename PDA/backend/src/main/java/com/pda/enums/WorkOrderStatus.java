package com.pda.enums;

/**
 * 派工单状态枚举
 */
public enum WorkOrderStatus {
    /**
     * 已创建 - 派工单已创建，等待开始生产
     */
    CREATED("已创建", "派工单已创建，等待开始生产", "#6c757d"),
    
    /**
     * 进行中 - 派工单正在执行生产
     */
    IN_PROGRESS("进行中", "派工单正在执行生产任务", "#007bff"),
    
    /**
     * 已暂停 - 派工单生产被暂停
     */
    PAUSED("已暂停", "派工单生产被暂停", "#ffc107"),
    
    /**
     * 已完成 - 派工单生产已完成
     */
    COMPLETED("已完成", "派工单生产已完成", "#28a745"),
    
    /**
     * 已取消 - 派工单被取消
     */
    CANCELLED("已取消", "派工单被取消，不再执行", "#dc3545"),
    
    /**
     * 异常终止 - 派工单因异常而终止
     */
    TERMINATED("异常终止", "派工单因设备故障或其他异常而终止", "#dc3545");

    private final String displayName;
    private final String description;
    private final String color; // 用于前端显示的颜色

    WorkOrderStatus(String displayName, String description, String color) {
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
    public static WorkOrderStatus fromDisplayName(String displayName) {
        for (WorkOrderStatus status : values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的派工单状态: " + displayName);
    }

    /**
     * 检查是否为活跃状态（正在进行或暂停）
     */
    public boolean isActive() {
        return this == IN_PROGRESS || this == PAUSED;
    }

    /**
     * 检查是否为完成状态
     */
    public boolean isFinished() {
        return this == COMPLETED || this == CANCELLED || this == TERMINATED;
    }

    /**
     * 检查是否可以开始生产
     */
    public boolean canStart() {
        return this == CREATED || this == PAUSED;
    }

    /**
     * 检查是否可以暂停
     */
    public boolean canPause() {
        return this == IN_PROGRESS;
    }

    /**
     * 检查是否可以停止
     */
    public boolean canStop() {
        return this == IN_PROGRESS || this == PAUSED;
    }

    /**
     * 检查是否可以取消
     */
    public boolean canCancel() {
        return this == CREATED || this == PAUSED;
    }

    /**
     * 检查是否可以恢复
     */
    public boolean canResume() {
        return this == PAUSED;
    }

    /**
     * 获取可以转换到的状态列表
     */
    public WorkOrderStatus[] getValidTransitions() {
        switch (this) {
            case CREATED:
                return new WorkOrderStatus[]{IN_PROGRESS, CANCELLED};
            case IN_PROGRESS:
                return new WorkOrderStatus[]{PAUSED, COMPLETED, TERMINATED};
            case PAUSED:
                return new WorkOrderStatus[]{IN_PROGRESS, COMPLETED, CANCELLED, TERMINATED};
            case COMPLETED:
            case CANCELLED:
            case TERMINATED:
                return new WorkOrderStatus[]{}; // 终态，不能再转换
            default:
                return new WorkOrderStatus[]{};
        }
    }

    /**
     * 检查是否可以转换到指定状态
     */
    public boolean canTransitionTo(WorkOrderStatus targetStatus) {
        WorkOrderStatus[] validTransitions = getValidTransitions();
        for (WorkOrderStatus status : validTransitions) {
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
            case PAUSED:
                return 2;
            case CREATED:
                return 3;
            case COMPLETED:
                return 4;
            case CANCELLED:
                return 5;
            case TERMINATED:
                return 6;
            default:
                return 99;
        }
    }
}