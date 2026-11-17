package com.example.ngy_kintai.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "month_time_managiment")
@Data
public class month_time_management_Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "target_year_month")
    private String targetYearMonth;

    @Column(name = "monthly_actual_minutes")
    private Integer monthlyActualMinutes;

    @Column(name = "monthly_overtime_minutes")
    private Integer monthlyOvertimeMinutes;
}