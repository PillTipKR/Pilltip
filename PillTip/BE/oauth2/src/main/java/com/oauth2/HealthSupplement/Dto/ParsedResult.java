package com.oauth2.HealthSupplement.Dto;

public class ParsedResult {
    public String name;        // 성분명
    public Double amount;      // 수치 (예: 300.0)
    public String unit;        // 단위 (예: mg, ug, CFU 등)
    public Double minRatio;    // 0.8
    public Double maxRatio;    // 1.2

    @Override
    public String toString() {
        return String.format("성분명: %s, 수치: %.3f, 단위: %s, 범위: %s ~ %s",
                name, amount, unit,
                minRatio != null ? minRatio : "기준없음",
                maxRatio != null ? maxRatio : "기준없음");
    }
}

