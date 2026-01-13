package kr.suhsaechan.mapsee.member.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import kr.suhsaechan.mapsee.common.exception.CustomException;
import kr.suhsaechan.mapsee.common.exception.constant.ErrorCode;
import kr.suhsaechan.mapsee.member.dto.FcmNotificationRequest;
import kr.suhsaechan.mapsee.member.entity.FcmToken;
import kr.suhsaechan.mapsee.member.entity.Member;
import kr.suhsaechan.mapsee.member.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Firebase Cloud Messaging 알림 전송 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FcmService {

  private final FcmTokenRepository fcmTokenRepository;

  /**
   * 단일 기기로 푸시 알림 전송
   *
   * @param fcmToken FCM 토큰
   * @param title    알림 제목
   * @param body     알림 본문
   * @param data     추가 데이터
   * @param imageUrl 이미지 URL (선택)
   */
  public void sendNotification(
      String fcmToken,
      String title,
      String body,
      Map<String, String> data,
      String imageUrl) {
    try {
      // Firebase App이 초기화되어 있는지 확인
      if (FirebaseApp.getApps().isEmpty()) {
        log.error("Firebase App이 초기화되지 않았습니다. FCM 알림을 전송할 수 없습니다.");
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
      }

      // 알림 메시지 빌더 생성
      Notification.Builder notificationBuilder = Notification.builder()
          .setTitle(title)
          .setBody(body);

      // 이미지 URL이 있으면 추가
      if (imageUrl != null && !imageUrl.isBlank()) {
        notificationBuilder.setImage(imageUrl);
      }

      Notification notification = notificationBuilder.build();

      // 메시지 빌더 생성
      Message.Builder messageBuilder = Message.builder()
          .setToken(fcmToken)
          .setNotification(notification);

      // 추가 데이터가 있으면 추가
      if (data != null && !data.isEmpty()) {
        messageBuilder.putAllData(data);
      }

      Message message = messageBuilder.build();

      // 메시지 전송
      String response = FirebaseMessaging.getInstance().send(message);
      log.info("FCM 알림 전송 성공: {}", response);

    } catch (FirebaseMessagingException e) {
      log.error("FCM 알림 전송 실패: {}", e.getMessage(), e);

      // 토큰이 유효하지 않은 경우 처리
      if (isInvalidTokenError(e)) {
        log.warn("유효하지 않은 FCM 토큰: {}", fcmToken);
        deleteInvalidToken(fcmToken);
      }

      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Member ID를 기반으로 해당 회원의 모든 기기에 푸시 알림 전송
   *
   * @param memberId Member ID
   * @param title    알림 제목
   * @param body     알림 본문
   * @param data     추가 데이터
   * @param imageUrl 이미지 URL (선택)
   */
  @Transactional(readOnly = true)
  public void sendNotificationToMember(UUID memberId, String title, String body, Map<String, String> data, String imageUrl) {
    // 회원의 모든 FCM 토큰 조회
    List<FcmToken> tokens = fcmTokenRepository.findByMemberId(memberId);

    if (tokens.isEmpty()) {
      log.warn("Member ID {}에 등록된 FCM 토큰이 없습니다.", memberId);
      return;
    }

    log.debug("Member ID {}에 등록된 FCM 토큰 {}개를 찾았습니다.", memberId, tokens.size());

    // 모든 기기에 알림 전송
    sendNotificationToMultipleDevices(tokens, title, body, data, imageUrl);
  }

  /**
   * Member 엔티티를 기반으로 해당 회원의 모든 기기에 푸시 알림 전송
   *
   * @param member   Member 엔티티
   * @param title    알림 제목
   * @param body     알림 본문
   * @param data     추가 데이터
   * @param imageUrl 이미지 URL (선택)
   */
  @Transactional(readOnly = true)
  public void sendNotificationToAllDevices(Member member, String title, String body, Map<String, String> data, String imageUrl) {
    // 회원의 모든 FCM 토큰 조회
    List<FcmToken> tokens = fcmTokenRepository.findByMember(member);

    if (tokens.isEmpty()) {
      log.warn("Member {}에 등록된 FCM 토큰이 없습니다.", member.getEmail());
      return;
    }

    log.debug("Member {}에 등록된 FCM 토큰 {}개를 찾았습니다.", member.getEmail(), tokens.size());

    // 모든 기기에 알림 전송
    sendNotificationToMultipleDevices(tokens, title, body, data, imageUrl);
  }

  /**
   * 여러 기기에 푸시 알림 전송 (멀티캐스트)
   *
   * @param tokens   FCM 토큰 리스트
   * @param title    알림 제목
   * @param body     알림 본문
   * @param data     추가 데이터
   * @param imageUrl 이미지 URL (선택)
   */
  private void sendNotificationToMultipleDevices(List<FcmToken> tokens, String title, String body, Map<String, String> data, String imageUrl) {
    try {
      // Firebase App이 초기화되어 있는지 확인
      if (FirebaseApp.getApps().isEmpty()) {
        log.error("Firebase App이 초기화되지 않았습니다. FCM 알림을 전송할 수 없습니다.");
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
      }

      // FCM 토큰 문자열 리스트 추출
      List<String> fcmTokens = tokens.stream()
          .map(FcmToken::getFcmToken)
          .collect(Collectors.toList());

      // 알림 메시지 빌더 생성
      Notification.Builder notificationBuilder = Notification.builder()
          .setTitle(title)
          .setBody(body);

      // 이미지 URL이 있으면 추가
      if (imageUrl != null && !imageUrl.isBlank()) {
        notificationBuilder.setImage(imageUrl);
      }

      Notification notification = notificationBuilder.build();

      // 멀티캐스트 메시지 빌더 생성
      MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
          .addAllTokens(fcmTokens)
          .setNotification(notification);

      // 추가 데이터가 있으면 추가
      if (data != null && !data.isEmpty()) {
        messageBuilder.putAllData(data);
      }

      MulticastMessage message = messageBuilder.build();

      // 메시지 전송 (최대 500개까지 한 번에 전송 가능)
      BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

      log.info("FCM 멀티캐스트 알림 전송 완료: 성공 {}, 실패 {}", response.getSuccessCount(), response.getFailureCount());

      // 실패한 토큰 처리
      if (response.getFailureCount() > 0) {
        handleFailedTokens(tokens, response);
      }

    } catch (FirebaseMessagingException e) {
      log.error("FCM 멀티캐스트 알림 전송 실패: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 전송 실패한 토큰 처리
   *
   * @param tokens   FCM 토큰 엔티티 리스트
   * @param response 배치 응답
   */
  @Transactional
  protected void handleFailedTokens(List<FcmToken> tokens, BatchResponse response) {
    List<SendResponse> responses = response.getResponses();

    for (int i = 0; i < responses.size(); i++) {
      SendResponse sendResponse = responses.get(i);

      // 전송 실패한 경우
      if (!sendResponse.isSuccessful()) {
        FirebaseMessagingException exception = sendResponse.getException();

        if (exception != null && isInvalidTokenError(exception)) {
          // 유효하지 않은 토큰인 경우 DB에서 삭제
          FcmToken token = tokens.get(i);
          log.warn("유효하지 않은 FCM 토큰 삭제: memberId={}, deviceId={}", token.getMember().getId(), token.getDeviceId());
          fcmTokenRepository.delete(token);
        }
      }
    }
  }

  /**
   * FCM 토큰이 유효하지 않은 에러인지 확인
   *
   * @param exception FirebaseMessagingException
   * @return 유효하지 않은 토큰 에러 여부
   */
  private boolean isInvalidTokenError(FirebaseMessagingException exception) {
    MessagingErrorCode errorCode = exception.getMessagingErrorCode();
    return errorCode == MessagingErrorCode.INVALID_ARGUMENT
        || errorCode == MessagingErrorCode.UNREGISTERED;
  }

  /**
   * 유효하지 않은 FCM 토큰 삭제
   *
   * @param fcmToken FCM 토큰 문자열
   */
  @Transactional
  protected void deleteInvalidToken(String fcmToken) {
    fcmTokenRepository.findByFcmToken(fcmToken).ifPresent(token -> {
      log.info("유효하지 않은 FCM 토큰 삭제: memberId={}, deviceId={}", token.getMember().getId(), token.getDeviceId());
      fcmTokenRepository.delete(token);
    });
  }
}
