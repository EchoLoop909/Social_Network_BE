package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.PostHashtagRequest;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Service.PostHashtagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(PathResources.POST_HASHTAG)
public class PostHashtagController {

    @Autowired
    private PostHashtagService postHashtagService;

    @PostMapping(PathResources.INSERT)
    public ResponseEntity<?> attach(@RequestBody PostHashtagRequest dto, HttpServletRequest request) {
        return postHashtagService.attach(dto, request.getRemoteAddr());
    }

    @GetMapping(PathResources.LIST)
    public Object getByPost(@RequestParam String postId) {
        return postHashtagService.getByPost(postId);
    }

    @DeleteMapping(PathResources.DELETE)
    public ResponseEntity<?> detach(@RequestBody PostHashtagRequest dto, HttpServletRequest request) {
        return postHashtagService.detach(dto, request.getRemoteAddr());
    }
}
