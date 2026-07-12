package com.example.social_network.Service;

import com.example.social_network.Payload.Request.LoginRequest;
import com.example.social_network.Payload.Request.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface UserService {
    Object getUser(String userId, int pageIdx, int pageSize);

    ResponseEntity<?> uploadImg(String username, String url);

    /** Luồng đăng ký: tạo user trên Keycloak + lưu bảng users (PENDING_ACTIVATION). */
    ResponseEntity<?> register(RegisterRequest request);

    /** Đăng nhập (backend proxy Keycloak): trả access_token + refresh_token + thông tin user. */
    ResponseEntity<?> login(LoginRequest request);

    /** Đăng xuất: thu hồi phiên tại Keycloak. */
    ResponseEntity<?> logout(String refreshToken);
}
