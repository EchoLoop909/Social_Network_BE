package com.example.social_network.Payload.Request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/** Dữ liệu đăng nhập (Cách 2 - backend proxy tới Keycloak). */
@Data
public class LoginRequest {

    @NotBlank(message = "Tài khoản không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
