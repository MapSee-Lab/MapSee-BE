package kr.suhsaechan.mapsee.member.dto.onboarding.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBirthDateRequest {
  @Schema(hidden = true)
  @JsonIgnore
  private UUID memberId;

  @NotNull(message = "생년월일은 필수입니다.")
  @Schema(description = "생년월일", example = "1990-01-01", required = true)
  private LocalDate birthDate;
}

