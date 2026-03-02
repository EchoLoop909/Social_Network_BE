package com.example.social_network.Service;

import com.example.social_network.Payload.Request.CommentRequest;
import com.example.social_network.models.Dto.CommentUpdateDto;
import com.example.social_network.models.Dto.DeleteDto;
import org.springframework.http.ResponseEntity;

public interface CommentService {
    ResponseEntity<?> addComment(CommentRequest dto, String ip);

    Object getListByPost(String postId, int pageIdx, int pageSize);

    ResponseEntity<?> updateComment(CommentUpdateDto dto, String ip);

    ResponseEntity<?> deleteComment(DeleteDto dto, String ip);
}
