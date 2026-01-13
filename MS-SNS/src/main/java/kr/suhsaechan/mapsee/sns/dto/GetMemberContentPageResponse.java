package kr.suhsaechan.mapsee.sns.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 콘텐츠 목록 조회 응답")
public class GetMemberContentPageResponse {

  @Schema(description = "콘텐츠 페이지 정보 (최신순)")
  private Page<ContentDto> contentPage;
}
