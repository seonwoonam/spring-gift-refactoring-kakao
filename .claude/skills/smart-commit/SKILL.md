---
name: smart-commit
description: 수정된 파일들을 분석하고 적절한 커밋 메시지를 생성하여 Git 커밋합니다. 구조 변경과 작동 변경을 분리하고, README.md 기능 목록 단위로 커밋합니다.
disable-model-invocation: true
allowed-tools: Bash(git:*), Read, Grep, Glob
argument-hint: [커밋 관련 추가 지시사항]
---

# Smart Commit

수정된 파일을 분석하고 프로젝트 요구사항에 맞는 논리적 단위로 그룹화하여 커밋한다.

## 핵심 원칙

- **커밋 단위 = README.md 기능 목록 단위**: 커밋하기 전 README.md의 기능 목록을 확인하고, 해당 단위에 맞춰 커밋한다.
- **구조 변경과 작동 변경을 절대 섞지 않는다**: 한 커밋에는 구조 변경(리팩토링) 또는 작동 변경(기능 추가/수정) 중 하나만 담는다.
- **커밋은 목적 1개**: git diff를 보고 30초 안에 커밋 의도를 설명할 수 없으면 더 쪼갠다.
- **언어** : 한국어로 작성한다.

## 1단계: 변경 사항 파악

아래 명령어를 병렬로 실행하여 현재 상태를 파악한다.

```bash
git status --short
```

```bash
git diff --stat
```

```bash
git diff --staged --stat
```

```bash
git log --oneline -5
```

- untracked 파일, staged 파일, unstaged 변경 파일을 모두 확인한다.
- 최근 커밋 메시지 스타일을 참고한다.

## 2단계: README.md 기능 목록 확인

README.md를 읽어 현재 정의된 기능 목록과 구현 전략을 확인한다.

```bash
# README.md의 기능 목록/체크리스트를 확인
```

- 변경 사항이 README.md의 어떤 기능 항목에 해당하는지 매핑한다.
- README.md에 없는 변경이 포함되어 있으면 사용자에게 알린다.

## 3단계: 변경 내용 분석 및 분류

변경된 각 파일의 diff를 읽어 **무엇이 왜 변경되었는지** 파악한다.

```bash
git diff <파일>          # unstaged 변경
git diff --staged <파일>  # staged 변경
```

- 새 파일(untracked)은 Read 도구로 내용을 확인한다.
- `.env`, `credentials`, 시크릿이 포함된 파일은 커밋 대상에서 **제외**하고 사용자에게 경고한다.

각 변경을 아래 카테고리로 분류한다:

| 카테고리 | 설명 | 커밋 타입 |
|----------|------|-----------|
| **구조 변경** | 리팩토링, 레이어 분리, 코드 이동 (작동 변경 없음) | `refactor`, `style` |
| **작동 변경** | 새 기능, 버그 수정, 동작 수정 | `feat`, `fix` |
| **문서** | README, 전략 문서 등 | `docs` |
| **테스트** | 테스트 코드 추가/수정 | `test` |
| **유지보수** | 빌드, 설정 파일 등 | `chore` |

**구조 변경과 작동 변경이 동일 커밋에 섞여 있으면 반드시 분리한다.**

## 4단계: 그룹화 판단

변경 파일이 **하나의 논리적 변경**에 해당하면 단일 커밋으로 진행한다.

변경이 여러 목적을 가지면 아래 기준으로 그룹화한다:

| 그룹화 기준 | 예시 |
|------------|------|
| README.md 기능 항목 | 기능 목록의 한 항목에 대응하는 파일들 |
| 구조 변경 단위 | Controller → Service 로직 이동 (작동 변경 없음) |
| 작동 변경 단위 | 새 API 엔드포인트 추가 (컨트롤러 + 서비스 + DTO) |
| 문서 단위 | README, 전략 문서 등 |
| 테스트 단위 | 테스트 코드 + 테스트 리소스 |
| 스타일 단위 | 코드 포맷, 불필요 코드 제거 (작동 변경 없음) |

### 분리 검증

각 그룹에 대해 아래를 검증한다:

1. git diff를 보고 **30초 안에 커밋 의도를 설명**할 수 있는가?
2. 한 커밋에 **구조 변경과 작동 변경이 섞여 있지 않은가?**
3. 커밋 **목적이 1개**인가?

위 조건을 만족하지 않으면 그룹을 더 쪼갠다.

### 승인 요청

그룹화 결과를 사용자에게 보여주고 **승인을 받은 후** 커밋을 진행한다.

표시 형식:

```
커밋 1 [구조 변경]: <커밋 메시지>
  README 항목: <대응하는 기능 항목>
  - path/to/file1
  - path/to/file2

커밋 2 [작동 변경]: <커밋 메시지>
  README 항목: <대응하는 기능 항목>
  - path/to/file3
```

## 5단계: 커밋 실행

승인받은 그룹 순서대로 커밋한다. 각 커밋마다:

```bash
git add <파일1> <파일2> ...
```

```bash
git commit -m "$(cat <<'EOF'
<커밋 메시지>

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
```

## 커밋 메시지 규칙 (AngularJS Convention)

### 구조

```
<type>(<scope>): <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

### 기본 규칙

- 커밋 메시지의 **모든 줄은 100자를 넘지 않는다.**
- 제목, 본문, 바닥글을 **빈 행으로 구분**한다.

### Subject line

- **명령문, 현재 시제**로 작성한다: "change" (O) / "changed", "changes" (X)
- 첫 글자는 **소문자**로 작성한다.
- 끝에 **마침표(.)를 붙이지 않는다.**

### 타입 (`<type>`)

| 타입 | 설명 | 변경 종류 |
|------|------|-----------|
| `feat` | 새로운 기능 추가 | 작동 변경 |
| `fix` | 버그 수정 | 작동 변경 |
| `docs` | 문서 수정 | - |
| `style` | 포맷팅, 세미콜론 누락 등 (로직 변경 없음) | 구조 변경 |
| `refactor` | 코드 리팩토링 (기능 변경 없음) | 구조 변경 |
| `test` | 누락된 테스트 추가 | - |
| `chore` | 유지보수 | - |

### 스코프 (`<scope>`)

변경이 영향을 미치는 범위를 괄호 안에 표기한다. 생략 가능하다.

예: `feat(gift):`, `fix(option):`, `refactor(product):`

### Body

- subject와 동일하게 **명령문, 현재 시제**로 작성한다.
- 변경의 **동기(motivation)**와 **이전 동작과의 차이**를 설명한다.

### Footer

#### Breaking changes

모든 breaking change는 footer에 **BREAKING CHANGE:** 로 시작하여 변경 내용, 사유, 마이그레이션 방법을 기술한다.

```
BREAKING CHANGE: isolate scope bindings definition has changed.

    To migrate the code follow the example below:

    Before:
    scope: { myAttr: 'attribute' }

    After:
    scope: { myAttr: '@' }
```

#### Referencing issues

닫는 이슈는 footer에 **Closes** 키워드로 참조한다.

```
Closes #234
```

여러 이슈:

```
Closes #123, #245, #992
```

### 작성 예시

구조 변경:
```
refactor(product): extract business logic to service layer

move product CRUD logic from ProductController to ProductService
so that controller only handles request validation and delegation.
```

작동 변경:
```
feat(gift): add gift sending API endpoint

identify sender via Member-Id header,
deduct option stock, then delegate delivery to GiftDelivery.

Closes #42
```

스타일:
```
style(location): add couple of missing semi colons
```

### Co-Authored-By

모든 커밋 메시지 마지막에 아래를 포함한다.

```
Co-Authored-By: Claude <noreply@anthropic.com>
```

## 주의사항

- `--force`, `--no-verify`, `--amend`는 사용하지 않는다.
- `git push`는 하지 않는다.
- 커밋 전에 반드시 사용자 승인을 받는다.
- 이미 staged된 파일이 있으면 사용자에게 알리고 처리 방법을 확인한다.
- 구조 변경 커밋 후에는 기존 작동이 유지되는지 확인을 권장한다.

$ARGUMENTS
