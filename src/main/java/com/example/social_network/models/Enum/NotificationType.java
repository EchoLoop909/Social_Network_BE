package com.example.social_network.models.Enum;

public enum NotificationType {
    LIKE,           // Có người thả cảm xúc vào bài viết/bình luận
    COMMENT,        // Có người bình luận bài viết
    REPLY,          // Có người trả lời bình luận
    FOLLOW,         // Có người theo dõi (tài khoản public)
    FOLLOW_REQUEST, // Có yêu cầu theo dõi chờ duyệt (tài khoản private)
    TAG,            // Được gắn thẻ trong bài viết
    MESSAGE,        // Có người nhắn tin trong hội thoại
    SYSTEM          // Thông báo hệ thống (vi phạm, kiểm duyệt...)
}
