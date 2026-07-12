package com.example.social_network.models.Entity;

import com.example.social_network.models.Enum.PostStatus;
import com.example.social_network.models.Enum.PostType;
import com.example.social_network.models.Enum.PostVisibility;
import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import javax.persistence.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "posts")
@Data
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Post implements Serializable {

    @Id
    @Column(name = "id_post", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "text", length = 5000)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 20)
    private PostType postType; // IMAGE, VIDEO, CAROUSEL, TEXT

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private PostVisibility visibility; // PUBLIC, FOLLOWERS, PRIVATE

    // Trạng thái kiểm duyệt: PENDING_REVIEW -> PUBLISHED / FLAGGED
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PostStatus status;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "is_pinned")
    private Boolean isPinned;

    // Bộ đếm denormalized để tránh COUNT(*) tốn kém khi render bảng tin
    @Column(name = "reaction_count", nullable = false)
    private Integer reactionCount;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // Trả kèm trong response để FE render ảnh/video (id_post ở PostMedia đã @JsonIgnore để tránh đệ quy)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<PostMedia> mediaList = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (id == null)
            id = UUID.randomUUID().toString();
        createTime = LocalDateTime.now();
        isPinned = false;
        if (postType == null)
            postType = PostType.IMAGE;
        if (visibility == null)
            visibility = PostVisibility.PUBLIC;
        if (status == null)
            status = PostStatus.PENDING_REVIEW;
        if (reactionCount == null)
            reactionCount = 0;
        if (commentCount == null)
            commentCount = 0;
    }
}
