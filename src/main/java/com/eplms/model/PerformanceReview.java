package com.eplms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "performance_reviews")
public class PerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private Manager reviewer;

    @Column(nullable = false)
    private int reviewYear;

    @Column(length = 2000)
    private String selfReview;

    @Column(length = 2000)
    private String managerReview;

    private Double selfRating;

    private Double managerRating;

    private Double finalRating;

    @Column(nullable = false)
    private LocalDate submittedDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.SELF_REVIEW_PENDING;
}
