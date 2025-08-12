package com.pda.enums;

/**
 * 用户状态枚举
 */
public enum UserStatus {
    /**
     * 激活状态 - 用户可以正常使用系统
     */
    ACTIVE("激活", "用户可以正常登录和使用系统"),
    
    /**
     * 禁用状态 - 用户被管理员禁用
     */
    DISABLED("禁用", "用户被管理员禁用，无法登录系统"),
    
    /**
     * 锁定状态 - 用户因多次登录失败被锁定
     */
    LOCKED("锁定", "用户因多次登录失败被系统锁定"),
    
    /**
     * 过期状态 - 用户账户已过期
     */
    EXPIRED("过期", "用户账户已过期，需要管理员重新激活");

    private final String displayName;
    private final String description;

    UserStatus(String displayName, String description) {
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
     * 根据显示名称获取状态
     */
    public static UserStatus fromDisplayName(String displayName) {
        for (UserStatus status : values()) {
            if (status.displayName.equals(displayName)) {
                return status;
            }
        }
        return DISABLED; // 默认返回禁用
    }

    /**
     * 检查用户是否可以登录
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }

    /**
     * 检查用户是否被禁用
     */
    public boolean isDisabled() {
        return this == DISABLED;
    }

    /**
     * 检查用户是否被锁定
     */
    public boolean isLocked() {
        return this == LOCKED;
    }

    /**
     * 检查用户是否已过期
     */
    public boolean isExpired() {
        return this == EXPIRED;
    }

    /**
     * 检查用户是否处于非正常状态
     */
    public boolean isInactive() {
        return this != ACTIVE;
    }
}