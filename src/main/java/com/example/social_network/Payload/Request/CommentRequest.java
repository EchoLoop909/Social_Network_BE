package com.example.social_network.Payload.Request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CommentRequest {

    private String text;

    private String userId;

    private String postId;
}