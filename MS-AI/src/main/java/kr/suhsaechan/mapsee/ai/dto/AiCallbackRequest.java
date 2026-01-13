package kr.suhsaechan.mapsee.ai.dto;

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
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCallbackRequest {

  @Schema(description = "처리 결과 상태", example = "SUCCESS", allowableValues = {"SUCCESS", "FAILED"})
  @NotNull(message = "resultStatus는 필수입니다.")
  private String resultStatus;

  @Schema(description = "Content UUID (FAILED 상태일 때 필수, SUCCESS일 때는 contentInfo.contentId 사용 가능)")
  private UUID contentId;

  @Schema(description = "콘텐츠 정보")
  @Valid
  private ContentInfo contentInfo;

  @Schema(description = "추출된 장소 목록")
  @Valid
  private List<PlaceInfo> places;

  /**
   * 콘텐츠 정보
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ContentInfo {

    @Schema(description = "Content UUID (백엔드에서 전송받은 UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotNull(message = "contentId는 필수입니다.")
    private UUID contentId;

    @Schema(description = "썸네일 URL", example = "https://img.youtube.com/vi/VIDEO_ID/maxresdefault.jpg")
    @NotNull(message = "thumbnailUrl은 필수입니다.")
    private String thumbnailUrl;

    @Schema(description = "SNS 플랫폼", example = "YOUTUBE", allowableValues = {"INSTAGRAM", "YOUTUBE", "YOUTUBE_SHORTS"})
    @NotNull(message = "platform은 필수입니다.")
    private String platform;

    @Schema(description = "콘텐츠 제목", example = "일본 전국 라멘 투어 - 개당 1200원의 가성비 초밥")
    // 필수 아님
    private String title;

    @Schema(description = "콘텐츠 URL", example = "https://www.youtube.com/watch?v=VIDEO_ID")
    private String contentUrl;

    @Schema(description = "업로더 아이디", example = "travel_lover_123")
    private String platformUploader;

    @Schema(description = "AI 콘텐츠 요약", example = "샷포로 3대 스시 맛집 '토리톤' 방문...")
    private String summary;
  }

  /**
   * 장소 정보
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class PlaceInfo {

    @Schema(description = "장소명", example = "명동 교자")
    @NotNull(message = "name은 필수입니다.")
    private String name;

    @Schema(description = "주소", example = "서울특별시 중구 명동길 29")
    @NotNull(message = "address는 필수입니다.")
    private String address;

    @Schema(description = "국가 코드 (ISO 3166-1 alpha-2)", example = "KR")
    private String country;

    @Schema(description = "장소 설명", example = "칼국수와 만두로 유명한 맛집")
    private String description;

    @Schema(description = "AI 추출 원본 데이터", example = "명동 교자에서 칼국수 먹었어요 (caption, confidence: 0.95)")
    private String rawData;

    @Schema(description = "언어 코드 (ISO 639-1)", example = "ko", allowableValues = {"ko", "en", "ja", "zh"})
    private String language = "ko";
  }
}
