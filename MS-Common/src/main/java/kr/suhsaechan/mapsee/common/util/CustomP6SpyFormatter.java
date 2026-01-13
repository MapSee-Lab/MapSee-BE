package kr.suhsaechan.mapsee.common.util;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.FormatStyle;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomP6SpyFormatter implements MessageFormattingStrategy {

  private static final Map<String, Long> recentQueryCache = new ConcurrentHashMap<>();
  private static final int CACHE_SIZE_LIMIT = 100;
  private static final int EVICTION_SIZE = 20;

  @Override
  public String formatMessage(
      int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
    if (sql == null || sql.trim().isEmpty()) {
      return "";
    }

    // 중복 방지를 위한 키 생성
    String cacheKey = sql.trim() + connectionId;

    // 캐시 크기 제한 (LRU 방식으로 오래된 항목 제거)
    if (recentQueryCache.size() > CACHE_SIZE_LIMIT) {
      recentQueryCache.entrySet()
          .stream()
          .sorted(Map.Entry.comparingByValue())
          .limit(EVICTION_SIZE)
          .map(Map.Entry::getKey)
          .forEach(recentQueryCache::remove);
    }

    // 중복 검사 (타임스탬프 저장)
    Long lastSeen = recentQueryCache.putIfAbsent(cacheKey, System.currentTimeMillis());
    if (lastSeen != null) {
      // 이미 최근에 로깅된 쿼리는 무시
      return "";
    }

    // SQL 포맷팅
    String formattedSql = formatSql(category, sql);

    StringBuilder sb = new StringBuilder();
    sb.append("\n------------------------------------------------------------------------------------------");
    sb.append("\n[SQL] ").append(TimeUtil.getCurrentStandardDateTime());
    sb.append(" | ").append(elapsed).append("ms");
    sb.append(" | ").append(category);
    sb.append(" | connection ").append(connectionId);
    if (formattedSql.trim().toLowerCase(Locale.ROOT).startsWith("select")) {
      sb.append("\n[SELECT 쿼리]\n");
    } else if (formattedSql.trim().toLowerCase(Locale.ROOT).startsWith("insert")) {
      sb.append("\n[INSERT 쿼리]\n");
    } else if (formattedSql.trim().toLowerCase(Locale.ROOT).startsWith("update")) {
      sb.append("\n[UPDATE 쿼리]\n");
    } else if (formattedSql.trim().toLowerCase(Locale.ROOT).startsWith("delete")) {
      sb.append("\n[DELETE 쿼리]\n");
    } else {
      sb.append("\n[기타 쿼리]\n");
    }
    sb.append(formattedSql);
    sb.append("\n------------------------------------------------------------------------------------------");

    return sb.toString();
  }

  private String formatSql(String category, String sql) {
    if (sql == null || sql.trim().isEmpty()) {
      return sql;
    }

    // DDL은 HibernateSQL 포맷으로 출력
    if (category.contains("statement") && sql.trim().toLowerCase(Locale.ROOT).startsWith("create")) {
      return FormatStyle.DDL.getFormatter().format(sql);
    }

    // DML은 HibernateSQL 포맷으로 출력
    if (category.contains("statement") &&
        (sql.trim().toLowerCase(Locale.ROOT).startsWith("select") ||
            sql.trim().toLowerCase(Locale.ROOT).startsWith("insert") ||
            sql.trim().toLowerCase(Locale.ROOT).startsWith("update") ||
            sql.trim().toLowerCase(Locale.ROOT).startsWith("delete"))) {
      return FormatStyle.BASIC.getFormatter().format(sql);
    }

    return sql;
  }
}
