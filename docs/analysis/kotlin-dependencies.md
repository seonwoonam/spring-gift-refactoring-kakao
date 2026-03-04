# 코드 제거 분석 보고서: 미사용 Kotlin 의존성

> 분석 일시: 2026-02-26

## 분석 대상

- 설명: `build.gradle.kts`에 포함된 Kotlin 플러그인, 의존성, 설정 블록 및 관련 디렉토리를 제거하려 한다. 프로젝트에 Kotlin 소스 파일이 없으므로 불필요한 의존성이다.
- 관련 파일: `build.gradle.kts`, `src/main/kotlin/`, `src/test/kotlin/`

## 1. 대상 코드 현황

| # | 파일 | 라인 | 코드 내용 | 사용 여부 |
|---|------|------|-----------|-----------|
| 1 | build.gradle.kts | 2 | `kotlin("jvm") version "1.9.25"` | 미사용 (`.kt` 파일 없음) |
| 2 | build.gradle.kts | 3 | `kotlin("plugin.spring") version "1.9.25"` | 미사용 (Kotlin 클래스 없음) |
| 3 | build.gradle.kts | 4 | `kotlin("plugin.jpa") version "1.9.25"` | 미사용 (Kotlin 엔티티 없음) |
| 4 | build.gradle.kts | 7 | `id("org.jlleitschuh.gradle.ktlint") version "14.0.1"` | 미사용 (Kotlin 소스 없음) |
| 5 | build.gradle.kts | 29 | `implementation("com.fasterxml.jackson.module:jackson-module-kotlin")` | 미사용 (Java 소스에서 참조 없음) |
| 6 | build.gradle.kts | 30 | `implementation("org.jetbrains.kotlin:kotlin-reflect")` | 미사용 (Java 소스에서 참조 없음) |
| 7 | build.gradle.kts | 41 | `testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")` | 미사용 (테스트도 Java로 작성됨) |
| 8 | build.gradle.kts | 45-49 | `kotlin { compilerOptions { ... } }` 블록 | 미사용 (Kotlin 컴파일 대상 없음) |
| 9 | build.gradle.kts | 51-55 | `allOpen { ... }` 블록 | 미사용 (Kotlin 엔티티 없음, Java 엔티티에는 불필요) |
| 10 | build.gradle.kts | 57-59 | `ktlint { verbose.set(true) }` 블록 | 미사용 (Kotlin 소스 없음) |
| 11 | src/main/kotlin/gift/.gitkeep | - | 빈 디렉토리 유지 파일 | 미사용 |
| 12 | src/test/kotlin/gift/.gitkeep | - | 빈 디렉토리 유지 파일 | 미사용 |

### 참조 확인

- 프로젝트 내 `.kt` 파일: **0개**
- Java 소스에서 `kotlin`, `ktlint` 참조: **0건**
- `jackson-module-kotlin`, `kotlin-reflect` 을 Java 코드에서 직접 사용: **0건**

## 2. 주석 및 TODO 분석

대상 코드 주변에 의도를 설명하는 주석이 없습니다.

| # | 파일:라인 | 주석 내용 | 삭제에 영향 |
|---|-----------|-----------|-------------|
| - | - | (주석 없음) | 없음 |

> `build.gradle.kts` 전체에 TODO, FIXME, HACK, NOTE, XXX 주석이 존재하지 않습니다.

## 3. Git Blame 분석

모든 Kotlin 관련 코드가 **동일한 초기 커밋**에서 추가되었습니다.

| # | 파일:라인 | 작성자 | 커밋 | 날짜 | 커밋 메시지 |
|---|-----------|--------|------|------|-------------|
| 1 | build.gradle.kts:2-4 | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |
| 2 | build.gradle.kts:7 | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |
| 3 | build.gradle.kts:29-30 | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |
| 4 | build.gradle.kts:41 | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |
| 5 | build.gradle.kts:45-59 | wotjd243 | 55ca9e43 | 2026-02-18 | feat: set up the project |

### 커밋 상세

- **커밋 55ca9e43** (`feat: set up the project`): 프로젝트 초기 설정 커밋으로, 62개 파일이 한꺼번에 추가되었다. Spring Boot + Kotlin 프로젝트 템플릿(Spring Initializr)에서 생성된 것으로 판단된다.
- 이후 `build.gradle.kts`를 수정한 커밋은 **cae1c13** (`test: 스타일 정리 검증을 위한 단위 및 통합 테스트 추가`) 1건이며, 이 커밋에서는 `rest-assured` 의존성만 추가했고 Kotlin 관련 변경은 없다.

## 4. 삭제 영향도 판단

### 주석/TODO 의도 확인
- [x] 의도를 설명하는 주석 없음
- [x] TODO/FIXME 없음

### Git 이력 확인
- [x] 초기 설정 또는 템플릿 코드로 판단됨 (Spring Initializr 생성 보일러플레이트)
- [x] 커밋 메시지에 삭제를 막을 만한 의도 없음 (`feat: set up the project`는 단순 초기 설정)

### 이후 단계 충돌 확인
- [x] README.md 구현 목록과 충돌 없음 (README 2-3항에서 이 작업을 명시적으로 계획하고 있음)
- [x] 향후 작업에서 필요하지 않음 (모든 소스가 Java로 작성되어 있으며 Kotlin 전환 계획 없음)
- [x] 다른 브랜치에서 참조하지 않음 (main 브랜치만 존재)

## 5. 최종 판단

| 대상 | 판단 | 근거 요약 |
|------|------|-----------|
| Kotlin 플러그인 3개 (jvm, plugin.spring, plugin.jpa) | **삭제 가능** | `.kt` 파일 0개. 초기 템플릿 보일러플레이트. |
| ktlint 플러그인 | **삭제 가능** | Kotlin 소스 없어 린트 대상 없음. |
| jackson-module-kotlin 의존성 | **삭제 가능** | Java 코드에서 사용하지 않음. Jackson은 Java 기반으로 동작 중. |
| kotlin-reflect 의존성 | **삭제 가능** | Java 코드에서 참조 없음. |
| kotlin-test-junit5 의존성 | **삭제 가능** | 테스트도 전부 Java로 작성됨. |
| kotlin compilerOptions 블록 | **삭제 가능** | Kotlin 컴파일 대상 없음. |
| allOpen 블록 | **삭제 가능** | Kotlin JPA 엔티티 없음. Java 엔티티에는 불필요. |
| ktlint 설정 블록 | **삭제 가능** | ktlint 플러그인 제거 시 함께 제거. |
| src/main/kotlin/ 디렉토리 | **삭제 가능** | `.gitkeep`만 존재. 실제 소스 없음. |
| src/test/kotlin/ 디렉토리 | **삭제 가능** | `.gitkeep`만 존재. 실제 소스 없음. |

## 6. 권장 작업 순서

1. `build.gradle.kts` plugins 블록에서 Kotlin 플러그인 3개 제거 (라인 2-4)
2. `build.gradle.kts` plugins 블록에서 ktlint 플러그인 제거 (라인 7)
3. `build.gradle.kts` dependencies 블록에서 Kotlin 관련 의존성 3개 제거 (라인 29, 30, 41)
4. `build.gradle.kts`에서 `kotlin { ... }` 블록 제거 (라인 45-49)
5. `build.gradle.kts`에서 `allOpen { ... }` 블록 제거 (라인 51-55)
6. `build.gradle.kts`에서 `ktlint { ... }` 블록 제거 (라인 57-59)
7. `src/main/kotlin/` 디렉토리 삭제
8. `src/test/kotlin/` 디렉토리 삭제
9. Gradle sync 및 빌드/테스트 실행으로 정상 동작 확인
