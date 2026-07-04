package com.example.social_network.models.Enum;

public enum UserStatus {
    PENDING_ACTIVATION, // Vừa đăng ký, chờ xác thực email
    ACTIVE,             // Đã xác thực, hoạt động bình thường
    SUSPENDED           // Bị quản trị viên đóng băng (khóa tài khoản)
}
