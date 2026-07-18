package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.BookmarkRequest;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Payload.Util.SecurityUtils;
import com.example.social_network.Service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(PathResources.BOOKMARK)
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    // Lưu / bỏ lưu 1 bài (toggle). Người dùng lấy từ token. Body: { postId }.
    @PostMapping(PathResources.TOGGLE)
    public ResponseEntity<?> toggle(@RequestBody BookmarkRequest dto, HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return bookmarkService.toggle(dto.getPostId(), userId, request.getRemoteAddr());
    }

    // Danh sách bài đã lưu của CHÍNH MÌNH (trả về các Post).
    @GetMapping(PathResources.LIST)
    public Object getMyBookmarks(@RequestParam(defaultValue = "1") int pageIdx,
                                 @RequestParam(defaultValue = "30") int pageSize) {
        String userId = SecurityUtils.getCurrentUserId();
        return bookmarkService.getMyBookmarks(userId, pageIdx - 1, pageSize);
    }

    // Danh sách id bài đã lưu (để FE tô nút đã lưu).
    @GetMapping(PathResources.IDS)
    public Object getMyBookmarkPostIds() {
        return bookmarkService.getMyBookmarkPostIds(SecurityUtils.getCurrentUserId());
    }
}
