package com.example.imprint.service.post;

import com.example.imprint.domain.post.PostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface PostService {
    Long write(Long boardId, PostDto.Write dto) throws AccessDeniedException;
    Long update(Long postId, PostDto.Update dto) throws AccessDeniedException;
    Long update(Long boardId, Long postId, PostDto.Update dto) throws AccessDeniedException;
    void delete(Long id) throws AccessDeniedException;
    void delete(Long boardId, Long postId) throws AccessDeniedException;
    PostDto.Response get(Long id);
    PostDto.Response get(Long boardId, Long postId);
    List<PostDto.Response> getPostList(Long boardId);
    Page<PostDto.Response> getPostList(Long boardId, Pageable pageable);
}
