package kr.suhsaechan.mapsee.member.dto.onboarding.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInterestsRequest {
  @Schema(hidden = true)
  @JsonIgnore
  private UUID memberId;

  @NotNull(message = "관심사 목록은 필수입니다.")
  @Size(min = 3, message = "관심사는 최소 3개 이상 선택해야 합니다.")
  @Schema(description = "관심사 ID 목록", example = "[\"id1\", \"id2\", \"id3\"]", required = true)
  private List<UUID> interestIds;
}

