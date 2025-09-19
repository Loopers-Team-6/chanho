package com.loopers.config.batch;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

@Component
@Profile("local")
@Order(2)
@RequiredArgsConstructor
public class TestJobLaucher {

    private final JobLauncher jobLauncher;
    private final Job weeklyRankingJob;
    private final Job monthlyRankingJob;

    @PostConstruct
    public void init() throws Exception {
        LocalDate today = LocalDate.now();

        // 1. 최근 3주간의 주간 랭킹 집계 Job 실행
        for (int i = 0; i < 3; i++) {
            LocalDate targetDate = today.minusWeeks(i);
            LocalDate lastSunday = targetDate.with(DayOfWeek.SUNDAY);
            LocalDate lastMonday = lastSunday.minusDays(6);

            JobParameters weeklyParams = new JobParametersBuilder()
                    .addString("startDate", lastMonday.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .addString("endDate", lastSunday.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(weeklyRankingJob, weeklyParams);
        }

        // 2. 지난 2개월간의 월간 랭킹 집계 Job 실행
        for (int i = 0; i < 2; i++) {
            LocalDate targetDate = today.minusMonths(i);
            LocalDate firstDay = targetDate.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate lastDay = targetDate.with(TemporalAdjusters.lastDayOfMonth());

            JobParameters monthlyParams = new JobParametersBuilder()
                    .addString("startDate", firstDay.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .addString("endDate", lastDay.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(monthlyRankingJob, monthlyParams);
        }
    }
}
