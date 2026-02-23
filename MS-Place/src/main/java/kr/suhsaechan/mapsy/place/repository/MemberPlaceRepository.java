package kr.suhsaechan.mapsy.place.repository;

import kr.suhsaechan.mapsy.member.entity.Member;
import kr.suhsaechan.mapsy.place.constant.PlaceSavedStatus;
import kr.suhsaechan.mapsy.place.entity.MemberPlace;
import kr.suhsaechan.mapsy.place.entity.Place;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberPlaceRepository extends JpaRepository<MemberPlace, UUID> {

  /**
   * 회원과 저장 상태로 MemberPlace 목록 조회 (삭제되지 않은 것만)
   *
   * @param member 회원
   * @param savedStatus 저장 상태
   * @return MemberPlace 목록
   */
  List<MemberPlace> findByMemberAndSavedStatusAndDeletedAtIsNull(
      Member member,
      PlaceSavedStatus savedStatus
  );

  /**
   * 회원과 장소로 MemberPlace 조회 (삭제되지 않은 것만)
   *
   * @param member 회원
   * @param place 장소
   * @return MemberPlace (Optional)
   */
  Optional<MemberPlace> findByMemberAndPlaceAndDeletedAtIsNull(
      Member member,
      Place place
  );

  /**
   * 회원과 장소로 MemberPlace 존재 여부 확인 (삭제되지 않은 것만)
   *
   * @param member 회원
   * @param place 장소
   * @return 존재 여부
   */
  boolean existsByMemberAndPlaceAndDeletedAtIsNull(
      Member member,
      Place place
  );

  /**
   * 회원과 저장 상태로 MemberPlace 목록 조회 (Place와 함께 Fetch Join)
   * - N+1 문제 방지를 위한 Fetch Join
   * - 최신순으로 정렬
   *
   * @param member 회원
   * @param savedStatus 저장 상태
   * @return MemberPlace 목록 (Place 포함)
   */
  @Query("SELECT mp FROM MemberPlace mp " +
      "JOIN FETCH mp.place " +
      "WHERE mp.member = :member " +
      "AND mp.savedStatus = :savedStatus " +
      "AND mp.deletedAt IS NULL " +
      "ORDER BY mp.createdAt DESC")
  List<MemberPlace> findByMemberAndSavedStatusWithPlace(
      @Param("member") Member member,
      @Param("savedStatus") PlaceSavedStatus savedStatus
  );

  /**
   * 회원의 북마크 페이지네이션 조회
   * - N+1 문제 방지를 위한 Fetch Join
   * - countQuery를 분리하여 Page 객체 지원
   *
   * @param memberId 회원 ID
   * @param savedStatus 저장 상태 (SAVED, TEMPORARY 등)
   * @param pageable 페이지 정보
   * @return 북마크 Page
   */
  @Query(value = "SELECT mp FROM MemberPlace mp " +
      "JOIN FETCH mp.place " +
      "WHERE mp.member.id = :memberId " +
      "AND mp.savedStatus = :savedStatus " +
      "AND mp.deletedAt IS NULL",
      countQuery = "SELECT COUNT(mp) FROM MemberPlace mp " +
          "WHERE mp.member.id = :memberId " +
          "AND mp.savedStatus = :savedStatus " +
          "AND mp.deletedAt IS NULL")
  Page<MemberPlace> findBookmarksByMemberIdAndSavedStatus(
      @Param("memberId") UUID memberId,
      @Param("savedStatus") PlaceSavedStatus savedStatus,
      Pageable pageable
  );

  /**
   * MemberPlace ID로 조회 (회원 검증 포함)
   *
   * @param id MemberPlace ID
   * @param memberId 회원 ID
   * @return MemberPlace (Optional)
   */
  @Query("SELECT mp FROM MemberPlace mp " +
      "WHERE mp.id = :id " +
      "AND mp.member.id = :memberId " +
      "AND mp.deletedAt IS NULL")
  Optional<MemberPlace> findByIdAndMemberIdAndDeletedAtIsNull(
      @Param("id") UUID id,
      @Param("memberId") UUID memberId
  );

  /**
   * 회원의 TOP 저장 장소 조회
   * - Pageable의 size로 개수 제한, sort로 정렬 가능
   *
   * @param memberId 회원 ID
   * @param savedStatus 저장 상태
   * @param pageable 페이지 정보 (size, sort 사용)
   * @return MemberPlace 목록
   */
  @Query("SELECT mp FROM MemberPlace mp " +
      "JOIN FETCH mp.place " +
      "WHERE mp.member.id = :memberId " +
      "AND mp.savedStatus = :savedStatus " +
      "AND mp.deletedAt IS NULL")
  List<MemberPlace> findTopPlacesByMemberIdAndSavedStatus(
      @Param("memberId") UUID memberId,
      @Param("savedStatus") PlaceSavedStatus savedStatus,
      Pageable pageable
  );
}
