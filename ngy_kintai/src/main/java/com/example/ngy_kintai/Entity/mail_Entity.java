package com.example.ngy_kintai.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "massege")
@Data
public class mail_Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mail_id")
    private Integer mailId;

    @Column(name = "sender_user_id", length = 8, nullable = false)
    private String senderUserId;

    @Column(name = "receiver_user_id", length = 8, nullable = false)
    private String receiverUserId;

    @Column(name = "subject", length = 32, nullable = false)
    private String subject;

    @Column(name = "body", length = 256, nullable = false)
    private String body;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}