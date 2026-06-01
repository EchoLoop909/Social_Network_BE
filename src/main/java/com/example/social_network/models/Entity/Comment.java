package com.example.social_network.models.Entity;


import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "comments")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Comment implements Serializable {

    @Id
    @Column(name = "id_comment", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "text", nullable = false, length = 2000)
    private String text;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_post", nullable = false)
    private Post post;

    // Threaded comments: NULL = comment gốc; có giá trị = Reply
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id", referencedColumnName = "id_comment")
    @JsonIgnore
    private Comment parentComment;

    // Danh sách các reply trực tiếp của comment này
    @JsonIgnore
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        createTime = LocalDateTime.now();
    }
}
