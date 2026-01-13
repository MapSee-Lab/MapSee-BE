package kr.suhsaechan.mapsee.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.suhsaechan.mapsee.ai.dto.PlaceExtractionRequest;
import kr.suhsaechan.mapsee.ai.dto.PlaceExtractionResponse;
import kr.suhsaechan.mapsee.common.exception.CustomException;
import kr.suhsaechan.mapsee.common.exception.constant.ErrorCode;
import kr.suhsaechan.mapsee.common.properties.AiServerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * AI 서버와의 통신을 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiServerService {

  private final OkHttpClient okHttpClient;
  private final ObjectMapper objectMapper;
  private final AiServerProperties aiServerProperties;

  /**
   * AI 서버에 장소 추출 요청을 전송합니다.
   * 비동기 요청이며, AI 서버는 202 Accepted를 즉시 반환합니다.
   * 실제 결과는 Webhook Callback으로 수신됩니다.
   *
   * @param contentId Content UUID
   * @param snsUrl    분석할 SNS URL
   * @return AI 서버 응답 (202 Accepted)
   */
  public PlaceExtractionResponse sendPlaceExtractionRequest(UUID contentId, String snsUrl) {
    String aiServerPlaceExtractionUrl = aiServerProperties.getBaseUrl() + aiServerProperties.getExtractPlacesUri();

    PlaceExtractionRequest aiContentRequest = PlaceExtractionRequest.builder()
            .contentId(contentId)
            .snsUrl(snsUrl)
            .build();

    log.info("Requesting place extraction to AI server: contentId={}, snsUrl={}", contentId, snsUrl);

    try {
      // JSON 직렬화
      String jsonBody = objectMapper.writeValueAsString(aiContentRequest);
      RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json"));

      // OkHttp 요청 생성
      Request request = new Request.Builder()
          .url(aiServerPlaceExtractionUrl)
          .addHeader("X-API-Key", aiServerProperties.getApiKey())
          .addHeader("Content-Type", "application/json")
          .addHeader("Accept", "application/json")
          .post(body)
          .build();

      // OkHttp로 POST 요청 실행
      try (Response httpResponse = okHttpClient.newCall(request).execute()) {
        if (!httpResponse.isSuccessful()) {
          log.error("AI server HTTP error: code={}", httpResponse.code());
          throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }

        if (httpResponse.body() == null) {
          log.error("AI server response body is null");
          throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }

        String responseBody = httpResponse.body().string();
        log.info("AI server raw response: contentId={}, httpStatus={}, responseBody={}",
            contentId, httpResponse.code(), responseBody);

        PlaceExtractionResponse response = objectMapper.readValue(responseBody, PlaceExtractionResponse.class);

        log.info("AI server accepted the request: contentId={}, received={}, status={}",
            contentId, response.getReceived(), response.getStatus());

        return response;
      }

    } catch (CustomException e) {
      log.error("AI server error: contentId={}, error={}", contentId, e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error during AI server call: contentId={}", contentId, e);
      throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
    }
  }
}
