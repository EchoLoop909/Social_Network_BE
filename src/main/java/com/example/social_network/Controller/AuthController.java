package com.example.social_network.Controller;

import com.example.social_network.Payload.Request.LoginRequest;
import com.example.social_network.Payload.Request.RefreshTokenRequest;
import com.example.social_network.Payload.Request.RegisterRequest;
import com.example.social_network.Payload.Util.PathResources;
import com.example.social_network.Payload.Util.SecurityUtils;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.UserService;
import com.example.social_network.models.Dto.DeleteDto;
import com.example.social_network.models.Dto.UserUpdateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping(PathResources.Auth)
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping(PathResources.GetUser)
    public Object getUser(@RequestParam(required = false) String userId,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(defaultValue = "1") int pageIdx,
                          @RequestParam(defaultValue = "100") int pageSize){
        // viewerId từ token để ẩn user đã bị chặn / chặn mình
        String viewerId = SecurityUtils.getCurrentUserId();
        return userService.getUser(userId, keyword, viewerId, pageIdx -1, pageSize);
    }

    // Đồng bộ toàn bộ user trong DB lên Keycloak (cần đăng nhập). Chạy 1 lần để tạo tài khoản còn thiếu.
    @PostMapping(PathResources.SYNC_KEYCLOAK)
    public ResponseEntity<?> syncKeycloak() {
        return userService.syncUsersToKeycloak();
    }

    @PostMapping(PathResources.UpdaloadImg)
    public ResponseEntity<?> uploadImg(@RequestParam String username,
                                       @RequestParam String url){
        return userService.uploadImg(username,url);
    }

    // Cập nhật hồ sơ cá nhân (gồm cả is_private).
    // Mặc định lấy user từ token; có thể truyền param userId để test trên Swagger.
    @PutMapping(PathResources.UpdateUser)
    public ResponseEntity<?> updateUser(@RequestParam(required = false) String userId,
                                        @RequestBody UserUpdateDto dto,
                                        HttpServletRequest request){
        String effectiveId = (userId != null && !userId.trim().isEmpty())
                ? userId.trim()
                : SecurityUtils.getCurrentUserId();
        return userService.updateUser(effectiveId, dto, request.getRemoteAddr());
    }

    // Xóa mềm tài khoản. Ưu tiên param userId (tiện test Swagger), nếu không có thì lấy id trong body.
    @DeleteMapping(PathResources.DELETE)
    public ResponseEntity<?> deleteUser(@RequestParam(required = false) String userId,
                                        @RequestBody(required = false) DeleteDto dto,
                                        HttpServletRequest request){
        if (userId != null && !userId.trim().isEmpty()) {
            if (dto == null) dto = new DeleteDto();
            dto.setId(userId.trim());
        }
        return userService.deleteUser(dto, request.getRemoteAddr());
    }

    /** Đăng ký tài khoản mới. */
    @PostMapping(PathResources.REGISTER)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
                                      BindingResult bindingResult) {
        // B1 - validate định dạng đầu vào
        if (bindingResult.hasErrors()) {
            return ResponseHelper.getErrorResponse(bindingResult, HttpStatus.BAD_REQUEST);
        }
        return userService.register(request);
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
