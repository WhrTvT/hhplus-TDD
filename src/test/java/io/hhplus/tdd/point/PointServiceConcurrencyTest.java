package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointServiceConcurrencyTest {
    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private PointService pointService;

    @BeforeEach
    void setUp() {
        userPointTable = new UserPointTable();
        pointHistoryTable = new PointHistoryTable();
        pointService = new PointService(userPointTable, pointHistoryTable);
    }

    @Test
    @DisplayName("ReentrantLock 적용 - 동시 포인트 충전 테스트")
    void testConcurrentPointCharge() throws InterruptedException {
        // Given
        long userId = 1L;
        userPointTable.insertOrUpdate(userId, 0L);

        int threadCount = 10;
        long chargeAmount = 100L;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When
        IntStream.range(0, threadCount).forEach(i -> executor.submit(() -> {
            try {
                UserPoint userPoint = pointService.setUserPointCharge(userId, chargeAmount);
//                System.out.println("User Point: " + userPoint.point());
            } finally {
                latch.countDown();
            }
        }));

        latch.await();
        executor.shutdown();

        // Then
        UserPoint userPoint = userPointTable.selectById(userId);
        assertThat(userPoint.point()).isEqualTo(threadCount * chargeAmount);

        List<PointHistory> histories = pointHistoryTable.selectAllByUserId(userId);
        assertThat(histories).hasSize(threadCount);
    }

    @Test
    @DisplayName("ReentrantLock 적용 - 동시 포인트 사용 테스트")
    void testConcurrentPointUse() throws InterruptedException {
        // Given
        long userId = 1L;
        long initialAmount = 1000L;
        userPointTable.insertOrUpdate(userId, initialAmount);

        int threadCount = 10;
        long useAmount = 50L;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When
        IntStream.range(0, threadCount).forEach(i -> executor.submit(() -> {
            try {
                UserPoint userPoint = pointService.setUserPointUse(userId, useAmount);
//                System.out.println("User Point: " + userPoint.point());
            } finally {
                latch.countDown();
            }
        }));

        latch.await();
        executor.shutdown();

        // Then
        UserPoint userPoint = userPointTable.selectById(userId);
        assertThat(userPoint.point()).isEqualTo(initialAmount - (threadCount * useAmount));

        List<PointHistory> histories = pointHistoryTable.selectAllByUserId(userId);
        assertThat(histories).hasSize(threadCount);
    }

    @Test
    @DisplayName("ReentrantLock 적용 - 동시 충전 및 사용 테스트")
    void testConcurrentChargeAndUse() throws InterruptedException {
        // Given
        long userId = 1L;
        long initialAmount = 1000L;
        userPointTable.insertOrUpdate(userId, initialAmount);

        int threadCount = 10;
        long chargeAmount = 100L;
        long useAmount = 50L;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount * 2);
        CountDownLatch latch = new CountDownLatch(threadCount * 2);

        // When
        IntStream.range(0, threadCount).forEach(i -> {
            executor.submit(() -> {
                try {
                    UserPoint userPoint = pointService.setUserPointCharge(userId, chargeAmount);
//                    System.out.println("User Point Charge: " + userPoint.point());
                } finally {
                    latch.countDown();
                }
            });

            executor.submit(() -> {
                try {
                    UserPoint userPoint = pointService.setUserPointUse(userId, useAmount);
//                    System.out.println("User Point Use: " + userPoint.point());
                } finally {
                    latch.countDown();
                }
            });
        });

        latch.await();
        executor.shutdown();

        // Then
        UserPoint userPoint = userPointTable.selectById(userId);
        long expectedFinalAmount = initialAmount + (threadCount * chargeAmount) - (threadCount * useAmount);
        assertThat(userPoint.point()).isEqualTo(expectedFinalAmount);

        List<PointHistory> histories = pointHistoryTable.selectAllByUserId(userId);
        assertThat(histories).hasSize(threadCount * 2);
    }

    @Test
    @DisplayName("ReentrantLock 적용 - 다수 사용자 동시 충전 및 사용 테스트")
    void testConcurrentMultipleUsers() throws InterruptedException {
        int userCount = 5;
        int threadCount = 10;
        long chargeAmount = 100L;
        long useAmount = 50L;

        ExecutorService executor = Executors.newFixedThreadPool(userCount * threadCount);
        CountDownLatch latch = new CountDownLatch(userCount * threadCount * 2);

        // When
        IntStream.range(1, userCount + 1).forEach(userId -> {
            UserPoint CreatedPoint = userPointTable.insertOrUpdate(userId, 1000L); // 각 사용자 초기 포인트 설정
//            System.out.println("User Create : " + CreatedPoint.id() + " Point : " + CreatedPoint.point());

            IntStream.range(0, threadCount).forEach(i -> {
                executor.submit(() -> {
                    try {
                        UserPoint userPoint = pointService.setUserPointCharge(userId, chargeAmount);
//                        System.out.println("User Charge : " + userPoint.id() + " Point : " + userPoint.point());
                    } finally {
                        latch.countDown();
                    }
                });

                executor.submit(() -> {
                    try {
                        UserPoint userPoint = pointService.setUserPointUse(userId, useAmount);
//                        System.out.println("User Use : " + userPoint.id() + " Point : " + userPoint.point());
                    } finally {
                        latch.countDown();
                    }
                });
            });
        });

        latch.await();
        executor.shutdown();

        // Then
        IntStream.range(1, userCount + 1).forEach(userId -> {
            UserPoint userPoint = userPointTable.selectById((long) userId);
            long expectedFinalAmount = 1000L + (threadCount * chargeAmount) - (threadCount * useAmount);
            assertThat(userPoint.point()).isEqualTo(expectedFinalAmount);

            List<PointHistory> histories = pointHistoryTable.selectAllByUserId(userId);
            assertThat(histories).hasSize(threadCount * 2);
        });
    }
}
