package com.example.ngy_kintai.Repository;

import com.example.ngy_kintai.Entity.calendar_Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface calendar_Repository extends JpaRepository<calendar_Entity, Long> {
    List<calendar_Entity> findByUserIdOrderByStartTimeAsc(String userId);
}