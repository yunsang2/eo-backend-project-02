package com.example.imprint.repository.user;

import com.example.imprint.domain.message.MessageEntity;
import com.example.imprint.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
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
    //관리자찾음
    Optional<UserEntity> findByRole(String role);
}
