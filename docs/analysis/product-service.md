# 리팩토링 분석 보고서: ProductService 추출

> 분석 일시: 2026-02-26

## 분석 대상

- 설명: ProductController와 AdminProductController에 직접 구현된 상품 CRUD + 검증 비즈니스 로직을 ProductService로 추출. 두 컨트롤러가 ProductRepository와 CategoryRepository를 직접 호출하고, 이름 검증(NameValidator)도 Controller에서 수행하고 있음.
- 관련 파일:
  - `src/main/java/gift/controller/ProductController.java` (REST API)
  - `src/main/java/gift/controller/AdminProductController.java` (Thymeleaf 관리자 페이지)
  - `src/main/java/gift/model/Product.java` (엔티티)
  - `src/main/java/gift/repository/ProductRepository.java`
  - `src/main/java/gift/dto/ProductRequest.java`
  - `src/main/java/gift/dto/ProductResponse.java`
  - `src/main/java/gift/common/NameValidator.java` (검증 유틸)
  - `src/main/java/gift/repository/CategoryRepository.java` (카테고리 조회에 사용)

## 1. 대상 코드 현황

### ProductController (REST API)

| # | 파일 | 라인 | 코드 내용 | 사용 여부 |
|---|------|------|-----------|-----------|
| 1 | ProductController.java | 27-28 | `ProductRepository`, `CategoryRepository` 직접 주입 | 사용중 |
| 2 | ProductController.java | 37 | `productRepository.findAll(pageable).map(ProductResponse::from)` | 사용중 - 페이징 조회 |
| 3 | ProductController.java | 43-44 | `productRepository.findById(id).orElseThrow(...)` | 사용중 - 단건 조회 |
| 4 | ProductController.java | 50 | `validateName(request.name())` | 사용중 - 상품명 검증 |
| 5 | ProductController.java | 52-53 | `categoryRepository.findById(...).orElseThrow(...)` | 사용중 - 카테고리 검증 (생성) |
| 6 | ProductController.java | 55 | `productRepository.save(request.toEntity(category))` | 사용중 - 상품 생성 |
| 7 | ProductController.java | 65-68 | `validateName` + `categoryRepository.findById` + `productRepository.findById` | 사용중 - 상품 수정 |
| 8 | ProductController.java | 73-74 | `product.update(...)` + `productRepository.save(product)` | 사용중 - 상품 수정 저장 |
| 9 | ProductController.java | 80 | `productRepository.deleteById(id)` | 사용중 - 상품 삭제 |
| 10 | ProductController.java | 84-89 | `validateName()` private 메서드 — `NameValidator.validate(name, "Product name", 15, true)` | 사용중 - 상품명 검증 로직 |

### AdminProductController (관리자 Thymeleaf)

| # | 파일 | 라인 | 코드 내용 | 사용 여부 |
|---|------|------|-----------|-----------|
| 11 | AdminProductController.java | 20-21 | `ProductRepository`, `CategoryRepository` 직접 주입 | 사용중 |
| 12 | AdminProductController.java | 30 | `productRepository.findAll()` | 사용중 - 전체 조회 |
| 13 | AdminProductController.java | 36,65,114,132 | `categoryRepository.findAll()` | 사용중 - 카테고리 목록 조회 (4곳) |
| 14 | AdminProductController.java | 48 | `NameValidator.validate(name, "Product name", 15)` | 사용중 - 상품명 검증 (생성) |
| 15 | AdminProductController.java | 54-55 | `categoryRepository.findById(...).orElseThrow(...)` | 사용중 - 카테고리 검증 (생성) |
| 16 | AdminProductController.java | 56 | `productRepository.save(new Product(...))` | 사용중 - 상품 생성 |
| 17 | AdminProductController.java | 62-63 | `productRepository.findById(id).orElseThrow(...)` | 사용중 - 단건 조회 (수정 폼) |
| 18 | AdminProductController.java | 78-79 | `productRepository.findById(id).orElseThrow(...)` | 사용중 - 단건 조회 (수정) |
| 19 | AdminProductController.java | 81 | `NameValidator.validate(name, "Product name", 15)` | 사용중 - 상품명 검증 (수정) |
| 20 | AdminProductController.java | 87-88 | `categoryRepository.findById(...).orElseThrow(...)` | 사용중 - 카테고리 검증 (수정) |
| 21 | AdminProductController.java | 90-91 | `product.update(...)` + `productRepository.save(product)` | 사용중 - 상품 수정 저장 |
| 22 | AdminProductController.java | 97 | `productRepository.deleteById(id)` | 사용중 - 상품 삭제 |

### 외부 도메인의 ProductRepository 직접 사용

| # | 파일 | 라인 | 코드 내용 | 비고 |
|---|------|------|-----------|------|
| 23 | WishController.java | 25,64 | `productRepository.findById(...)` | 위시리스트 추가 시 상품 존재 검증 |
| 24 | OptionController.java | 28,37,52,70 | `productRepository.findById(...)` | 옵션 CRUD 시 상품 존재 검증 |

### 중복 패턴 요약

- **`productRepository.findById(id).orElseThrow(...)`** 패턴이 **7곳**에서 반복 (ProductController 3곳, AdminProductController 2곳, WishController 1곳, OptionController 3곳에서 `findById` 호출)
- **`categoryRepository.findById(...).orElseThrow(...)`** 패턴이 **4곳**에서 반복 (ProductController 2곳, AdminProductController 2곳)
- **`NameValidator.validate(name, "Product name", 15, ...)`** 검증이 **4곳**에서 반복 (ProductController 2곳 호출, AdminProductController 2곳)
- **`categoryRepository.findAll()`** 패턴이 AdminProductController에서 **4곳** 반복
- AdminProductController에 `checkKakao=false`로 호출하지만 ProductController에서는 `checkKakao=true`로 호출하는 **불일치** 존재

## 2. 주석 및 TODO 분석

> 대상 코드 주변에 의도를 설명하는 주석이 없습니다.

product 패키지 전체에 `//`, TODO, FIXME, NOTE 등의 주석이 없습니다.

## 3. Git Blame 분석

| # | 파일:라인 | 작성자 | 커밋 | 날짜 | 커밋 메시지 |
|---|-----------|--------|------|------|-------------|
| 1 | ProductController.java 대부분 | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |
| 2 | ProductController.java:18-19 (NameValidator import) | theo.cha | c89e65ee | 2026-02-26 | refactor: Validator 중복 코드를 NameValidator로 통합 |
| 3 | ProductController.java:22,43-44,52-53,67-68,70-71 | theo.cha | 7513bc95 | 2026-02-26 | refactor: orElseThrow()로 null 처리 패턴 통일 |
| 4 | ProductController.java:85 (NameValidator.validate 호출) | theo.cha | c89e65ee | 2026-02-26 | refactor: Validator 중복 코드를 NameValidator로 통합 |
| 5 | AdminProductController.java 대부분 | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |
| 6 | AdminProductController.java:5 (NameValidator import) | theo.cha | c89e65ee | 2026-02-26 | refactor: Validator 중복 코드를 NameValidator로 통합 |
| 7 | AdminProductController.java:48,81 (NameValidator.validate) | theo.cha | c89e65ee | 2026-02-26 | refactor: Validator 중복 코드를 NameValidator로 통합 |
| 8 | AdminProductController.java:55,63,79,88 (orElseThrow) | theo.cha | 768789ed | 2026-02-26 | style: 에러 메시지를 영어로 통일 |

### 커밋 상세

- **55ca9e43 (wotjd243, 2026-02-18)**: 프로젝트 초기 설정 커밋. ProductController, AdminProductController를 포함한 전체 구조 생성. 초기 템플릿 코드.
- **7513bc95 (theo.cha, 2026-02-26)**: null 처리 패턴 통일. `.orElse(null)` → `.orElseThrow()` 변경. ProductService 추출과 양립 가능하며, 추출 후에도 동일 패턴 유지.
- **c89e65ee (theo.cha, 2026-02-26)**: Validator 중복 코드 통합. `ProductNameValidator` → `NameValidator`로 교체. ProductService 추출 시 `NameValidator` 호출을 Service로 이동하면 됨.
- **768789ed (theo.cha, 2026-02-26)**: 에러 메시지 영어 통일. AdminProductController의 `orElseThrow` 메시지 변경. 추출에 영향 없음.

## 4. 리팩토링 영향도 판단

### 주석/TODO 의도 확인
- [x] 의도를 설명하는 주석 없음
- [x] TODO/FIXME 없음

### Git 이력 확인
- [x] 초기 설정 또는 템플릿 코드로 판단됨
- [x] 커밋 메시지에 리팩토링을 막을 만한 의도 없음

### 이후 단계 충돌 확인
- [x] README.md 구현 목록과 충돌 없음 — README.md 3-3에서 ProductService 추출을 명시적으로 계획하고 있음
- [x] 향후 작업에서 필요하지 않음 — OptionService(3-4), WishService(3-5) 추출 시 ProductService를 의존할 수 있으므로 선행 작업으로 적합

### 추가 분석: NameValidator checkKakao 불일치

- **ProductController:85** → `NameValidator.validate(name, "Product name", 15, true)` (카카오 검증 O)
- **AdminProductController:48,81** → `NameValidator.validate(name, "Product name", 15)` (카카오 검증 X — 기본값 false)
- REST API와 관리자 페이지에서 동일한 상품 생성/수정인데 검증 규칙이 다름. ProductService로 추출 시 이 불일치를 통일해야 함.

### 추가 분석: 외부 도메인 의존관계

- **WishController**: `productRepository.findById()`로 상품 존재를 검증. ProductService 추출 후 `productService.findById()`를 제공하면 WishService(3-5)에서 활용 가능.
- **OptionController**: `productRepository.findById()`로 상품 존재를 검증 (3곳). OptionService(3-4) 추출 시 ProductService를 의존하는 것이 자연스러움.
- 단, 3-4와 3-5는 각각의 Service 추출 단계에서 처리하는 것이 범위 관리에 유리.

### 추가 분석: AdminProductController의 categoryRepository.findAll() 사용

- AdminProductController에서 카테고리 목록 조회(`categoryRepository.findAll()`)가 4곳에서 반복됨.
- CategoryService 추출(3-2)이 선행되었다면 `categoryService.findAll()`로 대체 가능.
- CategoryService가 아직 미추출이라면, ProductService에서 CategoryRepository를 직접 의존하되, 추후 CategoryService로 전환.

## 5. 최종 판단

| 대상 | 판단 | 근거 요약 |
|------|------|-----------|
| ProductController → ProductService 추출 | 리팩토링 가능 | 초기 설정 코드. 주석/의도 없음. README.md 3-3에서 명시적 계획. CRUD + 검증 로직이 Controller에 혼재 |
| AdminProductController → ProductService 공유 | 리팩토링 가능 | 동일한 비즈니스 로직(생성, 수정, 삭제, 검증)을 AdminProductController에서도 사용. Service 공유가 자연스러움 |
| NameValidator checkKakao 불일치 수정 | 리팩토링 가능 | REST API는 `checkKakao=true`, Admin은 `checkKakao=false`로 불일치. Service 추출 시 통일 |
| WishController/OptionController의 productRepository 사용 | 리팩토링 보류 | 각각 3-4(OptionService), 3-5(WishService) 추출에서 처리하는 것이 적절 |

## 6. 권장 작업 순서

1. `ProductService` 클래스 생성 (`src/main/java/gift/service/ProductService.java`)
   - `@Service` 어노테이션
   - `ProductRepository`, `CategoryRepository` 주입
   - `findAll(Pageable)`, `findById(Long)`, `create(ProductRequest)`, `update(Long, ProductRequest)`, `delete(Long)` 메서드 구현
   - `NameValidator.validate(name, "Product name", 15, true)` 검증을 Service 내부에서 수행
2. `ProductController`에서 `ProductRepository`, `CategoryRepository` 의존을 `ProductService`로 교체
   - `validateName()` private 메서드 제거 (Service로 이동)
   - Controller는 요청 파싱 + 응답 변환(ResponseEntity)만 담당
3. `AdminProductController`에서 `ProductRepository` 의존을 `ProductService`로 교체
   - 상품 CRUD 로직은 `ProductService` 호출
   - `NameValidator` 호출을 `ProductService`에 위임하여 `checkKakao` 불일치 해소
   - `categoryRepository.findAll()`은 카테고리 목록 표시용이므로 CategoryService 또는 CategoryRepository 직접 유지 (3-2 선행 여부에 따라 결정)
4. 기존 테스트(`ProductControllerTest`) 실행하여 동작 확인
5. WishController/OptionController의 productRepository 사용은 3-4(OptionService), 3-5(WishService) 단계에서 처리
