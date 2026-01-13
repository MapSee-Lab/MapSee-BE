package kr.suhsaechan.mapsee.member.repository;

import kr.suhsaechan.mapsee.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {

  Optional<Member> findByEmail(String email);
  boolean existsByName(String name);
  boolean existsByNameAndIdNot(String name, UUID id);
  boolean existsByEmail(String email);
}
