package com.eplms.config;

import com.eplms.model.*;
import com.eplms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seedData(EmployeeRepository employeeRepo,
                               ManagerRepository managerRepo,
                               HRManagerRepository hrRepo,
                               LeaveBalanceRepository balanceRepo,
                               PasswordEncoder encoder) {
        return args -> {
            if (managerRepo.count() > 0) return;

            // HR Manager
            HRManager hr = new HRManager();
            hr.setName("Alice HR");
            hr.setEmail("hr@eplms.com");
            hr.setPassword(encoder.encode("password"));
            hr.setRole(Role.HR_MANAGER);
            hrRepo.save(hr);

            // Manager
            Manager manager = new Manager();
            manager.setName("Bob Manager");
            manager.setEmail("manager@eplms.com");
            manager.setPassword(encoder.encode("password"));
            manager.setRole(Role.MANAGER);
            managerRepo.save(manager);

            // Employee 1
            Employee emp1 = new Employee();
            emp1.setName("Carol Employee");
            emp1.setEmail("carol@eplms.com");
            emp1.setPassword(encoder.encode("password"));
            emp1.setRole(Role.EMPLOYEE);
            emp1.setManager(manager);
            employeeRepo.save(emp1);

            LeaveBalance balance1 = new LeaveBalance();
            balance1.setEmployee(emp1);
            balanceRepo.save(balance1);

            // Employee 2
            Employee emp2 = new Employee();
            emp2.setName("Dave Employee");
            emp2.setEmail("dave@eplms.com");
            emp2.setPassword(encoder.encode("password"));
            emp2.setRole(Role.EMPLOYEE);
            emp2.setManager(manager);
            employeeRepo.save(emp2);

            LeaveBalance balance2 = new LeaveBalance();
            balance2.setEmployee(emp2);
            balanceRepo.save(balance2);

            log.info("Seed data loaded: hr@eplms.com, manager@eplms.com, carol@eplms.com, dave@eplms.com (password: password)");
        };
    }
}
