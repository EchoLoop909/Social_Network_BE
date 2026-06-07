package com.example.social_network.models.Entity;

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

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "is_pinned")
    private Boolean isPinned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
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
    }
}
