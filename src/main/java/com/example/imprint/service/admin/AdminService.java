package com.example.imprint.service.admin;

import com.example.imprint.domain.board.BoardEntity;
import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.user.UserRole;
import com.example.imprint.domain.user.UserStatus;
import com.example.imprint.repository.board.BoardRepository;
import com.example.imprint.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    // 전체 회원 목록 조회 (관리자용)
    @Transactional(readOnly = true)
    public Page<UserEntity> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    // 유저 검색 (관리자용)
    @Transactional(readOnly = true)
    public Page<UserEntity> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable);
    }

    // 유저 직급 수정 (ADMIN 전용)
    @Transactional
    public void updateUserRole(Long userId, UserRole newRole) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.updateRole(newRole);

        // USER로 강등 시, 해당 유저가 가진 모든 게시판 관리 권한 삭제
        if (newRole == UserRole.USER) {
            // 관계의 주인이 BoardEntity이므로, 보드 쪽의 managerList에서 유저를 제거해야 DB에 반영됩니다.
            List<BoardEntity> managedBoards = user.getManagingBoardList();

            for (BoardEntity board : managedBoards) {
                board.getManagerList().remove(user);
            }

            // 유저 측 객체 리스트도 깔끔하게 비워줍니다.
            user.getManagingBoardList().clear();
        }
    }

    // 관리 중인 게시판 개수에 따라 직급을 자동으로 맞춤
    @Transactional
    public void syncUserRole(UserEntity user) {
        // 어드민은 직급이 변하지 않도록 방어
        if (user.getRole() == UserRole.ADMIN) return;

        // 리포지토리를 거치지 않고 엔티티의 리스트 사이즈를 직접 확인
        if (user.getManagingBoardList().isEmpty()) {
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