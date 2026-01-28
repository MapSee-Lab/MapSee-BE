package kr.suhsaechan.mapsy.place.repository;

import kr.suhsaechan.mapsy.place.constant.PlacePlatform;
import kr.suhsaechan.mapsy.place.entity.Place;
import kr.suhsaechan.mapsy.place.entity.PlacePlatformReference;
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

  /**
   * 플랫폼과 플랫폼 ID로 PlacePlatformReference 조회
   * - AI 콜백에서 네이버 placeId로 중복 체크 시 사용
   *
   * @param placePlatform   플랫폼 (NAVER, GOOGLE, KAKAO)
   * @param placePlatformId 플랫폼별 장소 ID
   * @return Optional<PlacePlatformReference>
   */
  Optional<PlacePlatformReference> findByPlacePlatformAndPlacePlatformId(
      PlacePlatform placePlatform,
      String placePlatformId
  );
}
