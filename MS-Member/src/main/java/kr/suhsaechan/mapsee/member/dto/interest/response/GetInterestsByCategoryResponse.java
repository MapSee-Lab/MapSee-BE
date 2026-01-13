package kr.suhsaechan.mapsee.member.dto.interest.response;

import kr.suhsaechan.mapsee.member.entity.Interest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 특정 카테고리 관심사 조회 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "특정 카테고리 관심사 조회 응답")
public class GetInterestsByCategoryResponse {

  @Schema(description = "관심사 목록")
  private List<InterestItem> interests;

  /**
   * 관심사 항목
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "관심사 항목")
  public static class InterestItem {

    @Schema(description = "관심사 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "카테고리 코드", example = "FOOD")
    private String category;

    @Schema(description = "카테고리 표시 이름", example = "맛집/푸드")
    private String categoryDisplayName;

    @Schema(description = "관심사 이름", example = "한식")
    private String name;
  }

  /**
   * Entity List를 Response로 변환
   */
  public static GetInterestsByCategoryResponse from(List<Interest> interests) {
    List<InterestItem> interestItems = interests.stream()
        .map(interest -> InterestItem.builder()
            .id(interest.getId())
            .category(interest.getCategory().name())
            .categoryDisplayName(interest.getCategory().getDisplayName())
            .name(interest.getName())
            .build())
        .toList();

    return GetInterestsByCategoryResponse.builder()
        .interests(interestItems)
        .build();
  }
}
