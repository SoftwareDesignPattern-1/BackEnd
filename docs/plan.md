# 벌금통 (bulgumtong)
## 프로젝트 계획서 (Project Planning Document)

> **작성일:** 2026-05-22
> **과목:** 소프트웨어 디자인 패턴 (4학년 팀 프로젝트)
> **프로젝트명:** bulgumtong
> **기술 스택:** Java 17+ (순수 Java, 프레임워크 없음)
> **빌드 도구:** Gradle
> **저장 방식:** Java Collection (List, Map) + 더미 데이터
> **인터페이스:** 웹
> **개발 기간:** 2주

---

## 1. 문제 정의 및 범위 설정 (Problem & Scope)

### 1.1 팩트 체크 및 기본 주제

| 항목 | 내용 |
|------|------|
| 과제 배경 | 4학년 소프트웨어 디자인 패턴 수업 1팀 프로젝트 |
| 프로젝트 주제 | 스터디 그룹 출석 및 벌금 자동 계산기 |
| 핵심 도메인 | 스터디원 출결 체크 → 결석 횟수에 따른 벌금 자동 합산 및 정산 |

### 1.2 핵심 가치 정의 (품질 속성)

| 가치 | 설명 | 적용 패턴 |
|------|------|-----------|
| **투명성 (Transparency)** | "누가, 언제, 왜 벌금을 냈는가"에 대한 의혹을 없애기 위해 변경 이력을 누적 저장 | Command 패턴 |
| **무결성 (Integrity)** | 대리 출석·임의 변경 방지를 위해 타임어택 내에만 출석 인정, 출결 상태 전이 규칙 엄격 제한 | State 패턴 |
| **보안성 (Security)** | 스터디장과 일반 멤버의 권한을 분리하여 타인의 출결 조작 방어 | Proxy 패턴 |

### 1.3 In Scope — MVP 범위

- 스터디원 추가 / 조회
- 날짜별 출결 체크 (출석 / 결석 / 지각)
- 결석 횟수 × 벌금 단가 → 총 벌금 자동 계산
- 멤버별 벌금 정산 리포트 출력
- 출결 변경 이력 누적 저장
- 스터디장 / 일반 멤버 권한 분리

### 1.4 Out of Scope

- 외부 DB 연동
- 회원가입 / 로그인 구현 → 더미 데이터로 대체
- JSON 파일 영속성
- 알림 기능

---

## 2. 타당성 및 리스크 분석 (Feasibility & Risk)

### 2.1 기술적 리스크 및 대책

| # | 리스크 | 발생 가능성 | 대응 전략 |
|---|--------|:-----------:|-----------|
| 1 | **동시성 문제** — 여러 명이 동시에 출석을 누를 때 데이터 충돌 | 중 | `ConcurrentHashMap` 사용 + 핵심 메서드 `synchronized` 적용 |
| 2 | **데이터 휘발성** — 외부 DB 없어 프로그램 종료 시 데이터 소멸 | 높음 | 프로그램 시작 시 테스트 시나리오용 더미 데이터 자동 로드 |
| 3 | **프레임워크 주객전도** — Spring 환경 설정 오류로 시간 낭비 | 높음 | 순수 Java 콘솔로 기술 스택 낮춰 디자인 패턴 설계에만 집중 |

---

## 3. 기술 스택 및 구현 방식 (Tech Stack)

### 3.1 의존성 (build.gradle)

```groovy
plugins {
    id 'java'
    id 'application'
}

group = 'com.bulgumtong'
version = '1.0.0'

application {
    mainClass = 'com.bulgumtong.Main'
}

repositories { mavenCentral() }

dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
}

test { useJUnitPlatform() }
```

### 3.2 실행 방법

```bash
./gradlew run     # 실행
./gradlew test    # 테스트
```

---

## 4. 아키텍처 설계 (Architecture)

### 4.1 패키지 구조

```
bulgumtong/
├── build.gradle
└── src/main/java/com/bulgumtong/
    ├── Main.java
    ├── config/AppConfig.java
    ├── init/DataInitializer.java
    ├── model/
    │   ├── Member.java
    │   ├── AttendanceRecord.java
    │   └── AttendanceStatus.java
    ├── pattern/
    │   ├── factory/  (Penalty, AbsencePenalty, PenaltyFactory)
    │   ├── command/  (Command, CheckAttendanceCommand, CommandHistory)
    │   ├── state/    (AttendanceState, PresentState, AbsentState)
    │   └── proxy/    (StudyService, StudyServiceImpl, AuthProxy)
    ├── service/      (AttendanceService, FineCalculatorService)
    ├── repository/   (AttendanceRepository)
    └── report/       (ReportPrinter)
```

### 4.2 적용 디자인 패턴 요약

| 패턴 | 적용 위치 | 목적 |
|------|-----------|------|
| **Factory** | `PenaltyFactory` | 벌금 유형별 정산 객체 생성, OCP 준수 |
| **Command** | `CommandHistory` | 출결 변경 이력 누적 → 투명성 확보 |
| **State** | `AttendanceState` | 출결 상태 전이 규칙 엄격 제한 → 무결성 확보 |
| **Proxy** | `AuthProxy` | 스터디장/멤버 권한 분리 → 보안성 확보 |

---

## 5. WBS (Work Breakdown Structure)

### 5.1 WBS 개요

> 총 **8개 대분류**, **27개 중분류**, **약 70개 소분류 작업**으로 구성
> 2주(14일) / 1일 평균 4~5시간 기준 산정

### 5.2 상세 작업 분할

#### 📌 1.0 계획 및 분석 (Planning & Analysis) — `0.5일`

| WBS ID | 작업명 | 예상 공수 | 산출물 | 상태 |
|--------|--------|:---------:|--------|:----:|
| 1.1 | 문제 정의 및 범위 설정 | 1h | Scope Definition | ✅ |
| 1.1.1 | 핵심 문제 및 도메인 정의 | 0.5h | 기획 메모 | ✅ |
| 1.1.2 | In Scope / Out of Scope 확정 | 0.5h | 범위 매트릭스 | ✅ |
| 1.2 | 핵심 가치 및 패턴 매핑 | 1h | 가치-패턴 매핑표 | ✅ |
| 1.3 | 타당성 및 리스크 분석 | 1h | 리스크 매트릭스 | ✅ |
| 1.4 | 기술 스택 확정 | 0.5h | Tech Stack 문서 | ✅ |
| 1.5 | WBS 및 일정 수립 | 1h | 본 문서 | 🟡 |

---

#### 📌 2.0 설계 (Design) — `2일`

| WBS ID | 작업명 | 예상 공수 | 산출물 | 의존성 |
|--------|--------|:---------:|--------|--------|
| 2.1 | 클래스 다이어그램 작성 | 3h | UML (Mermaid) | 1.0 |
| 2.1.1 | model 패키지 클래스 설계 | 1h | Member, AttendanceRecord | - |
| 2.1.2 | 4개 패턴 클래스 다이어그램 | 2h | Factory/Command/State/Proxy UML | - |
| 2.2 | 시퀀스 다이어그램 작성 | 2h | Sequence Diagram | 2.1 |
| 2.2.1 | "출결 체크" 시퀀스 | 1h | Mermaid 코드 | - |
| 2.2.2 | "벌금 정산" 시퀀스 | 1h | Mermaid 코드 | - |
| 2.3 | 상태 다이어그램 (State 패턴용) | 1h | State Transition Diagram | 2.1 |
| 2.4 | 더미 데이터 시나리오 설계 | 1h | 테스트 데이터 명세 | 2.1 |

---

#### 📌 3.0 환경 설정 (Environment Setup) — `0.5일`

| WBS ID | 작업명 | 예상 공수 | 산출물 | 의존성 |
|--------|--------|:---------:|--------|--------|
| 3.1 | Git 저장소 생성 및 초기 커밋 | 0.5h | GitHub repo | - |
| 3.2 | Gradle 프로젝트 초기화 | 0.5h | build.gradle | 3.1 |
| 3.3 | .gitignore, README 작성 | 0.5h | 프로젝트 메타파일 | 3.2 |
| 3.4 | 패키지 구조 생성 | 0.5h | 빈 디렉토리 트리 | 3.2 |
| 3.5 | Git 브랜치 전략 수립 (main / feature/*) | 0.5h | 브랜치 컨벤션 문서 | 3.1 |

---

#### 📌 4.0 핵심 모델 구현 (Core Model) — `1일`

| WBS ID | 작업명 | 예상 공수 | 산출물 | 의존성 |
|--------|--------|:---------:|--------|--------|
| 4.1 | Enum 정의 | 1h | AttendanceStatus, Role | 3.0 |
| 4.2 | Member 클래스 구현 | 1h | Member.java | 4.1 |
| 4.3 | AttendanceRecord 클래스 구현 | 1h | AttendanceRecord.java | 4.1 |
| 4.4 | AppConfig 상수 클래스 | 0.5h | AppConfig.java | - |
| 4.5 | AttendanceRepository 구현 | 1.5h | Repository.java | 4.2, 4.3 |
| 4.5.1 | ConcurrentHashMap 기반 저장 로직 | 1h | - | - |
| 4.5.2 | CRUD 메서드 (save, findAll, findById) | 0.5h | - | - |
| 4.6 | DataInitializer 더미 데이터 | 1h | DataInitializer.java | 4.5 |

---

#### 📌 5.0 디자인 패턴 구현 (Pattern Implementation) — `4일`

##### 5.1 Factory 패턴 — `0.5일`

| WBS ID | 작업명 | 예상 공수 | 산출물 |
|--------|--------|:---------:|--------|
| 5.1.1 | Penalty 인터페이스 정의 | 0.5h | Penalty.java |
| 5.1.2 | AbsencePenalty 구현체 | 1h | AbsencePenalty.java |
| 5.1.3 | PenaltyFactory 클래스 | 1h | PenaltyFactory.java |
| 5.1.4 | Factory 단위 테스트 | 1h | PenaltyFactoryTest.java |

##### 5.2 Command 패턴 — `1일`

| WBS ID | 작업명 | 예상 공수 | 산출물 |
|--------|--------|:---------:|--------|
| 5.2.1 | Command 인터페이스 정의 (execute, undo) | 0.5h | Command.java |
| 5.2.2 | CheckAttendanceCommand 구현 | 1.5h | CheckAttendanceCommand.java |
| 5.2.3 | CommandHistory (이력 누적 스택) | 1h | CommandHistory.java |
| 5.2.4 | 이력 조회 메서드 구현 | 0.5h | - |
| 5.2.5 | Command 단위 테스트 | 1h | CommandTest.java |

##### 5.3 State 패턴 — `1일`

| WBS ID | 작업명 | 예상 공수 | 산출물 |
|--------|--------|:---------:|--------|
| 5.3.1 | AttendanceState 인터페이스 | 0.5h | AttendanceState.java |
| 5.3.2 | PresentState 구현 (전이 규칙 포함) | 1h | PresentState.java |
| 5.3.3 | AbsentState 구현 (전이 규칙 포함) | 1h | AbsentState.java |
| 5.3.4 | 타임어택 검증 로직 | 1h | TimeValidator.java |
| 5.3.5 | State 단위 테스트 | 1h | StateTest.java |

##### 5.4 Proxy 패턴 — `1일`

| WBS ID | 작업명 | 예상 공수 | 산출물 |
|--------|--------|:---------:|--------|
| 5.4.1 | StudyService 인터페이스 | 0.5h | StudyService.java |
| 5.4.2 | StudyServiceImpl 실제 구현체 | 1.5h | StudyServiceImpl.java |
| 5.4.3 | AuthProxy 권한 검증 Proxy | 1.5h | AuthProxy.java |
| 5.4.4 | 권한 위반 예외 처리 | 0.5h | UnauthorizedException.java |
| 5.4.5 | Proxy 단위 테스트 | 1h | AuthProxyTest.java |

---

#### 📌 6.0 비즈니스 로직 및 통합 (Service & Integration) — `2일`

| WBS ID | 작업명 | 예상 공수 | 산출물 | 의존성 |
|--------|--------|:---------:|--------|--------|
| 6.1 | AttendanceService 구현 | 2h | AttendanceService.java | 5.0 |
| 6.1.1 | 출결 체크 로직 (Command + State 연계) | 1h | - | 5.2, 5.3 |
| 6.1.2 | 이력 조회 로직 | 1h | - | 5.2 |
| 6.2 | FineCalculatorService 구현 | 1.5h | FineCalculatorService.java | 5.1 |
| 6.2.1 | Factory 활용 벌금 계산 | 1h | - | - |
| 6.2.2 | 멤버별 누적 벌금 합산 | 0.5h | - | - |
| 6.3 | ReportPrinter 구현 | 1h | ReportPrinter.java | 6.1, 6.2 |
| 6.4 | Main.java CLI 통합 | 2h | Main.java | 6.1~6.3 |
| 6.4.1 | Scanner 입력 처리 | 0.5h | - | - |
| 6.4.2 | 메뉴 라우팅 (switch-case) | 1h | - | - |
| 6.4.3 | AuthProxy 통합 | 0.5h | - | 5.4 |

---

#### 📌 7.0 테스트 및 디버깅 (Testing) — `2일`

| WBS ID | 작업명 | 예상 공수 | 산출물 |
|--------|--------|:---------:|--------|
| 7.1 | 통합 테스트 시나리오 작성 | 1h | 테스트 시나리오 문서 |
| 7.2 | 정상 흐름 테스트 (Happy Path) | 2h | IntegrationTest.java |
| 7.2.1 | 출결 → 벌금 정산 전 과정 검증 | - | - |
| 7.3 | 예외 흐름 테스트 (Edge Case) | 2h | - |
| 7.3.1 | 권한 없는 사용자 접근 차단 검증 | - | - |
| 7.3.2 | 타임어택 초과 시 출석 거부 검증 | - | - |
| 7.3.3 | 잘못된 상태 전이 차단 검증 | - | - |
| 7.4 | 동시성 테스트 (멀티스레드) | 2h | ConcurrencyTest.java |
| 7.5 | 버그 수정 및 리팩토링 | 3h | 커밋 로그 |

---

#### 📌 8.0 문서화 및 발표 준비 (Documentation) — `2일`

| WBS ID | 작업명 | 예상 공수 | 산출물 |
|--------|--------|:---------:|--------|
| 8.1 | README.md 작성 | 1.5h | README.md |
| 8.1.1 | 프로젝트 소개, 빌드/실행법 | - | - |
| 8.1.2 | 패턴별 적용 근거 설명 | - | - |
| 8.2 | 최종 보고서 작성 | 4h | 최종보고서.md/pdf |
| 8.2.1 | 1. 프로젝트 개요 | - | - |
| 8.2.2 | 2. 요구사항 분석 | - | - |
| 8.2.3 | 3. 시스템 설계 (다이어그램 포함) | - | - |
| 8.2.4 | 4. 구현 결과 (코드 스니펫) | - | - |
| 8.2.5 | 5. AI 협업 기록 | - | - |
| 8.2.6 | 6. 결론 및 향후 과제 | - | - |
| 8.3 | 발표 자료 제작 (PPT) | 3h | 발표자료.pptx |
| 8.4 | 시연 시나리오 작성 및 리허설 | 2h | 시연 스크립트 |

---

### 5.3 마일스톤 요약

| 마일스톤 | 기준 작업 | 누적 공수 | 기간 |
|---------|----------|:---------:|:----:|
| **M1** — 설계 완료 | 2.0 종료 | 2.5일 | Day 3 |
| **M2** — 패턴 4종 구현 완료 | 5.0 종료 | 7일 | Day 7 |
| **M3** — MVP 동작 가능 (통합 완료) | 6.0 종료 | 9일 | Day 9 |
| **M4** — 테스트 완료 | 7.0 종료 | 11일 | Day 11 |
| **M5** — 최종 발표 준비 완료 | 8.0 종료 | 13일 | Day 13 |
| **여유 버퍼** | - | 1일 | Day 14 |

### 5.4 크리티컬 패스 (Critical Path)

```
1.0 → 2.0 → 4.0 → 5.2 (Command) → 5.3 (State) → 6.1 → 7.0 → 8.0
```