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

    private static final int PRODUCT_COUNT = 100_000;
    private static final int BRAND_COUNT = 100;

    @BeforeAll
    void setup() {
        testDataSetupService.setupData(PRODUCT_COUNT, BRAND_COUNT);
    }

    @AfterAll
    void cleanup() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("인덱스 전략에 따른 상품 목록 조회 성능을 비교 측정한다.")
    void compareProductSearchPerformance() {
        long brandIdToSearch = 1L;

        // 인덱스 없는 상태에서 측정
        measurePerformance("인덱스 없음", brandIdToSearch);

        // 브랜드 ID 단일 인덱스 생성 및 측정
        executeNativeQuery("CREATE INDEX idx_brand_id ON products (brand_id)");
        measurePerformance("브랜드 ID 단일 인덱스", brandIdToSearch);
        executeNativeQuery("DROP INDEX idx_brand_id ON products");

        // 브랜드 ID + 가격 복합 인덱스 생성 및 측정
        executeNativeQuery("CREATE INDEX idx_brand_id_price ON products (brand_id, price)");
        measurePerformance("브랜드 ID + 가격 복합 인덱스", brandIdToSearch);
        executeNativeQuery("DROP INDEX idx_brand_id_price ON products");
    }

    private void measurePerformance(String caseName, long brandId) {
        System.out.println("--- [" + caseName + "] 성능 측정을 시작합니다. ---");

        // 실행 계획 확인
        String explainQuery = "EXPLAIN SELECT * FROM products WHERE brand_id = " + brandId + " ORDER BY price ASC LIMIT 20";
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
            findProducts(brandId);
        }

        // 실제 측정 및 평균 계산
        long totalExecutionTime = 0;
        int iterations = 10;
        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            findProducts(brandId);
            long endTime = System.nanoTime();
            totalExecutionTime += (endTime - startTime);
        }
        long averageTime = totalExecutionTime / iterations;
        System.out.println("[" + caseName + "] 평균 실행 시간: " + TimeUnit.NANOSECONDS.toMillis(averageTime) + " ms\n");
    }

    private void findProducts(long brandId) {
        entityManager.createQuery("SELECT p FROM ProductEntity p WHERE p.brand.id = :brandId ORDER BY p.price ASC", ProductEntity.class)
                .setParameter("brandId", brandId)
                .setMaxResults(20)
                .getResultList();
    }

    private void executeNativeQuery(String sql) {
        transactionTemplate.execute(status -> {
            entityManager.createNativeQuery(sql).executeUpdate();
            return null;
        });
    }
}
