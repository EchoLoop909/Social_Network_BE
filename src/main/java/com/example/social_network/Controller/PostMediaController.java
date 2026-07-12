package com.example.social_network.Controller;

import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Service.PostMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;

@RestController
@RequestMapping(PathResources.POST_MEDIA)
public class PostMediaController {

    @Autowired
    private PostMediaService postMediaService;

    // API 1: upload ảnh/video lên Cloudinary, KHÔNG lưu DB, trả về list { url, type, ... , order }
    @PostMapping(value = PathResources.UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestPart("files") MultipartFile[] files) {
        return postMediaService.upload(files);
    }
}
