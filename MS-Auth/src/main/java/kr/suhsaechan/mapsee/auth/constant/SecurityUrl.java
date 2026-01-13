package kr.suhsaechan.mapsee.auth.constant;

import java.util.List;

/**
 * Security 관련 URL 상수 관리
 */
public class SecurityUrl {

  /**
   * 인증 생략할 URL 패턴 목록
   */
  public static final List<String> AUTH_WHITELIST =
      List.of(
          // Auth
          "/api/auth/sign-in", // 소셜 로그인
          "/api/auth/reissue", // 토큰 재발급

          // Member
          "/api/members/check-name", // 닉네임 중복 확인

          // AI 서버
          "/api/ai/callback", // AI 서버 Webhook Callback

          // Test APIs (개발/테스트 전용)
          "/api/test/**", // 테스트 API

          // Swagger
          "/docs/swagger-ui/**", // Swagger UI
          "/v3/api-docs/**", // Swagger API 문서
          "/docs/swagger", // Swagger UI HTML

          // Actuator (선택사항)
          "/actuator/**",

          // 기타 정적 리소스
          "/favicon.ico",
          "/error");
}
