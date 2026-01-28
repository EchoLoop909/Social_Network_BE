package com.example.social_network.models.Entity;

import lombok.*;
import javax.persistence.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "followerships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_user_checked", "id_user_follower"})
)
@Data
public class Followership implements Serializable {

    @Id
    @Column(name = "id_followership", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_checked", nullable = false)
    private User userChecked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_follower", nullable = false)
    private User userFollower;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        createTime = LocalDateTime.now();
    }
}
