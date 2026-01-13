package kr.suhsaechan.mapsee.member.dto;

import kr.suhsaechan.mapsee.member.constant.MemberGender;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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
  private List<UUID> interestIds; // 관심사 목록

}
