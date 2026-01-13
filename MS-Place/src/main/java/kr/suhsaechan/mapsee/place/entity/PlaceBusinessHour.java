package kr.suhsaechan.mapsee.place.entity;

import kr.suhsaechan.mapsee.common.entity.BaseEntity;
import kr.suhsaechan.mapsee.place.constant.PlaceWeekday;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "place_business_hour",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_place_weekday", columnNames = {"place_id", "weekday"})
    },
    indexes = {
        @Index(name = "idx_business_hour_place", columnList = "place_id")
    }
)
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlaceBusinessHour extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Place place;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PlaceWeekday weekday;

  @Column(nullable = false)
  private LocalTime openTime;

  @Column(nullable = false)
  private LocalTime closeTime;

}
