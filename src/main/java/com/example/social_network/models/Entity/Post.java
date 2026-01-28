package com.example.social_network.models.Entity;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import javax.persistence.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
@Entity
@Table(name = "posts")
@Data
public class Post implements Serializable {

    @Id
    @Column(name = "id_post", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "text", length = 5000)
    private String text;

    @Column(name = "photo")
    private String photo;

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
    private List<Like> likes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        createTime = LocalDateTime.now();
        isPinned = false;
    }
}
