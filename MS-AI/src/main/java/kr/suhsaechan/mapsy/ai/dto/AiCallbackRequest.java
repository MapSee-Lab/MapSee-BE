package kr.suhsaechan.mapsy.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * AI 서버로부터 Webhook Callback으로 받는 분석 결과 DTO
 *
 * @see <a href="https://github.com/MapSee-Lab/MapSy-BE/issues/16">GitHub Issue #16</a>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCallbackRequest {

  @Schema(description = "Content UUID", example = "550e8400-e29b-41d4-a716-446655440000")
  @NotNull(message = "contentId는 필수입니다.")
  private UUID contentId;

  @Schema(description = "처리 결과 상태", example = "SUCCESS", allowableValues = {"SUCCESS", "FAILED"})
  @NotNull(message = "resultStatus는 필수입니다.")
  private String resultStatus;

  @Schema(description = "SNS 콘텐츠 정보 (SUCCESS 시 필수)")
  @Valid
  private SnsInfoCallback snsInfo;

  @Schema(description = "추출된 장소 상세 목록")
  @Valid
  private List<PlaceDetailCallback> placeDetails;

  @Schema(description = "추출 처리 통계")
  @Valid
  private ExtractionStatistics statistics;

  @Schema(description = "실패 사유 (FAILED 시)")
  private String errorMessage;

  /**
   * SNS 콘텐츠 정보
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SnsInfoCallback {

    @Schema(description = "SNS 플랫폼", example = "INSTAGRAM",
        allowableValues = {"INSTAGRAM", "YOUTUBE", "YOUTUBE_SHORTS", "TIKTOK", "FACEBOOK", "TWITTER"})
    @NotNull(message = "platform은 필수입니다.")
    private String platform;

    @Schema(description = "콘텐츠 타입", example = "reel")
    @NotNull(message = "contentType은 필수입니다.")
    private String contentType;

    @Schema(description = "원본 SNS URL", example = "https://www.instagram.com/reel/ABC123/")
    @NotNull(message = "url은 필수입니다.")
    private String url;

    @Schema(description = "작성자 ID", example = "username")
    private String author;

    @Schema(description = "게시물 본문", example = "여기 정말 맛있어! #맛집 #서울")
    private String caption;

    @Schema(description = "좋아요 수", example = "1234")
    private Integer likesCount;

    @Schema(description = "댓글 수", example = "56")
    private Integer commentsCount;

    @Schema(description = "게시 날짜 (ISO 8601)", example = "2024-01-15T10:30:00Z")
    private String postedAt;

    @Schema(description = "해시태그 리스트", example = "[\"맛집\", \"서울\"]")
    private List<String> hashtags;

    @Schema(description = "대표 이미지/썸네일 URL", example = "https://...")
    private String thumbnailUrl;

    @Schema(description = "이미지 URL 리스트", example = "[\"https://...\"]")
    private List<String> imageUrls;

    @Schema(description = "작성자 프로필 이미지 URL", example = "https://...")
    private String authorProfileImageUrl;
  }

  /**
   * 장소 상세 정보
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class PlaceDetailCallback {

    // 기본 정보
    @Schema(description = "네이버 Place ID", example = "11679241")
    @NotNull(message = "placeId는 필수입니다.")
    private String placeId;

    @Schema(description = "장소명", example = "늘푸른목장 잠실본점")
    @NotNull(message = "name은 필수입니다.")
    private String name;

    @Schema(description = "카테고리", example = "소고기구이")
    private String category;

    @Schema(description = "한줄 설명", example = "된장찌개와 냉면으로 완성하는 한상차림")
    private String description;

    // 위치 정보
    @Schema(description = "위도", example = "37.5112")
    private Double latitude;

    @Schema(description = "경도", example = "127.0867")
    private Double longitude;

    @Schema(description = "지번 주소", example = "서울 송파구 백제고분로9길 34 1F")
    private String address;

    @Schema(description = "도로명 주소", example = "서울 송파구 백제고분로9길 34 1F")
    private String roadAddress;

    @Schema(description = "지하철 정보", example = "잠실새내역 4번 출구에서 412m")
    private String subwayInfo;

    @Schema(description = "찾아가는 길", example = "잠실새내역 4번 출구에서 맥도널드 골목 끼고...")
    private String directionsText;

    // 평점/리뷰
    @Schema(description = "별점 (0.0~5.0)", example = "4.42")
    private Double rating;

    @Schema(description = "방문자 리뷰 수", example = "1510")
    private Integer visitorReviewCount;

    @Schema(description = "블로그 리뷰 수", example = "1173")
    private Integer blogReviewCount;

    // 영업 정보
    @Schema(description = "영업 상태", example = "영업 중")
    private String businessStatus;

    @Schema(description = "영업 시간 요약", example = "24:00에 영업 종료")
    private String businessHours;

    @Schema(description = "요일별 상세 영업시간", example = "[\"월 11:30 - 24:00\", \"화 11:30 - 24:00\"]")
    private List<String> openHoursDetail;

    @Schema(description = "휴무일 정보", example = "연중무휴")
    private String holidayInfo;

    // 연락처/링크
    @Schema(description = "전화번호", example = "02-3431-4520")
    private String phoneNumber;

    @Schema(description = "홈페이지 URL", example = "http://example.com")
    private String homepageUrl;

    @Schema(description = "네이버 지도 URL", example = "https://map.naver.com/p/search/늘푸른목장/place/11679241")
    private String naverMapUrl;

    @Schema(description = "예약 가능 여부", example = "true")
    private Boolean reservationAvailable;

    // 부가 정보
    @Schema(description = "편의시설 목록", example = "[\"단체 이용 가능\", \"주차\", \"발렛파킹\"]")
    private List<String> amenities;

    @Schema(description = "키워드/태그", example = "[\"소고기\", \"한우\", \"회식\"]")
    private List<String> keywords;

    @Schema(description = "TV 방송 출연 정보", example = "[\"줄서는식당 14회 (24.05.13)\"]")
    private List<String> tvAppearances;

    @Schema(description = "대표 메뉴", example = "[\"경주갈비살\", \"한우된장밥\"]")
    private List<String> menuInfo;

    @Schema(description = "대표 이미지 URL", example = "https://...")
    private String imageUrl;

    @Schema(description = "이미지 URL 목록", example = "[\"https://...\"]")
    private List<String> imageUrls;
  }

  /**
   * 추출 처리 통계
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ExtractionStatistics {

    @Schema(description = "LLM이 추출한 장소명 리스트", example = "[\"늘푸른목장\", \"강남역\"]")
    private List<String> extractedPlaceNames;

    @Schema(description = "LLM이 추출한 장소 수", example = "2")
    private Integer totalExtracted;

    @Schema(description = "네이버 지도에서 찾은 장소 수", example = "1")
    private Integer totalFound;

    @Schema(description = "검색 실패한 장소명", example = "[\"강남역\"]")
    private List<String> failedSearches;
  }
}
