package com.example.social_network.models.Entity;

import com.example.social_network.models.Enum.LikeTargetType;
import com.example.social_network.models.Enum.ReactionType;
import lombok.*;
import javax.persistence.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_user", "target_type", "target_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like implements Serializable {

    @Id
    @Column(name = "id_like", length = 36, nullable = false, updatable = false)
    private String id;

    // Polymorphic: POST hoặc COMMENT
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 10)
    private LikeTargetType targetType;

    // ID của Post hoặc Comment được like (không có FK cứng để hỗ trợ polymorphic)
    @Column(name = "target_id", nullable = false, length = 36)
    private String targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false, length = 10)
    private ReactionType reactionType; // LIKE, HEART, HAHA, WOW, SAD, ANGRY

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (reactionType == null) reactionType = ReactionType.LIKE;
        createTime = LocalDateTime.now();
    }
}
