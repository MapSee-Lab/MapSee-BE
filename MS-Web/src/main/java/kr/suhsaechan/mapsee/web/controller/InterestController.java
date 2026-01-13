package kr.suhsaechan.mapsee.web.controller;

import kr.suhsaechan.mapsee.member.constant.InterestCategory;
import kr.suhsaechan.mapsee.member.dto.interest.response.GetAllInterestsResponse;
import kr.suhsaechan.mapsee.member.dto.interest.response.GetInterestByIdResponse;
import kr.suhsaechan.mapsee.member.dto.interest.response.GetInterestsByCategoryResponse;
import kr.suhsaechan.mapsee.member.service.InterestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interests")
@Tag(name = "관심사 관리", description = "관심사 조회 API")
public class InterestController implements InterestControllerDocs {

  private final InterestService interestService;

  /**
   * 전체 관심사 목록 조회 (대분류별 그룹핑)
   */
  @GetMapping
  public ResponseEntity<GetAllInterestsResponse> getAllInterests() {
    GetAllInterestsResponse result = interestService.getAllInterestsGroupedByCategory();
    return ResponseEntity.ok(result);
  }

  /**
   * 특정 카테고리 관심사 조회
   */
  @GetMapping("/categories/{category}")
  public ResponseEntity<GetInterestsByCategoryResponse> getInterestsByCategory(
      @PathVariable InterestCategory category) {
    GetInterestsByCategoryResponse result = interestService.getInterestsByCategory(category);
    return ResponseEntity.ok(result);
  }

  /**
   * 관심사 상세 조회
   */
  @GetMapping("/{interestId}")
  public ResponseEntity<GetInterestByIdResponse> getInterestById(@PathVariable UUID interestId) {
    GetInterestByIdResponse result = interestService.getInterestById(interestId);
    return ResponseEntity.ok(result);
  }
}
