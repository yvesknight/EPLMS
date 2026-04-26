package com.eplms.controller;

import com.eplms.dto.*;
import com.eplms.model.GoalStatus;
import com.eplms.service.PerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/performance")
@Tag(name = "Performance Management")
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @PostMapping("/goals")
    @Operation(summary = "Create a performance goal")
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(performanceService.createGoal(dto));
    }

    @PutMapping("/goals/{goalId}/status")
    @Operation(summary = "Update goal status")
    public ResponseEntity<GoalResponse> updateGoalStatus(
            @PathVariable Long goalId,
            @RequestParam GoalStatus status) {
        return ResponseEntity.ok(performanceService.updateGoalStatus(goalId, status));
    }

    @GetMapping("/goals/employee/{employeeId}")
    @Operation(summary = "Get all goals for an employee")
    public ResponseEntity<List<GoalResponse>> getGoals(@PathVariable Long employeeId) {
        return ResponseEntity.ok(performanceService.getGoalsByEmployee(employeeId));
    }

    @PostMapping("/review/self")
    @Operation(summary = "Submit self-review")
    public ResponseEntity<ReviewResponse> submitSelfReview(@Valid @RequestBody SelfReviewDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(performanceService.submitSelfReview(dto));
    }

    @PostMapping("/review/manager")
    @Operation(summary = "Submit manager review")
    public ResponseEntity<ReviewResponse> submitManagerReview(@Valid @RequestBody ManagerReviewDto dto) {
        return ResponseEntity.ok(performanceService.submitManagerReview(dto));
    }

    @GetMapping("/review/employee/{employeeId}")
    @Operation(summary = "Get all reviews for an employee")
    public ResponseEntity<List<ReviewResponse>> getReviews(@PathVariable Long employeeId) {
        return ResponseEntity.ok(performanceService.getReviewsByEmployee(employeeId));
    }
}
