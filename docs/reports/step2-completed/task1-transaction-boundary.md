# 작업 1 완료 보고서: 트랜잭션 경계 세우기

## 목적
OrderService.createOrder()에 @Transactional을 추가하여 주문의 원자성을 보장한다.

## 변경 사항

### 1. `src/main/java/gift/service/OrderService.java`
- `createOrder()`에 `@Transactional` 추가
- `findByMemberId()`에 `@Transactional(readOnly = true)` 추가
- `import org.springframework.transaction.annotation.Transactional` 추가

### 2. `src/test/java/gift/controller/OrderControllerTest.java`
- `createOrderInsufficientPoints_stockRemainsUnchanged` 테스트 추가
- 포인트 부족 시 주문 실패 -> 재고가 원래대로 유지됨을 검증 (트랜잭션 롤백 확인)
- `EntityManager` 주입하여 persistence context 초기화 후 DB 상태 검증

## 변경하지 않은 것
- KakaoNotificationService (기존 best-effort 패턴 유지)
- OptionService, MemberService (개별 트랜잭션 불필요 - OrderService에서 감싸므로)

## 테스트 결과
- OrderControllerTest 9/9 통과
