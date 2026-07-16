package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Repository.StoryRepository;
import com.example.social_network.Repository.StoryViewRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.StoryViewService;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Dto.UserSummaryDto;
import com.example.social_network.models.Entity.Story;
import com.example.social_network.models.Entity.StoryView;
import com.example.social_network.models.Entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoryViewServiceImpl implements StoryViewService {

    private static final Logger logger = LoggerFactory.getLogger(StoryViewServiceImpl.class);

    @Autowired
    private StoryViewRepository storyViewRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public ResponseEntity<?> recordView(String storyId, String viewerId, String ip) {
        try {
            if (storyId == null || storyId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "storyId is required"));
            }
            if (viewerId == null || viewerId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.UNAUTHORIZED, new ResponseMess(1, "Chưa xác thực người dùng"));
            }

            Story story = storyRepository.findById(storyId.trim()).orElse(null);
            if (story == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Story not found"));
            }
            User user = userRepository.findById(viewerId).orElse(null);
            if (user == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "User not found with id = " + viewerId));
            }

            // Idempotent: mỗi user chỉ ghi 1 lượt / story (PK tổ hợp). Đã có thì bỏ qua.
            StoryView.StoryViewId id = new StoryView.StoryViewId(storyId.trim(), viewerId);
            if (!storyViewRepository.existsById(id)) {
                StoryView sv = new StoryView();
                sv.setId(id);
                sv.setStory(story);
                sv.setUser(user);
                storyViewRepository.save(sv);
                logger.info("User {} viewed story {}. IP: {}", viewerId, storyId, ip);
            }

            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "RECORD STORY VIEW SUCCESS"));
        } catch (Exception e) {
            logger.error("Error in recordView: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public Object getViewers(String storyId) {
        try {
            if (storyId == null || storyId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "storyId is required"));
            }
            List<StoryView> views = storyViewRepository.findViewers(storyId.trim());
            List<UserSummaryDto> result = views.stream()
                    .map(v -> UserSummaryDto.from(v.getUser()))
                    .collect(Collectors.toList());
            logger.info("getViewers of story {} -> {} người xem", storyId, result.size());
            return ResponseHelper.getResponses(result, result.size(), 1, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getViewers: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }
}
