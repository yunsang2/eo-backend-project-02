package com.example.imprint.repository.user;

import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.user.UserRole;
import com.example.imprint.domain.user.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

//JpaRepository를 상속받으면, 기본적인 save(저장), findById(조회), delete(삭제) 메서드를 구현 없이 사용가능
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    //회원으로 해당 이메일이 있는지 확인(로그인)
    Optional<UserEntity> findByEmail(String email);

    //이메일 중복 확인(회원가입)
    boolean existsByEmail(String email);

    //별명 중복확인(회원가입)
    boolean existsByNickname(String nickname);

    //별명 가져오기(쪽지용)
    Optional<UserEntity> findByNickname(String nickname);

    //비밀번호 재설정 토큰으로 유저 찾기
    Optional<UserEntity> findByResetToken(String resetToken);

    //권한 가져오기
    Optional<UserEntity> findByRole(UserRole role);

    // 오늘 가입자 수 조회
    long countByCreatedAtAfter(LocalDateTime startOfDay);

    // 상태별 유저 수 (ACTIVE, BANNED 등)
    long countByStatus(UserStatus status);

    // 관리자용 유저 검색 (이메일, 닉네임, 이름 통합 검색)
    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.email LIKE %:keyword% " +
            "OR u.nickname LIKE %:keyword% " +
            "OR u.name LIKE %:keyword% " +
            "ORDER BY u.createdAt DESC")
    Page<UserEntity> searchUsers(@Param("keyword") String keyword, Pageable pageable);
}
