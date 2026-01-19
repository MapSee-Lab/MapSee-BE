package kr.suhsaechan.mapsy.auth.dto;

import kr.suhsaechan.mapsy.common.constant.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class SignInRequest {

  @NotBlank(message = "Firebase ID Token은 필수입니다.")
  @Schema(description = "Firebase ID Token (클라이언트에서 Firebase 인증 후 전달)",
      example = "eyJhbGciOiJSUzI1NiIsImtpZCI6...")
  private String firebaseIdToken;

  @Schema(description = "FCM 푸시 알림 토큰 (선택)", example = "dXQzM2k1N2RkZjM0OGE3YjczZGY5...")
  private String fcmToken;

  @Schema(description = "디바이스 타입 (IOS, ANDROID)", example = "IOS")
  private DeviceType deviceType;

  @Schema(description = "디바이스 고유 식별자 (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
  private String deviceId;
}
