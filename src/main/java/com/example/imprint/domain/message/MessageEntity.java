package com.example.imprint.domain.message;

import com.example.imprint.domain.BaseTimeEntity;
import com.example.imprint.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "MESSAGES")
// 상속 으로 보낸(createdAt)시간 조회
public class MessageEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 쪽지 내용
    @Column(nullable = false, columnDefinition = "TEXT", length = 1000)
    private String content;

    // 보내는 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SENDER_ID")
    private UserEntity sender;

    // 받는 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECEIVER_ID")
    private UserEntity receiver;

    // 읽음 여부
    @Column(nullable = false)
    private boolean isRead;
    private LocalDateTime readAt;

    // 삭제 관리
    private boolean deletedBySender;    // 보낸 사람이 삭제했는지
    private boolean deletedByReceiver;  // 받은 사람이 삭제했는지
    private boolean isStoredBySender;   // 보낸 사람이 보관했는지
    private boolean isStoredByReceiver; // 받은 사람이 보관했는지

    // 읽었을때 시간 저장 및 상태 변환
    public void read() {
        if (!this.isRead) {
            this.isRead = true;
            // 읽은 순간 현재 시간 기록
            this.readAt = LocalDateTime.now();
        }
    }

    // 보낸사람, 받은사람이 서로 지워야 삭제 가능
    public void deleteBySender() { this.deletedBySender = true; }
    public void deleteByReceiver() { this.deletedByReceiver = true; }
}