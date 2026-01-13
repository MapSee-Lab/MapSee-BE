package kr.suhsaechan.mapsee.auth.jwt;

import kr.suhsaechan.mapsee.auth.dto.CustomUserDetails;
import kr.suhsaechan.mapsee.auth.service.CustomUserDetailsService;
import kr.suhsaechan.mapsee.common.exception.CustomException;
import kr.suhsaechan.mapsee.common.exception.constant.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtil {

  private final CustomUserDetailsService customUserDetailsService;
  private final RedisTemplate<String, Object> redisTemplate;

  @Value("${jwt.secret-key}")
  private String secretKey;

  @Value("${jwt.access-exp-time}")
  private Long accessTokenExpTime; // AccessToken 만료 시간

  @Value("${jwt.refresh-exp-time}")
  private Long refreshTokenExpTime; // RefreshToken 만료 시간

  @Value("${jwt.issuer}")
  private String issuer; // JWT 발급자

  private static final String ACCESS_CATEGORY = "access";
  private static final String REFRESH_CATEGORY = "refresh";
  private static final String BLACKLIST_PREFIX = "BL:";
  private static final String BLACKLIST_VALUE = "blacklisted";
  public static final String REFRESH_KEY_PREFIX = "RT:";

  // 토큰에서 memberId 파싱
  public UUID getMemberId(String token) {
    // JWT 토큰에서 member_id를 String으로 추출
    String memberIdString = Jwts.parser()
        .verifyWith(getSignKey())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("member_id", String.class);  // member_id를 String으로 추출

    // String을 UUID로 변환하여 반환
    return UUID.fromString(memberIdString);  // String을 UUID로 변환
  }

  // 토큰에서 username 파싱
  public String getUsername(String token) {
    return Jwts.parser()
        .verifyWith(getSignKey())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("username", String.class);
  }

  // 토큰에서 role 파싱
  public String getRole(String token) {
    return Jwts.parser()
        .verifyWith(getSignKey())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("role", String.class);
  }

  // 토큰 만료 여부 확인
  public Boolean isExpired(String token) {
    return Jwts.parser()
        .verifyWith(getSignKey())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getExpiration()
        .before(new Date());
  }

  // Access/Refresh 토큰 여부
  public String getCategory(String token) {
    return Jwts.parser()
        .verifyWith(getSignKey())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("category", String.class);
  }

  /**
   * AccessToken 생성
   */
  public String createAccessToken(CustomUserDetails customUserDetails) {
    log.debug("액세스 토큰 생성 중: 회원: {}", customUserDetails.getUsername());
    return createToken(ACCESS_CATEGORY, customUserDetails, accessTokenExpTime);
  }

  /**
   * RefreshToken 생성
   */
  public String createRefreshToken(CustomUserDetails customUserDetails) {
    log.debug("리프레시 토큰 생성 중: 회원: {}", customUserDetails.getUsername());
    return createToken(REFRESH_CATEGORY, customUserDetails, refreshTokenExpTime);
  }

  /**
   * JWT 토큰 생성 메서드
   */
  private String createToken(String category, CustomUserDetails customUserDetails, Long expiredAt) {
    return Jwts.builder()
        .subject(customUserDetails.getUsername())
        .claim("category", category)
        .claim("username", customUserDetails.getUsername())
        .claim("member_id", customUserDetails.getMemberId())
        .claim("role", customUserDetails.getMember().getMemberRole())
        .issuer(issuer)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiredAt))
        .signWith(getSignKey())
        .compact();
  }

  /**
   * JWT 토큰 유효성 검사
   */
  public boolean validateToken(String token) throws ExpiredJwtException {
    try {
      Jwts.parser()
          .verifyWith(getSignKey())
          .build()
          .parseSignedClaims(token);
      if (isTokenBlacklisted(token)) {
        log.error("액세스 토큰이 블랙리스트에 등록되어있습니다. 요청된 토큰: {}", token);
        throw new CustomException(ErrorCode.TOKEN_BLACKLISTED);
      }
      log.debug("JWT 토큰이 유효합니다.");
      return true;
    } catch (ExpiredJwtException e) {
      log.warn("JWT 토큰이 만료되었습니다: {}", e.getMessage());
      throw e; // 만료된 토큰 예외를 호출한 쪽으로 전달
    } catch (UnsupportedJwtException e) {
      log.warn("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      log.warn("형식이 잘못된 JWT 토큰입니다: {}", e.getMessage());
    } catch (SignatureException e) {
      log.warn("JWT 서명이 유효하지 않습니다: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.warn("JWT 토큰이 비어있거나 null입니다: {}", e.getMessage());
    }
    return false;
  }

  /**
   * JWT 서명에 사용할 키 생성
   */
  private SecretKey getSignKey() {
    try {
      // Base64 문자열로부터 SecretKey를 생성
      byte[] keyBytes = Decoders.BASE64.decode(secretKey);
      return Keys.hmacShaKeyFor(keyBytes);
    } catch (IllegalArgumentException e) {
      log.error("비밀 키 생성 실패: {}", e.getMessage());
      throw e; // 예외 재발생
    }
  }

  /**
   * JWT 토큰에서 클레임 (Claims) 추출
   */
  public Claims getClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSignKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  /**
   * token의 남은 유효기간(밀리초)를 반환합니다.
   */
  public long getRemainingValidationMilliSecond(String token) {
    Claims claims = getClaims(token);
    Date expiration = claims.getExpiration();
    long nowMillis = System.currentTimeMillis();
    long remaining = expiration.getTime() - nowMillis;
    return remaining > 0 ? remaining : 0;
  }

  /**
   * 리프레시 토큰 만료 시간 반환
   */
  public long getRefreshExpirationTime() {
    return refreshTokenExpTime;
  }

  /**
   * JWT 토큰에서 Authentication 객체 생성
   */
  public Authentication getAuthentication(String token) {
    Claims claims = getClaims(token);
    String memberEmail = claims.getSubject();
    log.debug("JWT에서 인증정보 파싱: memberEmail={}", memberEmail);
    CustomUserDetails userDetails = customUserDetailsService.loadUserByUsername(memberEmail);
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  /**
   * "Authorization" 헤더에서 순수한 accessToken을 파싱 후 반환합니다.
   */
  public String extractAccessToken(HttpServletRequest request) {
    String authorizationHeader = request.getHeader("Authorization");
    log.debug("요청된 AuthorizationHeader: {}", authorizationHeader);
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      log.error("액세스 토큰이 담긴 헤더가 존재하지 않습니다.");
      throw new CustomException(ErrorCode.MISSING_AUTH_TOKEN);
    }
    return authorizationHeader.substring("Bearer ".length()).trim();
  }

  /**
   * 토큰을 비활성화 합니다
   */
  public void deactivateToken(String accessToken, String refreshTokenKey) {
    // accessToken 블랙리스트 등록
    if (isTokenBlacklisted(accessToken)) {
      log.error("accessToken이 이미 블랙리스트에 등록되어있습니다. accessToken: {}", accessToken);
    } else {
      log.debug("accessToken을 블랙리스트에 등록합니다");
      blacklistAccessToken(accessToken);
    }

    // redis에 저장된 리프레시 토큰 삭제
    deleteRefreshToken(refreshTokenKey);
  }

  // accessToken을 블랙리스트에 등록합니다
  private void blacklistAccessToken(String accessToken) {
    String key = BLACKLIST_PREFIX + accessToken;
    redisTemplate.opsForValue().set(
        key,
        BLACKLIST_VALUE,
        getRemainingValidationMilliSecond(accessToken),
        TimeUnit.MILLISECONDS);
  }

  // 해당 토큰이 블랙리스트 존재 확인
  private Boolean isTokenBlacklisted(String accessToken) {
    String key = BLACKLIST_PREFIX + accessToken;
    return redisTemplate.hasKey(key);
  }

  // redis에 저장된 리프레시 토큰을 삭제
  private void deleteRefreshToken(String key) {
    Boolean isDeleted = redisTemplate.delete(key);
    if (isDeleted) {
      log.debug("리프레시 토큰 삭제 성공");
    } else { // 토큰이 이미 삭제되었거나, 존재하지 않는 경우
      log.debug("리프레시 토큰을 찾을 수 없습니다.");
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }
  }
}
