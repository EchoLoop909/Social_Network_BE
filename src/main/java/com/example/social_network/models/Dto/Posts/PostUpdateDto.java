package com.example.social_network.models.Dto.Posts;

import lombok.Data;

@Data
public class PostUpdateDto {
    private String id;
    private String text;
    private String photo;
    private Boolean isPinned;
    private String visibility;
}