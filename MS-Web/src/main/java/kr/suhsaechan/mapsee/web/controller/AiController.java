package kr.suhsaechan.mapsee.web.controller;

import kr.suhsaechan.mapsee.ai.dto.AiCallbackRequest;
import kr.suhsaechan.mapsee.ai.dto.AiCallbackResponse;
import kr.suhsaechan.mapsee.common.exception.CustomException;
import kr.suhsaechan.mapsee.common.exception.constant.ErrorCode;
import kr.suhsaechan.mapsee.common.properties.AiServerProperties;
import kr.suhsaechan.mapsee.common.util.CommonUtil;
import kr.suhsaechan.mapsee.sns.service.AiCallbackService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI 서버 Webhook Callback을 처리하는 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/ai")
@Tag(name = "AI 서버 API", description = "AI 서버 연동 관련 API 제공")
public class AiController implements AiControllerDocs {

  private final AiCallbackService aiCallbackService;
  private final AiServerProperties aiServerProperties;
  private final CommonUtil commonUtil;

  @PostMapping("/callback")
  @Override
  public ResponseEntity<AiCallbackResponse> handleCallback(
      @RequestHeader(value = "X-API-Key", required = true) String apiKey,
      @RequestBody AiCallbackRequest request) {

    // API Key 검증
    if (!aiServerProperties.getCallbackApiKey().equals(apiKey)) {
      log.error("Invalid API Key from AI server. Expected: {}, Received: {}",
              commonUtil.maskSecureString(aiServerProperties.getCallbackApiKey()),
              commonUtil.maskSecureString(apiKey));
      throw new CustomException(ErrorCode.INVALID_API_KEY);
    }

    return ResponseEntity.ok(aiCallbackService.processAiServerCallback(request));
  }
}
