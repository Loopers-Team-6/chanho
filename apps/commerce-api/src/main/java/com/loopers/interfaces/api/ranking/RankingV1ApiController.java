package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingFacade;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
public class RankingV1ApiController implements RankingV1ApiSpec {

    private final RankingFacade rankingFacade;

    @GetMapping(params = {"period=daily"})
    @Override
    public ApiResponse<PageResponse<RankingV1Dto.ProductRankResponse>> getDailyRanking(
            @RequestParam(required = false) LocalDate date,
            Pageable pageable
    ) {
        LocalDate targetDate = (date == null)
                ? LocalDate.now()
                : date;

        return ApiResponse.success(rankingFacade.getDailyRanking(targetDate, pageable));
    }

    @GetMapping(params = {"period=weekly"})
    @Override
    public ApiResponse<PageResponse<RankingV1Dto.ProductRankResponse>> getWeeklyRanking(
            @RequestParam(required = false) LocalDate date,
            Pageable pageable
    ) {
        LocalDate targetDate = (date == null)
                ? LocalDate.now()
                : date;

        return ApiResponse.success(rankingFacade.getWeeklyRanking(targetDate, pageable));
    }

    @GetMapping(params = {"period=monthly"})
    @Override
    public ApiResponse<PageResponse<RankingV1Dto.ProductRankResponse>> getMonthlyRanking(
            @RequestParam(required = false) LocalDate date,
            Pageable pageable
    ) {
        LocalDate targetDate = (date == null)
                ? LocalDate.now()
                : date;

        return ApiResponse.success(rankingFacade.getMonthlyRanking(targetDate, pageable));
    }
}
