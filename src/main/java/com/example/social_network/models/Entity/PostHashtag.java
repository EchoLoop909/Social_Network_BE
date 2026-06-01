package com.example.social_network.models.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Bảng trung gian Post ↔ Hashtag (M:N).
 * Dùng composite PK (id_post, id_hashtag) thay vì surrogate key.
 */
@Entity
@Table(name = "post_hashtags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PostHashtag implements Serializable {

    @EmbeddedId
    private PostHashtagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "id_post", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("hashtagId")
    @JoinColumn(name = "id_hashtag", nullable = false)
    private Hashtag hashtag;

    // ─── Composite PK ────────────────────────────────────────────────────────

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostHashtagId implements Serializable {

        @Column(name = "id_post", length = 36)
        private String postId;

        @Column(name = "id_hashtag", length = 36)
        private String hashtagId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PostHashtagId)) return false;
            PostHashtagId that = (PostHashtagId) o;
            return Objects.equals(postId, that.postId) &&
                   Objects.equals(hashtagId, that.hashtagId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(postId, hashtagId);
        }
    }
}
