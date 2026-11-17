package com.example.ngy_kintai.Repository;

import com.example.ngy_kintai.Entity.mail_Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface mail_Repository extends JpaRepository<mail_Entity, Long> {
    List<mail_Entity> findByReceiverUserIdOrderBySentAtDesc(String receiverUserId);
    List<mail_Entity> findBySenderUserIdOrderBySentAtDesc(String senderUserId);
}