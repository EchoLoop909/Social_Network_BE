package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Request.MessageRequest;
import com.example.social_network.Payload.Response.MessageResponse;
import com.example.social_network.Repository.ConversationRepository;
import com.example.social_network.Repository.MessageRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.MessageService;
import com.example.social_network.models.Entity.Conversation;
import com.example.social_network.models.Entity.Messages;
import com.example.social_network.models.Entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private KafkaTemplate<String, MessageRequest> kafkaTemplate;

    @Override
    @Transactional
    public ResponseEntity<?> sendMessage(MessageRequest request, String ip) {
        try {
            // SỬA LẠI: KHÔNG GỌI processAndSaveMessage(request) Ở ĐÂY NỮA!
            // Thay vào đó, ném thẳng gói tin vào Kafka Topic
            kafkaTemplate.send("chat-messages", request.getConversationId(), request);

            logger.info("Đã ném request nhắn tin của User {} vào Kafka thành công. IP: {}",
                    request.getSenderId(), ip);

            // Trả về luôn cho Swagger/Client mà không cần chờ lưu DB
            return ResponseEntity.ok("Tin nhắn đã được đưa vào hàng đợi Kafka để xử lý!");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error in sendMessage: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("SYSTEM ERROR: " + e.getMessage());
        }
    }

    // Hàm này tách riêng ra để Kafka Listener có thể gọi trực tiếp và lấy kết quả
    @Override
    @Transactional
    public MessageResponse processAndSaveMessage(MessageRequest dto) {
        // 1. Validate ID
        if (dto.getSenderId() == null || dto.getSenderId().trim().isEmpty()) {
            throw new IllegalArgumentException("senderId is required");
        }
        if (dto.getConversationId() == null || dto.getConversationId().trim().isEmpty()) {
            throw new IllegalArgumentException("conversationId is required");
        }

        // 2. Validate nội dung (Phải có text hoặc photo)
        boolean hasText = dto.getText() != null && !dto.getText().trim().isEmpty();
        boolean hasPhoto = dto.getPhoto() != null && !dto.getPhoto().trim().isEmpty();
        if (!hasText && !hasPhoto) {
            throw new IllegalArgumentException("Message must contain text or photo");
        }

        // Validate độ dài text
        if (hasText && dto.getText().length() > 3000) {
            throw new IllegalArgumentException("Message text exceeds maximum length of 3000 characters");
        }

        // 3. Kiểm tra User và Conversation
        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id = " + dto.getSenderId()));

        Conversation conversation = conversationRepository.findById(dto.getConversationId())
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found with id = " + dto.getConversationId()));

        // Tùy chọn: Validate xem User có nằm trong Conversation này không (Dựa vào bảng Participant)
        // participantRepository.findByConversationAndUser(...)

        // 4. Tạo và lưu Message
        Messages message = new Messages();
        message.setConversation(conversation);
        message.setUserTransmitter(sender);
        message.setText(dto.getText() != null ? dto.getText().trim() : null);
        message.setPhoto(dto.getPhoto());
        message.setMessageType(dto.getMessageType() != null ? dto.getMessageType() : "TEXT");

        Messages savedMessage = messageRepository.save(message);

        // 5. Cập nhật lastMessageTime cho Conversation
        conversation.setLastMessageTime(LocalDateTime.now());
        conversationRepository.save(conversation);

        // 6. Map sang DTO Response
        MessageResponse response = new MessageResponse();
        response.setId(savedMessage.getId());
        response.setConversationId(conversation.getId());
        response.setSenderId(sender.getId());
        // Giả sử User entity của bạn có getFullName() hoặc getUsername()
        // response.setSenderName(sender.getFullName());
        response.setText(savedMessage.getText());
        response.setPhoto(savedMessage.getPhoto());
        response.setMessageType(savedMessage.getMessageType());
        response.setCreateTime(savedMessage.getCreateTime());

        return response;
    }

    @Override
    public Object getMessagesByConversation(String conversationId, int pageIdx, int pageSize) {
        try {
            if (conversationId == null || conversationId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("conversationId is required");
            }

            Pageable paging = PageRequest.of(pageIdx, pageSize);
            // Cần định nghĩa hàm này trong MessageRepository:
            // Page<Messages> findByConversation_IdOrderByCreateTimeDesc(String conversationId, Pageable pageable);
            Page<Messages> messagePage = messageRepository.findByConversation_IdOrderByCreateTimeDesc(conversationId, paging);

            // Map Entity sang DTO để trả về client
            List<MessageResponse> results = messagePage.getContent().stream().map(msg -> {
                MessageResponse res = new MessageResponse();
                res.setId(msg.getId());
                res.setConversationId(msg.getConversation().getId());
                res.setSenderId(msg.getUserTransmitter().getId());
                res.setText(msg.getText());
                res.setPhoto(msg.getPhoto());
                res.setMessageType(msg.getMessageType());
                res.setCreateTime(msg.getCreateTime());
                return res;
            }).collect(Collectors.toList());

            logger.info("Get list messages for Conversation {} success. Page: {}, Size: {}", conversationId, pageIdx, pageSize);

            return ResponseHelper.getResponses(results, messagePage.getTotalElements(), messagePage.getTotalPages(), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error in getMessagesByConversation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("SYSTEM ERROR: " + e.getMessage());
        }
    }
}