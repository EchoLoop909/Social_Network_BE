package com.example.social_network.Service;

import com.example.social_network.Payload.Request.LoginRequest;
import com.example.social_network.Payload.Request.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface UserService {
    Object getUser(String userId, int pageIdx, int pageSize);

    ResponseEntity<?> uploadImg(String username, String url);

    /** Luồng đăng ký: tạo user trên Keycloak + lưu bảng users (PENDING_ACTIVATION). */
    ResponseEntity<?> register(RegisterRequest request);

    /** Kích hoạt tài khoản: PENDING_ACTIVATION -> ACTIVE (gọi khi email đã xác thực). */
    ResponseEntity<?> activate(String userId);

    /**
     * Lấy user hiện tại + sync-on-login: nếu email đã verify và đang PENDING_ACTIVATION -> ACTIVE;
     * nếu tài khoản SUSPENDED -> chặn (403).
     */
    ResponseEntity<?> getCurrentUser(String userId, boolean emailVerified);

    /** Đăng nhập (backend proxy Keycloak): trả access_token + refresh_token + thông tin user. */
    ResponseEntity<?> login(LoginRequest request);

    /** Cấp access_token mới từ refresh_token. */
    ResponseEntity<?> refresh(String refreshToken);

    /** Đăng xuất: thu hồi phiên tại Keycloak. */
    ResponseEntity<?> logout(String refreshToken);
}
