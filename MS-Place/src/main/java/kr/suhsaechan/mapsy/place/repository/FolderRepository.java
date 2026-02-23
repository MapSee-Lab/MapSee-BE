package kr.suhsaechan.mapsy.place.repository;

import kr.suhsaechan.mapsy.member.entity.Member;
import kr.suhsaechan.mapsy.place.entity.Folder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {

  List<Folder> findByOwnerAndDeletedAtIsNullOrderByCreatedAtAsc(Member owner);

  @Query("SELECT f, COUNT(fp) FROM Folder f " +
      "LEFT JOIN FolderPlace fp ON fp.folder = f AND fp.deletedAt IS NULL " +
      "WHERE f.owner = :owner AND f.deletedAt IS NULL " +
      "GROUP BY f " +
      "ORDER BY f.createdAt ASC")
  List<Object[]> findByOwnerWithPlaceCount(@Param("owner") Member owner);

  Optional<Folder> findByOwnerAndIsDefaultTrueAndDeletedAtIsNull(Member owner);

  Optional<Folder> findByIdAndDeletedAtIsNull(UUID id);
}
