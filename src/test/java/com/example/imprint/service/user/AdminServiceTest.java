package com.example.imprint.service.user;

import com.example.imprint.domain.board.BoardEntity;
import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.user.UserRole;
import com.example.imprint.repository.board.BoardRepository;
import com.example.imprint.repository.user.BoardManagerRepository;
import com.example.imprint.repository.user.UserRepository;
import com.example.imprint.service.admin.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardManagerRepository boardManagerRepository;

    private UserEntity user;
    private BoardEntity board1;
    private BoardEntity board2;

    @BeforeEach
    void setUp() {
        // 테스트용 기본 유저 (ID: 1, Role: USER)
        user = UserEntity.builder()
                .id(1L)
                .nickname("테스트유저")
                .role(UserRole.USER)
                .build();

        // 테스트용 게시판 1, 2
        board1 = BoardEntity.builder().id(10L).name("자유게시판").build();
        board2 = BoardEntity.builder().id(20L).name("공지사항").build();
    }

    @Test
    @DisplayName("매니저 임명 시 직급이 MANAGER로 승격되어야 한다")
    void assignManagerTest() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(boardRepository.findById(10L)).thenReturn(Optional.of(board1));
        when(boardManagerRepository.existsByBoardIdAndUserId(10L, 1L)).thenReturn(false);

        // 임명 후 권한 조회를 하면 1개가 있다고 가정
        BoardManager fakeManager = BoardManager.builder()
                .user(user)
                .board(board1)
                .build();

        // when
        adminService.assignManager(1L, 10L);

        // then
        assertThat(user.getRole()).isEqualTo(UserRole.MANAGER);
        verify(boardManagerRepository, times(1)).save(any(BoardManager.class));
    }

    @Test
    @DisplayName("여러 게시판 중 하나만 해임되었을 때는 MANAGER 직급을 유지해야 한다")
    void dismissOneManagerKeepRoleTest() {
        // given: 유저가 이미 매니저이고 두 개의 게시판 권한을 가진 상태
        user.changeRole(UserRole.MANAGER);
        BoardManager bm1 = BoardManager.builder().user(user).board(board1).build();
        BoardManager bm2 = BoardManager.builder().user(user).board(board2).build();

        when(boardManagerRepository.findByBoardIdAndUserId(10L, 1L)).thenReturn(Optional.of(bm1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        // 하나를 지워도 여전히 하나(bm2)가 남아있는 상황
        when(boardManagerRepository.findAllByUserId(1L)).thenReturn(List.of(bm2));

        // when: 게시판 10번 권한 해임
        adminService.dismissManager(1L, 10L);

        // then: 직급은 여전히 MANAGER여야 함
        assertThat(user.getRole()).isEqualTo(UserRole.MANAGER);
        verify(boardManagerRepository, times(1)).delete(bm1);
    }

    @Test
    @DisplayName("모든 매니저 권한이 삭제되면 USER로 자동 강등되어야 한다")
    void dismissAllManagerDemoteTest() {
        // given: 유저가 매니저이고 권한이 하나만 있는 상태
        user.changeRole(UserRole.MANAGER);
        BoardManager bm1 = BoardManager.builder().user(user).board(board1).build();

        when(boardManagerRepository.findByBoardIdAndUserId(10L, 1L)).thenReturn(Optional.of(bm1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        // 삭제 후 조회 시 빈 리스트 반환
        when(boardManagerRepository.findAllByUserId(1L)).thenReturn(Collections.emptyList());

        // when: 마지막 남은 권한 해임
        adminService.dismissManager(1L, 10L);

        // then: 직급이 USER로 돌아와야 함
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        verify(boardManagerRepository, times(1)).delete(bm1);
    }

    @Test
    @DisplayName("어드민이 직접 직급을 USER로 수정하면 모든 게시판 권한이 삭제되어야 한다")
    void updateUserRoleToUserTest() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        BoardManager bm1 = BoardManager.builder().user(user).board(board1).build();
        when(boardManagerRepository.findAllByUserId(1L)).thenReturn(List.of(bm1));

        // when: 어드민이 해당 유저를 USER로 강제 수정
        adminService.updateUserRole(1L, UserRole.USER);

        // then
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        verify(boardManagerRepository, times(1)).deleteAll(anyList());
    }
}