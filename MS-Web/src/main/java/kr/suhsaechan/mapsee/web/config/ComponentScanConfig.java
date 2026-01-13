package kr.suhsaechan.mapsee.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 컴포넌트 스캔 설정 모든 모듈의 패키지를 스캔하여 Bean으로 등록
 */
@Configuration
@ComponentScan(basePackages = "kr.suhsaechan.mapsee")
public class ComponentScanConfig {

}
