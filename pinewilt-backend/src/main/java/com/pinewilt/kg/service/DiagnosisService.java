package com.pinewilt.kg.service;

import com.pinewilt.kg.dto.DiagnosisRequest;
import com.pinewilt.kg.dto.DiagnosisResult;
import org.springframework.stereotype.Service;

@Service
public class DiagnosisService {

    public DiagnosisResult assess(DiagnosisRequest req) {
        // 1. 计算 I1 (媒介昆虫指数) - 几何平均数
        // 前端传来的是等级(1,2,3)，对应分值通常也就是 1.0, 2.0, 3.0 (根据表格归一化处理，这里简化处理直接用等级值)
        double i1 = Math.pow(req.getVectorCount() * req.getLifeHistory() * req.getVectorDistribution() * req.getVectorCapacity(), 0.25);

        // 2. 计算 I2 (寄主植物指数) - 几何平均数
        double i2 = Math.pow(req.getHostDistribution() * req.getHostSusceptibility() * req.getHostValue(), 1.0/3.0);

        // 3. 计算 I3 (降雨量指数) - 算术平均数
        double s3_1 = getRainfallScore(req.getAnnualRainfall(), true);
        double s3_2 = getRainfallScore(req.getSummerRainfall(), false);
        double i3 = (s3_1 + s3_2) / 2.0;

        // 4. 计算 I4 (温度指数) - 算术平均数
        double s4_1 = getTempScore(req.getAnnualTemp(), true);
        double s4_2 = getTempScore(req.getSummerTemp(), false);
        double i4 = (s4_1 + s4_2) / 2.0;

        // 5. 计算 D0
        double d0 = (0.15 * i1) + (0.15 * i2) + (0.30 * i3) + (0.40 * i4);

        // 6. 判定等级
        String level;
        String advice;

        if (d0 >= 2.00) {
            level = "高风险";
            advice = "风险极高！建议立即实施疫区封锁，严禁松木流出。对枯死木进行彻底的伐除和粉碎/烧毁处理（除治），并在天牛羽化期进行大规模化学或生物防治。";
        } else if (d0 >= 1.00) {
            level = "中风险";
            advice = "存在入侵风险。建议加强无人机与地面监测频率，重点清理林间衰弱木。悬挂诱捕器监测媒介昆虫密度，并可释放花绒寄甲进行生物防治。";
        } else {
            level = "低风险";
            advice = "目前风险可控。建议保持日常巡查，通过营林措施优化林分结构（如营造混交林），严格排查外来松木包装材料。";
        }

        // 返回结果 (注意保留三位小数，看起来更专业)
        return new DiagnosisResult(d0, level, advice);
    }

    // 辅助方法：降雨量打分 (参考你的图片表格)
    private double getRainfallScore(Double val, boolean isAnnual) {
        if (val == null) return 0.0;
        if (isAnnual) { // 年均降雨
            if (val < 1548.6) return 3.0;
            if (val < 2304.5) return 2.0;
            return 0.0; // 假设大于这个值不适宜
        } else { // 6-8月降雨
            if (val < 235) return 3.0;
            if (val < 968.8) return 2.0;
            return 1.0;
        }
    }

    // 辅助方法：温度打分
    private double getTempScore(Double val, boolean isAnnual) {
        if (val == null) return 0.0;
        if (isAnnual) {
            if (val >= 14) return 3.0;
            if (val >= 12) return 2.0;
            return 0.0;
        } else {
            if (val >= 25) return 3.0;
            if (val >= 18) return 2.0;
            return 1.0;
        }
    }
}