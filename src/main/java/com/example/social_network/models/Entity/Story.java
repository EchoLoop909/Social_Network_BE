package com.example.social_network.models.Entity;

import com.example.social_network.models.Enum.MediaType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "stories")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Story implements Serializable {

    @Id
    @Column(name = "id_story", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Column(name = "media_url", nullable = false, length = 2048)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 10)
    private MediaType mediaType;

    @Column(name = "caption", length = 2200)
    private String caption;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Story hết hạn sau 24h — luôn filter: WHERE expires_at > NOW()
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // true = đã lưu vào Highlights (không bị xóa sau 24h)
    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived;

    @JsonIgnore
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoryView> views = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
        expiresAt = createdAt.plusHours(24);
        if (isArchived == null) isArchived = false;
        if (mediaType == null) mediaType = MediaType.IMAGE;
    }
}
