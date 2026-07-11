package com.example.social_network.models.Dto;

import lombok.Data;

@Data
public class CommentUpdateDto {
    private String commentId;
    private String text;

    // userId KHÔNG nhận từ request — quyền sửa kiểm tra bằng JWT subject ở Service
}
