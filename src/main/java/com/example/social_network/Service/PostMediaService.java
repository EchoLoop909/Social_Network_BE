package com.example.social_network.Service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface PostMediaService {

    /**
     * Upload 1 list ảnh/video lên Cloudinary (KHÔNG lưu DB).
     * Thứ tự phần tử trong mảng files = thứ tự hiển thị (order suy ra từ index).
     * Trả về list { url, type, width, height, durationSec, order } cho FE.
     */
    ResponseEntity<?> upload(MultipartFile[] files);
}
