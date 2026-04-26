package com.eplms.dto;

import com.eplms.model.ReviewStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReviewResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long reviewerId;
    private String reviewerName;
    private int reviewYear;
    private String selfReview;
    private String managerReview;
    private Double selfRating;
    private Double managerRating;
    private Double finalRating;
    private LocalDate submittedDate;
    private ReviewStatus status;
}
