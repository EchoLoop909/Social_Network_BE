package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.LoginRequest;
import com.example.social_network.Payload.Request.RefreshTokenRequest;
import com.example.social_network.Payload.Request.RegisterRequest;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
        return userService.getUser(userId,pageIdx -1,pageSize);
    }

    @PostMapping(PathResources.UpdaloadImg)
    public ResponseEntity<?> uploadImg(@RequestParam String username,
                                       @RequestParam String url){
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

    /** Kích hoạt tài khoản sau khi xác thực email (webhook Keycloak hoặc gọi thủ công). */
    @PostMapping(PathResources.ACTIVATE + "/{userId}")
    public ResponseEntity<?> activate(@PathVariable String userId) {
        return (ResponseEntity<?>) userService.activate(userId);
    }

    /**
     * Thông tin user hiện tại (yêu cầu JWT). Frontend gọi ngay sau khi đăng nhập.
     * Đọc claim email_verified trong token để sync trạng thái ACTIVE và chặn tài khoản SUSPENDED.
     */
    @GetMapping(PathResources.ME)
    public ResponseEntity<?> me(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject(); // = sub = id_user
        Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");
        return (ResponseEntity<?>) userService.getCurrentUser(userId, Boolean.TRUE.equals(emailVerified));
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

    /** Làm mới access_token bằng refresh_token. */
    @PostMapping(PathResources.REFRESH)
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request,
                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseHelper.getErrorResponse(bindingResult, HttpStatus.BAD_REQUEST);
        }
        return (ResponseEntity<?>) userService.refresh(request.getRefreshToken());
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
