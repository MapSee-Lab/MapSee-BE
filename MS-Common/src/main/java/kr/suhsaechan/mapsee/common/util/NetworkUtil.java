package kr.suhsaechan.mapsee.common.util;

import static me.suhsaechan.suhlogger.util.SuhLogger.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.suhsaechan.mapsee.common.exception.CustomException;
import kr.suhsaechan.mapsee.common.exception.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.suhsaechan.suhlogger.util.SuhLogger;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * HTTP 통신을 위한 유틸리티 클래스
 * WebClient를 활용하여 외부 API 호출을 수행합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NetworkUtil {

  private final WebClient webClient;
  private final ObjectMapper objectMapper;

  /**
   * POST 요청을 전송합니다.
   *
   * @param url          요청 URL
   * @param requestBody  요청 본문
   * @param headers      HTTP 헤더 (nullable)
   * @param responseType 응답 타입 클래스
   * @param <T>          응답 타입
   * @return 응답 객체
   * @throws CustomException 네트워크 오류 또는 API 오류 발생 시
   */
  public <T> T sendPostRequest(String url, Object requestBody,
                                Map<String, String> headers,
                                Class<T> responseType) {
    try {
      log.debug("Sending POST request to: {}", url);
      log.debug("Request body: {}", requestBody);

      return webClient.post()
          .uri(url)
          .headers(httpHeaders -> {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            if (headers != null) {
              headers.forEach(httpHeaders::set);
            }
          })
          .bodyValue(requestBody)
          .retrieve()
          .onStatus(HttpStatusCode::is4xxClientError, response -> {
            log.error("Client error occurred: status={}", response.statusCode());
            return Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR));
          })
          .onStatus(HttpStatusCode::is5xxServerError, response -> {
            log.error("Server error occurred: status={}", response.statusCode());
            return Mono.error(new CustomException(ErrorCode.AI_SERVER_ERROR));
          })
          .bodyToMono(responseType)
          .block();

    } catch (WebClientException e) {
      log.error("WebClient error: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.NETWORK_ERROR);
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error during HTTP request: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * GET 요청을 전송합니다.
   * RAW JSON 응답을 먼저 로깅한 후 파싱합니다.
   *
   * @param url          요청 URL
   * @param headers      HTTP 헤더 (nullable)
   * @param responseType 응답 타입 클래스
   * @param <T>          응답 타입
   * @return 응답 객체
   * @throws CustomException 네트워크 오류 또는 API 오류 발생 시
   */
  public <T> T sendGetRequest(String url, Map<String, String> headers,
                               Class<T> responseType) {
    try {
      lineLog(null);
      log.info("WebClient GET Request Start");
      log.info("URL: {}", url);
      log.info("Headers: {}", headers != null ? headers : "none");
      log.info("Response Type: {}", responseType.getSimpleName());
      lineLog(null);

      // 1단계: RAW JSON String으로 먼저 받기
      String rawJsonResponse = webClient.get()
          .uri(url)
          .headers(httpHeaders -> {
            if (headers != null) {
              headers.forEach(httpHeaders::set);
            }
          })
          .retrieve()
          .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
              clientResponse.bodyToMono(String.class)
                  .flatMap(errorBody -> {
                    lineLog(null);
                    log.error("Client Error (4xx) Occurred");
                    log.error("Status Code: {}", clientResponse.statusCode());
                    log.error("RAW Error Body: {}", errorBody);
                    lineLog(null);
                    return Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR));
                  })
          )
          .onStatus(HttpStatusCode::is5xxServerError, serverResponse ->
              serverResponse.bodyToMono(String.class)
                  .flatMap(errorBody -> {
                    lineLog(null);
                    log.error("Server Error (5xx) Occurred");
                    log.error("Status Code: {}", serverResponse.statusCode());
                    log.error("RAW Error Body: {}", errorBody);
                    lineLog(null);
                    return Mono.error(new CustomException(ErrorCode.AI_SERVER_ERROR));
                  })
          )
          .bodyToMono(String.class)  // 먼저 String으로 받기
          .block();

      // 2단계: RAW JSON 로깅
      lineLog(null);
      log.info("RAW JSON Response Received:");
      log.info("{}", rawJsonResponse);
      lineLog(null);

      // 3단계: ObjectMapper로 수동 파싱
      T parsedResponse;
      try {
        log.info("Parsing RAW JSON to {}", responseType.getSimpleName());
        parsedResponse = objectMapper.readValue(rawJsonResponse, responseType);
        log.info("Parsing Successful");
        log.info("Parsed Response:");
        lineLogDebug("PARSED_RESPONSE");
        superLogDebug(parsedResponse);
      } catch (Exception parseException) {
        log.error("JSON Parsing Failed!");
        log.error("Exception: {}", parseException.getMessage(), parseException);
        log.error("RAW JSON that failed to parse:");
        log.error("{}", rawJsonResponse);
        throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
      }

      log.info("WebClient GET Request End - Success");
      return parsedResponse;

    } catch (WebClientException e) {
      lineLog(null);
      log.error("WebClient Exception");
      log.error("Message: {}", e.getMessage(), e);
      lineLog(null);
      throw new CustomException(ErrorCode.NETWORK_ERROR);
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      lineLog(null);
      log.error("Unexpected Exception");
      log.error("Message: {}", e.getMessage(), e);
      lineLog(null);
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }
}
