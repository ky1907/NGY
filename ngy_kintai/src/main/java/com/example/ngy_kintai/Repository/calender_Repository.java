package com.example.ngy_kintai.Repository;

import com.example.ngy_kintai.Entity.calender_Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface calender_Repository extends JpaRepository<calender_Entity, Long> {
    List<calender_Entity> findByUserIdOrderByStartTimeAsc(String userId);
}