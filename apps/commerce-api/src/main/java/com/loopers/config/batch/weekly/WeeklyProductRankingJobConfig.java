package com.loopers.config.batch.weekly;

import com.loopers.domain.ranking.WeeklyProductRanking;
import com.loopers.domain.ranking.WeeklyProductRankingRepository;
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
public class WeeklyProductRankingJobConfig {

    private final EntityManagerFactory entityManagerFactory;
    private static final int CHUNK_SIZE = 1000;

    @Bean
    public Job weeklyRankingJob(
            JobRepository jobRepository,
            Step cleanupWeeklyRankingStep,
            Step weeklyRankingStep
    ) {
        return new JobBuilder("weeklyRankingJob", jobRepository)
                .start(cleanupWeeklyRankingStep)
                .next(weeklyRankingStep)
                .build();
    }

    @Bean
    @JobScope
    public Step cleanupWeeklyRankingStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            WeeklyProductRankingRepository repository,
            @Value("#{jobParameters['startDate']}") String startDateStr
    ) {
        return new StepBuilder("cleanupWeeklyRankingStep", jobRepository)
                .tasklet(
                        (contribution, chunkContext) -> {
                            LocalDate weekStartDate = LocalDate.parse(startDateStr);
                            repository.deleteByWeekStartDate(weekStartDate);
                            return RepeatStatus.FINISHED;
                        }, transactionManager
                )
                .build();
    }

    @Bean
    public Step weeklyRankingStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<WeeklyProductMetricDto> weeklyMetricReader,
            ItemProcessor<WeeklyProductMetricDto, WeeklyProductRanking> weeklyMetricProcessor,
            ItemWriter<WeeklyProductRanking> weeklyRankingWriter
    ) {
        return new StepBuilder("weeklyRankingStep", jobRepository)
                .<WeeklyProductMetricDto, WeeklyProductRanking>chunk(CHUNK_SIZE, transactionManager)
                .reader(weeklyMetricReader)
                .processor(weeklyMetricProcessor)
                .writer(weeklyRankingWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<WeeklyProductMetricDto> weeklyMetricReader(
            DataSource dataSource,
            @Value("#{jobParameters['startDate']}") String startDateStr,
            @Value("#{jobParameters['endDate']}") String endDateStr
    ) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        return new JdbcPagingItemReaderBuilder<WeeklyProductMetricDto>()
                .name("weeklyMetricReader")
                .dataSource(dataSource)
                .pageSize(CHUNK_SIZE)
                .fetchSize(CHUNK_SIZE)
                .rowMapper(new BeanPropertyRowMapper<>(WeeklyProductMetricDto.class))
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
    public ItemProcessor<WeeklyProductMetricDto, WeeklyProductRanking> weeklyMetricProcessor(
            @Value("#{jobParameters['startDate']}") String startDateStr
    ) {
        LocalDate weekStartDate = LocalDate.parse(startDateStr);

        return dto -> {
            double score = (dto.getTotalViewCount() * 0.1) +
                    (dto.getTotalLikeCount() * 0.3) +
                    (dto.getTotalOrderCount() * 0.6);

            return new WeeklyProductRanking(dto.getProductId(), score, weekStartDate);
        };
    }

    @Bean
    public ItemWriter<WeeklyProductRanking> weeklyRankingWriter() {
        return new JpaItemWriterBuilder<WeeklyProductRanking>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
