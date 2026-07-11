package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Like;
import com.example.social_network.models.Enum.LikeTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, String> {

    // API 1/2/4: tìm cảm xúc hiện tại của 1 user cho 1 đối tượng (khớp UNIQUE id_user+target_type+target_id)
    Optional<Like> findByUser_IdAndTargetTypeAndTargetId(String userId, LikeTargetType targetType, String targetId);

    // API 3: danh sách người đã react cho 1 đối tượng, mới nhất trước
    Page<Like> findByTargetTypeAndTargetIdOrderByCreateTimeDesc(LikeTargetType targetType, String targetId, Pageable pageable);

    // API 3: thống kê số lượng theo từng loại cảm xúc (GROUP BY) — trả [reaction_type, count]
    @Query(value =
            "SELECT reaction_type, COUNT(*) " +
                    "FROM likes " +
                    "WHERE target_type = :targetType AND target_id = :targetId " +
                    "GROUP BY reaction_type",
            nativeQuery = true)
    List<Object[]> countByReactionType(@Param("targetType") String targetType,
                                       @Param("targetId") String targetId);
}
