package kr.suhsaechan.mapsy.place.dto;

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
@Schema(description = "폴더 목록 조회 응답")
public class GetFoldersResponse {
  @Schema(description = "폴더 목록")
  private List<FolderDto> folders;
}
