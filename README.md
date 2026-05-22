# 벌금통 (bulgumtong)

> 4학년 소프트웨어 디자인 패턴 수업 팀 프로젝트  
> 스터디 그룹 출결 체크 및 벌금 자동 계산 시스템

---

## 프로젝트 개요

스터디원이 결석하거나 지각하면 벌금이 자동으로 계산되는 출결 관리 시스템입니다.  
**투명성 · 무결성 · 보안성** 세 가지 핵심 가치를 GoF 디자인 패턴 4종으로 구현했습니다.

| 핵심 가치 | 적용 패턴 | 설명 |
|-----------|-----------|------|
| 투명성 | Command | 모든 출결 변경이 이력으로 누적되어 "누가 언제 바꿨는지" 추적 가능 |
| 무결성 | State | 출결 상태 전이 규칙 엄격 제한, 타임어택 내에만 출석 인정 |
| 보안성 | Proxy | 스터디장 / 일반 멤버 권한 분리, 타인 출결 조작 방어 |

---

## 기술 스택

| 항목 | 내용 |
|------|------|
| 언어 | Java 17 (순수 Java, 프레임워크 없음) |
| 빌드 | Gradle 8.10 |
| 서버 | `com.sun.net.httpserver` (내장 HTTP 서버) |
| 저장소 | `ConcurrentHashMap` 기반 인메모리 |
| 테스트 | JUnit Jupiter 5.10 |

---

## 디자인 패턴

### Factory — 벌금 유형 생성

```
PenaltyFactory.create(ABSENCE) → AbsencePenalty  (결석 1회 = 5,000원)
PenaltyFactory.create(LATE)    → LatePenalty     (지각 1회 = 2,000원)
```

새로운 벌금 유형 추가 시 기존 코드를 수정하지 않고 구현체만 추가 (OCP 준수).

### Command — 출결 변경 이력

```
CheckAttendanceCommand.execute()  →  출결 상태 변경 + 이전 상태 저장
CheckAttendanceCommand.undo()     →  이전 상태로 복원 (타임어택 우회)
CommandHistory.push()             →  이력 스택에 누적
```

### State — 출결 상태 전이

```
ABSENT  →  PRESENT  (세션 시작 후 10분 이내만 허용)
ABSENT  →  LATE
PRESENT →  ABSENT / LATE
LATE    →  PRESENT / ABSENT
PRESENT →  PRESENT  ← IllegalStateException
ABSENT  →  ABSENT   ← IllegalStateException
```

### Proxy — 권한 분리

```
클라이언트 → AuthProxy (권한 검증) → StudyServiceImpl (비즈니스 로직)
```

| 기능 | LEADER | MEMBER |
|------|:------:|:------:|
| 멤버 추가 | ✅ | ❌ |
| 전체 출결 조회 | ✅ | ❌ |
| 본인 출결 조회 | ✅ | ✅ |
| 타인 출결 체크 | ✅ | ❌ |
| 변경 이력 조회 | ✅ | ❌ |
| 벌금 리포트 | ✅ | ❌ |

---

## 패키지 구조

```
src/main/java/com/bulgumtong/
├── Main.java                        # HTTP 서버 진입점 (port 8080)
├── config/
│   └── AppConfig.java               # 벌금 단가, 타임어택 시간 상수
├── init/
│   └── DataInitializer.java         # 더미 데이터 자동 로드
├── model/
│   ├── Member.java
│   ├── AttendanceRecord.java        # State 패턴 Context
│   ├── AttendanceStatus.java        # PRESENT / ABSENT / LATE
│   └── Role.java                    # LEADER / MEMBER
├── pattern/
│   ├── factory/                     # Factory 패턴
│   │   ├── Penalty.java
│   │   ├── AbsencePenalty.java
│   │   ├── LatePenalty.java
│   │   └── PenaltyFactory.java
│   ├── command/                     # Command 패턴
│   │   ├── Command.java
│   │   ├── CheckAttendanceCommand.java
│   │   └── CommandHistory.java
│   ├── state/                       # State 패턴
│   │   ├── AttendanceState.java
│   │   ├── AbsentState.java
│   │   ├── PresentState.java
│   │   ├── LateState.java
│   │   └── TimeValidator.java
│   └── proxy/                       # Proxy 패턴
│       ├── StudyService.java
│       ├── StudyServiceImpl.java
│       ├── AuthProxy.java
│       └── UnauthorizedException.java
├── repository/
│   ├── MemberRepository.java
│   └── AttendanceRepository.java
├── service/
│   ├── AttendanceService.java
│   └── FineCalculatorService.java
└── report/
    └── ReportPrinter.java
```

---

## 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행 (http://localhost:8080)
./gradlew run

# 테스트
./gradlew test
```

---

## API 엔드포인트

| Method | URL | 권한 | 설명 |
|--------|-----|:----:|------|
| GET | `/api/members` | 전체 | 멤버 목록 조회 |
| POST | `/api/members?requesterId=&name=&role=` | LEADER | 멤버 추가 |
| GET | `/api/attendance?requesterId=&memberId=` | 본인/LEADER | 출결 조회 |
| POST | `/api/attendance?requesterId=&memberId=&date=&status=` | 본인/LEADER | 출결 체크 |
| GET | `/api/history?requesterId=` | LEADER | 변경 이력 조회 |
| GET | `/api/report?requesterId=` | LEADER | 벌금 정산 리포트 |

`status` 값: `PRESENT` / `ABSENT` / `LATE`  
`date` 형식: `YYYY-MM-DD`

---

## 더미 데이터

서버 시작 시 자동 로드됩니다.

| ID | 이름 | 역할 |
|----|------|------|
| m-001 | 김민준 | 스터디장 |
| m-002 | 이수연 | 일반 멤버 |
| m-003 | 박준혁 | 일반 멤버 |
| m-004 | 정유나 | 일반 멤버 |
| m-005 | 최성현 | 일반 멤버 |

2주치 세션 이력이 포함되어 있어 서버 시작 직후 `/api/report`로 벌금 정산 확인이 가능합니다.

---

## 문서

| 문서 | 경로 |
|------|------|
| 프로젝트 계획서 (WBS) | `docs/plan.md` |
| API 명세서 | `docs/api-spec.md` |
| 디자인 패턴 상세 설명 | `docs/design-patterns.md` |
| Postman 컬렉션 | `docs/bulgumtong.postman_collection.json` |
