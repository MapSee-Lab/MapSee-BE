package kr.suhsaechan.mapsee.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class ReissueResponse {

  @Schema(description = "액세스 토큰")
  private String accessToken;

  @Schema(description = "리프레시 토큰")
  private String refreshToken;

  @Schema(description = "첫 로그인 여부")
  private Boolean isFirstLogin;
}
