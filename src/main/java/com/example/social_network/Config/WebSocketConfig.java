package com.example.social_network.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Cấu hình endpoint cho client kết nối tới
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Mở CORS để client gọi được
                .withSockJS(); // Fallback phòng trường hợp trình duyệt không hỗ trợ chuẩn WebSocket
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Tiền tố cho các tin nhắn từ server gửi về client
        registry.enableSimpleBroker("/topic", "/queue");

        // Tiền tố cho các request từ client gửi lên server
        registry.setApplicationDestinationPrefixes("/app");
    }
}