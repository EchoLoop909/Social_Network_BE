package com.example.social_network.models.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import javax.persistence.*;
import java.io.*;
import java.util.UUID;

@Entity
@Table(name = "status")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Statususer implements Serializable {

    @Id
    @Column(name = "id_status", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "text", unique = true, nullable = false, length = 100)
    private String text;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
    }
}
