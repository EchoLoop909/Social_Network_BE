package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.CommentRequest;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Service.CommentService;
import com.example.social_network.models.Dto.CommentUpdateDto;
import com.example.social_network.models.Dto.DeleteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(PathResources.COMMENT)
public class CommentController {

    @Autowired
    private CommentService commentService;

    // API 1: tạo comment (gốc hoặc reply). id_user lấy từ JWT subject.
    @PostMapping(PathResources.INSERT)
    public ResponseEntity<?> addComment(@RequestBody CommentRequest dto,
                                        @AuthenticationPrincipal Jwt jwt,
                                        HttpServletRequest request) {
        return commentService.addComment(dto, jwt.getSubject(), request.getRemoteAddr());
    }

    // API 2: danh sách comment GỐC theo bài viết (phân trang) + replyCount.
    @GetMapping(PathResources.LIST)
    public Object getRootComments(@RequestParam String postId,
                                  @RequestParam(defaultValue = "1") int pageIdx,
                                  @RequestParam(defaultValue = "10") int pageSize) {
        return commentService.getRootComments(postId, pageIdx - 1, pageSize);
    }

    // API 3: danh sách reply theo 1 comment gốc (phân trang).
    @GetMapping(PathResources.REPLIES)
    public Object getReplies(@RequestParam String parentCommentId,
                             @RequestParam(defaultValue = "1") int pageIdx,
                             @RequestParam(defaultValue = "10") int pageSize) {
        return commentService.getReplies(parentCommentId, pageIdx - 1, pageSize);
    }

    // API 4: sửa comment — chỉ chủ sở hữu (JWT subject).
    @PutMapping(PathResources.UPDATE)
    public ResponseEntity<?> updateComment(@RequestBody CommentUpdateDto dto,
                                           @AuthenticationPrincipal Jwt jwt,
                                           HttpServletRequest request) {
        return commentService.updateComment(dto, jwt.getSubject(), request.getRemoteAddr());
    }

    // API 5: xoá comment — chỉ chủ sở hữu; xoá gốc cascade toàn bộ reply.
    @DeleteMapping(PathResources.DELETE)
    public ResponseEntity<?> deleteComment(@RequestBody DeleteDto dto,
                                           @AuthenticationPrincipal Jwt jwt,
                                           HttpServletRequest request) {
        return commentService.deleteComment(dto, jwt.getSubject(), request.getRemoteAddr());
    }
}
