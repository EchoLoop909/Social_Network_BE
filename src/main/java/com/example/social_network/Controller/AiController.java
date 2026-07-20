package com.example.social_network.Controller;

import com.example.social_network.Repository.PostEmbeddingRepository;
import com.example.social_network.Service.EmbeddingService;
import com.example.social_network.Service.PostEmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * TẠM THỜI (Chặng 1) — chỉ để kiểm tra Gemini embedding hoạt động.
 * Sau khi verify xong sẽ xóa controller này.
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private PostEmbeddingService postEmbeddingService;

    @Autowired
    private PostEmbeddingRepository postEmbeddingRepository;

    // GET /ai/embed-test?text=xin chào
    @GetMapping("/embed-test")
    public Object embedTest(@RequestParam String text) {
        float[] v = embeddingService.embed(text);
        Map<String, Object> r = new HashMap<>();
        r.put("input", text);
        r.put("dim", v == null ? 0 : v.length);            // kỳ vọng 3072 (gemini-embedding-001)
        r.put("first5", v == null ? null : Arrays.copyOf(v, Math.min(5, v.length)));
        r.put("ok", v != null);
        return r;
    }

    // POST /ai/backfill-embeddings?delayMs=400 — chạy nền embed toàn bộ bài chưa có vector.
    // Xem tiến độ ở log console. Idempotent: gọi lại chỉ bù bài còn thiếu.
    @PostMapping("/backfill-embeddings")
    public Object backfill(@RequestParam(defaultValue = "400") int delayMs) {
        new Thread(() -> postEmbeddingService.backfillMissing(delayMs), "embed-backfill").start();
        Map<String, Object> r = new HashMap<>();
        r.put("started", true);
        r.put("delayMs", delayMs);
        r.put("note", "Xem log console để theo dõi tiến độ; gọi GET /ai/embedding-count để đếm.");
        return r;
    }

    // GET /ai/embedding-count — đếm số bài đã có vector.
    @GetMapping("/embedding-count")
    public Object embeddingCount() {
        Map<String, Object> r = new HashMap<>();
        r.put("count", postEmbeddingRepository.count());
        return r;
    }
}
