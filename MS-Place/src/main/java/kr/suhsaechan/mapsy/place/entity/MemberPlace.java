package kr.suhsaechan.mapsy.place.entity;

import kr.suhsaechan.mapsy.common.entity.SoftDeletableBaseEntity;
import kr.suhsaechan.mapsy.common.exception.CustomException;
import kr.suhsaechan.mapsy.common.exception.constant.ErrorCode;
import kr.suhsaechan.mapsy.member.entity.Member;
import kr.suhsaechan.mapsy.place.constant.PlaceSavedStatus;
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
   * 북마크 폴더명
   * - 사용자가 장소를 분류하기 위한 폴더
   * - 예: "가고 싶은 곳", "가본 곳", "즐겨찾기", "맛집" 등
   * - 기본값: "default"
   */
  @Column(length = 50)
  @Builder.Default
  private String folder = "default";

  /**
   * 사용자 메모
   * - 장소에 대한 개인적인 메모 (최대 1000자)
   */
  @Column(columnDefinition = "TEXT")
  private String memo;

  /**
   * 별점 (1-5)
   * - 사용자가 매긴 개인적인 평점
   * - null 가능 (평가하지 않은 경우)
   */
  @Column
  private Integer rating;

  /**
   * 방문 여부
   * - true: 방문함, false: 방문하지 않음
   * - 기본값: false
   */
  @Column(nullable = false)
  @Builder.Default
  private Boolean visited = false;

  /**
   * 방문 일시
   * - 사용자가 실제로 방문한 시간
   * - visited = true일 때만 의미 있음
   */
  @Column
  private LocalDateTime visitedAt;

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

  /**
   * 폴더 변경
   *
   * @param newFolder 새 폴더명
   */
  public void updateFolder(String newFolder) {
    this.folder = newFolder;
  }

  /**
   * 메모 수정
   *
   * @param newMemo 새 메모
   */
  public void updateMemo(String newMemo) {
    this.memo = newMemo;
  }

  /**
   * 별점 수정
   *
   * @param newRating 새 별점 (1-5)
   * @throws CustomException 별점이 1-5 범위를 벗어난 경우
   */
  public void updateRating(Integer newRating) {
    if (newRating != null && (newRating < 1 || newRating > 5)) {
      throw new CustomException(ErrorCode.INVALID_RATING);
    }
    this.rating = newRating;
  }

  /**
   * 방문 여부 및 일시 기록
   *
   * @param visitedAt 방문 일시 (null이면 현재 시간)
   */
  public void markAsVisited(LocalDateTime visitedAt) {
    this.visited = true;
    this.visitedAt = visitedAt != null ? visitedAt : LocalDateTime.now();
  }

  /**
   * 방문 취소
   */
  public void unmarkVisited() {
    this.visited = false;
    this.visitedAt = null;
  }
}
