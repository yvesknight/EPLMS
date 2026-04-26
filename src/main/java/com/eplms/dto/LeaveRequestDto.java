package com.eplms.dto;

import com.eplms.model.LeaveType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveRequestDto {
    @NotNull private Long employeeId;
    @NotNull private LeaveType leaveType;
    @NotNull private LocalDate startDate;
    @NotNull @Future private LocalDate endDate;
    private String reason;
}
