package com.example.social_network.models.Enum;

public enum FollowStatus {
    PENDING,    //  Đang chờ (giữ lại để tương thích; luồng mới không tạo mới ở trạng thái này)
    FOLLOWING,  // Đang theo dõi — set ngay khi gửi lời mời (chưa được chủ tài khoản đích chấp nhận)
    ACCEPTED,   // Đã được chủ tài khoản đích chấp nhận (được xem cả tài khoản riêng tư)
    REJECTED    // Lời mời theo dõi đã bị từ chối
}
