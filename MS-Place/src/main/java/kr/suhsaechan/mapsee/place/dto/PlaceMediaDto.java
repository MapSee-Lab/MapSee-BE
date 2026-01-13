package kr.suhsaechan.mapsee.place.dto;

import kr.suhsaechan.mapsee.place.entity.PlaceMedia;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "장소 미디어 정보")
public class PlaceMediaDto {

  @Schema(description = "미디어 ID", example = "550e8400-e29b-41d4-a716-446655440000")
  private UUID id;

  @Schema(description = "미디어 URL", example = "https://example.com/media1.jpg")
  private String url;

  @Schema(description = "MIME 타입", example = "image/jpeg")
  private String mimeType;

  @Schema(description = "정렬 순서", example = "0")
  private Integer position;

  public static PlaceMediaDto from(PlaceMedia entity) {
    if (entity == null) {
      return null;
    }

    return PlaceMediaDto.builder()
        .id(entity.getId())
        .url(entity.getUrl())
        .mimeType(entity.getMimeType())
        .position(entity.getPosition())
        .build();
  }
}
