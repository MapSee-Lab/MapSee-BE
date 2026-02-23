package kr.suhsaechan.mapsy.place.entity;

import kr.suhsaechan.mapsy.common.entity.SoftDeletableBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "folder_place",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_folder_place",
            columnNames = {"folder_id", "place_id"}
        )
    }
)
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FolderPlace extends SoftDeletableBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Folder folder;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Place place;

  @Column(nullable = false)
  @Builder.Default
  private int position = 0;

}
