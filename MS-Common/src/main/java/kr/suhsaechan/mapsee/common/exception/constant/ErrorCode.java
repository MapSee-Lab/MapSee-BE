package kr.suhsaechan.mapsee.common.exception.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  // Global
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다."),

  INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 입력값입니다."),

  ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근이 거부되었습니다"),

  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),

  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메소드입니다."),

  UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 미디어 타입입니다."),

  DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류가 발생했습니다."),

  // Authentication
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),

  MISSING_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "인증 토큰이 필요합니다."),

  INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),

  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),

  EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),

  EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),

  REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "리프레시 토큰을 찾을 수 없습니다."),

  REFRESH_TOKEN_NOT_STORED(HttpStatus.UNAUTHORIZED, "Redis에 저장된 리프레시 토큰을 찾을 수 없습니다."),

  REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "Redis에 저장된 리프레시 토큰과 일치하지 않습니다."),

  TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "블랙리스트 처리된 토큰입니다."),

  // Member
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),

  INVALID_ONBOARDING_STEP(HttpStatus.BAD_REQUEST, "유효하지 않은 온보딩 단계입니다."),

  NAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다."),

  INVALID_NAME_LENGTH(HttpStatus.BAD_REQUEST, "닉네임은 2자 이상 50자 이하여야 합니다."),

  INVALID_BIRTH_DATE(HttpStatus.BAD_REQUEST, "유효하지 않은 생년월일 형식입니다."),

  AGE_RESTRICTION_UNDER_14(HttpStatus.BAD_REQUEST, "만 14세 미만은 회원가입이 불가능합니다."),

  INVALID_GENDER(HttpStatus.BAD_REQUEST, "유효하지 않은 성별 값입니다."),

  EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다."),

  INTEREST_NOT_FOUND(HttpStatus.BAD_REQUEST, "유효하지 않은 관심사 ID가 포함되어 있습니다."),

  MEMBER_ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "이미 탈퇴한 회원입니다."),

  INSUFFICIENT_INTEREST_SELECTION(HttpStatus.BAD_REQUEST, "관심사는 최소 3개 이상 선택해야 합니다."),

  DUPLICATE_INTEREST_IDS(HttpStatus.BAD_REQUEST, "중복된 관심사 ID가 포함되어 있습니다."),

  // AI Server / Network
  EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 API 호출 중 오류가 발생했습니다."),

  NETWORK_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "네트워크 통신 오류가 발생했습니다."),

  INVALID_API_KEY(HttpStatus.UNAUTHORIZED, "유효하지 않은 API Key입니다."),

  AI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI 서버 처리 중 오류가 발생했습니다."),

  // Content
  CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "콘텐츠를 찾을 수 없습니다."),

  URL_TOO_LONG(HttpStatus.BAD_REQUEST, "URL이 허용된 최대 길이를 초과했습니다."),

  THUMBNAIL_URL_TOO_LONG(HttpStatus.BAD_REQUEST, "썸네일 URL이 허용된 최대 길이를 초과했습니다."),

  CAPTION_TOO_LONG(HttpStatus.BAD_REQUEST, "캡션이 허용된 최대 길이를 초과했습니다."),

  MEMBER_TERMS_REQUIRED_NOT_AGREED(HttpStatus.BAD_REQUEST, "필수 약관에 동의하지 않았습니다."),

  // Place
  PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "장소를 찾을 수 없습니다."),

  GOOGLE_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "Google Places API에서 장소를 찾을 수 없습니다."),

  GOOGLE_PLACE_API_ERROR(HttpStatus.BAD_GATEWAY, "Google Places API 호출 중 오류가 발생했습니다."),

  // MemberPlace
  MEMBER_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "회원의 장소 정보를 찾을 수 없습니다."),

  PLACE_ALREADY_SAVED(HttpStatus.BAD_REQUEST, "이미 저장된 장소입니다."),

  CANNOT_DELETE_SAVED_PLACE(HttpStatus.BAD_REQUEST, "임시 저장된 장소만 삭제할 수 있습니다.");

  private final HttpStatus status;
  private final String message;
}
