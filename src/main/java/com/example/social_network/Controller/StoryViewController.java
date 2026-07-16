package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.StoryViewRequest;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Payload.Util.SecurityUtils;
import com.example.social_network.Service.StoryViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(PathResources.STORY_VIEW)
public class StoryViewController {

    @Autowired
    private StoryViewService storyViewService;

    // Ghi nhận lượt xem story. viewer mặc định lấy từ token; có thể truyền ?userId= để test Swagger.
    @PostMapping(PathResources.INSERT)
    public ResponseEntity<?> recordView(@RequestParam(required = false) String userId,
                                        @RequestBody StoryViewRequest dto,
                                        HttpServletRequest request) {
        String viewer = (userId != null && !userId.trim().isEmpty())
                ? userId.trim()
                : SecurityUtils.getCurrentUserId();
        return storyViewService.recordView(dto.getStoryId(), viewer, request.getRemoteAddr());
    }

    // Danh sách người đã xem 1 story.
    @GetMapping(PathResources.LIST)
    public Object getViewers(@RequestParam String storyId) {
        return storyViewService.getViewers(storyId);
    }
}
