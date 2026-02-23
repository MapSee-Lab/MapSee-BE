package kr.suhsaechan.mapsy.place.repository;

import kr.suhsaechan.mapsy.member.entity.Member;
import kr.suhsaechan.mapsy.place.entity.Folder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {

  List<Folder> findByOwnerAndDeletedAtIsNullOrderByCreatedAtAsc(Member owner);

  Optional<Folder> findByOwnerAndIsDefaultTrueAndDeletedAtIsNull(Member owner);

  Optional<Folder> findByIdAndDeletedAtIsNull(UUID id);
}
