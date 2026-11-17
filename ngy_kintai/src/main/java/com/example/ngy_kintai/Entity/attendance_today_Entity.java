package com.example.ngy_kintai.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_today")
@Data
public class attendance_today_Entity {
    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "work_status_id")
    private String workStatusId;
}