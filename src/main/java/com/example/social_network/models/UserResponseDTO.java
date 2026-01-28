package com.example.social_network.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO{
    private Long id;

    private String email;
    private String username;
    private String  bio;
    private String avatarUrl;
}
