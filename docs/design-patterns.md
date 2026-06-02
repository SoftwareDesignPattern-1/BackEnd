# bulgumtong 디자인 패턴 구현 문서

> 4개 패턴(Factory, Command, State, Proxy) 각각의 적용 목적, 클래스 구조, 메서드별 역할을 상세히 기술합니다.

---

## 목차

1. [Factory 패턴](#1-factory-패턴)
2. [Command 패턴](#2-command-패턴)
3. [State 패턴](#3-state-패턴)
4. [Proxy 패턴](#4-proxy-패턴)
5. [패턴 간 협력 흐름](#5-패턴-간-협력-흐름)

---

## 1. Factory 패턴

### 적용 목적

벌금 유형(결석/지각)이 추가될 때 **호출 코드를 수정하지 않고** 새로운 벌금 객체를 생성할 수 있도록 한다. (OCP: 개방-폐쇄 원칙)

### 클래스 구조

```
Penalty (interface)
    ├── AbsencePenalty
    └── LatePenalty

PenaltyFactory
    └── create(PenaltyType) → Penalty
```

### 파일별 메서드 설명

---

#### `Penalty.java` — 인터페이스

> 경로: `pattern/factory/Penalty.java`

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `calculate(int count)` | `int` | 횟수를 받아 벌금 총액(원)을 계산한다. 구현체마다 단가가 다르다. |
| `getDescription()` | `String` | 이 벌금 유형의 설명 문자열을 반환한다. 리포트 출력에 사용된다. |

---

#### `AbsencePenalty.java` — 결석 벌금 구현체

> 경로: `pattern/factory/AbsencePenalty.java`

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `calculate(int absenceCount)` | `int` | `absenceCount × AppConfig.FINE_PER_ABSENCE(5,000원)` 를 반환한다. |
| `getDescription()` | `String` | `"결석 벌금 (1회당 5,000원)"` 을 반환한다. |

---

#### `LatePenalty.java` — 지각 벌금 구현체

> 경로: `pattern/factory/LatePenalty.java`

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `calculate(int lateCount)` | `int` | `lateCount × AppConfig.FINE_PER_LATE(2,000원)` 을 반환한다. |
| `getDescription()` | `String` | `"지각 벌금 (1회당 2,000원)"` 을 반환한다. |

---

#### `PenaltyFactory.java` — 팩토리

> 경로: `pattern/factory/PenaltyFactory.java`

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `create(PenaltyType type)` | `Penalty` | `PenaltyType` 열거값에 따라 알맞은 구현체를 생성해 반환한다. 호출자는 `AbsencePenalty`, `LatePenalty` 클래스를 직접 알 필요가 없다. |

```java
// 호출 예시 — FineCalculatorService에서
Penalty p = PenaltyFactory.create(PenaltyFactory.PenaltyType.ABSENCE);
int fine = p.calculate(2); // → 10,000원
```

> **새로운 벌금 유형 추가 시:** `Penalty` 구현체 하나와 `PenaltyType` 열거값 하나만 추가하면 된다.
> `FineCalculatorService` 등 기존 호출 코드는 수정하지 않는다.

---

## 2. Command 패턴

### 적용 목적

출결 변경 행위를 **객체로 캡슐화**해 이력을 누적 저장(투명성)하고, 필요 시 되돌리기(undo)가 가능하도록 한다.

### 클래스 구조

```
Command (interface)
    └── CheckAttendanceCommand   ← 구체적 명령

CommandHistory                   ← 명령 이력 저장소 (Invoker)
AttendanceRecord                 ← 명령이 조작하는 대상 (Receiver)
```

### 파일별 메서드 설명

---

#### `Command.java` — 인터페이스

> 경로: `pattern/command/Command.java`

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `execute()` | `void` | 명령을 실행한다. 실행 전 상태를 내부에 저장해 undo를 대비한다. |
| `undo()` | `void` | 실행 전 상태로 되돌린다. |
| `getDescription()` | `String` | 이 명령이 무엇을 했는지 설명하는 문자열을 반환한다. 이력 조회 API에서 사용된다. |

---

#### `CheckAttendanceCommand.java` — 출결 변경 명령

> 경로: `pattern/command/CheckAttendanceCommand.java`

**필드:**

| 필드 | 설명 |
|------|------|
| `record` | 조작 대상 `AttendanceRecord` (Receiver) |
| `targetStatus` | 변경할 목표 상태 (`PRESENT` / `ABSENT` / `LATE`) |
| `executedBy` | 명령을 실행한 멤버 ID (이력 추적용) |
| `previousState` | execute() 직전의 State 객체 — undo 복원에 사용 |
| `previousCheckedAt` | execute() 직전의 checkedAt 시각 — undo 복원에 사용 |

**메서드:**

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `execute()` | `void` | 실행 전 상태(`previousState`, `previousCheckedAt`)를 저장한 뒤 `targetStatus`에 맞는 `record.checkIn()` / `markAbsent()` / `markLate()` 를 호출한다. |
| `undo()` | `void` | `record.setState(previousState)` 와 `record.setCheckedAt(previousCheckedAt)` 를 직접 호출해 State 전이를 우회하고 이전 상태를 그대로 복원한다. 타임어택 검증을 거치지 않으므로 시간이 지난 뒤에도 안전하게 되돌릴 수 있다. |
| `getDescription()` | `String` | `"[memberId] 출결 변경 → 출석 (by leaderId)"` 형식의 이력 문자열을 반환한다. |
| `getRecord()` | `AttendanceRecord` | 명령이 조작하는 레코드를 반환한다. |
| `getExecutedBy()` | `String` | 실행자 ID를 반환한다. |

```java
// 흐름 예시 — AttendanceService에서
CheckAttendanceCommand cmd = new CheckAttendanceCommand(record, AttendanceStatus.PRESENT, requesterId);
cmd.execute();          // State 전이 발생
commandHistory.push(cmd); // 이력 누적
```

---

#### `CommandHistory.java` — 이력 저장소

> 경로: `pattern/command/CommandHistory.java`

**내부 구조:**
- `Deque<Command> history` — `ArrayDeque` 기반 스택. `push()`로 최신 항목이 위에 쌓인다.

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `push(Command command)` | `void` | 실행 완료된 명령을 스택에 추가한다. `AttendanceService.checkAttendance()` 가 `cmd.execute()` 직후 호출한다. |
| `undoLast()` | `void` | 스택 최상단 명령을 꺼내 `undo()` 를 호출한다. 이력이 비어있으면 `IllegalStateException` 을 던진다. |
| `getDescriptions()` | `List<String>` | 전체 이력의 설명 문자열 목록을 최신순으로 반환한다. `GET /api/history` 엔드포인트에서 사용된다. 반환 리스트는 수정 불가(unmodifiable). |
| `isEmpty()` | `boolean` | 이력이 비어있는지 확인한다. |
| `size()` | `int` | 누적 이력 수를 반환한다. |

---

## 3. State 패턴

### 적용 목적

출결 상태(`ABSENT` / `PRESENT` / `LATE`)에 따라 허용되는 전이(transition)가 다르므로, **조건 분기 없이 상태 객체 자체가 전이 규칙을 캡슐화**한다. 임의 상태 변경을 원천 차단해 무결성을 확보한다.

### 클래스 구조

```
AttendanceState (interface)          ← State
    ├── PresentState                 ← ConcreteState
    ├── AbsentState                  ← ConcreteState
    └── LateState                    ← ConcreteState

AttendanceRecord                     ← Context (state 필드 보유)
TimeValidator                        ← 타임어택 검증 유틸
```

### 파일별 메서드 설명

---

#### `AttendanceState.java` — State 인터페이스

> 경로: `pattern/state/AttendanceState.java`

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `checkIn(AttendanceRecord record)` | `void` | 출석 처리 요청. 현재 상태가 허용하지 않으면 예외를 던진다. |
| `markAbsent(AttendanceRecord record)` | `void` | 결석 처리 요청. 현재 상태가 허용하지 않으면 예외를 던진다. |
| `markLate(AttendanceRecord record)` | `void` | 지각 처리 요청. 현재 상태가 허용하지 않으면 예외를 던진다. |
| `getStatus()` | `AttendanceStatus` | 이 상태 객체가 나타내는 열거값을 반환한다. |

---

#### `AbsentState.java` — 결석 상태

> 경로: `pattern/state/AbsentState.java`

| 메서드 | 동작 | 설명 |
|--------|------|------|
| `checkIn(record)` | **전이 허용 (조건부)** | `TimeValidator.isWithinWindow()` 로 타임어택을 검증한다. 통과 시 `record.setState(new PresentState())` 로 전이하고 `checkedAt` 을 현재 시각으로 기록한다. 시간 초과 시 `IllegalStateException` |
| `markAbsent(record)` | **전이 거부** | 이미 결석이므로 `IllegalStateException` 을 던진다. |
| `markLate(record)` | **전이 허용** | `record.setState(new LateState())` 로 전이한다. 타임어택 적용 없음. |
| `getStatus()` | — | `AttendanceStatus.ABSENT` 반환 |

---

#### `PresentState.java` — 출석 상태

> 경로: `pattern/state/PresentState.java`

| 메서드 | 동작 | 설명 |
|--------|------|------|
| `checkIn(record)` | **전이 거부** | 이미 출석이므로 `IllegalStateException` 을 던진다. |
| `markAbsent(record)` | **전이 허용** | `record.setState(new AbsentState())` 로 전이하고 `checkedAt` 을 `null` 로 초기화한다. |
| `markLate(record)` | **전이 허용** | `record.setState(new LateState())` 로 전이한다. |
| `getStatus()` | — | `AttendanceStatus.PRESENT` 반환 |

---

#### `LateState.java` — 지각 상태

> 경로: `pattern/state/LateState.java`

| 메서드 | 동작 | 설명 |
|--------|------|------|
| `checkIn(record)` | **전이 허용** | `record.setState(new PresentState())` 로 전이하고 `checkedAt` 을 현재 시각으로 갱신한다. 지각→출석 정정 시나리오에 사용된다. |
| `markAbsent(record)` | **전이 허용** | `record.setState(new AbsentState())` 로 전이하고 `checkedAt` 을 `null` 로 초기화한다. |
| `markLate(record)` | **전이 거부** | 이미 지각이므로 `IllegalStateException` 을 던진다. |
| `getStatus()` | — | `AttendanceStatus.LATE` 반환 |

---

#### `TimeValidator.java` — 타임어택 검증

> 경로: `pattern/state/TimeValidator.java`

| 메서드 | 반환 | 설명 |
|--------|------|------|
| `isWithinWindow(LocalDateTime sessionStart)` | `boolean` | 현재 시각과 `sessionStart` 의 차이가 `0 ≤ 경과 시간 ≤ AppConfig.ATTENDANCE_WINDOW_MINUTES(10분)` 이면 `true` 를 반환한다. `AbsentState.checkIn()` 에서만 호출되어 출석 처리 가능 여부를 결정한다. |

---

#### `AttendanceRecord.java` — State Context

> 경로: `model/AttendanceRecord.java`

State 패턴에서 **Context** 역할. `AttendanceState` 를 필드로 보유하고, 모든 상태 전이 요청을 현재 상태 객체에 위임한다.

| 메서드 | 설명 |
|--------|------|
| `checkIn()` | `state.checkIn(this)` 를 호출한다. 실제 로직은 현재 state 객체가 처리한다. |
| `markAbsent()` | `state.markAbsent(this)` 를 호출한다. |
| `markLate()` | `state.markLate(this)` 를 호출한다. |
| `getStatus()` | `state.getStatus()` 를 호출해 현재 상태 열거값을 반환한다. |
| `getCurrentState()` | 현재 `AttendanceState` 객체를 그대로 반환한다. `CheckAttendanceCommand.execute()` 가 undo를 위해 이전 상태를 저장할 때 사용한다. |
| `setState(AttendanceState)` | 상태 객체를 교체한다. 각 ConcreteState의 전이 메서드와 Command의 `undo()` 가 직접 호출한다. |

**상태 전이 요약:**

```
        checkIn()        markLate()
ABSENT ─────────► PRESENT ────────► LATE
  │    (10분 이내)    │                │
  │                   │ markAbsent()   │ checkIn()
  │◄──────────────────┘                │
  │           markAbsent()             │
  │◄───────────────────────────────────┘
```

---

## 4. Proxy 패턴

### 적용 목적

`StudyServiceImpl` (실제 서비스)을 직접 노출하지 않고, **동일 인터페이스를 구현한 `AuthProxy` 가 앞에서 권한 검증을 수행**한다. 클라이언트(Main, Controller)는 `StudyService` 인터페이스만 바라보므로 Proxy 존재를 알 필요가 없다.

### 클래스 구조

```
StudyService (interface)             ← Subject
    ├── StudyServiceImpl             ← RealSubject
    └── AuthProxy                    ← Proxy (StudyServiceImpl을 래핑)

UnauthorizedException               ← 권한 위반 예외
MemberRepository                    ← AuthProxy가 권한 검증에 사용
```

### 파일별 메서드 설명

---

#### `StudyService.java` — Subject 인터페이스

> 경로: `pattern/proxy/StudyService.java`

`StudyServiceImpl` 과 `AuthProxy` 가 동일하게 구현하는 인터페이스. `Main.java` 는 이 타입으로만 서비스를 참조한다.

| 메서드 | 설명 |
|--------|------|
| `addMember(requesterId, name, roleStr)` | 멤버 추가 |
| `getAllMembers()` | 전체 멤버 조회 |
| `checkAttendance(requesterId, targetMemberId, date, status)` | 출결 체크 |
| `getRecordsByMember(requesterId, memberId)` | 특정 멤버 출결 조회 |
| `getAllRecords(requesterId)` | 전체 출결 조회 |
| `getCommandHistory(requesterId)` | 변경 이력 조회 |

---

#### `StudyServiceImpl.java` — RealSubject (실제 서비스)

> 경로: `pattern/proxy/StudyServiceImpl.java`

권한 검증 없이 **비즈니스 로직만 수행**한다. `AuthProxy` 가 허용한 요청만 여기까지 도달한다.

| 메서드 | 설명 |
|--------|------|
| `addMember(requesterId, name, roleStr)` | `Role` 열거값으로 변환 후 `Member` 객체를 생성해 `MemberRepository` 에 저장한다. |
| `getAllMembers()` | `MemberRepository.findAll()` 을 그대로 반환한다. |
| `checkAttendance(...)` | `AttendanceService.checkAttendance()` 에 위임한다. Command + State 패턴이 여기서 발동된다. |
| `getRecordsByMember(...)` | `AttendanceService.getRecordsByMember()` 에 위임한다. |
| `getAllRecords(...)` | `AttendanceService.getAllRecords()` 에 위임한다. |
| `getCommandHistory(...)` | `AttendanceService.getHistory()` 에 위임한다. |

---

#### `AuthProxy.java` — Proxy (권한 검증 계층)

> 경로: `pattern/proxy/AuthProxy.java`

`StudyService` 인터페이스를 구현하며, 내부적으로 `StudyServiceImpl` 인스턴스(`real`)를 보유한다. 각 메서드 호출 시 권한을 먼저 검증하고, 통과하면 `real.메서드()` 를 그대로 위임한다.

| 메서드 | 권한 규칙 | 처리 방식 |
|--------|-----------|-----------|
| `addMember(requesterId, ...)` | LEADER만 | `requireLeader()` 후 `real.addMember()` 위임 |
| `getAllMembers()` | 전체 공개 | 검증 없이 `real.getAllMembers()` 위임 |
| `checkAttendance(requesterId, targetMemberId, ...)` | LEADER 또는 본인 | `requesterId == targetMemberId` 이거나 LEADER면 허용 |
| `getRecordsByMember(requesterId, memberId)` | LEADER 또는 본인 | `requesterId == memberId` 이거나 LEADER면 허용 |
| `getAllRecords(requesterId)` | LEADER만 | `requireLeader()` 후 `real.getAllRecords()` 위임 |
| `getCommandHistory(requesterId)` | LEADER만 | `requireLeader()` 후 `real.getCommandHistory()` 위임 |

**내부 헬퍼 메서드:**

| 메서드 | 설명 |
|--------|------|
| `getRequester(requesterId)` | `MemberRepository` 에서 요청자를 조회한다. 존재하지 않으면 `UnauthorizedException` 을 던진다. 모든 권한 검증의 진입점. |
| `requireLeader(requesterId)` | `getRequester()` 로 요청자를 확인하고 `isLeader()` 가 `false` 이면 `UnauthorizedException` 을 던진다. |

```java
// Main.java 조립 — Proxy를 통해서만 접근
StudyServiceImpl real    = new StudyServiceImpl(memberRepo, attendanceSvc);
StudyService     service = new AuthProxy(real, memberRepo); // AuthProxy로 래핑

// 클라이언트는 StudyService 인터페이스만 사용
service.checkAttendance(...); // Proxy가 권한 먼저 검증
```

---

#### `UnauthorizedException.java` — 권한 위반 예외

> 경로: `pattern/proxy/UnauthorizedException.java`

`RuntimeException` 을 상속한 비검사 예외. `AuthProxy` 에서 권한 검증 실패 시 던지며, `Main.java` 의 각 핸들러에서 잡아 HTTP 403 응답으로 변환한다.

---

## 5. 패턴 간 협력 흐름

### 출결 체크 전체 흐름 (`POST /api/attendance`)

```
Main.handleAttendance()
  │
  ▼
AuthProxy.checkAttendance()          [Proxy 패턴]
  │ 권한 검증 (requesterId 확인, 본인/리더 여부)
  │
  ▼
StudyServiceImpl.checkAttendance()   [RealSubject]
  │
  ▼
AttendanceService.checkAttendance()
  │ ① 레코드 조회 또는 생성
  │
  ▼
CheckAttendanceCommand(record, status, requesterId)
  │ ② cmd.execute() 호출
  │    └─ record.getCurrentState() 저장   (undo 대비)
  │    └─ record.checkIn() / markAbsent() / markLate()
  │                                        [Command 패턴]
  │         ▼
  │     AttendanceRecord.checkIn()         [State Context]
  │         └─ state.checkIn(this)
  │                                        [State 패턴]
  │             ├─ AbsentState: TimeValidator 검증 후 PresentState로 전이
  │             ├─ PresentState: 예외 (이미 출석)
  │             └─ LateState: PresentState로 전이
  │
  ③ commandHistory.push(cmd)              [Command 이력 누적]
```

### 벌금 정산 흐름 (`GET /api/report`)

```
Main.handleReport()
  │
  ▼
AuthProxy.getAllRecords()              [Proxy — 리더 검증]
  │
  ▼
ReportPrinter.generateFineReport()
  │
  ▼
FineCalculatorService.calculateTotalFine(memberId)
  │
  ├─ PenaltyFactory.create(ABSENCE) → AbsencePenalty  [Factory 패턴]
  │    └─ calculate(absenceCount) → 결석 벌금
  │
  └─ PenaltyFactory.create(LATE) → LatePenalty         [Factory 패턴]
       └─ calculate(lateCount)   → 지각 벌금
```
