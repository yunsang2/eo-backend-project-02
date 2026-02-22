package com.example.imprint.service.user;

import com.example.imprint.domain.board.BoardEntity;
import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.user.UserRole;
import com.example.imprint.domain.user.UserStatus;
import com.example.imprint.repository.user.UserRepository;
import com.example.imprint.service.admin.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private UserRepository userRepository;


    private UserEntity user;

    @BeforeEach
    void setUp() {
        // 테스트용 기본 유저 (기본 MANAGER 상태로 설정)
        user = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("테스트유저")
                .role(UserRole.MANAGER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("관리자가 유저의 직급을 ADMIN으로 수정할 수 있다")
    void updateUserRoleToAdminTest() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        adminService.updateUserRole(1L, UserRole.ADMIN);

        // then
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("유저를 USER로 강등시키면 관리하던 게시판 리스트가 비워져야 한다")
    void demoteToUserAndDeletePermissionsTest() {
        // given
        // 이미 관리 중인 게시판이 하나 있다고 가정
        BoardEntity board = BoardEntity.builder().id(10L).name("자유게시판").build();
        user.getManagingBoardList().add(board);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        adminService.updateUserRole(1L, UserRole.USER);

        // then
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        // ManyToMany 관계이므로 리스트가 비워졌는지 확인 (AdminService 로직에 따라 다름)
        assertThat(user.getManagingBoardList()).isEmpty();
    }

    @Test
    @DisplayName("사용자의 상태를 BANNED로 변경할 수 있다")
    void updateUserStatusToBannedTest() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        adminService.updateUserStatus(1L, UserStatus.BANNED);

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
    }

    @Test
    @DisplayName("사용자를 ACTIVE 상태로 복구할 수 있다")
    void updateUserStatusToActiveTest() {
        // given: 이미 차단된 유저
        user.ban();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        adminService.updateUserStatus(1L, UserStatus.ACTIVE);

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
}