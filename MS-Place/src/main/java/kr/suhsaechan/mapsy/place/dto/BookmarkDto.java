package kr.suhsaechan.mapsy.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import kr.suhsaechan.mapsy.place.entity.MemberPlace;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "북마크 DTO")
public class BookmarkDto {

  @Schema(description = "MemberPlace ID", example = "550e8400-e29b-41d4-a716-446655440000")
  private UUID memberPlaceId;

  @Schema(description = "장소 정보")
  private PlaceDto place;

  @Schema(description = "폴더명", example = "가고 싶은 곳")
  private String folder;

  @Schema(description = "메모", example = "친구랑 같이 가기")
  private String memo;

  @Schema(description = "별점 (1-5)", example = "4")
  private Integer rating;

  @Schema(description = "방문 여부", example = "false")
  private Boolean visited;

  @Schema(description = "방문 일시")
  private LocalDateTime visitedAt;

  @Schema(description = "저장 일시")
  private LocalDateTime savedAt;

  public static BookmarkDto from(MemberPlace memberPlace) {
    if (memberPlace == null) {
      return null;
    }

    return BookmarkDto.builder()
        .memberPlaceId(memberPlace.getId())
        .place(PlaceDto.from(memberPlace.getPlace()))
        .folder(memberPlace.getFolder())
        .memo(memberPlace.getMemo())
        .rating(memberPlace.getRating())
        .visited(memberPlace.getVisited())
        .visitedAt(memberPlace.getVisitedAt())
        .savedAt(memberPlace.getSavedAt())
        .build();
  }
}
