package com.example.social_network.models.Enum;

public enum ReportStatus {
    PENDING,  // Chờ quản trị viên duyệt
    RESOLVED, // Đã xử lý (ẩn/xóa nội dung hoặc khóa tài khoản)
    REJECTED  // Đã xem xét và bỏ qua (không vi phạm)
}
