package kr.suhsaechan.mapsy.sns.entity;

import kr.suhsaechan.mapsy.common.entity.SoftDeletableBaseEntity;
import kr.suhsaechan.mapsy.sns.constant.ContentPlatform;
import kr.suhsaechan.mapsy.common.constant.ContentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

  @Column(length = 2000)
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

  // ========== 신규 필드 (AI 콜백 #16) ==========

  @Column(length = 50)
  private String contentType;

  @Column
  private Integer likesCount;

  @Column
  private Integer commentsCount;

  @Column
  private LocalDateTime postedAt;

  @Column(columnDefinition = "varchar(100)[]")
  @JdbcTypeCode(SqlTypes.ARRAY)
  private List<String> hashtags;

  @Column(columnDefinition = "text[]")
  @JdbcTypeCode(SqlTypes.ARRAY)
  private List<String> imageUrls;

  @Column(length = 500)
  private String authorProfileImageUrl;
}
