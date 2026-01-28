package com.example.social_network.Config.Cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
}