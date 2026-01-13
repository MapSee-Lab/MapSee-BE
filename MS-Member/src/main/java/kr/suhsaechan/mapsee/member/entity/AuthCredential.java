package kr.suhsaechan.mapsee.member.entity;

import kr.suhsaechan.mapsee.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthCredential extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Member member;

  @Column(nullable = false, length = 255)
  private String passwordHash;

  @Column(nullable = true)
  private LocalDateTime lastPasswordChangeAt;

}
