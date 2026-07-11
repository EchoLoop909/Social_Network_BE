package com.example.social_network.models.Dto.Posts;

import lombok.Data;

import java.util.List;

@Data
public class PostInsertDto {
    // Nội dung chữ — bắt buộc nếu media rỗng
    private String text;
    // PUBLIC / FOLLOWERS / PRIVATE — bắt buộc
    private String visibility;
    // Danh sách media theo đúng định dạng response của API upload (có thể rỗng / tập con)
    private List<MediaItem> media;

    // id_user KHÔNG nhận từ request — lấy từ JWT subject ở tầng Controller/Service

    @Data
    public static class MediaItem {
        private String url;          // URL Cloudinary
        private String type;         // IMAGE | VIDEO
        private Integer width;       // px, có thể null
        private Integer height;      // px, có thể null
        private Integer durationSec; // giây, null nếu là ảnh
        private Integer order;       // thứ tự hiển thị (index 0-based từ mảng upload)
    }
}
