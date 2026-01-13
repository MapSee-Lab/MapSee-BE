package kr.suhsaechan.mapsee.member.dto.onboarding.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.suhsaechan.mapsee.member.constant.MemberGender;
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
public class UpdateGenderRequest {
  @Schema(hidden = true)
  @JsonIgnore
  private UUID memberId;

  @NotNull(message = "성별은 필수입니다.")
  @Schema(description = "성별", example = "MALE", required = true)
  private MemberGender gender;
}

