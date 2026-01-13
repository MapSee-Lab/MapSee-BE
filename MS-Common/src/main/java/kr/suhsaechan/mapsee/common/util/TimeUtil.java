package kr.suhsaechan.mapsee.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {

  private TimeUtil() {
    throw new AssertionError();
  }

  public static final DateTimeFormatter STANDARD_DATETIME_FORMATTER
      = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public static final DateTimeFormatter ISO_DATETIME_FORMATTER
      = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  public static String formatStandardDateTime(LocalDateTime dateTime) {
    return dateTime.format(STANDARD_DATETIME_FORMATTER);
  }

  public static String getCurrentStandardDateTime() {
    return LocalDateTime.now().format(STANDARD_DATETIME_FORMATTER);
  }
}
