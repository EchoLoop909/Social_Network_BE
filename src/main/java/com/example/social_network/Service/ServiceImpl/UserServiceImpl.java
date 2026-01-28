package com.example.social_network.Service.ServiceImpl;

import com.cloudinary.Cloudinary;
import com.example.social_network.Payload.Response.Message;
import com.example.social_network.Payload.Response.MessageResponse;
import com.example.social_network.Payload.Util.Status;
import com.example.social_network.Repository.StatususerRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.models.CreateUserRequestDTO;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.Service.UserService;
import com.example.social_network.models.Dto.LoginRequest;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Entity.Statususer;
import com.example.social_network.models.Entity.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StatususerRepository statusRepository;

    @Value("${keycloak.auth-server-url}")
    String serverUrl;

    @Value("${keycloak.realm}")
    String realm;

    @Value("${idp.client-id}")
    String clientId;

    @Value("${idp.client-secret}")
    String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();


    public String getAdminToken() {
        String url = serverUrl + "/realms/" + realm +"/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<?> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        return response.getBody().get("access_token").toString();
    }

    @Override
    public ResponseEntity<?> createUser(CreateUserRequestDTO req, Jwt jwt) {
        try{
//            if (req.getStatus() == null) {
//                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(0, "Status is required"));
//            }
//
//            Statususer status = statusRepository.findById(req.getStatus())
//                    .orElseThrow(() -> new RuntimeException("Status not found"));
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.setBearerAuth(getAdminToken());
//            // 3. Body tạo user (đúng format Keycloak)
//            Map<String, Object> body = new HashMap<>();
//            body.put("username", req.getUsername());
//            body.put("email", req.getEmail());
//            body.put("enabled", true);
//
//            List<Map<String, Object>> credentials = new ArrayList<>();
//            Map<String, Object> password = new HashMap<>();
//            password.put("type", "password");
//            password.put("value", req.getPassword());
//            password.put("temporary", false);
//            credentials.add(password);
//
//            body.put("credentials", credentials);
//
//            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
//
//            // 4. Call Keycloak Admin API
//            String url = serverUrl + "/admin/realms/" + realm + "/users";
//
//            ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);
//            if (response.getStatusCode() != HttpStatus.CREATED) {
//                return ResponseEntity
//                        .status(response.getStatusCode())
//                        .body("Create user on Keycloak failed");
//            }
//
//            User user = new User();
//            user.setUsername(req.getUsername());
//            user.setEmail(req.getEmail());
//            user.setName(req.getName());
//            user.setSurname(req.getSurname());
//            user.setDescription(req.getDescription());
//            user.setPhoto(req.getPhoto());
//
//            user.setStatus(status);
//            user.setIsChecked(false);
//            user.setCreationDate(LocalDateTime.now());
//            userRepository.save(user);
//
//            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "INSERT SUCCESSFULLY"));
            String keycloackId = jwt.getClaimAsString("sub");//Lay id keycloack
            Optional<User> user = userRepository.findByKeycloakId(keycloackId);
            if(user == null){
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(0, "User is required"));
            }else {

                Statususer status = statusRepository
                        .findById(req.getStatus())
                        .orElseThrow(() -> new RuntimeException("Status không tồn tại"));

                User newUser = new User();
                newUser.setUsername(req.getUsername());
                newUser.setEmail(req.getEmail());
                newUser.setName(req.getName());
                newUser.setSurname(req.getSurname());
                newUser.setDescription(req.getDescription());
                newUser.setPhoto(req.getPhoto());
                newUser.setStatus(status);
                newUser.setFirstname(req.getFirstname());
                newUser.setLastname(req.getLastname());
                newUser.setKeycloakId(keycloackId);

                userRepository.save(newUser);

                return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "INSERT SUCCESSFULLY"));
            }

        } catch (Exception e) {
            logger.error("Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("System Error: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> login(LoginRequest request) {
        try {
            String url = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("username", request.getUsername());
            body.add("password", request.getPassword());

            HttpEntity<MultiValueMap<String, String>> entity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    entity,
                    Map.class
            );

            Map<String, Object> res = response.getBody();

            if (res == null || !res.containsKey("access_token")) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseMess(1, "LOGIN FAILED"));
            }

            return ResponseEntity.ok(res);

        } catch (HttpClientErrorException.Unauthorized e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseMess(1, "INVALID USERNAME OR PASSWORD"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMess(1, "SYSTEM ERROR"));
        }
    }

    @Override
    public Object getUser(String userId, int pageIdx, int pageSize) {
        try {
            Pageable paging = PageRequest.of(pageIdx, pageSize);
            Page<User> usersPage;

            if (userId == null || userId.isEmpty()) {
                usersPage = userRepository.findAll(paging);
            } else {
                usersPage = userRepository.findUser(userId, paging);
            }

            List<User> results = usersPage.getContent();

            logger.info("get list user success page: " + pageIdx + " size: " + pageSize);
            return ResponseHelper.getResponses(
                    results,
                    usersPage.getTotalElements(),
                    usersPage.getTotalPages(),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            logger.error("error: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(Status.EXCEPTION, Message.EXCEPTION, false, true));
        }
    }
}
