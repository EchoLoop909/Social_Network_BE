package com.example.social_network.models.Entity;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Data
public class Messages implements Serializable {

    @Id
    @Column(name = "id_message", length = 36, nullable = false, updatable = false)
    private String id;

    // Trỏ vào cuộc trò chuyện thay vì trỏ vào 1 user nhận cụ thể
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conversation", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_transmitter", nullable = false) // Người gửi
    private User userTransmitter;

    @Column(name = "text", length = 3000) // Có thể null nếu chỉ gửi ảnh
    private String text;

    @Column(name = "photo")
    private String photo;

    // Thêm loại tin nhắn: TEXT, IMAGE, SYSTEM (VD: "User A đã thêm User B vào nhóm")
    @Column(name = "message_type")
    private String messageType;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        createTime = LocalDateTime.now();
    }
}