package kr.suhsaechan.mapsee.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 장소 검색 API 연동을 위한 설정 정보
 * application-dev.yml의 place 설정을 매핑합니다.
 */
@Component
@Getter
@Setter
public class PlaceProperties {

  /**
   * Google Places API Key
   * 환경변수: GOOGLE_PLACES_API_KEY
   */
  @Value("${place.google.api-key:}")
  private String googleApiKey;

  /**
   * Kakao Places API Key
   * 환경변수: KAKAO_PLACES_API_KEY
   */
  @Value("${place.kakao.api-key:}")
  private String kakaoApiKey;

  /**
   * Naver Places API Key
   * 환경변수: NAVER_PLACES_API_KEY
   */
  @Value("${place.naver.api-key:}")
  private String naverApiKey;
}

