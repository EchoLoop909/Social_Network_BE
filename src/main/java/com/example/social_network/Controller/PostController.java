package com.example.social_network.Controller;

import com.example.social_network.Config.Cloudinary.CloudinaryService;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(PathResources.IMAGES)
public class PostController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private PostService postService;

    @PostMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Lỗi upload ảnh: " + e.getMessage());
        }
    }

    @GetMapping(PathResources.Post)
    public Object getPost(@RequestParam (required = false) String id,
                          @RequestParam (required = false) String userId,
                          @RequestParam (required = false) String postId,
                          @RequestParam(defaultValue = "1") int pageIdx,
                          @RequestParam(defaultValue = "100") int pageSize){
        return postService.getList(id,userId,postId,pageIdx -1,pageSize);
    }
}
