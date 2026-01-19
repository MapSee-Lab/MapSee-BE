package kr.suhsaechan.mapsy.member.dto;

import kr.suhsaechan.mapsy.member.constant.MemberGender;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

  private String name;
  private MemberGender gender; // MALE, FEMALE, NONE
  private LocalDate birthDate;

}
