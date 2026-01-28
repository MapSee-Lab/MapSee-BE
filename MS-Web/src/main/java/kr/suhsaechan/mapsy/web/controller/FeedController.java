package kr.suhsaechan.mapsy.web.controller;

import java.util.List;
import kr.suhsaechan.mapsy.auth.dto.CustomUserDetails;
import kr.suhsaechan.mapsy.place.dto.KeywordDto;
import kr.suhsaechan.mapsy.place.dto.PlaceDto;
import kr.suhsaechan.mapsy.place.service.FeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/feed")
public class FeedController implements FeedControllerDocs {

  private final FeedService feedService;

  /**
   * 최신 장소 피드 조회
   */
  @GetMapping("/latest")
  @Override
  public ResponseEntity<Page<PlaceDto>> getLatestPlaces(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    log.info("Get latest places feed: page={}, size={}",
        pageable.getPageNumber(), pageable.getPageSize());

    Page<PlaceDto> response = feedService.getLatestPlaces(pageable);

    return ResponseEntity.ok(response);
  }

  /**
   * 인기 장소 피드 조회
   */
  @GetMapping("/popular")
  @Override
  public ResponseEntity<Page<PlaceDto>> getPopularPlaces(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PageableDefault(size = 20) Pageable pageable
  ) {
    log.info("Get popular places feed: page={}, size={}",
        pageable.getPageNumber(), pageable.getPageSize());

    Page<PlaceDto> response = feedService.getPopularPlaces(pageable);

    return ResponseEntity.ok(response);
  }

  /**
   * 내 TOP 저장 장소 조회
   */
  @GetMapping("/my-top")
  @Override
  public ResponseEntity<List<PlaceDto>> getMyTopPlaces(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    log.info("Get my top places for member: {}", userDetails.getMemberId());

    List<PlaceDto> response = feedService.getMyTopPlaces(userDetails.getMemberId());

    return ResponseEntity.ok(response);
  }

  /**
   * 트렌드 키워드 조회
   */
  @GetMapping("/trending-keywords")
  @Override
  public ResponseEntity<List<KeywordDto>> getTrendingKeywords(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(defaultValue = "20") int size
  ) {
    log.info("Get trending keywords: size={}", size);

    List<KeywordDto> response = feedService.getTrendingKeywords(size);

    return ResponseEntity.ok(response);
  }
}
