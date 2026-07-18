package com.example.social_network.Payload.Request;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bật/tắt Highlight cho 1 story.
 * isArchived = true -> lưu vào Highlights (giữ mãi); false -> bỏ khỏi Highlights.
 * Nếu isArchived = null -> đảo trạng thái hiện tại (toggle).
 */
@NoArgsConstructor
@Data
public class StoryHighlightDto {
    private String id;
    private Boolean isArchived;
}
