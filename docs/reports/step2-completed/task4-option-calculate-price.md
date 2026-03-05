# 작업 4 완료 보고서: 주문 금액 계산 도메인 이동

## 목적
가격 계산(price * quantity)을 서비스에서 Option 도메인 객체로 이동하여 디미터 법칙 위반을 해소한다.

## 변경 사항

### 1. `src/main/java/gift/model/Option.java` (line 45-47)
- `calculateTotalPrice(int quantity)` 메서드 추가
```java
public int calculateTotalPrice(int quantity) {
    return product.getPrice() * quantity;
}
```

### 2. `src/main/java/gift/service/OrderService.java` (line 45)
- 기존: `var price = option.getProduct().getPrice() * quantity;`
- 변경: `var price = option.calculateTotalPrice(quantity);`

### 3. `src/test/java/gift/model/OptionTest.java`
- `calculateTotalPriceWithSingleQuantity`: 수량 1 -> 10000원 검증
- `calculateTotalPriceWithMultipleQuantity`: 수량 3 -> 30000원 검증

## 변경하지 않은 것
- 계산 결과값 (가격 x 수량) 동일
- Product 클래스, Order 클래스 변경 없음
- `getProduct()` getter 유지

## 참고
- 순수 구조 변경 (리팩토링), 동작 변경 없음
