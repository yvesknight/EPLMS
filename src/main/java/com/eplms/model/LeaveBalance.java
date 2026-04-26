package com.eplms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "leave_balances")
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Column(nullable = false)
    private int annualLeave = 21;

    @Column(nullable = false)
    private int sickLeave = 10;

    @Column(nullable = false)
    private int maternityLeave = 90;

    @Column(nullable = false)
    private int paternityLeave = 5;

    @Column(nullable = false)
    private int unpaidLeave = 30;

    public int getBalanceFor(LeaveType type) {
        return switch (type) {
            case ANNUAL -> annualLeave;
            case SICK -> sickLeave;
            case MATERNITY -> maternityLeave;
            case PATERNITY -> paternityLeave;
            case UNPAID -> unpaidLeave;
        };
    }

    public void deductBalance(LeaveType type, int days) {
        switch (type) {
            case ANNUAL -> annualLeave -= days;
            case SICK -> sickLeave -= days;
            case MATERNITY -> maternityLeave -= days;
            case PATERNITY -> paternityLeave -= days;
            case UNPAID -> unpaidLeave -= days;
        }
    }
}
