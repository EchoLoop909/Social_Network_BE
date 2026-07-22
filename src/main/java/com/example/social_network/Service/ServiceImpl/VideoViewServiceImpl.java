package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Repository.PostRepository;
import com.example.social_network.Repository.UserRepository;
import com.example.social_network.Repository.VideoViewRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.VideoViewService;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Dto.VideoViewRequest;
import com.example.social_network.models.Entity.Post;
import com.example.social_network.models.Entity.User;
import com.example.social_network.models.Entity.VideoView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class VideoViewServiceImpl implements VideoViewService {

    private static final Logger logger = LoggerFactory.getLogger(VideoViewServiceImpl.class);

    @Autowired
    private VideoViewRepository videoViewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Override
    public ResponseEntity<?> recordView(VideoViewRequest req, String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.UNAUTHORIZED, new ResponseMess(1, "Chưa xác thực người dùng"));
            }
            if (req == null || req.getPostId() == null || req.getPostId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "postId is required"));
            }

            String postId = req.getPostId().trim();
            int watched = req.getWatchedSeconds() == null ? 0 : Math.max(0, req.getWatchedSeconds());
            int duration = req.getDuration() == null ? 0 : Math.max(0, req.getDuration());

            // Tìm bản ghi cũ (nếu có) -> cập nhật; chưa có -> tạo mới
            VideoView vv = videoViewRepository.findByUser_IdAndPost_Id(userId, postId).orElse(null);
            if (vv == null) {
                User user = userRepository.findById(userId).orElse(null);
                Post post = postRepository.findById(postId).orElse(null);
                if (user == null || post == null) {
                    return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "User hoặc post không tồn tại"));
                }
                vv = new VideoView();
                vv.setUser(user);
                vv.setPost(post);
                vv.setWatchedSeconds(0);
            }

            // Giữ số giây xem XA NHẤT (xem lại nhiều lần thì lấy lần xa nhất)
            int newWatched = Math.max(vv.getWatchedSeconds() == null ? 0 : vv.getWatchedSeconds(), watched);
            vv.setWatchedSeconds(newWatched);
            if (duration > 0) vv.setDurationSec(duration);

            int dur = vv.getDurationSec() == null ? 0 : vv.getDurationSec();
            double ratio = dur > 0 ? Math.min(1.0, (double) newWatched / dur) : 0.0; // cap ở 1.0
            vv.setWatchedRatio(ratio);

            videoViewRepository.save(vv);
            logger.info("Video view: user {} xem post {} = {}s/{}s (ratio {})", userId, postId, newWatched, dur, String.format("%.2f", ratio));
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "RECORD VIEW SUCCESS"));

        } catch (Exception e) {
            logger.error("Error in recordView: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }
}
