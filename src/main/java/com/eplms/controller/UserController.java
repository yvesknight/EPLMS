package com.eplms.controller;

import com.eplms.dto.UserResponse;
import com.eplms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/employees")
    @Operation(summary = "Get all employees")
    public ResponseEntity<List<UserResponse>> getAllEmployees() {
        return ResponseEntity.ok(userService.getAllEmployees());
    }

    @GetMapping("/employees/{id}")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<UserResponse> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getEmployee(id));
    }

    @GetMapping("/managers")
    @Operation(summary = "Get all managers")
    public ResponseEntity<List<UserResponse>> getAllManagers() {
        return ResponseEntity.ok(userService.getAllManagers());
    }
}
