package com.eplms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ManagerReviewDto {
    @NotNull private Long reviewId;
    @NotNull private Long reviewerId;
    private String managerReview;
    @Min(1) @Max(5) private Double managerRating;
}
