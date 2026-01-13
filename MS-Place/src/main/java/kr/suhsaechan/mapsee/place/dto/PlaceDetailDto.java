package kr.suhsaechan.mapsee.place.dto;

import kr.suhsaechan.mapsee.place.entity.Place;
import kr.suhsaechan.mapsee.place.entity.PlaceBusinessHour;
import kr.suhsaechan.mapsee.place.entity.PlaceMedia;
import kr.suhsaechan.mapsee.place.entity.PlacePlatformReference;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "장소 상세 정보")
public class PlaceDetailDto {

  @Schema(description = "장소 ID", example = "550e8400-e29b-41d4-a716-446655440000")
  private UUID id;

  @Schema(description = "장소명", example = "제주 카페 쿠모")
  private String name;

  @Schema(description = "주소", example = "제주특별자치도 제주시 애월읍")
  private String address;

  @Schema(description = "국가 코드 (ISO 3166-1 alpha-2)", example = "KR")
  private String country;

  @Schema(description = "위도", example = "33.4996213")
  private BigDecimal latitude;

  @Schema(description = "경도", example = "126.5311884")
  private BigDecimal longitude;

  @Schema(description = "업종", example = "카페")
  private String businessType;

  @Schema(description = "전화번호", example = "010-1234-5678")
  private String phone;

  @Schema(description = "장소 설명", example = "제주 바다를 바라보며 커피를 즐길 수 있는 카페")
  private String description;

  @Schema(description = "장소 유형 배열", example = "[\"cafe\", \"restaurant\"]")
  private List<String> types;

  @Schema(description = "영업 상태", example = "OPERATIONAL")
  private String businessStatus;

  @Schema(description = "Google 아이콘 URL", example = "https://maps.gstatic.com/mapfiles/place_api/icons/cafe-71.png")
  private String iconUrl;

  @Schema(description = "평점 (0.0 ~ 5.0)", example = "4.5")
  private BigDecimal rating;

  @Schema(description = "리뷰 수", example = "123")
  private Integer userRatingsTotal;

  @Schema(description = "사진 URL 배열", example = "[\"https://example.com/photo1.jpg\", \"https://example.com/photo2.jpg\"]")
  private List<String> photoUrls;

  @Schema(description = "플랫폼별 참조 정보 (Google Place ID 등)")
  private List<PlacePlatformReferenceDto> platformReferences;

  @Schema(description = "영업시간 목록")
  private List<PlaceBusinessHourDto> businessHours;

  @Schema(description = "추가 미디어 목록")
  private List<PlaceMediaDto> medias;

  public static PlaceDetailDto from(
      Place place,
      List<PlacePlatformReference> platformReferences,
      List<PlaceBusinessHour> businessHours,
      List<PlaceMedia> medias
  ) {
    if (place == null) {
      return null;
    }

    return PlaceDetailDto.builder()
        .id(place.getId())
        .name(place.getName())
        .address(place.getAddress())
        .country(place.getCountry())
        .latitude(place.getLatitude())
        .longitude(place.getLongitude())
        .businessType(place.getBusinessType())
        .phone(place.getPhone())
        .description(place.getDescription())
        .types(place.getTypes())
        .businessStatus(place.getBusinessStatus())
        .iconUrl(place.getIconUrl())
        .rating(place.getRating())
        .userRatingsTotal(place.getUserRatingsTotal())
        .photoUrls(place.getPhotoUrls())
        .platformReferences(platformReferences.stream()
            .map(PlacePlatformReferenceDto::from)
            .collect(Collectors.toList()))
        .businessHours(businessHours.stream()
            .map(PlaceBusinessHourDto::from)
            .collect(Collectors.toList()))
        .medias(medias.stream()
            .map(PlaceMediaDto::from)
            .collect(Collectors.toList()))
        .build();
  }
}
