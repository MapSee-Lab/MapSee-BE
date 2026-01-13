package kr.suhsaechan.mapsee.sns.repository;

import kr.suhsaechan.mapsee.member.entity.Member;
import kr.suhsaechan.mapsee.sns.entity.Content;
import kr.suhsaechan.mapsee.sns.entity.ContentMember;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentMemberRepository extends JpaRepository<ContentMember, UUID> {

  /**
   * Content와 Member로 ContentMember 조회
   */
  Optional<ContentMember> findByContentAndMember(Content content, Member member);

  /**
   * Content와 Member로 ContentMember 존재 여부 확인
   */
  boolean existsByContentAndMember(Content content, Member member);

  /**
   * Content ID로 모든 ContentMember 조회
   */
  List<ContentMember> findByContentId(UUID contentId);

  /**
   * Content ID로 알림 미전송된 ContentMember 조회 (Member Fetch Join으로 N+1 방지)
   */
  @Query("SELECT cm FROM ContentMember cm " +
      "JOIN FETCH cm.member " +
      "WHERE cm.content.id = :contentId AND cm.notified = false")
  List<ContentMember> findUnnotifiedMembersWithMember(@Param("contentId") UUID contentId);

  /**
   * Member ID로 ContentMember 조회 (페이징 등에 활용)
   */
  List<ContentMember> findByMemberId(UUID memberId);

  /**
   * Content로 모든 ContentMember 조회 (Member Fetch Join으로 N+1 방지)
   * - MemberPlace 생성 시 사용
   */
  @Query("SELECT cm FROM ContentMember cm " +
      "JOIN FETCH cm.member " +
      "WHERE cm.content = :content")
  List<ContentMember> findAllByContentWithMember(@Param("content") Content content);

}
