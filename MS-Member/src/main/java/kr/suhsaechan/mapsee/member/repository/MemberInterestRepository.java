package kr.suhsaechan.mapsee.member.repository;

import kr.suhsaechan.mapsee.member.entity.MemberInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MemberInterestRepository extends JpaRepository<MemberInterest, UUID> {

  List<MemberInterest> findByMemberId(UUID memberId);

  void deleteByMemberId(UUID memberId);
}

