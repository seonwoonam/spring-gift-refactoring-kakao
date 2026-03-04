# 리팩토링 분석 보고서: KakaoAuthService 추출

> 분석 일시: 2026-02-26

## 분석 대상

- 설명: KakaoAuthController.callback()에 직접 구현된 Kakao OAuth2 콜백 흐름(토큰 요청 → 사용자 정보 조회 → 회원 자동 등록/조회 → kakaoAccessToken 저장 → JWT 발급)을 KakaoAuthService로 추출. login() 메서드의 인가 URL 생성 로직도 함께 분석.
- 관련 파일:
  - `src/main/java/gift/controller/KakaoAuthController.java` (REST API — 4개 의존성)
  - `src/main/java/gift/client/KakaoLoginClient.java` (카카오 API 호출 클라이언트)
  - `src/main/java/gift/config/KakaoLoginProperties.java` (카카오 OAuth 설정값)
  - `src/main/java/gift/service/AuthService.java` (기존 인증 서비스 — 3-1에서 추출 완료)
  - `src/main/java/gift/auth/JwtProvider.java` (JWT 토큰 생성/검증)
  - `src/main/java/gift/dto/TokenResponse.java` (JWT 응답 DTO)
  - `src/main/java/gift/model/Member.java` (updateKakaoAccessToken, getKakaoAccessToken)
  - `src/main/java/gift/repository/MemberRepository.java` (회원 조회/저장)

## 1. 대상 코드 현황

### KakaoAuthController (REST API)

| # | 파일 | 라인 | 코드 내용 | 사용 여부 |
|---|------|------|-----------|-----------|
| 1 | KakaoAuthController.java | 23-26 | `KakaoLoginProperties`, `KakaoLoginClient`, `MemberRepository`, `JwtProvider` 주입 | 사용중 |
| 2 | KakaoAuthController.java | 41-53 | `login()` — 인가 URL 생성 + 302 리다이렉트 | 사용중 — URL 생성에 `properties` 사용 |
| 3 | KakaoAuthController.java | 57 | `kakaoLoginClient.requestAccessToken(code)` | 사용중 — 인가 코드 → 액세스 토큰 교환 |
| 4 | KakaoAuthController.java | 58 | `kakaoLoginClient.requestUserInfo(kakaoToken.accessToken())` | 사용중 — 액세스 토큰 → 사용자 정보 조회 |
| 5 | KakaoAuthController.java | 61-62 | `memberRepository.findByEmail(email).orElseGet(() -> new Member(email))` | 사용중 — 회원 자동 등록/조회 |
| 6 | KakaoAuthController.java | 63 | `member.updateKakaoAccessToken(kakaoToken.accessToken())` | 사용중 — 카카오 액세스 토큰 저장 |
| 7 | KakaoAuthController.java | 64 | `memberRepository.save(member)` | 사용중 — 회원 저장 |
| 8 | KakaoAuthController.java | 66 | `jwtProvider.createToken(member.getEmail())` | 사용중 — JWT 발급 |

### 기존 AuthService와의 의존성 중복

| # | 의존성 | KakaoAuthController | AuthService | 비고 |
|---|--------|---------------------|-------------|------|
| 1 | `MemberRepository` | 사용중 (findByEmail, save) | 사용중 (findByEmail, existsByEmail, save) | **중복** |
| 2 | `JwtProvider` | 사용중 (createToken) | 사용중 (createToken) | **중복** |
| 3 | `KakaoLoginClient` | 사용중 | 미사용 | KakaoAuth 전용 |
| 4 | `KakaoLoginProperties` | 사용중 (login URL 생성) | 미사용 | KakaoAuth 전용 |

### kakaoAccessToken 사용 현황 (프로젝트 전체)

| # | 파일 | 라인 | 코드 내용 | 비고 |
|---|------|------|-----------|------|
| 1 | KakaoAuthController.java | 63 | `member.updateKakaoAccessToken(...)` | 토큰 저장 |
| 2 | KakaoNotificationService.java | 16 | `member.getKakaoAccessToken()` — null 체크 | 주문 알림 발송 시 사용 |
| 3 | KakaoNotificationService.java | 21 | `kakaoMessageClient.sendToMe(member.getKakaoAccessToken(), ...)` | 실제 토큰 사용 |
| 4 | Member.java | 45-46 | `updateKakaoAccessToken()` 메서드 | 엔티티 메서드 |
| 5 | Member.java | 80 | `getKakaoAccessToken()` 메서드 | 엔티티 메서드 |

## 2. 주석 및 TODO 분석

| # | 파일:라인 | 주석 내용 | 리팩토링에 영향 |
|---|-----------|-----------|-----------------|
| 1 | KakaoAuthController.java:14-19 | `/* Handles the Kakao OAuth2 login flow. 1. /login redirects the user to Kakao's authorization page 2. /callback receives the authorization code, exchanges it for an access token, retrieves user info, auto-registers the member if new, and issues a service JWT */` | 없음 — 흐름 설명 주석. Service로 이동 시에도 동일한 흐름이 Service에 캡슐화되므로 적합 |
| 2 | JwtProvider.java:11-16 | `/** Provides JWT token creation and validation. @author brian.kim @since 1.0 */` | 없음 — JwtProvider는 변경하지 않음 |

> KakaoAuthController, KakaoLoginClient, KakaoLoginProperties에 TODO, FIXME 등의 특수 주석은 없습니다.

## 3. Git Blame 분석

| # | 파일:라인 | 작성자 | 커밋 | 날짜 | 커밋 메시지 |
|---|-----------|--------|------|------|-------------|
| 1 | KakaoAuthController.java 전체 (1-69) | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |
| 2 | KakaoLoginClient.java 전체 (1-58) | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |
| 3 | KakaoLoginProperties.java 전체 (1-7) | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |
| 4 | AuthService.java 전체 (1-38) | theo.cha | 671576bf | 2026-02-26 | (3-1 MemberService + AuthService 분리에서 생성) |

### 커밋 상세

- **55ca9e43 (wotjd243, 2026-02-18)**: 프로젝트 초기 설정 커밋. KakaoAuthController, KakaoLoginClient, KakaoLoginProperties 전체가 이 커밋에서 생성됨. 62개 파일 일괄 추가. 초기 템플릿 코드.
- **671576bf (theo.cha, 2026-02-26)**: 3-1 리팩토링에서 AuthService 생성. MemberController에 있던 register/login 로직을 AuthService로 추출. KakaoAuthController는 이때 건드리지 않음.

> KakaoAuthController 전체가 초기 커밋(55ca9e43)에서 생성되었으며, 이후 어떤 수정도 없었습니다. 프로젝트에서 유일하게 한 번도 수정되지 않은 Controller입니다.

## 4. 리팩토링 영향도 판단

### 주석/TODO 의도 확인
- [x] 의도를 설명하는 주석 없음 (블록 주석은 흐름 설명으로, Service 이동에 적합)
- [x] TODO/FIXME 없음

### Git 이력 확인
- [x] 초기 설정 또는 템플릿 코드로 판단됨 — 전체 코드가 55ca9e43 단일 커밋
- [x] 커밋 메시지에 리팩토링을 막을 만한 의도 없음

### 이후 단계 충돌 확인
- [x] README.md 구현 목록과 충돌 없음 — README.md 3-7에서 KakaoAuthService 추출을 명시적으로 계획하고 있음
- [x] 향후 작업에서 필요하지 않음 — 3-7이 최종 단계

### 추가 분석: AuthService와의 관계 — 별도 Service vs 통합

KakaoAuthController의 callback() 로직은 기존 AuthService의 register()/login()과 **유사한 패턴**을 공유합니다:

| 비교 항목 | AuthService.register() | KakaoAuthController.callback() |
|-----------|----------------------|-------------------------------|
| 회원 조회/생성 | `existsByEmail` → `new Member(email, password)` | `findByEmail` → `orElseGet(() -> new Member(email))` |
| 저장 | `memberRepository.save(member)` | `memberRepository.save(member)` |
| JWT 발급 | `jwtProvider.createToken(email)` | `jwtProvider.createToken(email)` |
| 추가 로직 | 비밀번호 중복 검증 | 카카오 토큰 교환 + 사용자 정보 조회 + kakaoAccessToken 저장 |

**설계 선택지:**

1. **별도 KakaoAuthService 생성** (README.md 3-7 계획)
   - `KakaoAuthService`: `KakaoLoginClient`, `KakaoLoginProperties`, `MemberRepository`, `JwtProvider` 주입
   - `buildLoginUrl()`: 인가 URL 생성
   - `processCallback(String code)`: 토큰 교환 → 사용자 조회 → 자동 등록 → JWT 발급
   - 장점: 카카오 OAuth 관련 로직이 하나의 Service에 응집. AuthService와 책임이 명확히 분리.
   - 단점: `MemberRepository`, `JwtProvider` 의존이 AuthService와 중복.

2. **AuthService에 통합** (`kakaoLogin(String code)` 메서드 추가)
   - AuthService에 `KakaoLoginClient`, `KakaoLoginProperties` 의존 추가
   - 장점: 인증 관련 로직이 하나의 Service에 모인다. 의존성 중복 없음.
   - 단점: AuthService의 책임이 커진다 (이메일/비밀번호 인증 + 카카오 OAuth). 카카오 관련 의존성이 AuthService에 섞인다.

**권장: 별도 KakaoAuthService 생성** — 카카오 OAuth는 외부 API 연동(토큰 교환, 사용자 정보 조회)이 핵심이므로, 이메일/비밀번호 인증과는 성격이 다르다. 별도 Service가 단일 책임 원칙에 부합.

### 추가 분석: login() 메서드 — URL 생성 로직

`login()` 메서드는 Kakao 인가 URL을 생성하여 302 리다이렉트합니다:
- `KakaoLoginProperties`에서 `clientId`, `redirectUri`를 가져와 URL 파라미터로 조립
- 순수한 URL 생성 로직으로, Controller에 두어도 무방하지만 Service로 이동하면 Controller가 더 얇아짐
- `scope` 값(`account_email,talk_message`)이 하드코딩되어 있음

### 추가 분석: 테스트 파일

- `KakaoAuthControllerTest`가 존재하지 않음
- `MemberTest`에 `updateKakaoAccessToken` 테스트가 존재 (src/test/java/gift/model/MemberTest.java:161)

## 5. 최종 판단

| 대상 | 판단 | 근거 요약 |
|------|------|-----------|
| KakaoAuthController → KakaoAuthService 추출 | 리팩토링 가능 | 초기 설정 코드(55ca9e43). 한 번도 수정된 적 없음. OAuth 콜백 전체 흐름이 Controller에 있음. README.md 3-7에서 명시적 계획 |
| AuthService와 통합 vs 별도 Service | 별도 KakaoAuthService 권장 | 카카오 OAuth는 외부 API 연동이 핵심. 이메일/비밀번호 인증과 성격이 다름. 단일 책임 원칙 |
| login() URL 생성 로직 Service 이동 | 리팩토링 가능 | URL 생성 로직을 Service로 이동하면 Controller가 순수 위임만 담당 |
| KakaoLoginClient 변경 | 변경 불필요 | 이미 REST API 호출을 잘 캡슐화하고 있음. KakaoAuthService에서 그대로 사용 |
| KakaoLoginProperties 변경 | 변경 불필요 | record 타입으로 설정값을 잘 관리하고 있음 |

## 6. 권장 작업 순서

1. `KakaoAuthService` 클래스 생성 (`src/main/java/gift/service/KakaoAuthService.java`)
   - `@Service` 어노테이션
   - `KakaoLoginClient`, `KakaoLoginProperties`, `MemberRepository`, `JwtProvider` 주입
   - `buildLoginUrl()`: 인가 URL 생성 (KakaoLoginProperties 사용)
   - `processCallback(String code)`: 토큰 교환 → 사용자 정보 조회 → 회원 자동 등록/조회 → kakaoAccessToken 저장 → JWT 발급 → `TokenResponse` 반환
2. `KakaoAuthController`에서 의존성을 `KakaoAuthService`로 교체
   - `MemberRepository`, `JwtProvider`, `KakaoLoginClient`, `KakaoLoginProperties` 의존 제거
   - `KakaoAuthService` 단일 의존으로 변경
   - `login()`: `kakaoAuthService.buildLoginUrl()` 호출 + 302 리다이렉트
   - `callback()`: `kakaoAuthService.processCallback(code)` 호출 + ResponseEntity 반환
3. 블록 주석(14-19라인)은 KakaoAuthService로 이동하거나 Controller에 유지 (선택)
4. 테스트 실행하여 동작 확인 (KakaoAuthControllerTest 부재 — 통합 테스트 또는 수동 확인 필요)
