package kr.suhsaechan.mapsee.sns.dto;

import kr.suhsaechan.mapsee.place.entity.Place;
import kr.suhsaechan.mapsee.sns.entity.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "콘텐츠 정보 조회 응답")
public class GetContentInfoResponse {

  @Schema(description = "콘텐츠 상세 정보")
  private ContentDto content;

  @Schema(description = "연관된 장소 목록 (position 순서)")
  private List<PlaceDto> places;

  public static GetContentInfoResponse from(Content content, List<Place> places) {
    return GetContentInfoResponse.builder()
        .content(ContentDto.from(content))
        .places(places.stream()
            .map(PlaceDto::from)
            .collect(Collectors.toList()))
        .build();
  }
}
