package com.eplms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SelfReviewDto {
    @NotNull private Long employeeId;
    @NotNull private Integer reviewYear;
    private String selfReview;
    @Min(1) @Max(5) private Double selfRating;
}
