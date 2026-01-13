package kr.suhsaechan.mapsee.place.entity;

import kr.suhsaechan.mapsee.common.entity.SoftDeletableBaseEntity;
import kr.suhsaechan.mapsee.common.exception.CustomException;
import kr.suhsaechan.mapsee.common.exception.constant.ErrorCode;
import kr.suhsaechan.mapsee.member.entity.Member;
import kr.suhsaechan.mapsee.place.constant.PlaceSavedStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * MemberPlace 엔티티
 * - Member와 Place 간의 M:N 관계를 표현하는 중간 테이블
 * - 각 회원별로 장소의 저장 상태(TEMPORARY/SAVED)를 관리
 * - AI 분석으로 추출된 장소는 TEMPORARY로 시작하여 사용자가 저장하면 SAVED로 변경
 */
@Entity
@Table(
    name = "member_place",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_member_place",
            columnNames = {"member_id", "place_id"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberPlace extends SoftDeletableBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  private Place place;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private PlaceSavedStatus savedStatus = PlaceSavedStatus.TEMPORARY;

  /**
   * 이 MemberPlace를 생성한 원본 Content의 ID
   * - Content 엔티티를 직접 참조하지 않음 (순환 의존성 방지)
   * - TG-Place 모듈이 TG-SNS 모듈을 import하지 않도록 UUID로 관리
   */
  private UUID sourceContentId;

  /**
   * 사용자가 장소를 저장한 시간
   * - TEMPORARY -> SAVED 상태 변경 시 기록됨
   */
  private LocalDateTime savedAt;

  /**
   * 임시 저장 상태에서 저장 상태로 변경
   * - savedStatus: TEMPORARY -> SAVED
   * - savedAt: 현재 시간 기록
   *
   * @throws CustomException 이미 SAVED 상태인 경우
   */
  public void markAsSaved() {
    // 이미 저장된 장소인지 검증
    if (this.savedStatus == PlaceSavedStatus.SAVED) {
      throw new CustomException(ErrorCode.PLACE_ALREADY_SAVED);
    }

    this.savedStatus = PlaceSavedStatus.SAVED;
    this.savedAt = LocalDateTime.now();
  }
}
