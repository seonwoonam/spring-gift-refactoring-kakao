# 리팩토링 분석 보고서: OrderService 추출

> 분석 일시: 2026-02-26

## 분석 대상

- 설명: OrderController에 직접 구현된 주문 생성 흐름(옵션 조회 → 재고 차감 → 포인트 차감 → 주문 저장 → 카카오 알림 발송)을 OrderService로 추출. 프로젝트에서 가장 복잡한 Controller 메서드이며, 6개의 Repository/Client에 의존하고 있음.
- 관련 파일:
  - `src/main/java/gift/controller/OrderController.java` (REST API — 6개 의존성)
  - `src/main/java/gift/model/Order.java` (엔티티)
  - `src/main/java/gift/repository/OrderRepository.java`
  - `src/main/java/gift/dto/OrderRequest.java`
  - `src/main/java/gift/dto/OrderResponse.java`
  - `src/main/java/gift/client/KakaoMessageClient.java` (카카오 알림 전송)
  - `src/main/java/gift/repository/OptionRepository.java` (재고 차감에 사용)
  - `src/main/java/gift/repository/MemberRepository.java` (포인트 차감에 사용)
  - `src/main/java/gift/model/Member.java` (deductPoint 메서드)
  - `src/main/java/gift/repository/WishRepository.java` (주입되었으나 미사용)
  - `src/main/java/gift/auth/AuthenticationResolver.java` (인증 처리)

## 1. 대상 코드 현황

### OrderController 의존성 (6개 — 프로젝트 최다)

| # | 파일 | 라인 | 의존성 | 사용 여부 |
|---|------|------|--------|-----------|
| 1 | OrderController.java | 25 | `OrderRepository` | 사용중 — 주문 저장/조회 |
| 2 | OrderController.java | 26 | `OptionRepository` | 사용중 — 옵션 조회 + 재고 차감 저장 |
| 3 | OrderController.java | 27 | `WishRepository` | **미사용** — 주입만 되어 있고 메서드에서 호출 없음 |
| 4 | OrderController.java | 28 | `MemberRepository` | 사용중 — 포인트 차감 저장 |
| 5 | OrderController.java | 29 | `AuthenticationResolver` | 사용중 — JWT 인증 |
| 6 | OrderController.java | 30 | `KakaoMessageClient` | 사용중 — 카카오 알림 발송 |

### createOrder 흐름 (7단계 중 6단계 구현)

| # | 파일 | 라인 | 코드 내용 | 단계 |
|---|------|------|-----------|------|
| 1 | OrderController.java | 75-79 | `authenticationResolver.extractMember(authorization)` + null 체크 | 1. 인증 확인 |
| 2 | OrderController.java | 82-83 | `optionRepository.findById(request.optionId()).orElseThrow(...)` | 2. 옵션 조회/검증 |
| 3 | OrderController.java | 86-87 | `option.subtractQuantity(...)` + `optionRepository.save(option)` | 3. 재고 차감 |
| 4 | OrderController.java | 90-92 | `option.getProduct().getPrice() * request.quantity()` + `member.deductPoint(price)` + `memberRepository.save(member)` | 4. 포인트 차감 |
| 5 | OrderController.java | 95 | `orderRepository.save(new Order(...))` | 5. 주문 저장 |
| 6 | — | — | **(미구현)** 주석에 "6. cleanup wish"라 명시되었으나 코드 없음 | 6. 위시리스트 정리 |
| 7 | OrderController.java | 98 | `sendKakaoMessageIfPossible(member, saved, option)` | 7. 카카오 알림 |

### 카카오 알림 전송 (private 메서드)

| # | 파일 | 라인 | 코드 내용 | 사용 여부 |
|---|------|------|-----------|-----------|
| 8 | OrderController.java | 103-112 | `sendKakaoMessageIfPossible()` — kakaoAccessToken null 체크 + try-catch best-effort 전송 | 사용중 |
| 9 | KakaoMessageClient.java | 16-29 | `sendToMe()` — RestClient로 카카오 API 호출 | 사용중 |

### getOrders (주문 조회)

| # | 파일 | 라인 | 코드 내용 | 사용 여부 |
|---|------|------|-----------|-----------|
| 10 | OrderController.java | 53-57 | 인증 확인 (auth check 보일러플레이트) | 사용중 |
| 11 | OrderController.java | 58 | `orderRepository.findByMemberId(member.getId(), pageable)` | 사용중 |

## 2. 주석 및 TODO 분석

| # | 파일:라인 | 주석 내용 | 리팩토링에 영향 |
|---|-----------|-----------|-----------------|
| 1 | OrderController.java:62-69 | `// order flow: 1. auth check 2. validate option 3. subtract stock 4. deduct points 5. save order 6. cleanup wish 7. send kakao notification` | 있음 — **6. cleanup wish가 미구현**. Service 추출 시 구현 여부를 결정해야 함 |
| 2 | OrderController.java:53,75 | `// auth check` | 없음 — 인증 보일러플레이트 표시 |
| 3 | OrderController.java:81 | `// validate option` | 없음 — 단계 표시 |
| 4 | OrderController.java:85 | `// subtract stock` | 없음 — 단계 표시 |
| 5 | OrderController.java:89 | `// deduct points` | 없음 — 단계 표시 |
| 6 | OrderController.java:94 | `// save order` | 없음 — 단계 표시 |
| 7 | OrderController.java:97 | `// best-effort kakao notification` | 없음 — 실패 무시 전략 표시 |
| 8 | Order.java:24 | `// primitive FK` | 없음 — memberId가 엔티티 참조가 아닌 primitive FK임을 설명 |
| 9 | Member.java:56-57 | `// point deduction for order payment` | 없음 — deductPoint 메서드 용도 설명 |

### 핵심 발견: "cleanup wish" 미구현

주석(68라인)에 "6. cleanup wish"가 명시되어 있지만, 실제 코드에서 `wishRepository`가 메서드 내에서 호출되지 않습니다. `WishRepository`는 생성자에 주입만 되어 있고 사용되지 않는 상태입니다. 이는:
- 초기 설계 시 계획되었으나 구현이 누락된 것으로 보임
- Service 추출 시 구현 여부를 결정해야 함 (위시리스트에 있는 상품을 주문하면 위시리스트에서 자동 제거)

## 3. Git Blame 분석

| # | 파일:라인 | 작성자 | 커밋 | 날짜 | 커밋 메시지 |
|---|-----------|--------|------|------|-------------|
| 1 | OrderController.java 대부분 (1-113) | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |
| 2 | OrderController.java:20,82-83 | theo.cha | 7513bc95 | 2026-02-26 | refactor: orElseThrow()로 null 처리 패턴 통일 |
| 3 | KakaoMessageClient.java 전체 | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |
| 4 | Order.java 전체 | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |

### 커밋 상세

- **55ca9e43 (wotjd243, 2026-02-18)**: 프로젝트 초기 설정 커밋. OrderController, KakaoMessageClient, Order 엔티티 모두 포함. 62개 파일 일괄 추가. 초기 템플릿 코드.
- **7513bc95 (theo.cha, 2026-02-26)**: null 처리 패턴 통일. OrderController의 `optionRepository.findById()`를 `.orElseThrow()`로 변경. Service 추출에 영향 없음.

## 4. 리팩토링 영향도 판단

### 주석/TODO 의도 확인
- [ ] 의도를 설명하는 주석 없음 — **주석 있음**: "order flow" 주석이 7단계를 명시하며, 그 중 "6. cleanup wish"가 미구현
- [x] TODO/FIXME 없음

### Git 이력 확인
- [x] 초기 설정 또는 템플릿 코드로 판단됨
- [x] 커밋 메시지에 리팩토링을 막을 만한 의도 없음

### 이후 단계 충돌 확인
- [x] README.md 구현 목록과 충돌 없음 — README.md 3-6에서 OrderService 추출, 3-7에서 KakaoAuthService 추출을 명시적으로 계획
- [x] 향후 작업에서 필요하지 않음

### 추가 분석: 의존 도메인 수 (프로젝트 최다)

OrderController는 **6개 의존성**을 주입받으며, 이는 프로젝트 내 모든 Controller 중 최다입니다:

| Controller | 의존성 수 |
|-----------|----------|
| **OrderController** | **6개** (OrderRepository, OptionRepository, WishRepository, MemberRepository, AuthenticationResolver, KakaoMessageClient) |
| ProductController | 2개 |
| AdminProductController | 2개 |
| OptionController | 2개 |
| WishController | 3개 |
| CategoryController | 1개 |

이는 Controller가 너무 많은 책임을 가지고 있다는 명확한 신호입니다.

### 추가 분석: 트랜잭션 경계 부재

`createOrder()` 메서드는 다음 3개의 독립적인 저장 연산을 수행합니다:
1. `optionRepository.save(option)` — 재고 차감
2. `memberRepository.save(member)` — 포인트 차감
3. `orderRepository.save(new Order(...))` — 주문 저장

현재 `@Transactional`이 없으므로, 중간에 실패하면 데이터 불일치가 발생할 수 있습니다. Service 추출 시 `@Transactional` 적용이 자연스럽습니다.

### 추가 분석: 카카오 알림 분리 여부 (README.md 3-6 계획)

README.md 3-6에서는 두 가지를 제안합니다:
- `OrderService`: 주문 처리(조회, 차감, 저장)
- `KakaoNotificationService`: 카카오 알림 별도 분리

그러나 현재 `KakaoMessageClient`가 이미 REST API 호출을 캡슐화하고 있고, `sendKakaoMessageIfPossible()`은 null 체크 + try-catch 래핑만 합니다. 별도 `KakaoNotificationService`를 만들면 중간 계층이 하나 더 생기므로, OrderService 내부에서 `KakaoMessageClient`를 직접 호출하는 것이 간결합니다.

### 추가 분석: WishRepository 미사용 — cleanup wish 미구현

- `WishRepository`가 주입되어 있지만 실제 사용되지 않음
- 주석 "6. cleanup wish"가 미구현 상태
- 주문 시 해당 상품이 위시리스트에 있으면 자동 제거하는 기능으로 추정
- Service 추출 시 이 기능의 구현 여부를 결정해야 함

## 5. 최종 판단

| 대상 | 판단 | 근거 요약 |
|------|------|-----------|
| OrderController → OrderService 추출 | 리팩토링 가능 | 프로젝트 최복잡 메서드. 6개 의존성. 트랜잭션 경계 부재. README.md 3-6에서 명시적 계획 |
| @Transactional 적용 | 리팩토링 가능 | 3개 독립 저장 연산이 트랜잭션 없이 실행 중. 데이터 일관성 위험 |
| KakaoNotificationService 별도 분리 | 리팩토링 보류 | KakaoMessageClient가 이미 API 호출을 캡슐화. 중간 계층 추가는 과도. OrderService 내에서 직접 호출 권장 |
| WishRepository 미사용 제거 or "cleanup wish" 구현 | 추가 확인 필요 | 주석에 계획되어 있으나 미구현. 요구사항 확인 후 결정 |
| 인증 보일러플레이트 (auth check) 제거 | 리팩토링 보류 | WishService(3-5)에서 HandlerMethodArgumentResolver 도입 시 함께 처리하는 것이 효율적 |

## 6. 권장 작업 순서

1. `OrderService` 클래스 생성 (`src/main/java/gift/service/OrderService.java`)
   - `@Service`, `@Transactional` 어노테이션
   - `OrderRepository`, `OptionRepository`, `MemberRepository`, `KakaoMessageClient` 주입
   - `findByMemberId(Long memberId, Pageable pageable)`: 주문 목록 조회
   - `createOrder(Member member, OrderRequest request)`: 옵션 조회 → 재고 차감 → 포인트 차감 → 주문 저장 → 카카오 알림
   - `sendKakaoMessageIfPossible()` 로직을 Service로 이동
2. **WishRepository 처리 결정**
   - "cleanup wish" 구현: 주문 생성 시 해당 상품이 위시리스트에 있으면 자동 제거
   - 또는: WishRepository 주입 제거 + 주석 "6. cleanup wish" 제거
3. `OrderController`에서 의존성을 `OrderService` + `AuthenticationResolver`로 축소
   - Controller는 인증 확인 + Service 호출 + ResponseEntity 반환만 담당
4. 기존 테스트(`OrderControllerTest`) 실행하여 동작 확인
5. KakaoNotificationService 별도 분리는 불필요 — KakaoMessageClient가 이미 캡슐화하고 있으므로 OrderService에서 직접 호출
