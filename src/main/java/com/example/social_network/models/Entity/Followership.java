package com.example.social_network.models.Entity;

import com.example.social_network.models.Enum.FollowStatus;
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    // ACCEPTED: Public account hoặc đã được duyệt
    // PENDING:  Tài khoản Private, chờ phê duyệt
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private FollowStatus status;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    // Thời điểm cập nhật gần nhất (đổi trạng thái duyệt/từ chối) — null nếu chưa từng đổi
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // Thời điểm lời mời được chấp thuận (chuyển sang ACCEPTED) — null nếu chưa được duyệt
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (status == null) status = FollowStatus.FOLLOWING;
        createTime = LocalDateTime.now();
    }
}
