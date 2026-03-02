package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.LikeRequestDto;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(PathResources.LIKE)
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping(PathResources.LIKEPOST)
    public ResponseEntity<?> toggleLike(@RequestBody LikeRequestDto dto,
                                        HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return likeService.toggleLike(dto, ip);
    }
}