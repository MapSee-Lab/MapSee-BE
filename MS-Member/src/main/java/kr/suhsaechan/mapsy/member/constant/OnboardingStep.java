package kr.suhsaechan.mapsy.member.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OnboardingStep {
  TERMS("서비스 이용약관 및 개인정보처리방침 동의"),
  BIRTH_DATE("생년월일 설정"),
  GENDER("성별 설정"),
  COMPLETED("온보딩 완료");

  private final String description;
}

