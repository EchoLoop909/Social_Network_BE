package com.example.social_network.models.Entity;

import com.example.social_network.models.Enum.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Thông báo đẩy tới người dùng (Notification Engine).
 * Được sinh ra khi có sự kiện like/comment/reply/follow/tag.
 */
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Notification implements Serializable {

    @Id
    @Column(name = "id_notification", length = 36, nullable = false, updatable = false)
    private String id;

    // Người nhận thông báo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recipient", nullable = false)
    private User recipient;

    // Người tạo ra sự kiện (null nếu là thông báo hệ thống)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actor")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    // ID của đối tượng liên quan (bài viết, bình luận...) để điều hướng khi bấm vào — có thể null
    @Column(name = "target_id", length = 36)
    private String targetId;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (isRead == null) isRead = false;
        createTime = LocalDateTime.now();
    }
}
