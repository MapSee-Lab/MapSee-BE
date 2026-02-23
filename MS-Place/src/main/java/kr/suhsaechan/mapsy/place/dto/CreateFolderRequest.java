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
@Schema(description = "폴더 생성 요청")
public class CreateFolderRequest {
  @Schema(description = "폴더 이름", example = "가고 싶은 곳")
  private String name;

  @Schema(description = "공개 설정", example = "PRIVATE")
  private FolderVisibility visibility;
}
