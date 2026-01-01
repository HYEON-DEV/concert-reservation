
# 콘서트 예매 시스템 설계 문서 (STEP-1)

본 과제는 “콘서트 오픈 시점 트래픽 집중” 상황에서 **대기열/좌석 중복 방지/결제**까지 고려한 서버 설계 문서를 작성하는 것을 목표로 합니다.  
이번 단계에서는 **구현 없이 설계 문서만 제출**합니다.

---

## 문서 링크

- [API Spec (OpenAPI 3.0)](/docs/openapi.yml)
- [ERD (DBML)](/docs/erd.dbml)
- [Infrastructure Diagram](/docs/infra.md)


---

## Scenario Summary

- 사용자는 콘서트 날짜(회차)를 조회하고 좌석(1~50)을 선택하여 예약/결제를 진행한다.
- 오픈 시점에 동시 접속이 몰릴 수 있으므로 **대기열**을 통해 유입을 제어한다.
- 좌석은 `AVAILABLE → HELD(임시배정) → CONFIRMED(결제완료)` 상태로 전이된다.
- 최종 정합성 데이터(예약/결제/좌석 상태)는 RDBMS(MySQL)에 저장한다.
- 대기열/캐시는 Redis로 처리하여 DB 부하를 보호한다.

---

## Architecture Overview

본 시스템은 콘서트 오픈 시 발생하는 순간적인 트래픽 집중과 좌석 중복 예약을 방지하기 위해,
대기열 처리와 좌석 상태 관리를 분리한 구조로 설계했습니다.

대기열 및 임시 상태 관리는 Redis를 활용하여 빠른 응답성과 확장성을 확보하고,
좌석 상태 및 결제 결과와 같은 최종 정합성이 필요한 데이터는 MySQL을 Source of Truth로 유지합니다.

---

## Components

### 1) Client
웹/앱 클라이언트. API 호출 및 대기열 토큰/인증 토큰을 헤더로 전송한다.

### 2) API Server (Spring Boot)
비즈니스 로직 처리:
- 콘서트/날짜/좌석 조회
- 좌석 HOLD(임시배정) / 예약 생성
- 결제 처리 및 상태 확정
- 만료(hold_expires_at / expires_at) 기반 정리 작업(스케줄러)

### 3) MySQL (RDBMS)
서비스의 최종 정합성 소스(Source of Truth). <br/> 
좌석 상태(AVAILABLE/HELD/CONFIRMED)와 예약/결제 이력을 저장한다. <br/>
임시배정 만료 시간(`hold_expires_at` / `expires_at`)을 저장하고, <br/>
만료 정리 작업이 가능하도록 인덱스를 둔다. <br/>
좌석 중복 예약 방지는 **트랜잭션 + 조건부 UPDATE(AVAILABLE일 때만 HELD로 변경)** 를 기본으로 하며, <br/>
필요 시 `SELECT ... FOR UPDATE` 전략을 적용할 수 있다.

### 4) Redis
- 대기열(Queue):
    - waiting: ZSET(순번/정렬)
    - active: KEY/SET(TTL로 활성 세션 관리)
- 캐시(Cache):
    - 콘서트 날짜/좌석 조회 결과를 짧은 TTL로 캐싱
    - 오픈 시점 트래픽에서 DB 부하를 낮추고 응답을 빠르게 한다.

### 5) Logging & Monitoring 
API 지표(응답시간/에러율) 및 로그(구조화 로그)를 수집하여 운영 시 장애 탐지/분석에 사용한다.

### 6) Object Storage 
이미지/정적 리소스(포스터 등)를 저장할 수 있다.

---

## Architecture Decision Records (ADR)

### ADR-001: 대기열(Queue) 구현 - Redis ZSET 선택

**문제(Context)**  
오픈 시점에 동시 요청이 폭증하며, 공정한 순서 보장과 빠른 순번 조회가 필요합니다.

**대안(Options)**
- Redis ZSET 기반 대기열
- 메시지 큐(Kafka/SQS 등) 기반 대기열
- RDB 테이블 기반 대기열

**결정(Decision)**  
Redis ZSET으로 대기열을 구현합니다.

**이유(Rationale)**  
ZSET은 score 기반 자동 정렬로 순번 조회(ZRANK), 상위 N명 통과(ZRANGE)가 단순하며,
메모리 기반으로 빠르게 처리되어 DB 부하를 보호합니다. 과제 범위에서 메시지 큐는 운영/구현 복잡도가 커
우선순위를 낮췄습니다.

**위험/대응(Risks & Mitigation)**  
Redis 장애/유실 위험이 있으므로 대기열 토큰은 TTL 기반으로 운영하고 재발급/재진입 정책을 둡니다.
향후 규모 확장 시 메시지 큐 도입을 검토할 수 있도록 대기열 컴포넌트를 분리합니다.

---

### ADR-002: 좌석 예약 동시성 제어 - MySQL 트랜잭션 + 조건부 UPDATE

**문제(Context)**  
동일 좌석에 대한 동시 예약 요청이 발생할 수 있어 중복 예약을 방지해야 합니다.

**대안(Options)**
- 조건부 UPDATE(AVAILABLE일 때만 HELD로 변경)
- SELECT ... FOR UPDATE 기반 비관적 락
- 애플리케이션 레벨 락

**결정(Decision)**  
기본 전략은 조건부 UPDATE로 하고, 필요 시 SELECT ... FOR UPDATE로 확장합니다.

**이유(Rationale)**  
조건부 UPDATE는 락 범위를 최소화하고 DB 레벨에서 원자성을 보장합니다.
충돌 시 업데이트 건수가 0으로 반환되어 재시도/실패 처리가 명확합니다.

**위험/대응(Risks & Mitigation)**  
충돌이 잦은 구간에서는 재시도 정책(예: 2~3회, 짧은 backoff)을 적용합니다.
고부하 상황에 한해 SELECT ... FOR UPDATE 전략을 선택적으로 적용할 수 있습니다.

