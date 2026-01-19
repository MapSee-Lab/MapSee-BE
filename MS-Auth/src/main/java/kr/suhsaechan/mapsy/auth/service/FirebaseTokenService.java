package kr.suhsaechan.mapsy.auth.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import kr.suhsaechan.mapsy.auth.dto.FirebaseUserInfo;
import kr.suhsaechan.mapsy.common.exception.CustomException;
import kr.suhsaechan.mapsy.common.exception.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FirebaseTokenService {

  /**
   * Firebase ID Token 검증
   *
   * @param idToken 클라이언트가 제공한 Firebase ID Token
   * @return 검증된 FirebaseToken 객체
   * @throws CustomException 토큰 검증 실패 시
   */
  public FirebaseToken verifyIdToken(String idToken) {
    try {
      FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
      FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);

      log.info("Firebase 토큰 검증 성공 - uid={}, email={}",
          decodedToken.getUid(), decodedToken.getEmail());

      return decodedToken;
    } catch (FirebaseAuthException e) {
      log.error("Firebase 토큰 검증 실패 - error={}", e.getMessage());

      // 에러 코드에 따른 처리
      String authErrorCode = e.getAuthErrorCode().name();

      if ("EXPIRED_ID_TOKEN".equals(authErrorCode)) {
        throw new CustomException(ErrorCode.FIREBASE_TOKEN_EXPIRED);
      } else if ("INVALID_ID_TOKEN".equals(authErrorCode) || "INVALID_ARGUMENT".equals(authErrorCode)) {
        throw new CustomException(ErrorCode.FIREBASE_TOKEN_INVALID);
      } else {
        throw new CustomException(ErrorCode.FIREBASE_TOKEN_VERIFICATION_FAILED);
      }
    }
  }

  /**
   * FirebaseToken에서 사용자 정보 추출
   *
   * @param decodedToken 검증된 FirebaseToken
   * @return UserInfo DTO
   */
  public FirebaseUserInfo extractUserInfo(FirebaseToken decodedToken) {
    String email = decodedToken.getEmail();
    String name = decodedToken.getName();  // Firebase에서 제공하는 displayName
    String profileImageUrl = decodedToken.getPicture();

    // sign_in_provider 추출 (google.com, kakao.com 등)
    Map<String, Object> claims = decodedToken.getClaims();
    Map<String, Object> firebase = (Map<String, Object>) claims.get("firebase");
    String signInProvider = firebase != null ? (String) firebase.get("sign_in_provider") : null;

    log.debug("Firebase 사용자 정보 추출 - email={}, name={}, provider={}",
        email, name, signInProvider);

    return FirebaseUserInfo.builder()
        .email(email)
        .name(name)
        .profileImageUrl(profileImageUrl)
        .signInProvider(signInProvider)
        .uid(decodedToken.getUid())
        .build();
  }
}
