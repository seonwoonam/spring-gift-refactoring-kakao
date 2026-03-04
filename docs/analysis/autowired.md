# 코드 제거 분석 보고서: 미사용 @Autowired 제거

> 분석 일시: 2026-02-26

## 분석 대상

- 설명: 생성자가 하나뿐인 Spring Bean에서 불필요한 `@Autowired` 어노테이션을 제거한다. Spring 4.3+부터 생성자가 하나일 경우 `@Autowired`는 자동 적용되므로 명시할 필요가 없다.
- 관련 파일:
  - `src/main/java/gift/auth/JwtProvider.java`
  - `src/main/java/gift/auth/AuthenticationResolver.java`
  - `src/main/java/gift/controller/MemberController.java`
  - `src/main/java/gift/controller/AdminMemberController.java`

## 1. 대상 코드 현황

| # | 파일 | 라인 | 코드 내용 | 사용 여부 | 생성자 수 |
|---|------|------|-----------|-----------|-----------|
| 1 | `JwtProvider.java` | 23 | `@Autowired` (생성자 위) | 미사용 (생성자 1개이므로 불필요) | 1개 |
| 2 | `AuthenticationResolver.java` | 19 | `@Autowired` (생성자 위) | 미사용 (생성자 1개이므로 불필요) | 1개 |
| 3 | `MemberController.java` | 27 | `@Autowired` (생성자 위) | 미사용 (생성자 1개이므로 불필요) | 1개 |
| 4 | `AdminMemberController.java` | 23 | `@Autowired` (생성자 위) | 미사용 (생성자 1개이므로 불필요) | 1개 |

### 참고: `@Autowired`를 사용하지 않는 동일 프로젝트 클래스들

아래 클래스들은 동일한 프로젝트에서 이미 `@Autowired` 없이 생성자 주입을 사용하고 있다. 이는 프로젝트 내에서도 `@Autowired` 사용이 일관되지 않음을 보여준다.

| 클래스 | 생성자 위 `@Autowired` |
|--------|----------------------|
| `ProductController` | 없음 |
| `AdminProductController` | 없음 |
| `CategoryController` | 없음 |
| `OptionController` | 없음 |
| `WishController` | 없음 |
| `OrderController` | 없음 |
| `KakaoAuthController` | 없음 |
| `KakaoLoginClient` | 없음 |
| `KakaoMessageClient` | 없음 |

### 참고: 테스트 코드의 `@Autowired` (제거 대상 아님)

테스트 클래스에서 필드 주입용으로 사용하는 `@Autowired`는 Spring Boot 테스트의 표준 패턴이며, 생성자 주입과는 다르다. 이들은 제거 대상이 아니다.

| 테스트 클래스 | `@Autowired` 필드 수 |
|--------------|---------------------|
| `GiftAcceptanceTest` | 1개 (jwtProvider) |
| `ProductControllerTest` | 4개 (mockMvc, objectMapper, productRepository, categoryRepository) |
| `OptionControllerTest` | 5개 (mockMvc, objectMapper, productRepository, categoryRepository, optionRepository) |
| `WishControllerTest` | 7개 (mockMvc, objectMapper, jwtProvider, memberRepository, productRepository, categoryRepository, wishRepository) |
| `CategoryControllerTest` | 3개 (mockMvc, objectMapper, categoryRepository) |
| `MemberControllerTest` | 3개 (mockMvc, objectMapper, memberRepository) |
| `OrderControllerTest` | 8개 (mockMvc, objectMapper, jwtProvider, memberRepository, productRepository, categoryRepository, optionRepository, orderRepository) |

## 2. 주석 및 TODO 분석

| # | 파일:라인 | 주석 내용 | 삭제에 영향 |
|---|-----------|-----------|-------------|
| 1 | `JwtProvider.java:12-17` | Javadoc: `Provides JWT token creation and validation. @author brian.kim @since 1.0` | 없음 (클래스 설명이며 `@Autowired`의 존재 이유를 설명하지 않음) |
| 2 | `AuthenticationResolver.java:8-13` | Javadoc: `Resolves the authenticated member from an Authorization header. @author brian.kim @since 1.0` | 없음 |
| 3 | `MemberController.java:15-20` | Javadoc: `Handles member registration and login. @author brian.kim @since 1.0` | 없음 |
| 4 | `AdminMemberController.java:12-17` | Javadoc: `Admin controller for managing members. @author brian.kim @since 1.0` | 없음 |

> 대상 코드 주변에 `@Autowired`의 존재 이유를 설명하는 주석, TODO, FIXME, HACK, NOTE, XXX가 없습니다.

## 3. Git Blame 분석

| # | 파일:라인 | 작성자 | 커밋 | 날짜 | 커밋 메시지 |
|---|-----------|--------|------|------|-------------|
| 1 | `JwtProvider.java:23` | wotjd243 | `55ca9e43` | 2026-02-18 | `feat: set up the project` |
| 2 | `AuthenticationResolver.java:19` | wotjd243 | `55ca9e43` | 2026-02-18 | `feat: set up the project` |
| 3 | `MemberController.java:27` | wotjd243 | `55ca9e43` | 2026-02-18 | `feat: set up the project` |
| 4 | `AdminMemberController.java:23` | wotjd243 | `55ca9e43` | 2026-02-18 | `feat: set up the project` |

### 커밋 상세

- **커밋 `55ca9e43`**: 프로젝트 초기 설정 커밋. 62개 파일이 추가된 대규모 보일러플레이트 커밋이다.
- 모든 `@Autowired`가 동일한 초기 설정 커밋에서 추가되었으며, 의도적으로 `@Autowired`를 사용한 것이 아니라 템플릿/습관적 코드로 판단된다.
- 같은 커밋에서 추가된 다른 Controller/Component들(`ProductController`, `CategoryController`, `OptionController`, `WishController`, `OrderController`, `KakaoAuthController`, `KakaoLoginClient`, `KakaoMessageClient`)은 `@Autowired`를 사용하지 않는다. 이는 일부 클래스에만 습관적으로 붙인 것으로 보인다.

## 4. 삭제 영향도 판단

### 주석/TODO 의도 확인
- [x] 의도를 설명하는 주석 없음
- [x] TODO/FIXME 없음

### Git 이력 확인
- [x] 초기 설정 또는 템플릿 코드로 판단됨
- [x] 커밋 메시지에 삭제를 막을 만한 의도 없음 (`feat: set up the project`이라는 일반적 설정 메시지)

### 이후 단계 충돌 확인
- [x] README.md 구현 목록과 충돌 없음 (README.md 2-1항에서 "대안 A: @Autowired만 제거"를 명시적으로 제안하고 있음)
- [x] 향후 작업에서 필요하지 않음 (Spring 4.3+ 공식 권장 방식은 단일 생성자에 `@Autowired` 생략)
- [x] `@Autowired` 제거 시 `import org.springframework.beans.factory.annotation.Autowired;`도 함께 제거해야 한다 (미사용 import 정리)

## 5. 최종 판단

| 대상 | 판단 | 근거 요약 |
|------|------|-----------|
| `JwtProvider.java:23` `@Autowired` | **삭제 가능** | 생성자 1개. Spring 4.3+에서 불필요. 초기 설정 커밋에서 습관적으로 추가된 코드. 같은 프로젝트의 9개 클래스가 이미 `@Autowired` 없이 동작 중. |
| `AuthenticationResolver.java:19` `@Autowired` | **삭제 가능** | 동일 근거. |
| `MemberController.java:27` `@Autowired` | **삭제 가능** | 동일 근거. |
| `AdminMemberController.java:23` `@Autowired` | **삭제 가능** | 동일 근거. |

## 6. 권장 작업 순서

삭제 가능으로 판단된 항목의 제거 순서를 제안한다.

1. `JwtProvider.java` - `@Autowired` 어노테이션 (23줄) 제거 + `import org.springframework.beans.factory.annotation.Autowired;` (5줄) 제거
2. `AuthenticationResolver.java` - `@Autowired` 어노테이션 (19줄) 제거 + `import org.springframework.beans.factory.annotation.Autowired;` (5줄) 제거
3. `MemberController.java` - `@Autowired` 어노테이션 (27줄) 제거 + `import org.springframework.beans.factory.annotation.Autowired;` (6줄) 제거
4. `AdminMemberController.java` - `@Autowired` 어노테이션 (23줄) 제거 + `import org.springframework.beans.factory.annotation.Autowired;` (3줄) 제거
5. 빌드 및 테스트 실행하여 동작 변경 없음을 확인
