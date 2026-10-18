## 이 프로젝트는 Spring 스케줄러와 Spring Batch의 동작 방식, 멀티 인스턴스 환경에서의 도입 방법을 학습하기 위해 만들어진 토이 프로젝트입니다.


## 스케줄러 & 배치 학습 프로젝트

### 기술 스택

#### Language
![Java](https://img.shields.io/badge/Java%2021-007396?style=for-the-badge&logo=openjdk&logoColor=white)

#### Framework & Library
![Spring Boot](https://img.shields.io/badge/SpringBoot%204.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Batch](https://img.shields.io/badge/Spring%20Batch%206-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![ShedLock](https://img.shields.io/badge/ShedLock%207.10-555555?style=for-the-badge)

#### Infra
![PostgreSQL](https://img.shields.io/badge/PostgreSQL%2018-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis%208.2-DC382D?style=for-the-badge&logo=redis&logoColor=white)


### 실행 방법
```bash
docker compose up -d   # PostgreSQL, Redis 실행
```
- PostgreSQL: 배치 메타데이터(`BATCH_*` 테이블) 저장
- Redis: 스케줄러 분산 락(ShedLock) 저장


---

### 학습 단계
> 단계별 실행 방법과 결과는 각 클래스 주석에 정리

#### 스케줄러 (`scheduler`)
| 단계 | 내용 | 코드 |
|---|---|---|
| 1 | `@Scheduled` 기본 (fixedRate / fixedDelay / cron) | `BasicJob` |
| 2 | 스케줄러 스레드 풀과 작업 간 블로킹 확인 | `ThreadPoolJob` |
| 3 | 타임존(`zone`)과 cron 표현식 | `ZoneJob` |
| 4 | 인스턴스 2개 실행 → 중복 실행 문제 재현 | `DuplicateJob` |
| 5 | 분산 락으로 중복 실행 방지 | `LockJob` |

#### 배치 (`batch`)
| 단계 | 내용 | 코드 |
|---|---|---|
| 1 | DB 연결 + 배치 메타데이터 + Tasklet Job | `TaskletJobConfig` |
| 2 | JobParameters와 JobInstance | `ParameterJobConfig` |
| 3 | Chunk 처리 (Reader / Processor / Writer) | `ChunkJobConfig` |
| 4 | 실패와 재시작 | `RestartJobConfig` |
| 5 | Skip / Retry | `SkipRetryJobConfig` |

#### 스케줄러 + 배치 (`schedulerbatch`)
| 단계 | 내용 | 코드 |
|---|---|---|
| 1 | 스케줄러로 배치 Job 실행 | `BatchLaunchScheduler` |
| 2 | 멀티 인스턴스에서 한 번만 실행 (분산 락 + JobInstance 중복 방지) | `MultiInstanceBatchScheduler` |


---

# 스케줄러(Scheduler)

## 개념

### 스케줄러
- 정해진 시각이나 주기에 메서드를 자동으로 실행하는 기능
- ex) 매일 새벽 4시에 탈퇴 회원 데이터 정리, 10분마다 미발송 메일 재전송

### TaskScheduler
- Spring이 예약 작업을 실행하는 데 쓰는 실행기
- `@EnableScheduling`을 붙이면 Spring Boot가 자동으로 만들어 줌
- 기본 스레드는 **1개** → 서로 다른 작업끼리도 앞 작업이 끝날 때까지 대기
  - 동시에 돌아야 하는 작업이 있으면 `spring.task.scheduling.pool.size`를 늘림

### 타임존
- cron은 `zone` 미지정 시 **JVM 기본 타임존** 기준으로 해석됨
  - 로컬(KST)에서 잘 돌던 cron이 UTC 서버/컨테이너에서는 9시간 어긋남
- cron에는 항상 `zone`을 명시 (ex. `zone = "Asia/Seoul"`)

### 멀티 인스턴스 중복 실행
- 스케줄러는 인스턴스(JVM)마다 따로 동작 → 인스턴스가 N대면 같은 작업이 N번 실행됨
- 멱등하지 않은 작업(메일 발송, 금액 지급 등)은 결과가 틀어짐
  - 멱등(idempotent): 여러 번 실행해도 결과가 같은 성질

### 분산 락(Distributed Lock)
- 인스턴스들이 공유하는 저장소(Redis, DB)에 "실행 중" 표시를 남겨 한 대만 실행하게 하는 방법
- 락을 못 잡은 인스턴스는 기다리지 않고 **이번 실행을 건너뜀**
- ShedLock은 락만 제공 (재시도, 놓친 실행 보충, 작업 분산은 하지 않음)


## 코드 용어
- `@EnableScheduling`: 스케줄링 기능을 켜는 설정 어노테이션
- `@Scheduled`: 이 메서드를 예약 실행하라는 표시 (반환값 `void`, 파라미터 없음)
  - `fixedRate`: 이전 실행의 **시작** 시각 기준으로 일정 간격마다 실행
  - `fixedDelay`: 이전 실행이 **끝난** 시각 기준으로 일정 시간 뒤 실행
  - `cron`: 달력 기준 특정 시각에 실행
  - `zone`: cron을 해석할 타임존
- `@EnableSchedulerLock`: ShedLock 기능을 켜는 설정 어노테이션 (없으면 `@SchedulerLock`이 무시됨)
- `@SchedulerLock`: 실행 전에 분산 락을 잡으라는 표시
  - `name`: 락 이름. 같은 name끼리는 동시에 하나만 실행
  - `lockAtMostFor`: 실행 서버가 죽어도 이 시간 뒤 락 해제 (안전장치)
  - `lockAtLeastFor`: 작업이 빨리 끝나도 이 시간까지 락 유지 (서버 간 시계 오차 대비)


## cron 표현식

Spring cron은 리눅스 crontab(5필드)과 달리 **초부터 시작하는 6필드**이다.

```
┌─ 초 (0-59)
│ ┌─ 분 (0-59)
│ │ ┌─ 시 (0-23)
│ │ │ ┌─ 일 (1-31)
│ │ │ │ ┌─ 월 (1-12 또는 JAN-DEC)
│ │ │ │ │ ┌─ 요일 (0-7 또는 MON-SUN, 0과 7은 일요일)
│ │ │ │ │ │
* * * * * *
```

| 표현식 | 의미 |
|---|---|
| `0 0 4 * * *` | 매일 04:00:00 |
| `0 */10 * * * *` | 10분마다 |
| `0 0 9 * * MON-FRI` | 평일 09:00 |
| `0 0 0 1 * *` | 매월 1일 00:00 |


## 락 시간 설정 기준
- `작업 시간 < lockAtMostFor < 실행 주기`
  - lockAtMostFor가 작업 시간보다 짧으면 작업 도중 락이 풀려 다른 인스턴스가 중복 실행
- `lockAtLeastFor <= lockAtMostFor`


---

# 배치(Spring Batch)

## 개념

### 배치
- 대량의 데이터를 사람의 개입 없이 일괄로 처리하는 작업
- ex) 매일 밤 정산, 오래된 데이터 정리, 대량 상태 변경, 파일 → DB 적재
- 스케줄러는 **언제** 실행할지, 배치는 대량 데이터를 **어떻게** 처리할지 담당 → 보통 스케줄러가 배치 Job을 실행

### Job / Step
- Job: 배치 작업 하나 (ex. 정산 배치)
- Step: Job을 이루는 단계. 하나의 Job은 하나 이상의 Step을 순서대로 실행
- Step의 처리 방식
  - Tasklet: 한 번 실행하고 끝나는 단순 작업
  - Chunk: 데이터를 N개씩 읽기 → 가공 → 쓰기를 반복하는 대량 처리

### JobInstance / JobExecution
- JobInstance: "어떤 작업"인지 구분하는 단위 = Job + 식별 파라미터
  - ex) 정산 Job + date=10월 13일 → "10월 13일자 정산"
- JobExecution: 그 작업을 실행한 시도 1회
- 배치는 이 이력을 DB에 남겨, 끝난 작업은 다시 실행하지 않고 실패한 작업은 멈춘 지점부터 이어서 처리함

### JobParameters
- Job 실행 시 넘기는 값
- 식별 파라미터: JobInstance를 구분하는 기준 (기본값)
- 비식별 파라미터: 구분에는 쓰지 않고 기록만 함 (ex. 요청 시각, 요청자)

### 청크와 트랜잭션
- 청크 하나 = 트랜잭션 하나 → 청크 단위로 커밋
- 실패하면 그 청크만 롤백되고, 앞에서 커밋된 청크는 유지
- 청크 크기가 크면 커밋 횟수는 줄지만, 실패 시 롤백되는 범위가 커짐

### Skip / Retry
- 오류가 나도 Step 전체를 실패시키지 않고 처리하는 방법
- Skip: 다시 해도 실패하는 오류(데이터 오류)는 그 건만 건너뜀
- Retry: 다시 하면 성공할 수 있는 오류(네트워크, 락 경합)는 같은 건을 다시 시도
- 어떤 오류가 어느 쪽인지는 프레임워크가 판단하지 않음 → 예외 타입으로 직접 분류


## 코드 용어
- `Job`: 배치 작업 전체. 하나 이상의 Step으로 구성
- `Step`: Job 안의 독립적인 단계
- `Tasklet`: Step을 메서드 하나 실행으로 처리하는 방식
  - `RepeatStatus.FINISHED`: 작업 종료 (`CONTINUABLE`이면 다시 호출)
- `Chunk`: Step을 N개씩 읽기 → 가공 → 쓰기 반복으로 처리하는 방식
  - `ItemReader`: 하나씩 읽음 (`null` 반환 시 종료)
  - `ItemProcessor`: 하나씩 가공 (`null` 반환 시 쓰기에서 제외)
  - `ItemWriter`: 청크 단위로 한 번에 씀
- `JdbcCursorItemReader`: DB 커서로 한 건씩 읽는 Reader
- `JdbcBatchItemWriter`: 청크를 JDBC batch update로 한 번에 쓰는 Writer
- `faultTolerant()`: Skip/Retry 기능을 켬
  - `skip` / `skipLimit`: 건너뛸 예외 / 최대 건너뛸 건수
  - `retry` / `retryLimit`: 재시도할 예외 / 최대 재시도 횟수
- `JobParameters` / `JobParametersBuilder`: Job 실행 시 넘기는 값 / 코드에서 생성
- `JobExecution` / `StepExecution`: Job / Step의 실행 시도 1회
- `ExecutionContext`: 재시작에 필요한 상태(Reader 위치 등)를 저장하는 공간
- `JobRepository`: 실행 이력 저장소
- `JobOperator`: Job 실행/중지/재시작 (Batch 6부터 `JobLauncher` 대체)


## 메타데이터 테이블
| 테이블 | 내용 |
|---|---|
| `BATCH_JOB_INSTANCE` | Job 이름 + 식별 파라미터 해시(`job_key`) |
| `BATCH_JOB_EXECUTION` | Job 실행 시도별 상태, 시작/종료 시각 |
| `BATCH_JOB_EXECUTION_PARAMS` | 실행 시 넘긴 파라미터 |
| `BATCH_STEP_EXECUTION` | Step 실행별 상태와 처리 건수 (읽기/제외/쓰기/건너뜀/커밋/롤백) |
| `BATCH_JOB_EXECUTION_CONTEXT` / `BATCH_STEP_EXECUTION_CONTEXT` | 재시작용 상태 |


---

## 참고 자료

### 스케줄러
- [Spring Framework - Task Execution and Scheduling](https://docs.spring.io/spring-framework/reference/integration/scheduling.html)
- [Spring Boot - Task Execution and Scheduling](https://docs.spring.io/spring-boot/reference/features/task-execution-and-scheduling.html)
- [Spring Guide - Scheduling Tasks](https://spring.io/guides/gs/scheduling-tasks)
- [ShedLock GitHub](https://github.com/lukas-krecan/ShedLock)

### 배치
- [Spring Batch Reference](https://docs.spring.io/spring-batch/reference/)
- [Spring Batch 6.0 Migration Guide](https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-6.0-Migration-Guide)
- [Spring Boot - Spring Batch](https://docs.spring.io/spring-boot/reference/io/spring-batch.html)