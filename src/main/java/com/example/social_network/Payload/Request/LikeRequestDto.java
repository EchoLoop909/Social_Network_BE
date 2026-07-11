package com.example.social_network.Payload.Request;

import lombok.Data;

@Data
public class LikeRequestDto {
    // POST hoặc COMMENT (bắt buộc)
    private String targetType;

    // id_post hoặc id_comment tuỳ targetType (bắt buộc)
    private String targetId;

    // LIKE / HEART / HAHA / WOW / SAD / ANGRY — bắt buộc cho API react;
    // KHÔNG dùng ở API unlike (unlike xoá theo user + target, bất kể loại cảm xúc)
    private String reactionType;

    // id_user KHÔNG nhận từ request — lấy từ JWT subject ở Controller/Service
}
