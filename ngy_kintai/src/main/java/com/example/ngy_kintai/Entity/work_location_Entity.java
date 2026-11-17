package com.example.ngy_kintai.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "work_location")
@Data
public class work_location_Entity {
    @Id
    @Column(name = "work_location_id")
    private String workLocationId;

    @Column(name = "work_location_name")
    private String workLocationName;
}