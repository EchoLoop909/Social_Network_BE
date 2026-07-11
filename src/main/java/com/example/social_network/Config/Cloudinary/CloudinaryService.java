package com.example.social_network.Config.Cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) throws IOException {
        // upload(file_bytes, options)
        // ObjectUtils.asMap để thêm các tùy chọn (folder, public_id,...)
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", "social_media_app")); // "folder" là thư mục trên Cloudinary

        // Trả về url ảnh (secure_url là link https)
        return uploadResult.get("secure_url").toString();
    }

    /**
     * Upload 1 file media (ảnh HOẶC video) lên Cloudinary.
     * Dùng resource_type = "auto" để Cloudinary tự nhận diện ảnh/video.
     * Cloudinary trả sẵn kích thước ảnh/video nên ta lấy luôn để lưu vào bảng post_media,
     * không cần FE tự đo hay tự truyền.
     * Trả về Map:
     *   { url, type: IMAGE|VIDEO, width, height, durationSec }
     *   - width/height: px (Integer, có thể null nếu Cloudinary không trả)
     *   - durationSec : thời lượng video tính bằng giây (null nếu là ảnh)
     */
    public Map<String, Object> uploadMedia(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "social_media_app",
                        "resource_type", "auto")); // auto = tự nhận image hoặc video

        Map<String, Object> media = new HashMap<>();
        media.put("url", uploadResult.get("secure_url").toString());
        // Cloudinary trả resource_type = "image" | "video" | "raw"
        String resourceType = String.valueOf(uploadResult.get("resource_type"));
        media.put("type", "video".equalsIgnoreCase(resourceType) ? "VIDEO" : "IMAGE");

        // Kích thước Cloudinary trả về (dùng để đổ vào cột width/height của post_media)
        media.put("width", uploadResult.get("width"));   // Integer, có thể null
        media.put("height", uploadResult.get("height")); // Integer, có thể null

        // "duration" chỉ có với video (đơn vị giây, kiểu Double); ảnh -> null
        Object duration = uploadResult.get("duration");
        media.put("durationSec",
                duration == null ? null : (int) Math.round(Double.parseDouble(duration.toString())));

        return media;
    }
}