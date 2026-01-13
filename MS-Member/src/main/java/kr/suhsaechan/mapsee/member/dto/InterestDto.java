package kr.suhsaechan.mapsee.member.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class InterestDto {
  private UUID id;
  private String name;
}