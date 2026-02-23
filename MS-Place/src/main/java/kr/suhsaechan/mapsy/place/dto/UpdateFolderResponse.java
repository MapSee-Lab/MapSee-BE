package kr.suhsaechan.mapsy.place.dto;

import kr.suhsaechan.mapsy.place.constant.FolderVisibility;
import kr.suhsaechan.mapsy.place.entity.Folder;
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
@Schema(description = "폴더 수정 응답")
public class UpdateFolderResponse {
  @Schema(description = "폴더 ID")
  private UUID id;

  @Schema(description = "폴더 이름")
  private String name;

  @Schema(description = "공개 설정")
  private FolderVisibility visibility;

  @Schema(description = "기본 폴더 여부")
  private Boolean isDefault;

  @Schema(description = "수정일시")
  private LocalDateTime updatedAt;

  public static UpdateFolderResponse from(Folder folder) {
    return UpdateFolderResponse.builder()
        .id(folder.getId())
        .name(folder.getName())
        .visibility(folder.getVisibility())
        .isDefault(folder.getIsDefault())
        .updatedAt(folder.getUpdatedAt())
        .build();
  }
}
