package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Messages;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Messages, String> {

    // 1. Dùng để load lịch sử tin nhắn khi mở 1 khung chat (phân trang, tin mới nhất lên đầu)
    Page<Messages> findByConversation_IdOrderByCreateTimeDesc(String conversationId, Pageable pageable);

    // 2. Tùy chọn: Lấy tin nhắn cuối cùng của 1 nhóm chat (hữu ích khi hiển thị danh sách Inbox)
    Messages findTopByConversation_IdOrderByCreateTimeDesc(String conversationId);
}