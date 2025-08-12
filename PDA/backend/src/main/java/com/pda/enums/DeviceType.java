package com.pda.enums;

/**
 * 设备类型枚举
 */
public enum DeviceType {
    /**
     * AOI设备 - 自动光学检测设备
     */
    AOI("AOI", "自动光学检测设备", 8, "AOI"),
    
    /**
     * CNC设备 - 数控机床
     */
    CNC("CNC", "数控机床", 16, "CNC"),
    
    /**
     * CCM08设备 - 连铸机08
     */
    CCM08("CCM08", "连铸机08", 16, "CCM08"),
    
    /**
     * CCM23设备 - 连铸机23
     */
    CCM23("CCM23", "连铸机23", 8, "CCM23");

    private final String code;
    private final String displayName;
    private final int maxCount;
    private final String prefix;

    DeviceType(String code, String displayName, int maxCount, String prefix) {
        this.code = code;
        this.displayName = displayName;
        this.maxCount = maxCount;
        this.prefix = prefix;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * 根据代码获取设备类型
     */
    public static DeviceType fromCode(String code) {
        for (DeviceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的设备类型代码: " + code);
    }

    /**
     * 根据显示名称获取设备类型
     */
    public static DeviceType fromDisplayName(String displayName) {
        for (DeviceType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的设备类型名称: " + displayName);
    }

    /**
     * 生成设备编号
     * @param index 设备序号（从1开始）
     * @return 设备编号（如：CNC01, CCM08-01等）
     */
    public String generateDeviceCode(int index) {
        if (index < 1 || index > maxCount) {
            throw new IllegalArgumentException("设备序号超出范围: " + index + "，有效范围: 1-" + maxCount);
        }
        
        if (this == CCM08 || this == CCM23) {
            return String.format("%s-%02d", prefix, index);
        } else {
            return String.format("%s%02d", prefix, index);
        }
    }

    /**
     * 获取所有可用的设备编号
     */
    public String[] getAllDeviceCodes() {
        String[] codes = new String[maxCount];
        for (int i = 1; i <= maxCount; i++) {
            codes[i - 1] = generateDeviceCode(i);
        }
        return codes;
    }

    /**
     * 检查设备编号是否属于此类型
     */
    public boolean isValidDeviceCode(String deviceCode) {
        if (deviceCode == null || deviceCode.trim().isEmpty()) {
            return false;
        }
        
        for (int i = 1; i <= maxCount; i++) {
            if (generateDeviceCode(i).equals(deviceCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从设备编号中提取序号
     */
    public int extractIndexFromDeviceCode(String deviceCode) {
        if (!isValidDeviceCode(deviceCode)) {
            throw new IllegalArgumentException("无效的设备编号: " + deviceCode);
        }
        
        String numberPart;
        if (this == CCM08 || this == CCM23) {
            // CCM08-01, CCM23-01 格式
            numberPart = deviceCode.substring(deviceCode.lastIndexOf('-') + 1);
        } else {
            // CNC01, AOI01 格式
            numberPart = deviceCode.substring(prefix.length());
        }
        
        return Integer.parseInt(numberPart);
    }
}