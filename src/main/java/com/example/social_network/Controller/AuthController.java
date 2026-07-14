package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.LoginRequest;
import com.example.social_network.Payload.Request.RefreshTokenRequest;
import com.example.social_network.Payload.Request.RegisterRequest;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Payload.Util.SecurityUtils;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(PathResources.Auth)
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping(PathResources.GetUser)
    public Object getUser(@RequestParam(required = false) String userId,
                          @RequestParam(defaultValue = "1") int pageIdx,
                          @RequestParam(defaultValue = "100") int pageSize){
        // viewerId từ token để ẩn user đã bị chặn / chặn mình
        String viewerId = SecurityUtils.getCurrentUserId();
        return userService.getUser(userId, viewerId, pageIdx -1, pageSize);
    }

    @PostMapping(PathResources.UpdaloadImg)
    public ResponseEntity<?> uploadImg(@RequestParam String username,
                                       @RequestParam String url){
        return userService.uploadImg(username,url);
    }

    @PutMapping(PathResources.UpdateUser)
    public ResponseEntity<?> updateUser(@RequestParam String username,
                                       @RequestParam String url){
        String userId = SecurityUtils.getCurrentUserId();
        return userService.uploadImg(username,url);
    }

    /** Đăng ký tài khoản mới. */
    @PostMapping(PathResources.REGISTER)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
                                      BindingResult bindingResult) {
        // B1 - validate định dạng đầu vào
        if (bindingResult.hasErrors()) {
            return ResponseHelper.getErrorResponse(bindingResult, HttpStatus.BAD_REQUEST);
        }
        return (ResponseEntity<?>) userService.register(request);
    }

    /** Đăng nhập: FE gọi vào đây, backend proxy tới Keycloak. */
    @PostMapping(PathResources.LOGIN)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseHelper.getErrorResponse(bindingResult, HttpStatus.BAD_REQUEST);
        }
        return (ResponseEntity<?>) userService.login(request);
    }

    /** Đăng xuất: thu hồi phiên tại Keycloak. */
    @PostMapping(PathResources.LOGOUT)
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequest request,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseHelper.getErrorResponse(bindingResult, HttpStatus.BAD_REQUEST);
        }
        return (ResponseEntity<?>) userService.logout(request.getRefreshToken());
    }
}
