package com.example.imprint.domain;

import com.example.imprint.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity //JAP가 관리하는 엔티티임을 명시
@Getter // Lombok: getter 자동 생성
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 (JPA 필수)
@EntityListeners(AuditingEntityListener.class) // 생성&수정 시간 자동 관리
public class BoardEntity extends BaseTimeEntity {

    @Id // 기본 키(Primary Key)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가
    private Long id;  // 게시판 번호

    @Column(nullable = false, length = 100) // null 불가, 최대 100자
    private String name; // 게시판 이름

    // 게시판 만든사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false)
    private UserEntity creator;

     // 빌더 패턴 사용
    public BoardEntity(Long id, String name, UserEntity creator, boolean isPrivate) {
        this.id = id;
    // 게시판 관리자 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private UserEntity manager;

    @Builder // 빌더 패턴 사용
    public BoardEntity(String name, UserEntity creator, boolean isPrivate) {
        this.name = name;
        this.creator = creator;
    }

    public void update(String name) {  // 게시판 이름 수정 메서드
        this.name = name;
    }

    // 추가 : 만든 사람인지 확인
    public boolean isCreator(Long userId) {
        return this.creator != null && this.creator.getId().equals(userId);
    }
}
