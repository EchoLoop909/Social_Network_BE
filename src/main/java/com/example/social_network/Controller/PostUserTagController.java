package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.PostUserTagRequest;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Payload.Util.SecurityUtils;
import com.example.social_network.Service.PostUserTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(PathResources.POST_USER_TAG)
public class PostUserTagController {

    @Autowired
    private PostUserTagService postUserTagService;

    // Gắn thẻ user vào bài. taggerId lấy từ token; người được gắn nhận thông báo TAG.
    @PostMapping(PathResources.INSERT)
    public ResponseEntity<?> addTag(@RequestBody PostUserTagRequest dto, HttpServletRequest request) {
        String taggerId = SecurityUtils.getCurrentUserId();
        return postUserTagService.addTag(dto, taggerId, request.getRemoteAddr());
    }

    @GetMapping(PathResources.LIST)
    public Object getByPost(@RequestParam String postId) {
        return postUserTagService.getByPost(postId);
    }

    // Bài mà 1 user được gắn thẻ (tab "Được gắn thẻ"). Không truyền userId -> của chính mình.
    @GetMapping(PathResources.TAGGED)
    public Object getTaggedPosts(@RequestParam(required = false) String userId) {
        String ownerId = (userId != null && !userId.trim().isEmpty()) ? userId.trim() : SecurityUtils.getCurrentUserId();
        return postUserTagService.getTaggedPosts(ownerId);
    }

    @DeleteMapping(PathResources.DELETE)
    public ResponseEntity<?> removeTag(@RequestBody PostUserTagRequest dto, HttpServletRequest request) {
        return postUserTagService.removeTag(dto, request.getRemoteAddr());
    }
}
