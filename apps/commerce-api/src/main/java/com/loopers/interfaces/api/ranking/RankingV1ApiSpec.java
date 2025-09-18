package com.loopers.interfaces.api.ranking;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

@Tag(
        name = "Ranking V1 API",
        description = "my-commerce 랭킹 API 입니다."
)
public interface RankingV1ApiSpec {

    @Operation(summary = "랭킹 조회")
    ApiResponse<PageResponse<RankingV1Dto.ProductRankResponse>> getDailyRanking(LocalDate date, Pageable pageable);

}
