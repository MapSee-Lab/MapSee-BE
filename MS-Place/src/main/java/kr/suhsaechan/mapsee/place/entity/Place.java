package kr.suhsaechan.mapsee.place.entity;

import kr.suhsaechan.mapsee.common.entity.SoftDeletableBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
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
  private String country;         //국가 코드 (ISO 3166-1 alpha-2: KR, US, JP, CN 등)

  @Column(nullable = false, precision = 10, scale = 7)
  @DecimalMin("-90.0")
  @DecimalMax("90.0")
  private BigDecimal latitude;    //위도

  @Column(nullable = false, precision = 10, scale = 7)
  @DecimalMin("-180.0")
  @DecimalMax("180.0")
  private BigDecimal longitude;   //경도

  @Column(length = 100)
  private String businessType;    //업종

  @Column(length = 50)
  private String phone;

  @Column(columnDefinition = "TEXT")
  private String description;     //요약 설명

  // Google Places API 추가 정보
  @Column(columnDefinition = "varchar(50)[]")
  @JdbcTypeCode(SqlTypes.ARRAY)
  private List<String> types;     //장소 유형 배열 (restaurant, cafe, park 등)

  @Column(length = 30)
  private String businessStatus;  //영업 상태 (OPERATIONAL, CLOSED_TEMPORARILY, CLOSED_PERMANENTLY)

  @Column(length = 500)
  private String iconUrl;         //Google 아이콘 URL

  @Column(precision = 2, scale = 1)
  private BigDecimal rating;      //평점 (0.0 ~ 5.0)

  @Column
  private Integer userRatingsTotal; //리뷰 수

  @Column(columnDefinition = "text[]")
  @JdbcTypeCode(SqlTypes.ARRAY)
  private List<String> photoUrls; //사진 URL 배열 (최대 10개)

}
