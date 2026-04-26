package com.eplms.dto;

import com.eplms.model.GoalStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GoalResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String title;
    private String description;
    private LocalDate targetDate;
    private GoalStatus status;
}
