package com.example.social_network.Payload.Request;

import lombok.Data;

@Data
public class BlockUserDto {
    // id của B — người mà A (lấy từ JWT) muốn chặn / bỏ chặn.
    private String targetUserId;
}
