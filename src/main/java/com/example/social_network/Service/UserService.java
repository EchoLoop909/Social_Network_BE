package com.example.social_network.Service;

import com.example.social_network.models.CreateUserRequestDTO;
import com.example.social_network.models.Dto.LoginRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<?> createUser(CreateUserRequestDTO req, Jwt jwt);

    ResponseEntity<?> login(LoginRequest request);

    Object getUser(String userId, int pageIdx, int pageSize);
}
