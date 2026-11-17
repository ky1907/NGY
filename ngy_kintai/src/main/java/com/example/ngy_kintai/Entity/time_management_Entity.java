package com.example.ngy_kintai.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_managiment")
@Data
public class time_management_Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "work_date")
    private LocalDate workDate;

    @Column(name = "actual_work_minutes")
    private Integer actualWorkMinutes;

    @Column(name = "overtime_minutes")
    private Integer overtimeMinutes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}