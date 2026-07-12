package com.example.social_network.Controller;

import com.example.social_network.Config.Cloudinary.CloudinaryService;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Payload.Util.SecurityUtils;
import com.example.social_network.Service.PostService;
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

    @PostMapping(value = PathResources.UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Lỗi upload ảnh: " + e.getMessage());
        }
    }

    @GetMapping(PathResources.Post)
    public Object getPost(@RequestParam(required = false) String id,
                          @RequestParam(required = false) String userId,
                          @RequestParam(required = false) String postId,
                          @RequestParam(defaultValue = "1") int pageIdx,
                          @RequestParam(defaultValue = "100") int pageSize) {
        return postService.getList(id, userId, postId, pageIdx - 1, pageSize);
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

    @PostMapping(PathResources.DELETE)
    public ResponseEntity<?> delete(@RequestBody DeleteDto dto,
                                    HttpServletRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return postService.delete(dto, userId, request.getRemoteAddr());
    }
}
