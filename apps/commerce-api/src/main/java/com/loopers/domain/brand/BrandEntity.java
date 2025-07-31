package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "brands")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandEntity extends BaseEntity {

    private String brandName;

    public BrandEntity(String brandName) {
        if (brandName == null || brandName.isBlank()) {
            throw new IllegalArgumentException("브랜드 이름은 반드시 존재해야 합니다.");
        }

        if (brandName.length() > 50) {
            throw new IllegalArgumentException("브랜드 이름은 50자 이하여야 합니다.");
        }

        this.brandName = brandName;
    }
}
