package kr.suhsaechan.mapsy.place.repository;

import kr.suhsaechan.mapsy.place.entity.Keyword;
import kr.suhsaechan.mapsy.place.entity.Place;
import kr.suhsaechan.mapsy.place.entity.PlaceKeyword;
import kr.suhsaechan.mapsy.place.entity.PlaceKeyword.PlaceKeywordId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PlaceKeyword 엔티티에 대한 Repository
 */
@Repository
public interface PlaceKeywordRepository extends JpaRepository<PlaceKeyword, PlaceKeywordId> {

  /**
   * 특정 장소의 모든 키워드 조회
   *
   * @param place 장소
   * @return List<PlaceKeyword>
   */
  List<PlaceKeyword> findByPlace(Place place);

  /**
   * 특정 키워드가 연결된 모든 장소 조회
   *
   * @param keyword 키워드
   * @return List<PlaceKeyword>
   */
  List<PlaceKeyword> findByKeyword(Keyword keyword);

  /**
   * 특정 키워드가 연결된 장소 수 조회
   * - 키워드의 인기도 계산에 사용
   *
   * @param keyword 키워드
   * @return 장소 수
   */
  long countByKeyword(Keyword keyword);

  /**
   * 특정 장소와 키워드의 연결 존재 여부 확인
   *
   * @param place   장소
   * @param keyword 키워드
   * @return 존재 여부
   */
  boolean existsByPlaceAndKeyword(Place place, Keyword keyword);

  /**
   * 특정 키워드로 장소 검색 (Place 직접 조회)
   * - N+1 문제 방지를 위한 fetch join
   *
   * @param keyword 키워드
   * @return List<Place>
   */
  @Query("""
    SELECT pk.place FROM PlaceKeyword pk
    JOIN FETCH pk.place p
    WHERE pk.keyword = :keyword
    AND p.isDeleted = false
    ORDER BY p.createdAt DESC
    """)
  List<Place> findPlacesByKeyword(@Param("keyword") Keyword keyword);

  /**
   * 특정 키워드 목록으로 장소 검색 (여러 키워드 OR 조건)
   *
   * @param keywords 키워드 목록
   * @return List<Place>
   */
  @Query("""
    SELECT DISTINCT pk.place FROM PlaceKeyword pk
    JOIN FETCH pk.place p
    WHERE pk.keyword IN :keywords
    AND p.isDeleted = false
    ORDER BY p.createdAt DESC
    """)
  List<Place> findPlacesByKeywords(@Param("keywords") List<Keyword> keywords);

  /**
   * 특정 장소의 키워드 모두 삭제
   * - 장소 삭제 시 자동 호출됨 (CascadeType.ALL)
   *
   * @param place 장소
   */
  void deleteByPlace(Place place);

  /**
   * 특정 키워드와 연결된 모든 PlaceKeyword 삭제
   * - 키워드 삭제 시 자동 호출됨 (CascadeType.ALL)
   *
   * @param keyword 키워드
   */
  void deleteByKeyword(Keyword keyword);
}
