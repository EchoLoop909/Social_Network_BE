package com.example.social_network.models.Enum;

public enum PostStatus {
    PENDING_REVIEW, // Vừa tạo, chờ kiểm duyệt AI (bất đồng bộ qua Kafka)
    PUBLISHED,      // Đã kiểm duyệt hợp lệ, hiển thị công khai
    FLAGGED         // Bị gắn cờ vi phạm, ẩn khỏi bảng tin
}
