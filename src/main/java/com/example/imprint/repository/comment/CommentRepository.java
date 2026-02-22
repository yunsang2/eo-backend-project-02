package com.example.imprint.repository.comment;

import com.example.imprint.domain.comment.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    Page<CommentEntity> findByPostId(Long postId, Pageable pageable);

    // 하루에 작성된 댓글 수
    long countByCreatedAtAfter(LocalDateTime startOfDay);
}
