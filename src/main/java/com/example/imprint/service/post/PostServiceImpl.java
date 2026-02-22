package com.example.imprint.service.post;

import com.example.imprint.domain.board.BoardEntity;
import com.example.imprint.domain.page.PaginationDto;
import com.example.imprint.domain.post.PostDto;
import com.example.imprint.domain.post.PostEntity;
import com.example.imprint.domain.post.PostMapper;
import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.user.UserRole;
import com.example.imprint.repository.board.BoardRepository;
import com.example.imprint.repository.post.PostRepository;
import com.example.imprint.repository.user.UserRepository;
import com.example.imprint.service.board.BoardService;
import com.example.imprint.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final BoardService boardService;

    @Override
    @Transactional
    public Long write(Long boardId, PostDto.Write dto) throws AccessDeniedException {
        log.info("게시물 생성을 시도합니다.");

        UserEntity user = userRepository.findById(userService.getCurrentUser().getId()).orElseThrow(
                () -> new RuntimeException("이스터에그를 찾으셨습니다! (있을 수 없는 오류 404)")
        );

        if (!userService.isActive(user.getId())) {
            throw new AccessDeniedException("계정이 비활성 상태입니다.");
        }

        BoardEntity board = boardRepository.findById(boardId).orElseThrow(
                () -> new RuntimeException("게시판을 찾을 수 없습니다. (id = " + boardId + ")")
        );

        log.info("게시판을 선택했습니다.\n{}", board);

        PostEntity post = PostEntity.builder()
                .board(board)
                .writer(user)
                .title(dto.title())
                .content(dto.content())
                .build();

        log.info("게시판에 게시글 생성을 시도합니다.\n{}", post);

        board.addPost(post);

        log.info("게시판에 게시글을 생성했습니다.");

        return post.getId();
    }

    @Override
    @Transactional
    public Long update(Long postId, PostDto.Update dto) throws AccessDeniedException {
        log.info("게시물 수정을 시도합니다.");

        UserEntity user = userRepository.findById(userService.getCurrentUser().getId()).orElseThrow(
                () -> new RuntimeException("이스터에그를 찾으셨습니다! (있을 수 없는 오류 404)")
        );

        Long userId = user.getId();

        if (!userService.isActive(userId)) {
            throw new AccessDeniedException("계정이 비활성 상태입니다.");
        }

        PostEntity post = postRepository.findById(postId).orElseThrow(
                () -> new RuntimeException("게시물을 찾을 수 없습니다. (id = " + postId + ")")
        );

        if (user.getRole() != UserRole.ADMIN && !post.getWriter().getId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        log.info("게시물을 선택했습니다.\n{}", post);

        post.update(dto.title(), dto.content());

        log.info("게시물을 수정했습니다.");

        return post.getId();
    }

    @Override
    @Transactional
    public Long update(Long boardId, Long postId, PostDto.Update dto) throws AccessDeniedException {
        log.info("게시물 수정을 시도합니다.");

        UserEntity user = userRepository.findById(userService.getCurrentUser().getId()).orElseThrow(
                () -> new RuntimeException("이스터에그를 찾으셨습니다! (있을 수 없는 오류 404)")
        );

        Long userId = user.getId();

        if (!userService.isActive(userId)) {
            throw new AccessDeniedException("계정이 비활성 상태입니다.");
        }

        PostEntity post = postRepository.findById(postId).orElseThrow(
                () -> new RuntimeException("게시물을 찾을 수 없습니다. (id = " + postId + ")")
        );

        if (!post.getBoard().getId().equals(boardId)) {
            throw new RuntimeException("게시판에서 게시물을 찾을 수 없습니다. (boardId = " + boardId + ", postId = " + postId + ")");
        }

        if (user.getRole() != UserRole.ADMIN && !post.getWriter().getId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        log.info("게시물을 선택했습니다.\n{}", post);

        post.update(dto.title(), dto.content());

        log.info("게시물을 수정했습니다.");

        return post.getId();
    }

    @Override
    @Transactional
    public void delete(Long id) throws AccessDeniedException {
        log.info("게시물 삭제를 시도합니다.");

        UserEntity user = userRepository.findById(userService.getCurrentUser().getId()).orElseThrow(
                () -> new RuntimeException("이스터에그를 찾으셨습니다! (있을 수 없는 오류 404)")
        );

        Long userId = user.getId();

        PostEntity post = postRepository.findById(id).orElseThrow(
                () -> new RuntimeException("게시물을 찾을 수 없습니다. (id = " + id + ")")
        );

        if (user.getRole() != UserRole.ADMIN && !post.getBoard().getManagerList().contains(user) && !post.getWriter().getId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        log.info("게시물을 선택했습니다.\n{}", post);

        postRepository.delete(post);

        log.info("게시물을 삭제했습니다.");
    }

    @Override
    @Transactional
    public void delete(Long boardId, Long postId) throws AccessDeniedException {
        log.info("게시물 삭제를 시도합니다.");

        UserEntity user = userRepository.findById(userService.getCurrentUser().getId()).orElseThrow(
                () -> new RuntimeException("이스터에그를 찾으셨습니다! (있을 수 없는 오류 404)")
        );

        Long userId = user.getId();

        PostEntity post = postRepository.findById(postId).orElseThrow(
                () -> new RuntimeException("게시물을 찾을 수 없습니다. (id = " + postId + ")")
        );

        if (!post.getBoard().getId().equals(boardId)) {
            throw new RuntimeException("게시판에서 게시물을 찾을 수 없습니다. (boardId = " + boardId + ", postId = " + postId + ")");
        }

        if (user.getRole() != UserRole.ADMIN && !post.getBoard().getManagerList().contains(user) && !post.getWriter().getId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        log.info("게시물을 선택했습니다.\n{}", post);

        postRepository.delete(post);

        log.info("게시물을 삭제했습니다.");
    }

    @Override
    public PostDto.Response get(Long id) {
        log.info("게시물 조회를 시도합니다.");

        PostEntity post = postRepository.findById(id).orElseThrow(
                () -> new RuntimeException("게시물을 찾을 수 없습니다. (id = " + id + ")")
        );

        log.info("게시물을 조회했습니다.\n{}", post);

        return PostMapper.fromEntityToDto(post);
    }

    @Override
    public PostDto.Response get(Long boardId, Long postId) {
        log.info("게시물 조회를 시도합니다.");

        PostEntity post = postRepository.findById(postId).orElseThrow(
                () -> new RuntimeException("게시물을 찾을 수 없습니다. (id = " + postId + ")")
        );

        if (!post.getBoard().getId().equals(boardId)) {
            throw new RuntimeException("게시판에서 게시물을 찾을 수 없습니다. (boardId = " + boardId + ", postId = " + postId + ")");
        }

        log.info("게시물을 조회했습니다.\n{}", post);

        return PostMapper.fromEntityToDto(post);
    }

    @Override
    public List<PostDto.Response> getPostList(Long boardId) {
        log.info("게시물 목록 조회를 시도합니다.");

        List<PostDto.Response> postDtoList = new ArrayList<>();
        List<PostEntity> postEntityList = postRepository.findAllById(Collections.singleton(boardId));

        postEntityList.forEach(postEntity -> postDtoList.add(PostMapper.fromEntityToDto(postEntity)));

        log.info("게시물 목록을 조회했습니다.");

        return postDtoList;
    }

    @Override
    public Page<PostDto.Response> getPostList(Long boardId, Pageable pageable) {
        log.info("게시물 목록 조회를 시도합니다.");

        Page<PostEntity> postEntityPage = postRepository.findByBoardId(boardId, pageable);

        log.info("게시물 목록을 조회했습니다. (page = {})", pageable.getPageNumber());

        return postEntityPage.map(PostMapper::fromEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PostDto.pagedResponse searchPosts(String keyword, int page, int size) {
        // 페이징 설정 (Spring Data JPA는 0페이지부터 시작하므로 page - 1)
        Pageable pageable = PageRequest.of(page - 1, size);

        // 검색 쿼리 실행
        Page<PostEntity> postPage = postRepository.searchAllByKeyword(keyword, pageable);

        // Entity -> Dto 변환
        List<PostDto.Response> postList = postPage.getContent().stream()
                .map(PostMapper::fromEntityToDto)
                .toList();

        // 페이지네이션 정보 생성
        PaginationDto pagination = PaginationDto.of(
                postPage.getPageable(),
                postPage.getTotalElements(),
                postPage.getTotalPages()
        );

        return new PostDto.pagedResponse(postList, pagination);
    }
}