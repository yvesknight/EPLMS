package com.eplms.dto;

import lombok.Data;

@Data
public class LeaveBalanceResponse {
    private Long employeeId;
    private String employeeName;
    private int annualLeave;
    private int sickLeave;
    private int maternityLeave;
    private int paternityLeave;
    private int unpaidLeave;
}
