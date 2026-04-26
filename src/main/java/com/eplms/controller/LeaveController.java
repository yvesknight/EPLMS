package com.eplms.controller;

import com.eplms.dto.*;
import com.eplms.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave")
@Tag(name = "Leave Management")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping("/request")
    @Operation(summary = "Submit a leave request")
    public ResponseEntity<LeaveRequestResponse> submitLeave(@Valid @RequestBody LeaveRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.submitLeave(dto));
    }

    @GetMapping("/balance/{employeeId}")
    @Operation(summary = "Get leave balance for an employee")
    public ResponseEntity<LeaveBalanceResponse> getBalance(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getBalance(employeeId));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get all leave requests for an employee")
    public ResponseEntity<List<LeaveRequestResponse>> getLeavesByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getLeavesByEmployee(employeeId));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all pending leave requests")
    public ResponseEntity<List<LeaveRequestResponse>> getPendingLeaves() {
        return ResponseEntity.ok(leaveService.getPendingLeaves());
    }

    @PutMapping("/approve/{id}")
    @Operation(summary = "Approve or reject a leave request")
    public ResponseEntity<LeaveRequestResponse> processApproval(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalActionDto dto) {
        return ResponseEntity.ok(leaveService.processApproval(id, dto));
    }
}
