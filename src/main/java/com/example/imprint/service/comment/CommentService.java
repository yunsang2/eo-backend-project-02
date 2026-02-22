package com.example.imprint.service.comment;

import com.example.imprint.domain.comment.CommentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface CommentService {
    Long write(Long postId, CommentDto.Write dto) throws AccessDeniedException;
    Long update(Long commentId, CommentDto.Update dto) throws AccessDeniedException;
    Long update(Long postId, Long commentId, CommentDto.Update dto) throws AccessDeniedException;
    void delete(Long id) throws AccessDeniedException;
    void delete(Long postId, Long commentId) throws AccessDeniedException;
    CommentDto.Response get(Long id);
    CommentDto.Response get(Long postId, Long commentId);
    List<CommentDto.Response> getCommentList(Long postId);
    Page<CommentDto.Response> getCommentList(Long postId, Pageable pageable);
}
