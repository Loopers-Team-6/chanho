package com.loopers.infrastructure;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class PerformanceTestHelper {

    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    public void executeNativeQueryWithTransaction(String sql) {
        transactionTemplate.execute(status -> {
            entityManager.createNativeQuery(sql).executeUpdate();
            return null;
        });
    }

    public void analizeTable(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            throw new IllegalArgumentException("테이블 이름은 null 또는 빈 문자열일 수 없습니다.");
        }

        transactionTemplate.execute(status -> {
            entityManager.createNativeQuery("ANALYZE TABLE " + tableName).getResultList();
            return null;
        });
    }

    /**
     * 주어진 작업(Runnable)의 실행 시간을 측정하고 평균을 출력합니다.
     *
     * @param caseName    테스트 케이스 이름
     * @param task        측정할 작업
     * @param repeatCount 반복 횟수
     */
    public void recordExecutionTime(String caseName, Runnable task, int repeatCount, int warmupCount) {
        // 워밍업
        for (int i = 0; i < warmupCount; i++) {
            task.run();
        }

        // 실제 측정
        long totalExecutionTime = 0;
        for (int i = 0; i < repeatCount; i++) {
            long startTime = System.nanoTime();
            task.run();
            long endTime = System.nanoTime();
            totalExecutionTime += endTime - startTime;
        }
        long averageTime = totalExecutionTime / repeatCount;
        System.out.println("CASE    : [" + caseName + "]");
        System.out.println("COUNT   : [" + repeatCount + "]");
        System.out.println("AVERAGE : [" + TimeUnit.NANOSECONDS.toMillis(averageTime) + " ms]");
    }

    /**
     * EXPLAIN 쿼리를 실행하고 그 결과를 보기 좋게 출력합니다.
     *
     * @param caseName     테스트 케이스 이름
     * @param explainQuery EXPLAIN 쿼리 문자열
     */
    public void printExplainResult(String caseName, String explainQuery) {
        System.out.println("--- [" + caseName + "] EXPLAIN 결과 -----------------------");

        @SuppressWarnings("unchecked")
        List<Object[]> explainResult = entityManager.createNativeQuery(explainQuery, Object[].class).getResultList();

        List<String> columnNames = List.of("id", "select_type", "table", "partitions", "type", "possible_keys", "key", "key_len", "ref", "rows", "filtered", "Extra");

        for (int i = 0; i < explainResult.size(); i++) {
            Object[] row = explainResult.get(i);
            System.out.println("  [ Row " + (i + 1) + " ]");
            for (int j = 0; j < row.length; j++) {
                String header = (j < columnNames.size()) ? columnNames.get(j) : "column_" + (j + 1);
                String value = (row[j] != null) ? row[j].toString() : "NULL";
                System.out.printf("    %-15s : %s%n", header, value);
            }
        }
        System.out.println("------------------------------------------------------------");
    }
}
