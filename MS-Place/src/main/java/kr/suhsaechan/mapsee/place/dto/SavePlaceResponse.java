package kr.suhsaechan.mapsee.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "장소 저장 응답")
public class SavePlaceResponse {
  @Schema(description = "회원 장소 ID", example = "550e8400-e29b-41d4-a716-446655440000")
  private UUID memberPlaceId;

  @Schema(description = "장소 ID", example = "550e8400-e29b-41d4-a716-446655440000")
  private UUID placeId;

  @Schema(description = "저장 상태", example = "SAVED")
  private String savedStatus;

  @Schema(description = "저장 일시", example = "2024-11-24T10:30:00")
  private LocalDateTime savedAt;
}
