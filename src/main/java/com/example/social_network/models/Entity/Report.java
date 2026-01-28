package com.example.social_network.models.Entity;

import lombok.Data;
import javax.persistence.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Data
public class Report implements Serializable {

    @Id
    @Column(name = "id_report", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_post", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_report_reason", nullable = false)
    private ReportReason reportReason;

    @Column(name = "extra_information", length = 2000)
    private String extraInformation;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        createTime = LocalDateTime.now();
    }
}
