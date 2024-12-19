package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
@RequiredArgsConstructor
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);
    private final PointService pointService;

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    public UserPoint point(
            @PathVariable long id
    ) {
        UserPoint userPoint = pointService.getUserPoint(id); // real data
        log.info("User {}'s point {}", id, userPoint.point()); // log print

//        return new UserPoint(0, 0, 0);
        return userPoint;
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable long id
    ) {
        List<PointHistory> list = pointService.getUserHistory(id);
        log.info("User {}'s history {}", id, list);

//        return List.of();
        return list;
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        log.info("Charging User {}'s point {}", id, amount);
        UserPoint userPoint = pointService.setUserPointCharge(id, amount);

//        return new UserPoint(0, 0, 0);
        return userPoint;
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        log.info("Using User {}'s point {}", id, amount);
        UserPoint userPoint = pointService.setUserPointUse(id, amount);

//        return new UserPoint(0, 0, 0);
        return userPoint;
    }
}
