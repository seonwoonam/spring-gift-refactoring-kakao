# 작업 3 완료 보고서: 카테고리별 상품 목록 필터링

## 목적
GET /api/products?categoryId=1 호출 시 해당 카테고리의 상품만 반환하도록 구현한다.

## 변경 사항

### 1. `src/main/java/gift/repository/ProductRepository.java`
- `Page<Product> findByCategoryId(Long categoryId, Pageable pageable)` 추가
- Spring Data JPA 네이밍 규칙으로 자동 생성

### 2. `src/main/java/gift/service/ProductService.java`
- `findAll(Pageable)` -> `findAll(Long categoryId, Pageable pageable)`로 변경
- categoryId가 null이면 `productRepository.findAll(pageable)` (기존 동작)
- categoryId가 not null이면 `productRepository.findByCategoryId(categoryId, pageable)`

### 3. `src/main/java/gift/controller/ProductController.java`
- `getProducts()`에 `@RequestParam(required = false) Long categoryId` 파라미터 추가
- `productService.findAll(categoryId, pageable)` 호출

### 4. `src/test/java/gift/controller/ProductControllerTest.java`
- 카테고리 필터링 조회 테스트 추가 (categoryId 지정 시 해당 카테고리 상품만 반환)
- 전체 조회 유지 확인 테스트 추가 (categoryId 없으면 전체 반환)

## 변경하지 않은 것
- `findAll()` (파라미터 없는 오버로드) - AdminProductController에서 사용 중
- 기존 상품 CRUD 동작
- AdminProductController
- ProductResponse 구조

## 테스트 결과
- ProductControllerTest 12/12 통과 (기존 10 + 신규 2)
