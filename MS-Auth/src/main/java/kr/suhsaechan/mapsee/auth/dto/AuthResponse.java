package kr.suhsaechan.mapsee.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class AuthResponse {

  @Schema(description = "액세스 토큰")
  private String accessToken;

  @Schema(description = "리프레시 토큰")
  private String refreshToken;

  @Schema(description = "첫 로그인 여부")
  private Boolean isFirstLogin;

  @Schema(description = "약관/온보딩 완료 여부")
  private boolean requiresOnboarding;

  @Schema(description = "현재 온보딩 단계", example = "TERMS")
  private String onboardingStep;
}
