package kr.suhsaechan.mapsee.sns.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetContentInfoRequest {

  @Schema(description = "콘텐츠 ID", example = "550e8400-e29b-41d4-a716-446655440000")
  @NotNull(message = "contentId는 필수입니다.")
  private UUID contentId;
}
