package com.fundraise.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ReadinessScoreDto {
    private String category;
    private BigDecimal score;
    private LocalDateTime computedAt;

    public String getGrade() {
        if (score == null) return "N/A";
        double s = score.doubleValue();
        if (s >= 90) return "A";
        if (s >= 75) return "B";
        if (s >= 60) return "C";
        if (s >= 40) return "D";
        return "F";
    }
}
