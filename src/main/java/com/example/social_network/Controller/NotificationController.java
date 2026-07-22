package com.example.social_network.Controller;

import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Payload.Util.SecurityUtils;
import com.example.social_network.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(PathResources.NOTIFICATION)
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Danh sách thông báo của người đang đăng nhập (mới nhất trước).
    @GetMapping(PathResources.LIST)
    public Object getMyNotifications(@RequestParam(defaultValue = "1") int pageIdx,
                                     @RequestParam(defaultValue = "20") int pageSize) {
        String userId = SecurityUtils.getCurrentUserId();
        return notificationService.getMyNotifications(userId, pageIdx - 1, pageSize);
    }

    // Số thông báo chưa đọc (badge chuông).
    @GetMapping(PathResources.UNREAD_COUNT)
    public Object getUnreadCount() {
        String userId = SecurityUtils.getCurrentUserId();
        return notificationService.getUnreadCount(userId);
    }

    // Đánh dấu 1 thông báo đã đọc.
    @PostMapping(PathResources.READ)
    public ResponseEntity<?> markRead(@RequestParam String id, HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return notificationService.markRead(id, userId, request.getRemoteAddr());
    }

    // Đánh dấu tất cả thông báo đã đọc.
    @PostMapping(PathResources.READ_ALL)
    public ResponseEntity<?> markAllRead(HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return notificationService.markAllRead(userId, request.getRemoteAddr());
    }

    // Xóa 1 thông báo (chỉ chính chủ).
    @PostMapping(PathResources.DELETE)
    public ResponseEntity<?> delete(@RequestParam String id, HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return notificationService.deleteNotification(id, userId, request.getRemoteAddr());
    }
}
