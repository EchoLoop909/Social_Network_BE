package com.example.social_network.Service;

import com.example.social_network.Payload.Request.CommentRequest;
import com.example.social_network.models.Dto.CommentUpdateDto;
import com.example.social_network.models.Dto.DeleteDto;
import org.springframework.http.ResponseEntity;

public interface CommentService {

    // API 1: tạo comment (gốc hoặc reply). userId lấy từ JWT subject.
    ResponseEntity<?> addComment(CommentRequest dto, String userId, String ip);

    // API 2: danh sách comment GỐC theo bài viết (phân trang) + replyCount.
    Object getRootComments(String postId, int pageIdx, int pageSize);

    // API 3: danh sách reply theo 1 comment gốc (phân trang).
    Object getReplies(String parentCommentId, int pageIdx, int pageSize);

    // API 4: sửa comment — chỉ chủ sở hữu (userId từ JWT).
    ResponseEntity<?> updateComment(CommentUpdateDto dto, String userId, String ip);

    // API 5: xoá comment — chỉ chủ sở hữu (userId từ JWT); xoá gốc cascade toàn bộ reply.
    ResponseEntity<?> deleteComment(DeleteDto dto, String userId, String ip);
}
