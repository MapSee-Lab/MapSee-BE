package kr.suhsaechan.mapsy.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "북마크 수정 요청")
public class UpdateBookmarkRequest {

  @Schema(description = "폴더명 (null이면 변경하지 않음)", example = "가고 싶은 곳")
  private String folder;

  @Schema(description = "메모 (null이면 변경하지 않음)", example = "친구랑 같이 가기")
  private String memo;

  @Schema(description = "별점 1-5 (null이면 변경하지 않음)", example = "4")
  private Integer rating;

  @Schema(description = "방문 여부 (null이면 변경하지 않음)", example = "true")
  private Boolean visited;

  @Schema(description = "방문 일시 (null이면 visited=true 시 현재 시간)")
  private LocalDateTime visitedAt;
}
