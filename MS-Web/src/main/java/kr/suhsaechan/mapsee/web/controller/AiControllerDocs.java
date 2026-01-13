package kr.suhsaechan.mapsee.web.controller;

import kr.suhsaechan.mapsee.ai.dto.AiCallbackRequest;
import kr.suhsaechan.mapsee.ai.dto.AiCallbackResponse;
import kr.suhsaechan.mapsee.common.constant.Author;
import io.swagger.v3.oas.annotations.Operation;
import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import org.springframework.http.ResponseEntity;

public interface AiControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.01.15", author = Author.SUHSAECHAN, issueNumber = 129, description = "AI 서버 Callback API ContentInfo 파라미터 추가 (contentUrl, platformUploader 필드)"),
      @ApiChangeLog(date = "2025.11.18", author = Author.SUHSAECHAN, issueNumber = 83, description = "AI 서버 Callback API ContentInfo 파라미터 추가 (summary 필드)"),
      @ApiChangeLog(date = "2025.11.12", author = Author.SUHSAECHAN, issueNumber = 70, description = "명세 변경, 기존 전체정보 > 상호명으로만 받음"),
      @ApiChangeLog(date = "2025.11.02", author = Author.KANGJIYUN, issueNumber = 48, description = "AI 서버 Webhook Callback 리팩터링"),
      @ApiChangeLog(date = "2025.10.31", author = Author.SUHSAECHAN, issueNumber = 48, description = "AI 서버 Webhook Callback 처리 API 구현"),
  })
  @Operation(summary = "AI 서버 Webhook Callback", description = """
              ## 인증(API Key): **필요** (Header: X-API-Key)

              ## 요청 파라미터 (AiCallbackRequest)
              - **`contentId`**: Content UUID (필수)
              - **`resultStatus`**: 처리 결과 상태 (SUCCESS/FAILED) (필수)
              - **`snsPlatform`**: SNS 플랫폼 (INSTAGRAM/YOUTUBE/YOUTUBE_SHORTS/TIKTOK/FACEBOOK/TWITTER) (필수)
              - **`contentInfo`**: 콘텐츠 정보 (SUCCESS 시 필수)
                - **`title`**: 콘텐츠 제목
                - **`contentUrl`**: 콘텐츠 URL
                - **`thumbnailUrl`**: 썸네일 URL
                - **`platformUploader`**: 업로더 아이디
                - **`summary`**: AI 콘텐츠 요약 (선택)
              - **`places`**: 추출된 장소 목록 (SUCCESS 시)
                - **`name`**: 장소명
                - **`address`**: 주소
                - **`country`**: 국가 코드 (ISO 3166-1 alpha-2)
                - **`latitude`**: 위도
                - **`longitude`**: 경도
                - **`description`**: 장소 설명
                - **`rawData`**: AI 추출 원본 데이터

              ## 반환값 (AiCallbackResponse)
              - **`received`**: 수신 여부 (true)
              - **`contentId`**: Content UUID

              ## 특이사항
              - AI 서버가 장소 추출 분석 완료 후 이 Webhook을 호출합니다.
              - API Key는 환경변수를 통해 설정되며, 반드시 일치해야 합니다.
              - Content 상태를 ANALYZING → COMPLETED/FAILED로 변경합니다.
              - SUCCESS인 경우:
                - ContentInfo로 Content 메타데이터 업데이트 (title, contentUrl, thumbnailUrl, platformUploader, summary)
                - Place 생성 및 Content-Place 연결 수행
                - snsPlatform 값으로 Content.platform 동기화

              ## 에러코드
              - **`INVALID_API_KEY`**: 유효하지 않은 API Key입니다.
              - **`CONTENT_NOT_FOUND`**: 콘텐츠를 찾을 수 없습니다.
              - **`INVALID_REQUEST`**: 잘못된 요청입니다.
              """)
  ResponseEntity<AiCallbackResponse> handleCallback(String apiKey, AiCallbackRequest request);
}
