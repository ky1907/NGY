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
    private Long mailId;

    @Column(name = "sender_user_id")
    private String senderUserId;

    @Column(name = "receiver_user_id")
    private String receiverUserId;

    @Column(name = "subject")
    private String subject;

    @Column(name = "body")
    private String body;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}