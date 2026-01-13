package kr.suhsaechan.mapsee.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "저장한 장소 목록 응답")
public class GetSavedPlacesResponse {
  @Schema(description = "장소 목록")
  private List<PlaceDto> places;
}
