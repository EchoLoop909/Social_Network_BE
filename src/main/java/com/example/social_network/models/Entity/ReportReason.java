package com.example.social_network.models.Entity;

import lombok.Data;
import javax.persistence.*;
import java.io.*;
import java.util.UUID;

@Entity
@Table(name = "report_reasons")
@Data
public class ReportReason implements Serializable {

    @Id
    @Column(name = "id_report_reason", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
    }
}
