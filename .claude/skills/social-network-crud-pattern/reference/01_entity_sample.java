// Nguồn thật: Social_Network_BE/src/main/java/com/example/social_network/models/Entity/Comment.java
// Dùng làm mẫu convention cho MỌI Entity mới trong package models/Entity

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

/*
QUY TẮC BẮT BUỘC khi tạo Entity mới:
1. @Entity + @Table(name = "<snake_case_plural>")
2. @Data (Lombok) — KHÔNG dùng @Getter/@Setter thủ công
3. @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) — bắt buộc trên MỌI entity
   có quan hệ Lazy, để tránh lỗi serialize proxy Hibernate ra JSON
4. implements Serializable
5. @Id là String (UUID dạng chuỗi), @Column(name = "id_<ten_bang_so_it>", length = 36,
   nullable = false, updatable = false)
6. Quan hệ @ManyToOne LUÔN fetch = FetchType.LAZY
7. Quan hệ ngược (@OneToMany) LUÔN có @JsonIgnore để tránh vòng lặp serialize + tránh
   kéo cả cây con không cần thiết
8. Field thời gian dùng LocalDateTime, tên cột create_time
9. @PrePersist để tự sinh id (UUID.randomUUID().toString()) và set createTime — 
   KHÔNG để logic này ở Service
10. Field đếm/denormalized (ví dụ reaction_count) khởi tạo mặc định 0 trong @PrePersist
    nếu null, không để null xuống DB
*/
