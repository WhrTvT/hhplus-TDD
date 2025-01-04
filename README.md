# 동시성 제어 방식에 대한 분석 및 보고서

**1. 개요**
- 다수의 프로세스나 스레드가 동시에 동일한 자원에 접근할 때 발생할 수 있는 충돌 또는 데이터 일관성 문제를 방지하기 위한 기법이다.
- 다수의 스레드가 한 자원(변수, 데이터베이스, 파일 등)에 동시 접근하면 예상치 못한 결과가 발생할 수 있다.
- 현 프로젝트는 DB를 사용하지 않기 때문에 DB 동시성 테스트 대신 스레드 병렬 처리를 통해 동시성 발생 환경을 만들고 테스트를 진행한다.
---
**2. ReentrantLock을 활용한 동시성 제어**
- Spring에서 동시성 제어를 할 때에는 ReentrantLock를 사용한다.
- ReentrantLock은 동일한 스레드가 계속해서 같은 lock에 접근할 수 있는 기능을 제공한다.
- 기본적으로 공정 모드(먼저 lock을 요청한 스레드가 우선적으로 lock을 획득하도록 보장)로 설정된다.

**2-2. 예시**
```
private final ReentrantLock lock = new ReentrantLock(); // ReentrantLock
lock.lock(); // 잠금 설정
try {
  // 동시성 제어가 필요한 코드
  return ...;
} finally {
  lock.unlock(); // 잠금 해제
}
```
---
**3. executerService를 이용한 동시성 통합 테스트**
- executerService는 스레드 풀을 관리하고, 작업을 비동기적으로 실행할 수 있도록 지원합니다.
- 동시성 테스트에서 다수의 스레드를 사용하여 동시에 메서드를 호출하는 환경을 시뮬레이션할 수 있습니다.

**3-2. 예시**
```
/**
  * ExecutorService
  * newFixedThreadPool : 일정 수의 스레드 생성
  * threadCount : 생성할 스레드 수
  **/
ExecutorService executor = Executors.newFixedThreadPool(threadCount); // ExecutorService
CountDownLatch latch = new CountDownLatch(threadCount); // 모든 스레드의 작업 완료 확인하기 위해 사용

IntStream.range(0, threadCount).forEach(i -> { // 스레드만큼 반복
  executor.submit(() -> { // 비동기로 작업 진행
    try {
      // 동시성 제어를 테스트할 코드
    } finally {
      latch.countDown(); // 각 스레드가 작업을 마칠 때마다 countDown() 호출
    }
  });

  executor.submit(() -> { // 비동기로 작업 진행
    try {
      // 동시성 제어를 테스트할 코드
    } finally {
      latch.countDown(); // 각 스레드가 작업을 마칠 때마다 countDown() 호출
    }
  })
});

latch.await(); // 모든 작업이 완료될 때까지 대기
executor.shutdown(); // executor 종료
```
---
**4. 마무리**
- 동시성 문제는 다중 스레드 환경에서 자원 접근 충돌로 인해 발생합니다.
- 이에, ReentrantLock을 활용한 동기화로 데이터 무결성을 유지하고 ExecutorService를 사용해서 동시성 테스트를 통해 안정성을 검증할 수 있습니다.