package kr.suhsaechan.mapsee.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 서버 연동을 위한 설정 정보
 * application.yml의 ai.server 설정을 매핑합니다.
 */
@Component
@ConfigurationProperties(prefix = "ai.server")
@Getter
@Setter
public class AiServerProperties {

  /**
   * AI 서버 Base URL
   * 예: https://ai.tripgether.suhsaechan.kr
   */
  private String baseUrl;

  /**
   * 백엔드 → AI 서버 요청 시 사용할 API Key
   * Header: X-API-Key
   */
  private String apiKey;

  /**
   * AI 서버 → 백엔드 Callback 시 검증할 API Key
   * Header: X-API-Key
   */
  private String callbackApiKey;

  /**
   * 장소 추출 요청 엔드포인트
   * 기본값: /api/extract-places
   */
  private String extractPlacesUri = "/api/extract-places";
}
