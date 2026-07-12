package com.example.social_network.models.Entity;

import com.example.social_network.models.Enum.MediaType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(
        name = "post_media",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_post", "display_order"})
)
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PostMedia implements Serializable {

    @Id
    @Column(name = "id_media", length = 36, nullable = false, updatable = false)
    private String id;

    @JsonIgnore // tránh đệ quy Post -> mediaList -> post; media luôn được trả kèm trong Post
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_post", nullable = false)
    private Post post;

    @Column(name = "media_url", nullable = false, length = 2048)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 10)
    private MediaType mediaType; // IMAGE hoặc VIDEO

    // Thứ tự hiển thị trong carousel (0-indexed)
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    // Metadata ảnh/video để render responsive (có thể null)
    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    // Thời lượng video tính bằng giây; null nếu là ảnh
    @Column(name = "duration_sec")
    private Integer durationSec;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (displayOrder == null) displayOrder = 0;
        if (mediaType == null) mediaType = MediaType.IMAGE;
    }
}
