package com.example.imprint.service.user;

import com.example.imprint.domain.BoardEntity;
import com.example.imprint.domain.user.BoardManager;
import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.user.UserRole;
import com.example.imprint.repository.BoardRepository;
import com.example.imprint.repository.user.BoardManagerRepository;
import com.example.imprint.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final BoardManagerRepository boardManagerRepository;

    // 유저 직급 수정 (ADMIN 전용)
    // 직접적인 권한 변경 시 사용
    @Transactional
    public void updateUserRole(Long userId, UserRole newRole) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.changeRole(newRole);

        // USER로 강등 시, 해당 유저가 가진 모든 게시판 관리 권한 삭제
        if (newRole == UserRole.USER) {
            List<BoardManager> managedBoards = boardManagerRepository.findAllByUserId(userId);
            if (!managedBoards.isEmpty()) {
                boardManagerRepository.deleteAll(managedBoards);
            }
        }
    }

    // 게시판 매니저 임명
    @Transactional
    public void assignManager(Long userId, Long boardId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시판을 찾을 수 없습니다."));

        // 중복 체크
        if (boardManagerRepository.existsByBoardIdAndUserId(boardId, userId)) {
            throw new IllegalStateException("이미 해당 게시판의 매니저입니다.");
        }

        // 매니저 추가
        BoardManager manager = BoardManager.builder()
                .user(user)
                .board(board)
                .build();
        boardManagerRepository.save(manager);

        // 직급 동기화 (USER -> MANAGER 승격 포함)
        this.syncUserRole(user);
    }

    // 매니저 권한 강등
    @Transactional
    public void dismissManager(Long userId, Long boardId) {
        BoardManager boardManager = boardManagerRepository.findByBoardIdAndUserId(boardId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시판의 관리 권한이 없습니다."));

        boardManagerRepository.delete(boardManager);

        // 삭제 후 유저의 남은 관리 게시판 확인 및 직급 동기화
        UserEntity user = userRepository.findById(userId).orElseThrow();
        this.syncUserRole(user);
    }

    // 관리 중인 게시판 개수에 따라 직급을 자동으로 맞춤
    private void syncUserRole(UserEntity user) {
        // 어드민은 직급이 변하지 않도록 방어
        if (user.getRole() == UserRole.ADMIN) return;

        List<BoardManager> managedBoards = boardManagerRepository.findAllByUserId(user.getId());

        if (managedBoards.isEmpty()) {
            user.changeRole(UserRole.USER);
        } else {
            user.changeRole(UserRole.MANAGER);
        }
    }
}