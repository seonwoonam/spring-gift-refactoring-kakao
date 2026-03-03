# 코드 제거 분석 보고서: Validator 중복 코드 정리

> 분석 일시: 2026-02-26

## 분석 대상

- 설명: `ProductNameValidator`와 `OptionNameValidator` 간 중복된 검증 로직 정리. Controller 내 `validateName()` 패턴 중복 포함.
- 관련 파일:
  - `src/main/java/gift/product/ProductNameValidator.java`
  - `src/main/java/gift/option/OptionNameValidator.java`
  - `src/main/java/gift/controller/ProductController.java` (82-87행)
  - `src/main/java/gift/controller/OptionController.java` (87-92행)
  - `src/main/java/gift/controller/AdminProductController.java` (47, 80행)
  - `src/test/java/gift/product/ProductNameValidatorTest.java`
  - `src/test/java/gift/option/OptionNameValidatorTest.java`

## 1. 대상 코드 현황

### 1-1. Validator 클래스 간 중복

| # | 항목 | ProductNameValidator | OptionNameValidator | 동일 여부 |
|---|------|---------------------|---------------------|-----------|
| 1 | 정규식 패턴 | `^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ ()\\[\\]+\\-&/_]*$` | 동일 | **동일** |
| 2 | null/blank 체크 | `name == null \|\| name.isBlank()` | 동일 | **동일** |
| 3 | 길이 검증 로직 | `name.length() > MAX_LENGTH` | 동일 | **동일** |
| 4 | 특수문자 검증 로직 | `!ALLOWED_PATTERN.matcher(name).matches()` | 동일 | **동일** |
| 5 | 에러 수집 방식 | `List<String> errors = new ArrayList<>()` | 동일 | **동일** |
| 6 | MAX_LENGTH | 15 | 50 | **차이** |
| 7 | 카카오 검증 | 있음 (`allowKakao` 파라미터) | 없음 | **차이** |
| 8 | 에러 메시지 | "Product name ..." | "Option name ..." | **프리픽스만 차이** |

### 1-2. Controller 내 validateName() 중복

| # | 파일 | 라인 | 코드 내용 | 사용 여부 |
|---|------|------|-----------|-----------|
| 1 | ProductController.java | 82-87 | `validateName()` → `ProductNameValidator.validate(name)` + 예외 throw | 사용중 (48, 63행에서 호출) |
| 2 | OptionController.java | 87-92 | `validateName()` → `OptionNameValidator.validate(name)` + 예외 throw | 사용중 (49행에서 호출) |
| 3 | AdminProductController.java | 47 | `ProductNameValidator.validate(name, true)` 인라인 호출 | 사용중 |
| 4 | AdminProductController.java | 80 | `ProductNameValidator.validate(name, true)` 인라인 호출 | 사용중 |

### 1-3. 테스트 파일 대응

| # | 파일 | 테스트 클래스 수 | 비고 |
|---|------|-----------------|------|
| 1 | ProductNameValidatorTest.java | 6개 (ValidNames, NullOrBlank, ExceedsMaxLength, InvalidCharacters, KakaoRestriction, MultipleErrors) | 카카오 검증 테스트 포함 |
| 2 | OptionNameValidatorTest.java | 5개 (ValidNames, NullOrBlank, ExceedsMaxLength, InvalidCharacters, MultipleErrors) | 카카오 검증 없음, 나머지 구조 동일 |

## 2. 주석 및 TODO 분석

| # | 파일:라인 | 주석 내용 | 삭제에 영향 |
|---|-----------|-----------|-------------|
| 1 | OptionNameValidator.java:7-12 | `/* Validates option names against the following rules: - Must not be null or blank - Maximum length of 50 characters (including spaces) - Only Korean, English, digits, spaces, and selected special characters are allowed: ( ) [ ] + - & / _ */` | 없음 (검증 규칙 설명일 뿐, 중복 유지를 정당화하지 않음) |
| 2 | OptionController.java:19-22 | `/* Each product must have at least one option at all times. Option names are validated against allowed characters and length constraints. */` | 없음 (비즈니스 규칙 설명) |

> 두 Validator 클래스 주변에 TODO, FIXME, HACK, NOTE, XXX 주석은 없습니다.
> 중복 유지를 정당화하거나 "향후 확장", "임시" 등의 의도를 설명하는 주석이 없습니다.

## 3. Git Blame 분석

| # | 파일:라인 | 작성자 | 커밋 | 날짜 | 커밋 메시지 |
|---|-----------|--------|------|------|-------------|
| 1 | ProductNameValidator.java:1-41 (구조) | wotjd243 | `55ca9e43` | 2026-02-18 | feat: set up the project |
| 2 | ProductNameValidator.java:23,28,32,36 (메시지) | theo.cha | `768789ed` | 2026-02-26 | style: 에러 메시지를 영어로 통일 |
| 3 | OptionNameValidator.java:1-39 (구조) | wotjd243 | `55ca9e43` | 2026-02-18 | feat: set up the project |
| 4 | OptionNameValidator.java:25,30,34 (메시지) | theo.cha | `768789ed` | 2026-02-26 | style: 에러 메시지를 영어로 통일 |
| 5 | ProductController.java:82-87 | wotjd243 | `55ca9e43` | 2026-02-18 | feat: set up the project |
| 6 | OptionController.java:87-92 | wotjd243 | `55ca9e43` | 2026-02-18 | feat: set up the project |
| 7 | AdminProductController.java:47,80 | wotjd243 | `55ca9e43` | 2026-02-18 | feat: set up the project |

### 커밋 상세

**`55ca9e43` (feat: set up the project)**
- 작성자: wotjd243
- 날짜: 2026-02-18
- 프로젝트 초기 설정 커밋. 62개 파일 추가 (2,601 insertions).
- 모든 Validator 코드, Controller 코드가 이 커밋에서 일괄 생성됨.
- **판단:** 프로젝트 템플릿/보일러플레이트 코드. 의도적으로 중복을 설계한 것이 아니라, 초기 구현 시 자연스럽게 발생한 중복으로 보임.

**`768789ed` (style: 에러 메시지를 영어로 통일)**
- 작성자: theo.cha
- 날짜: 2026-02-26
- 에러 메시지 문자열만 한국어→영어로 변경. 로직 변경 없음.
- **판단:** 스타일 정리 작업. Validator 구조나 중복과 무관.

## 4. 삭제 영향도 판단

### 주석/TODO 의도 확인
- [x] 의도를 설명하는 주석 없음 (OptionNameValidator의 Javadoc은 검증 규칙 설명이지 중복 유지 이유가 아님)
- [x] TODO/FIXME 없음

### Git 이력 확인
- [x] 초기 설정 커밋(`55ca9e43`)에 포함된 코드로, 템플릿/보일러플레이트로 판단됨
- [x] 커밋 메시지에 삭제를 막을 만한 의도 없음 (단순 프로젝트 셋업)

### 이후 단계 충돌 확인
- [x] README.md 구현 목록에서 "2-2. Validator 중복 코드 정리"로 이미 정리 대상으로 선정됨
- [x] README.md에 3가지 대안이 제시되어 있음 (대안 A: 통합, 대안 B: 공통 유틸 추출, 대안 C: Bean Validation 전환)
- [x] 향후 서비스 계층 추출(3-3, 3-4) 시 Validator 호출이 Controller에서 Service로 이동할 예정이므로, 먼저 Validator 중복을 정리하면 Service 추출이 더 깔끔해짐
- [x] 다른 브랜치 없음 (main 브랜치만 존재)

## 5. 최종 판단

| 대상 | 판단 | 근거 요약 |
|------|------|-----------|
| ProductNameValidator + OptionNameValidator 중복 (정규식, 검증 구조) | **삭제 가능 (통합)** | 95% 동일한 코드. 차이는 MAX_LENGTH와 카카오 검증뿐. 초기 설정 커밋에서 발생한 자연적 중복. 주석/TODO에 유지 이유 없음. README에서도 정리 대상으로 명시됨. |
| ProductController.validateName() + OptionController.validateName() 중복 | **삭제 가능 (통합 후 자동 해소)** | Validator를 통합하면 Controller의 validateName() 패턴도 공통화 가능. 서비스 계층 추출 시 자연스럽게 이동됨. |
| AdminProductController의 인라인 검증 호출 (47, 80행) | **삭제 가능 (통합 시 호출 대상 변경)** | Validator 통합 후 호출 대상만 변경하면 됨. `allowKakao=true` 파라미터는 통합 Validator에서 지원 필요. |
| ProductNameValidatorTest + OptionNameValidatorTest 중복 | **삭제 가능 (테스트 리팩토링)** | Validator 통합 시 테스트도 하나로 합칠 수 있음. 단, 통합 Validator의 파라미터 조합별 테스트 필요. |

## 6. 권장 작업 순서

README.md에 제시된 3가지 대안 중 **대안 A (하나의 NameValidator로 통합)** 가 가장 적합한 것으로 판단됩니다.

### 이유:
- 중복 코드의 95%가 동일하여 파라미터화로 충분히 통합 가능
- 대안 B(공통 유틸 추출)는 클래스 수가 줄지 않아 이점이 적음
- 대안 C(Bean Validation 어노테이션)는 현 단계에서 구현 복잡도가 과도함

### 작업 순서:

1. **`NameValidator` 통합 클래스 생성** — `gift.common.NameValidator` 또는 적절한 패키지에 배치
   - `validate(String name, int maxLength, boolean checkKakao)` 메서드
   - 정규식 패턴, null/blank 체크, 길이 검증, 특수문자 검증 통합
   - 카카오 검증은 `checkKakao` 파라미터로 제어
   - 에러 메시지의 프리픽스("Product name", "Option name")는 파라미터로 전달하거나 enum으로 관리

2. **Controller 호출부 수정**
   - `ProductController.validateName()` → `NameValidator.validate(name, 15, false)` 호출로 변경
   - `OptionController.validateName()` → `NameValidator.validate(name, 50, false)` 호출로 변경
   - `AdminProductController` → `NameValidator.validate(name, 15, true)` 호출로 변경

3. **기존 Validator 클래스 삭제**
   - `ProductNameValidator.java` 삭제
   - `OptionNameValidator.java` 삭제

4. **테스트 리팩토링**
   - 통합 `NameValidator`에 대한 테스트 작성 (기존 두 테스트의 케이스 병합)
   - `ProductNameValidatorTest.java` 삭제
   - `OptionNameValidatorTest.java` 삭제

5. **전체 테스트 실행으로 회귀 확인**
