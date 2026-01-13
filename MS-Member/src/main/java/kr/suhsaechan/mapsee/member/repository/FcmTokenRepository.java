package kr.suhsaechan.mapsee.member.repository;

import kr.suhsaechan.mapsee.member.entity.FcmToken;
import kr.suhsaechan.mapsee.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FcmTokenRepository extends JpaRepository<FcmToken, UUID> {

  Optional<FcmToken> findByMemberAndDeviceId(Member member, String deviceId);

  Optional<FcmToken> findByMemberIdAndDeviceId(UUID memberId, String deviceId);

  List<FcmToken> findByMember(Member member);

  List<FcmToken> findByMemberId(UUID memberId);

  Optional<FcmToken> findByFcmToken(String fcmToken);

  List<FcmToken> findByLastUsedAtBefore(LocalDateTime dateTime);

  void deleteByMemberAndDeviceId(Member member, String deviceId);

  void deleteByMember(Member member);
}
