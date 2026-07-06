package com.example.social_network.Service;

import org.springframework.http.ResponseEntity;

public interface UserService {
    Object getUser(String userId, int pageIdx, int pageSize);

    ResponseEntity<?> uploadImg(String username, String url);
}
