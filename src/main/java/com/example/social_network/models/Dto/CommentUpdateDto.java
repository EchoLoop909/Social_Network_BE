package com.example.social_network.models.Dto;

import lombok.Data;

@Data
public class CommentUpdateDto {
    private String commentId;
    private String userId;
    private String text;
}