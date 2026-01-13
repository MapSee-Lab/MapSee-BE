package kr.suhsaechan.mapsee.member.dto.interest.response;

import kr.suhsaechan.mapsee.member.constant.InterestCategory;
import kr.suhsaechan.mapsee.member.entity.Interest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 전체 관심사 목록 조회 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "전체 관심사 목록 조회 응답")
public class GetAllInterestsResponse {

  @Schema(description = "카테고리별 그룹핑된 관심사 목록")
  private List<CategoryGroup> categories;

  /**
   * 카테고리 그룹
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "카테고리 그룹")
  public static class CategoryGroup {

    @Schema(description = "카테고리 코드", example = "FOOD")
    private String category;

    @Schema(description = "카테고리 표시 이름", example = "맛집/푸드")
    private String displayName;

    @Schema(description = "관심사 목록")
    private List<InterestItem> interests;
  }

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

    @Schema(description = "관심사 이름", example = "한식")
    private String name;
  }

  /**
   * Entity List를 Response로 변환
   */
  public static GetAllInterestsResponse from(Map<InterestCategory, List<Interest>> groupedInterests) {
    List<CategoryGroup> categoryGroups = groupedInterests.entrySet().stream()
        .map(entry -> CategoryGroup.builder()
            .category(entry.getKey().name())
            .displayName(entry.getKey().getDisplayName())
            .interests(entry.getValue().stream()
                .map(interest -> InterestItem.builder()
                    .id(interest.getId())
                    .name(interest.getName())
                    .build())
                .toList())
            .build())
        .toList();

    return GetAllInterestsResponse.builder()
        .categories(categoryGroups)
        .build();
  }
}
