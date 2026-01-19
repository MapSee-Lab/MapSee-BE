package kr.suhsaechan.mapsy.place.repository;

import kr.suhsaechan.mapsy.place.entity.Keyword;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Keyword 엔티티에 대한 Repository
 */
@Repository
public interface KeywordRepository extends JpaRepository<Keyword, UUID> {

  /**
   * 키워드 문자열로 조회
   *
   * @param keyword 키워드 문자열
   * @return Optional<Keyword>
   */
  Optional<Keyword> findByKeyword(String keyword);

  /**
   * 트렌드 점수 기준 상위 키워드 조회
   * - 트렌드 점수가 높은 순서대로 정렬
   *
   * @param pageable 페이징 정보
   * @return Page<Keyword>
   */
  @Query("""
    SELECT k FROM Keyword k
    ORDER BY k.trendScore DESC, k.count DESC
    """)
  Page<Keyword> findTopByTrendScore(Pageable pageable);

  /**
   * 사용 횟수 기준 상위 키워드 조회
   * - count가 높은 순서대로 정렬
   *
   * @param pageable 페이징 정보
   * @return Page<Keyword>
   */
  Page<Keyword> findAllByOrderByCountDesc(Pageable pageable);

  /**
   * 키워드 검색 (자동완성용)
   * - 키워드가 특정 문자열로 시작하는 경우
   * - 사용 횟수가 높은 순서대로 정렬
   *
   * @param prefix   키워드 시작 문자열
   * @param pageable 페이징 정보
   * @return List<Keyword>
   */
  @Query("""
    SELECT k FROM Keyword k
    WHERE k.keyword LIKE :prefix%
    ORDER BY k.count DESC
    """)
  List<Keyword> findByKeywordStartingWithOrderByCountDesc(
      @Param("prefix") String prefix,
      Pageable pageable
  );

  /**
   * 키워드 포함 검색 (자동완성용)
   * - 키워드에 특정 문자열이 포함된 경우
   * - 사용 횟수가 높은 순서대로 정렬
   *
   * @param term     검색어
   * @param pageable 페이징 정보
   * @return List<Keyword>
   */
  @Query("""
    SELECT k FROM Keyword k
    WHERE k.keyword LIKE %:term%
    ORDER BY k.count DESC
    """)
  List<Keyword> findByKeywordContainingOrderByCountDesc(
      @Param("term") String term,
      Pageable pageable
  );

  /**
   * 특정 키워드 목록 조회 (배치 조회)
   *
   * @param keywords 키워드 문자열 목록
   * @return List<Keyword>
   */
  List<Keyword> findByKeywordIn(List<String> keywords);
}
