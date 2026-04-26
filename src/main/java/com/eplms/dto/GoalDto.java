package com.eplms.dto;

import com.eplms.model.GoalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GoalDto {
    @NotNull private Long employeeId;
    @NotBlank private String title;
    private String description;
    @NotNull private LocalDate targetDate;
    private GoalStatus status;
}
