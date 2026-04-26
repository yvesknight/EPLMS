package com.eplms.service;

import com.eplms.dto.*;
import com.eplms.exception.BusinessException;
import com.eplms.exception.ResourceNotFoundException;
import com.eplms.model.*;
import com.eplms.repository.*;
import com.eplms.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final EmployeeRepository employeeRepo;
    private final ManagerRepository managerRepo;
    private final HRManagerRepository hrRepo;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider tokenProvider;
    private final LeaveBalanceRepository balanceRepo;

    public AuthService(EmployeeRepository employeeRepo, ManagerRepository managerRepo,
                       HRManagerRepository hrRepo, PasswordEncoder encoder,
                       JwtTokenProvider tokenProvider, LeaveBalanceRepository balanceRepo) {
        this.employeeRepo = employeeRepo;
        this.managerRepo = managerRepo;
        this.hrRepo = hrRepo;
        this.encoder = encoder;
        this.tokenProvider = tokenProvider;
        this.balanceRepo = balanceRepo;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        validateEmailUnique(request.getEmail());

        return switch (request.getRole()) {
            case EMPLOYEE -> registerEmployee(request);
            case MANAGER -> registerManager(request);
            case HR_MANAGER -> registerHR(request);
        };
    }

    public AuthResponse login(LoginRequest request) {
        // Try each user table
        var empOpt = employeeRepo.findByEmail(request.getEmail());
        if (empOpt.isPresent()) return buildAuthResponse(empOpt.get(), request.getPassword());

        var mgrOpt = managerRepo.findByEmail(request.getEmail());
        if (mgrOpt.isPresent()) return buildAuthResponse(mgrOpt.get(), request.getPassword());

        var hrOpt = hrRepo.findByEmail(request.getEmail());
        if (hrOpt.isPresent()) return buildAuthResponse(hrOpt.get(), request.getPassword());

        throw new BusinessException("Invalid email or password");
    }

    private AuthResponse buildAuthResponse(User user, String rawPassword) {
        if (!encoder.matches(rawPassword, user.getPassword())) {
            throw new BusinessException("Invalid email or password");
        }
        String token = tokenProvider.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        return new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getId());
    }

    private UserResponse registerEmployee(RegisterRequest req) {
        Employee emp = new Employee();
        emp.setName(req.getName());
        emp.setEmail(req.getEmail());
        emp.setPassword(encoder.encode(req.getPassword()));
        emp.setRole(Role.EMPLOYEE);
        if (req.getManagerId() != null) {
            Manager mgr = managerRepo.findById(req.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found: " + req.getManagerId()));
            emp.setManager(mgr);
        }
        Employee saved = employeeRepo.save(emp);

        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(saved);
        balanceRepo.save(balance);

        return toUserResponse(saved);
    }

    private UserResponse registerManager(RegisterRequest req) {
        Manager mgr = new Manager();
        mgr.setName(req.getName());
        mgr.setEmail(req.getEmail());
        mgr.setPassword(encoder.encode(req.getPassword()));
        mgr.setRole(Role.MANAGER);
        return toUserResponse(managerRepo.save(mgr));
    }

    private UserResponse registerHR(RegisterRequest req) {
        HRManager hr = new HRManager();
        hr.setName(req.getName());
        hr.setEmail(req.getEmail());
        hr.setPassword(encoder.encode(req.getPassword()));
        hr.setRole(Role.HR_MANAGER);
        return toUserResponse(hrRepo.save(hr));
    }

    private void validateEmailUnique(String email) {
        if (employeeRepo.existsByEmail(email) || managerRepo.existsByEmail(email) || hrRepo.existsByEmail(email)) {
            throw new BusinessException("Email already registered: " + email);
        }
    }

    private UserResponse toUserResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setEmail(user.getEmail());
        res.setRole(user.getRole());
        if (user instanceof Employee emp && emp.getManager() != null) {
            res.setManagerId(emp.getManager().getId());
            res.setManagerName(emp.getManager().getName());
        }
        return res;
    }
}
