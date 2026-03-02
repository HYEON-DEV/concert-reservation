# STEP9 Kafka 기초 학습 및 적용 보고서

## 1) 학습 정리

### Kafka를 쓰는 이유
- Producer/Consumer 구조로 서비스 간 비동기 메시지 전달 가능
- Topic/Partition 기반으로 확장성과 순서 보장을 함께 설계 가능
- Consumer Group으로 동일 이벤트를 여러 서비스가 독립적으로 소비 가능
- Replication, Offset Commit, DLQ 같은 운영 전략을 적용하기 좋음

### 이번 과제에서 중요했던 포인트
- 결제 완료 후 데이터 플랫폼 전송은 핵심 결제 트랜잭션과 분리되어야 함
- Kafka는 부가 로직을 서비스 밖 비동기 메시지 처리로 넘기기에 적합함
- 메시지 키를 `performanceId`로 두어 동일 공연 단위 메시지 순서를 최대한 유지함
- Consumer는 manual ack로 처리 완료 후 offset commit 하도록 구성함

## 2) 로컬 실행
- `docker-compose up -d kafka mysql`
- Kafka broker: `localhost:9092`
- 자동 생성/명시 생성 토픽
  - `concert.reservation.completed.v1`

## 3) 코드 적용 내용

### Producer
- 결제 성공 후 `ReservationCompletedEvent` 발행
- `@TransactionalEventListener(AFTER_COMMIT)` 에서 Kafka publish
- 파일
  - `src/main/java/kr/hhplus/be/server/concert/application/ReservationCompletedEventListener.java`
  - `src/main/java/kr/hhplus/be/server/concert/infrastructure/KafkaReservationMessageProducer.java`

### Message
- `ReservationCompletedMessage`를 Kafka payload로 사용
- 현재는 필수 정보 포함 payload를 직접 전송
- 파일
  - `src/main/java/kr/hhplus/be/server/concert/application/event/ReservationCompletedMessage.java`

### Consumer
- Kafka consumer가 메시지를 받아 mock 데이터 플랫폼 전송 수행
- 이후 기존 랭킹 후속 처리도 같이 수행
- manual ack 적용
- 파일
  - `src/main/java/kr/hhplus/be/server/concert/infrastructure/KafkaReservationCompletedConsumer.java`

## 4) 트레이드오프
- 현재는 Full Payload 방식이라 소비 측 DB 재조회가 줄어듦
- 반면 스키마 변경 영향이 커질 수 있어, 이후에는 Zero Payload(event id + reservation id) 방식도 고려 가능
- 현재는 단일 consumer group 기준이며, DLQ/재처리/멱등성 저장소는 다음 단계에서 확장 가능
