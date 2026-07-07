package com.example.social_network.Payload.Request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/** Dùng cho /auth/refresh và /auth/logout. */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "refreshToken không được để trống")
    private String refreshToken;
}
