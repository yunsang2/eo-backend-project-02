package com.example.imprint.controller.message;

import com.example.imprint.domain.message.MessageResponseDto;
import com.example.imprint.domain.message.support.SupportsRequestDto;
import com.example.imprint.service.message.SupportsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 문의함 컨트롤러 (Support Controller)
@Slf4j
@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportsController {

    private final SupportsService supportsService;

    //사용자가 관리자에게 문의를 전송합니다.
    @PostMapping("/send")
    public ResponseEntity<MessageResponseDto> sendSupport(
            @Valid @RequestBody SupportsRequestDto requestDto) {
        try {
            MessageResponseDto response = supportsService.sendSupportMessage(requestDto);

            log.info("[POST /api/support/send] 문의 전송 성공 - 메시지 ID: {}", response.getId());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);

        } catch (IllegalArgumentException e) {
            log.error("[POST /api/support/send] 문의 전송 실패: {}", e.getMessage());
            throw e;
        }
    }

    // 관리자가 사용자에게 답변을 전송합니다.
    @PostMapping("/reply")
    public ResponseEntity<MessageResponseDto> sendAdminReply(
            @AuthenticationPrincipal Long adminUserId,
            @Valid @RequestBody AdminReplyRequest request) {

        log.info("[POST /api/support/reply] 관리자 답변 요청 - 관리자 ID: {}, 수신자 ID: {}",
                adminUserId, request.getReceiverUserId());

        try {
            MessageResponseDto response = supportsService.sendAdminReply(
                    adminUserId,
                    request.getReceiverUserId(),
                    request.getContent()
            );

            log.info("[POST /api/support/reply] 관리자 답변 성공 - 메시지 ID: {}",
                    response.getId());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);

        } catch (IllegalArgumentException e) {
            log.error("[POST /api/support/reply] 관리자 답변 실패: {}", e.getMessage());
            throw e;
        }
    }

    //사용자가 본인이 보낸 문의 목록을 조회합니다.
    @GetMapping("/my-messages")
    public ResponseEntity<List<MessageResponseDto>> getMyMessages(
            @AuthenticationPrincipal Long userId) {

        log.info("[GET /api/support/my-messages] 내 문의 조회 요청 - 사용자 ID: {}", userId);

        try {
            List<MessageResponseDto> messages = supportsService.getMySupportMessages(userId);

            log.info("[GET /api/support/my-messages] 내 문의 조회 성공 - 조회 수: {}",
                    messages.size());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(messages);

        } catch (IllegalArgumentException e) {
            log.error("[GET /api/support/my-messages] 내 문의 조회 실패: {}", e.getMessage());
            throw e;
        }
    }

    // 관리자가 전체 문의 목록을 조회합니다.
    @GetMapping("/all-messages")
    public ResponseEntity<List<MessageResponseDto>> getAllMessages() {
        try {
            List<MessageResponseDto> messages =
                    supportsService.getAllSupportMessages();

            log.info("[GET /api/support/all-messages] 전체 문의 조회 성공 - 조회 수: {}",
                    messages.size());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(messages);

        } catch (IllegalArgumentException e) {
            log.error("[GET /api/support/all-messages] 전체 문의 조회 실패: {}",
                    e.getMessage());
            throw e;
        }
    }

    // 메시지를 읽음 상태로 변경합니다.
    @GetMapping("/{messageId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable @NotNull Long messageId) {
        try {
            supportsService.markAsRead(messageId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "메시지를 읽었습니다.");

            log.info("[PATCH /api/support/{}/read] 읽음 처리 성공", messageId);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);

        } catch (IllegalArgumentException e) {
            log.error("[PATCH /api/support/{}/read] 읽음 처리 실패: {}",
                    messageId, e.getMessage());
            throw e;
        }
    }

    // 메시지를 삭제합니다 (Soft Delete).
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable @NotNull Long messageId,
            @AuthenticationPrincipal Long userId) {

        log.info("[DELETE /api/support/{}] 메시지 삭제 요청 - 사용자 ID: {}",
                messageId, userId);

        try {
            supportsService.deleteMessage(messageId, userId);

            log.info("[DELETE /api/support/{}] 메시지 삭제 성공", messageId);

            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();

        } catch (IllegalArgumentException e) {
            log.error("[DELETE /api/support/{}] 메시지 삭제 실패: {}",
                    messageId, e.getMessage());
            throw e;
        }
    }


    // 관리자 답변 요청 DTO (Inner Class)
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class AdminReplyRequest {

        // 답변을 받을 사용자 ID
        @NotNull(message = "수신자 ID는 필수입니다")
        private Long receiverUserId;


        // 답변 내용
        @NotNull(message = "답변 내용은 필수입니다")
        private String content;
    }
}