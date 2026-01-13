package kr.suhsaechan.mapsee.place.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import lombok.ToString;

/**
 * Google Places API 응답 DTO
 */
@Getter
@ToString
@NoArgsConstructor
public class GooglePlaceSearchDto {

  private List<Candidate> candidates;
  private String status;

  /**
   * Google Places API 검색 결과
   */
  @Getter
  @NoArgsConstructor
  public static class Candidate {

    @JsonProperty("place_id")
    private String placeId;

    private String name;

    @JsonProperty("formatted_address")
    private String formattedAddress;

    private Geometry geometry;

    private List<String> types;

    @JsonProperty("business_status")
    private String businessStatus;

    private String icon;

    private List<Photo> photos;

    private BigDecimal rating;

    @JsonProperty("user_ratings_total")
    private Integer userRatingsTotal;
  }

  /**
   * 좌표 정보
   */
  @Getter
  @NoArgsConstructor
  public static class Geometry {
    private Location location;
  }

  /**
   * 위도/경도
   */
  @Getter
  @NoArgsConstructor
  public static class Location {
    private BigDecimal lat;
    private BigDecimal lng;
  }

  /**
   * 사진 정보
   */
  @Getter
  @NoArgsConstructor
  public static class Photo {
    @JsonProperty("photo_reference")
    private String photoReference;

    private Integer height;
    private Integer width;

    @JsonProperty("html_attributions")
    private List<String> htmlAttributions;
  }

  /**
   * Service 레이어에서 사용할 장소 상세 정보
   */
  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PlaceDetail {
    private String placeId;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String country;

    // 추가 정보
    private List<String> types;
    private String businessStatus;
    private String iconUrl;
    private BigDecimal rating;
    private Integer userRatingsTotal;
    private List<String> photoUrls;
  }
}
