package kr.suhsaechan.mapsee.web.controller;

import kr.suhsaechan.mapsee.common.constant.Author;
import kr.suhsaechan.mapsee.member.constant.InterestCategory;
import kr.suhsaechan.mapsee.member.dto.interest.response.GetAllInterestsResponse;
import kr.suhsaechan.mapsee.member.dto.interest.response.GetInterestByIdResponse;
import kr.suhsaechan.mapsee.member.dto.interest.response.GetInterestsByCategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

public interface InterestControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.11.04", author = Author.SUHSAECHAN, issueNumber = 61, description = "전체 관심사 목록 조회 init"),
  })
  @Operation(summary = "전체 관심사 목록 조회", description = "13개 대분류 카테고리별로 그룹핑된 전체 관심사 목록을 조회합니다. (Redis 캐싱 적용)")
  ResponseEntity<GetAllInterestsResponse> getAllInterests();

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.11.04", author = Author.SUHSAECHAN, issueNumber = 61, description = "특정 카테고리 관심사 조회 init"),
  })
  @Operation(summary = "특정 카테고리 관심사 조회", description = "특정 대분류 카테고리의 관심사 목록을 조회합니다.")
  ResponseEntity<GetInterestsByCategoryResponse> getInterestsByCategory(
      @Parameter(description = "관심사 카테고리 (FOOD, CAFE_DESSERT 등)", required = true)
      @PathVariable InterestCategory category
  );

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.11.04", author = Author.SUHSAECHAN, issueNumber = 61, description = "관심사 상세 조회 init"),
  })
  @Operation(summary = "관심사 상세 조회", description = "관심사 ID로 특정 관심사의 상세 정보를 조회합니다.")
  ResponseEntity<GetInterestByIdResponse> getInterestById(
      @Parameter(description = "관심사 ID", required = true)
      @PathVariable UUID interestId
  );
}
