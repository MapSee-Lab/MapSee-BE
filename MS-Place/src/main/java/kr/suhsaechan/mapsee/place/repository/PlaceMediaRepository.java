package kr.suhsaechan.mapsee.place.repository;

import kr.suhsaechan.mapsee.place.entity.PlaceMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * PlaceMedia 엔티티에 대한 Repository
 */
@Repository
public interface PlaceMediaRepository extends JpaRepository<PlaceMedia, UUID> {

  /**
   * Place ID로 미디어 조회 (position 순서대로)
   *
   * @param placeId 장소 ID
   * @return PlaceMedia 리스트 (position 순서)
   */
  @Query("SELECT pm FROM PlaceMedia pm WHERE pm.place.id = :placeId ORDER BY pm.position ASC")
  List<PlaceMedia> findByPlaceIdOrderByPosition(@Param("placeId") UUID placeId);
}
