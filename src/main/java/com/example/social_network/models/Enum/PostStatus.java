package com.example.social_network.models.Enum;

public enum PostStatus {
    PENDING_REVIEW, // Vừa tạo, chờ kiểm duyệt AI (xử lý bất đồng bộ)
    PUBLISHED,      // Đã kiểm duyệt hợp lệ, hiển thị công khai
    FLAGGED         // Bị gắn cờ vi phạm, ẩn khỏi bảng tin
}
