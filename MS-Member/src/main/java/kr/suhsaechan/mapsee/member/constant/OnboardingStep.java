package kr.suhsaechan.mapsee.member.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OnboardingStep {
  TERMS("서비스 이용약관 및 개인정보처리방침 동의"),
  NAME("이름 설정"),
  BIRTH_DATE("생년월일 설정"),
  GENDER("성별 설정"),
  INTERESTS("관심사 설정"),
  COMPLETED("온보딩 완료");

  private final String description;
}

