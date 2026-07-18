package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Request.LikeRequestDto;
import com.example.social_network.Repository.CommentRepository;
import com.example.social_network.Repository.LikeRepository;
import com.example.social_network.Repository.PostRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.LikeService;
import com.example.social_network.Service.NotificationService;
import com.example.social_network.models.Dto.NotificationEvent;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Enum.NotificationType;
import com.example.social_network.models.Entity.Comment;
import com.example.social_network.models.Entity.Like;
import com.example.social_network.models.Entity.Post;
import com.example.social_network.models.Entity.User;
import com.example.social_network.models.Enum.LikeTargetType;
import com.example.social_network.models.Enum.ReactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LikeServiceImpl implements LikeService {
    private static final Logger logger = LoggerFactory.getLogger(LikeServiceImpl.class);

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NotificationService notificationService;

    // ================= API 1: REACT (tạo mới hoặc đổi loại cảm xúc) =================
    // @Transactional: có 2 thao tác ghi (likes + reaction_count) -> catch phải throw
    // lại RuntimeException để Spring rollback đúng cả 2 (ngoại lệ cho method transaction).
    @Override
    @Transactional
    public ResponseEntity<?> react(LikeRequestDto dto, String userId, String ip) {
        try {
            if (dto.getTargetType() == null || dto.getTargetType().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "targetType is required"));
            }
            if (dto.getTargetId() == null || dto.getTargetId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "targetId is required"));
            }
            if (dto.getReactionType() == null || dto.getReactionType().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "reactionType is required"));
            }

            LikeTargetType targetType;
            try {
                targetType = LikeTargetType.valueOf(dto.getTargetType().trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "targetType không hợp lệ: " + dto.getTargetType()));
            }
            ReactionType reactionType;
            try {
                reactionType = ReactionType.valueOf(dto.getReactionType().trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "reactionType không hợp lệ: " + dto.getReactionType()));
            }
            String targetId = dto.getTargetId().trim();

            // User lấy từ JWT subject
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "User not found with id = " + userId));
            }

            // Đối tượng phải tồn tại thật (dùng đúng repository theo targetType)
            if (!targetExists(targetType, targetId)) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, targetType + " not found with id = " + targetId));
            }

            Optional<Like> existingOpt = likeRepository.findByUser_IdAndTargetTypeAndTargetId(userId, targetType, targetId);

            if (!existingOpt.isPresent()) {
                // Chưa react -> tạo mới + tăng reaction_count
                Like like = new Like();
                like.setUser(user);
                like.setTargetType(targetType);
                like.setTargetId(targetId);
                like.setReactionType(reactionType);
                likeRepository.save(like);
                adjustReactionCount(targetType, targetId, +1);
                logger.info("User {} reacted {} on {} {}. IP: {}", userId, reactionType, targetType, targetId, ip);

                // Thông báo cho chủ đối tượng (bài viết/bình luận) — chỉ khi react LẦN ĐẦU, bỏ qua nếu tự react.
                // Bọc try/catch riêng: lỗi noti KHÔNG được làm rollback lượt react.
                try {
                    String ownerId = resolveOwnerId(targetType, targetId);
                    if (ownerId != null && !ownerId.equals(userId)) {
                        notificationService.publish(new NotificationEvent(
                                ownerId, userId, NotificationType.LIKE.name(), targetId, null));
                    }
                } catch (Exception ex) {
                    logger.error("Lỗi tạo notification LIKE cho {} {}: {}", targetType, targetId, ex.getMessage());
                }
            } else {
                Like existing = existingOpt.get();
                if (existing.getReactionType() != reactionType) {
                    // Đã react nhưng đổi loại -> chỉ update reaction_type, KHÔNG đổi reaction_count
                    existing.setReactionType(reactionType);
                    likeRepository.save(existing);
                    logger.info("User {} changed reaction to {} on {} {}. IP: {}", userId, reactionType, targetType, targetId, ip);
                } else {
                    // Trùng loại cảm xúc cũ -> idempotent, không làm gì
                    logger.info("User {} already reacted {} on {} {} (idempotent). IP: {}", userId, reactionType, targetType, targetId, ip);
                }
            }

            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "REACT SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in react: {}", e.getMessage());
            // BẮT BUỘC throw lại để @Transactional rollback cả likes lẫn reaction_count
            throw new RuntimeException("REACT FAILED: " + e.getMessage(), e);
        }
    }

    // ================= API 2: UNLIKE (xoá cảm xúc) =================
    @Override
    @Transactional
    public ResponseEntity<?> unlike(LikeRequestDto dto, String userId, String ip) {
        try {
            if (dto.getTargetType() == null || dto.getTargetType().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "targetType is required"));
            }
            if (dto.getTargetId() == null || dto.getTargetId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "targetId is required"));
            }
            LikeTargetType targetType;
            try {
                targetType = LikeTargetType.valueOf(dto.getTargetType().trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "targetType không hợp lệ: " + dto.getTargetType()));
            }
            String targetId = dto.getTargetId().trim();

            Optional<Like> existingOpt = likeRepository.findByUser_IdAndTargetTypeAndTargetId(userId, targetType, targetId);
            if (!existingOpt.isPresent()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "You haven't reacted to this yet"));
            }

            likeRepository.delete(existingOpt.get());
            // Giảm reaction_count đi 1, chặn không cho âm (đã xử lý trong adjustReactionCount)
            adjustReactionCount(targetType, targetId, -1);

            logger.info("User {} unliked {} {}. IP: {}", userId, targetType, targetId, ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "UNLIKE SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in unlike: {}", e.getMessage());
            throw new RuntimeException("UNLIKE FAILED: " + e.getMessage(), e);
        }
    }

    // ================= API 3: LIST người đã react + thống kê theo loại =================
    @Override
    public Object getList(String targetTypeStr, String targetId, int pageIdx, int pageSize) {
        try {
            if (targetTypeStr == null || targetTypeStr.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "targetType is required"));
            }
            if (targetId == null || targetId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "targetId is required"));
            }
            LikeTargetType targetType;
            try {
                targetType = LikeTargetType.valueOf(targetTypeStr.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "targetType không hợp lệ: " + targetTypeStr));
            }
            String tid = targetId.trim();

            Pageable paging = PageRequest.of(pageIdx, pageSize);
            Page<Like> page = likeRepository.findByTargetTypeAndTargetIdOrderByCreateTimeDesc(targetType, tid, paging);

            // Thống kê số lượng theo từng loại cảm xúc (👍 12 ❤️ 5 ...)
            Map<String, Long> reactionSummary = new LinkedHashMap<>();
            for (Object[] row : likeRepository.countByReactionType(targetType.name(), tid)) {
                reactionSummary.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
            }

            Map<String, Object> data = new HashMap<>();
            data.put("likes", page.getContent());
            data.put("reactionSummary", reactionSummary);

            logger.info("Get like list for {} {} success. Page: {}, Size: {}", targetType, tid, pageIdx, pageSize);
            return ResponseHelper.getResponses(data, page.getTotalElements(), page.getTotalPages(), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error in getList like: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    // ================= API 4: cảm xúc hiện tại của user cho 1 đối tượng =================
    @Override
    public Object getMyReaction(String targetTypeStr, String targetId, String userId) {
        try {
            if (targetTypeStr == null || targetTypeStr.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "targetType is required"));
            }
            if (targetId == null || targetId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "targetId is required"));
            }
            LikeTargetType targetType;
            try {
                targetType = LikeTargetType.valueOf(targetTypeStr.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "targetType không hợp lệ: " + targetTypeStr));
            }

            Optional<Like> existingOpt = likeRepository.findByUser_IdAndTargetTypeAndTargetId(userId, targetType, targetId.trim());

            // reactionType = null nếu user chưa react -> FE không tô sáng icon nào
            Map<String, Object> data = new HashMap<>();
            data.put("reactionType", existingOpt.map(l -> l.getReactionType().name()).orElse(null));

            logger.info("Get my reaction for {} {} by user {} success", targetType, targetId, userId);
            return ResponseHelper.buildResponse(data, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error in getMyReaction: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    // ---- Helpers dùng chung: chọn đúng repository theo targetType (POST vs COMMENT) ----

    private boolean targetExists(LikeTargetType targetType, String targetId) {
        if (targetType == LikeTargetType.POST) {
            return postRepository.existsById(targetId);
        }
        return commentRepository.existsById(targetId);
    }

    /** Lấy id chủ sở hữu của đối tượng (tác giả bài viết hoặc người viết bình luận) để gửi thông báo. */
    private String resolveOwnerId(LikeTargetType targetType, String targetId) {
        if (targetType == LikeTargetType.POST) {
            Post p = postRepository.findById(targetId).orElse(null);
            return (p != null && p.getUser() != null) ? p.getUser().getId() : null;
        }
        Comment c = commentRepository.findById(targetId).orElse(null);
        return (c != null && c.getUser() != null) ? c.getUser().getId() : null;
    }

    /**
     * Cập nhật reaction_count denormalized của Post hoặc Comment tuỳ targetType.
     * delta = +1 khi react mới, -1 khi unlike. Chặn không cho count xuống dưới 0.
     */
    private void adjustReactionCount(LikeTargetType targetType, String targetId, int delta) {
        if (targetType == LikeTargetType.POST) {
            Post post = postRepository.findById(targetId).orElse(null);
            if (post != null) {
                int cur = post.getReactionCount() == null ? 0 : post.getReactionCount();
                post.setReactionCount(Math.max(0, cur + delta));
                postRepository.save(post);
            }
        } else { // COMMENT
            Comment comment = commentRepository.findById(targetId).orElse(null);
            if (comment != null) {
                int cur = comment.getReactionCount() == null ? 0 : comment.getReactionCount();
                comment.setReactionCount(Math.max(0, cur + delta));
                commentRepository.save(comment);
            }
        }
    }
}
