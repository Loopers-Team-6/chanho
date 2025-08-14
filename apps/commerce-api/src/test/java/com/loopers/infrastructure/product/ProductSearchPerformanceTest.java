package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductEntity;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class ProductSearchPerformanceTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TestDataSetupService testDataSetupService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private static final int PRODUCT_COUNT = 1_000_000;
    private static final int BRAND_COUNT = 1000;
    private static final int USER_COUNT = 10_000;

    @BeforeAll
    void setup() {
        testDataSetupService.setupData(PRODUCT_COUNT, BRAND_COUNT, USER_COUNT);
    }

    @AfterAll
    void cleanup() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품 조회 시 브랜드 필터, 가격순 정렬 성능 비교")
    @Test
    void compareProductSearchPerformance() {
        List<Long> brandIdsToSearch = List.of(1L, 2L, 3L);

        // 인덱스 없는 상태에서 측정
        measurePerformance("1. 인덱스 없음", brandIdsToSearch);

        // 브랜드 ID 단일 인덱스 생성 및 측정
        executeNativeQuery("CREATE INDEX idx_brand_id ON products (brand_id)");
        measurePerformance("2. 브랜드 ID 단일 인덱스", brandIdsToSearch);
        executeNativeQuery("DROP INDEX idx_brand_id ON products");

        // 브랜드 ID + 가격 복합 인덱스 생성 및 측정
        executeNativeQuery("CREATE INDEX idx_brand_id_price ON products (brand_id, price)");
        measurePerformance("3. 브랜드 ID + 가격 복합 인덱스", brandIdsToSearch);
        executeNativeQuery("DROP INDEX idx_brand_id_price ON products");

        // 가격 + 브랜드 ID 복합 인덱스 생성 및 측정
        executeNativeQuery("CREATE INDEX idx_price_brand_id ON products (price, brand_id)");
        measurePerformance("4. 가격 + 브랜드 ID 복합 인덱스", brandIdsToSearch);
        executeNativeQuery("DROP INDEX idx_price_brand_id ON products");
    }

    private void measurePerformance(String caseName, List<Long> brandIds) {
        System.out.println("--- [" + caseName + "] 성능 측정을 시작합니다. ---");

        // 실행 계획 확인
//        String explainQuery = "EXPLAIN SELECT * FROM products WHERE brand_id = " + brandIds + " ORDER BY price ASC LIMIT 20";
        String ids = brandIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String explainQuery = "EXPLAIN SELECT * FROM products p WHERE p.brand_id IN (" + ids + ") ORDER BY p.price ASC LIMIT 20";
        List<Object[]> explainResult = entityManager.createNativeQuery(explainQuery, Object[].class).getResultList();
        System.out.println("--- EXPLAIN 결과 ---");
        // 헤더 (컬럼명)
        List<String> columnNames = List.of("id", "select_type", "table", "partitions", "type", "possible_keys", "key", "key_len", "ref", "rows", "filtered", "Extra");
        // 결과 출력
        for (int i = 0; i < explainResult.size(); i++) {
            Object[] row = explainResult.get(i);
            System.out.println("  [ Row " + (i + 1) + " ]");
            for (int j = 0; j < row.length; j++) {
                String header = (j < columnNames.size()) ? columnNames.get(j) : "column_" + (j + 1);
                String value = (row[j] != null) ? row[j].toString() : "NULL";
                System.out.printf("    %-15s : %s%n", header, value);
            }
        }
        System.out.println("--------------------");

        // 워밍업
        for (int i = 0; i < 10; i++) {
            findProducts(brandIds);
        }

        // 실제 측정 및 평균 계산
        long totalExecutionTime = 0;
        int iterations = 10;
        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            findProducts(brandIds);
            long endTime = System.nanoTime();
            totalExecutionTime += (endTime - startTime);
        }
        long averageTime = totalExecutionTime / iterations;
        System.out.println("[" + caseName + "] 평균 실행 시간: " + TimeUnit.NANOSECONDS.toMillis(averageTime) + " ms\n");
    }

    private void findProducts(List<Long> brandIds) {
        entityManager.createQuery("SELECT p FROM ProductEntity p WHERE p.brand.id IN :brandIds ORDER BY p.price ASC", ProductEntity.class)
                .setParameter("brandIds", brandIds)
                .setMaxResults(20)
                .getResultList();
    }

    @DisplayName("상품 조회 시 좋아요 순 정렬의 정규화 vs 비정규화 성능을 비교한다")
    @Test
    void compareLikeCountSortPerformance() {
        // 1. 정규화 방식 (JOIN + COUNT)
        measureLikeCountPerformance("1. 정규화 (JOIN)", false);

        // 2. like_count 컬럼 추가 후 비정규화 방식
        executeNativeQuery("UPDATE products p SET like_count = (SELECT COUNT(*) FROM likes l WHERE l.product_id = p.id)");
        executeNativeQuery("CREATE INDEX idx_like_count ON products (like_count DESC)");
        transactionTemplate.execute(status -> {
            entityManager.createNativeQuery("ANALYZE TABLE products").getResultList();
            return null;
        });
        measureLikeCountPerformance("2. 비정규화 (products.like_count 컬럼)", true);

        // 정리
        executeNativeQuery("DROP INDEX idx_like_count ON products");
        executeNativeQuery("ALTER TABLE products DROP COLUMN like_count");
    }

    private void measureLikeCountPerformance(String caseName, boolean useLikeCountColumn) {
        System.out.println("--- [" + caseName + "] 성능 측정을 시작합니다. ---");

        // 실행 계획 확인
        String explainQuery = useLikeCountColumn
                ? "EXPLAIN SELECT * FROM products ORDER BY like_count DESC LIMIT 20"
                : "EXPLAIN SELECT p.* FROM products p JOIN likes l ON p.id = l.product_id GROUP BY p.id ORDER BY COUNT(l.id) DESC LIMIT 20";
        List<Object[]> explainResult = entityManager.createNativeQuery(explainQuery, Object[].class).getResultList();
        System.out.println("--- EXPLAIN 결과 ---");
        // 헤더 (컬럼명)
        List<String> columnNames = List.of("id", "select_type", "table", "partitions", "type", "possible_keys", "key", "key_len", "ref", "rows", "filtered", "Extra");
        // 결과 출력
        for (int i = 0; i < explainResult.size(); i++) {
            Object[] row = explainResult.get(i);
            System.out.println("  [ Row " + (i + 1) + " ]");
            for (int j = 0; j < row.length; j++) {
                String header = (j < columnNames.size()) ? columnNames.get(j) : "column_" + (j + 1);
                String value = (row[j] != null) ? row[j].toString() : "NULL";
                System.out.printf("    %-15s : %s%n", header, value);
            }
        }
        System.out.println("--------------------");

        // 워밍업
        for (int i = 0; i < 10; i++) {
            if (useLikeCountColumn) {
                entityManager.createQuery("SELECT p FROM ProductEntity p ORDER BY p.likeCount DESC", ProductEntity.class)
                        .setMaxResults(20)
                        .getResultList();
            } else {
                entityManager.createQuery("SELECT p FROM ProductEntity p JOIN LikeEntity l ON p.id = l.product.id GROUP BY p.id ORDER BY COUNT(l.id) DESC", ProductEntity.class)
                        .setMaxResults(20)
                        .getResultList();
            }
        }

        // 실제 측정 및 평균 계산
        long totalExecutionTime = 0;
        int iterations = 10;
        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            if (useLikeCountColumn) {
                entityManager.createQuery("SELECT p FROM ProductEntity p ORDER BY p.likeCount DESC", ProductEntity.class)
                        .setMaxResults(20)
                        .getResultList();
            } else {
                entityManager.createQuery("SELECT p FROM ProductEntity p JOIN LikeEntity l ON p.id = l.product.id GROUP BY p.id ORDER BY COUNT(l.id) DESC", ProductEntity.class)
                        .setMaxResults(20)
                        .getResultList();
            }
            long endTime = System.nanoTime();
            totalExecutionTime += (endTime - startTime);
        }
        long averageTime = totalExecutionTime / iterations;
        System.out.println("[" + caseName + "] 평균 실행 시간: " + TimeUnit.NANOSECONDS.toMillis(averageTime) + " ms\n");
    }

    private void executeNativeQuery(String sql) {
        transactionTemplate.execute(status -> {
            entityManager.createNativeQuery(sql).executeUpdate();
            return null;
        });
    }
}
