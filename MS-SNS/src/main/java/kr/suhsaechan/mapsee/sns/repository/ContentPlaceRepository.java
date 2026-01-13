package kr.suhsaechan.mapsee.sns.repository;

import kr.suhsaechan.mapsee.sns.entity.ContentPlace;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentPlaceRepository extends JpaRepository<ContentPlace, UUID> {

  // Content의 모든 ContentPlace 삭제
  void deleteByContentId(UUID contentId);

  // Content의 모든 ContentPlace 삭제 후 즉시 flush
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("DELETE FROM ContentPlace cp WHERE cp.content.id = :contentId")
  void deleteByContentIdWithFlush(@Param("contentId") UUID contentId);

  // Content ID로 ContentPlace 목록 조회 (Place를 Fetch Join하여 N+1 문제 해결, position 순서대로)
  @Query("SELECT cp FROM ContentPlace cp JOIN FETCH cp.place WHERE cp.content.id = :contentId ORDER BY cp.position ASC")
  List<ContentPlace> findByContentIdWithPlace(@Param("contentId") UUID contentId);
}
