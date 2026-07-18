package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    // Danh sách thông báo của 1 người nhận, mới nhất trước (phân trang)
    Page<Notification> findByRecipient_IdOrderByCreateTimeDesc(String recipientId, Pageable pageable);

    // Đếm số thông báo chưa đọc (cho badge chuông)
    long countByRecipient_IdAndIsReadFalse(String recipientId);

    // Đánh dấu tất cả thông báo của 1 người là đã đọc
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :userId AND n.isRead = false")
    int markAllRead(@Param("userId") String userId);
}
