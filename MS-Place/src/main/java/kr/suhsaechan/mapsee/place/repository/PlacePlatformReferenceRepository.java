package kr.suhsaechan.mapsee.place.repository;

import kr.suhsaechan.mapsee.place.constant.PlacePlatform;
import kr.suhsaechan.mapsee.place.entity.Place;
import kr.suhsaechan.mapsee.place.entity.PlacePlatformReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PlacePlatformReference 엔티티에 대한 Repository
 */
@Repository
public interface PlacePlatformReferenceRepository extends JpaRepository<PlacePlatformReference, UUID> {

  /**
   * Place와 플랫폼으로 PlacePlatformReference 조회
   *
   * @param place         장소
   * @param placePlatform 플랫폼 (GOOGLE, KAKAO, NAVER)
   * @return Optional<PlacePlatformReference>
   */
  Optional<PlacePlatformReference> findByPlaceAndPlacePlatform(Place place, PlacePlatform placePlatform);

  /**
   * Place에 연결된 모든 PlacePlatformReference 조회
   *
   * @param place 장소
   * @return PlacePlatformReference 리스트
   */
  List<PlacePlatformReference> findByPlace(Place place);
}
