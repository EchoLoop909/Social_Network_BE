package com.example.social_network.models.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lượt xem video của 1 user với 1 bài (post video).
 * Mỗi cặp (user, post) chỉ 1 dòng — cập nhật khi xem lại (giữ số giây xem xa nhất).
 * watchedRatio = watchedSeconds / durationSec -> tín hiệu ngầm cho gợi ý feed.
 */
@Entity
@Table(name = "video_view",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_user", "id_post"}))
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VideoView implements Serializable {

    @Id
    @Column(name = "id_view", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_post", nullable = false)
    private Post post;

    @Column(name = "watched_seconds", nullable = false)
    private Integer watchedSeconds;

    @Column(name = "duration_sec")
    private Integer durationSec;

    // Tỉ lệ xem (0..1). Đã cap ở 1.0.
    @Column(name = "watched_ratio", nullable = false)
    private Double watchedRatio;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (watchedSeconds == null) watchedSeconds = 0;
        if (watchedRatio == null) watchedRatio = 0.0;
        updatedAt = LocalDateTime.now();
    }
}
