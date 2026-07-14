package com.example.social_network.models.Dto;

import com.example.social_network.models.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

/** Thông tin rút gọn của 1 user để hiển thị trong danh sách bạn bè / lời mời / gợi ý. */
@Data
@AllArgsConstructor
public class UserSummaryDto {
    private String id;
    private String username;
    private String name;
    private String firstname;
    private String lastname;
    private String photo;
    private Boolean isPrivate;

    public static UserSummaryDto from(User u) {
        return new UserSummaryDto(
                u.getId(),
                u.getUsername(),
                u.getName(),
                u.getFirstname(),
                u.getLastname(),
                u.getPhoto(),
                u.getIsPrivate()
        );
    }
}
