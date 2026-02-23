package kr.suhsaechan.mapsy.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "폴더 내 장소 목록 조회 응답")
public class GetFolderPlacesResponse {
  @Schema(description = "폴더 ID")
  private UUID folderId;

  @Schema(description = "폴더 이름")
  private String folderName;

  @Schema(description = "장소 목록")
  private List<PlaceDto> places;
}
