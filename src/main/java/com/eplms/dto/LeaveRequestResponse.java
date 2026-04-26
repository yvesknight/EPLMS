package com.eplms.dto;

import com.eplms.model.LeaveStatus;
import com.eplms.model.LeaveType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveRequestResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalDays;
    private String reason;
    private LeaveStatus status;
    private String approverComment;
}
