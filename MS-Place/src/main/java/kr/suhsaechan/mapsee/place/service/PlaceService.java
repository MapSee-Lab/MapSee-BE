package kr.suhsaechan.mapsee.place.service;

import kr.suhsaechan.mapsee.common.exception.CustomException;
import kr.suhsaechan.mapsee.common.exception.constant.ErrorCode;
import kr.suhsaechan.mapsee.place.dto.PlaceDetailDto;
import kr.suhsaechan.mapsee.place.entity.Place;
import kr.suhsaechan.mapsee.place.entity.PlaceBusinessHour;
import kr.suhsaechan.mapsee.place.entity.PlaceMedia;
import kr.suhsaechan.mapsee.place.entity.PlacePlatformReference;
import kr.suhsaechan.mapsee.place.repository.PlaceBusinessHourRepository;
import kr.suhsaechan.mapsee.place.repository.PlaceMediaRepository;
import kr.suhsaechan.mapsee.place.repository.PlacePlatformReferenceRepository;
import kr.suhsaechan.mapsee.place.repository.PlaceRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceService {

  private final PlaceRepository placeRepository;
  private final PlacePlatformReferenceRepository placePlatformReferenceRepository;
  private final PlaceBusinessHourRepository placeBusinessHourRepository;
  private final PlaceMediaRepository placeMediaRepository;

  /**
   * 장소 상세 정보 조회
   * - Place 기본 정보
   * - 플랫폼별 참조 정보 (Google Place ID 등)
   * - 영업시간
   * - 추가 미디어
   *
   * @param placeId 조회할 장소 ID
   * @return 장소 상세 정보 DTO
   */
  @Transactional(readOnly = true)
  public PlaceDetailDto getPlaceDetail(UUID placeId) {
    // 1. Place 조회
    Place place = placeRepository.findById(placeId)
        .orElseThrow(() -> {
          log.error("Place not found: placeId={}", placeId);
          return new CustomException(ErrorCode.PLACE_NOT_FOUND);
        });

    log.info("Place found: placeId={}, name={}", placeId, place.getName());

    // 2. PlacePlatformReference 조회 (Google Place ID 등)
    List<PlacePlatformReference> platformReferences =
        placePlatformReferenceRepository.findByPlace(place);

    // 3. PlaceBusinessHour 조회 (요일 순서)
    List<PlaceBusinessHour> businessHours =
        placeBusinessHourRepository.findByPlaceIdOrderByWeekday(placeId);

    // 4. PlaceMedia 조회 (position 순서)
    List<PlaceMedia> medias =
        placeMediaRepository.findByPlaceIdOrderByPosition(placeId);

    log.info("Found {} platform references, {} business hours, {} medias for placeId={}",
        platformReferences.size(), businessHours.size(), medias.size(), placeId);

    // 5. DTO 변환 후 반환
    return PlaceDetailDto.from(place, platformReferences, businessHours, medias);
  }
}
