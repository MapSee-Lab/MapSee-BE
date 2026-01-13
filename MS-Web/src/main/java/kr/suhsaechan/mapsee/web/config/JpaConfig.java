package kr.suhsaechan.mapsee.web.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "kr.suhsaechan.mapsee")
@EntityScan(basePackages = "kr.suhsaechan.mapsee")
public class JpaConfig {

}
