package com.example.social_network.Service;

import com.example.social_network.Payload.Request.LoginRequest;
import com.example.social_network.Payload.Request.RegisterRequest;
import com.example.social_network.models.Dto.DeleteDto;
import com.example.social_network.models.Dto.UserUpdateDto;
import org.springframework.http.ResponseEntity;

public interface UserService {
    // viewerId = người đang đăng nhập (từ token) — dùng để ẩn user đã bị chặn / chặn mình.
    // keyword = từ khóa tìm theo tên (username/name/firstname/lastname), null = không lọc.
    Object getUser(String userId, String keyword, String viewerId, int pageIdx, int pageSize);

    ResponseEntity<?> uploadImg(String username, String url);

    /** Cập nhật hồ sơ cá nhân (gồm cả is_private). userId lấy từ token. */
    ResponseEntity<?> updateUser(String userId, UserUpdateDto dto, String ip);

    /** Xóa mềm tài khoản: set deletion_date + status = SUSPENDED. */
    ResponseEntity<?> deleteUser(DeleteDto dto, String ip);

    /** Luồng đăng ký: tạo user trên Keycloak + lưu bảng users (PENDING_ACTIVATION). */
    ResponseEntity<?> register(RegisterRequest request);

    /** Đăng nhập (backend proxy Keycloak): trả access_token + refresh_token + thông tin user. */
    ResponseEntity<?> login(LoginRequest request);

    /** Đăng xuất: thu hồi phiên tại Keycloak. */
    ResponseEntity<?> logout(String refreshToken);

    /**
     * Đồng bộ toàn bộ user trong DB lên Keycloak: tạo user còn thiếu (mật khẩu mặc định),
     * gán lại id_user = sub. User đã tồn tại trên Keycloak (409) sẽ được bỏ qua.
     */
    ResponseEntity<?> syncUsersToKeycloak();
}
