package kr.suhsaechan.mapsee.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * AI 서버로부터 장소 추출 요청 응답 (200 OK)
 * AI 서버 실제 응답 형식: {"received": true, "contentId": "..."}
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceExtractionResponse {
  @Schema(description = "Content UUID")
  private UUID contentId;

  @Schema(description = "AI 서버 요청 수신 여부", example = "true")
  private Boolean received;

  @Schema(description = "처리 상태 (deprecated, received 필드 사용 권장)", example = "ACCEPTED")
  private String status;
}
