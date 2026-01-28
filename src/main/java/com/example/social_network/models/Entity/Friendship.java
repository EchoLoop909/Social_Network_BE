package com.example.social_network.models.Entity;

import lombok.*;
import javax.persistence.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "friendships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_user_transmitter", "id_user_receiver"})
)
@Data
public class Friendship implements Serializable {

    @Id
    @Column(name = "id_friendship", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_transmitter", nullable = false)
    private User userTransmitter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_receiver", nullable = false)
    private User userReceiver;

    @Column(name = "status", nullable = false)
    private Boolean status;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        status = false;
        createTime = LocalDateTime.now();
    }
}
