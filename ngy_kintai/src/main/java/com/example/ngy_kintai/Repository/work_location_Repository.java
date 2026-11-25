package com.example.ngy_kintai.Repository;

import com.example.ngy_kintai.Entity.work_location_Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface work_location_Repository extends JpaRepository<work_location_Entity, String> {
}