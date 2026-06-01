package com.example.social_network.models.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Lưu trữ những người đã xem Story.
 * Composite PK (id_story, id_user) đảm bảo mỗi user chỉ được ghi nhận 1 lần / story.
 */
@Entity
@Table(name = "story_views")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StoryView implements Serializable {

    @EmbeddedId
    private StoryViewId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("storyId")
    @JoinColumn(name = "id_story", nullable = false)
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    @PrePersist
    public void prePersist() {
        if (viewedAt == null) viewedAt = LocalDateTime.now();
    }

    // ─── Composite PK ────────────────────────────────────────────────────────

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoryViewId implements Serializable {

        @Column(name = "id_story", length = 36)
        private String storyId;

        @Column(name = "id_user", length = 36)
        private String userId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StoryViewId)) return false;
            StoryViewId that = (StoryViewId) o;
            return Objects.equals(storyId, that.storyId) &&
                   Objects.equals(userId, that.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(storyId, userId);
        }
    }
}
