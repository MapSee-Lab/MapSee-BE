package kr.suhsaechan.mapsee.web.controller;

import kr.suhsaechan.mapsee.auth.dto.CustomUserDetails;
import kr.suhsaechan.mapsee.place.dto.GetSavedPlacesResponse;
import kr.suhsaechan.mapsee.place.dto.PlaceDto;
import kr.suhsaechan.mapsee.sns.dto.ContentDto;
import kr.suhsaechan.mapsee.sns.dto.GetRecentContentResponse;
import kr.suhsaechan.mapsee.sns.dto.GetContentInfoResponse;
import kr.suhsaechan.mapsee.sns.dto.GetMemberContentPageResponse;
import kr.suhsaechan.mapsee.sns.dto.RequestPlaceExtractionRequest;
import kr.suhsaechan.mapsee.sns.dto.RequestPlaceExtractionResponse;
import kr.suhsaechan.mapsee.sns.service.ContentService;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/content")
//@Tag()
public class ContentController implements ContentControllerDocs {

  private final ContentService contentService;

  /**
   * 프론트에서 전달한 SNS URL을 받아 AI 서버에 장소 추출을 의뢰합니다.
   * - 컨트롤러는 URL 수신/검증/로그만 담당
   * - 비즈니스 처리는 AiServerService로 위임
   */
  @PostMapping("/analyze")
  @Override
  public ResponseEntity<RequestPlaceExtractionResponse> requestPlaceExtraction(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody RequestPlaceExtractionRequest request
  ) {
    return ResponseEntity.ok(contentService.createContentAndRequestPlaceExtraction(request, userDetails.getMemberId()));
  }

  /**
   * 단일 SNS 컨텐츠 정보 및 연관된 장소 목록 조회
   * - Content ID로 Content 정보와 연관된 Place 목록을 조회합니다.
   * - Place 목록은 position 순서대로 정렬되어 반환됩니다.
   */
  @GetMapping("/{contentId}")
  @Override
  public ResponseEntity<GetContentInfoResponse> getContentInfo(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID contentId
  ) {
    GetContentInfoResponse response = contentService.getContentInfo(contentId);
    return ResponseEntity.ok(response);
  }

  /**
   * 회원의 Content 목록 조회
   * - 인증된 회원이 소유한 Content 목록을 최신순으로 조회합니다.
   * - Place 정보는 제외하고 Content 정보만 반환합니다.
   */
  @GetMapping("/member")
  @Override
  public ResponseEntity<GetMemberContentPageResponse> getMemberContentPage(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(required = false, defaultValue = "10") Integer pageSize
  ) {
    // Member ID 추출
    UUID memberId = userDetails.getMemberId();

    // Service 호출
    Page<ContentDto> contentPage = contentService.getMemberContentPage(memberId, pageSize);

    // Response 생성
    GetMemberContentPageResponse response = GetMemberContentPageResponse.builder()
        .contentPage(contentPage)
        .build();

    return ResponseEntity.ok(response);
  }


    @GetMapping("/recent")
    public ResponseEntity<GetRecentContentResponse> getRecentContents(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(contentService.getRecentContents(userDetails.getMemberId()));
    }

    /**
     * 사용자가 저장한 장소 목록 조회 (최신순, 최대 10개)
     */
    @GetMapping("/place/saved")
    public ResponseEntity<GetSavedPlacesResponse> getSavedPlaces(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<PlaceDto> places = contentService.getSavedPlaces(userDetails.getMemberId());

        GetSavedPlacesResponse response = GetSavedPlacesResponse.builder()
                .places(places)
                .build();

        return ResponseEntity.ok(response);
    }
}
