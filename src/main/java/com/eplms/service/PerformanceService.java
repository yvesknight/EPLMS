package com.eplms.service;

import com.eplms.dto.*;
import com.eplms.exception.BusinessException;
import com.eplms.exception.ResourceNotFoundException;
import com.eplms.model.*;
import com.eplms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PerformanceService {

    private final PerformanceGoalRepository goalRepo;
    private final PerformanceReviewRepository reviewRepo;
    private final EmployeeRepository employeeRepo;
    private final ManagerRepository managerRepo;
    private final NotificationService notificationService;

    public PerformanceService(PerformanceGoalRepository goalRepo, PerformanceReviewRepository reviewRepo,
                               EmployeeRepository employeeRepo, ManagerRepository managerRepo,
                               NotificationService notificationService) {
        this.goalRepo = goalRepo;
        this.reviewRepo = reviewRepo;
        this.employeeRepo = employeeRepo;
        this.managerRepo = managerRepo;
        this.notificationService = notificationService;
    }

    @Transactional
    public GoalResponse createGoal(GoalDto dto) {
        Employee employee = employeeRepo.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + dto.getEmployeeId()));

        PerformanceGoal goal = new PerformanceGoal();
        goal.setEmployee(employee);
        goal.setTitle(dto.getTitle());
        goal.setDescription(dto.getDescription());
        goal.setTargetDate(dto.getTargetDate());
        goal.setStatus(dto.getStatus() != null ? dto.getStatus() : GoalStatus.IN_PROGRESS);
        return toGoalResponse(goalRepo.save(goal));
    }

    @Transactional
    public GoalResponse updateGoalStatus(Long goalId, GoalStatus status) {
        PerformanceGoal goal = goalRepo.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + goalId));
        goal.setStatus(status);
        return toGoalResponse(goalRepo.save(goal));
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> getGoalsByEmployee(Long employeeId) {
        return goalRepo.findByEmployeeId(employeeId).stream().map(this::toGoalResponse).toList();
    }

    @Transactional
    public ReviewResponse submitSelfReview(SelfReviewDto dto) {
        Employee employee = employeeRepo.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + dto.getEmployeeId()));

        if (reviewRepo.findByEmployeeIdAndReviewYear(dto.getEmployeeId(), dto.getReviewYear()).isPresent()) {
            throw new BusinessException("Self-review already submitted for year: " + dto.getReviewYear());
        }

        PerformanceReview review = new PerformanceReview();
        review.setEmployee(employee);
        review.setReviewYear(dto.getReviewYear());
        review.setSelfReview(dto.getSelfReview());
        review.setSelfRating(dto.getSelfRating());
        review.setStatus(ReviewStatus.MANAGER_REVIEW_PENDING);

        ReviewResponse response = toReviewResponse(reviewRepo.save(review));
        notificationService.notifyReviewSubmitted(employee.getName(), dto.getReviewYear());
        return response;
    }

    @Transactional
    public ReviewResponse submitManagerReview(ManagerReviewDto dto) {
        PerformanceReview review = reviewRepo.findById(dto.getReviewId())
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + dto.getReviewId()));

        if (review.getStatus() == ReviewStatus.COMPLETED) {
            throw new BusinessException("Review is already completed");
        }

        Manager reviewer = managerRepo.findById(dto.getReviewerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found: " + dto.getReviewerId()));

        review.setReviewer(reviewer);
        review.setManagerReview(dto.getManagerReview());
        review.setManagerRating(dto.getManagerRating());
        review.setFinalRating(calculateFinalRating(review.getSelfRating(), dto.getManagerRating()));
        review.setStatus(ReviewStatus.COMPLETED);

        ReviewResponse response = toReviewResponse(reviewRepo.save(review));
        notificationService.notifyManagerReviewCompleted(review.getEmployee().getName(), review.getFinalRating());
        return response;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByEmployee(Long employeeId) {
        return reviewRepo.findByEmployeeId(employeeId).stream().map(this::toReviewResponse).toList();
    }

    private double calculateFinalRating(Double selfRating, Double managerRating) {
        if (selfRating == null && managerRating == null) return 0;
        if (selfRating == null) return managerRating;
        if (managerRating == null) return selfRating;
        return Math.round(((selfRating * 0.4) + (managerRating * 0.6)) * 10.0) / 10.0;
    }

    private GoalResponse toGoalResponse(PerformanceGoal g) {
        GoalResponse res = new GoalResponse();
        res.setId(g.getId());
        res.setEmployeeId(g.getEmployee().getId());
        res.setEmployeeName(g.getEmployee().getName());
        res.setTitle(g.getTitle());
        res.setDescription(g.getDescription());
        res.setTargetDate(g.getTargetDate());
        res.setStatus(g.getStatus());
        return res;
    }

    private ReviewResponse toReviewResponse(PerformanceReview r) {
        ReviewResponse res = new ReviewResponse();
        res.setId(r.getId());
        res.setEmployeeId(r.getEmployee().getId());
        res.setEmployeeName(r.getEmployee().getName());
        res.setReviewYear(r.getReviewYear());
        res.setSelfReview(r.getSelfReview());
        res.setManagerReview(r.getManagerReview());
        res.setSelfRating(r.getSelfRating());
        res.setManagerRating(r.getManagerRating());
        res.setFinalRating(r.getFinalRating());
        res.setSubmittedDate(r.getSubmittedDate());
        res.setStatus(r.getStatus());
        if (r.getReviewer() != null) {
            res.setReviewerId(r.getReviewer().getId());
            res.setReviewerName(r.getReviewer().getName());
        }
        return res;
    }
}
