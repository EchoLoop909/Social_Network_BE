package com.example.social_network.Payload.Request;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Tạo hashtag mới. name không cần dấu '#'. */
@NoArgsConstructor
@Data
public class HashtagRequest {
    private String name;
}
