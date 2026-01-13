package kr.suhsaechan.mapsee.place.dto;

import kr.suhsaechan.mapsee.place.constant.PlacePlatform;
import kr.suhsaechan.mapsee.place.entity.PlacePlatformReference;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "플랫폼별 장소 참조 정보")
public class PlacePlatformReferenceDto {

  @Schema(description = "플랫폼 타입", example = "GOOGLE")
  private PlacePlatform placePlatform;

  @Schema(description = "플랫폼 장소 ID", example = "ChIJN1t_tDeuEmsRUsoyG83frY4")
  private String placePlatformId;

  public static PlacePlatformReferenceDto from(PlacePlatformReference entity) {
    if (entity == null) {
      return null;
    }

    return PlacePlatformReferenceDto.builder()
        .placePlatform(entity.getPlacePlatform())
        .placePlatformId(entity.getPlacePlatformId())
        .build();
  }
}
