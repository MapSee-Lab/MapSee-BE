package kr.suhsaechan.mapsee.auth.service;

import kr.suhsaechan.mapsee.auth.dto.AuthRequest;
import kr.suhsaechan.mapsee.auth.dto.AuthResponse;
import kr.suhsaechan.mapsee.auth.dto.CustomUserDetails;
import kr.suhsaechan.mapsee.auth.dto.ReissueRequest;
import kr.suhsaechan.mapsee.auth.dto.ReissueResponse;
import kr.suhsaechan.mapsee.auth.dto.SignInRequest;
import kr.suhsaechan.mapsee.auth.dto.SignInResponse;
import kr.suhsaechan.mapsee.auth.jwt.JwtUtil;
import kr.suhsaechan.mapsee.common.exception.CustomException;
import kr.suhsaechan.mapsee.common.exception.constant.ErrorCode;
import kr.suhsaechan.mapsee.member.constant.MemberOnboardingStatus;
import kr.suhsaechan.mapsee.member.constant.OnboardingStep;
import kr.suhsaechan.mapsee.member.entity.FcmToken;
import kr.suhsaechan.mapsee.member.entity.Member;
import kr.suhsaechan.mapsee.member.entity.MemberInterest;
import kr.suhsaechan.mapsee.member.repository.FcmTokenRepository;
import kr.suhsaechan.mapsee.member.repository.MemberInterestRepository;
import kr.suhsaechan.mapsee.member.repository.MemberRepository;
import kr.suhsaechan.mapsee.member.service.MemberService;
import io.jsonwebtoken.ExpiredJwtException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private static final String REFRESH_KEY_PREFIX = "RT:";

  private final MemberRepository memberRepository;
  private final MemberService memberService;
  private final JwtUtil jwtUtil;
  private final RedisTemplate<String, Object> redisTemplate;
  private final MemberInterestRepository memberInterestRepository;
  private final FcmTokenRepository fcmTokenRepository;

  /**
   * 로그인 로직 클라이언트로부터 플랫폼, 닉네임, 프로필url, 이메일을 입력받아 JWT를 발급합니다.
   */
  @Transactional
  public SignInResponse signIn(SignInRequest request) {
    // FCM 토큰 입력값 검증
    validateFcmTokenRequest(request);

    // 요청 값으로부터 사용자 정보 획득
    String email = request.getEmail();
    String name = request.getName();

    //회원 조회
    Member member = memberRepository.findByEmail(email)
        .orElseGet(() -> {
          // 신규 회원 생성 시 기본값 자동 설정
          Member newMember = Member.builder()
              .email(email)
              .name("name")
              .build();
          memberRepository.save(newMember);
          log.debug("신규 회원 가입: {}", email);
          return newMember;
        });

    boolean isFirstLogin = member.getOnboardingStatus() == MemberOnboardingStatus.NOT_STARTED;

    //온보딩 상태 갱신 (NOT_STARTED → IN_PROGRESS)
    if (isFirstLogin) {
      member.setOnboardingStatus(MemberOnboardingStatus.IN_PROGRESS);
      memberRepository.save(member);
      log.debug("온보딩 상태 변경: {} → {}", MemberOnboardingStatus.NOT_STARTED, MemberOnboardingStatus.IN_PROGRESS);
    } else {
      log.debug("기존 회원 로그인: {}", email);
    }

    // FCM 토큰 저장/업데이트
    saveFcmToken(member, request);

    // JWT 토큰 생성
    CustomUserDetails customUserDetails = new CustomUserDetails(member);
    String accessToken = jwtUtil.createAccessToken(customUserDetails);
    String refreshToken = jwtUtil.createRefreshToken(customUserDetails);

    log.debug("로그인 성공: email={}, accessToken={}, refreshToken={}", email, accessToken, refreshToken);

    // RefreshToken -> Redis 저장 (키: "RT:{memberId}")
    redisTemplate.opsForValue().set(
        REFRESH_KEY_PREFIX + customUserDetails.getMemberId(),
        refreshToken,
        jwtUtil.getRefreshExpirationTime(),
        TimeUnit.MILLISECONDS);

    //온보딩 필요 여부 확인
    boolean requiresOnboarding = (member.getOnboardingStatus() != MemberOnboardingStatus.COMPLETED);

    // 온보딩 단계 계산 및 저장
    // COMPLETED 상태면 계산하지 않고 캐시된 값 사용
    String onboardingStep;
    if (member.getOnboardingStatus() == MemberOnboardingStatus.COMPLETED) {
      // COMPLETED 상태면 캐시된 값 사용 (없으면 COMPLETED 반환)
      onboardingStep = member.getOnboardingStep() != null 
          ? member.getOnboardingStep().name() 
          : OnboardingStep.COMPLETED.name();
    } else {
      // IN_PROGRESS 또는 NOT_STARTED 상태면 계산 후 저장
      OnboardingStep step = memberService.calculateAndSaveOnboardingStep(member);
      onboardingStep = step.name();
    }

    //응답 생성
    return SignInResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .isFirstLogin(isFirstLogin)
        .requiresOnboarding(requiresOnboarding)
        .onboardingStep(onboardingStep)
        .build();
  }

  /**
   * refreshToken을 통해 accessToken을 재발급합니다
   */
  @Transactional
  public ReissueResponse reissue(ReissueRequest request) {
    log.debug("accessToken이 만료되어 토큰 재발급을 진행합니다.");

    String refreshToken = request.getRefreshToken();

    // 리프레시 토큰이 없는 경우
    if (refreshToken == null || refreshToken.isBlank()) {
      log.error("refreshToken을 찾을 수 없습니다.");
      throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    // 리프레시 토큰 유효성 검사 및 만료 여부 확인
    try {
      if (!jwtUtil.validateToken(refreshToken)) {
        log.error("유효하지 않은 refreshToken 입니다.");
        throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
      }
    } catch (ExpiredJwtException e) {
      log.error("만료된 refreshToken 입니다: {}", e.getMessage());
      throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    // 새로운 accessToken 생성
    CustomUserDetails customUserDetails = (CustomUserDetails) jwtUtil.getAuthentication(refreshToken).getPrincipal();

    // Redis에 저장된 refreshToken과 일치 여부 확인
    String refreshKey = REFRESH_KEY_PREFIX + customUserDetails.getMemberId();
    String storedRefreshToken = (String) redisTemplate.opsForValue().get(refreshKey);

    if (storedRefreshToken == null) {
      log.error("Redis에 저장된 refreshToken을 찾을 수 없습니다. memberId: {}", customUserDetails.getMemberId());
      throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_STORED);
    }

    if (!storedRefreshToken.equals(refreshToken)) {
      log.error("Redis에 저장된 refreshToken과 일치하지 않습니다. memberId: {}", customUserDetails.getMemberId());
      throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH);
    }

    String newAccessToken = jwtUtil.createAccessToken(customUserDetails);

    // 회원 존재 여부 및 탈퇴 여부 검증
    Member memberForValidation = memberRepository.findByEmail(jwtUtil.getUsername(newAccessToken))
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 탈퇴한 회원은 토큰 재발급 불가
    if (memberForValidation.isDeleted()) {
      log.error("탈퇴한 회원의 토큰 재발급 시도 - memberId={}", memberForValidation.getId());
      throw new CustomException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
    }

    return ReissueResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(refreshToken)
        .isFirstLogin(false)
        .build();
  }

  /**
   * 로그아웃 액세스 토큰을 블랙리스트에 등록합니다 redis에 저장되어있는 리프레시토큰을 삭제합니다
   */
  @Transactional
  public void logout(AuthRequest request) {
    Member member = request.getMember();
    String accessToken = request.getAccessToken();

    // accessToken 입력값 검증
    if (member == null || accessToken == null || accessToken.isBlank()) {
      log.error("로그아웃 요청에 필수 정보가 누락되었습니다.");
      throw new CustomException(ErrorCode.MISSING_AUTH_TOKEN);
    }

    // 저장된 refreshToken 키
    String key = REFRESH_KEY_PREFIX + member.getId();

    // 토큰 비활성화
    jwtUtil.deactivateToken(accessToken, key);
  }

  /**
   * 회원 탈퇴
   *
   * @param memberId 탈퇴할 회원 ID
   * @param accessToken 탈퇴 시 사용한 AccessToken (토큰 무효화용)
   */
  @Transactional
  public void withdrawMember(UUID memberId, String accessToken) {
    // 회원 조회
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 이미 탈퇴한 회원인지 확인
    if (member.isDeleted()) {
      log.warn("[Auth] 이미 탈퇴한 회원 - memberId={}", memberId);
      throw new CustomException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
    }

    // 탈퇴 처리 (email, name에 타임스탬프 자동 추가)
    String timestamp = member.withdraw(memberId.toString());

    // 회원 관심사 소프트삭제
    List<MemberInterest> memberInterests = memberInterestRepository.findByMemberId(memberId);
    memberInterests.forEach(interest -> interest.softDelete(memberId.toString()));

    // FCM 토큰 삭제 (하드삭제 - 소프트삭제 시 FK 제약조건 위반 방지)
    fcmTokenRepository.deleteByMember(member);
    log.info("[Auth] FCM 토큰 삭제 완료 - memberId={}", memberId);

    memberRepository.save(member);

    // 토큰 무효화 처리 (로그아웃과 동일한 보안 처리)
    if (accessToken != null) {
      try {
        String refreshTokenKey = REFRESH_KEY_PREFIX + memberId;
        jwtUtil.deactivateToken(accessToken, refreshTokenKey);
        log.info("[Auth] 토큰 무효화 완료 - memberId={}", memberId);
      } catch (Exception e) {
        log.warn("[Auth] 토큰 무효화 중 오류 발생 (탈퇴는 정상 처리됨) - memberId={}, error={}", memberId, e.getMessage());
        // 토큰 무효화 실패해도 탈퇴는 진행 (이미 만료된 토큰일 수 있음)
      }
    }

    log.info("[Auth] 회원 탈퇴 완료 - memberId={}, timestamp={}", memberId, timestamp);
  }

  /**
   * FCM 토큰 요청 검증
   */
  private void validateFcmTokenRequest(SignInRequest request) {
    String fcmToken = request.getFcmToken();
    String deviceId = request.getDeviceId();
    var deviceType = request.getDeviceType();

    // fcmToken이 있으면 deviceType과 deviceId도 필수
    if (fcmToken != null && !fcmToken.isBlank()) {
      if (deviceType == null) {
        log.error("fcmToken이 제공되었으나 deviceType이 누락되었습니다.");
        throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
      }
      if (deviceId == null || deviceId.isBlank()) {
        log.error("fcmToken이 제공되었으나 deviceId가 누락되었습니다.");
        throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
      }
    }

    // deviceType이나 deviceId만 있고 fcmToken이 없는 경우
    if ((deviceType != null || (deviceId != null && !deviceId.isBlank()))
        && (fcmToken == null || fcmToken.isBlank())) {
      log.error("deviceType 또는 deviceId가 제공되었으나 fcmToken이 누락되었습니다.");
      throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
    }
  }

  /**
   * FCM 토큰 저장 또는 업데이트
   */
  private void saveFcmToken(Member member, SignInRequest request) {
    String fcmToken = request.getFcmToken();
    String deviceId = request.getDeviceId();
    var deviceType = request.getDeviceType();

    // FCM 정보가 없으면 저장하지 않음
    if (fcmToken == null || fcmToken.isBlank()) {
      log.debug("FCM 토큰 정보가 없어 저장을 건너뜁니다.");
      return;
    }

    // 기존 토큰 조회
    Optional<FcmToken> existingToken = fcmTokenRepository.findByMemberAndDeviceId(member, deviceId);

    if (existingToken.isPresent()) {
      // 기존 토큰이 있으면 업데이트
      FcmToken token = existingToken.get();
      token.setFcmToken(fcmToken);
      token.setDeviceType(deviceType);
      token.setLastUsedAt(LocalDateTime.now());
      fcmTokenRepository.save(token);
      log.debug("FCM 토큰 업데이트: memberId={}, deviceId={}", member.getId(), deviceId);
    } else {
      // 새로운 토큰 생성
      FcmToken newToken = FcmToken.builder()
          .member(member)
          .fcmToken(fcmToken)
          .deviceType(deviceType)
          .deviceId(deviceId)
          .lastUsedAt(LocalDateTime.now())
          .build();
      fcmTokenRepository.save(newToken);
      log.debug("FCM 토큰 신규 저장: memberId={}, deviceId={}", member.getId(), deviceId);
    }
  }
}
