package kr.suhsaechan.mapsee.sns.entity;

import kr.suhsaechan.mapsee.common.entity.BaseEntity;
import kr.suhsaechan.mapsee.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ContentMember 엔티티
 *
 * Content와 Member 간의 M:N 관계를 나타내는 중간 테이블
 * 여러 회원이 같은 URL을 요청하면 같은 Content를 공유하며,
 * 각 회원별로 ContentMember 레코드가 생성됨
 *
 * AI 분석 완료시 notified=false인 모든 ContentMember의 회원에게 알림 전송
 */
@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"content_id", "member_id"})
    },
    indexes = {
        @Index(columnList = "content_id"),
        @Index(columnList = "member_id"),
        @Index(columnList = "notified")
    }
)
@Builder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContentMember extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @ManyToOne
  private Content content;

  @ManyToOne
  private Member member;

  // 알림 전송 여부
  @Column(nullable = false)
  @Builder.Default
  private Boolean notified = false;

}
