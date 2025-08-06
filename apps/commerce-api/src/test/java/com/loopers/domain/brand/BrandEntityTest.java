package com.loopers.domain.brand;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BrandEntityTest {
    /*
     * 브랜드 단위 테스트
     * - [o]  브랜드는 이름을 가지고 있어야 하며, 50자 이내여야 한다
     */

    @DisplayName("브랜드를 생성할 때, 브랜드 이름은 반드시 존재해야 하며 50자 이내여야 한다")
    @Nested
    class CreateBrand {

        @DisplayName("브랜드 이름이 없거나 50자를 초과하면, 브랜드 생성에 실패한다")
        @NullAndEmptySource
        @ValueSource(strings = {
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", // 51 characters
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", // 100 characters
        })
        @ParameterizedTest
        void failsWhenCreateBrandEntityWithInvalidName(String invalidBrandName) {
            // arrange
            // act & assert
            assertThrows(IllegalArgumentException.class, () -> BrandEntity.create(invalidBrandName));
        }

        @DisplayName("브랜드 이름이 유효하면, 브랜드 생성에 성공한다")
        @ParameterizedTest
        @ValueSource(strings = {
                "Valid Brand",
                "Brand123",
                "Brand-Name_1",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" // 50 characters
        })
        void succeedsWhenCreateBrandEntityWithValidName(String validBrandName) {
            // arrange
            // act & assert
            BrandEntity brand = BrandEntity.create(validBrandName);
            assertThat(brand.getId()).isNotNull();
            assertThat(brand.getBrandName()).isEqualTo(validBrandName);
        }

    }
}
