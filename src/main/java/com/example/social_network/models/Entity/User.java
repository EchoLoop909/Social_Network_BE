package com.example.social_network.models.Entity;

import com.example.social_network.models.Enum.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User implements Serializable {

    @Id
    @Column(name = "id_user", length = 36, nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "username", unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "firstname", nullable = false, length = 100)
    private String firstname;

    @Column(name = "lastname", nullable = false, length = 100)
    private String lastname;

    @Column(name = "surname", nullable = false, length = 100)
    private String surname;

    @Column(name = "photo")
    private String photo;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "deletion_date")
    private LocalDateTime deletionDate;

    @Column(name = "is_checked")
    private Boolean isChecked;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate; // false = Public, true = Private account

    // Trạng thái tài khoản: PENDING_ACTIVATION -> ACTIVE / SUSPENDED
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        creationDate = LocalDateTime.now();
        isChecked = false;
        isPrivate = false;
        if (status == null) status = UserStatus.PENDING_ACTIVATION;
    }
}
