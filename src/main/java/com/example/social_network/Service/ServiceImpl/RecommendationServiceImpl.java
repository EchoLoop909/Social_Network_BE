package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Repository.BookmarkRepository;
import com.example.social_network.Repository.LikeRepository;
import com.example.social_network.Repository.PostEmbeddingRepository;
import com.example.social_network.Repository.PostRepository;
import com.example.social_network.Repository.VideoViewRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.RecommendationService;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Entity.Post;
import com.example.social_network.models.Entity.PostEmbedding;
import com.example.social_network.models.Enum.LikeTargetType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostEmbeddingRepository postEmbeddingRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private VideoViewRepository videoViewRepository;

    @Value("${recommend.candidate-limit:300}")
    private int candidateLimit;

    @Value("${recommend.weight.similarity:0.6}")
    private double wSim;

    @Value("${recommend.weight.recency:0.2}")
    private double wRec;

    @Value("${recommend.weight.popularity:0.2}")
    private double wPop;

    @Override
    public Object recommend(String viewerId, int limit) {
        try {
            if (viewerId == null || viewerId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.UNAUTHORIZED,
                        new ResponseMess(1, "Chưa xác thực người dùng"));
            }

            // 1. ỨNG VIÊN: bài người xem được phép thấy (findFeed đã lọc riêng tư + block trong SQL),
            //    lấy tối đa candidateLimit bài mới nhất làm tập ứng viên.
            List<Post> candidates = postRepository
                    .findFeed(viewerId, PageRequest.of(0, candidateLimit)).getContent();
            if (candidates.isEmpty()) {
                return ResponseHelper.getResponses(new ArrayList<Post>(), 0L, 0, HttpStatus.OK);
            }

            // 2. Tương tác của user kèm TRỌNG SỐ: save=1.0, like=0.8, xem video=theo tỉ lệ xem.
            Set<String> likedIds = new HashSet<>(
                    likeRepository.findTargetIdsByUserAndType(viewerId, LikeTargetType.POST));
            Set<String> savedIds = new HashSet<>(bookmarkRepository.findPostIds(viewerId));
            List<Object[]> views = videoViewRepository.findViewsByUser(viewerId); // [postId, watchedRatio]

            Map<String, Double> weights = new HashMap<>();
            for (String id : savedIds) weights.merge(id, 1.0, Math::max);
            for (String id : likedIds) weights.merge(id, 0.8, Math::max);
            for (Object[] row : views) {
                String pid = (String) row[0];
                double ratio = row[1] == null ? 0.0 : ((Number) row[1]).doubleValue();
                double w = viewWeight(ratio);           // xem đủ lâu mới tính
                if (w > 0) weights.merge(pid, w, Math::max);
            }

            // Loại khỏi kết quả: bài đã like/save + video đã xem gần hết (>=0.9) -> khỏi gợi ý lại.
            Set<String> interactedIds = new HashSet<>();
            interactedIds.addAll(likedIds);
            interactedIds.addAll(savedIds);
            for (Object[] row : views) {
                double ratio = row[1] == null ? 0.0 : ((Number) row[1]).doubleValue();
                if (ratio >= 0.9) interactedIds.add((String) row[0]);
            }

            // 3. VECTOR SỞ THÍCH = trung bình CÓ TRỌNG SỐ (null nếu chưa có tín hiệu nào)
            float[] interest = buildInterestVector(weights);

            // 4. Chuẩn bị chuẩn hóa recency & popularity trên tập ứng viên
            long minTime = Long.MAX_VALUE, maxTime = Long.MIN_VALUE;
            int maxPop = 0;
            for (Post p : candidates) {
                long t = p.getCreateTime() != null ? p.getCreateTime().toEpochSecond(ZoneOffset.UTC) : 0L;
                minTime = Math.min(minTime, t);
                maxTime = Math.max(maxTime, t);
                maxPop = Math.max(maxPop, safe(p.getReactionCount()) + safe(p.getCommentCount()));
            }
            final long minTimeF = minTime;
            final long spanTime = Math.max(1L, maxTime - minTime);
            final int maxPopF = Math.max(1, maxPop);

            // 5. Nạp embedding của các bài ứng viên
            Map<String, float[]> candVecs = loadVectors(
                    candidates.stream().map(Post::getId).collect(Collectors.toList()));

            // 6. PASS 1: tính recency/popularity + cosine THÔ cho từng ứng viên (bỏ bài của mình + đã tương tác).
            //    Cosine của các caption ngắn tiếng Việt nằm dải hẹp -> lưu min/max để chuẩn hóa min-max ở pass 2.
            List<Cand> cands = new ArrayList<>();
            double minCos = Double.MAX_VALUE, maxCos = -Double.MAX_VALUE;
            for (Post p : candidates) {
                if (p.getUser() != null && viewerId.equals(p.getUser().getId())) continue;
                if (interactedIds.contains(p.getId())) continue;

                long t = p.getCreateTime() != null ? p.getCreateTime().toEpochSecond(ZoneOffset.UTC) : minTimeF;
                double recNorm = (double) (t - minTimeF) / spanTime;                                  // [0,1]
                double popNorm = (double) (safe(p.getReactionCount()) + safe(p.getCommentCount())) / maxPopF; // [0,1]

                Double rawCos = null;
                if (interest != null) {
                    float[] cv = candVecs.get(p.getId());
                    if (cv != null) {
                        rawCos = cosine(interest, cv);       // [-1,1]
                        if (rawCos < minCos) minCos = rawCos;
                        if (rawCos > maxCos) maxCos = rawCos;
                    }
                }
                cands.add(new Cand(p, recNorm, popNorm, rawCos));
            }

            // 7. PASS 2: chuẩn hóa min-max độ giống (kéo giãn dải hẹp -> chủ đề nổi rõ) + chấm điểm cuối.
            double cosSpan = (maxCos > minCos) ? (maxCos - minCos) : 1.0;
            List<Scored> scored = new ArrayList<>();
            for (Cand c : cands) {
                double score;
                if (interest != null) {
                    // TẦNG 1: cá nhân hóa. Bài chưa có vector -> simNorm = 0 (chưa embed thì chưa match được).
                    double simNorm = (c.rawCos == null) ? 0.0 : (c.rawCos - minCos) / cosSpan;
                    score = wSim * simNorm + wRec * c.recNorm + wPop * c.popNorm;
                } else {
                    // TẦNG 3 (cold-start): chưa tương tác gì -> chỉ hot + mới.
                    score = 0.5 * c.recNorm + 0.5 * c.popNorm;
                }
                scored.add(new Scored(c.post, score));
            }

            // Xếp theo điểm giảm dần, lấy top-N
            scored.sort((a, b) -> Double.compare(b.score, a.score));
            int n = Math.max(1, limit);
            List<Post> result = scored.stream().limit(n).map(s -> s.post).collect(Collectors.toList());

            logger.info("Recommend cho {}: {} ứng viên -> trả {} bài (cá nhân hóa={})",
                    viewerId, candidates.size(), result.size(), interest != null);
            return ResponseHelper.getResponses(result, (long) result.size(), 1, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error in recommend: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    /**
     * Trung bình CÓ TRỌNG SỐ các vector -> "vector sở thích".
     * weights: postId -> trọng số (save=1.0, like=0.8, xem video theo tỉ lệ). null nếu không có tín hiệu.
     */
    private float[] buildInterestVector(Map<String, Double> weights) {
        if (weights.isEmpty()) return null;
        Map<String, float[]> vecs = loadVectors(new ArrayList<>(weights.keySet()));
        if (vecs.isEmpty()) return null;

        int dim = -1;
        float[] sum = null;
        double wsum = 0.0;
        for (Map.Entry<String, Double> e : weights.entrySet()) {
            float[] v = vecs.get(e.getKey());
            double w = e.getValue() == null ? 0.0 : e.getValue();
            if (v == null || w <= 0) continue;
            if (sum == null) {
                dim = v.length;
                sum = new float[dim];
            }
            if (v.length != dim) continue;
            for (int i = 0; i < dim; i++)
                sum[i] += (float) (w * v[i]);   // cộng có trọng số
            wsum += w;
        }
        if (sum == null || wsum <= 0) return null;
        for (int i = 0; i < dim; i++) sum[i] /= wsum;   // chia TỔNG trọng số
        return sum;
    }

    /** Đổi tỉ lệ xem video -> trọng số (xem càng nhiều càng nặng; lướt ngay <30% = bỏ qua). */
    private static double viewWeight(double ratio) {
        if (ratio >= 0.9) return 1.2;   // xem gần hết / xem lại -> mê
        if (ratio >= 0.6) return 0.8;   // xem quá nửa -> thích
        if (ratio >= 0.3) return 0.3;   // xem 1 phần -> hơi quan tâm
        return 0.0;                     // lướt ngay -> bỏ qua
    }

    /** Nạp vector (parse JSON) cho danh sách id bài. */
    private Map<String, float[]> loadVectors(List<String> ids) {
        Map<String, float[]> map = new HashMap<>();
        if (ids == null || ids.isEmpty()) return map;
        for (PostEmbedding pe : postEmbeddingRepository.findAllById(ids)) {
            try {
                float[] v = MAPPER.readValue(pe.getVector(), float[].class);
                map.put(pe.getIdPost(), v);
            } catch (Exception ex) {
                logger.warn("Không parse được vector của {}: {}", pe.getIdPost(), ex.getMessage());
            }
        }
        return map;
    }

    /** Cosine similarity giữa 2 vector cùng chiều. */
    private static double cosine(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) return 0.0;
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        if (na == 0 || nb == 0) return 0.0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    private static int safe(Integer x) {
        return x == null ? 0 : x;
    }

    private static class Scored {
        final Post post;
        final double score;
        Scored(Post post, double score) {
            this.post = post;
            this.score = score;
        }
    }

    /** Ứng viên kèm điểm thành phần thô (cosine chưa chuẩn hóa) để min-max ở pass 2. */
    private static class Cand {
        final Post post;
        final double recNorm;
        final double popNorm;
        final Double rawCos; // null nếu bài chưa có vector hoặc chưa có sở thích
        Cand(Post post, double recNorm, double popNorm, Double rawCos) {
            this.post = post;
            this.recNorm = recNorm;
            this.popNorm = popNorm;
            this.rawCos = rawCos;
        }
    }
}
