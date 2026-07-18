package com.example.social_network.Service;

import org.springframework.http.ResponseEntity;

public interface BookmarkService {

    /** Lưu / bỏ lưu 1 bài (toggle). Trả về trạng thái mới { saved: true|false }. userId từ token. */
    ResponseEntity<?> toggle(String postId, String userId, String ip);

    /** Danh sách bài đã lưu của user (trả về các Post, mới lưu trước). */
    Object getMyBookmarks(String userId, int pageIdx, int pageSize);

    /** Danh sách id bài user đã lưu (để FE tô nút đã lưu). */
    Object getMyBookmarkPostIds(String userId);
}
