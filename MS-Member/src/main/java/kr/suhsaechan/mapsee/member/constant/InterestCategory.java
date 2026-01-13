package kr.suhsaechan.mapsee.member.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum InterestCategory {

  FOOD("맛집/푸드"),
  CAFE_DESSERT("카페/디저트"),
  LOCAL_MARKET("로컬시장/골목"),
  NATURE_OUTDOOR("자연/아웃도어"),
  URBAN_PHOTOSPOTS("도시산책/포토스팟"),
  CULTURE_ART("문화/예술"),
  HISTORY_ARCHITECTURE("역사/건축/종교"),
  EXPERIENCE_CLASS("체험/클래스"),
  SHOPPING_FASHION("쇼핑/패션"),
  NIGHTLIFE("나이트라이프/음주"),
  WELLNESS("웰니스/휴식"),
  FAMILY_KIDS("가족/아이동반"),
  KPOP_CULTURE("K-POP·K-컬처"),
  DRIVE_SUBURBS("드라이브/근교");

  private final String displayName;

  @JsonValue
  public String toValue() {
    return this.name();
  }

  @JsonCreator
  public static InterestCategory fromValue(String value) {
    return Arrays.stream(values())
        .filter(category -> category.name().equals(value))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Invalid InterestCategory: " + value));
  }
}
