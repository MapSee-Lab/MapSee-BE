package kr.suhsaechan.mapsee.place.service;

import kr.suhsaechan.mapsee.place.dto.GooglePlaceSearchDto;

/**
 * 플랫폼별 장소 검색 인터페이스
 * <p>
 * Google Places API를 통한 장소 검색 기능을 제공
 */
public interface PlacePlatformSearcher {

  /**
   * 상호명으로 장소 상세 정보 검색
   *
   * @param placeName 장소명
   * @param address   주소 (fallback용, 현재 미사용)
   * @param language  언어 코드 (ko, en, ja, zh)
   * @return 장소 상세 정보 (place_id, 좌표, 추가 정보 등)
   */
  GooglePlaceSearchDto.PlaceDetail searchPlaceDetail(String placeName, String address, String language);
}
