package com.loopers.domain.point;

import jakarta.persistence.EntityNotFoundException;
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
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public PointEntity findByUserId(Long id) {
        return pointRepository.findByUserId(id)
                .orElseThrow(() -> new EntityNotFoundException("사용자의 포인트 정보가 존재하지 않습니다: " + id));
    }

    @Override
    public void deductPoints(Long userId, BigDecimal points) {
        PointEntity point = findByUserIdWithPessimisticLock(userId);

        point.use(points);
    }

    @Override
    public PointEntity findByUserIdWithPessimisticLock(long userId) {
        return pointRepository.findByUserIdWithPessimisticLock(userId)
                .orElseThrow(() -> new EntityNotFoundException("포인트 정보를 찾을 수 없습니다."));
    }
}
