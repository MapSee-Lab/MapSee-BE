package kr.suhsaechan.mapsy.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;
import kr.suhsaechan.mapsy.place.entity.Keyword;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "키워드 DTO")
public class KeywordDto {

  @Schema(description = "키워드 ID", example = "550e8400-e29b-41d4-a716-446655440000")
  private UUID id;

  @Schema(description = "키워드 문자열", example = "강남카페")
  private String keyword;

  @Schema(description = "사용 횟수", example = "42")
  private Integer count;

  @Schema(description = "트렌드 점수", example = "15.50")
  private BigDecimal trendScore;

  public static KeywordDto from(Keyword keyword) {
    if (keyword == null) {
      return null;
    }

    return KeywordDto.builder()
        .id(keyword.getId())
        .keyword(keyword.getKeyword())
        .count(keyword.getCount())
        .trendScore(keyword.getTrendScore())
        .build();
  }
}
