package kr.suhsaechan.mapsee.member.entity;

import kr.suhsaechan.mapsee.common.entity.BaseEntity;
import kr.suhsaechan.mapsee.member.constant.InterestCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Interest extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private InterestCategory category;

  @Column(length = 100, nullable = false)
  private String name;

}
