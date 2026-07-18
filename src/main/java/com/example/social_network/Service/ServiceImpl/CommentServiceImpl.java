package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Request.CommentRequest;
import com.example.social_network.Repository.CommentRepository;
import com.example.social_network.Repository.PostRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.CommentService;
import com.example.social_network.Service.NotificationService;
import com.example.social_network.models.Dto.CommentRootResponse;
import com.example.social_network.models.Dto.NotificationEvent;
import com.example.social_network.models.Enum.NotificationType;
import com.example.social_network.models.Dto.CommentUpdateDto;
import com.example.social_network.models.Dto.DeleteDto;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Entity.Comment;
import com.example.social_network.models.Entity.Post;
import com.example.social_network.models.Entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private NotificationService notificationService;

    // ================= API 1: TẠO COMMENT (gốc hoặc reply) =================
    @Override
    public ResponseEntity<?> addComment(CommentRequest dto, String userId, String ip) {
        try {
            if (dto.getPostId() == null || dto.getPostId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "postId is required"));
            }
            if (dto.getText() == null || dto.getText().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "Comment text cannot be empty"));
            }
            if (dto.getText().length() > 2000) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "Comment text exceeds maximum length of 2000 characters"));
            }

            // Tác giả lấy từ JWT subject, KHÔNG nhận từ request
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "User not found with id = " + userId));
            }

            Post post = postRepository.findById(dto.getPostId().trim()).orElse(null);
            if (post == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Post not found with id = " + dto.getPostId()));
            }

            // Nếu là reply: validate comment cha
            Comment parent = null;
            if (dto.getParentCommentId() != null && !dto.getParentCommentId().trim().isEmpty()) {
                parent = commentRepository.findById(dto.getParentCommentId().trim()).orElse(null);
                if (parent == null) {
                    return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Parent comment not found with id = " + dto.getParentCommentId()));
                }
                // Comment cha phải thuộc CÙNG bài viết (chặn reply nhầm sang bài khác)
                if (!parent.getPost().getId().equals(post.getId())) {
                    return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "Parent comment does not belong to the given post"));
                }
                // Comment cha phải là comment GỐC — không cho reply-của-reply (giới hạn 2 cấp)
                if (parent.getParentComment() != null) {
                    return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "Cannot reply to a reply — only one level of nesting is allowed"));
                }
            }

            Comment comment = new Comment();
            comment.setText(dto.getText().trim());
            comment.setUser(user);
            comment.setPost(post);
            comment.setParentComment(parent); // null = comment gốc
            commentRepository.save(comment);

            logger.info("User {} added comment {} on Post {} (parent={}). IP: {}",
                    userId, comment.getId(), post.getId(), parent != null ? parent.getId() : "null", ip);

            // Thông báo: reply -> báo chủ comment gốc (REPLY); comment gốc -> báo chủ bài viết (COMMENT).
            // Bọc try/catch riêng để lỗi noti không ảnh hưởng việc tạo comment. targetId = postId để điều hướng.
            try {
                if (parent != null) {
                    String parentOwner = parent.getUser() != null ? parent.getUser().getId() : null;
                    if (parentOwner != null && !parentOwner.equals(userId)) {
                        notificationService.publish(new NotificationEvent(
                                parentOwner, userId, NotificationType.REPLY.name(), post.getId(), comment.getText()));
                    }
                } else {
                    String postOwner = post.getUser() != null ? post.getUser().getId() : null;
                    if (postOwner != null && !postOwner.equals(userId)) {
                        notificationService.publish(new NotificationEvent(
                                postOwner, userId, NotificationType.COMMENT.name(), post.getId(), comment.getText()));
                    }
                }
            } catch (Exception ex) {
                logger.error("Lỗi tạo notification cho comment {}: {}", comment.getId(), ex.getMessage());
            }

            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "ADD COMMENT SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in addComment: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    // ================= API 2: LIST COMMENT GỐC theo bài viết =================
    @Override
    public Object getRootComments(String postId, int pageIdx, int pageSize) {
        try {
            if (postId == null || postId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "postId is required"));
            }

            Pageable paging = PageRequest.of(pageIdx, pageSize);
            Page<Comment> page = commentRepository.findByPost_IdAndParentCommentIsNullOrderByCreateTimeDesc(postId.trim(), paging);

            // Đếm replyCount cho từng comment gốc để FE hiển thị "Xem N phản hồi"
            List<CommentRootResponse> results = new ArrayList<>();
            for (Comment c : page.getContent()) {
                long replyCount = commentRepository.countByParentComment_Id(c.getId());
                results.add(new CommentRootResponse(c, replyCount));
            }

            logger.info("Get root comments for Post {} success. Page: {}, Size: {}", postId, pageIdx, pageSize);
            return ResponseHelper.getResponses(results, page.getTotalElements(), page.getTotalPages(), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error in getRootComments: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    // ================= API 3: LIST REPLY theo 1 comment gốc =================
    @Override
    public Object getReplies(String parentCommentId, int pageIdx, int pageSize) {
        try {
            if (parentCommentId == null || parentCommentId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "parentCommentId is required"));
            }

            Pageable paging = PageRequest.of(pageIdx, pageSize);
            Page<Comment> page = commentRepository.findByParentComment_IdOrderByCreateTimeAsc(parentCommentId.trim(), paging);
            List<Comment> results = page.getContent();

            logger.info("Get replies for Comment {} success. Page: {}, Size: {}", parentCommentId, pageIdx, pageSize);
            return ResponseHelper.getResponses(results, page.getTotalElements(), page.getTotalPages(), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error in getReplies: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    // ================= API 4: UPDATE COMMENT =================
    @Override
    public ResponseEntity<?> updateComment(CommentUpdateDto dto, String userId, String ip) {
        try {
            if (dto.getCommentId() == null || dto.getCommentId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "commentId is required"));
            }
            if (dto.getText() == null || dto.getText().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "Comment text cannot be empty"));
            }
            if (dto.getText().length() > 2000) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "Comment text exceeds maximum length of 2000 characters"));
            }

            Comment comment = commentRepository.findById(dto.getCommentId().trim()).orElse(null);
            if (comment == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Comment not found"));
            }

            // Chỉ người tạo comment mới được sửa (so với JWT subject)
            if (!comment.getUser().getId().equals(userId)) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.FORBIDDEN, new ResponseMess(1, "You don't have permission to update this comment"));
            }

            // KHÔNG cho đổi id_post / id_parent_comment sau khi tạo — chỉ cập nhật text
            comment.setText(dto.getText().trim());
            commentRepository.save(comment);

            logger.info("Comment {} updated by user {}. IP: {}", comment.getId(), userId, ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "UPDATE COMMENT SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in updateComment: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "UPDATE FAILED: " + e.getMessage()));
        }
    }

    // ================= API 5: DELETE COMMENT =================
    @Override
    public ResponseEntity<?> deleteComment(DeleteDto dto, String userId, String ip) {
        try {
            if (dto.getId() == null || dto.getId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "commentId is required"));
            }

            Comment comment = commentRepository.findById(dto.getId().trim()).orElse(null);
            if (comment == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Comment not found"));
            }

            // Chỉ người tạo comment mới được xoá (TODO: bổ sung quyền admin sau nếu cần)
            if (!comment.getUser().getId().equals(userId)) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.FORBIDDEN, new ResponseMess(1, "You don't have permission to delete this comment"));
            }

            // Xoá comment gốc sẽ tự cascade xoá toàn bộ reply con nhờ mapping
            // @OneToMany(cascade = ALL, orphanRemoval = true) — KHÔNG cần tự lặp xoá.
            commentRepository.delete(comment);

            logger.info("Comment {} (and its replies if any) deleted by user {}. IP: {}", dto.getId(), userId, ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "DELETE COMMENT SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in deleteComment: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "DELETE FAILED: " + e.getMessage()));
        }
    }
}
