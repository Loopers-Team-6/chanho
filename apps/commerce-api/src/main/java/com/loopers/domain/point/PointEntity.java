package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "points")
@NoArgsConstructor
@Getter
public class PointEntity extends BaseEntity {
    @Column(nullable = false, name = "amount")
    private Long amount = 0L;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    public PointEntity(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("사용자 정보가 필요합니다.");
        }
        this.user = user;
    }

    public void charge(long points) {
        if (points <= 0) {
            throw new IllegalArgumentException("충전 포인트는 0보다 커야 합니다.");
        }

        this.amount += points;
    }
}
