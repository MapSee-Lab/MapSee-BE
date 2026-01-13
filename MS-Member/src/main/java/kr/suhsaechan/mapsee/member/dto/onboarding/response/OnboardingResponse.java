package kr.suhsaechan.mapsee.member.dto.onboarding.response;

import kr.suhsaechan.mapsee.member.constant.OnboardingStep;
import kr.suhsaechan.mapsee.member.dto.MemberDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingResponse {
  @Schema(description = "현재 온보딩 단계", example = "BIRTH_DATE")
  private OnboardingStep currentStep;

  @Schema(description = "온보딩 상태", example = "IN_PROGRESS")
  private String onboardingStatus;

  @Schema(description = "회원 정보 (디버깅용)")
  private MemberDto member;
}
