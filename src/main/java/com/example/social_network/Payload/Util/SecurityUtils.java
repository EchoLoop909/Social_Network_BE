package com.example.social_network.Payload.Util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Tiện ích đọc thông tin user hiện tại từ SecurityContext.
 * <p>
 * Token do Keycloak cấp đã được resource server (SecurityConfig) xác thực ở tầng
 * filter trước khi vào Controller — nên chỉ cần lấy id_user từ đây, KHÔNG nhận từ request.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** id_user (= JWT subject / sub) của request hiện tại, hoặc null nếu chưa xác thực. */
    public static String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt) {
            return ((Jwt) auth.getPrincipal()).getSubject();
        }
        return null;
    }
}
