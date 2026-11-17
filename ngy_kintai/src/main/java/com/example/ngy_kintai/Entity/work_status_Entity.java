package com.example.ngy_kintai.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "work_status")
@Data
public class work_status_Entity {
    @Id
    @Column(name = "work_status_id")
    private String workStatusId;

    @Column(name = "work_status_name")
    private String workStatusName;
}