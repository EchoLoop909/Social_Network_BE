package com.example.social_network.Scheduler;

import com.example.social_network.Repository.StoryRepository;
import com.example.social_network.models.Entity.Story;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tác nhân Hệ thống (Phân hệ 4 - "Thu hồi dữ liệu hết hạn"):
 * định kỳ quét và xóa các story đã hết hạn, TRỪ story đã lưu Highlight (is_archived = true).
 * Dùng deleteAll(entities) để cascade xóa story_views (bulk DELETE JPQL sẽ không cascade).
 */
@Component
public class StoryCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(StoryCleanupScheduler.class);

    @Autowired
    private StoryRepository storyRepository;

    // Chạy mỗi 15 phút (kèm delay khởi động 1 phút để không quét ngay lúc app vừa lên).
    @Scheduled(fixedRate = 15 * 60 * 1000, initialDelay = 60 * 1000)
    @Transactional
    public void purgeExpiredStories() {
        try {
            List<Story> expired = storyRepository.findByExpiresAtBeforeAndIsArchivedFalse(LocalDateTime.now());
            if (!expired.isEmpty()) {
                storyRepository.deleteAll(expired);
                logger.info("Scheduler: đã thu hồi {} story hết hạn", expired.size());
            }
        } catch (Exception e) {
            logger.error("Lỗi khi thu hồi story hết hạn: {}", e.getMessage());
        }
    }
}
