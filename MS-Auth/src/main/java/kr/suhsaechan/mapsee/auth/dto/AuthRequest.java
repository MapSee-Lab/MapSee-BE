package kr.suhsaechan.mapsee.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.suhsaechan.mapsee.common.constant.DeviceType;
import kr.suhsaechan.mapsee.common.constant.SocialPlatform;
import kr.suhsaechan.mapsee.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class AuthRequest {

  @Schema(hidden = true, description = "회원")
  @JsonIgnore
  private Member member;

  @Schema(description = "로그인 플랫폼 (KAKAO, GOOGLE 등)", example = "KAKAO")
  private SocialPlatform socialPlatform;

  @Schema(description = "소셜 로그인 후 반환된 이메일", example = "user@example.com")
  private String email;

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

  @Schema(hidden = true)
  private String accessToken;

  @Schema(hidden = true)
  private String refreshToken;
}
