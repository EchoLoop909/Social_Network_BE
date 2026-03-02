package com.example.social_network.Service;

import com.example.social_network.Payload.Request.LikeRequestDto;
import org.springframework.http.ResponseEntity;

public interface LikeService {
    ResponseEntity<?> toggleLike(LikeRequestDto dto, String ip);
}
