package kr.suhsaechan.mapsee.sns.entity;

import kr.suhsaechan.mapsee.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import kr.suhsaechan.mapsee.place.entity.Place;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;

@Entity
@Table(
    name = "content_place",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_content_place_pair", columnNames = {"content_id", "place_id"}),
        @UniqueConstraint(name = "uk_content_place_pos", columnNames = {"content_id", "position"})
    },
    indexes = {
        @Index(name = "idx_content_place_content", columnList = "content_id"),
        @Index(name = "idx_content_place_place", columnList = "place_id")
    }
)
@Builder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContentPlace extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Content content;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Place place;

  @Column(nullable = false)
  @Builder.Default
  private int position = 0;
}
