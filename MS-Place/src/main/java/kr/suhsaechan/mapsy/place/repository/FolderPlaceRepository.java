package kr.suhsaechan.mapsy.place.repository;

import kr.suhsaechan.mapsy.place.entity.Folder;
import kr.suhsaechan.mapsy.place.entity.FolderPlace;
import kr.suhsaechan.mapsy.place.entity.Place;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderPlaceRepository extends JpaRepository<FolderPlace, UUID> {

  @Query("SELECT fp FROM FolderPlace fp " +
      "JOIN FETCH fp.place " +
      "WHERE fp.folder = :folder " +
      "AND fp.deletedAt IS NULL " +
      "ORDER BY fp.position ASC")
  List<FolderPlace> findByFolderWithPlaceOrderByPosition(@Param("folder") Folder folder);

  Optional<FolderPlace> findByFolderAndPlaceAndDeletedAtIsNull(Folder folder, Place place);

  boolean existsByFolderAndPlaceAndDeletedAtIsNull(Folder folder, Place place);

  List<FolderPlace> findByFolderAndDeletedAtIsNull(Folder folder);

  @Query("SELECT COUNT(fp) FROM FolderPlace fp WHERE fp.folder = :folder AND fp.deletedAt IS NULL")
  int countByFolderAndDeletedAtIsNull(@Param("folder") Folder folder);

  @Query("SELECT COALESCE(MAX(fp.position), 0) FROM FolderPlace fp WHERE fp.folder = :folder AND fp.deletedAt IS NULL")
  int findMaxPositionByFolder(@Param("folder") Folder folder);
}
