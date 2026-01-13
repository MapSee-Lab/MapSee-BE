package kr.suhsaechan.mapsee.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateServiceAgreementTermsRequest {
  @Schema(hidden = true)
  @JsonIgnore
  private UUID memberId;

  @Schema(description = "서비스 이용약관 및 개인정보처리방침 동의 여부", example = "true")
  @NotNull
  private Boolean isServiceTermsAndPrivacyAgreed;

  @Schema(description = "마케팅 수신 동의 여부(선택)", example = "false")
  private Boolean isMarketingAgreed;
}
