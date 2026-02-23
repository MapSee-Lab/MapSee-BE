package kr.suhsaechan.mapsy.place.dto;

import kr.suhsaechan.mapsy.place.entity.FolderPlace;
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
@Schema(description = "폴더에 장소 추가 응답")
public class AddFolderPlaceResponse {
  @Schema(description = "폴더-장소 연결 ID")
  private UUID id;

  @Schema(description = "폴더 ID")
  private UUID folderId;

  @Schema(description = "장소 ID")
  private UUID placeId;

  @Schema(description = "정렬 순서")
  private int position;

  @Schema(description = "생성일시")
  private LocalDateTime createdAt;

  public static AddFolderPlaceResponse from(FolderPlace folderPlace) {
    return AddFolderPlaceResponse.builder()
        .id(folderPlace.getId())
        .folderId(folderPlace.getFolder().getId())
        .placeId(folderPlace.getPlace().getId())
        .position(folderPlace.getPosition())
        .createdAt(folderPlace.getCreatedAt())
        .build();
  }
}
