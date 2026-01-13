package kr.suhsaechan.mapsee.member.repository;

import kr.suhsaechan.mapsee.member.constant.InterestCategory;
import kr.suhsaechan.mapsee.member.entity.Interest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface InterestRepository extends JpaRepository<Interest, UUID> {

  /**
   * 카테고리별 관심사 조회
   */
  List<Interest> findByCategory(InterestCategory category);

  /**
   * 전체 관심사 조회 (카테고리 및 이름 순서로 정렬)
   */
  @Query("SELECT i FROM Interest i ORDER BY i.category ASC, i.name ASC")
  List<Interest> findAllOrderByCategoryAndName();

  /**
   * 카테고리와 이름으로 존재 여부 확인
   */
  boolean existsByCategoryAndName(InterestCategory category, String name);

  /**
   * 이름으로 관심사 조회
   */
  Optional<Interest> findByName(String name);
}
