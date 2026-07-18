package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Response.NotificationResponse;
import com.example.social_network.Repository.NotificationRepository;
import com.example.social_network.Repository.ParticipantRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.NotificationService;
import com.example.social_network.models.Dto.NotificationEvent;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Entity.Conversation;
import com.example.social_network.models.Entity.Messages;
import com.example.social_network.models.Entity.Notification;
import com.example.social_network.models.Entity.Participant;
import com.example.social_network.models.Entity.User;
import com.example.social_network.models.Enum.NotificationType;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    // Topic RIÊNG cho thông báo — tách khỏi topic chat để dùng chung cho mọi loại noti sau này.
    public static final String NOTI_TOPIC = "notifications";

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    // Đẩy real-time xuống trình duyệt người nhận.
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Gửi (produce) sự kiện thông báo vào Kafka.
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    // ====== PRODUCER tổng quát ======
    @Override
    public void publish(NotificationEvent event) {
        if (event == null || event.getRecipientId() == null || event.getRecipientId().trim().isEmpty()) {
            logger.warn("Bỏ qua notification thiếu recipientId");
            return;
        }
        // Key = recipientId -> thông báo của cùng 1 người vào cùng partition (giữ thứ tự).
        kafkaTemplate.send(NOTI_TOPIC, event.getRecipientId(), event);
        logger.info("ĐẨY notification vào Kafka topic '{}' cho recipient {} (type {})",
                NOTI_TOPIC, event.getRecipientId(), event.getType());
    }

    // ====== Tiện ích: sinh noti cho tin nhắn mới ======
    @Override
    public void notifyNewMessage(Conversation conv, User sender, Messages msg) {
        if (conv == null || sender == null) return;

        String preview;
        if (msg != null && msg.getText() != null && !msg.getText().trim().isEmpty()) {
            String t = msg.getText().trim();
            preview = t.length() > 100 ? t.substring(0, 100) + "..." : t;
        } else {
            preview = "Đã gửi một ảnh";
        }

        // Báo cho mọi thành viên trong hội thoại TRỪ người gửi.
        for (Participant p : participantRepository.findByConversation_Id(conv.getId())) {
            User u = p.getUser();
            if (u == null || u.getId().equals(sender.getId())) continue;
            NotificationEvent event = new NotificationEvent(
                    u.getId(), sender.getId(), NotificationType.MESSAGE.name(), conv.getId(), preview);
            publish(event);
        }
    }

    // ====== CONSUMER: lưu DB + đẩy WebSocket ======
    @Override
    @Transactional
    public void processAndDeliver(NotificationEvent event) {
        try {
            if (event == null || event.getRecipientId() == null || event.getRecipientId().trim().isEmpty()) {
                logger.warn("Bỏ qua notification event thiếu recipientId");
                return;
            }

            User recipient = userRepository.findById(event.getRecipientId()).orElse(null);
            if (recipient == null) {
                logger.warn("Bỏ qua notification: recipient {} không tồn tại", event.getRecipientId());
                return;
            }

            User actor = null;
            if (event.getActorId() != null && !event.getActorId().trim().isEmpty()) {
                actor = userRepository.findById(event.getActorId()).orElse(null);
            }

            NotificationType type = NotificationType.SYSTEM;
            if (event.getType() != null && !event.getType().trim().isEmpty()) {
                try {
                    type = NotificationType.valueOf(event.getType().trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    type = NotificationType.SYSTEM; // type lạ -> mặc định SYSTEM, không chặn
                }
            }

            Notification noti = new Notification();
            noti.setRecipient(recipient);
            noti.setActor(actor);
            noti.setType(type);
            noti.setTargetId(event.getTargetId());
            notificationRepository.save(noti); // id, isRead=false, createTime tự set trong @PrePersist

            // Đẩy real-time xuống kênh RIÊNG của người nhận (kèm preview cho toast).
            NotificationResponse res = toResponse(noti);
            res.setMessage(event.getPreview());
            messagingTemplate.convertAndSend("/topic/notifications/" + recipient.getId(), res);

            logger.info("Consumer đã LƯU + ĐẨY notification {} cho recipient {}", noti.getId(), recipient.getId());
        } catch (Exception e) {
            logger.error("Error in processAndDeliver (notification): {}", e.getMessage());
        }
    }

    // ====== Đồng bộ im lặng quan hệ kết bạn (không lưu DB, không hiện chuông) ======
    @Override
    public void notifyFriendshipSync(String recipientId) {
        try {
            if (recipientId == null || recipientId.trim().isEmpty()) return;
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "FRIENDSHIP_SYNC"); // FE hiểu đây là tín hiệu refresh, không phải noti
            messagingTemplate.convertAndSend("/topic/notifications/" + recipientId, payload);
            logger.info("Đẩy FRIENDSHIP_SYNC cho {}", recipientId);
        } catch (Exception ex) {
            logger.error("Lỗi gửi friendship-sync cho {}: {}", recipientId, ex.getMessage());
        }
    }

    // ====== Danh sách thông báo của mình ======
    @Override
    @Transactional(readOnly = true)
    public Object getMyNotifications(String userId, int pageIdx, int pageSize) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.UNAUTHORIZED, new ResponseMess(1, "Chưa xác thực người dùng"));
            }
            Pageable paging = PageRequest.of(pageIdx, pageSize);
            Page<Notification> page = notificationRepository.findByRecipient_IdOrderByCreateTimeDesc(userId, paging);

            List<NotificationResponse> results = new ArrayList<>();
            for (Notification n : page.getContent()) results.add(toResponse(n));

            logger.info("Get notifications of {} -> {} thông báo (page {})", userId, results.size(), pageIdx);
            return ResponseHelper.getResponses(results, page.getTotalElements(), page.getTotalPages(), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getMyNotifications: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    // ====== Đếm chưa đọc ======
    @Override
    public Object getUnreadCount(String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.UNAUTHORIZED, new ResponseMess(1, "Chưa xác thực người dùng"));
            }
            long count = notificationRepository.countByRecipient_IdAndIsReadFalse(userId);
            logger.info("Unread notifications of {} = {}", userId, count);
            return ResponseHelper.getResponses(count, count, 1, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getUnreadCount: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    // ====== Đánh dấu 1 thông báo đã đọc ======
    @Override
    public ResponseEntity<?> markRead(String id, String userId, String ip) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "id is required"));
            }
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.UNAUTHORIZED, new ResponseMess(1, "Chưa xác thực người dùng"));
            }

            Notification noti = notificationRepository.findById(id.trim()).orElse(null);
            if (noti == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Notification not found"));
            }
            if (noti.getRecipient() == null || !noti.getRecipient().getId().equals(userId)) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.FORBIDDEN, new ResponseMess(1, "Bạn không có quyền với thông báo này"));
            }

            noti.setIsRead(true);
            notificationRepository.save(noti);

            logger.info("Notification {} marked read by {}. IP: {}", noti.getId(), userId, ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "READ NOTIFICATION SUCCESS"));
        } catch (Exception e) {
            logger.error("Error in markRead: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "UPDATE FAILED: " + e.getMessage()));
        }
    }

    // ====== Đánh dấu tất cả đã đọc ======
    @Override
    @Transactional
    public ResponseEntity<?> markAllRead(String userId, String ip) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.UNAUTHORIZED, new ResponseMess(1, "Chưa xác thực người dùng"));
            }
            int updated = notificationRepository.markAllRead(userId);
            logger.info("Marked {} notifications read for {}. IP: {}", updated, userId, ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "READ ALL NOTIFICATION SUCCESS"));
        } catch (Exception e) {
            logger.error("Error in markAllRead: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "UPDATE FAILED: " + e.getMessage()));
        }
    }

    // ====== Helper: Entity -> DTO ======
    private NotificationResponse toResponse(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.setId(n.getId());
        r.setType(n.getType() != null ? n.getType().name() : null);
        r.setTargetId(n.getTargetId());
        r.setIsRead(n.getIsRead());
        r.setCreateTime(n.getCreateTime());
        User actor = n.getActor();
        if (actor != null) {
            r.setActorId(actor.getId());
            r.setActorName(actor.getName() != null && !actor.getName().isEmpty() ? actor.getName() : actor.getUsername());
            r.setActorPhoto(actor.getPhoto());
        }
        return r;
    }
}
