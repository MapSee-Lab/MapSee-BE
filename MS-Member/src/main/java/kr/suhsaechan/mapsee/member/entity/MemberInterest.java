package kr.suhsaechan.mapsee.member.entity;

import kr.suhsaechan.mapsee.common.entity.SoftDeletableBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberInterest extends SoftDeletableBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Interest interest;

  public UUID getInterestId() {
    return this.interest != null ? this.interest.getId() : null;  // interest가 null이 아닐 경우 interest의 ID 반환
  }
}
