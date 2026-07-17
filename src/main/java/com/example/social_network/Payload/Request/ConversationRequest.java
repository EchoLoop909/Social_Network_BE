package com.example.social_network.Payload.Request;

import lombok.Data;

import java.util.List;

/**
 * DTO tạo/mở hội thoại. Người tạo lấy từ token (KHÔNG nhận trong body).
 * - participantIds: danh sách id người được thêm vào (KHÔNG gồm chính mình).
 *   + đúng 1 người  -> chat 1-1 (SINGLE); nếu đã tồn tại thì mở lại phòng cũ.
 *   + từ 2 người trở lên -> chat nhóm (GROUP).
 * - name: tên nhóm (tùy chọn, chỉ dùng cho GROUP).
 */
@Data
public class ConversationRequest {
    private List<String> participantIds;
    private String name;
}
