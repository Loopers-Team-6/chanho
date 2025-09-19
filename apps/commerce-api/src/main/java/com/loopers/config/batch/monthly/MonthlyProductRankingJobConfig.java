package com.loopers.config.batch.monthly;

import com.loopers.domain.ranking.MonthlyProductRanking;
import com.loopers.domain.ranking.MonthlyProductRankingRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MonthlyProductRankingJobConfig {

    private final EntityManagerFactory entityManagerFactory;
    private static final int CHUNK_SIZE = 1000;

    @Bean
    public Job monthlyRankingJob(
            JobRepository jobRepository,
            Step cleanupMonthlyRankingStep,
            Step monthlyRankingStep
    ) {
        return new JobBuilder("monthlyRankingJob", jobRepository)
                .start(cleanupMonthlyRankingStep)
                .next(monthlyRankingStep)
                .build();
    }

    @Bean
    @JobScope
    public Step cleanupMonthlyRankingStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            MonthlyProductRankingRepository repository,
            @Value("#{jobParameters['startDate']}") String startDateStr
    ) {
        return new StepBuilder("cleanupMonthlyRankingStep", jobRepository)
                .tasklet(
                        (contribution, chunkContext) -> {
                            LocalDate monthStartDate = LocalDate.parse(startDateStr);
                            repository.deleteByMonthStartDate(monthStartDate);
                            return RepeatStatus.FINISHED;
                        }, transactionManager
                )
                .build();
    }

    @Bean
    public Step monthlyRankingStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<MonthlyProductMetricDto> monthlyMetricReader,
            ItemProcessor<MonthlyProductMetricDto, MonthlyProductRanking> monthlyMetricProcessor,
            ItemWriter<MonthlyProductRanking> monthlyRankingWriter
    ) {
        return new StepBuilder("monthlyRankingStep", jobRepository)
                .<MonthlyProductMetricDto, MonthlyProductRanking>chunk(CHUNK_SIZE, transactionManager)
                .reader(monthlyMetricReader)
                .processor(monthlyMetricProcessor)
                .writer(monthlyRankingWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<MonthlyProductMetricDto> monthlyMetricReader(
            DataSource dataSource,
            @Value("#{jobParameters['startDate']}") String startDateStr,
            @Value("#{jobParameters['endDate']}") String endDateStr
    ) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        return new JdbcPagingItemReaderBuilder<MonthlyProductMetricDto>()
                .name("monthlyMetricReader")
                .dataSource(dataSource)
                .pageSize(CHUNK_SIZE)
                .fetchSize(CHUNK_SIZE)
                .rowMapper(new BeanPropertyRowMapper<>(MonthlyProductMetricDto.class))
                .selectClause("product_id, " +
                        "SUM(view_count) as totalViewCount, " +
                        "SUM(like_count) as totalLikeCount, " +
                        "SUM(order_count) as totalOrderCount")
                .fromClause("product_metrics")
                .whereClause("metric_date >= :startDate AND metric_date <= :endDate")
                .groupClause("product_id")
                .sortKeys(Map.of("product_id", Order.ASCENDING))
                .parameterValues(params)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<MonthlyProductMetricDto, MonthlyProductRanking> monthlyMetricProcessor(
            @Value("#{jobParameters['startDate']}") String startDateStr
    ) {
        LocalDate monthStartDate = LocalDate.parse(startDateStr);

        return dto -> {
            double score = (dto.getTotalViewCount() * 0.1) +
                    (dto.getTotalLikeCount() * 0.3) +
                    (dto.getTotalOrderCount() * 0.6);

            return new MonthlyProductRanking(dto.getProductId(), score, monthStartDate);
        };
    }

    @Bean
    public ItemWriter<MonthlyProductRanking> monthlyRankingWriter() {
        return new JpaItemWriterBuilder<MonthlyProductRanking>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
