package com.example.ngy_kintai.Repository;

import com.example.ngy_kintai.Entity.attendance_Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface attendance_Repository extends JpaRepository<attendance_Entity, Long> {
    List<attendance_Entity> findByUserIdOrderByStartTimeDesc(String userId);
}