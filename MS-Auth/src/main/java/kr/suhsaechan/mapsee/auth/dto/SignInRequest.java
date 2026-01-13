package kr.suhsaechan.mapsee.auth.dto;

import kr.suhsaechan.mapsee.common.constant.DeviceType;
import kr.suhsaechan.mapsee.common.constant.SocialPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class SignInRequest {

  @Schema(description = "로그인 플랫폼 (KAKAO, GOOGLE 등)", example = "KAKAO")
  private SocialPlatform socialPlatform;

  @NotBlank(message = "이메일은 필수입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  @Schema(description = "소셜 로그인 후 반환된 이메일", example = "user@example.com")
  private String email;

  @NotBlank(message = "이름은 필수입니다.")
  @Schema(description = "소셜 로그인 후 반환된 닉네임", example = "홍길동")
  private String name;

  @Schema(description = "소셜 로그인 후 반환된 프로필 URL", example = "https://example.com/profile.jpg")
  private String profileUrl;

  @Schema(description = "FCM 푸시 알림 토큰 (선택)", example = "dXQzM2k1N2RkZjM0OGE3YjczZGY5...")
  private String fcmToken;

  @Schema(description = "디바이스 타입 (IOS, ANDROID)", example = "IOS")
  private DeviceType deviceType;

  @Schema(description = "디바이스 고유 식별자 (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
  private String deviceId;
}
