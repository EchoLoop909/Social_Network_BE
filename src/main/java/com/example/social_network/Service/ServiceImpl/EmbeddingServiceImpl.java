package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Service.EmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gọi Gemini Embedding API (text-embedding-004) qua REST.
 * KHÔNG train gì — chỉ gửi text, nhận về vector đã tính sẵn.
 * Endpoint: POST {endpoint}/{model}:embedContent?key=API_KEY
 * Body:     { "model": "models/{model}", "content": { "parts": [ { "text": "..." } ] } }
 * Response: { "embedding": { "values": [ ...768 số... ] } }
 */
@Service
public class EmbeddingServiceImpl implements EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingServiceImpl.class);

    @Autowired
    private RestTemplate restTemplate; // bean khai báo sẵn trong SecurityConfig

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model:gemini-embedding-001}")
    private String model;

    @Value("${gemini.endpoint:https://generativelanguage.googleapis.com/v1beta/models}")
    private String endpoint;

    @Override
    @SuppressWarnings("unchecked")
    public float[] embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            String url = endpoint + "/" + model + ":embedContent?key=" + apiKey;

            // Dựng body theo đúng format Gemini yêu cầu
            Map<String, Object> content = new HashMap<>();
            content.put("parts", Collections.singletonList(Collections.singletonMap("text", text)));
            Map<String, Object> body = new HashMap<>();
            body.put("model", "models/" + model);
            body.put("content", content);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> respBody = response.getBody();
            if (respBody == null || respBody.get("embedding") == null) {
                logger.warn("Gemini trả về rỗng cho text (len={})", text.length());
                return null;
            }

            Map<String, Object> embedding = (Map<String, Object>) respBody.get("embedding");
            List<Number> values = (List<Number>) embedding.get("values");
            if (values == null || values.isEmpty()) {
                return null;
            }

            float[] vector = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                vector[i] = values.get(i).floatValue();
            }
            return vector;

        } catch (Exception e) {
            // Lỗi embedding KHÔNG được làm hỏng nghiệp vụ chính -> chỉ log, trả null
            logger.error("Lỗi gọi Gemini embedding: {}", e.getMessage());
            return null;
        }
    }
}
