# STEP5 동시성 제어 구현 보고서 (콘서트 예약 서비스)

## 1) 문제 상황

콘서트 예약 서비스에서 다음 동시성 문제가 발생할 수 있다.

- 동일 좌석 동시 예약 요청으로 중복 예약 발생.
- 동일 사용자 동시 결제로 잔액이 음수가 되는 문제.
- 임시 배정(HOLD) 만료 후에도 좌석이 해제되지 않아 재판매가 막히는 문제.

## 2) 해결 전략

### A. 좌석 임시 배정 동시성 제어 (비관적 락)

- `ConcertSeatJpaRepository.findByPerformanceIdAndSeatNoForUpdate(...)` 에서 `PESSIMISTIC_WRITE` 사용.
- 예약 유즈케이스(`ReserveSeatInteractor`)는 트랜잭션 내부에서 `SELECT ... FOR UPDATE`로 좌석 1건을 잠근 뒤,
  상태를 검증하고 HOLD를 설정한다.
- 결과: 동일 좌석 동시 요청 시 직렬화되어 1건만 성공.

### B. 잔액 차감 동시성 제어 (조건부 UPDATE)

- `UserPointJpaRepository.decreaseIfEnough(...)` 에서
  `UPDATE ... SET balance = balance - :amount WHERE user_id = :userId AND balance >= :amount` 사용.
- 영향 행 수가 0이면 잔액 부족으로 처리.
- 결과: 읽기-검증-차감 분리로 인한 Lost Update를 피하고 음수 잔액을 방지.

### C. 임시 배정 만료 해제 스케줄러

- `SeatHoldCleanupScheduler`를 추가하여 주기적으로 만료 HOLD를 정리.
- `ConcertSeatJpaRepository.releaseExpiredHolds(now)` 벌크 업데이트로 만료된 좌석을 `AVAILABLE`로 일괄 복구.
- 운영 환경에서는 `app.seat-hold.cleanup-delay-ms`로 주기 조정 가능.

## 3) 테스트

### 3-1. 좌석 동시 예약 테스트

- 파일: `ReserveSeatConcurrencyIT`
- 20개 스레드가 동일 좌석에 동시 예약 요청.
- 기대 결과: 정확히 1건만 HOLD 성공.

### 3-2. 포인트 동시 차감/충전 관련 테스트

- 파일: `PointConcurrencyIT`
- 동시 충전 시 합계 보존 검증.
- 조건부 업데이트 기반 차감 로직은 서비스/레포지토리 단위로 반영.

### 3-3. 만료 해제 스케줄러 테스트

- 파일: `SeatHoldCleanupSchedulerTest`
- 스케줄러 실행 시 만료 해제 쿼리 호출 여부 검증.

## 4) 정리

이번 구현에서는 과제 요구사항에 맞춰 다음을 충족했다.

- 2개 이상 동시성 이슈 구현/테스트: 좌석 임시 배정, 잔액 제어, 만료 해제 스케줄러.
- 조건부 UPDATE + SELECT FOR UPDATE를 함께 사용.
- 멀티스레드 테스트 포함.
- 문서화(본 보고서) 완료.
