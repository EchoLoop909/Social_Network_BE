// Nguồn thật: Social_Network_BE/src/main/java/com/example/social_network/Repository/CommentRepository.java
// Dùng làm mẫu convention cho MỌI Repository mới trong package Repository

package com.example.social_network.Repository;

import com.example.social_network.models.Entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    Page<Comment> findByPost_IdOrderByCreateTimeDesc(String postId, Pageable pageable);
}

/*
QUY TẮC BẮT BUỘC khi tạo Repository mới:
1. package com.example.social_network.Repository (chữ R hoa, không phải "repository")
2. @Repository trên interface
3. extends JpaRepository<Entity, IdType> — IdType là String (UUID) trừ khi entity dùng kiểu khác
4. Ưu tiên dùng Spring Data derived query method (findBy...OrderBy...Desc) thay vì @Query
   native khi query đơn giản (join 1 cấp, filter theo FK + sort). Chỉ dùng @Query native
   khi query phức tạp hơn derived method có thể biểu diễn.
5. Method phân trang LUÔN nhận Pageable và trả về Page<Entity>
6. KHÔNG viết logic nghiệp vụ trong Repository — chỉ query thuần
*/
