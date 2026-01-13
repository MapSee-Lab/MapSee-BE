package kr.suhsaechan.mapsee.web.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Cloud Messaging 설정
 * Firebase Admin SDK를 초기화하여 푸시 알림 기능을 제공합니다.
 */
@Configuration
@Slf4j
public class FirebaseConfig {

  @Value("${firebase.credentials.path}")
  private String credentialsPath;

  /**
   * Firebase App 초기화
   */
  @PostConstruct
  public void initialize() {
    try {
      // Firebase App이 이미 초기화되어 있는지 확인
      if (FirebaseApp.getApps().isEmpty()) {
        // Service Account JSON 파일 로드
        ClassPathResource resource = new ClassPathResource(credentialsPath);

        if (!resource.exists()) {
          log.error("Firebase 인증 파일을 찾을 수 없습니다: {}", credentialsPath);
          log.error("FCM 푸시 알림 기능이 비활성화됩니다.");
          return;
        }

        try (InputStream serviceAccount = resource.getInputStream()) {
          FirebaseOptions options = FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccount))
              .build();

          FirebaseApp.initializeApp(options);
          log.info("✅ Firebase Admin SDK 초기화 완료");
          log.debug("Firebase 인증 파일 경로: {}", credentialsPath);
        }
      } else {
        log.debug("Firebase App이 이미 초기화되어 있습니다.");
      }
    } catch (IOException e) {
      log.error("Firebase 초기화 실패: {}", e.getMessage(), e);
      log.error("FCM 푸시 알림 기능이 비활성화됩니다.");
    }
  }
}
