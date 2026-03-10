package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Participant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, String> {

    // 1. Lấy danh sách tất cả thành viên trong một cuộc trò chuyện cụ thể
    List<Participant> findByConversation_Id(String conversationId);

    // 2. Dùng để Validate: Kiểm tra xem User này có thực sự đang ở trong đoạn chat này không
    // (Bảo mật: Tránh việc user truyền bậy conversationId để nhắn tin vào nhóm họ không tham gia)
    Optional<Participant> findByConversation_IdAndUser_Id(String conversationId, String userId);

    // 3. CỰC KỲ QUAN TRỌNG: Lấy danh sách các cuộc trò chuyện của một User (để hiển thị danh sách Inbox).
    // Sắp xếp theo thời gian có tin nhắn mới nhất giảm dần (Nhóm nào vừa có tin nhắn sẽ nhảy lên đầu)
    Page<Participant> findByUser_IdOrderByConversation_LastMessageTimeDesc(String userId, Pageable pageable);
}