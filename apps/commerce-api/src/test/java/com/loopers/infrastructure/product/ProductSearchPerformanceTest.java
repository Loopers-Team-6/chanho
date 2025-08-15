package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductEntity;
import com.loopers.infrastructure.PerformanceTestHelper;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@DisplayName("상품 조회 성능 테스트")
public class ProductSearchPerformanceTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestDataSetupService testDataSetupService;

    @Autowired
    private PerformanceTestHelper performanceTestHelper;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private static final int PRODUCT_COUNT = 100_000;
    private static final int BRAND_COUNT = 100;
    private static final int USER_COUNT = 1_000;

    @BeforeAll
    void setup() {
        testDataSetupService.setupData(PRODUCT_COUNT, BRAND_COUNT, USER_COUNT);
    }

    @AfterAll
    void cleanup() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("브랜드 필터 및 가격순 정렬 시")
    class BrandIdFilterAndPriceSortTest {
        private final List<Long> brandIdsToSearch = List.of(1L, 2L, 3L);

        @Test
        @DisplayName("인덱스 유무에 따른 성능을 비교한다")
        void compareProductSearchPerformance() {
            // 1. 인덱스 없음
            measureBrandIdFilterAndPriceSort("1. 인덱스 없음");

            // 2. brand_id 단일 인덱스
            performanceTestHelper.executeNativeQueryWithTransaction("CREATE INDEX idx_brand_id ON products (brand_id)");
            performanceTestHelper.analizeTable("products");
            measureBrandIdFilterAndPriceSort("2. 브랜드 ID 단일 인덱스");
            performanceTestHelper.executeNativeQueryWithTransaction("DROP INDEX idx_brand_id ON products");

            // 3. brand_id, price 복합 인덱스
            performanceTestHelper.executeNativeQueryWithTransaction("CREATE INDEX idx_brand_id_price ON products (brand_id, price)");
            performanceTestHelper.analizeTable("products");
            measureBrandIdFilterAndPriceSort("3. 브랜드 ID + 가격 복합 인덱스");
            performanceTestHelper.executeNativeQueryWithTransaction("DROP INDEX idx_brand_id_price ON products");

            // 4. price, brand_id 복합 인덱스
            performanceTestHelper.executeNativeQueryWithTransaction("CREATE INDEX idx_price_brand_id ON products (price, brand_id)");
            performanceTestHelper.analizeTable("products");
            measureBrandIdFilterAndPriceSort("4. 가격 + 브랜드 ID 복합 인덱스");
            performanceTestHelper.executeNativeQueryWithTransaction("DROP INDEX idx_price_brand_id ON products");
        }

        private void measureBrandIdFilterAndPriceSort(String caseName) {
            // 실행 계획 확인
            String ids = brandIdsToSearch.stream().map(String::valueOf).collect(Collectors.joining(","));
            String explainQuery = "EXPLAIN SELECT * FROM products p WHERE p.brand_id IN (" + ids + ") ORDER BY p.price ASC LIMIT 20";
            performanceTestHelper.printExplainResult(caseName, explainQuery);

            Runnable findProducts = () -> {
                entityManager.createQuery("SELECT p FROM ProductEntity p WHERE p.brand.id IN :brandIds ORDER BY p.price ASC", ProductEntity.class)
                        .setParameter("brandIds", brandIdsToSearch)
                        .setMaxResults(20)
                        .getResultList();
            };

            performanceTestHelper.recordExecutionTime(caseName, findProducts, 10, 10);
        }
    }

    @Nested
    @DisplayName("좋아요 개수 순 정렬 시")
    class LikeCountSortPerformanceTest {
        @Test
        @DisplayName("정규화 vs 비정규화 방식의 성능을 비교한다")
        void compareLikeCountSortPerformance() {
            // 1. 정규화 방식 (JOIN + COUNT)
            measureLikeCountPerformance("1. 정규화 (JOIN)", false);

            // 2. like_count 컬럼 추가 후 비정규화 방식
            performanceTestHelper.executeNativeQueryWithTransaction("UPDATE products p SET like_count = (SELECT COUNT(*) FROM likes l WHERE l.product_id = p.id)");
            performanceTestHelper.executeNativeQueryWithTransaction("CREATE INDEX idx_like_count ON products (like_count DESC)");
            performanceTestHelper.analizeTable("products");
            measureLikeCountPerformance("2. 비정규화 (products.like_count 컬럼)", true);
            performanceTestHelper.executeNativeQueryWithTransaction("DROP INDEX idx_like_count ON products");
            performanceTestHelper.executeNativeQueryWithTransaction("UPDATE products SET like_count = 0");
        }

        private void measureLikeCountPerformance(String caseName, boolean useLikeCountColumn) {
            System.out.println("--- [" + caseName + "] 성능 측정을 시작합니다. ---");

            // 실행 계획 확인
            String explainQuery = useLikeCountColumn
                    ? "EXPLAIN SELECT * FROM products ORDER BY like_count DESC LIMIT 20"
                    : "EXPLAIN SELECT p.* FROM products p JOIN likes l ON p.id = l.product_id GROUP BY p.id ORDER BY COUNT(l.id) DESC LIMIT 20";
            performanceTestHelper.printExplainResult(caseName, explainQuery);

            Runnable task = () -> {
                if (useLikeCountColumn) {
                    entityManager.createQuery("SELECT p FROM ProductEntity p ORDER BY p.likeCount DESC", ProductEntity.class)
                            .setMaxResults(20)
                            .getResultList();
                } else {
                    entityManager.createQuery("SELECT p FROM ProductEntity p JOIN LikeEntity l ON p.id = l.product.id GROUP BY p.id ORDER BY COUNT(l.id) DESC", ProductEntity.class)
                            .setMaxResults(20)
                            .getResultList();
                }
            };

            performanceTestHelper.recordExecutionTime(caseName, task, 10, 10);
        }
    }
}
