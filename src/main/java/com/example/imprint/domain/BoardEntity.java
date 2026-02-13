package com.example.imprint.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity //JAP가 관리하는 엔티티임을 명시
@Getter // Lombok: getter 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 (JPA 필수)
@EntityListeners(AuditingEntityListener.class) // 생성&수정 시간 자동 관리
public class BoardEntity extends BaseTimeEntity {

    @Id // 기본 키(Primary Key)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가
    private Long id;  // 게시판 번호

    @Column(nullable = false, length = 100) // null 불가, 최대 100자
    private String name; // 게시판 이름

    @Builder // 빌더 패턴 사용
    public BoardEntity(String name) {
        this.name = name;
    }

    public void updateBoard(String name) {  // 게시판 이름 수정 메서드
        this.name = name;
    }
}
