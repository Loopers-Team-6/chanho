package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointRepository pointRepository;

    @Override
    public PointEntity save(PointEntity point) {
        try {
            return pointRepository.save(point);
        } catch (DataIntegrityViolationException e) {
            throw new CoreException(ErrorType.CONFLICT, e.getMessage());
        }
    }

    @Override
    public PointEntity findByUserId(Long id) {
        return pointRepository.findByUserId(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자의 포인트 정보가 존재하지 않습니다: " + id));
    }

    @Override
    public void deductPoints(Long userId, BigDecimal points) {
        PointEntity point = findByUserId(userId);

        point.use(points);
    }
}
