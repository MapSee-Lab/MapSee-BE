package kr.suhsaechan.mapsee.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.suhsaechan.mapsee.auth.constant.SecurityUrl;
import kr.suhsaechan.mapsee.auth.dto.CustomUserDetails;
import kr.suhsaechan.mapsee.auth.jwt.JwtUtil;
import kr.suhsaechan.mapsee.auth.service.CustomUserDetailsService;
import kr.suhsaechan.mapsee.common.exception.CustomException;
import kr.suhsaechan.mapsee.common.exception.ErrorResponse;
import kr.suhsaechan.mapsee.common.exception.constant.ErrorCode;
import kr.suhsaechan.mapsee.member.entity.Member;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 토큰 기반 인증 필터
 */
@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String uri = request.getRequestURI();
    String method = request.getMethod();
    log.debug("요청된 URI: {}, Method: {}", uri, method);

    // OPTIONS 요청 (CORS Preflight) 즉시 통과
    if ("OPTIONS".equals(method)) {
      log.debug("OPTIONS 요청 - CORS Preflight 통과");
      filterChain.doFilter(request, response);
      return;
    }

    // 화이트리스트 체크 : 화이트리스트 경로면 필터링 건너뜀
    if (isWhitelistedPath(uri)) {
      filterChain.doFilter(request, response);
      return;
    }

    // 요청 타입 구분 : API 요청만 처리
    boolean isApiRequest = uri.startsWith("/api/");

    try {
      String token = null;
      String bearerToken = request.getHeader("Authorization");

      // API 요청 : Authorization 헤더에서 "Bearer " 토큰 추출
      if (isApiRequest) {
        log.debug("일반 API 요청입니다.");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
          token = bearerToken.substring(7).trim(); // "Bearer " 제거
        }
      }

      // 토큰 검증: 토큰이 유효하면 인증 설정
      if (token != null && jwtUtil.validateToken(token)) {
        Authentication authentication = jwtUtil.getAuthentication(token);

        // 탈퇴한 회원 체크
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
          CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
          Member member = userDetails.getMember();

          if (member != null && member.isDeleted()) {
            log.error("탈퇴한 회원의 API 접근 시도 - memberId={}", member.getId());
            sendErrorResponse(response, ErrorCode.MEMBER_ALREADY_WITHDRAWN);
            return;
          }
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 인증 성공
        filterChain.doFilter(request, response);
        return;
      }

      // 토큰이 없거나 유효하지 않은 경우
      if (isApiRequest) {
        // 토큰 없음
        if (token == null) {
          log.error("토큰이 존재하지 않습니다.");
          sendErrorResponse(response, ErrorCode.MISSING_AUTH_TOKEN);
        } else { // 유효하지 않은 토큰
          log.error("토큰이 유효하지 않습니다.");
          sendErrorResponse(response, ErrorCode.INVALID_ACCESS_TOKEN);
        }
        return; // 필터 체인 진행하지 않음
      }
    } catch (ExpiredJwtException e) {
      log.error("토큰 만료: {}", e.getMessage());
      // 토큰 만료 예외 처리
      if (isApiRequest) {
        sendErrorResponse(response, ErrorCode.EXPIRED_ACCESS_TOKEN);
      }
      return;
    } catch (CustomException e) {
      log.error("토큰 검증 실패: {}", e.getMessage());
      if (isApiRequest) {
        // 블랙리스트 등 커스텀 에러 상황을 명확히 전달
        sendErrorResponse(response, ErrorCode.TOKEN_BLACKLISTED);
      }
      return;
    }

    // 필터 체인 계속 진행
    filterChain.doFilter(request, response);
  }

  /**
   * 에러 응답을 JSON 형태로 클라이언트에 전송
   */
  private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(errorCode.getStatus().value());
    response.setCharacterEncoding("UTF-8");
    ErrorResponse errorResponse = ErrorResponse.getResponse(errorCode);
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(response.getWriter(), errorResponse);
  }

  /**
   * 화이트리스트 경로 확인 (인증x)
   */
  private boolean isWhitelistedPath(String uri) {
    return SecurityUrl.AUTH_WHITELIST
        .stream()
        .anyMatch(pattern -> pathMatcher.match(pattern, uri));
  }
}
