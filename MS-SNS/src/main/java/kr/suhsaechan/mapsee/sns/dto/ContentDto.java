package kr.suhsaechan.mapsee.sns.dto;

import kr.suhsaechan.mapsee.sns.constant.ContentPlatform;
import kr.suhsaechan.mapsee.common.constant.ContentStatus;
import kr.suhsaechan.mapsee.sns.entity.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "콘텐츠 정보 DTO")
public class ContentDto {

  @Schema(description = "콘텐츠 ID", example = "550e8400-e29b-41d4-a716-446655440000")
  private UUID id;

  @Schema(description = "플랫폼 유형", example = "INSTAGRAM")
  private ContentPlatform platform;

  @Schema(description = "처리 상태", example = "COMPLETED")
  private ContentStatus status;

  @Schema(description = "업로더 이름", example = "travel_lover_123")
  private String platformUploader;

  @Schema(description = "캡션", example = "제주도 여행 브이로그")
  private String caption;

  @Schema(description = "썸네일 URL", example = "https://example.com/thumbnail.jpg")
  private String thumbnailUrl;

  @Schema(description = "원본 SNS URL", example = "https://www.instagram.com/p/ABC123/")
  private String originalUrl;

  @Schema(description = "제목", example = "제주도 힐링 여행")
  private String title;

  @Schema(description = "요약 설명", example = "제주도의 아름다운 카페와 맛집을 소개합니다.")
  private String summary;

  @Schema(description = "마지막 확인 시각", example = "2025-11-23T10:30:00")
  private LocalDateTime lastCheckedAt;

  public static ContentDto from(Content entity) {
    if (entity == null) {
      return null;
    }

    return ContentDto.builder()
        .id(entity.getId())
        .platform(entity.getPlatform())
        .status(entity.getStatus())
        .platformUploader(entity.getPlatformUploader())
        .caption(entity.getCaption())
        .thumbnailUrl(entity.getThumbnailUrl())
        .originalUrl(entity.getOriginalUrl())
        .title(entity.getTitle())
        .summary(entity.getSummary())
        .lastCheckedAt(entity.getLastCheckedAt())
        .build();
  }
}
