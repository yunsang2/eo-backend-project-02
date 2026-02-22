package com.example.imprint.service.admin;

import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.user.UserRole;
import com.example.imprint.domain.user.UserStatus;
import com.example.imprint.repository.board.BoardRepository;
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

        user.updateRole(newRole);

        // USER로 강등 시, 해당 유저가 가진 모든 게시판 관리 권한 삭제
        if (newRole == UserRole.USER) {
            List<BoardManager> managedBoards = boardManagerRepository.findAllByUserId(userId);
            if (!managedBoards.isEmpty()) {
                boardManagerRepository.deleteAll(managedBoards);
            }
        }
    }

    // 관리 중인 게시판 개수에 따라 직급을 자동으로 맞춤
    private void syncUserRole(UserEntity user) {
        // 어드민은 직급이 변하지 않도록 방어
        if (user.getRole() == UserRole.ADMIN) return;

        List<BoardManager> managedBoards = boardManagerRepository.findAllByUserId(user.getId());

        if (managedBoards.isEmpty()) {
            user.updateRole(UserRole.USER);
        } else {
            user.updateRole(UserRole.MANAGER);
        }
    }

    // 사용자의 상태를 직접 수정 (ACTIVE, BANNED)
    @Transactional
    public void updateUserStatus(Long userId, UserStatus newStatus) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 유저 엔티티에 정의된 메서드 활용
        switch (newStatus) {
            case ACTIVE -> user.activate();
            case BANNED -> user.ban();
            case DELETED -> user.delete();
            default -> throw new IllegalArgumentException("지원하지 않는 상태 변경입니다.");
        }
    }
}