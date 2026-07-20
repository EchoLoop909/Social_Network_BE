package com.example.social_network.Config;

import com.example.social_network.Payload.Util.PathResources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000", "http://localhost:3001", "http://localhost:3002",
                "http://localhost:1234", "http://localhost:8080"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Public: /auth/register, /auth/login, /auth/logout, swagger.
     * Còn lại yêu cầu JWT hợp lệ do Keycloak cấp (OAuth2 resource server).
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .antMatchers(HttpMethod.POST,
                                PathResources.Auth + PathResources.REGISTER,
                                PathResources.Auth + PathResources.LOGIN,
                                PathResources.Auth + PathResources.LOGOUT).permitAll()
                        .antMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**").permitAll()
                        // WebSocket handshake (SockJS) — cho phép để client kết nối nhận tin real-time
                        .antMatchers("/ws/**").permitAll()
                        // TẠM THỜI (Chặng 1) — test Gemini embedding không cần token; gỡ sau khi verify
                        .antMatchers("/ai/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    /** Dùng để Backend gọi Admin REST API của Keycloak. */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
