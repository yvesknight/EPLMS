package com.eplms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApprovalActionDto {
    @NotBlank private String action; // APPROVE or REJECT
    private String comment;
}
