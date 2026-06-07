package com.example.social_network.models.Entity;

import com.example.social_network.models.Enum.MessageType;
import lombok.*;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    // Chia sẻ bài viết qua tin nhắn (nullable — chỉ có khi messageType = SHARED_POST)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_shared_post")
    private Post sharedPost;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 15)
    private MessageType messageType; // TEXT, IMAGE, VIDEO, SHARED_POST, SYSTEM

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (messageType == null) messageType = MessageType.TEXT;
        createTime = LocalDateTime.now();
    }
}