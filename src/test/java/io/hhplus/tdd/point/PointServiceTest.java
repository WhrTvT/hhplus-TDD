package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointServiceTest {
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
    @DisplayName("사용자 포인트 조회 성공 시나리오")
    void testGetUserPoint_Success(){
        // Given
        long userId = 1L;
        userPointTable.insertOrUpdate(userId, 100);

        // When
        UserPoint userPoint = pointService.getUserPoint(userId);

        // Then
        assertThat(userPoint.point()).isEqualTo(100);
    }

    @Test
    @DisplayName("사용자 포인트 조회 실패 시나리오")
    void testGetUserPoint_Fail(){
        // Given
        long userId = 999L;

        // When & Then
        assertThatThrownBy(() -> pointService.getUserPoint(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("사용자의 포인트 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("사용자 히스토리 조회 성공 시나리오")
    void testGetUserHistory_Success(){
        // Given
        long userId = 1L;
        pointHistoryTable.insert(userId, 100, TransactionType.CHARGE, System.currentTimeMillis());

        // When
        List<PointHistory> histories = pointService.getUserHistory(userId);

        // Then
        Assertions.assertThat(histories.get(0).amount()).isEqualTo(100);
        Assertions.assertThat(histories.get(0).type()).isEqualTo(TransactionType.CHARGE);
    }

    @Test
    @DisplayName("사용자 히스토리 조회 실패 시나리오")
    void testGetUserHistory_Fail(){
        // Given
        long userId = 999L;

        // When & Then
        assertThatThrownBy(() -> pointService.getUserHistory(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("사용자의 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("포인트 충전 성공 시나리오")
    void testChargeUserPoint_Success(){
        // Given
        long userId = 1L;
        long amount = 100L;

        // When
        UserPoint userPoint = pointService.setUserPointCharge(userId, amount);

        // Then
        assertThat(userPoint.point()).isEqualTo(100);
    }

    @Test
    @DisplayName("포인트 충전 실패 시나리오(최소금액 미충족)")
    void testChargeUserPoint_Fail_MinimumAmount(){
        // Given
        long userId = 1L;
        long amount = 99L;

        // When & Then
        assertThatThrownBy(() -> pointService.setUserPointCharge(userId, amount))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("100원부터 충전이 가능합니다.");
    }

    @Test
    @DisplayName("포인트 충전 실패 시나리오(최대금액 미충족)")
    void testChargeUserPoint_Fail_MaximumAmount(){
        // Given
        long userId = 1L;
        long amount = 500001L;

        // When & Then
        assertThatThrownBy(() -> pointService.setUserPointCharge(userId, amount))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("회당 충전금액이 50만원을 넘길 수 없습니다.");
    }

    @Test
    @DisplayName("포인트 사용 성공 시나리오")
    void testUseUserPoint_Success(){
        // Given
        long userId = 1L;
        long initialAmount = 100L;
        long useAmount = 50L;
        userPointTable.insertOrUpdate(userId, initialAmount);

        // When
        UserPoint userPoint = pointService.setUserPointUse(userId, useAmount);

        // Then
        assertThat(userPoint.point()).isEqualTo(50);
    }

    @Test
    @DisplayName("포인트 사용 실패 시나리오")
    void testUseUserPoint_Fail(){
        // Given
        long userId = 1L;
        long initialAmount = 100L;
        long useAmount = 120L;
        userPointTable.insertOrUpdate(userId, initialAmount);

        // When & Then
        assertThatThrownBy(() -> pointService.setUserPointUse(userId, useAmount))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("잔고가 부족합니다.");
    }
}