# bulgumtong API 명세서

> **Base URL:** `http://localhost:8080`
> **Content-Type:** `application/json; charset=UTF-8`
> **인증 방식:** 모든 요청에 `requesterId` (멤버 ID) 쿼리 파라미터 포함

---

## 공통

### 권한 레벨

| 역할 | 설명 |
|------|------|
| LEADER | 스터디장 — 모든 기능 접근 가능 |
| MEMBER | 일반 멤버 — 본인 출결 체크 및 조회만 가능 |

### 공통 에러 응답

| HTTP 코드 | 발생 조건 |
|-----------|-----------|
| 400 | 필수 파라미터 누락, 잘못된 날짜 형식, 잘못된 상태값 |
| 403 | 권한 없는 요청 (UnauthorizedException) |

```json
{ "error": "에러 메시지" }
```

---

## 1. 멤버 (Members)

### 1.1 멤버 목록 조회

```
GET /api/members
```

**권한:** 전체 공개

**응답 예시 (200)**
```json
[
  { "id": "m-001", "name": "김민준", "role": "스터디장" },
  { "id": "m-002", "name": "이수연", "role": "일반 멤버" }
]
```

---

### 1.2 멤버 추가

```
POST /api/members
```

**권한:** LEADER만 가능

**쿼리 파라미터**

| 파라미터 | 필수 | 설명 |
|----------|:----:|------|
| requesterId | ✅ | 요청자 멤버 ID |
| name | ✅ | 새 멤버 이름 |
| role | | `LEADER` 또는 `MEMBER` (기본값: `MEMBER`) |

**요청 예시**
```
POST /api/members?requesterId=m-001&name=홍길동&role=MEMBER
```

**응답 예시 (201)**
```json
{ "id": "1f1ab98a-...", "name": "홍길동", "role": "일반 멤버" }
```

**에러 응답 (403)**
```json
{ "error": "스터디장만 접근할 수 있는 기능입니다." }
```

---

## 2. 출결 (Attendance)

### 출결 상태값

| 값 | 설명 | 벌금 |
|----|------|------|
| `PRESENT` | 출석 | 0원 |
| `LATE` | 지각 | 2,000원 |
| `ABSENT` | 결석 | 5,000원 |

### 상태 전이 규칙 (State 패턴)

```
ABSENT  →  PRESENT  (타임어택 10분 이내만 가능)
ABSENT  →  LATE
PRESENT →  ABSENT
PRESENT →  LATE
LATE    →  PRESENT
LATE    →  ABSENT
PRESENT →  PRESENT  ❌ (IllegalStateException)
ABSENT  →  ABSENT   ❌ (IllegalStateException)
LATE    →  LATE     ❌ (IllegalStateException)
```

> **타임어택:** 세션 시작 시각으로부터 **10분 이내**에만 PRESENT 처리 가능.
> 초과 시 400 에러 반환.

---

### 2.1 출결 기록 조회

```
GET /api/attendance
```

**권한:** LEADER — 모든 멤버 조회 가능 / MEMBER — 본인 기록만 조회 가능

**쿼리 파라미터**

| 파라미터 | 필수 | 설명 |
|----------|:----:|------|
| requesterId | ✅ | 요청자 멤버 ID |
| memberId | ✅ | 조회할 대상 멤버 ID |

**요청 예시**
```
GET /api/attendance?requesterId=m-001&memberId=m-003
```

**응답 예시 (200)** — 최신 날짜 순 정렬
```json
[
  { "memberId": "m-003", "date": "2026-05-22", "status": "출석" },
  { "memberId": "m-003", "date": "2026-05-15", "status": "결석" },
  { "memberId": "m-003", "date": "2026-05-08", "status": "결석" }
]
```

**에러 응답 (403)**
```json
{ "error": "본인 기록만 조회할 수 있습니다." }
```

---

### 2.2 출결 체크 (생성 / 변경)

```
POST /api/attendance
```

**권한:** LEADER — 모든 멤버 처리 가능 / MEMBER — 본인 출결만 체크 가능

> 해당 날짜 기록이 없으면 생성, 있으면 상태 변경 (Command 패턴으로 이력 누적)

**쿼리 파라미터**

| 파라미터 | 필수 | 설명 |
|----------|:----:|------|
| requesterId | ✅ | 요청자 멤버 ID |
| memberId | ✅ | 대상 멤버 ID |
| date | ✅ | 세션 날짜 (`YYYY-MM-DD`) |
| status | ✅ | 출결 상태 (`PRESENT` / `LATE` / `ABSENT`) |

**요청 예시**
```
POST /api/attendance?requesterId=m-001&memberId=m-003&date=2026-05-22&status=PRESENT
```

**응답 예시 (200)**
```json
{ "memberId": "m-003", "date": "2026-05-22", "status": "출석" }
```

**에러 응답 (400) — 타임어택 초과**
```json
{ "error": "출석 인정 시간이 초과되었습니다. 세션 시작 후 10분 이내에만 출석 가능합니다." }
```

**에러 응답 (403) — 타인 출결 체크 시도**
```json
{ "error": "본인 출결만 체크할 수 있습니다." }
```

---

## 3. 변경 이력 (History)

### 3.1 출결 변경 이력 조회

```
GET /api/history
```

**권한:** LEADER만 가능

> Command 패턴으로 누적된 모든 출결 변경 이력을 최신순으로 반환

**쿼리 파라미터**

| 파라미터 | 필수 | 설명 |
|----------|:----:|------|
| requesterId | ✅ | 요청자 멤버 ID (LEADER만 유효) |

**요청 예시**
```
GET /api/history?requesterId=m-001
```

**응답 예시 (200)**
```json
[
  "[m-003] 출결 변경 → 출석 (by m-001)",
  "[m-002] 출결 변경 → 지각 (by m-002)"
]
```

**에러 응답 (403)**
```json
{ "error": "스터디장만 접근할 수 있는 기능입니다." }
```

---

## 4. 벌금 리포트 (Report)

### 4.1 멤버별 벌금 정산 리포트

```
GET /api/report
```

**권한:** LEADER만 가능

> Factory 패턴으로 생성된 AbsencePenalty / LatePenalty를 통해 계산

**쿼리 파라미터**

| 파라미터 | 필수 | 설명 |
|----------|:----:|------|
| requesterId | ✅ | 요청자 멤버 ID (LEADER만 유효) |

**요청 예시**
```
GET /api/report?requesterId=m-001
```

**응답 예시 (200)**
```json
{
  "report": "=== 벌금 정산 리포트 ===\n박준혁       | 결석 2회 | 지각 0회 | 벌금 10,000원\n이수연       | 결석 0회 | 지각 1회 | 벌금 2,000원\n...\n총 벌금 합계: 19,000원\n"
}
```

**에러 응답 (403)**
```json
{ "error": "스터디장만 접근할 수 있는 기능입니다." }
```

---

## 5. 더미 데이터

서버 시작 시 자동으로 로드되는 초기 데이터입니다.

### 멤버

| ID | 이름 | 역할 |
|----|------|------|
| m-001 | 김민준 | 스터디장 |
| m-002 | 이수연 | 일반 멤버 |
| m-003 | 박준혁 | 일반 멤버 |
| m-004 | 정유나 | 일반 멤버 |
| m-005 | 최성현 | 일반 멤버 |

### 세션 이력

| 날짜 | 김민준 | 이수연 | 박준혁 | 정유나 | 최성현 |
|------|--------|--------|--------|--------|--------|
| 2주 전 (14:00) | 출석 | 출석 | 결석 | 지각 | 출석 |
| 1주 전 (14:00) | 출석 | 지각 | 결석 | 출석 | 결석 |

---

## 6. 벌금 단가 설정

| 구분 | 단가 |
|------|------|
| 결석 1회 | 5,000원 |
| 지각 1회 | 2,000원 |
| 타임어택 윈도우 | 10분 |

> `AppConfig.java`에서 상수로 관리됩니다.
