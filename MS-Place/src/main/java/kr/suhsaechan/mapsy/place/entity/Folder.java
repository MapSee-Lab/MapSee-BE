package kr.suhsaechan.mapsy.place.entity;

import kr.suhsaechan.mapsy.common.entity.SoftDeletableBaseEntity;
import kr.suhsaechan.mapsy.member.entity.Member;
import kr.suhsaechan.mapsy.place.constant.FolderVisibility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Folder extends SoftDeletableBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Member owner;

  @Column(nullable = false, length = 100)
  @Builder.Default
  private String name = "제목 없음";

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private FolderVisibility visibility = FolderVisibility.PRIVATE;

  @Column(length = 500)
  private String shareLink;

  @Column(length = 500)
  private String thumbnailUrl;

  @Column(nullable = false)
  @Builder.Default
  private Boolean isDefault = false;

  public void updateName(String name) {
    this.name = name;
  }

  public void updateVisibility(FolderVisibility visibility) {
    this.visibility = visibility;
  }

  @PrePersist
  protected void onCreate() {
      if (name == null) {
          name = "제목 없음";
      }
      if (visibility == null) {
          visibility = FolderVisibility.PRIVATE;
      }
      if (isDefault == null) {
          isDefault = false;
      }
  }

}
