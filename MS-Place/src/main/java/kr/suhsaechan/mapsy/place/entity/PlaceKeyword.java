package kr.suhsaechan.mapsy.place.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PlaceKeyword 엔티티
 * - Place와 Keyword 간의 M:N 관계를 표현하는 중간 테이블
 * - 복합키(Composite Key) 사용
 */
@Entity
@Table(name = "place_keywords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PlaceKeyword {

  @EmbeddedId
  private PlaceKeywordId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("placeId")
  @JoinColumn(name = "place_id")
  private Place place;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("keywordId")
  @JoinColumn(name = "keyword_id")
  private Keyword keyword;

  /**
   * PlaceKeyword 생성 편의 메서드
   *
   * @param place   장소
   * @param keyword 키워드
   * @return PlaceKeyword 엔티티
   */
  public static PlaceKeyword of(Place place, Keyword keyword) {
    PlaceKeywordId id = new PlaceKeywordId(place.getId(), keyword.getId());
    return PlaceKeyword.builder()
        .id(id)
        .place(place)
        .keyword(keyword)
        .build();
  }

  /**
   * PlaceKeyword 복합키
   * - placeId, keywordId 조합으로 유일성 보장
   */
  @Embeddable
  @Getter
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  @AllArgsConstructor
  @Builder
  public static class PlaceKeywordId implements Serializable {

    private UUID placeId;
    private UUID keywordId;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      PlaceKeywordId that = (PlaceKeywordId) o;
      return Objects.equals(placeId, that.placeId)
          && Objects.equals(keywordId, that.keywordId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(placeId, keywordId);
    }
  }
}
