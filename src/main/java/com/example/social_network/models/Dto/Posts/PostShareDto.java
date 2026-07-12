package com.example.social_network.models.Dto.Posts;

import lombok.Data;

@Data
public class PostShareDto {
    // id bài viết muốn share — bắt buộc
    private String originalPostId;
    // Caption riêng của người share — optional (có thể null/rỗng)
    private String text;

    // id_user KHÔNG nhận từ request — lấy từ JWT subject ở tầng Controller/Service
}
