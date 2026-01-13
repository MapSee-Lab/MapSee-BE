package kr.suhsaechan.mapsee.place.service;

import kr.suhsaechan.mapsee.place.dto.GooglePlaceSearchDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 장소 검색 서비스
 * <p>
 * Google Places API를 통한 장소 검색 기능을 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceSearchService {

  private final GooglePlaceSearcher googlePlaceSearcher;

  /**
   * Google 플랫폼으로 장소 검색
   *
   * @param placeName 장소명
   * @param address   주소
   * @param language  언어 코드
   * @return 장소 상세 정보
   */
  public GooglePlaceSearchDto.PlaceDetail searchGooglePlace(
      String placeName,
      String address,
      String language) {

    return googlePlaceSearcher.searchPlaceDetail(placeName, address, language);
  }
}
