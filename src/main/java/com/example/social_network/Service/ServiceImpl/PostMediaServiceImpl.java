package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Config.Cloudinary.CloudinaryService;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.PostMediaService;
import com.example.social_network.models.Dto.ResponseMess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PostMediaServiceImpl implements PostMediaService {
    private static final Logger logger = LoggerFactory.getLogger(PostMediaServiceImpl.class);

    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public ResponseEntity<?> upload(MultipartFile[] files) {
        try {
            if (files == null || files.length == 0) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                        new ResponseMess(1, "Vui lòng chọn ít nhất một tệp ảnh hoặc video"));
            }

            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                // file.isEmpty() = true khi tệp gửi lên không có nội dung (0 byte / không đính tệp)
                if (file == null || file.isEmpty()) {
                    return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST,
                            new ResponseMess(1, "Tệp \"" + (file != null ? file.getOriginalFilename() : "")
                                    + "\" không có nội dung, vui lòng chọn tệp khác"));
                }
                // Tái sử dụng logic upload Cloudinary có sẵn (url, type, width, height, durationSec)
                Map<String, Object> media = cloudinaryService.uploadMedia(file);
                // order = vị trí trong mảng files (KHÔNG nhận từ FE)
                media.put("order", i);
                result.add(media);
            }

            logger.info("Upload {} media file(s) to Cloudinary success", result.size());
            // Trả list trong trường Object để FE đọc từ res.data.Object
            return ResponseHelper.buildResponse(result, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in upload media: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR,
                    new ResponseMess(1, "Tải media lên thất bại: " + e.getMessage()));
        }
    }
}
