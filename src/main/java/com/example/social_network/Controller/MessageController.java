package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.MessageRequest;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(PathResources.MESSAGE) // Giả sử PathResources.MESSAGE = "/api/messages"
public class MessageController {

    @Autowired
    private MessageService messageService;

    // API để gửi tin nhắn qua REST (dự phòng cho WebSocket hoặc dùng cho tích hợp)
    @PostMapping(PathResources.INSERT)
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequest requestDto, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return messageService.sendMessage(requestDto, ip);
    }

    // Lấy lịch sử tin nhắn của một cuộc trò chuyện
    @GetMapping("/history")
    public Object getMessagesByConversation(
            @RequestParam String conversationId,
            @RequestParam(defaultValue = "1") int pageIdx,
            @RequestParam(defaultValue = "20") int pageSize) {
        return messageService.getMessagesByConversation(conversationId, pageIdx - 1, pageSize);
    }
}