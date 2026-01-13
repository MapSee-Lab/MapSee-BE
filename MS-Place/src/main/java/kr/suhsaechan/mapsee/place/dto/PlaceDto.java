package kr.suhsaechan.mapsee.place.dto;


import kr.suhsaechan.mapsee.place.entity.Place;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "장소 DTO")
public class PlaceDto {
  @Schema(description = "장소 ID", example = "550e8400-e29b-41d4-a716-446655440000")
  private UUID placeId;

  @Schema(description = "장소명", example = "스타벅스 서울역점")
  private String name;

  @Schema(description = "주소", example = "서울특별시 중구 명동길 29")
  private String address;

  @Schema(description = "별점 (0.0 ~ 5.0)", example = "4.5")
  private BigDecimal rating;

  @Schema(description = "리뷰 수", example = "123")
  private Integer userRatingsTotal;

  @Schema(description = "사진 URL 배열 (최대 10개)")
  private List<String> photoUrls;

  @Schema(description = "장소 요약 설명", example = "서울역 인근, 공부하기 좋은 카페")
  private String description;

  public static PlaceDto from(Place place) {
    if (place == null) {
      return null;
    }

    return PlaceDto.builder()
        .placeId(place.getId())
        .name(place.getName())
        .address(place.getAddress())
        .rating(place.getRating())
        .userRatingsTotal(place.getUserRatingsTotal())
        .photoUrls(place.getPhotoUrls())
        .description(place.getDescription())
        .build();
  }
}
