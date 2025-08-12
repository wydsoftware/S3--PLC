package com.pda.enums;

/**
 * 用户角色枚举
 */
public enum UserRole {
    /**
     * 管理员 - 拥有所有权限
     */
    ADMIN("管理员", "拥有系统所有权限，可以管理用户、设备、派工单等"),
    
    /**
     * 操作员 - 基本操作权限
     */
    OPERATOR("操作员", "可以进行设备派工、停止生产、设备报修等基本操作"),
    
    /**
     * 维修员 - 设备维修权限
     */
    MAINTENANCE("维修员", "可以处理设备报修、更新设备状态等维修相关操作"),
    
    /**
     * 查看员 - 只读权限
     */
    VIEWER("查看员", "只能查看设备状态、生产记录等信息，无法进行操作");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
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
     * 根据显示名称获取角色
     */
    public static UserRole fromDisplayName(String displayName) {
        for (UserRole role : values()) {
            if (role.displayName.equals(displayName)) {
                return role;
            }
        }
        return OPERATOR; // 默认返回操作员
    }

    /**
     * 检查是否为管理员
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * 检查是否有操作权限
     */
    public boolean canOperate() {
        return this == ADMIN || this == OPERATOR || this == MAINTENANCE;
    }

    /**
     * 检查是否有维修权限
     */
    public boolean canMaintenance() {
        return this == ADMIN || this == MAINTENANCE;
    }

    /**
     * 检查是否有管理权限
     */
    public boolean canManage() {
        return this == ADMIN;
    }
}