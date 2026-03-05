# 작업 5 완료 보고서: validateName 중복 제거

## 목적
ProductService와 OptionService에 중복된 validateName 메서드를 NameValidator.validateOrThrow()로 통합한다.

## 변경 사항

### 1. `src/main/java/gift/common/NameValidator.java` (line 24-29)
- `validateOrThrow(String name)` 메서드 추가
```java
public void validateOrThrow(String name) {
    List<String> errors = validate(name);
    if (!errors.isEmpty()) {
        throw new IllegalArgumentException(String.join(", ", errors));
    }
}
```

### 2. `src/main/java/gift/service/ProductService.java`
- `private void validateName(String name)` 메서드 삭제
- `create()`, `update()`의 호출부를 `NAME_VALIDATOR.validateOrThrow(name)`으로 변경

### 3. `src/main/java/gift/service/OptionService.java`
- `private void validateName(String name)` 메서드 삭제
- `create()`의 호출부를 `NAME_VALIDATOR.validateOrThrow(name)`으로 변경

## 변경하지 않은 것
- 검증 규칙 (maxLength, allowedCharacters, noKakao) 변경 없음
- 예외 타입 (`IllegalArgumentException`) 및 메시지 형식 동일
- `NameValidator.validate()` 메서드 유지

## 참고
- 순수 구조 변경 (리팩토링), 동작 변경 없음
