package kr.suhsaechan.mapsee.web.config;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp 설정
 * 브라우저와 유사한 HTTP 클라이언트 Bean을 생성합니다.
 */
@Configuration
public class OkHttpConfig {

  /**
   * OkHttpClient Bean 생성
   * CookieJar 및 Logging Interceptor 설정 포함
   */
  @Bean
  public OkHttpClient okHttpClient() {
    // CookieJar 설정 (브라우저처럼 쿠키 자동 관리)
    CookieJar cookieJar = new CookieJar() {
      private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

      @Override
      public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cookieStore.put(url.host(), cookies);
      }

      @Override
      public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = cookieStore.get(url.host());
        return cookies != null ? cookies : new ArrayList<>();
      }
    };

    // Logging Interceptor (디버깅용)
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

    return new OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build();
  }
}
