package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Request.CommentRequest;
import com.example.social_network.Repository.CommentRepository;
import com.example.social_network.Repository.PostRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.CommentService;
import com.example.social_network.models.Dto.CommentUpdateDto;
import com.example.social_network.models.Dto.DeleteDto;
import com.example.social_network.models.Entity.Comment;
import com.example.social_network.models.Entity.Post;
import com.example.social_network.models.Entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
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

    @Override
    public ResponseEntity<?> addComment(CommentRequest dto, String ip) {
        try {
            // 1. Validate ID đầu vào
            if (dto.getUserId() == null || dto.getUserId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("userId is required");
            }
            if (dto.getPostId() == null || dto.getPostId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("postId is required");
            }

            // 2. Validate Text KHÔNG ĐƯỢC ĐỂ TRỐNG
            if (dto.getText() == null || dto.getText().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Comment text cannot be empty");
            }

            // Validate độ dài text (Entity khai báo length = 2000)
            if (dto.getText().length() > 2000) {
                return ResponseEntity.badRequest().body("Comment text exceeds maximum length of 2000 characters");
            }

            // 3. Kiểm tra User có tồn tại không
            User user = userRepository.findById(dto.getUserId()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found with id = " + dto.getUserId());
            }

            // 4. Kiểm tra Post có tồn tại không
            Post post = postRepository.findById(dto.getPostId()).orElse(null);
            if (post == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Post not found with id = " + dto.getPostId());
            }

            // 5. Khởi tạo Comment và lưu vào DB
            Comment comment = new Comment();
            comment.setText(dto.getText().trim());
            comment.setUser(user);
            comment.setPost(post);

            commentRepository.save(comment);

            logger.info("User {} commented on Post {} successfully. IP: {}", user.getId(), post.getId(), ip);
            return ResponseEntity.ok("ADD COMMENT SUCCESS");

        } catch (Exception e) {
            logger.error("Error in addComment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("SYSTEM ERROR: " + e.getMessage());
        }
    }

    @Override
    public Object getListByPost(String postId, int pageIdx, int pageSize) {
        try {
            if (postId == null || postId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("postId is required");
            }

            Pageable paging = PageRequest.of(pageIdx, pageSize);
            Page<Comment> commentPage = commentRepository.findByPost_IdOrderByCreateTimeDesc(postId, paging);
            List<Comment> results = commentPage.getContent();

            logger.info("Get list comment for Post {} success. Page: {}, Size: {}", postId, pageIdx, pageSize);

            // Dùng hàm ResponseHelper có sẵn trong project của bạn
            return ResponseHelper.getResponses(results, commentPage.getTotalElements(), commentPage.getTotalPages(), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error in getListByPost: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("SYSTEM ERROR: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> updateComment(CommentUpdateDto dto, String ip) {
        try {
            if (dto.getCommentId() == null || dto.getCommentId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("commentId is required");
            }
            if (dto.getText() == null || dto.getText().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Comment text cannot be empty");
            }

            Comment comment = commentRepository.findById(dto.getCommentId()).orElse(null);
            if (comment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
            }

            // (Tùy chọn) Kiểm tra quyền: Phải là người tạo comment mới được phép sửa
            if (dto.getUserId() != null && !comment.getUser().getId().equals(dto.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You don't have permission to update this comment");
            }

            // Cập nhật nội dung
            comment.setText(dto.getText().trim());
            commentRepository.save(comment);

            logger.info("Comment {} updated successfully. IP: {}", comment.getId(), ip);
            return ResponseEntity.ok("UPDATE COMMENT SUCCESS");

        } catch (Exception e) {
            logger.error("Error in updateComment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("UPDATE FAILED: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> deleteComment(DeleteDto dto, String ip) {
        try {
            if (dto.getId() == null || dto.getId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("commentId is required");
            }

            Comment comment = commentRepository.findById(dto.getId()).orElse(null);
            if (comment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
            }

            // Xóa comment
            commentRepository.delete(comment);

            logger.info("Comment {} deleted successfully. IP: {}", dto.getId(), ip);
            return ResponseEntity.ok("DELETE COMMENT SUCCESS");

        } catch (Exception e) {
            logger.error("Error in deleteComment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("DELETE FAILED: " + e.getMessage());
        }
    }

}