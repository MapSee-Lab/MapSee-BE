package kr.suhsaechan.mapsee.sns.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 서버로부터 장소 추출 요청 응답 (202 Accepted)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiContentRequest {

  private UUID contentId;

  private String snsUrl;
}
