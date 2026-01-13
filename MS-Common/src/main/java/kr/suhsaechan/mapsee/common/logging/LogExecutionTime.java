package kr.suhsaechan.mapsee.common.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메소드 실행 시간을 로그로 남기는 어노테이션 특정 메소드에 대해서만 상세한 실행 시간 로깅이 필요할 때 사용
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {

  /**
   * 로그에 표시될 메소드 설명
   */
  String description() default "";

  /**
   * 성능 임계값 (밀리초) 이 시간을 초과하면 WARN 레벨로 로깅
   */
  long threshold() default 1000;
}
