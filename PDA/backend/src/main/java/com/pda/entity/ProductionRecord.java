package com.pda.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产记录实体类
 */
@Entity
@Table(name = "production_records", indexes = {
    @Index(name = "idx_work_order_id", columnList = "work_order_id"),
    @Index(name = "idx_device_id", columnList = "device_id"),
    @Index(name = "idx_production_date", columnList = "production_date"),
    @Index(name = "idx_operator_id", columnList = "operator_id")
})
public class ProductionRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联派工单ID
     */
    @NotNull(message = "派工单ID不能为空")
    @Column(name = "work_order_id", nullable = false)
    private Long workOrderId;

    /**
     * 派工单号（冗余字段）
     */
    @Column(name = "work_order_no", length = 50)
    private String workOrderNo;

    /**
     * 关联设备ID
     */
    @NotNull(message = "设备ID不能为空")
    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    /**
     * 设备编号（冗余字段）
     */
    @Column(name = "device_code", length = 50)
    private String deviceCode;

    /**
     * 生产日期
     */
    @NotNull(message = "生产日期不能为空")
    @Column(name = "production_date", nullable = false)
    private java.time.LocalDate productionDate;

    /**
     * 生产开始时间
     */
    @NotNull(message = "生产开始时间不能为空")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * 生产结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 计划生产数量
     */
    @NotNull(message = "计划生产数量不能为空")
    @PositiveOrZero(message = "计划生产数量不能为负数")
    @Column(name = "planned_quantity", nullable = false)
    private Integer plannedQuantity;

    /**
     * 实际生产数量
     */
    @PositiveOrZero(message = "实际生产数量不能为负数")
    @Column(name = "actual_quantity", nullable = false)
    private Integer actualQuantity = 0;

    /**
     * 合格数量
     */
    @PositiveOrZero(message = "合格数量不能为负数")
    @Column(name = "qualified_quantity", nullable = false)
    private Integer qualifiedQuantity = 0;

    /**
     * 不合格数量
     */
    @PositiveOrZero(message = "不合格数量不能为负数")
    @Column(name = "defective_quantity", nullable = false)
    private Integer defectiveQuantity = 0;

    /**
     * 生产效率（实际数量/计划数量）
     */
    @Column(name = "efficiency_rate", precision = 5, scale = 2)
    private BigDecimal efficiencyRate;

    /**
     * 合格率（合格数量/实际数量）
     */
    @Column(name = "qualified_rate", precision = 5, scale = 2)
    private BigDecimal qualifiedRate;

    /**
     * 生产耗时（分钟）
     */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    /**
     * 操作员ID
     */
    @NotNull(message = "操作员ID不能为空")
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    /**
     * 操作员姓名（冗余字段）
     */
    @Column(name = "operator_name", length = 100)
    private String operatorName;

    /**
     * 班次
     */
    @Column(name = "shift", length = 20)
    private String shift;

    /**
     * 产品名称
     */
    @Column(name = "product_name", length = 200)
    private String productName;

    /**
     * 产品规格
     */
    @Column(name = "product_spec", length = 200)
    private String productSpec;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    // 关联实体
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", insertable = false, updatable = false)
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", insertable = false, updatable = false)
    private Device device;

    // 构造函数
    public ProductionRecord() {}

    public ProductionRecord(Long workOrderId, Long deviceId, LocalDateTime startTime, Integer plannedQuantity, Long operatorId) {
        this.workOrderId = workOrderId;
        this.deviceId = deviceId;
        this.startTime = startTime;
        this.plannedQuantity = plannedQuantity;
        this.operatorId = operatorId;
        this.productionDate = startTime.toLocalDate();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getWorkOrderNo() {
        return workOrderNo;
    }

    public void setWorkOrderNo(String workOrderNo) {
        this.workOrderNo = workOrderNo;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public java.time.LocalDate getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(java.time.LocalDate productionDate) {
        this.productionDate = productionDate;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        if (startTime != null) {
            this.productionDate = startTime.toLocalDate();
        }
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        // 自动计算生产耗时
        if (startTime != null && endTime != null) {
            this.durationMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
        }
    }

    public Integer getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(Integer plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
        calculateEfficiencyRate();
    }

    public Integer getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(Integer actualQuantity) {
        this.actualQuantity = actualQuantity;
        calculateEfficiencyRate();
        calculateQualifiedRate();
    }

    public Integer getQualifiedQuantity() {
        return qualifiedQuantity;
    }

    public void setQualifiedQuantity(Integer qualifiedQuantity) {
        this.qualifiedQuantity = qualifiedQuantity;
        calculateQualifiedRate();
        // 自动计算不合格数量
        if (actualQuantity != null) {
            this.defectiveQuantity = actualQuantity - qualifiedQuantity;
        }
    }

    public Integer getDefectiveQuantity() {
        return defectiveQuantity;
    }

    public void setDefectiveQuantity(Integer defectiveQuantity) {
        this.defectiveQuantity = defectiveQuantity;
    }

    public BigDecimal getEfficiencyRate() {
        return efficiencyRate;
    }

    public void setEfficiencyRate(BigDecimal efficiencyRate) {
        this.efficiencyRate = efficiencyRate;
    }

    public BigDecimal getQualifiedRate() {
        return qualifiedRate;
    }

    public void setQualifiedRate(BigDecimal qualifiedRate) {
        this.qualifiedRate = qualifiedRate;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSpec() {
        return productSpec;
    }

    public void setProductSpec(String productSpec) {
        this.productSpec = productSpec;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    /**
     * 计算生产效率
     */
    private void calculateEfficiencyRate() {
        if (plannedQuantity != null && plannedQuantity > 0 && actualQuantity != null) {
            this.efficiencyRate = BigDecimal.valueOf(actualQuantity)
                    .divide(BigDecimal.valueOf(plannedQuantity), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
    }

    /**
     * 计算合格率
     */
    private void calculateQualifiedRate() {
        if (actualQuantity != null && actualQuantity > 0 && qualifiedQuantity != null) {
            this.qualifiedRate = BigDecimal.valueOf(qualifiedQuantity)
                    .divide(BigDecimal.valueOf(actualQuantity), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
    }

    /**
     * 检查生产是否已完成
     */
    public boolean isCompleted() {
        return endTime != null;
    }

    /**
     * 获取生产耗时（小时）
     */
    public BigDecimal getDurationHours() {
        if (durationMinutes != null) {
            return BigDecimal.valueOf(durationMinutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
        }
        return null;
    }

    @Override
    public String toString() {
        return "ProductionRecord{" +
                "id=" + id +
                ", workOrderNo='" + workOrderNo + '\'' +
                ", deviceCode='" + deviceCode + '\'' +
                ", productionDate=" + productionDate +
                ", plannedQuantity=" + plannedQuantity +
                ", actualQuantity=" + actualQuantity +
                ", qualifiedQuantity=" + qualifiedQuantity +
                ", efficiencyRate=" + efficiencyRate +
                ", qualifiedRate=" + qualifiedRate +
                '}';
    }
}