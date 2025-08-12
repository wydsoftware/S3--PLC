package com.pda.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_role", columnList = "role"),
    @Index(name = "idx_status", columnList = "status")
})
public class User {

    /**
     * 用户ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码(加密)
     */
    @JsonIgnore
    @NotBlank(message = "密码不能为空")
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * 角色
     */
    @Column(name = "role", length = 20)
    private String role = "user";

    /**
     * 状态(1:启用 0:禁用)
     */
    @Column(name = "status")
    private Integer status = 1;

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
     * 用户角色枚举
     */
    public enum Role {
        ADMIN("admin", "管理员"),
        USER("user", "普通用户");

        private final String code;
        private final String displayName;

        Role(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Role fromCode(String code) {
            for (Role role : values()) {
                if (role.code.equals(code)) {
                    return role;
                }
            }
            return USER; // 默认返回普通用户
        }
    }

    /**
     * 用户状态枚举
     */
    public enum Status {
        DISABLED(0, "禁用"),
        ENABLED(1, "启用");

        private final Integer code;
        private final String displayName;

        Status(Integer code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public Integer getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Status fromCode(Integer code) {
            for (Status status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return DISABLED; // 默认返回禁用
        }
    }

    /**
     * 检查用户是否启用
     */
    public boolean isEnabled() {
        return Status.ENABLED.getCode().equals(this.status);
    }

    /**
     * 检查用户是否为管理员
     */
    public boolean isAdmin() {
        return Role.ADMIN.getCode().equals(this.role);
    }

    /**
     * 获取角色显示名称
     */
    public String getRoleDisplayName() {
        return Role.fromCode(this.role).getDisplayName();
    }

    /**
     * 获取状态显示名称
     */
    public String getStatusDisplayName() {
        return Status.fromCode(this.status).getDisplayName();
    }
}