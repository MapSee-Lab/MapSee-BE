package kr.suhsaechan.mapsee.sns.entity;

import kr.suhsaechan.mapsee.common.entity.SoftDeletableBaseEntity;
import kr.suhsaechan.mapsee.sns.constant.ContentPlatform;
import kr.suhsaechan.mapsee.common.constant.ContentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Content extends SoftDeletableBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column
  private ContentPlatform platform;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private ContentStatus status = ContentStatus.PENDING;

  @Column(length = 255)
  private String platformUploader;

  @Column(length = 1000)
  private String caption;

  @Column(length = 500)
  private String thumbnailUrl;

  @Column(nullable = false, length = 2048, unique = true)
  private String originalUrl;

  @Column(length = 500)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String summary;

  private LocalDateTime lastCheckedAt;
}
