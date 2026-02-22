package com.example.imprint.service.comment;

import com.example.imprint.domain.comment.CommentDto;
import com.example.imprint.domain.comment.CommentEntity;
import com.example.imprint.domain.comment.CommentMapper;
import com.example.imprint.domain.post.PostEntity;
import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.user.UserRole;
import com.example.imprint.repository.comment.CommentRepository;
import com.example.imprint.repository.post.PostRepository;
import com.example.imprint.repository.user.UserRepository;
import com.example.imprint.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public Long write(Long postId, CommentDto.Write dto) throws AccessDeniedException {
        log.info("댓글 생성을 시도합니다.");

        UserEntity user = userRepository.findById(userService.getCurrentUser().getId()).orElseThrow(
                () -> new RuntimeException("이스터에그를 찾으셨습니다! (있을 수 없는 오류 404)")
        );

        if (!userService.isActive(user.getId())) {
            throw new AccessDeniedException("계정이 비활성 상태입니다.");
        }

        PostEntity post = postRepository.findById(postId).orElseThrow(
                () -> new RuntimeException("게시물을 찾을 수 없습니다. (id = " + postId + ")")
        );

        log.info("게시물을 선택했습니다.\n{}", post);

        CommentEntity comment = CommentEntity.builder()
                .post(post)
                .writer(user)
                .content(dto.content())
                .build();

        log.info("게시물에 댓글 생성을 시도합니다.\n{}", comment);

        post.addComment(comment);

        log.info("게시물에 댓글을 생성했습니다.");

        return comment.getId();
    }

    @Override
    public Long update(Long commentId, CommentDto.Update dto) throws AccessDeniedException {
        log.info("댓글 수정을 시도합니다.");

        UserEntity user = userRepository.findById(userService.getCurrentUser().getId()).orElseThrow(
                () -> new RuntimeException("이스터에그를 찾으셨습니다! (있을 수 없는 오류 404)")
        );

        Long userId = user.getId();

        if (!userService.isActive(userId)) {
            throw new AccessDeniedException("계정이 비활성 상태입니다.");
        }

        CommentEntity comment = commentRepository.findById(commentId).orElseThrow(
                () -> new RuntimeException("댓글을 찾을 수 없습니다. (id = " + commentId + ")")
        );

        if (user.getRole() != UserRole.ADMIN && !comment.getWriter().getId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        log.info("댓글을 선택했습니다.\n{}", comment);

        comment.updateContent(dto.content());

        log.info("댓글을 수정했습니다.");

        return comment.getId();
    }

    @Override
    public Long update(Long postId, Long commentId, CommentDto.Update dto) throws AccessDeniedException {
        log.info("댓글 수정을 시도합니다.");

        UserEntity user = userRepository.findById(userService.getCurrentUser().getId()).orElseThrow(
                () -> new RuntimeException("이스터에그를 찾으셨습니다! (있을 수 없는 오류 404)")
        );

        Long userId = user.getId();

        if (!userService.isActive(userId)) {
            throw new AccessDeniedException("계정이 비활성 상태입니다.");
        }

        CommentEntity comment = commentRepository.findById(commentId).orElseThrow(
                () -> new RuntimeException("댓글을 찾을 수 없습니다. (id = " + commentId + ")")
        );

        if (!comment.getPost().getId().equals(postId)) {
            throw new RuntimeException("게시물에서 댓글을 찾을 수 없습니다. (postId = " + postId + ", commentId = " + commentId + ")");
        }

        if (user.getRole() != UserRole.ADMIN && !comment.getWriter().getId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        log.info("댓글을 선택했습니다.\n{}", comment);

        comment.updateContent(dto.content());

        log.info("댓글을 수정했습니다.");

        return comment.getId();
    }

    @Override
    public void delete(Long id) throws AccessDeniedException {
        log.info("댓글 삭제를 시도합니다.");

        UserEntity user = userRepository.findById(userService.getCurrentUser().getId()).orElseThrow(
                () -> new RuntimeException("이스터에그를 찾으셨습니다! (있을 수 없는 오류 404)")
        );

        Long userId = user.getId();

        CommentEntity comment = commentRepository.findById(id).orElseThrow(
                () -> new RuntimeException("댓글을 찾을 수 없습니다. (id = " + id + ")")
        );

        if (user.getRole() != UserRole.ADMIN && !comment.getWriter().getId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        log.info("댓글을 선택했습니다.\n{}", comment);

        commentRepository.delete(comment);

        log.info("댓글 삭제했습니다.");
    }

    @Override
    public void delete(Long postId, Long commentId) throws AccessDeniedException {
        log.info("댓글 삭제를 시도합니다.");

        UserEntity user = userRepository.findById(userService.getCurrentUser().getId()).orElseThrow(
                () -> new RuntimeException("이스터에그를 찾으셨습니다! (있을 수 없는 오류 404)")
        );

        Long userId = user.getId();

        CommentEntity comment = commentRepository.findById(commentId).orElseThrow(
                () -> new RuntimeException("댓글을 찾을 수 없습니다. (id = " + commentId + ")")
        );

        if (!comment.getPost().getId().equals(postId)) {
            throw new RuntimeException("게시물에서 댓글을 찾을 수 없습니다. (postId = " + postId + ", commentId = " + commentId + ")");
        }

        if (user.getRole() != UserRole.ADMIN && !comment.getWriter().getId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        log.info("댓글을 선택했습니다.\n{}", comment);

        commentRepository.delete(comment);

        log.info("댓글 삭제했습니다.");
    }

    @Override
    public CommentDto.Response get(Long id) {
        log.info("댓글 조회를 시도합니다.");

        CommentEntity comment = commentRepository.findById(id).orElseThrow(
                () -> new RuntimeException("댓글을 찾을 수 없습니다. (id = " + id + ")")
        );

        log.info("게시물을 조회했습니다.\n{}", comment);

        return CommentMapper.fromEntityToDto(comment);
    }

    @Override
    public CommentDto.Response get(Long postId, Long commentId) {
        log.info("댓글 조회를 시도합니다.");

        CommentEntity comment = commentRepository.findById(commentId).orElseThrow(
                () -> new RuntimeException("댓글을 찾을 수 없습니다. (id = " + commentId + ")")
        );

        if (!comment.getPost().getId().equals(postId)) {
            throw new RuntimeException("게시물에서 댓글을 찾을 수 없습니다. (postId = " + postId + ", commentId = " + commentId + ")");
        }

        log.info("게시물을 조회했습니다.\n{}", comment);

        return CommentMapper.fromEntityToDto(comment);
    }

    @Override
    public List<CommentDto.Response> getCommentList(Long postId) {
        log.info("댓글 목록 조회를 시도합니다.");

        List<CommentDto.Response> commentDtoList = new ArrayList<>();
        List<CommentEntity> commentEntityList = commentRepository.findAllById(Collections.singleton(postId));

        commentEntityList.forEach(comment -> commentDtoList.add(CommentMapper.fromEntityToDto(comment)));

        log.info("댓글 목록을 조회했습니다.");

        return commentDtoList;
    }

    @Override
    public Page<CommentDto.Response> getCommentList(Long postId, Pageable pageable) {
        log.info("댓글 목록 조회를 시도합니다.");

        Page<CommentEntity> commentEntityPage = commentRepository.findByPostId(postId, pageable);

        log.info("댓글 목록을 조회했습니다. (page = {})", pageable.getPageNumber());

        return commentEntityPage.map(CommentMapper::fromEntityToDto);
    }
}
