package com.pda.entity;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Entity
@Table(name = "operation_logs", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_operation_time", columnList = "operation_time"),
    @Index(name = "idx_operation_type", columnList = "operation_type"),
    @Index(name = "idx_module", columnList = "module"),
    @Index(name = "idx_status", columnList = "status")
})
public class OperationLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 操作用户ID
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 操作用户名
     */
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    @Column(name = "username", length = 50)
    private String username;

    /**
     * 操作模块
     */
    @NotBlank(message = "操作模块不能为空")
    @Size(max = 50, message = "操作模块长度不能超过50个字符")
    @Column(name = "module", nullable = false, length = 50)
    private String module;

    /**
     * 操作类型
     */
    @NotNull(message = "操作类型不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 20)
    private OperationType operationType;

    /**
     * 操作描述
     */
    @NotBlank(message = "操作描述不能为空")
    @Size(max = 500, message = "操作描述长度不能超过500个字符")
    @Column(name = "operation_desc", nullable = false, length = 500)
    private String operationDesc;

    /**
     * 操作时间
     */
    @NotNull(message = "操作时间不能为空")
    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;

    /**
     * 操作状态（SUCCESS-成功，FAILED-失败）
     */
    @NotNull(message = "操作状态不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OperationStatus status;

    /**
     * 请求方法
     */
    @Size(max = 10, message = "请求方法长度不能超过10个字符")
    @Column(name = "request_method", length = 10)
    private String requestMethod;

    /**
     * 请求URL
     */
    @Size(max = 500, message = "请求URL长度不能超过500个字符")
    @Column(name = "request_url", length = 500)
    private String requestUrl;

    /**
     * 请求参数
     */
    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    /**
     * 响应结果
     */
    @Column(name = "response_result", columnDefinition = "TEXT")
    private String responseResult;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 执行时间（毫秒）
     */
    @Column(name = "execution_time")
    private Long executionTime;

    /**
     * 客户端IP
     */
    @Size(max = 50, message = "客户端IP长度不能超过50个字符")
    @Column(name = "client_ip", length = 50)
    private String clientIp;

    /**
     * 用户代理
     */
    @Size(max = 500, message = "用户代理长度不能超过500个字符")
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 设备信息
     */
    @Size(max = 100, message = "设备信息长度不能超过100个字符")
    @Column(name = "device_info", length = 100)
    private String deviceInfo;

    /**
     * 业务数据ID（如派工单ID、设备ID等）
     */
    @Column(name = "business_id")
    private Long businessId;

    /**
     * 业务数据类型
     */
    @Size(max = 50, message = "业务数据类型长度不能超过50个字符")
    @Column(name = "business_type", length = 50)
    private String businessType;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        LOGIN("登录", "用户登录系统"),
        LOGOUT("登出", "用户登出系统"),
        CREATE("创建", "创建数据"),
        UPDATE("更新", "更新数据"),
        DELETE("删除", "删除数据"),
        QUERY("查询", "查询数据"),
        EXPORT("导出", "导出数据"),
        IMPORT("导入", "导入数据"),
        START_PRODUCTION("开始生产", "开始生产派工单"),
        STOP_PRODUCTION("停止生产", "停止生产派工单"),
        DEVICE_REPAIR("设备报修", "设备报修操作"),
        DEVICE_FIXED("设备修复", "设备修复操作"),
        PLC_CONFIG("PLC配置", "PLC配置操作"),
        SYSTEM_CONFIG("系统配置", "系统配置操作");

        private final String displayName;
        private final String description;

        OperationType(String displayName, String description) {
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
         * 根据显示名称获取操作类型
         */
        public static OperationType getByDisplayName(String displayName) {
            for (OperationType type : values()) {
                if (type.getDisplayName().equals(displayName)) {
                    return type;
                }
            }
            return null;
        }

        /**
         * 检查是否为系统操作
         */
        public boolean isSystemOperation() {
            return this == LOGIN || this == LOGOUT || this == SYSTEM_CONFIG;
        }

        /**
         * 检查是否为业务操作
         */
        public boolean isBusinessOperation() {
            return this == START_PRODUCTION || this == STOP_PRODUCTION || 
                   this == DEVICE_REPAIR || this == DEVICE_FIXED;
        }

        /**
         * 检查是否为数据操作
         */
        public boolean isDataOperation() {
            return this == CREATE || this == UPDATE || this == DELETE || 
                   this == QUERY || this == EXPORT || this == IMPORT;
        }
    }

    /**
     * 操作状态枚举
     */
    public enum OperationStatus {
        SUCCESS("成功", "操作成功", "#28a745"),
        FAILED("失败", "操作失败", "#dc3545"),
        PARTIAL("部分成功", "操作部分成功", "#ffc107"),
        TIMEOUT("超时", "操作超时", "#6c757d");

        private final String displayName;
        private final String description;
        private final String color;

        OperationStatus(String displayName, String description, String color) {
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
         * 根据显示名称获取操作状态
         */
        public static OperationStatus getByDisplayName(String displayName) {
            for (OperationStatus status : values()) {
                if (status.getDisplayName().equals(displayName)) {
                    return status;
                }
            }
            return null;
        }

        /**
         * 检查是否成功
         */
        public boolean isSuccess() {
            return this == SUCCESS;
        }

        /**
         * 检查是否失败
         */
        public boolean isFailed() {
            return this == FAILED || this == TIMEOUT;
        }
    }

    // 构造函数
    public OperationLog() {
        this.operationTime = LocalDateTime.now();
    }

    public OperationLog(String module, OperationType operationType, String operationDesc, OperationStatus status) {
        this();
        this.module = module;
        this.operationType = operationType;
        this.operationDesc = operationDesc;
        this.status = status;
    }

    public OperationLog(Long userId, String username, String module, OperationType operationType, 
                       String operationDesc, OperationStatus status) {
        this(module, operationType, operationDesc, status);
        this.userId = userId;
        this.username = username;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public String getOperationDesc() {
        return operationDesc;
    }

    public void setOperationDesc(String operationDesc) {
        this.operationDesc = operationDesc;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public String getResponseResult() {
        return responseResult;
    }

    public void setResponseResult(String responseResult) {
        this.responseResult = responseResult;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取操作类型显示名称
     */
    public String getOperationTypeDisplayName() {
        return operationType != null ? operationType.getDisplayName() : "";
    }

    /**
     * 获取操作状态显示名称
     */
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }

    /**
     * 获取操作状态颜色
     */
    public String getStatusColor() {
        return status != null ? status.getColor() : "#6c757d";
    }

    /**
     * 检查操作是否成功
     */
    public boolean isSuccess() {
        return status != null && status.isSuccess();
    }

    /**
     * 检查操作是否失败
     */
    public boolean isFailed() {
        return status != null && status.isFailed();
    }

    /**
     * 获取执行时间描述
     */
    public String getExecutionTimeDescription() {
        if (executionTime == null) {
            return "未知";
        }
        if (executionTime < 1000) {
            return executionTime + "ms";
        } else {
            return String.format("%.2fs", executionTime / 1000.0);
        }
    }

    /**
     * 创建成功日志
     */
    public static OperationLog createSuccessLog(Long userId, String username, String module, 
                                               OperationType operationType, String operationDesc) {
        return new OperationLog(userId, username, module, operationType, operationDesc, OperationStatus.SUCCESS);
    }

    /**
     * 创建失败日志
     */
    public static OperationLog createFailedLog(Long userId, String username, String module, 
                                              OperationType operationType, String operationDesc, String errorMessage) {
        OperationLog log = new OperationLog(userId, username, module, operationType, operationDesc, OperationStatus.FAILED);
        log.setErrorMessage(errorMessage);
        return log;
    }

    @Override
    public String toString() {
        return "OperationLog{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", module='" + module + '\'' +
                ", operationType=" + operationType +
                ", operationDesc='" + operationDesc + '\'' +
                ", status=" + status +
                ", operationTime=" + operationTime +
                '}';
    }
}