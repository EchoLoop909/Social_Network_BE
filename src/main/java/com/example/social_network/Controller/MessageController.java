package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.ConversationRequest;
import com.example.social_network.Payload.Request.MessageRequest;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Payload.Util.SecurityUtils;
import com.example.social_network.Service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(PathResources.MESSAGE)
public class MessageController {

    @Autowired
    private MessageService messageService;

    // Tạo/mở hội thoại. Người tạo lấy từ token.
    @PostMapping(PathResources.CONVERSATION)
    public ResponseEntity<?> createConversation(@RequestBody ConversationRequest dto,
                                                HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return messageService.createConversation(dto, userId, request.getRemoteAddr());
    }

    // Gửi tin nhắn (lưu DB + đẩy WebSocket). Người gửi lấy từ token.
    @PostMapping(PathResources.SEND)
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequest dto,
                                         HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return messageService.sendMessage(dto, userId, request.getRemoteAddr());
    }

    // Lịch sử tin nhắn của 1 hội thoại.
    @GetMapping(PathResources.HISTORY)
    public Object getHistory(@RequestParam String conversationId,
                             @RequestParam(defaultValue = "1") int pageIdx,
                             @RequestParam(defaultValue = "20") int pageSize) {
        String userId = SecurityUtils.getCurrentUserId();
        return messageService.getMessages(conversationId, userId, pageIdx - 1, pageSize);
    }

    // Danh sách hội thoại (Inbox) của người đang đăng nhập.
    @GetMapping(PathResources.CONVERSATIONS)
    public Object getConversations(@RequestParam(defaultValue = "1") int pageIdx,
                                   @RequestParam(defaultValue = "30") int pageSize) {
        String userId = SecurityUtils.getCurrentUserId();
        return messageService.getConversations(userId, pageIdx - 1, pageSize);
    }

    // Đánh dấu đã đọc hội thoại (tới tin mới nhất).
    @PostMapping(PathResources.READ)
    public ResponseEntity<?> markRead(@RequestParam String conversationId, HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return messageService.markRead(conversationId, userId, request.getRemoteAddr());
    }

    // Trạng thái đã đọc của thành viên khác (cho dấu "Đã xem").
    @GetMapping(PathResources.READ_STATE)
    public Object getReadState(@RequestParam String conversationId) {
        String userId = SecurityUtils.getCurrentUserId();
        return messageService.getReadState(conversationId, userId);
    }
}
