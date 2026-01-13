package kr.suhsaechan.mapsee.place.dto;

import kr.suhsaechan.mapsee.place.constant.PlaceWeekday;
import kr.suhsaechan.mapsee.place.entity.PlaceBusinessHour;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "영업시간 정보")
public class PlaceBusinessHourDto {

  @Schema(description = "요일", example = "MONDAY")
  private PlaceWeekday weekday;

  @Schema(description = "오픈 시간", example = "09:00:00")
  private LocalTime openTime;

  @Schema(description = "마감 시간", example = "22:00:00")
  private LocalTime closeTime;

  public static PlaceBusinessHourDto from(PlaceBusinessHour entity) {
    if (entity == null) {
      return null;
    }

    return PlaceBusinessHourDto.builder()
        .weekday(entity.getWeekday())
        .openTime(entity.getOpenTime())
        .closeTime(entity.getCloseTime())
        .build();
  }
}
