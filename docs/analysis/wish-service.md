# 리팩토링 분석 보고서: WishService 추출

> 분석 일시: 2026-02-26

## 분석 대상

- 설명: WishController에 직접 구현된 위시리스트 CRUD + 인증 확인 + 상품 존재 검증 + 중복 확인 + 소유권 검증 비즈니스 로직을 WishService로 추출. 인증 보일러플레이트(`extractMember` + null 체크)가 매 메서드마다 반복되는 문제도 함께 분석.
- 관련 파일:
  - `src/main/java/gift/controller/WishController.java` (REST API — 3개 의존성)
  - `src/main/java/gift/model/Wish.java` (엔티티)
  - `src/main/java/gift/repository/WishRepository.java`
  - `src/main/java/gift/dto/WishRequest.java`
  - `src/main/java/gift/dto/WishResponse.java`
  - `src/main/java/gift/repository/ProductRepository.java` (상품 존재 검증에 사용)
  - `src/main/java/gift/auth/AuthenticationResolver.java` (인증 처리)
  - `src/main/java/gift/controller/OrderController.java` (WishRepository 주입 — 미사용)

## 1. 대상 코드 현황

### WishController (REST API)

| # | 파일 | 라인 | 코드 내용 | 사용 여부 |
|---|------|------|-----------|-----------|
| 1 | WishController.java | 24-26 | `WishRepository`, `ProductRepository`, `AuthenticationResolver` 주입 | 사용중 |
| 2 | WishController.java | 43-47 | `authenticationResolver.extractMember(authorization)` + null 체크 | 사용중 — 인증 보일러플레이트 (getWishes) |
| 3 | WishController.java | 48 | `wishRepository.findByMemberId(member.getId(), pageable)` | 사용중 — 위시 목록 조회 |
| 4 | WishController.java | 57-61 | `authenticationResolver.extractMember(authorization)` + null 체크 | 사용중 — 인증 보일러플레이트 (addWish) |
| 5 | WishController.java | 64-65 | `productRepository.findById(request.productId()).orElseThrow(...)` | 사용중 — 상품 존재 검증 |
| 6 | WishController.java | 68-71 | `wishRepository.findByMemberIdAndProductId(...)` + 중복 확인 | 사용중 — 비즈니스 규칙 (중복 위시 방지) |
| 7 | WishController.java | 73 | `wishRepository.save(new Wish(member.getId(), product))` | 사용중 — 위시 생성 |
| 8 | WishController.java | 83-87 | `authenticationResolver.extractMember(authorization)` + null 체크 | 사용중 — 인증 보일러플레이트 (removeWish) |
| 9 | WishController.java | 89-90 | `wishRepository.findById(id).orElseThrow(...)` | 사용중 — 위시 조회 |
| 10 | WishController.java | 92-94 | `wish.getMemberId().equals(member.getId())` — 소유권 검증 | 사용중 — 비즈니스 규칙 (타인 위시 삭제 방지) |
| 11 | WishController.java | 96 | `wishRepository.delete(wish)` | 사용중 — 위시 삭제 |

### 외부 도메인의 WishRepository 사용

| # | 파일 | 라인 | 코드 내용 | 사용 여부 |
|---|------|------|-----------|-----------|
| 12 | OrderController.java | 27,35,42 | `WishRepository wishRepository` — 주입만 되고 호출 없음 | **미사용** — "cleanup wish" 미구현 |

### 인증 보일러플레이트 반복 현황 (프로젝트 전체)

| # | 파일 | 반복 횟수 | 패턴 |
|---|------|----------|------|
| 1 | WishController.java | **3곳** (43, 57, 83라인) | `extractMember(authorization)` + null 체크 + 401 반환 |
| 2 | OrderController.java | **2곳** (54, 76라인) | 동일 패턴 |
| — | **합계** | **5곳** | — |

### 비즈니스 규칙 요약

1. **위시 중복 방지**: 같은 회원이 같은 상품을 중복 등록하면 기존 위시를 반환 (addWish:68-71)
2. **소유권 검증**: 위시 삭제 시 본인 위시만 삭제 가능 (removeWish:92-94)
3. **상품 존재 검증**: 위시 추가 시 상품이 존재하는지 확인 (addWish:64-65)

## 2. 주석 및 TODO 분석

| # | 파일:라인 | 주석 내용 | 리팩토링에 영향 |
|---|-----------|-----------|-----------------|
| 1 | WishController.java:43,57,83 | `// check auth` | 없음 — 인증 보일러플레이트 표시. Service 추출 시 Controller에 유지되거나 ArgumentResolver로 대체 |
| 2 | WishController.java:63 | `// check product` | 없음 — 단계 표시 |
| 3 | WishController.java:67 | `// check duplicate` | 없음 — 비즈니스 규칙 표시 |
| 4 | Wish.java:16 | `// primitive FK - no entity reference` | 없음 — memberId가 엔티티 참조가 아닌 primitive FK임을 설명 |

> TODO, FIXME 등의 특수 주석은 없습니다.

## 3. Git Blame 분석

| # | 파일:라인 | 작성자 | 커밋 | 날짜 | 커밋 메시지 |
|---|-----------|--------|------|------|-------------|
| 1 | WishController.java 대부분 | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |
| 2 | WishController.java:19,64-65,68-70,89-90 | theo.cha | 7513bc95 | 2026-02-26 | refactor: orElseThrow()로 null 처리 패턴 통일 |
| 3 | Wish.java 전체 | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |
| 4 | WishRepository.java 전체 | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |

### 커밋 상세

- **55ca9e43 (wotjd243, 2026-02-18)**: 프로젝트 초기 설정 커밋. WishController, Wish 엔티티, WishRepository 포함. 초기 템플릿 코드.
- **7513bc95 (theo.cha, 2026-02-26)**: null 처리 패턴 통일. `.orElse(null)` → `.orElseThrow()` 변경, 중복 체크 로직 정리. Service 추출에 영향 없음.

## 4. 리팩토링 영향도 판단

### 주석/TODO 의도 확인
- [x] 의도를 설명하는 주석 없음 (단계 표시 주석만 존재)
- [x] TODO/FIXME 없음

### Git 이력 확인
- [x] 초기 설정 또는 템플릿 코드로 판단됨
- [x] 커밋 메시지에 리팩토링을 막을 만한 의도 없음

### 이후 단계 충돌 확인
- [x] README.md 구현 목록과 충돌 없음 — README.md 3-5에서 WishService 추출을 명시적으로 계획하고 있음
- [x] 향후 작업에서 필요하지 않음

### 추가 분석: 인증 보일러플레이트 (프로젝트 전체 5곳 반복)

WishController에서만 **3곳**, OrderController에서 **2곳** — 총 **5곳**에서 동일한 패턴이 반복됩니다:

```java
var member = authenticationResolver.extractMember(authorization);
if (member == null) {
    return ResponseEntity.status(401).build();
}
```

README.md 3-5에서는 `HandlerMethodArgumentResolver` 도입을 제안하고 있습니다:
- `@LoginMember Member member` 커스텀 어노테이션 + ArgumentResolver
- 장점: 인증 보일러플레이트 완전 제거. Controller 메서드 시그니처가 깔끔해짐
- 단점: ArgumentResolver 클래스 추가. 구조 변경 범위가 넓음 (WishController + OrderController 모두 영향)

### 추가 분석: OrderController의 "cleanup wish" 미구현과의 관계

OrderService 분석 보고서에서 발견한 "cleanup wish" 미구현 이슈가 WishService와 직접 관련됩니다:
- OrderController에 `WishRepository`가 주입되어 있지만 사용되지 않음
- 주문 완료 시 위시리스트에서 해당 상품 자동 제거 기능이 계획되어 있었으나 미구현
- WishService에 `deleteByMemberIdAndProductId(Long memberId, Long productId)` 메서드를 제공하면, OrderService에서 호출 가능

### 추가 분석: ProductRepository 의존

- WishController에서 `productRepository.findById()`로 상품 존재를 검증 (1곳)
- ProductService가 선행 추출되었다면 `productService.findById()`로 대체 가능
- 미추출이면 ProductRepository 직접 유지

## 5. 최종 판단

| 대상 | 판단 | 근거 요약 |
|------|------|-----------|
| WishController → WishService 추출 | 리팩토링 가능 | 초기 설정 코드. 비즈니스 규칙(중복 방지, 소유권 검증)이 Controller에 혼재. README.md 3-5에서 명시적 계획 |
| HandlerMethodArgumentResolver 도입 | 리팩토링 가능 | 인증 보일러플레이트 5곳 반복. WishController(3곳) + OrderController(2곳) 모두 개선 가능 |
| "cleanup wish" 메서드 제공 | 추가 확인 필요 | OrderService의 미구현 기능과 연관. WishService에 메서드만 제공하고, 실제 호출은 OrderService 추출 시 결정 |
| ProductRepository 의존 → ProductService로 대체 | 추가 확인 필요 | ProductService 추출(3-3) 선행 여부에 따라 결정 |

## 6. 권장 작업 순서

1. `WishService` 클래스 생성 (`src/main/java/gift/service/WishService.java`)
   - `@Service` 어노테이션
   - `WishRepository`, `ProductRepository` (또는 ProductService) 주입
   - `findByMemberId(Long memberId, Pageable pageable)`: 위시 목록 조회
   - `addWish(Long memberId, Long productId)`: 상품 존재 검증 + 중복 확인 + 위시 생성
   - `removeWish(Long memberId, Long wishId)`: 위시 조회 + 소유권 검증 + 삭제
   - `deleteByMemberIdAndProductId(Long memberId, Long productId)`: OrderService 연동용 (cleanup wish)
2. (선택) `@LoginMember` 커스텀 어노테이션 + `LoginMemberArgumentResolver` 생성
   - `HandlerMethodArgumentResolver` 구현
   - WebMvcConfigurer에 ArgumentResolver 등록
   - 인증 실패 시 예외를 던지고 GlobalExceptionHandler에서 401 매핑
3. `WishController`에서 의존성을 `WishService` (+ AuthenticationResolver 또는 @LoginMember)로 교체
   - Controller는 인증 확인 + Service 호출 + ResponseEntity 반환만 담당
4. 기존 테스트(`WishControllerTest`) 실행하여 동작 확인
5. ArgumentResolver를 도입했다면 OrderController에도 동일하게 적용
