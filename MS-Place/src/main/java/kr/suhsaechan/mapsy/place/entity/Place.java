package kr.suhsaechan.mapsy.place.entity;

import kr.suhsaechan.mapsy.common.entity.SoftDeletableBaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.Check;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Check(constraints = "latitude BETWEEN -90 AND 90 AND longitude BETWEEN -180 AND 180")
@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Place extends SoftDeletableBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(length = 500)
  private String address;

  @Column(length = 2, nullable = false)
  @Builder.Default
  private String country = "KR";    // 국가 코드 (ISO 3166-1 alpha-2) - 기본값 KR

  @Column(nullable = false, precision = 10, scale = 7)
  @DecimalMin("-90.0")
  @DecimalMax("90.0")
  private BigDecimal latitude;      // 위도

  @Column(nullable = false, precision = 10, scale = 7)
  @DecimalMin("-180.0")
  @DecimalMax("180.0")
  private BigDecimal longitude;     // 경도

  @Column(length = 100)
  private String businessType;      // 업종 (category 매핑)

  @Column(length = 50)
  private String phone;

  @Column(length = 500)
  private String openingHours;      // 영업시간 (레거시, businessHours로 대체)

  @Column(columnDefinition = "TEXT")
  private String description;       // 요약 설명

  // Google Places API 추가 정보 (레거시)
  @Column(columnDefinition = "varchar(50)[]")
  @JdbcTypeCode(SqlTypes.ARRAY)
  private List<String> types;       // 장소 유형 배열 (restaurant, cafe, park 등)

  @Column(length = 30)
  private String businessStatus;    // 영업 상태 (영업 중, 영업 종료 등)

  @Column(length = 500)
  private String iconUrl;           // Google 아이콘 URL (레거시)

  @Column(precision = 3, scale = 2)
  @DecimalMin("0.0")
  @DecimalMax("5.0")
  private BigDecimal rating;        // 평점 (0.0 ~ 5.0)

  @Column
  private Integer userRatingsTotal; // 리뷰 수 (레거시, visitorReviewCount로 대체)

  @Column(columnDefinition = "text[]")
  @JdbcTypeCode(SqlTypes.ARRAY)
  private List<String> photoUrls;   // 사진 URL 배열 (최대 10개)

  // ========== 신규 필드 (AI 콜백 #16) ==========

  @Column(length = 500)
  private String roadAddress;       // 도로명 주소

  @Column
  private Integer visitorReviewCount;   // 방문자 리뷰 수

  @Column
  private Integer blogReviewCount;      // 블로그 리뷰 수

  @Column(length = 500)
  private String businessHours;         // 영업 시간 요약

  @Column(columnDefinition = "varchar(100)[]")
  @JdbcTypeCode(SqlTypes.ARRAY)
  private List<String> openHoursDetail; // 요일별 상세 영업시간

  @Column(length = 200)
  private String holidayInfo;           // 휴무일 정보

  @Column(length = 500)
  private String homepageUrl;           // 홈페이지 URL

  @Column(length = 500)
  private String naverMapUrl;           // 네이버 지도 URL

  @Column
  private Boolean reservationAvailable; // 예약 가능 여부

  @Column(length = 200)
  private String subwayInfo;            // 지하철 정보

  @Column(columnDefinition = "TEXT")
  private String directionsText;        // 찾아가는 길

  @Column(columnDefinition = "varchar(100)[]")
  @JdbcTypeCode(SqlTypes.ARRAY)
  private List<String> amenities;       // 편의시설 목록

  @Column(columnDefinition = "varchar(200)[]")
  @JdbcTypeCode(SqlTypes.ARRAY)
  private List<String> tvAppearances;   // TV 방송 출연 정보

  @Column(columnDefinition = "varchar(200)[]")
  @JdbcTypeCode(SqlTypes.ARRAY)
  private List<String> menuInfo;        // 대표 메뉴

  @Column(length = 500)
  private String imageUrl;              // 대표 이미지 URL

  /**
   * 이 장소와 연결된 키워드 목록
   * - PlaceKeyword 중간 테이블을 통한 다대다 관계
   */
  @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<PlaceKeyword> placeKeywords = new ArrayList<>();

  /**
   * 키워드 추가
   * - PlaceKeyword 연결 생성 및 양방향 관계 설정
   *
   * @param keyword 추가할 키워드
   */
  public void addKeyword(Keyword keyword) {
    PlaceKeyword placeKeyword = PlaceKeyword.of(this, keyword);
    this.placeKeywords.add(placeKeyword);
    keyword.getPlaceKeywords().add(placeKeyword);
  }

  /**
   * 키워드 제거
   *
   * @param keyword 제거할 키워드
   */
  public void removeKeyword(Keyword keyword) {
    this.placeKeywords.removeIf(pk -> pk.getKeyword().equals(keyword));
    keyword.getPlaceKeywords().removeIf(pk -> pk.getPlace().equals(this));
  }
}
