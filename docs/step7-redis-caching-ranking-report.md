# STEP7 Redis 캐싱/랭킹 구현 보고서

## 1) 구현 범위
- 필수: 콘서트 예약 시나리오의 Redis 기반 인기(빠른 매진) 랭킹
- 선택: 결제 완료 -> 랭킹 반영을 Redis Pub/Sub 비동기 처리로 분리
- 추가: 콘서트 조회 API Redis 캐싱 적용으로 DB I/O 감소

## 2) 캐싱 전략
- 대상 API
  - `GET /api/v1/concerts/{concertId}/performances`
  - `GET /api/v1/concerts/performances/{performanceId}/seats`
- 적용 방식
  - `@Cacheable` 조회 캐시
  - 좌석 상태 변경 시(`hold/pay`) `@CacheEvict`로 캐시 무효화
- TTL
  - `concert:performances`: 10분
  - `concert:seats`: 30초

## 3) 랭킹 설계 (Sorted Set)
- 키
  - 일간: `ranking:concert:daily:yyyyMMdd`
  - 주간: `ranking:concert:weekly:yyyy-Www`
- 멤버: `performanceId`
- 점수: 결제 성공 건수 누적 (`ZINCRBY +1`)
- TTL
  - 일간 2일
  - 주간 14일

## 4) 비동기 설계 (선택)
- 결제 유스케이스는 랭킹 점수 직접 갱신 대신 이벤트 발행
- Redis Pub/Sub 채널: `ranking:events:payment`
- Subscriber가 메시지를 소비하여 Sorted Set 점수 갱신
- 효과
  - 결제 트랜잭션 경로에서 랭킹 연산 분리
  - 랭킹 장애가 결제 핵심 경로에 미치는 영향 축소

## 5) 운영 관점 고려사항
- TTL 강제 설정으로 만료 누락 방지
- 키 네이밍 일관화(`prefix:domain:window`)
- 랭킹 메모리 증가 모니터링 필요(키 개수/카디널리티)
- 다중 인스턴스 환경에서 Redis를 외부 공용 캐시로 사용해 캐시 일관성 확보
