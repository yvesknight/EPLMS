package com.eplms.service;

import com.eplms.dto.UserResponse;
import com.eplms.exception.ResourceNotFoundException;
import com.eplms.model.Employee;
import com.eplms.model.Manager;
import com.eplms.repository.EmployeeRepository;
import com.eplms.repository.ManagerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final EmployeeRepository employeeRepo;
    private final ManagerRepository managerRepo;

    public UserService(EmployeeRepository employeeRepo, ManagerRepository managerRepo) {
        this.employeeRepo = employeeRepo;
        this.managerRepo = managerRepo;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllEmployees() {
        return employeeRepo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getEmployee(Long id) {
        return employeeRepo.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllManagers() {
        return managerRepo.findAll().stream().map(this::toResponse).toList();
    }

    private UserResponse toResponse(Employee emp) {
        UserResponse res = new UserResponse();
        res.setId(emp.getId());
        res.setName(emp.getName());
        res.setEmail(emp.getEmail());
        res.setRole(emp.getRole());
        if (emp.getManager() != null) {
            res.setManagerId(emp.getManager().getId());
            res.setManagerName(emp.getManager().getName());
        }
        return res;
    }

    private UserResponse toResponse(Manager mgr) {
        UserResponse res = new UserResponse();
        res.setId(mgr.getId());
        res.setName(mgr.getName());
        res.setEmail(mgr.getEmail());
        res.setRole(mgr.getRole());
        return res;
    }
}
