package com.example.imprint.service.message;

import com.example.imprint.domain.message.MessageEntity;
import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.user.UserRole;
import com.example.imprint.domain.user.UserStatus;
import com.example.imprint.repository.message.MessageRepository;
import com.example.imprint.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MessageServiceIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private MessageService messageService;
    @Autowired private ObjectMapper objectMapper;

    private UserEntity sender;
    private UserEntity receiver;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 저장
        sender = UserEntity.builder()
                .email("sender@test.com")
                .password("1234")
                .nickname("보낸이")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        receiver = UserEntity.builder()
                .email("receiver@test.com")
                .password("1234")
                .nickname("받는이")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(sender);
        userRepository.save(receiver);
    }

    @Test
    @DisplayName("쪽지 전체 기능 시나리오 테스트 (발송-정보확인-읽음전환-삭제)")
    @WithUserDetails(value = "receiver@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void fullMessageScenarioTest() throws Exception {

        // 발송 및 보관 확인 (Sender 시점)
        messageService.sendMessage(sender.getEmail(), receiver.getNickname(), "안녕하세요, 첫 쪽지입니다!");

        // 받은 쪽지함 목록 조회 및 정보 확인 (받는 사람, 보내는 사람, 시간, 안읽음 상태)
        mockMvc.perform(get("/message/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].senderNickname").value("보낸이"))
                .andExpect(jsonPath("$.data[0].receiverNickname").value("받는이"))
                .andExpect(jsonPath("$.data[0].content").value("안녕하세요, 첫 쪽지입니다!"))
                // 처음엔 안읽음 상태
                .andExpect(jsonPath("$.data[0].read").value(false))
                // 생성시간 존재
                .andExpect(jsonPath("$.data[0].createdAt").exists())
                .andDo(print());

        // 쪽지 단건 상세 읽기 및 상태 전환 확인
        MessageEntity savedMessage = messageRepository.findAll().get(0);
        Long messageId = savedMessage.getId();

        mockMvc.perform(get("/message/read/" + messageId))
                .andExpect(status().isOk())
                // 읽음 처리됨
                .andExpect(jsonPath("$.data.read").value(true))
                .andExpect(jsonPath("$.message").value("쪽지 상세 내용을 조회했습니다."));

        // 다시 목록 조회 시 읽음 상태(true)가 유지되는지 확인
        mockMvc.perform(get("/message/list"))
                .andExpect(jsonPath("$.data[0].read").value(true));

        // 받은 쪽지 삭제 검증
        mockMvc.perform(delete("/message/received/" + messageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("받은 쪽지가 삭제되었습니다."));

        // 삭제 후 목록에서 사라졌는지 최종 확인
        mockMvc.perform(get("/message/list"))
                // 삭제되어 비어있어야 함
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("보안 테스트: 제3자가 남의 쪽지를 읽으려 할 때 403 에러 발생")
    @WithUserDetails(value = "stranger@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void readMessageFail_PerfectStranger() throws Exception {
        // Given: 제3자(stranger) 계정 생성 및 저장
        UserEntity stranger = UserEntity.builder()
                .email("stranger@test.com").password("1234").nickname("낯선이")
                .role(UserRole.USER).status(UserStatus.ACTIVE).build();
        userRepository.save(stranger);

        // Given: sender가 receiver에게 비밀 쪽지를 보냄
        messageService.sendMessage(sender.getEmail(), receiver.getNickname(), "이것은 우리 둘만의 비밀이야.");

        // DB에서 해당 쪽지의 ID를 가져옴
        Long secretMessageId = messageRepository.findAll().stream()
                .filter(m -> m.getContent().equals("이것은 우리 둘만의 비밀이야."))
                .findFirst().get().getId();

        // When: 로그인된 'stranger'가 'receiver'의 쪽지 ID로 상세 조회를 시도
        mockMvc.perform(get("/message/read/" + secretMessageId))

                // Then: 403 Forbidden 상태코드와 실패 응답 규격 확인
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."))
                .andDo(print());
    }
}