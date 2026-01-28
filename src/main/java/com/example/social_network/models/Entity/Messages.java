package com.example.social_network.models.Entity;

import lombok.Data;
import javax.persistence.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Data
public class Messages implements Serializable {

    @Id
    @Column(name = "id_message", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "text", nullable = false, length = 3000)
    private String text;

    @Column(name = "photo")
    private String photo;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_transmitter", nullable = false)
    private User userTransmitter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_receiver", nullable = false)
    private User userReceiver;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        createTime = LocalDateTime.now();
    }
}
