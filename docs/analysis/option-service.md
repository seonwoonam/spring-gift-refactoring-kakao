# 리팩토링 분석 보고서: OptionService 추출

> 분석 일시: 2026-02-26

## 분석 대상

- 설명: OptionController에 직접 구현된 옵션 CRUD, 이름 검증, 중복 확인, 최소 옵션 개수 검증, 상품 소속 확인 등의 비즈니스 로직을 OptionService로 추출. OrderController에서 OptionRepository를 직접 사용하여 재고 차감하는 로직도 함께 분석.
- 관련 파일:
  - `src/main/java/gift/controller/OptionController.java` (REST API)
  - `src/main/java/gift/model/Option.java` (엔티티)
  - `src/main/java/gift/repository/OptionRepository.java`
  - `src/main/java/gift/dto/OptionRequest.java`
  - `src/main/java/gift/dto/OptionResponse.java`
  - `src/main/java/gift/controller/OrderController.java` (옵션 재고 차감 호출)
  - `src/main/java/gift/common/NameValidator.java` (검증 유틸)
  - `src/main/java/gift/repository/ProductRepository.java` (상품 존재 검증에 사용)

## 1. 대상 코드 현황

### OptionController (REST API)

| # | 파일 | 라인 | 코드 내용 | 사용 여부 |
|---|------|------|-----------|-----------|
| 1 | OptionController.java | 27-28 | `OptionRepository`, `ProductRepository` 직접 주입 | 사용중 |
| 2 | OptionController.java | 37-38 | `productRepository.findById(productId).orElseThrow(...)` | 사용중 - 옵션 조회 시 상품 존재 검증 |
| 3 | OptionController.java | 39-41 | `optionRepository.findByProductId(productId).stream().map(...)` | 사용중 - 상품별 옵션 목록 조회 |
| 4 | OptionController.java | 50 | `validateName(request.name())` | 사용중 - 옵션명 검증 |
| 5 | OptionController.java | 52-53 | `productRepository.findById(productId).orElseThrow(...)` | 사용중 - 옵션 생성 시 상품 존재 검증 |
| 6 | OptionController.java | 55-57 | `optionRepository.existsByProductIdAndName(...)` 중복 확인 | 사용중 - 비즈니스 규칙 |
| 7 | OptionController.java | 59 | `optionRepository.save(new Option(...))` | 사용중 - 옵션 생성 |
| 8 | OptionController.java | 70-71 | `productRepository.findById(productId).orElseThrow(...)` | 사용중 - 옵션 삭제 시 상품 존재 검증 |
| 9 | OptionController.java | 73-76 | `optionRepository.findByProductId(productId)` + 최소 개수 검증 | 사용중 - 비즈니스 규칙(최소 1개) |
| 10 | OptionController.java | 78-82 | `optionRepository.findById(optionId)` + 상품 소속 확인 | 사용중 - 비즈니스 규칙 |
| 11 | OptionController.java | 84 | `optionRepository.delete(option)` | 사용중 - 옵션 삭제 |
| 12 | OptionController.java | 88-93 | `validateName()` private 메서드 — `NameValidator.validate(name, "Option name", 50)` | 사용중 - 옵션명 검증 로직 |

### OrderController (옵션 재고 차감)

| # | 파일 | 라인 | 코드 내용 | 사용 여부 |
|---|------|------|-----------|-----------|
| 13 | OrderController.java | 26 | `private final OptionRepository optionRepository` | 사용중 |
| 14 | OrderController.java | 82-83 | `optionRepository.findById(request.optionId()).orElseThrow(...)` | 사용중 - 주문 시 옵션 조회 |
| 15 | OrderController.java | 86-87 | `option.subtractQuantity(request.quantity())` + `optionRepository.save(option)` | 사용중 - 재고 차감 |

### 중복 패턴 요약

- **`productRepository.findById(productId).orElseThrow(...)`** 패턴이 OptionController 내에서만 **3곳** 반복
- **`optionRepository.findById(...).orElseThrow(...)`** 패턴이 **2곳** (OptionController 1곳, OrderController 1곳)
- **상품 존재 검증 + 옵션 CRUD** 결합이 모든 엔드포인트에서 반복 — Service로 캡슐화 대상

## 2. 주석 및 TODO 분석

| # | 파일:라인 | 주석 내용 | 리팩토링에 영향 |
|---|-----------|-----------|-----------------|
| 1 | OptionController.java:20-23 | `/* Each product must have at least one option at all times. Option names are validated against allowed characters and length constraints. */` | 없음 — 비즈니스 규칙 설명 주석. Service로 이동 시 이 규칙이 Service에 캡슐화되므로 오히려 적합 |

> option 패키지 내에 TODO, FIXME 등의 주석은 없습니다.

## 3. Git Blame 분석

| # | 파일:라인 | 작성자 | 커밋 | 날짜 | 커밋 메시지 |
|---|-----------|--------|------|------|-------------|
| 1 | OptionController.java 대부분 | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |
| 2 | OptionController.java:3 (NameValidator import) | theo.cha | c89e65ee | 2026-02-26 | refactor: Validator 중복 코드를 NameValidator로 통합 |
| 3 | OptionController.java:18,37-38,41,52-53,56,70-71,75,78-81,89 | theo.cha | 7513bc95 | 2026-02-26 | refactor: orElseThrow()로 null 처리 패턴 통일 |
| 4 | OptionController.java:89 (NameValidator.validate 호출) | theo.cha | c89e65ee | 2026-02-26 | refactor: Validator 중복 코드를 NameValidator로 통합 |
| 5 | OrderController.java:82-87 (옵션 재고 차감) | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |

### 커밋 상세

- **55ca9e43 (wotjd243, 2026-02-18)**: 프로젝트 초기 설정 커밋. OptionController를 포함한 전체 구조 생성. 초기 템플릿 코드.
- **7513bc95 (theo.cha, 2026-02-26)**: null 처리 패턴 통일. `.orElse(null)` → `.orElseThrow()` 변경. OptionService 추출과 양립 가능.
- **c89e65ee (theo.cha, 2026-02-26)**: Validator 중복 코드 통합. `OptionNameValidator` → `NameValidator`로 교체. OptionService 추출 시 `NameValidator` 호출을 Service로 이동하면 됨.

## 4. 리팩토링 영향도 판단

### 주석/TODO 의도 확인
- [x] 의도를 설명하는 주석 없음 (블록 주석은 비즈니스 규칙 설명으로, Service 이동에 적합)
- [x] TODO/FIXME 없음

### Git 이력 확인
- [x] 초기 설정 또는 템플릿 코드로 판단됨
- [x] 커밋 메시지에 리팩토링을 막을 만한 의도 없음

### 이후 단계 충돌 확인
- [x] README.md 구현 목록과 충돌 없음 — README.md 3-4에서 OptionService 추출을 명시적으로 계획하고 있음
- [x] 향후 작업에서 필요하지 않음 — OrderService 추출(3-6)에서 `OptionService.subtractQuantity()`를 호출할 수 있으므로 선행 작업으로 적합

### 추가 분석: OptionController의 비즈니스 로직 복잡도

OptionController는 다른 Controller들과 달리 **비즈니스 규칙이 복잡**합니다:

1. **옵션명 중복 검증**: `existsByProductIdAndName()` — 같은 상품 내 옵션명 중복 불가
2. **최소 옵션 개수 검증**: `options.size() <= 1` — 상품당 최소 1개 옵션 필수
3. **상품 소속 검증**: `option.getProduct().getId().equals(productId)` — 옵션이 해당 상품에 속하는지 확인
4. **이름 유효성 검증**: `NameValidator.validate(name, "Option name", 50)` — 특수문자, 길이 제한

이러한 규칙들이 Controller에 산재해 있어, Service 추출의 가치가 높습니다.

### 추가 분석: OrderController와의 의존관계

- OrderController(OrderController.java:82-87)에서 `optionRepository.findById()` + `option.subtractQuantity()` + `optionRepository.save(option)` 패턴으로 재고 차감
- OptionService에 `subtractQuantity(Long optionId, int quantity)` 메서드를 제공하면 OrderService(3-6) 추출 시 깔끔하게 의존 가능
- 현재 `Option.subtractQuantity()`는 엔티티 메서드로, 도메인 로직은 이미 잘 분리되어 있음. Service는 조회 + 엔티티 호출 + 저장을 캡슐화하는 역할

## 5. 최종 판단

| 대상 | 판단 | 근거 요약 |
|------|------|-----------|
| OptionController → OptionService 추출 | 리팩토링 가능 | 초기 설정 코드. 비즈니스 규칙(중복 확인, 최소 개수, 소속 확인)이 Controller에 혼재. README.md 3-4에서 명시적 계획 |
| OrderController의 optionRepository 사용 → OptionService로 대체 | 추가 확인 필요 | OrderService 추출(3-6)과 함께 처리하는 것이 자연스러움. 단, `subtractQuantity` 메서드를 OptionService에서 제공하면 3-6에서 활용 가능 |
| ProductRepository 의존 제거 | 추가 확인 필요 | ProductService 추출(3-3)이 선행되었다면 `productService.findById()`로 대체 가능. 미추출이면 ProductRepository 직접 유지 |

## 6. 권장 작업 순서

1. `OptionService` 클래스 생성 (`src/main/java/gift/service/OptionService.java`)
   - `@Service` 어노테이션
   - `OptionRepository`, `ProductRepository` (또는 ProductService) 주입
   - `findByProductId(Long productId)`: 상품 존재 검증 + 옵션 목록 조회
   - `create(Long productId, OptionRequest request)`: 이름 검증 + 상품 존재 검증 + 중복 확인 + 저장
   - `delete(Long productId, Long optionId)`: 상품 존재 검증 + 최소 개수 검증 + 소속 확인 + 삭제
   - `subtractQuantity(Long optionId, int quantity)`: 옵션 조회 + 재고 차감 + 저장 (OrderService 연동용)
   - `NameValidator.validate(name, "Option name", 50)` 검증을 Service 내부에서 수행
2. `OptionController`에서 `OptionRepository`, `ProductRepository` 의존을 `OptionService`로 교체
   - `validateName()` private 메서드 제거 (Service로 이동)
   - Controller는 요청 파싱 + 응답 변환(ResponseEntity)만 담당
3. 기존 테스트(`OptionControllerTest`) 실행하여 동작 확인
4. OrderController의 optionRepository 사용은 3-6(OrderService 추출) 단계에서 `OptionService.subtractQuantity()` 호출로 대체
