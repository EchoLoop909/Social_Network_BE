package com.example.social_network.Controller;

import com.example.social_network.Config.Cloudinary.CloudinaryService;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Payload.Util.SecurityUtils;
import com.example.social_network.Service.PostService;
import com.example.social_network.Service.RecommendationService;
import com.example.social_network.Service.VideoViewService;
import com.example.social_network.models.Dto.VideoViewRequest;
import com.example.social_network.models.Dto.DeleteDto;
import com.example.social_network.models.Dto.Posts.PostInsertDto;
import com.example.social_network.models.Dto.Posts.PostShareDto;
import com.example.social_network.models.Dto.Posts.PostUpdateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping(PathResources.Post)
public class PostController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private PostService postService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private VideoViewService videoViewService;

    @PostMapping(value = PathResources.UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Lỗi upload ảnh: " + e.getMessage());
        }
    }

    @GetMapping(PathResources.GetlistPost)
    public Object getPost(@RequestParam(required = false) String id,
                          @RequestParam(required = false) String userId,
                          @RequestParam(required = false) String postId,
                          @RequestParam(defaultValue = "1") int pageIdx,
                          @RequestParam(defaultValue = "100") int pageSize) {
        String viewerId = SecurityUtils.getCurrentUserId();
        return postService.getList(id, userId, postId, viewerId, pageIdx - 1, pageSize);
    }

    // Danh sách user đã chia sẻ (repost) 1 bài viết.
    @GetMapping(PathResources.SHARERS)
    public Object getSharers(@RequestParam String postId) {
        return postService.getSharers(postId);
    }

    // Feed GỢI Ý bằng AI (embedding + độ mới + độ hot). Trả cùng định dạng feed thường.
    @GetMapping(PathResources.RECOMMEND)
    public Object recommend(@RequestParam(defaultValue = "30") int limit) {
        String viewerId = SecurityUtils.getCurrentUserId();
        return recommendationService.recommend(viewerId, limit);
    }

    // REELS: danh sách bài VIDEO người xem được phép thấy (phân trang, cùng format feed).
    @GetMapping(PathResources.REELS)
    public Object getReels(@RequestParam(defaultValue = "1") int pageIdx,
                           @RequestParam(defaultValue = "10") int pageSize) {
        String viewerId = SecurityUtils.getCurrentUserId();
        return postService.getReels(viewerId, pageIdx - 1, pageSize);
    }

    // FE gửi tiến độ xem video (giây đã xem + thời lượng) -> tín hiệu ngầm cho gợi ý.
    @PostMapping(PathResources.VIDEO_VIEW)
    public ResponseEntity<?> recordVideoView(@RequestBody VideoViewRequest req) {
        String userId = SecurityUtils.getCurrentUserId();
        return videoViewService.recordView(req, userId);
    }

    // API 2: tạo bài viết kèm media. id_user lấy từ token (KHÔNG nhận từ request).
    @PostMapping(PathResources.INSERT)
    public ResponseEntity<?> insert(@RequestBody PostInsertDto dto,
                                    HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return postService.insert(dto, userId, request.getRemoteAddr());
    }

    // Share (repost nội bộ) bài viết. id_user (người share) lấy từ token (KHÔNG nhận từ request).
    @PostMapping(PathResources.SHARE)
    public ResponseEntity<?> share(@RequestBody PostShareDto dto,
                                   HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return postService.share(dto, userId, request.getRemoteAddr());
    }

    @PostMapping(PathResources.UPDATE)
    public ResponseEntity<?> update(@RequestBody PostUpdateDto dto,
                                    HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return postService.update(dto, userId, request.getRemoteAddr());
    }

    @DeleteMapping(PathResources.DELETE)
    public ResponseEntity<?> delete(@RequestBody DeleteDto dto,
                                    HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return postService.delete(dto, userId, request.getRemoteAddr());
    }
}
