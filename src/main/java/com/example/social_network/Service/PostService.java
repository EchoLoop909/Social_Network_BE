package com.example.social_network.Service;

import com.example.social_network.models.Dto.DeleteDto;
import com.example.social_network.models.Dto.Posts.PostInsertDto;
import com.example.social_network.models.Dto.Posts.PostUpdateDto;
import org.springframework.http.ResponseEntity;

public interface PostService {
    Object getList(String id,String userId, String postId , int pageIdx, int pageSize);

    ResponseEntity<?> insert(PostInsertDto dto, String ip);

    ResponseEntity<?> update(PostUpdateDto dto, String ip);

    ResponseEntity<?> delete(DeleteDto dto, String ip);
}
