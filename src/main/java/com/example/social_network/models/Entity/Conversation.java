package com.example.social_network.models.Entity;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@Data
public class Conversation implements Serializable {

    @Id
    @Column(name = "id_conversation", length = 36, nullable = false, updatable = false)
    private String id;

    // Có thể null nếu là chat 1-1, có tên nếu là Group Chat
    @Column(name = "name")
    private String name;

    // Phân biệt: SINGLE (1-1) hoặc GROUP
    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime; // Rất quan trọng để sort danh sách Inbox

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        createTime = LocalDateTime.now();
        lastMessageTime = LocalDateTime.now();
    }
}
