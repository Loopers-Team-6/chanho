package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "points")
@NoArgsConstructor
@Getter
public class PointEntity extends BaseEntity {

    @Column(nullable = false, name = "amount", precision = 10, scale = 0)
    private BigDecimal amount = BigDecimal.ZERO;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private UserEntity user;

    public PointEntity(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("사용자 정보가 필요합니다.");
        }
        this.user = user;
    }

    public void charge(BigDecimal amountToCharge) {
        if (amountToCharge.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("충전할 금액은 0보다 커야 합니다.");
        }
        this.amount = this.amount.add(amountToCharge);
    }

    public void use(BigDecimal amountToUse) {
        if (amountToUse.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("사용할 금액은 0보다 커야 합니다.");
        }
        if (this.amount.compareTo(amountToUse) < 0) {
            throw new IllegalStateException("포인트가 부족합니다. 현재 포인트: %s, 차감하려는 포인트: %s".formatted(this.amount, amountToUse));
        }
        this.amount = this.amount.subtract(amountToUse);
    }
}
