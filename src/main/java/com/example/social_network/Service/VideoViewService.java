package com.example.social_network.Service;

import com.example.social_network.models.Dto.VideoViewRequest;
import org.springframework.http.ResponseEntity;

public interface VideoViewService {

    /** Ghi nhận / cập nhật tiến độ xem video của user (giữ số giây xem xa nhất). */
    ResponseEntity<?> recordView(VideoViewRequest req, String userId);
}
