package kr.suhsaechan.mapsy.place.dto;

import kr.suhsaechan.mapsy.place.constant.FolderVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
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
  @Size(max = 100, message = "폴더 이름은 100자 이하여야 합니다.")
  private String name;

  @Schema(description = "공개 설정", example = "PRIVATE")
  private FolderVisibility visibility;
}
