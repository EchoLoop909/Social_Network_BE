package com.example.social_network.Controller;

import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(PathResources.Auth)
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping(PathResources.GetUser)
    public Object getUser(@RequestParam(required = false) String userId,
                          @RequestParam(defaultValue = "1") int pageIdx,
                          @RequestParam(defaultValue = "100") int pageSize){
        return userService.getUser(userId,pageIdx -1,pageSize);
    }

    @PostMapping(PathResources.UpdaloadImg)
    public ResponseEntity<?> uploadImg(@RequestParam String username,
                                       @RequestParam String url){
        return userService.uploadImg(username,url);
    }
}
