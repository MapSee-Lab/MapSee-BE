package kr.suhsaechan.mapsee.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 닉네임 중복 확인 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "닉네임 중복 확인 응답")
public class CheckNameResponse {

  @Schema(description = "사용 가능 여부 (true: 사용 가능, false: 중복)", example = "true")
  private Boolean isAvailable;

  @Schema(description = "닉네임", example = "트립게더")
  private String name;
}
