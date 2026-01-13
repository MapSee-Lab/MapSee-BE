package kr.suhsaechan.mapsee.sns.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestPlaceExtractionRequest {

  @Schema(description = "SNS URL", example = "https://www.instagram.com/p/ABC123/")
  @NotNull(message = "snsUrl은 필수입니다.")
  private String snsUrl;
}
