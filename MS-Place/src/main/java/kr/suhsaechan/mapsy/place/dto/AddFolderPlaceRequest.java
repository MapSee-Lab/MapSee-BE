package kr.suhsaechan.mapsy.place.dto;

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
@Schema(description = "폴더에 장소 추가 요청")
public class AddFolderPlaceRequest {
  @Schema(description = "추가할 장소 ID", example = "550e8400-e29b-41d4-a716-446655440000")
  private UUID placeId;
}
