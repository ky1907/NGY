package com.example.ngy_kintai.Repository;

import com.example.ngy_kintai.Entity.user_Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface user_Repository extends JpaRepository<user_Entity, String> {
    user_Entity findByUserIdAndPassword(String userId, String password);
    List<user_Entity> findByDepartmentIdOrderByUserNameAsc(String departmentId);
    List<user_Entity> findByUserNameContaining(String userName);
}
