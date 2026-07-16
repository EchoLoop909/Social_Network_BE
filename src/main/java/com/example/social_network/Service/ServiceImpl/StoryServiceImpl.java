package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Request.StoryRequest;
import com.example.social_network.Repository.StoryRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.StoryService;
import com.example.social_network.models.Dto.DeleteDto;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Entity.Story;
import com.example.social_network.models.Entity.User;
import com.example.social_network.models.Enum.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StoryServiceImpl implements StoryService {

    private static final Logger logger = LoggerFactory.getLogger(StoryServiceImpl.class);

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public ResponseEntity<?> createStory(StoryRequest dto, String userId, String ip) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.UNAUTHORIZED, new ResponseMess(1, "Chưa xác thực người dùng"));
            }
            if (dto == null || dto.getMediaUrl() == null || dto.getMediaUrl().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "mediaUrl is required"));
            }
            if (dto.getCaption() != null && dto.getCaption().length() > 2200) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "Caption vượt quá 2200 ký tự"));
            }

            // mediaType: mặc định IMAGE, nếu truyền thì phải hợp lệ (IMAGE/VIDEO)
            MediaType mediaType = MediaType.IMAGE;
            if (dto.getMediaType() != null && !dto.getMediaType().trim().isEmpty()) {
                try {
                    mediaType = MediaType.valueOf(dto.getMediaType().trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "mediaType không hợp lệ (IMAGE/VIDEO)"));
                }
            }

            // Số giờ hết hạn: mặc định 24h, hợp lệ 1..168 (7 ngày)
            int hours = 24;
            if (dto.getExpiresInHours() != null) {
                hours = dto.getExpiresInHours();
                if (hours < 1 || hours > 168) {
                    return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "expiresInHours phải trong khoảng 1..168 giờ"));
                }
            }

            // metadata (nếu có) phải là JSON hợp lệ
            String metadata = null;
            if (dto.getMetadata() != null && !dto.getMetadata().trim().isEmpty()) {
                try {
                    new ObjectMapper().readTree(dto.getMetadata());
                    metadata = dto.getMetadata();
                } catch (Exception ex) {
                    return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "metadata không phải JSON hợp lệ"));
                }
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "User not found with id = " + userId));
            }

            LocalDateTime now = LocalDateTime.now();
            Story story = new Story();
            story.setUser(user);
            story.setMediaUrl(dto.getMediaUrl().trim());
            story.setMediaType(mediaType);
            story.setCaption(dto.getCaption() != null ? dto.getCaption().trim() : null);
            if (dto.getIsArchived() != null) {
                story.setIsArchived(dto.getIsArchived());
            }
            story.setMetadata(metadata);
            story.setCreatedAt(now);
            story.setExpiresAt(now.plusHours(hours));
            storyRepository.save(story);

            logger.info("User {} created story {}. IP: {}", userId, story.getId(), ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "CREATE STORY SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in createStory: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public Object getStories(String ownerId, int pageIdx, int pageSize) {
        try {
            if (ownerId == null || ownerId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "ownerId is required"));
            }

            Pageable paging = PageRequest.of(pageIdx, pageSize);
            Page<Story> storyPage = storyRepository.findVisibleByUser(ownerId.trim(), LocalDateTime.now(), paging);
            List<Story> results = storyPage.getContent();

            logger.info("Get stories of {} success. Page: {}, Size: {}", ownerId, pageIdx, pageSize);
            return ResponseHelper.getResponses(results, storyPage.getTotalElements(), storyPage.getTotalPages(), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error in getStories: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> deleteStory(DeleteDto dto, String userId, String ip) {
        try {
            if (dto == null || dto.getId() == null || dto.getId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "storyId is required"));
            }

            Story story = storyRepository.findById(dto.getId().trim()).orElse(null);
            if (story == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Story not found"));
            }
            if (userId == null || story.getUser() == null || !story.getUser().getId().equals(userId)) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.FORBIDDEN, new ResponseMess(1, "Bạn không có quyền xóa story này"));
            }

            storyRepository.delete(story);

            logger.info("User {} deleted story {}. IP: {}", userId, dto.getId(), ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "DELETE STORY SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in deleteStory: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "DELETE FAILED: " + e.getMessage()));
        }
    }
}
