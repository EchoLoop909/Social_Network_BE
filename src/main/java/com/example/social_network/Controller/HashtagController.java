package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.HashtagRequest;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Service.HashtagService;
import com.example.social_network.models.Dto.DeleteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(PathResources.HASHTAG)
public class HashtagController {

    @Autowired
    private HashtagService hashtagService;

    @PostMapping(PathResources.INSERT)
    public ResponseEntity<?> create(@RequestBody HashtagRequest dto, HttpServletRequest request) {
        return hashtagService.create(dto, request.getRemoteAddr());
    }

    @GetMapping(PathResources.LIST)
    public Object getList(@RequestParam(required = false) String keyword,
                          @RequestParam(defaultValue = "1") int pageIdx,
                          @RequestParam(defaultValue = "20") int pageSize) {
        return hashtagService.getList(keyword, pageIdx - 1, pageSize);
    }

    @DeleteMapping(PathResources.DELETE)
    public ResponseEntity<?> delete(@RequestBody DeleteDto dto, HttpServletRequest request) {
        return hashtagService.delete(dto, request.getRemoteAddr());
    }
}
