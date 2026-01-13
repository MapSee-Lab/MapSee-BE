package kr.suhsaechan.mapsee.sns.dto;

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
@Schema(description = "최근 콘텐츠 목록 응답")
public class GetRecentContentResponse {
  @Schema(description = "콘텐츠 목록")
  private List<ContentDto> contents;
}