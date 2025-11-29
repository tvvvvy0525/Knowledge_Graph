package com.pinewilt.kg.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class DiagnosisResult {
    private Double score;      // 最终得分 D0
    private String riskLevel;  // 风险等级 (高/中/低)
    private String advice;     // 简短建议
}