package kr.suhsaechan.mapsee.sns.repository;

import kr.suhsaechan.mapsee.sns.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentRepository extends JpaRepository<Content, UUID> {

  // SNS URL로 Content 조회
  Optional<Content> findByOriginalUrl(String originalUrl);

}
