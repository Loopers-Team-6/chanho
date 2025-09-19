package com.loopers.config;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.metric.ProductMetricEntity;
import com.loopers.domain.metric.ProductMetricRepository;
import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserGender;
import com.loopers.domain.user.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Profile("local")
@Order(1)
@RequiredArgsConstructor
public class TestDataInitializer {

    private static final Random RANDOM = new Random();

    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final ProductMetricRepository productMetricRepository;

    @PostConstruct
    public void init() {
        initUser();
        List<ProductEntity> products = initCatalog();
        initMetrics(products);
    }

    private void initUser() {
        List<UserEntity> users = new ArrayList<>();
        List<PointEntity> points = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            users.add(UserEntity.create(
                    "name_" + i,
                    "user" + i + "@example.com",
                    UserGender.MALE,
                    LocalDate.of(1990, 1, 1).plusDays(i)
            ));
        }
        List<UserEntity> persisted = userRepository.saveAll(users);

        for (UserEntity user : persisted) {
            PointEntity point = PointEntity.create(user);
            point.charge(BigDecimal.valueOf((RANDOM.nextInt(100) + 1) * 1000)); // 1,000 ~ 100,000원 충전
            points.add(point);
        }
        pointRepository.saveAll(points);
    }

    private List<ProductEntity> initCatalog() {
        if (productRepository.count() > 1) {
            return List.of();
        }

        List<BrandEntity> brands = new ArrayList<>();

        // 1. 브랜드 10개 생성
        for (int i = 1; i <= 10; i++) {
            brands.add(BrandEntity.create("Brand " + i));
        }
        List<BrandEntity> persisted = brandRepository.saveAll(brands);

        // 2. 상품 100개 생성
        List<ProductEntity> products = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            int price = (RANDOM.nextInt(100) + 1) * 1000; // 1,000 ~ 100,000원
            int stock = RANDOM.nextInt(100);
            BrandEntity randomBrand = persisted.get(RANDOM.nextInt(brands.size()));
            products.add(ProductEntity.create("Product " + i, price, stock, randomBrand));
        }
        return productRepository.saveAll(products);
    }

    private void initMetrics(List<ProductEntity> products) {
        if (productMetricRepository.count() > 1) {
            return;
        }

        List<ProductMetricEntity> metrics = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 지난 60일간의 데이터 생성
        for (int i = 0; i < 60; i++) {
            LocalDate date = today.minusDays(i);
            for (ProductEntity product : products) {
                ProductMetricEntity metric = ProductMetricEntity.create(product.getId(), date);
                // 랜덤 값으로 view, like, order count 설정 (실제 로직처럼)
                for (int i1 = 0; i1 < RANDOM.nextInt(1000); i1++) {
                    metric.increaseViewCount();
                }
                if (RANDOM.nextBoolean()) {
                    metric.increaseLikeCount();
                }
                metric.addOrder(RANDOM.nextInt(5), product.getPrice());
                metrics.add(metric);
            }
        }
        productMetricRepository.saveAll(metrics);
    }
}
