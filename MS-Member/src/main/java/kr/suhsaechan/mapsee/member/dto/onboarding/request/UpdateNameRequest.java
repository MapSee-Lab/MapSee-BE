package kr.suhsaechan.mapsee.member.dto.onboarding.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class UpdateNameRequest {
  @Schema(hidden = true)
  @JsonIgnore
  private UUID memberId;

  @NotBlank(message = "이름은 필수입니다.")
  @Schema(description = "이름", example = "홍길동", required = true)
  private String name;
}

