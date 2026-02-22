package com.example.imprint.domain.message.support;

import com.example.imprint.domain.message.MessageEntity;
import com.example.imprint.domain.message.MessageResponseDto;
import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.repository.message.MessageRepository;
import com.example.imprint.repository.user.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupportsService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    // 사용자가 관리자에게 문의 메시지를 전송합니다.
    @Transactional
    public MessageResponseDto sendSupportMessage(
            @NotNull Long senderUserId,
            @NotNull SupportsRequestDto requestDto) {

        log.info("문의 메시지 전송 시작 - 사용자 ID: {}", senderUserId);

        // 1. 발신자(사용자) 조회
        UserEntity sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다. - ID: {}", senderUserId);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다.");
                });

        log.debug("발신자 조회 성공 - 사용자명: {}", sender.getNickname());

        // 2. 수신자(관리자) 조회
        UserEntity admin = userRepository.findByRole("ADMIN")
                .orElseThrow(() -> {
                    log.error("시스템에 관리자가 존재하지 않습니다");
                    return new IllegalArgumentException(
                            "관리자를 찾을 수 없습니다. 시스템 설정을 확인해주세요."
                    );
                });

        log.debug("수신자(관리자) 조회 성공 - 관리자명: {}", admin.getNickname());

        // 3. 메시지 엔티티 생성
        MessageEntity message = MessageEntity.builder()
                .content(requestDto.getContent())
                .sender(sender)
                .receiver(admin)
                .isRead(false)
                .deletedBySender(false)
                .deletedByReceiver(false)
                .isStoredBySender(false)
                .isStoredByReceiver(false)
                .build();

        // 4. 메시지 저장
        MessageEntity savedMessage = messageRepository.save(message);

        log.info("문의 메시지 저장 완료 - 메시지 ID: {}", savedMessage.getId());

        // 5. DTO 변환 및 반환
        return MessageResponseDto.from(savedMessage);
    }

    // 관리자가 사용자에게 답변 메시지를 전송합니다.

    @Transactional
    public MessageResponseDto sendAdminReply(
            @NotNull Long adminUserId,
            @NotNull Long receiverUserId,
            @NotNull String content) {

        log.info("관리자 답변 전송 시작 - 관리자 ID: {}, 수신자 ID: {}",
                adminUserId, receiverUserId);

        // 1. 발신자(관리자) 조회
        UserEntity admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> {
                    log.error("관리자를 찾을 수 없습니다 - ID: {}", adminUserId);
                    return new IllegalArgumentException("관리자를 찾을 수 없습니다.");
                });

        // 2. 관리자 권한 검증
        if(!"ADMIN".equals(admin.getRole())) {
            log.error("관리자 권한 없음 - 사용자 ID: {}, Role: {}",
                    adminUserId, admin.getRole());
            throw new IllegalArgumentException("관리자만 답변을 보낼 수 있습니다.");
        }

        log.debug("관리자 권한 검증 완료 - 관리자명: {}", admin.getNickname());

        // 3. 수신자(사용자) 조회
        UserEntity receiver = userRepository.findById(receiverUserId)
                .orElseThrow(() -> {
                   log.error("수신자를 찾을 수 없습니다 - ID: {}", receiverUserId);
                   return new IllegalArgumentException("수신자를 찾을 수 없습니다.");
                });

        log.debug("수신자 조회 성공 - 사용자명: {}", receiver.getNickname());

        // 4. 답변 메시지 엔티티 생성
        MessageEntity replyMessage = MessageEntity.builder()
                .content(content)
                .sender(admin)
                .receiver(receiver)
                .isRead(false)
                .deletedBySender(false)
                .deletedByReceiver(false)
                .isStoredBySender(false)
                .isStoredByReceiver(false)
                .build();

        // 5. 답변 메시지 저장
        MessageEntity savedReply = messageRepository.save(replyMessage);

        log.info("관리자 답변 저장 완료 - 메시지 ID: {}", savedReply.getId());

        // 6. DTO 변환 및 반환
        return MessageResponseDto.from(savedReply);
    }

    // 사용자가 본인이 보낸 문의 목록을 조회합니다.
    public List<MessageResponseDto> getMySupportMessages(@NotNull Long userId) {
        log.info("내 문의 목록 조회 시작 - 사용자 ID: {}", userId);

        // 1. 사용자 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                   log.error("사용자를 찾을 수 없습니다 - ID: {}", userId);
                   return new IllegalArgumentException("사용자를 찾을 수 없습니다.");
                });

        log.debug("사용자 조회 성공 - 사용자명: {}", user.getNickname());

        // 2. 사용자가 보낸 메시지 조회(삭제하지 않은 것만)
        List<MessageEntity> messages = messageRepository
                .findAllBySenderAndDeletedBySenderFalseOrderByCreatedAtDesc(user);

        log.debug("조회된 메시지 수: {}", messages.size());

        // 3. 관리자에게 보낸 메시지만 필터링 및 DTO 변환
        List<MessageResponseDto> result = messages.stream()
                .filter(msg -> "ADMIN".equals(msg.getReceiver().getRole()))
                .map(MessageResponseDto::from)
                .collect(Collectors.toList());

        log.info("내 문의 목록 조회 완료 - 결과 수: {}", result.size());

        return result;
    }

    // 관리자가 전체 문의 목록을 조회합니다.
    public List<MessageResponseDto> getAllSupportMessages(@NotNull Long adminUserId) {
        log.info("전체 문의 목록 조회 시작 - 관리자 ID: {}", adminUserId);

        // 1. 관리자 조회
        UserEntity admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> {
                    log.error("관리자를 찾을 수 없습니다 - ID: {}", adminUserId);
                    return new IllegalArgumentException("관리자를 찾을 수 없습니다.");
                });

        // 2. 관리자 권한 검증
        if(!"ADMIN".equals(admin.getRole())) {
            log.error("관리자 권한 없음 - 사용자 ID: {}, Role: {}",
                    adminUserId, admin.getRole());
            throw new IllegalArgumentException("관리자만 문의 목록을 조회할 수 있습니다.");
        }

        log.debug("관리자 권한 검증 완료 - 관리자명: {}", admin.getNickname());

        // 3. 관리자가 받은 메시지 조회 (삭제하지 않은 것만)
        List<MessageEntity> messages = messageRepository
                .findAllByReceiverAndDeletedByReceiverFalseOrderByCreatedAtDesc(admin);

        log.debug("조회된 전체 문의 수: {}", messages.size());

        // 4. DTO 리스트로 변환
        List<MessageResponseDto> result = messages.stream()
                .map(MessageResponseDto::from)
                .collect(Collectors.toList());

        log.info("전체 문의 목록 조회 완료 - 결과 수: {}", result.size());

        return result;
    }

    // 메시지를 읽음 상태로 변경합니다.
    @Transactional
    public void markAsRead(@NotNull Long messageId, @NotNull Long userId) {
        log.info("메시지 읽음 처리 시작 - 메시지 ID: {}, 사용자 ID: {}",
                messageId, userId);

        // 1. 메시지 조회
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> {
                    log.error("메시지를 찾을 수 없습니다 - ID: {}", messageId);
                    return new IllegalArgumentException("메시지를 찾을 수 없습니다.");
                });

        // 2. 수신자 권한 검증
        if(!message.getReceiver().getId().equals(userId)) {
            log.error("메시지를 읽음 권한 없음 - 메시지 ID: {}, 요청자 ID: {}, 수신자 ID: {}",
                    messageId, userId, message.getReceiver().getId());
            throw new IllegalArgumentException("본인이 받은 메시지만 읽을 수 있습니다.");
        }

        // 3. 읽음 상태로 변경
        message.read();

        log.info("메시지 읽음 처리 완료 - 메시지 ID: {}", messageId);
    }

    // 메시지를 삭제합니다 (Soft Delete)
    @Transactional
    public void deleteMessage(@NotNull Long messageId, @NotNull Long userId) {
        log.info("메시지 삭제 시작 - 메시지 ID: {}, 사용자 ID: {}",
                messageId, userId);

        // 1. 메시지 조회
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> {
                    log.error("메시지를 찾을 수 없습니다 - ID: {}", messageId);
                    return new IllegalArgumentException("메시지를 찾을 수 없습니다.");
                });


        // 2. 삭제 권한 검증 및 삭제 처리
        if(message.getSender().getId().equals(userId)) {
            //발신자가 삭제
            message.deleteBySender();
            log.info("발신자가 메시지 삭제 - 메시지 ID: {}, 발신자 ID: {}",
                    messageId, userId);
        } else if(message.getReceiver().getId().equals(userId)) {
            // 수신자가 삭제
            message.deleteByReceiver();
            log.info("수신자가 메시지 삭제 - 메시지 ID: {}, 수신자 ID: {}",
                    messageId, userId);
        } else {
            //발신자도 수신자도 아님
            log.error("메시지 삭제 권한 없음 - 메시지 ID: {}, 요청자 ID: {}",
                    messageId, userId);
            throw new IllegalArgumentException("본인의 메시지만 삭제할 수 있습니다.");
        }
    }
}