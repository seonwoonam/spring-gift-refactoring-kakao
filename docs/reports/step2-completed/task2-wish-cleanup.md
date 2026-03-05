# 작업 2 완료 보고서: 주문 시 위시리스트 자동 정리

## 목적
주문 시 해당 상품이 회원의 위시리스트에 있으면 자동 제거한다.

## 변경 사항

### 1. `src/main/java/gift/repository/WishRepository.java`
- `deleteByMemberIdAndProductId(Long memberId, Long productId)` 메서드 추가

### 2. `src/main/java/gift/service/WishService.java`
- `removeByMemberIdAndProductId(Long memberId, Long productId)` 메서드 추가
- 해당 row가 없어도 예외 없이 0건 삭제 (정상 동작)

### 3. `src/main/java/gift/service/OrderService.java`
- `WishService` 의존성 추가 (생성자 주입)
- `createOrder()`에서 주문 저장 후 `wishService.removeByMemberIdAndProductId()` 호출
- 기존 `@Transactional` 안에서 실행되어 원자성 보장

### 4. `src/test/java/gift/controller/OrderControllerTest.java`
- 위시리스트에 상품 추가 -> 주문 -> 위시리스트에서 제거됨 검증 테스트 추가

## 변경하지 않은 것
- 기존 위시 CRUD API (WishController, WishService 기존 메서드)
- 위시리스트에 없는 상품을 주문하는 기존 흐름
