package com.eplms.service;

import com.eplms.dto.*;
import com.eplms.exception.BusinessException;
import com.eplms.exception.ResourceNotFoundException;
import com.eplms.model.*;
import com.eplms.repository.*;
import com.eplms.strategy.ApprovalEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRepo;
    private final LeaveBalanceRepository balanceRepo;
    private final EmployeeRepository employeeRepo;
    private final ApprovalEngine approvalEngine;
    private final NotificationService notificationService;

    public LeaveService(LeaveRequestRepository leaveRepo, LeaveBalanceRepository balanceRepo,
                        EmployeeRepository employeeRepo, ApprovalEngine approvalEngine,
                        NotificationService notificationService) {
        this.leaveRepo = leaveRepo;
        this.balanceRepo = balanceRepo;
        this.employeeRepo = employeeRepo;
        this.approvalEngine = approvalEngine;
        this.notificationService = notificationService;
    }

    @Transactional
    public LeaveRequestResponse submitLeave(LeaveRequestDto dto) {
        Employee employee = employeeRepo.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + dto.getEmployeeId()));

        if (!dto.getEndDate().isAfter(dto.getStartDate())) {
            throw new BusinessException("End date must be after start date");
        }

        int totalDays = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;

        LeaveBalance balance = balanceRepo.findByEmployeeId(employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave balance not found for employee: " + employee.getId()));

        if (balance.getBalanceFor(dto.getLeaveType()) < totalDays) {
            throw new BusinessException("Insufficient " + dto.getLeaveType() + " leave balance. Available: "
                    + balance.getBalanceFor(dto.getLeaveType()) + " days");
        }

        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setLeaveType(dto.getLeaveType());
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setTotalDays(totalDays);
        request.setReason(dto.getReason());

        LeaveRequest saved = leaveRepo.save(request);
        approvalEngine.evaluate(saved);
        leaveRepo.save(saved);

        if (saved.getStatus() == LeaveStatus.AUTO_APPROVED) {
            balance.deductBalance(dto.getLeaveType(), totalDays);
            balanceRepo.save(balance);
        }
        if (saved.getStatus() == LeaveStatus.ESCALATED) {
            notificationService.notifyLeaveEscalated(employee.getName(), totalDays);
        }

        notificationService.notifyLeaveStatusChange(employee.getName(), saved.getStatus().name(), saved.getApproverComment());
        return toResponse(saved);
    }

    @Transactional
    public LeaveRequestResponse processApproval(Long requestId, ApprovalActionDto dto) {
        LeaveRequest request = leaveRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + requestId));

        if (request.getStatus() == LeaveStatus.AUTO_APPROVED || request.getStatus() == LeaveStatus.APPROVED
                || request.getStatus() == LeaveStatus.REJECTED) {
            throw new BusinessException("Leave request is already finalized with status: " + request.getStatus());
        }

        boolean approved = "APPROVE".equalsIgnoreCase(dto.getAction());
        request.setStatus(approved ? LeaveStatus.APPROVED : LeaveStatus.REJECTED);
        request.setApproverComment(dto.getComment());

        if (approved) {
            LeaveBalance balance = balanceRepo.findByEmployeeId(request.getEmployee().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Leave balance not found"));
            balance.deductBalance(request.getLeaveType(), request.getTotalDays());
            balanceRepo.save(balance);
        }

        leaveRepo.save(request);
        notificationService.notifyLeaveStatusChange(request.getEmployee().getName(),
                request.getStatus().name(), request.getApproverComment());
        return toResponse(request);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getLeavesByEmployee(Long employeeId) {
        return leaveRepo.findByEmployeeId(employeeId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LeaveBalanceResponse getBalance(Long employeeId) {
        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));
        LeaveBalance balance = balanceRepo.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave balance not found for employee: " + employeeId));

        LeaveBalanceResponse res = new LeaveBalanceResponse();
        res.setEmployeeId(employeeId);
        res.setEmployeeName(employee.getName());
        res.setAnnualLeave(balance.getAnnualLeave());
        res.setSickLeave(balance.getSickLeave());
        res.setMaternityLeave(balance.getMaternityLeave());
        res.setPaternityLeave(balance.getPaternityLeave());
        res.setUnpaidLeave(balance.getUnpaidLeave());
        return res;
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getPendingLeaves() {
        return leaveRepo.findByStatus(LeaveStatus.PENDING).stream().map(this::toResponse).toList();
    }

    private LeaveRequestResponse toResponse(LeaveRequest r) {
        LeaveRequestResponse res = new LeaveRequestResponse();
        res.setId(r.getId());
        res.setEmployeeId(r.getEmployee().getId());
        res.setEmployeeName(r.getEmployee().getName());
        res.setLeaveType(r.getLeaveType());
        res.setStartDate(r.getStartDate());
        res.setEndDate(r.getEndDate());
        res.setTotalDays(r.getTotalDays());
        res.setReason(r.getReason());
        res.setStatus(r.getStatus());
        res.setApproverComment(r.getApproverComment());
        return res;
    }
}
