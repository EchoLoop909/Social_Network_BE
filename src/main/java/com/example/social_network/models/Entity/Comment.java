package com.example.social_network.models.Entity;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;

import javax.persistence.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "comments")
@Data
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
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

    // Bình luận cha — null nếu là bình luận gốc; có giá trị nếu là câu trả lời (cây phân cấp)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parent_comment")
    private Comment parentComment;

    @JsonIgnore
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    // Bộ đếm denormalized tổng lượt cảm xúc của bình luận
    @Column(name = "reaction_count", nullable = false)
    private Integer reactionCount;

    @PrePersist
    public void prePersist() {
        if (id == null)
            id = UUID.randomUUID().toString();
        createTime = LocalDateTime.now();
        if (reactionCount == null)
            reactionCount = 0;
    }
}
