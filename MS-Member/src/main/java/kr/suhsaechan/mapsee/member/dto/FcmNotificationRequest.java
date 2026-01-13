package kr.suhsaechan.mapsee.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Map;

/**
 * FCM 푸시 알림 전송 요청 DTO
 */
@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class FcmNotificationRequest {

  @Schema(description = "알림 제목", example = "새로운 메시지")
  @NotBlank(message = "알림 제목은 필수입니다.")
  private String title;

  @Schema(description = "알림 본문", example = "새로운 메시지가 도착했습니다.")
  @NotBlank(message = "알림 본문은 필수입니다.")
  private String body;

  @Schema(description = "추가 데이터 (key-value)", example = "{\"type\": \"message\", \"id\": \"123\"}")
  private Map<String, String> data;

  @Schema(description = "이미지 URL (선택)", example = "https://example.com/image.png")
  private String imageUrl;

  @Schema(description = "FCM 토큰 (단일 기기 전송용)", example = "fGcX...")
  private String fcmToken;
}
