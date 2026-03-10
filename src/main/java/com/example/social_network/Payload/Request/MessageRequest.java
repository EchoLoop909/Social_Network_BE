package com.example.social_network.Payload.Request;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class MessageRequest {
    private String conversationId;
    private String senderId;
    private String text;
    private String photo;
    private String messageType; // TEXT, IMAGE, SYSTEM
}