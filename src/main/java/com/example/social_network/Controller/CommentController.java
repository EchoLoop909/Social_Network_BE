package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.CommentRequest;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Service.CommentService;
import com.example.social_network.models.Dto.CommentUpdateDto;
import com.example.social_network.models.Dto.DeleteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.PathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(PathResources.COMMENT)
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping(PathResources.INSERT)
    public ResponseEntity<?> addComment(@RequestBody CommentRequest dto,
                                        HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return commentService.addComment(dto, ip);
    }

    // READ
    @GetMapping(PathResources.GETCOMMENT)
    public Object getCommentsByPost(
            @RequestParam String postId,
            @RequestParam(defaultValue = "1") int pageIdx,
            @RequestParam(defaultValue = "10") int pageSize) {
        return commentService.getListByPost(postId, pageIdx - 1, pageSize);
    }

    @PutMapping(PathResources.UPDATE)
    public ResponseEntity<?> updateComment(@RequestBody CommentUpdateDto dto, HttpServletRequest request) {
        return commentService.updateComment(dto, request.getRemoteAddr());
    }

    @DeleteMapping(PathResources.DELETE)
    public ResponseEntity<?> deleteComment(@RequestBody DeleteDto dto, HttpServletRequest request) {
        return commentService.deleteComment(dto, request.getRemoteAddr());
    }
}