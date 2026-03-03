# 코드 제거 분석 보고서: MemberController 구현 의도

> 분석 일시: 2026-02-26

## 분석 대상

- 설명: MemberController의 구현 의도를 분석하고, 비즈니스 로직이 Controller에 직접 구현된 현재 구조의 삭제/리팩토링 가능성을 판단
- 관련 파일:
  - `src/main/java/gift/controller/MemberController.java`
  - `src/main/java/gift/controller/AdminMemberController.java`
  - `src/test/java/gift/controller/MemberControllerTest.java`

## 1. 대상 코드 현황

| # | 파일 | 라인 | 코드 내용 | 사용 여부 |
|---|------|------|-----------|-----------|
| 1 | `MemberController.java` | 30-39 | `register()` - 이메일 중복 확인 → 회원 저장 → JWT 발급 | 사용중 (API 엔드포인트 `/api/members/register`) |
| 2 | `MemberController.java` | 41-52 | `login()` - 이메일 조회 → 비밀번호 검증 → JWT 발급 | 사용중 (API 엔드포인트 `/api/members/login`) |
| 3 | `AdminMemberController.java` | 26-30 | `list()` - 전체 회원 목록 조회 | 사용중 (Admin 페이지 `/admin/members`) |
| 4 | `AdminMemberController.java` | 37-50 | `create()` - 이메일 중복 확인 → 회원 저장 | 사용중 (Admin 회원 생성) |
| 5 | `AdminMemberController.java` | 52-58 | `editForm()` - 회원 수정 폼 | 사용중 |
| 6 | `AdminMemberController.java` | 60-71 | `update()` - 회원 정보 수정 | 사용중 |
| 7 | `AdminMemberController.java` | 73-83 | `chargePoint()` - 포인트 충전 | 사용중 |
| 8 | `AdminMemberController.java` | 85-89 | `delete()` - 회원 삭제 | 사용중 |

### 핵심 문제: 비즈니스 로직이 Controller에 직접 구현됨

MemberController의 `register()`와 `login()` 메서드는 다음과 같은 비즈니스 로직을 Controller 레이어에서 직접 처리하고 있다:

- **register()**: 이메일 중복 확인(`existsByEmail`) → 엔티티 생성/저장 → JWT 토큰 발급
- **login()**: 회원 조회(`findByEmail`) → 비밀번호 검증(`getPassword().equals()`) → JWT 토큰 발급
- **AdminMemberController**: 회원 CRUD + 포인트 충전 로직을 모두 직접 처리

Service 계층 없이 Controller가 Repository와 JwtProvider를 직접 호출하는 구조이다.

## 2. 주석 및 TODO 분석

| # | 파일:라인 | 주석 내용 | 삭제에 영향 |
|---|-----------|-----------|-------------|
| 1 | `MemberController.java:13-18` | Javadoc: `Handles member registration and login. @author brian.kim @since 1.0` | 없음 - 기능 설명만 있고, 의도적으로 Service 없이 구현한 이유가 명시되어 있지 않음 |
| 2 | `AdminMemberController.java:11-16` | Javadoc: `Admin controller for managing members. @author brian.kim @since 1.0` | 없음 - 동일하게 기능 설명만 존재 |

> 두 Controller 모두 "왜 Service 없이 직접 구현했는지"를 설명하는 주석이 없다. TODO/FIXME/HACK 등의 주석도 없다.

## 3. Git Blame 분석

| # | 파일:라인 | 작성자 | 커밋 | 날짜 | 커밋 메시지 |
|---|-----------|--------|------|------|-------------|
| 1 | `MemberController.java:1-53` (전체) | wotjd243 | `55ca9e43` | 2026-02-18 | `feat: set up the project` |
| 2 | `AdminMemberController.java:1-95` (전체) | wotjd243 | `55ca9e43` | 2026-02-18 | `feat: set up the project` |

### 커밋 상세

**커밋 `55ca9e43` - `feat: set up the project`**

- 프로젝트 초기 설정 커밋으로, 62개 파일이 한꺼번에 추가됨 (+2,601줄)
- 전체 도메인(member, product, option, order, wish, category, auth) 코드가 모두 이 커밋에 포함됨
- **프로젝트 템플릿/보일러플레이트 성격의 초기 커밋**으로, MemberController만을 위한 의도적인 설계 결정이 아닌 전체 프로젝트의 일괄 구성임

이후 커밋에서 MemberController의 비즈니스 로직 자체는 변경되지 않았음:
- `c280382` - `@Autowired` 어노테이션 제거 (구조 변경 없음)
- `ce2d13a` - `@ExceptionHandler` 제거 (GlobalExceptionHandler로 이동)

## 4. 삭제 영향도 판단

### 주석/TODO 의도 확인
- [x] 의도를 설명하는 주석 없음 - Service 없이 Controller에 로직을 둔 설계 이유가 명시되어 있지 않음
- [x] TODO/FIXME 없음

### Git 이력 확인
- [x] 초기 설정 커밋(`feat: set up the project`)에 포함된 템플릿/보일러플레이트 코드로 판단됨
- [x] 커밋 메시지에 "Controller에 직접 구현"에 대한 의도가 없음 - 단순 초기 설정

### 이후 단계 충돌 확인
- [ ] README.md 구현 목록과 충돌 없음 → **충돌 있음 (아래 상세)**
- [x] 향후 작업에서 필요하지 않음

**README.md 및 TODO.md와의 관계:**

README.md 섹션 `3-1. MemberService 추출`에서 명확하게 다음을 계획하고 있다:
> "MemberController에 회원가입(중복 확인 → 저장 → JWT 발급), 로그인(조회 → 비밀번호 검증 → JWT 발급) 비즈니스 로직이 직접 구현되어 있다."

계획된 리팩토링 방향:
- **MemberService**: 회원 관리(조회, 포인트) 로직 담당
- **AuthService**: 인증 관련(register, login, JWT 발급) 로직 담당
- MemberController는 요청 검증 + Service 위임만 남기기

TODO.md의 `3-4. MemberService 추출` 항목이 아직 미완료(unchecked) 상태이다.

## 5. 최종 판단

| 대상 | 판단 | 근거 요약 |
|------|------|-----------|
| `MemberController` 자체 | **삭제 불가** | 활성 API 엔드포인트(`/api/members/register`, `/api/members/login`)를 제공하며, 테스트(`MemberControllerTest`)가 존재함. 8개 테스트가 모두 이 Controller를 검증 중 |
| `MemberController` 내 비즈니스 로직 | **리팩토링 대상 (삭제 후 Service로 이동)** | 초기 설정 커밋의 보일러플레이트 코드. Service 계층 없이 Repository를 직접 호출하는 구조가 README.md `3-1`에서 이미 개선 대상으로 지정됨 |
| `AdminMemberController` 내 비즈니스 로직 | **리팩토링 대상 (삭제 후 Service로 이동)** | 동일 근거. 회원 생성, 수정, 포인트 충전 로직이 Controller에 직접 구현됨 |
| `MemberControllerTest` | **유지 (Service 추출 후 수정 필요)** | 현재 통합 테스트로 유효. Service 추출 시 단위 테스트 추가 및 기존 테스트 수정 필요 |

## 6. 권장 작업 순서

MemberController의 코드를 단순 삭제하는 것이 아니라, README.md `3-1`에 계획된 **MemberService + AuthService 추출 리팩토링**을 통해 비즈니스 로직을 이동해야 한다.

1. **AuthService 생성**: `register()`, `login()` 비즈니스 로직(이메일 중복 확인, 비밀번호 검증, JWT 발급)을 `AuthService`로 추출
2. **MemberService 생성**: 회원 조회, 수정, 포인트 충전 등 회원 관리 로직을 `MemberService`로 추출
3. **MemberController 리팩토링**: `AuthService`에 위임만 하도록 변경 (요청 파싱 + 응답 반환만 담당)
4. **AdminMemberController 리팩토링**: `MemberService`에 위임만 하도록 변경
5. **MemberControllerTest 수정**: Service 계층 도입에 맞춰 테스트 구조 조정 (기존 통합 테스트 유지 + Service 단위 테스트 추가)
6. **전체 테스트 실행**: 리팩토링 후 기존 동작이 유지되는지 확인
