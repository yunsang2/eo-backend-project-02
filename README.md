# 📑 Imprint Community Service

**Imprint**는 사용자 간의 자유로운 소통과 체계적인 게시판 운영 기능을 제공하는 **RESTful 커뮤니티 서비스**입니다.  
회원가입부터 게시글 관리, 쪽지, 신고 및 관리자 대시보드까지 커뮤니티 운영에 필요한 핵심 기능을 포함하고 있습니다.

---

## 🚀 주요 기능 (Key Features)

### 👤 사용자 및 인증 (User & Auth)
- **이메일 인증**: 회원가입 전 `JavaMailSender`를 이용한 6자리 인증 코드 검증 (유효시간 5분, 재발송 제한 1분).
- **보안 로그인**: `BCrypt` 암호화 기반 비밀번호 저장 및 세션 방식의 인증 관리.
- **비밀번호 재설정**: 이메일 토큰 발급 및 유효시간(10분) 검증을 통한 안전한 비밀번호 변경.
- **권한 체계**: `USER`(일반), `MANAGER`(게시판 관리), `ADMIN`(전체 관리)으로 세분화된 접근 제어.

### 📋 게시판 및 게시글 (Board & Post)
- **동적 게시판**: 관리자에 의한 게시판 생성/수정/삭제 및 페이징 목록 조회.
- **게시물 관리**: 게시판별 게시물 작성, 수정, 삭제 및 최신순 페이징 조회.
- **권한 기반 삭제**: 작성자 본인 외에도 해당 게시판의 매니저와 전체 관리자가 게시물을 삭제할 수 있는 운영 권한 부여.

### 💬 커뮤니티 소통 (Comment & Message)
- **댓글 시스템**: 게시물 상세 페이지 내 댓글 작성 및 관리 (작성자/관리자 권한 검증).
- **쪽지 시스템**: 사용자 닉네임 기반 1:1 쪽지 발송, 읽음 처리 및 발신/수신자 개별 논리 삭제 로직.

### 🛡️ 운영 및 관리 (Admin & Report)
- **신고 시스템**: 불량 유저에 대한 카테고리별 신고 접수 및 관리자 목록 조회.
- **관리자 대시보드**: 전체 가입자, 금일 가입자, 활성/차단 유저 현황 및 미확인 신고 건수 실시간 모니터링.
- **권한 위임**: 특정 유저를 게시판 매니저로 임명/해임 시 `BOARD_MANAGERS` 테이블과 연동하여 유저 직급 자동 동기화.

---

## 🛠 기술 스택 (Tech Stack)

- **Backend**: Java 25, Spring Boot 4.0.2
- **Database**: H2
- **Security**: Spring Security (Session-based)
- **Library**: Lombok, Jakarta Validation, JavaMailSender
- **Architecture**: REST API, Entity-DTO Mapping (Record 사용)

---

## 📖 API Reference (Quick View)


| 기능 | Method | URL | 권한 |
| :--- | :---: | :--- | :---: |
| 회원가입 | `POST` | `/account/signup` | 비로그인 |
| 로그인 | `POST` | `/account/login` | 비로그인 |
| 게시판 목록 | `GET` | `/boards` | 전체 |
| 게시글 작성 | `POST` | `/boards/{id}/posts` | USER |
| 쪽지 발송 | `POST` | `/message/send` | USER |
| 유저 상태 수정 | `PATCH` | `/api/admin/dashboard/users/{id}/status` | ADMIN |

---

## 🏗 데이터베이스 논리 구조 (Database Logic)

본 프로젝트는 다음과 같은 핵심 엔티티 관계를 기반으로 설계되었습니다.
- **Users**: 회원 정보, 역할(Role), 상태(Status) 관리.
- **Boards & Posts**: 1:N 관계를 통한 게시판 중심의 콘텐츠 구조.
- **BoardManagers**: 특정 게시판과 매니저 간의 다대다(N:M) 매핑 및 권한 처리.
- **Messages**: 발신자/수신자 간의 독립적인 삭제 상태를 지원하는 소통 데이터.
- **EmailVerifications**: 회원가입 및 보안을 위한 임시 인증 데이터 저장소.

---

## ⚠️ 공통 에러 응답 (Error Handling)

`GlobalExceptionHandler`를 통해 모든 예외를 공통된 `ApiResponseDto` 형식으로 반환합니다.
- `400 Bad Request`: 비즈니스 로직 오류 (예: 이메일 중복, 인증번호 불일치 등)
- `401 Unauthorized`: 인증 만료 또는 로그인 정보 불일치
- `403 Forbidden`: 권한 부족 (예: 타인 게시물 수정 시도)
- `500 Internal Server Error`: 서버 내부 시스템 오류
