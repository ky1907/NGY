package com.example.ngy_kintai.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "department")
@Data
public class department_Entity {
    @Id
    @Column(name = "department_id")
    private String departmentId;

    @Column(name = "department_name")
    private String departmentName;
}