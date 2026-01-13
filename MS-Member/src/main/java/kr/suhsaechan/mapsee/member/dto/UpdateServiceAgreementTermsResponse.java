package kr.suhsaechan.mapsee.member.dto;

import kr.suhsaechan.mapsee.member.constant.OnboardingStep;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateServiceAgreementTermsResponse {
  @Schema(description = "현재 온보딩 단계", example = "NAME")
  private OnboardingStep currentStep;

  @Schema(description = "온보딩 상태", example = "IN_PROGRESS")
  private String onboardingStatus;

  @Schema(description = "회원 정보 (디버깅용)")
  private MemberDto member;
}
