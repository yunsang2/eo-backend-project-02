package com.example.imprint.service.board;

import com.example.imprint.domain.board.BoardDto;
import com.example.imprint.domain.board.BoardEntity;
import com.example.imprint.domain.board.BoardMapper;
import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.user.UserResponseDto;
import com.example.imprint.domain.user.UserRole;
import com.example.imprint.repository.board.BoardRepository;
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
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final UserService userService;

    @Override
    @Transactional
    public Long create(BoardDto.Create requestDto) {
        log.info("게시판 생성을 시도합니다.");

        BoardEntity board = BoardMapper.fromDtoToEntity(requestDto);
        BoardEntity saved = boardRepository.save(board);

        log.info("게시판을 생성했습니다.\n{}", saved);

        return saved.getId();
    }

    @Override
    @Transactional
    public Long update(BoardDto.Update requestDto, Long boardId) {
        log.info("게시판 수정을 시도합니다.");

        BoardEntity board = boardRepository.findById(boardId).orElseThrow(
                () -> new IllegalArgumentException("게시판를 찾을 수 없습니다. (id = " + boardId + ")")
        );

        log.info("게시판을 선택했습니다.\n{}", board);

        board.updateName(requestDto.name());
        BoardEntity saved = boardRepository.save(board);

        log.info("게시판을 수정했습니다.\n{}", saved);
        return boardId;
    }

    @Override
    @Transactional
    public void delete(Long boardId) {
        log.info("게시판 삭제를 시도합니다.");

        BoardEntity board = boardRepository.findById(boardId).orElseThrow(
                () -> new IllegalArgumentException("게시판을 찾을 수 없습니다. (id = " + boardId + ")")
        );

        boardRepository.delete(board);

        log.info("게시판을 삭제했습니다.\n{}", board);
    }

    @Override
    public BoardDto.Response get(Long boardId) {
        log.info("게시판 조회를 시도합니다.");

        BoardEntity board = boardRepository.findById(boardId).orElseThrow(
                () -> new IllegalArgumentException("게시판를 찾을 수 없습니다. (id = " + boardId + ")")
        );

        log.info("게시판 정보를 조회했습니다.\n{}", board);

        return BoardMapper.fromEntityToDto(board);
    }

    @Override
    public List<BoardDto.Response> getBoardList() {
        log.info("게시판 목록 조회를 시도합니다.");

        List<BoardDto.Response> boardDtoList = new ArrayList<>();
        List<BoardEntity> boardEntityList = boardRepository.findAll();

        boardEntityList.forEach(boardEntity -> boardDtoList.add(BoardMapper.fromEntityToDto(boardEntity)));

        log.info("게시판 목록을 조회했습니다.");

        return boardDtoList;
    }

    @Override
    public Page<BoardDto.Response> getBoardList(Pageable pageable) {
        log.info("게시판 목록 조회를 시도합니다.");

        Page<BoardEntity> boardEntityPage = boardRepository.findAll(pageable);

        log.info("게시판 목록을 조회했습니다. (page = {})", pageable.getPageNumber());

        return boardEntityPage.map(BoardMapper::fromEntityToDto);
    }

    @Override
    public List<UserResponseDto> getManagerList(Long boardId) {
        log.info("게시판 매니저 리스트 조회를 시도합니다.");

        BoardEntity board = boardRepository.findById(boardId).orElseThrow(
                () -> new IllegalArgumentException("게시판를 찾을 수 없습니다. (id = " + boardId + ")")
        );

        log.info("매니저를 조회할 게시판을 선택했습니다.\n{}", board);

        List<UserResponseDto> responseDtoList = new ArrayList<>();

        board.getManagerList().forEach(userEntity ->
                responseDtoList.add(UserResponseDto.fromEntity(userEntity)));

        log.info("매니저 리스트를 조회했습니다.\n{}", responseDtoList);

        return responseDtoList;
    }

    @Override
    @Transactional
    public void addManager(Long boardId, Long userId) throws AccessDeniedException {
        log.info("게시판에 사용자 매니저 등록을 시도합니다.");

        BoardEntity board = boardRepository.findById(boardId).orElseThrow(
                () -> new IllegalArgumentException("게시판를 찾을 수 없습니다. (id = " + boardId + ")")
        );

        log.info("매니저를 등록할 게시판을 선택했습니다.\n{}", board);

        UserEntity user = userRepository.findById(userId).orElseThrow(
                () ->  new IllegalArgumentException("사용자를 찾을 수 없습니다. (id = " + userId + ")")
        );

        if (user.getRole().equals(UserRole.ADMIN) || board.getManagerList().contains(user)) {
            throw new AccessDeniedException("이미 권한을 가지고 있습니다.");
        }

        log.info("매니저로 등록할 사용자를 선택했습니다.\n{}", user);

        user.updateRole(UserRole.MANAGER);

        log.info("사용자의 권한을 매니저로 수정했습니다.");

        board.addManager(user);

        log.info("게시판에 사용자를 매니저로 등록했습니다.");
    }

    @Override
    @Transactional
    public void removeManager(Long boardId, Long userId) {
        log.info("게시판에 사용자 매니저 제외을 시도합니다.");

        BoardEntity board = boardRepository.findById(boardId).orElseThrow(
                () -> new IllegalArgumentException("게시판를 찾을 수 없습니다. (id = " + boardId + ")")
        );

        log.info("매니저를 등록할 게시판을 선택했습니다.\n{}", board);

        UserEntity user = userRepository.findById(userId).orElseThrow(
                () ->  new IllegalArgumentException("사용자를 찾을 수 없습니다. (id = " + userId + ")")
        );

        log.info("매니저에서 제외할 사용자를 선택했습니다.\n{}", user);

        if (!board.getManagerList().contains(user)) {
            throw new IllegalArgumentException("사용자가 이미 매니저 리스트에 없습니다. (id = " + userId + ")");
        }

        board.removeManager(user);
        log.info("게시판에 사용자를 매니저에서 제외했습니다.");

        if (user.getManagingBoardList().isEmpty()) {
            log.info("사용자가 더이상 관리할 게시판이 없음을 확인하였습니다.");

            user.updateRole(UserRole.USER);

            log.info("사용자의 권한을 USER로 강등시켰습니다.");
        }
    }
}
