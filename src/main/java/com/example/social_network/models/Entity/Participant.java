package com.example.social_network.models.Entity;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "participants")
@Data
public class Participant implements Serializable {

    @Id
    @Column(name = "id_participant", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conversation", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    // Lưu ID của tin nhắn cuối cùng user này đã đọc (Để hiện chữ in đậm/in nhạt trong Inbox)
    @Column(name = "last_read_message_id")
    private String lastReadMessageId;

    @Column(name = "joined_time", nullable = false)
    private LocalDateTime joinedTime;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        joinedTime = LocalDateTime.now();
    }
}