package com.example.social_network.Repository;

import com.example.social_network.models.Entity.StoryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryViewRepository extends JpaRepository<StoryView, StoryView.StoryViewId> {

    // Danh sách lượt xem của 1 story, mới xem nhất trước; JOIN FETCH user để trả thông tin người xem.
    @Query("SELECT v FROM StoryView v JOIN FETCH v.user " +
            "WHERE v.story.id = :storyId ORDER BY v.viewedAt DESC")
    List<StoryView> findViewers(@Param("storyId") String storyId);

    // Danh sách id các story mà 1 user đã xem (để FE tô viền "đã xem" theo DB).
    @Query("SELECT v.story.id FROM StoryView v WHERE v.user.id = :userId")
    List<String> findSeenStoryIds(@Param("userId") String userId);
}
