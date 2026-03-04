# Organize Docs

root 디렉토리에 흩어진 문서 파일들을 `docs/` 하위 폴더로 정리한다.

## 문서 디렉토리 구조

```
docs/
├── plan/        # 계획 및 요구사항 (REQUIREMENT.md, TODO.md 등)
├── log/         # 프롬프트 기록 (PROMPT.md)
└── reports/     # 작업 보고서 (작업 완료 후 문서화)
```

## root에 유지하는 파일

아래 파일은 컨벤션상 root에 위치해야 하므로 **이동하지 않는다**.

- `README.md` - 프로젝트 표준 문서
- `CLAUDE.md` - Claude Code 설정 파일

## 실행 절차

### 1단계: 현황 파악

root에 있는 `.md` 파일 목록을 확인한다.

```bash
ls *.md
```

`docs/` 하위 구조를 확인한다.

```bash
find docs -type f
```

### 2단계: 분류

각 파일을 아래 기준으로 분류한다.

| 카테고리 | 대상 폴더 | 예시 |
|----------|-----------|------|
| 계획/요구사항 | `docs/plan/` | REQUIREMENT.md, TODO.md |
| 프롬프트 기록 | `docs/log/` | PROMPT.md |
| 작업 보고서 | `docs/reports/` | step1-style-cleanup.md |
| root 유지 | root | README.md, CLAUDE.md |

### 3단계: 이동

필요한 디렉토리를 생성하고 `git mv`로 파일을 이동한다.

```bash
mkdir -p docs/plan docs/log docs/reports
git mv <source> <target>
```

### 4단계: 참조 경로 업데이트

이동한 파일을 참조하는 곳의 경로를 업데이트한다.

- `CLAUDE.md` 내 경로 참조
- 다른 문서에서의 상대 경로 링크

### 5단계: 사용자 승인 후 커밋

변경 내용을 사용자에게 보여주고 승인을 받은 후 커밋한다.

## 주의사항

- `README.md`, `CLAUDE.md`는 절대 이동하지 않는다.
- `git mv`를 사용하여 git 이력을 보존한다.
- 이동 후 깨진 참조가 없는지 반드시 확인한다.
