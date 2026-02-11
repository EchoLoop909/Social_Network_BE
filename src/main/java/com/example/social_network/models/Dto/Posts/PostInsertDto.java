package com.example.social_network.models.Dto.Posts;

import lombok.Data;

@Data
public class PostInsertDto {
    private String text;
    private String photo;
    private String userId; // id_user
}