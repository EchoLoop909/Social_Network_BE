package com.example.social_network.Service;

import org.springframework.http.ResponseEntity;

public interface PostService {
    Object getList(String id,String userId, String postId , int pageIdx, int pageSize);
}
