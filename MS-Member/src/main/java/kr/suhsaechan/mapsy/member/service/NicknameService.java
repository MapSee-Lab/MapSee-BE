package kr.suhsaechan.mapsy.member.service;

import kr.suhsaechan.mapsy.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.suhsaechan.suhrandomengine.core.SuhRandomKit;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NicknameService {

  private final MemberRepository memberRepository;

  /**
   * Spring Bean으로 SuhRandomKit 초기화
   */
  @Bean
  public SuhRandomKit suhRandomKit() {
    return SuhRandomKit.builder()
        .locale("ko")          // 한국어 닉네임
        .numberLength(4)       // 숫자 접미사 4자리
        .uuidLength(4)         // UUID 접미사 4자리
        .build();
  }

  /**
   * 중복되지 않는 랜덤 닉네임 생성
   *
   * @return 생성된 닉네임
   */
  public String generateUniqueNickname() {
    SuhRandomKit nicknameGenerator = suhRandomKit();
    String nickname;
    int attempts = 0;
    int maxAttempts = 100;  // 최대 시도 횟수

    do {
      // 기본 닉네임 생성 (예: "멋진고양이")
      nickname = nicknameGenerator.simpleNickname();

      attempts++;

      // 중복 체크
      if (!memberRepository.existsByName(nickname)) {
        log.info("랜덤 닉네임 생성 성공 - nickname={}, attempts={}", nickname, attempts);
        return nickname;
      }

      // 중복이면 숫자 접미사 추가 (예: "멋진고양이-1234")
      if (attempts > 10) {
        nickname = nicknameGenerator.nicknameWithNumber();

        if (!memberRepository.existsByName(nickname)) {
          log.info("숫자 접미사 닉네임 생성 성공 - nickname={}, attempts={}", nickname, attempts);
          return nickname;
        }
      }

      // 그래도 중복이면 UUID 접미사 추가 (예: "멋진고양이-abcd")
      if (attempts > 50) {
        nickname = nicknameGenerator.nicknameWithUuid();

        if (!memberRepository.existsByName(nickname)) {
          log.info("UUID 접미사 닉네임 생성 성공 - nickname={}, attempts={}", nickname, attempts);
          return nickname;
        }
      }

    } while (attempts < maxAttempts);

    // 최대 시도 횟수 초과 시 UUID 접미사 강제 적용
    nickname = nicknameGenerator.nicknameWithUuid();
    log.warn("최대 시도 횟수 초과, UUID 닉네임 강제 생성 - nickname={}", nickname);
    return nickname;
  }
}
