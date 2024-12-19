package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final ReentrantLock lock = new ReentrantLock(); // ReentrantLock

    public UserPoint getUserPoint(
           final long id
    ) {
        UserPoint currentPoint = userPointTable.selectById(id); // 현재 포인트

        if (currentPoint.point() == 0) {
            throw new RuntimeException("사용자의 포인트 정보를 찾을 수 없습니다."); // 사용자 포인트 정보 예외처리
        }

        return currentPoint; // 사용자 현재 데이터 조회
    }

    public List<PointHistory> getUserHistory(
            final long id
    ) {
        List<PointHistory> UserHistory = pointHistoryTable.selectAllByUserId(id); // 사용자 히스토리 정보
        if (UserHistory.isEmpty()) {
            throw new RuntimeException("사용자의 정보를 찾을 수 없습니다."); // 사용자 히스토리 정보 예외처리
        }
        return UserHistory; // 사용자 히스토리 조회
    }

    public UserPoint setUserPointCharge(
            final long id,
            final long amount
    ) {
        lock.lock(); // 잠금 설정
        try {
            if(amount < 100){ // min > 100 & max < 500,000
                throw new RuntimeException("100원부터 충전이 가능합니다.");
            } else if(amount > 500000){
                throw new RuntimeException("회당 충전금액이 50만원을 넘길 수 없습니다.");
            }

        UserPoint updatedPoint = userPointTable.insertOrUpdate(id, amount); // 포인트 충전
        pointHistoryTable.insert(updatedPoint.id(), amount, TransactionType.CHARGE, System.currentTimeMillis()); // 충전된 금액 히스토리 기록

        return updatedPoint;
        } finally {
            lock.unlock(); // 잠금 해제
        }
    }

    public UserPoint setUserPointUse(
            final long id,
            final long amount
    ) {
        lock.lock(); // 잠금 설정
        try {
            UserPoint currentPoint = userPointTable.selectById(id); // 현재 포인트

            if (currentPoint.point() < amount) {
                throw new RuntimeException("잔고가 부족합니다."); // 잔고 부족 예외 처리
            }

            UserPoint updatedPoint = userPointTable.insertOrUpdate(id, currentPoint.point() - amount); // 포인트 차감
            pointHistoryTable.insert(updatedPoint.id(), -amount, TransactionType.USE, System.currentTimeMillis()); // 사용한 금액 히스토리 기록
            return updatedPoint;
        } finally {
            lock.unlock(); // 잠금 해제
        }
    }
}