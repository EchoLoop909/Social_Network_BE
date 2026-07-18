package com.example.social_network.Service;

import com.example.social_network.Payload.Request.HashtagRequest;
import com.example.social_network.models.Dto.DeleteDto;
import org.springframework.http.ResponseEntity;

public interface HashtagService {
    ResponseEntity<?> create(HashtagRequest dto, String ip);

    Object getList(String keyword, int pageIdx, int pageSize);

    ResponseEntity<?> delete(DeleteDto dto, String ip);
}
