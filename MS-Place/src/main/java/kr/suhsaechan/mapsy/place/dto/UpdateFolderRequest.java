package kr.suhsaechan.mapsy.place.dto;

import kr.suhsaechan.mapsy.place.constant.FolderVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "폴더 수정 요청")
public class UpdateFolderRequest {
  @Schema(description = "폴더 이름", example = "맛집 모음")
  private String name;

  @Schema(description = "공개 설정", example = "SHARED")
  private FolderVisibility visibility;
}
