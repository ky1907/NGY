package com.example.ngy_kintai.Repository;

import com.example.ngy_kintai.Entity.month_time_management_Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface month_time_management_Repository extends JpaRepository<month_time_management_Entity, Long> {
    Optional<month_time_management_Entity> findByUserIdAndTargetYearMonth(String userId, String targetYearMonth);
}