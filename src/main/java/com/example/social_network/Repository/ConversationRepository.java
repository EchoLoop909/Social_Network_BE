package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    // Tùy chọn: Tìm kiếm nhóm chat theo tên (nếu là Group Chat)
    // List<Conversation> findByNameContainingIgnoreCaseAndType(String name, String type);

}