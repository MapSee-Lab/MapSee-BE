package kr.suhsaechan.mapsee.web.config;

import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway 데이터베이스 마이그레이션 설정
 *
 * <p>application.yml의 flyway 설정을 통해 자동으로 마이그레이션이 실행됩니다.</p>
 *
 * <h3>주요 설정</h3>
 * <ul>
 *   <li>baseline-on-migrate: true - 기존 DB에서도 안전하게 마이그레이션</li>
 *   <li>baseline-version: 0.0.0 - 초기 베이스라인 버전</li>
 *   <li>locations: classpath:db/migration - 마이그레이션 스크립트 위치</li>
 * </ul>
 *
 * <h3>마이그레이션 파일 네이밍 규칙</h3>
 * <pre>
 * V{version}__{description}.sql
 * 예: V0.1.5__simplify_media_structure.sql
 * </pre>
 *
 * @see org.flywaydb.core.Flyway
 */
@Configuration
public class FlywayConfig {

  /**
   * Flyway 콜백 설정
   *
   * <p>마이그레이션 전후 정보를 로깅합니다.</p>
   *
   * @return Callback Flyway 콜백
   */
  @Bean
  public Callback flywayCallback() {
    return new Callback() {
      @Override
      public boolean supports(Event event, Context context) {
        return event == Event.BEFORE_MIGRATE || event == Event.AFTER_MIGRATE;
      }

      @Override
      public boolean canHandleInTransaction(Event event, Context context) {
        return true;
      }

      @Override
      public void handle(Event event, Context context) {
        if (event == Event.BEFORE_MIGRATE) {
          System.out.println("Starting Flyway migration...");
        } else if (event == Event.AFTER_MIGRATE) {
          System.out.println("Flyway migration completed.");
        }
      }

      @Override
      public String getCallbackName() {
        return "FlywayLoggingCallback";
      }
    };
  }
}
