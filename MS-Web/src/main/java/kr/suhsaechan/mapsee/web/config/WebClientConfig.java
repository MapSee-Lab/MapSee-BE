package kr.suhsaechan.mapsee.web.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient 설정
 * 외부 API 통신을 위한 HTTP 클라이언트 Bean을 생성합니다.
 */
@Configuration
public class WebClientConfig {

  /**
   * WebClient Bean 생성
   * Timeout 설정 및 메모리 버퍼 크기 설정 포함
   */
  @Bean
  public WebClient webClient(WebClient.Builder builder) {
    HttpClient httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)  // 연결 타임아웃 10초
        .responseTimeout(Duration.ofSeconds(30))               // 응답 타임아웃 30초
        .doOnConnected(conn ->
            conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))   // 읽기 타임아웃 30초
                .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS))  // 쓰기 타임아웃 30초
        );

    return builder
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .codecs(configurer -> configurer
            .defaultCodecs()
            .maxInMemorySize(16 * 1024 * 1024))  // 16MB 버퍼
        .build();
  }
}
