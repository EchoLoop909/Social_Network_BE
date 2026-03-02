package com.example.social_network.Payload.Request;

import lombok.Data;

@Data
public class LikeRequestDto {
    private String postId;
    private String userId;
}