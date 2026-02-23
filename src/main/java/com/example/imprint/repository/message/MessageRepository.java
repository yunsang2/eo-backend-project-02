package com.example.imprint.repository.message;

import com.example.imprint.domain.message.MessageEntity;
import com.example.imprint.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    // 받은 쪽지함: 수신자가 나이고, 수신자가 삭제하지 않은 쪽지들만 조회
    List<MessageEntity> findAllByReceiverAndDeletedByReceiverFalseOrderByCreatedAtDesc(UserEntity receiver);

    // 보낸 쪽지함: 발신자가 나이고, 발신자가 삭제하지 않은 쪽지들만 조회
    List<MessageEntity> findAllBySenderAndDeletedBySenderFalseOrderByCreatedAtDesc(UserEntity sender);

    // 안 읽은 쪽지 개수 확인 (알림용)
    long countByReceiverAndIsReadFalseAndDeletedByReceiverFalse(UserEntity receiver);

    // 수신자의 권한이 ADMIN인 메시지들을 최신순으로 조회
    @Query("SELECT m FROM MessageEntity m JOIN m.receiver u WHERE u.role = com.example.imprint.domain.user.UserRole.ADMIN ORDER BY m.createdAt DESC")
    List<MessageEntity> findAdminSupports();

    // 관리자가 아직 읽지 않은(미처리) 메시지 카운트
    @Query("SELECT COUNT(m) FROM MessageEntity m JOIN m.receiver u WHERE u.role = com.example.imprint.domain.user.UserRole.ADMIN AND m.isRead = false")
    long countPendingSupports();
}