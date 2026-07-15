package com.example.social_network.models.Dto;

import lombok.Data;

/**
 * DTO cập nhật hồ sơ cá nhân. User được xác định qua token (không nhận id từ body).
 * Chỉ những field khác null mới được cập nhật (partial update).
 */
@Data
public class UserUpdateDto {
    private String name;
    private String firstname;
    private String lastname;
    private String surname;
    private String description;
    private String photo;
    private Boolean isPrivate; // false = Public, true = Private account
}
