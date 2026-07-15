package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.StoryRequest;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Payload.Util.SecurityUtils;
import com.example.social_network.Service.StoryService;
import com.example.social_network.models.Dto.DeleteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(PathResources.STORY)
public class StoryController {

    @Autowired
    private StoryService storyService;

    // Đăng story. id_user (chủ tin) lấy từ token.
    @PostMapping(PathResources.INSERT)
    public ResponseEntity<?> createStory(@RequestBody StoryRequest dto,
                                         HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return storyService.createStory(dto, userId, request.getRemoteAddr());
    }

    // Xem danh sách story đang hiển thị của 1 user. Không truyền userId -> lấy story của chính mình.
    @GetMapping(PathResources.LIST)
    public Object getStories(@RequestParam(required = false) String userId,
                             @RequestParam(defaultValue = "1") int pageIdx,
                             @RequestParam(defaultValue = "20") int pageSize) {
        String ownerId = (userId != null && !userId.trim().isEmpty())
                ? userId.trim()
                : SecurityUtils.getCurrentUserId();
        return storyService.getStories(ownerId, pageIdx - 1, pageSize);
    }

    // Xóa story theo id trong body. Chỉ chủ tin mới xóa được (userId từ token).
    @DeleteMapping(PathResources.DELETE)
    public ResponseEntity<?> deleteStory(@RequestBody DeleteDto dto,
                                         HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return storyService.deleteStory(dto, userId, request.getRemoteAddr());
    }
}
