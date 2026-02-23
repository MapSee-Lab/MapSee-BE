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
@Schema(description = "폴더 정보")
public class FolderDto {
  @Schema(description = "폴더 ID")
  private UUID id;

  @Schema(description = "폴더 이름")
  private String name;

  @Schema(description = "공개 설정")
  private FolderVisibility visibility;

  @Schema(description = "썸네일 URL")
  private String thumbnailUrl;

  @Schema(description = "기본 폴더 여부")
  private Boolean isDefault;

  @Schema(description = "폴더 내 장소 수")
  private int placeCount;

  @Schema(description = "생성일시")
  private LocalDateTime createdAt;

  public static FolderDto from(Folder folder, int placeCount) {
    return FolderDto.builder()
        .id(folder.getId())
        .name(folder.getName())
        .visibility(folder.getVisibility())
        .thumbnailUrl(folder.getThumbnailUrl())
        .isDefault(folder.getIsDefault())
        .placeCount(placeCount)
        .createdAt(folder.getCreatedAt())
        .build();
  }
}
