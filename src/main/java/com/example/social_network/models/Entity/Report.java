package com.example.social_network.models.Entity;

import com.example.social_network.models.Enum.ReportStatus;
import com.example.social_network.models.Enum.ReportTargetType;
import lombok.*;
import javax.persistence.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Báo cáo vi phạm. Đối tượng bị báo cáo có thể là BÀI VIẾT hoặc TÀI KHOẢN
 * (polymorphic qua targetType + targetId). Admin duyệt qua trường status.
 */
@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report implements Serializable {

    @Id
    @Column(name = "id_report", length = 36, nullable = false, updatable = false)
    private String id;

    // Người gửi báo cáo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reporter", nullable = false)
    private User reporter;

    // Loại đối tượng bị báo cáo: POST hoặc USER
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 10)
    private ReportTargetType targetType;

    // ID của bài viết hoặc tài khoản bị báo cáo (không FK cứng để hỗ trợ polymorphic)
    @Column(name = "target_id", nullable = false, length = 36)
    private String targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_report_reason", nullable = false)
    private ReportReason reportReason;

    @Column(name = "extra_information", length = 2000)
    private String extraInformation;

    // Trạng thái xử lý của quản trị viên: PENDING -> RESOLVED / REJECTED
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private ReportStatus status;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (status == null) status = ReportStatus.PENDING;
        createTime = LocalDateTime.now();
    }
}
