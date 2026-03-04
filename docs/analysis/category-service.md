# 리팩토링 분석 보고서: CategoryService 추출

> 분석 일시: 2026-02-26

## 분석 대상

- 설명: CategoryController에 직접 구현된 CRUD 비즈니스 로직을 CategoryService로 추출. 또한 ProductController, AdminProductController에서 CategoryRepository를 직접 사용하는 카테고리 조회 로직도 함께 분석.
- 관련 파일:
  - `src/main/java/gift/controller/CategoryController.java`
  - `src/main/java/gift/repository/CategoryRepository.java`
  - `src/main/java/gift/controller/ProductController.java`
  - `src/main/java/gift/controller/AdminProductController.java`

## 1. 대상 코드 현황

| # | 파일 | 라인 | 코드 내용 | 사용 여부 |
|---|------|------|-----------|-----------|
| 1 | CategoryController.java | 21 | `private final CategoryRepository categoryRepository` | 사용중 - Controller에서 직접 Repository 호출 |
| 2 | CategoryController.java | 29-31 | `categoryRepository.findAll().stream().map(CategoryResponse::from).toList()` | 사용중 - 전체 카테고리 조회 |
| 3 | CategoryController.java | 37 | `categoryRepository.save(request.toEntity())` | 사용중 - 카테고리 생성 |
| 4 | CategoryController.java | 47-48 | `categoryRepository.findById(id).orElseThrow(...)` | 사용중 - 카테고리 조회 (수정용) |
| 5 | CategoryController.java | 51 | `categoryRepository.save(category)` | 사용중 - 카테고리 수정 |
| 6 | CategoryController.java | 57 | `categoryRepository.deleteById(id)` | 사용중 - 카테고리 삭제 |
| 7 | ProductController.java | 28 | `private final CategoryRepository categoryRepository` | 사용중 - 상품 생성/수정 시 카테고리 조회 |
| 8 | ProductController.java | 52-53 | `categoryRepository.findById(request.categoryId()).orElseThrow(...)` | 사용중 - 상품 생성 시 카테고리 검증 |
| 9 | ProductController.java | 67-68 | `categoryRepository.findById(request.categoryId()).orElseThrow(...)` | 사용중 - 상품 수정 시 카테고리 검증 |
| 10 | AdminProductController.java | 21 | `private final CategoryRepository categoryRepository` | 사용중 - 카테고리 목록 조회 및 검증 |
| 11 | AdminProductController.java | 36,65,114,132 | `categoryRepository.findAll()` | 사용중 - 폼 렌더링 시 카테고리 목록 제공 (4곳) |
| 12 | AdminProductController.java | 54-55,87-88 | `categoryRepository.findById(categoryId).orElseThrow(...)` | 사용중 - 상품 생성/수정 시 카테고리 검증 (2곳) |

### 중복 패턴 요약

- `categoryRepository.findById(id).orElseThrow(...)` 패턴이 **5곳**에서 반복됨 (CategoryController 1곳, ProductController 2곳, AdminProductController 2곳)
- `categoryRepository.findAll()` 패턴이 **5곳**에서 반복됨 (CategoryController 1곳, AdminProductController 4곳)

## 2. 주석 및 TODO 분석

> 대상 코드 주변에 의도를 설명하는 주석이 없습니다.

category 패키지 전체와 ProductController, AdminProductController의 카테고리 관련 코드에 TODO, FIXME, NOTE 등의 주석이 없습니다.

## 3. Git Blame 분석

| # | 파일:라인 | 작성자 | 커밋 | 날짜 | 커밋 메시지 |
|---|-----------|--------|------|------|-------------|
| 1 | CategoryController.java:1-60 (대부분) | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |
| 2 | CategoryController.java:16,47-48 | theo.cha | 7513bc95 | 2026-02-26 | refactor: orElseThrow()로 null 처리 패턴 통일 |
| 3 | ProductController.java:28-33 | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |
| 4 | ProductController.java:52-53,67-68 | theo.cha | 7513bc95 | 2026-02-26 | refactor: orElseThrow()로 null 처리 패턴 통일 |
| 5 | AdminProductController.java:21-25,36,54,65,87,114,132 | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |
| 6 | AdminProductController.java:55,88 | theo.cha | 768789ed | 2026-02-26 | (orElseThrow 리팩토링) |

### 커밋 상세

- **55ca9e43 (wotjd243, 2026-02-18)**: 프로젝트 초기 설정 커밋. 62개 파일, 2601줄 추가. CategoryController를 포함한 전체 프로젝트의 초기 구조가 이 커밋에서 생성됨. 초기 템플릿/보일러플레이트 코드로 판단됨.
- **7513bc95 (theo.cha, 2026-02-26)**: null 처리 패턴 통일 리팩토링. `.orElse(null)` + if-null 패턴을 `.orElseThrow()`로 변경. 이 커밋은 CategoryService 추출과 충돌하지 않으며, 오히려 추출 후에도 동일 패턴을 유지하면 됨.

## 4. 리팩토링 영향도 판단

### 주석/TODO 의도 확인
- [x] 의도를 설명하는 주석 없음
- [x] TODO/FIXME 없음

### Git 이력 확인
- [x] 초기 설정 또는 템플릿 코드로 판단됨
- [x] 커밋 메시지에 리팩토링을 막을 만한 의도 없음

### 이후 단계 충돌 확인
- [x] README.md 구현 목록과 충돌 없음 — README.md 3-2에서 CategoryService 추출을 명시적으로 계획하고 있음
- [x] 향후 작업에서 필요하지 않음 — 오히려 ProductService 추출(3-3)에서 CategoryService를 의존할 수 있으므로 선행 작업으로 적합

### 추가 분석: 다른 도메인과의 관계

- **ProductController, AdminProductController**: 현재 `CategoryRepository`를 직접 주입받아 `findById()`, `findAll()`을 호출. CategoryService 추출 후 이 컨트롤러들도 `CategoryService`를 사용하도록 변경하면 Repository 직접 접근을 제거할 수 있음. 단, ProductService 추출(3-3)이 별도로 계획되어 있으므로, 이 단계에서는 CategoryController만 대상으로 하고, Product 쪽은 3-3에서 처리하는 것이 범위 관리에 유리.
- **테스트 코드**: CategoryControllerTest, ProductControllerTest, WishControllerTest, OptionControllerTest, OrderControllerTest에서 `CategoryRepository`를 `@Autowired`로 직접 사용. 이들은 통합 테스트에서 테스트 데이터 세팅 용도이므로 Service 추출과 무관하게 Repository 직접 사용이 적절.

## 5. 최종 판단

| 대상 | 판단 | 근거 요약 |
|------|------|-----------|
| CategoryController → CategoryService 추출 | 리팩토링 가능 | 초기 설정 코드. 주석/의도 없음. README.md 3-2에서 명시적 계획. CRUD 위임 구조로 단순 |
| ProductController의 categoryRepository 사용 | 리팩토링 보류 | ProductService 추출(3-3)에서 함께 처리하는 것이 적절 |
| AdminProductController의 categoryRepository 사용 | 리팩토링 보류 | ProductService 추출(3-3)에서 함께 처리하는 것이 적절 |

## 6. 권장 작업 순서

1. `CategoryService` 클래스 생성 (`src/main/java/gift/service/CategoryService.java`)
   - `@Service` 어노테이션
   - `CategoryRepository` 주입
   - `findAll()`, `create()`, `findById()`, `update()`, `delete()` 메서드 구현
2. `CategoryController`에서 `CategoryRepository` 의존을 `CategoryService`로 교체
   - Controller는 요청 파싱 + 응답 변환만 담당
3. 기존 테스트(`CategoryControllerTest`) 실행하여 동작 확인
4. ProductController/AdminProductController의 categoryRepository 사용은 3-3(ProductService 추출) 단계에서 처리
