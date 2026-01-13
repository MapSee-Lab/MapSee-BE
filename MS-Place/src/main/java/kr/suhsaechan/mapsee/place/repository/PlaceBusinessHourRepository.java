package kr.suhsaechan.mapsee.place.repository;

import kr.suhsaechan.mapsee.place.entity.PlaceBusinessHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * PlaceBusinessHour 엔티티에 대한 Repository
 */
@Repository
public interface PlaceBusinessHourRepository extends JpaRepository<PlaceBusinessHour, UUID> {

  /**
   * Place ID로 영업시간 조회 (요일 순서대로)
   *
   * @param placeId 장소 ID
   * @return PlaceBusinessHour 리스트 (요일 순서)
   */
  @Query("SELECT pbh FROM PlaceBusinessHour pbh WHERE pbh.place.id = :placeId ORDER BY pbh.weekday ASC")
  List<PlaceBusinessHour> findByPlaceIdOrderByWeekday(@Param("placeId") UUID placeId);
}
