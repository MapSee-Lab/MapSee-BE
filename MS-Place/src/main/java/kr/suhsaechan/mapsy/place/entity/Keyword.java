package kr.suhsaechan.mapsy.place.entity;

import kr.suhsaechan.mapsy.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Keyword 엔티티
 * - Instagram 게시물이나 장소 설명에서 추출된 키워드/해시태그 관리
 * - 트렌드 점수를 통해 인기 키워드 분석 가능
 * - Place와 다대다 관계 (PlaceKeyword 중간 테이블)
 */
@Entity
@Table(name = "keywords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Keyword extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  /**
   * 키워드 문자열
   * - 해시태그 형태 (#강남카페) 또는 일반 텍스트 (강남카페)
   * - unique 제약조건으로 중복 방지
   */
  @Column(unique = true, nullable = false, length = 100)
  private String keyword;

  /**
   * 키워드 사용 횟수
   * - 장소에 연결될 때마다 증가
   * - 트렌드 점수 계산에 사용
   */
  @Column(nullable = false)
  @Builder.Default
  private Integer count = 1;

  /**
   * 트렌드 점수
   * - 급상승률 기반 계산: (현재 count - 이전 count) / 이전 count
   * - 배치 작업으로 주기적 업데이트 (예: 매시간)
   * - 높을수록 최근 급상승 중인 키워드
   */
  @Column(precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal trendScore = BigDecimal.ZERO;

  /**
   * 이 키워드가 연결된 장소 목록
   * - PlaceKeyword 중간 테이블을 통한 다대다 관계
   */
  @OneToMany(mappedBy = "keyword")
  @Builder.Default
  private List<PlaceKeyword> placeKeywords = new ArrayList<>();

  /**
   * 키워드 사용 횟수 증가
   * - 장소에 키워드 연결 시 호출
   */
  public void incrementCount() {
    this.count++;
  }

  /**
   * 트렌드 점수 업데이트
   * - 배치 작업에서 호출
   *
   * @param newTrendScore 새로운 트렌드 점수
   */
  public void updateTrendScore(BigDecimal newTrendScore) {
    this.trendScore = newTrendScore;
  }
}
