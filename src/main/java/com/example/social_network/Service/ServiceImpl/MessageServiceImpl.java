package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Request.ConversationRequest;
import com.example.social_network.Payload.Request.MessageRequest;
import com.example.social_network.Payload.Response.MessageResponse;
import com.example.social_network.Repository.ConversationRepository;
import com.example.social_network.Repository.MessageRepository;
import com.example.social_network.Repository.ParticipantRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.MessageService;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Entity.Conversation;
import com.example.social_network.models.Entity.Messages;
import com.example.social_network.models.Entity.Participant;
import com.example.social_network.models.Entity.User;
import com.example.social_network.models.Enum.ConversationType;
import com.example.social_network.models.Enum.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private UserRepository userRepository;

    // Công cụ đẩy dữ liệu qua WebSocket (có nhờ WebSocketConfig). Đây là phần "real-time".
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Công cụ GỬI (produce) event vào Kafka.
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    // Topic chứa sự kiện tin nhắn chat.
    public static final String CHAT_TOPIC = "chat-messages";

    // ====== Tạo / mở hội thoại ======
    @Override
    @Transactional
    public ResponseEntity<?> createConversation(ConversationRequest dto, String userId, String ip) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.UNAUTHORIZED, new ResponseMess(1, "Chưa xác thực người dùng"));
            }
            if (dto == null || dto.getParticipantIds() == null || dto.getParticipantIds().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "participantIds is required"));
            }

            // Bỏ trùng + bỏ chính mình + bỏ rỗng
            Set<String> targets = new LinkedHashSet<>();
            for (String id : dto.getParticipantIds()) {
                if (id != null && !id.trim().isEmpty() && !id.trim().equals(userId))
                    targets.add(id.trim());
            }
            if (targets.isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "Cần ít nhất 1 người khác để tạo hội thoại"));
            }

            // Kiểm tra người tạo + các thành viên tồn tại
            User creator = userRepository.findById(userId).orElse(null);
            if (creator == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "User not found with id = " + userId));
            }
            List<User> members = new ArrayList<>();
            for (String id : targets) {
                User u = userRepository.findById(id).orElse(null);
                if (u == null) {
                    return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "User not found with id = " + id));
                }
                members.add(u);
            }

            // Chat 1-1: nếu đã có phòng SINGLE giữa 2 người -> mở lại phòng cũ
            if (targets.size() == 1) {
                String other = targets.iterator().next();
                List<Conversation> existed = conversationRepository.findSingleBetween(ConversationType.SINGLE, userId, other);
                if (!existed.isEmpty()) {
                    return ResponseHelper.buildResponse(existed.get(0), HttpStatus.OK);
                }
            }

            // Tạo hội thoại mới
            Conversation conv = new Conversation();
            conv.setType(targets.size() == 1 ? ConversationType.SINGLE : ConversationType.GROUP);
            if (conv.getType() == ConversationType.GROUP && dto.getName() != null && !dto.getName().trim().isEmpty()) {
                conv.setName(dto.getName().trim());
            }
            conversationRepository.save(conv);

            // Thiết lập thành viên: người tạo + các thành viên
            addParticipant(conv, creator);
            for (User u : members) addParticipant(conv, u);

            logger.info("User {} created conversation {} ({}). IP: {}", userId, conv.getId(), conv.getType(), ip);
            return ResponseHelper.buildResponse(conv, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error in createConversation: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    private void addParticipant(Conversation conv, User user) {
        Participant p = new Participant();
        p.setConversation(conv);
        p.setUser(user);
        participantRepository.save(p);
    }

    // ====== Gửi tin nhắn (BƯỚC 2 - Kafka) ======
    // Validate nhanh để báo lỗi ngay cho người gửi, RỒI "ném" event vào topic và trả về liền.
    // Việc lưu DB + đẩy WebSocket do CONSUMER (KafkaChatListener -> processAndDeliver) làm sau.
    @Override
    public ResponseEntity<?> sendMessage(MessageRequest dto, String userId, String ip) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.UNAUTHORIZED, new ResponseMess(1, "Chưa xác thực người dùng"));
            }
            if (dto == null || dto.getConversationId() == null || dto.getConversationId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "conversationId is required"));
            }
            boolean hasText = dto.getText() != null && !dto.getText().trim().isEmpty();
            boolean hasPhoto = dto.getPhoto() != null && !dto.getPhoto().trim().isEmpty();
            if (!hasText && !hasPhoto) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "Tin nhắn phải có text hoặc photo"));
            }
            if (hasText && dto.getText().length() > 3000) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "Nội dung tối đa 3000 ký tự"));
            }

            Conversation conv = conversationRepository.findById(dto.getConversationId().trim()).orElse(null);
            if (conv == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Conversation not found"));
            }
            // Bảo mật: người gửi phải là thành viên hội thoại (check ngay để chặn sớm)
            if (participantRepository.findByConversation_IdAndUser_Id(conv.getId(), userId).isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.FORBIDDEN, new ResponseMess(1, "Bạn không thuộc hội thoại này"));
            }

            // Gắn người gửi vào event để consumer biết ai gửi (consumer không có token/HTTP context).
            dto.setSenderId(userId);

            // NÉM event vào Kafka rồi trả về NGAY (không chờ lưu DB / đẩy WebSocket).
            // Dùng conversationId làm key -> tin cùng 1 phòng vào cùng partition -> giữ đúng thứ tự.
            kafkaTemplate.send(CHAT_TOPIC, conv.getId(), dto);

            logger.info("User {} ĐẨY tin vào Kafka topic '{}' (conversation {}). IP: {}", userId, CHAT_TOPIC, conv.getId(), ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK,
                    new ResponseMess(0, "Tin nhắn đã được đưa vào hàng đợi Kafka để xử lý"));

        } catch (Exception e) {
            logger.error("Error in sendMessage (produce): {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    // ====== CONSUMER gọi: lưu DB + đẩy WebSocket ======
    // Chạy nền khi nhận event từ Kafka. senderId lấy TỪ EVENT (không phải token).
    @Override
    @Transactional
    public void processAndDeliver(MessageRequest dto) {
        try {
            if (dto == null || dto.getSenderId() == null || dto.getConversationId() == null) {
                logger.warn("Bỏ qua event chat thiếu senderId/conversationId");
                return;
            }
            boolean hasText = dto.getText() != null && !dto.getText().trim().isEmpty();
            boolean hasPhoto = dto.getPhoto() != null && !dto.getPhoto().trim().isEmpty();
            if (!hasText && !hasPhoto) {
                logger.warn("Bỏ qua event chat rỗng (không text/photo)");
                return;
            }

            User sender = userRepository.findById(dto.getSenderId()).orElse(null);
            Conversation conv = conversationRepository.findById(dto.getConversationId()).orElse(null);
            if (sender == null || conv == null) {
                logger.warn("Bỏ qua event chat: sender/conversation không tồn tại (sender={}, conv={})", dto.getSenderId(), dto.getConversationId());
                return;
            }

            MessageType type = MessageType.TEXT;
            if (dto.getMessageType() != null && !dto.getMessageType().trim().isEmpty()) {
                try {
                    type = MessageType.valueOf(dto.getMessageType().trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    type = MessageType.TEXT; // event lỗi type -> mặc định TEXT, không chặn
                }
            }

            Messages msg = new Messages();
            msg.setConversation(conv);
            msg.setUserTransmitter(sender);
            msg.setText(hasText ? dto.getText().trim() : null);
            msg.setPhoto(hasPhoto ? dto.getPhoto().trim() : null);
            msg.setMessageType(type);
            messageRepository.save(msg);

            conv.setLastMessageTime(LocalDateTime.now());
            conversationRepository.save(conv);

            MessageResponse res = toResponse(msg);
            // ĐẨY REAL-TIME: người trong phòng đang subscribe sẽ nhận ngay
            messagingTemplate.convertAndSend("/topic/conversation/" + conv.getId(), res);

            logger.info("Consumer đã LƯU + ĐẨY tin {} (conversation {})", msg.getId(), conv.getId());
        } catch (Exception e) {
            logger.error("Error in processAndDeliver: {}", e.getMessage());
        }
    }

    // ====== Lịch sử tin nhắn ======
    @Override
    public Object getMessages(String conversationId, String userId, int pageIdx, int pageSize) {
        try {
            if (conversationId == null || conversationId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "conversationId is required"));
            }
            if (participantRepository.findByConversation_IdAndUser_Id(conversationId.trim(), userId).isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.FORBIDDEN, new ResponseMess(1, "Bạn không thuộc hội thoại này"));
            }

            Pageable paging = PageRequest.of(pageIdx, pageSize);
            Page<Messages> page = messageRepository.findByConversation_IdOrderByCreateTimeDesc(conversationId.trim(), paging);
            List<MessageResponse> results = page.getContent().stream().map(this::toResponse).collect(Collectors.toList());

            logger.info("Get messages of conversation {} -> {} tin (page {})", conversationId, results.size(), pageIdx);
            return ResponseHelper.getResponses(results, page.getTotalElements(), page.getTotalPages(), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getMessages: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    // ====== Danh sách hội thoại (Inbox) ======
    @Override
    public Object getConversations(String userId, int pageIdx, int pageSize) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.UNAUTHORIZED, new ResponseMess(1, "Chưa xác thực người dùng"));
            }
            Pageable paging = PageRequest.of(pageIdx, pageSize);
            Page<Participant> page = participantRepository.findByUser_IdOrderByConversation_LastMessageTimeDesc(userId, paging);

            List<Map<String, Object>> results = new ArrayList<>();
            for (Participant p : page.getContent()) {
                Conversation c = p.getConversation();
                Messages last = messageRepository.findTopByConversation_IdOrderByCreateTimeDesc(c.getId());
                Map<String, Object> item = new HashMap<>();
                item.put("conversationId", c.getId());
                item.put("name", c.getName());
                item.put("type", c.getType() != null ? c.getType().name() : null);
                item.put("lastMessageTime", c.getLastMessageTime());
                item.put("lastMessage", last == null ? null : toResponse(last));
                results.add(item);
            }

            logger.info("Get conversations of {} -> {} hội thoại", userId, results.size());
            return ResponseHelper.getResponses(results, page.getTotalElements(), page.getTotalPages(), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getConversations: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    // ====== Helper: Entity -> DTO trả về ======
    private MessageResponse toResponse(Messages m) {
        MessageResponse r = new MessageResponse();
        r.setId(m.getId());
        r.setConversationId(m.getConversation() != null ? m.getConversation().getId() : null);
        User s = m.getUserTransmitter();
        if (s != null) {
            r.setSenderId(s.getId());
            r.setSenderName(s.getName() != null && !s.getName().isEmpty() ? s.getName() : s.getUsername());
        }
        r.setText(m.getText());
        r.setPhoto(m.getPhoto());
        r.setMessageType(m.getMessageType() != null ? m.getMessageType().name() : null);
        r.setCreateTime(m.getCreateTime());
        return r;
    }
}
