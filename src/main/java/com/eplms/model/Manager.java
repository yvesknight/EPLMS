package com.eplms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "managers")
public class Manager extends User {

    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    private List<Employee> team = new ArrayList<>();
}
