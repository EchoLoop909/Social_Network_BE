// Dựa trên: Social_Network_BE/src/main/java/com/example/social_network/Service/ServiceImpl/CommentServiceImpl.java
// ĐÃ CHUẨN HOÁ: mọi thao tác (READ + CREATE/UPDATE/DELETE) đều bọc qua ResponseHelper
// để FE xử lý đồng nhất 1 cấu trúc response duy nhất (Status/Errors/isOk/isError/Object).
//
// Đây là CHUẨN MỚI áp dụng từ giờ trở đi cho mọi module — khác với code cũ hiện có
// trong repo (code cũ: CREATE/UPDATE/DELETE trả string thô, không bọc envelope).
// Khi refactor các module cũ (JobTitle, Comment bản gốc, ...), áp dụng lại theo mẫu này.

package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Request.CommentRequest;
import com.example.social_network.Repository.CommentRepository;
import com.example.social_network.Repository.PostRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.CommentService;
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
            if (dto.getUserId() == null || dto.getUserId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "userId is required"));
            }
            if (dto.getPostId() == null || dto.getPostId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "postId is required"));
            }
            if (dto.getText() == null || dto.getText().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "Comment text cannot be empty"));
            }
            if (dto.getText().length() > 2000) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "Comment text exceeds maximum length of 2000 characters"));
            }

            User user = userRepository.findById(dto.getUserId()).orElse(null);
            if (user == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "User not found with id = " + dto.getUserId()));
            }

            Post post = postRepository.findById(dto.getPostId()).orElse(null);
            if (post == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Post not found with id = " + dto.getPostId()));
            }

            Comment comment = new Comment();
            comment.setText(dto.getText().trim());
            comment.setUser(user);
            comment.setPost(post);
            commentRepository.save(comment);

            logger.info("User {} commented on Post {} successfully. IP: {}", user.getId(), post.getId(), ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "ADD COMMENT SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in addComment: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public Object getListByPost(String postId, int pageIdx, int pageSize) {
        try {
            if (postId == null || postId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "postId is required"));
            }

            Pageable paging = PageRequest.of(pageIdx, pageSize);
            Page<Comment> commentPage = commentRepository.findByPost_IdOrderByCreateTimeDesc(postId, paging);
            List<Comment> results = commentPage.getContent();

            logger.info("Get list comment for Post {} success. Page: {}, Size: {}", postId, pageIdx, pageSize);
            return ResponseHelper.getResponses(results, commentPage.getTotalElements(), commentPage.getTotalPages(), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error in getListByPost: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> updateComment(CommentUpdateDto dto, String ip) {
        try {
            if (dto.getCommentId() == null || dto.getCommentId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "commentId is required"));
            }
            if (dto.getText() == null || dto.getText().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "Comment text cannot be empty"));
            }

            Comment comment = commentRepository.findById(dto.getCommentId()).orElse(null);
            if (comment == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Comment not found"));
            }

            if (dto.getUserId() != null && !comment.getUser().getId().equals(dto.getUserId())) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.FORBIDDEN, new ResponseMess(1, "You don't have permission to update this comment"));
            }

            comment.setText(dto.getText().trim());
            commentRepository.save(comment);

            logger.info("Comment {} updated successfully. IP: {}", comment.getId(), ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "UPDATE COMMENT SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in updateComment: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "UPDATE FAILED: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> deleteComment(DeleteDto dto, String ip) {
        try {
            if (dto.getId() == null || dto.getId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "commentId is required"));
            }

            Comment comment = commentRepository.findById(dto.getId()).orElse(null);
            if (comment == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Comment not found"));
            }

            commentRepository.delete(comment);

            logger.info("Comment {} deleted successfully. IP: {}", dto.getId(), ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "DELETE COMMENT SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in deleteComment: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "DELETE FAILED: " + e.getMessage()));
        }
    }
}

/*
QUY TẮC BẮT BUỘC khi tạo ServiceImpl mới (CHUẨN MỚI — đồng nhất toàn bộ):
1. @Service + implements <Ten>Service
2. private static final Logger logger = LoggerFactory.getLogger(<Class>.class);
3. @Autowired field injection (KHÔNG dùng constructor injection) cho Repository
4. MỌI method bọc try/catch toàn bộ thân hàm
5. Validate input THỦ CÔNG bằng if/else ở đầu method (KHÔNG dùng @Valid/Bean Validation)
6. Validate tồn tại entity liên quan bằng .findById(...).orElse(null) rồi check null

7. QUY TẮC RESPONSE — BẮT BUỘC ĐỒNG NHẤT, MỌI ENDPOINT (READ lẫn CREATE/UPDATE/DELETE)
   ĐỀU PHẢI BỌC QUA ResponseHelper, không còn ngoại lệ:
   - READ/list -> ResponseHelper.getResponses(results, totalElements, totalPages, status)
   - CREATE/UPDATE/DELETE thành công ->
     ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "<VERB> <NOUN> SUCCESS"))
   - Lỗi validate (bad request) ->
     ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "<mo ta loi>"))
   - Không tìm thấy entity ->
     ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "<Entity> not found..."))
   - Không có quyền ->
     ResponseHelper.getResponseSearchMess(HttpStatus.FORBIDDEN, new ResponseMess(1, "..."))
   - Lỗi hệ thống (catch Exception) ->
     ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()))
   - QUY UOC CODE trong ResponseMess: 0 = thành công, 1 = lỗi (validate/not-found/forbidden/
     system error đều dùng code 1, phân biệt nhau bằng HttpStatus + message, không cần thêm
     mã lỗi chi tiết trừ khi được yêu cầu)
   - TUYỆT ĐỐI KHÔNG trả `ResponseEntity.ok("...")` hay `.body("...")` với string trần nữa —
     mọi response (kể cả lỗi) đều đi qua ResponseHelper để FE luôn nhận đúng 1 cấu trúc
     { Status, Errors, isOk, isError, Object, (totalElements/totalPage nếu có) }
8. logger.info/logger.error vẫn dùng slf4j placeholder {} như cũ, không đổi

LƯU Ý: Đây là chuẩn MỚI, khác với code cũ đang có sẵn trong repo (module Comment gốc,
JobTitle, v.v. — các module đó CREATE/UPDATE/DELETE đang trả string thô không bọc).
Khi tạo module MỚI, luôn follow chuẩn đồng nhất này. Khi được yêu cầu sửa/refactor module
CŨ, áp dụng lại theo chuẩn này trừ khi người dùng nói giữ nguyên.
*/
