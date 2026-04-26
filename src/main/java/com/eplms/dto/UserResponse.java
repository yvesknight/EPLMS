package com.eplms.dto;

import com.eplms.model.Role;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private Long managerId;
    private String managerName;
}
