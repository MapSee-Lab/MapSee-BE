package kr.suhsaechan.mapsy.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Firebase 토큰에서 추출한 사용자 정보")
public class FirebaseUserInfo {

  @Schema(description = "Firebase UID", example = "cXcpyzYSX....")
  private String uid;

  @Schema(description = "이메일", example = "user@example.com")
  private String email;

  @Schema(description = "Firebase displayName (사용 안 함)", example = "엘리페어")
  private String name;

  @Schema(description = "프로필 이미지 URL", example = "https://lh3.googleusercontent.com/a/...")
  private String profileImageUrl;

  @Schema(description = "로그인 제공자", example = "google.com")
  private String signInProvider;
}
