package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Request.PostUserTagRequest;
import com.example.social_network.Repository.PostRepository;
import com.example.social_network.Repository.PostUserTagRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.NotificationService;
import com.example.social_network.Service.PostUserTagService;
import com.example.social_network.models.Dto.NotificationEvent;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Dto.UserSummaryDto;
import com.example.social_network.models.Entity.Post;
import com.example.social_network.models.Entity.PostUserTag;
import com.example.social_network.models.Entity.User;
import com.example.social_network.models.Enum.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostUserTagServiceImpl implements PostUserTagService {
    private static final Logger logger = LoggerFactory.getLogger(PostUserTagServiceImpl.class);

    @Autowired
    private PostUserTagRepository postUserTagRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public ResponseEntity<?> addTag(PostUserTagRequest dto, String taggerId, String ip) {
        try {
            if (dto == null || dto.getPostId() == null || dto.getPostId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "postId is required"));
            }
            if (dto.getUserId() == null || dto.getUserId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "userId is required"));
            }
            Post post = postRepository.findById(dto.getPostId().trim()).orElse(null);
            if (post == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Post not found"));
            }
            User tagged = userRepository.findById(dto.getUserId().trim()).orElse(null);
            if (tagged == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "User not found"));
            }

            PostUserTag.PostUserTagId id = new PostUserTag.PostUserTagId(post.getId(), tagged.getId());
            boolean isNew = !postUserTagRepository.existsById(id);
            if (isNew) {
                postUserTagRepository.save(new PostUserTag(id, post, tagged));
            }

            // Thông báo cho người được gắn thẻ (bỏ qua nếu tự gắn mình). targetId = postId để FE mở bài.
            if (isNew && !tagged.getId().equals(taggerId)) {
                try {
                    notificationService.publish(new NotificationEvent(
                            tagged.getId(), taggerId, NotificationType.TAG.name(), post.getId(),
                            "đã nhắc đến bạn trong một bài viết"));
                } catch (Exception ex) {
                    logger.error("Lỗi tạo notification TAG cho {}: {}", tagged.getId(), ex.getMessage());
                }
            }

            logger.info("User {} tagged {} in post {}. IP: {}", taggerId, tagged.getId(), post.getId(), ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "TAG USER SUCCESS"));
        } catch (Exception e) {
            logger.error("Error in addTag: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public Object getByPost(String postId) {
        try {
            if (postId == null || postId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "postId is required"));
            }
            List<UserSummaryDto> result = postUserTagRepository.findByPost_Id(postId.trim())
                    .stream().map(t -> UserSummaryDto.from(t.getUser())).collect(Collectors.toList());
            logger.info("getByPost user-tags of {} -> {}", postId, result.size());
            return ResponseHelper.getResponses(result, result.size(), 1, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getByPost user-tag: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public Object getTaggedPosts(String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "userId is required"));
            }
            List<com.example.social_network.models.Entity.Post> posts = postUserTagRepository.findTaggedPosts(userId.trim());
            logger.info("getTaggedPosts of {} -> {}", userId, posts.size());
            return ResponseHelper.getResponses(posts, posts.size(), 1, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getTaggedPosts: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> removeTag(PostUserTagRequest dto, String ip) {
        try {
            if (dto == null || dto.getPostId() == null || dto.getUserId() == null
                    || dto.getPostId().trim().isEmpty() || dto.getUserId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "postId & userId are required"));
            }
            PostUserTag.PostUserTagId id = new PostUserTag.PostUserTagId(dto.getPostId().trim(), dto.getUserId().trim());
            PostUserTag tag = postUserTagRepository.findById(id).orElse(null);
            if (tag == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Thẻ không tồn tại"));
            }
            postUserTagRepository.delete(tag);
            logger.info("Removed tag user {} from post {}. IP: {}", dto.getUserId(), dto.getPostId(), ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "REMOVE TAG SUCCESS"));
        } catch (Exception e) {
            logger.error("Error in removeTag: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "DELETE FAILED: " + e.getMessage()));
        }
    }
}
