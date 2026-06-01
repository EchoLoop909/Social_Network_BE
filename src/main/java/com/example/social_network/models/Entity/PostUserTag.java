package com.example.social_network.models.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Tag người dùng vào ảnh/bài đăng (@mention).
 * x_position, y_position: tọa độ phần trăm (%) trên ảnh — null nếu chỉ tag trong caption.
 */
@Entity
@Table(name = "post_user_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PostUserTag implements Serializable {

    @EmbeddedId
    private PostUserTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "id_post", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    // Tọa độ X (%) trên ảnh để hiển thị pin khi hover — null nếu tag trong caption
    @Column(name = "x_position", precision = 5, scale = 2)
    private BigDecimal xPosition;

    // Tọa độ Y (%) trên ảnh
    @Column(name = "y_position", precision = 5, scale = 2)
    private BigDecimal yPosition;

    // ─── Composite PK ────────────────────────────────────────────────────────

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostUserTagId implements Serializable {

        @Column(name = "id_post", length = 36)
        private String postId;

        @Column(name = "id_user", length = 36)
        private String userId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PostUserTagId)) return false;
            PostUserTagId that = (PostUserTagId) o;
            return Objects.equals(postId, that.postId) &&
                   Objects.equals(userId, that.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(postId, userId);
        }
    }
}
