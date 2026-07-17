package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Conversation;
import com.example.social_network.models.Enum.ConversationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    // Tìm hội thoại 1-1 (SINGLE) đã tồn tại giữa 2 user a và b (để mở lại thay vì tạo mới).
    // Điều kiện: conversation có type SINGLE, VÀ có participant là a, VÀ có participant là b.
    @Query("SELECT c FROM Conversation c WHERE c.type = :type " +
            "AND EXISTS (SELECT 1 FROM Participant p1 WHERE p1.conversation = c AND p1.user.id = :a) " +
            "AND EXISTS (SELECT 1 FROM Participant p2 WHERE p2.conversation = c AND p2.user.id = :b)")
    List<Conversation> findSingleBetween(@Param("type") ConversationType type,
                                         @Param("a") String a,
                                         @Param("b") String b);
}
