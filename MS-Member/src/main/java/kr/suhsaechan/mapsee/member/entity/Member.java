package kr.suhsaechan.mapsee.member.entity;

import kr.suhsaechan.mapsee.common.entity.SoftDeletableBaseEntity;
import kr.suhsaechan.mapsee.member.constant.MemberRole;
import kr.suhsaechan.mapsee.member.constant.MemberOnboardingStatus;
import kr.suhsaechan.mapsee.member.constant.MemberGender;
import kr.suhsaechan.mapsee.member.constant.OnboardingStep;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member extends SoftDeletableBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(nullable = false, length = 100)
  private String name;

  @Column
  private LocalDate birthDate;

  @Enumerated(EnumType.STRING)
  @Column(length = 15)
  private MemberGender gender;

  //
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private MemberOnboardingStatus onboardingStatus = MemberOnboardingStatus.NOT_STARTED;

  @Column(nullable = false)
  @Builder.Default
  private Boolean tutorialEnabled = false;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private MemberRole memberRole = MemberRole.ROLE_USER;

  @Column(length = 500)
  private String profileImageUrl;

  // 서비스 이용약관 및 개인정보처리방침 동의 (필수)
  @Column(nullable = false)
  @Builder.Default
  private Boolean isServiceTermsAndPrivacyAgreed = false;

  // 마케팅 수신 동의 (선택)
  @Column(nullable = false)
  @Builder.Default
  private Boolean isMarketingAgreed = false;

  // 현재 온보딩 단계 (캐싱용)
  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private OnboardingStep onboardingStep;

  @PrePersist
  protected void onCreate() {
      if (onboardingStatus == null) {
          onboardingStatus = MemberOnboardingStatus.NOT_STARTED;
      }
      if (memberRole == null) {
          memberRole = MemberRole.ROLE_USER;    //기본값
      }
  }

  /**
   * 회원 탈퇴 처리 (소프트삭제)
   * email, name에 타임스탬프를 추가하여 UNIQUE 제약조건 회피
   *
   * @param deletedBy 탈퇴 처리자 ID
   * @return 탈퇴 시간 타임스탬프 문자열 (yyyy_MM_dd_HHmmss)
   */
  public String withdraw(String deletedBy) {
    // 소프트삭제 처리
    softDelete(deletedBy);

    // 타임스탬프 생성 (yyyy_MM_dd_HHmmss)
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HHmmss");
    String timestamp = this.getDeletedAt().format(formatter);

    // email, name에 타임스탬프 추가 (UNIQUE 제약조건 회피)
    this.email = this.email + "_" + timestamp;
    this.name = this.name + "_" + timestamp;

    return timestamp;
  }

}
