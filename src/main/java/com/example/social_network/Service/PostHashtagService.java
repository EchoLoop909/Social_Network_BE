package com.example.social_network.Service;

import com.example.social_network.Payload.Request.PostHashtagRequest;
import org.springframework.http.ResponseEntity;

public interface PostHashtagService {
    ResponseEntity<?> attach(PostHashtagRequest dto, String ip);

    Object getByPost(String postId);

    ResponseEntity<?> detach(PostHashtagRequest dto, String ip);
}
