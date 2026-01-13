package kr.suhsaechan.mapsee.member.entity;

import kr.suhsaechan.mapsee.common.constant.DeviceType;
import kr.suhsaechan.mapsee.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_member_device",
            columnNames = {"member_id", "device_id"}
        )
    },
    indexes = {
        @Index(name = "idx_member_id", columnList = "member_id"),
        @Index(name = "idx_device_id", columnList = "device_id")
    }
)
public class FcmToken extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Member member;

  @Column(nullable = false, length = 500)
  private String fcmToken;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private DeviceType deviceType;

  @Column(nullable = false, length = 100)
  private String deviceId;

  @Column
  private LocalDateTime lastUsedAt;
}
