package kr.suhsaechan.mapsee.place.repository;

import kr.suhsaechan.mapsee.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Place 엔티티에 대한 Repository
 */
@Repository
public interface PlaceRepository extends JpaRepository<Place, UUID> {

  /**
   * 이름과 좌표로 장소 조회 (중복 방지용)
   *
   * @param name      장소명
   * @param latitude  위도
   * @param longitude 경도
   * @return Optional<Place>
   */
  Optional<Place> findByNameAndLatitudeAndLongitude(String name, BigDecimal latitude, BigDecimal longitude);

  /**
   * 이름과 주소로 Place 검색 (DB 캐싱용, Google API 호출 최소화)
   * <p>
   * name과 address를 모두 trim, lowercase 처리하여 검색
   * address는 부분 매칭(LIKE) 사용
   *
   * @param name    장소명
   * @param address 주소 (null 가능)
   * @return Optional<Place> (가장 최근 생성된 장소)
   */
  @Query("""
    SELECT p FROM Place p
    WHERE LOWER(p.name) = LOWER(:name)
    AND (:address IS NULL OR LOWER(p.address) LIKE LOWER(CONCAT('%', :address, '%')))
    ORDER BY p.createdAt DESC
    """)
  Optional<Place> findByNormalizedNameAndAddress(
          @Param("name") String name,
          @Param("address") String address
  );
}
