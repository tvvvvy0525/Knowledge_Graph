package com.pinewilt.kg.dto;

import lombok.Data;

@Data
public class DiagnosisRequest {
    // I1: 媒介昆虫因素
    private Integer vectorCount;       // 种类数量 (0, 1, 2, 3)
    private Integer lifeHistory;       // 生活史: 1=2-3年1代, 2=1年1代, 3=1年2代以上
    private Integer vectorDistribution;// 分布: 1=小, 2=中, 3=广
    private Integer vectorCapacity;    // 携带能力: 1=弱, 2=较强, 3=强

    // I2: 寄主植物因素
    private Integer hostDistribution;  // 分布: 1=小, 2=中, 3=广
    private Integer hostSusceptibility;// 感病性: 0=不感病, 2=较易, 3=容易
    private Integer hostValue;         // 价值: 1=低, 2=一般, 3=高

    // I3: 降雨量 (实际数值，由后端判断区间)
    private Double annualRainfall;     // 年均降雨量
    private Double summerRainfall;     // 6-8月降雨量

    // I4: 温度 (实际数值)
    private Double annualTemp;         // 年均温
    private Double summerTemp;         // 6-8月均温
}