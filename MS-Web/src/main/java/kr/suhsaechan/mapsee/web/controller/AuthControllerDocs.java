package kr.suhsaechan.mapsee.web.controller;

import kr.suhsaechan.mapsee.auth.dto.*;
import kr.suhsaechan.mapsee.auth.dto.CustomUserDetails;
import kr.suhsaechan.mapsee.common.constant.Author;
import io.swagger.v3.oas.annotations.Operation;
import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import org.springframework.http.ResponseEntity;

public interface AuthControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.11.23", author = Author.SUHSAECHAN, issueNumber = 0, description = "FCM 토큰 멀티 디바이스 지원 추가"),
      @ApiChangeLog(date = "2025.10.16", author = Author.SUHSAECHAN, issueNumber = 22, description = "인증 모듈 추가 및 기본 OAuth 로그인 구현"),
  })
  @Operation(summary = "소셜 로그인", description = """
              ## 인증(JWT): **불필요**

              ## 요청 파라미터 (SignInRequest)
              - **`socialPlatform`**: 로그인 플랫폼 (KAKAO, GOOGLE)
              - **`email`**: 사용자 이메일 (필수)
              - **`name`**: 사용자 닉네임 (필수)
              - **`profileUrl`**: 사용자 프로필 url (선택)
              - **`fcmToken`**: FCM 푸시 알림 토큰 (선택)
              - **`deviceType`**: 디바이스 타입 - IOS, ANDROID (fcmToken 제공 시 필수)
              - **`deviceId`**: 디바이스 고유 식별자 UUID (fcmToken 제공 시 필수)

              ## 반환값 (SignInResponse)
              - **`accessToken`**: 발급된 AccessToken
              - **`refreshToken`**: 발급된 RefreshToken
              - **`isFirstLogin`**: 최초 로그인 여부
              - **`requiresOnboarding`**: 온보딩 필요 여부
              - **`onboardingStep`**: 현재 온보딩 단계

              ## 특이사항
              - 클라이언트에서 Kakao/Google OAuth 처리 후 받은 사용자 정보로 서버에 JWT 토큰을 요청합니다.
              - 액세스 토큰은 1시간, 리프레시 토큰은 7일 유효합니다.
              - **FCM 토큰을 전송하면 푸시 알림을 받을 수 있습니다. (멀티 디바이스 지원)**
              - fcmToken, deviceType, deviceId는 3개 모두 함께 전송하거나 모두 전송하지 않아야 합니다.
              - **@Valid 검증이 적용됩니다**: email, name은 필수 필드입니다.

              ## 에러코드
              - **`INVALID_SOCIAL_TOKEN`**: 유효하지 않은 소셜 인증 토큰입니다.
              - **`SOCIAL_AUTH_FAILED`**: 소셜 로그인 인증에 실패하였습니다.
              - **`MEMBER_NOT_FOUND`**: 회원 정보를 찾을 수 없습니다.
              - **`INVALID_INPUT_VALUE`**: FCM 토큰 관련 필드가 올바르지 않습니다.
              """)
  ResponseEntity<SignInResponse> signIn(SignInRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.10.16", author = Author.SUHSAECHAN, issueNumber = 22, description = "토큰 재발급 기능 구현"),
  })
  @Operation(summary = "토큰 재발급", description = """
              ## 인증(JWT): **불필요**

              ## 요청 파라미터 (ReissueRequest)
              - **`refreshToken`**: 리프레시 토큰 (필수)

              ## 반환값 (ReissueResponse)
              - **`accessToken`**: 재발급된 AccessToken
              - **`refreshToken`**: 리프레시 토큰 (변경되지 않음)
              - **`isFirstLogin`**: 최초 로그인 여부

              ## 특이사항
              - 만료된 액세스 토큰을 리프레시 토큰으로 재발급합니다.
              - **@Valid 검증이 적용됩니다**: refreshToken은 필수 필드입니다.

              ## 에러코드
              - **`REFRESH_TOKEN_NOT_FOUND`**: 리프레시 토큰을 찾을 수 없습니다.
              - **`INVALID_REFRESH_TOKEN`**: 유효하지 않은 리프레시 토큰입니다.
              - **`EXPIRED_REFRESH_TOKEN`**: 만료된 리프레시 토큰입니다.
              - **`MEMBER_NOT_FOUND`**: 회원 정보를 찾을 수 없습니다.
              """)
  ResponseEntity<ReissueResponse> reissue(ReissueRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.10.16", author = Author.SUHSAECHAN, issueNumber = 22, description = "로그아웃 기능 구현"),
  })
  @Operation(summary = "로그아웃", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터 (AuthRequest)
              - **`accessToken`**: 엑세스 토큰 (Header에서 자동 추출)
              - **`refreshToken`**: 리프레시 토큰

              ## 반환값
              - 성공 시 상태코드 200 (OK)와 빈 응답 본문

              ## 동작 설명
              - 액세스 토큰을 블랙리스트에 등록하여 무효화 처리
              - Redis에 저장된 리프레시 토큰 삭제

              ## 에러코드
              - **`INVALID_TOKEN`**: 유효하지 않은 토큰입니다.
              - **`UNAUTHORIZED`**: 인증이 필요한 요청입니다.
              """)
  ResponseEntity<Void> logout(CustomUserDetails customUserDetails, String authorization, AuthRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.11.19", author = Author.SUHSAECHAN, issueNumber = 91, description = "회원 탈퇴 API 추가")
  })
  @Operation(
      summary = "회원 탈퇴",
      description =
          """
              ## 인증(JWT): **필요**

              ## 요청 파라미터
              - 없음 (JWT 토큰에서 회원 ID 추출)

              ## 반환값
              - **`204 No Content`**: 탈퇴 성공

              ## 특이사항
              - 현재 로그인한 회원을 탈퇴 처리합니다. (소프트삭제)
              - 탈퇴 시 이메일과 닉네임에 타임스탬프가 추가됩니다. (예: email_2025_01_19_143022)
              - 이를 통해 동일한 이메일/닉네임으로 재가입이 가능합니다.
              - 회원의 관심사도 함께 소프트삭제 됩니다.
              - **보안**: AccessToken은 블랙리스트에 등록되고, RefreshToken은 Redis에서 삭제됩니다.
              - 탈퇴 후에는 해당 토큰으로 API 접근이 불가능합니다.

              ## 에러코드
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              - **`MEMBER_ALREADY_WITHDRAWN`**: 이미 탈퇴한 회원입니다.
              - **`UNAUTHORIZED`**: 인증이 필요합니다.
              """)
  ResponseEntity<Void> withdrawMember(CustomUserDetails userDetails, String authorization);
}
