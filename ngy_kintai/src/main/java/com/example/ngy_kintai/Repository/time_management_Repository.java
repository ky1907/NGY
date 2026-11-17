package com.example.ngy_kintai.Repository;

import com.example.ngy_kintai.Entity.time_management_Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface time_management_Repository extends JpaRepository<time_management_Entity, Long> {
    Optional<time_management_Entity> findByUserIdAndWorkDate(String userId, LocalDate workDate);
    List<time_management_Entity> findByUserIdAndWorkDateBetweenOrderByWorkDateAsc(String userId, LocalDate start, LocalDate end);
}