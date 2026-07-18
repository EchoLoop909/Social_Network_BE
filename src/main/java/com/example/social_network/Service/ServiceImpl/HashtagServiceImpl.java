package com.example.social_network.Service.ServiceImpl;

import com.example.social_network.Payload.Request.HashtagRequest;
import com.example.social_network.Repository.HashtagRepository;
import com.example.social_network.ResHelper.ResponseHelper;
import com.example.social_network.Service.HashtagService;
import com.example.social_network.models.Dto.DeleteDto;
import com.example.social_network.models.Dto.ResponseMess;
import com.example.social_network.models.Entity.Hashtag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class HashtagServiceImpl implements HashtagService {
    private static final Logger logger = LoggerFactory.getLogger(HashtagServiceImpl.class);

    @Autowired
    private HashtagRepository hashtagRepository;

    @Override
    public ResponseEntity<?> create(HashtagRequest dto, String ip) {
        try {
            if (dto == null || dto.getName() == null || dto.getName().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "name is required"));
            }
            String name = dto.getName().trim().replaceFirst("^#", "").toLowerCase();
            if (name.isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "Tên hashtag không hợp lệ"));
            }
            if (name.length() > 100) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "Tên hashtag tối đa 100 ký tự"));
            }

            Optional<Hashtag> existed = hashtagRepository.findByName(name);
            if (existed.isPresent()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "HASHTAG ALREADY EXISTS"));
            }

            Hashtag h = new Hashtag();
            h.setName(name);
            hashtagRepository.save(h);

            logger.info("Created hashtag {}. IP: {}", name, ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "CREATE HASHTAG SUCCESS"));
        } catch (Exception e) {
            logger.error("Error in create hashtag: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public Object getList(String keyword, int pageIdx, int pageSize) {
        try {
            String kw = keyword == null ? "" : keyword.trim().replaceFirst("^#", "");
            Pageable paging = PageRequest.of(pageIdx, pageSize);
            Page<Hashtag> page = hashtagRepository.findByNameContainingIgnoreCaseOrderByPostCountDesc(kw, paging);
            logger.info("getList hashtag keyword='{}' -> {} kết quả", kw, page.getNumberOfElements());
            return ResponseHelper.getResponses(page.getContent(), page.getTotalElements(), page.getTotalPages(), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error in getList hashtag: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "SYSTEM ERROR: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> delete(DeleteDto dto, String ip) {
        try {
            if (dto == null || dto.getId() == null || dto.getId().trim().isEmpty()) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.BAD_REQUEST, new ResponseMess(1, "id is required"));
            }
            Hashtag h = hashtagRepository.findById(dto.getId().trim()).orElse(null);
            if (h == null) {
                return ResponseHelper.getResponseSearchMess(HttpStatus.NOT_FOUND, new ResponseMess(1, "Hashtag not found"));
            }
            hashtagRepository.delete(h);
            logger.info("Deleted hashtag {}. IP: {}", dto.getId(), ip);
            return ResponseHelper.getResponseSearchMess(HttpStatus.OK, new ResponseMess(0, "DELETE HASHTAG SUCCESS"));
        } catch (Exception e) {
            logger.error("Error in delete hashtag: {}", e.getMessage());
            return ResponseHelper.getResponseSearchMess(HttpStatus.INTERNAL_SERVER_ERROR, new ResponseMess(1, "DELETE FAILED: " + e.getMessage()));
        }
    }
}
