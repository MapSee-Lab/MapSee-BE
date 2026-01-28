package kr.suhsaechan.mapsy.place.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import kr.suhsaechan.mapsy.place.constant.PlaceSavedStatus;
import kr.suhsaechan.mapsy.place.dto.KeywordDto;
import kr.suhsaechan.mapsy.place.dto.PlaceDto;
import kr.suhsaechan.mapsy.place.entity.Keyword;
import kr.suhsaechan.mapsy.place.entity.Place;
import kr.suhsaechan.mapsy.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Feed 비즈니스 로직 서비스
 * - 홈 화면 피드 데이터 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FeedService {

  private static final int DEFAULT_TOP_PLACES_LIMIT = 10;

  private final PlaceRepository placeRepository;
  private final MemberPlaceService memberPlaceService;
  private final KeywordService keywordService;

  /**
   * 최신 장소 피드 조회
   * - 생성일 기준 최신순 정렬
   *
   * @param pageable 페이지 정보
   * @return Page<PlaceDto>
   */
  public Page<PlaceDto> getLatestPlaces(Pageable pageable) {
    log.info("Fetching latest places: page={}, size={}",
        pageable.getPageNumber(), pageable.getPageSize());

    Page<Place> places = placeRepository.findAllByOrderByCreatedAtDesc(pageable);

    log.info("Found {} latest places (total: {})",
        places.getNumberOfElements(), places.getTotalElements());

    return places.map(PlaceDto::from);
  }

  /**
   * 인기 장소 피드 조회
   * - 저장 횟수 기준 내림차순 정렬
   *
   * @param pageable 페이지 정보
   * @return Page<PlaceDto>
   */
  public Page<PlaceDto> getPopularPlaces(Pageable pageable) {
    log.info("Fetching popular places: page={}, size={}",
        pageable.getPageNumber(), pageable.getPageSize());

    Page<Place> places = placeRepository.findPopularPlaces(PlaceSavedStatus.SAVED, pageable);

    log.info("Found {} popular places (total: {})",
        places.getNumberOfElements(), places.getTotalElements());

    return places.map(PlaceDto::from);
  }

  /**
   * 내 TOP 저장 장소 조회
   * - 저장일 기준 최신순
   * - 최대 10개
   *
   * @param memberId 회원 ID
   * @return List<PlaceDto>
   */
  public List<PlaceDto> getMyTopPlaces(UUID memberId) {
    log.info("Fetching my top places for member: {}", memberId);

    return memberPlaceService.getMyTopPlaces(memberId, DEFAULT_TOP_PLACES_LIMIT);
  }

  /**
   * 트렌드 키워드 조회
   * - 트렌드 점수 기준 내림차순 정렬
   *
   * @param size 조회 개수
   * @return List<KeywordDto>
   */
  public List<KeywordDto> getTrendingKeywords(int size) {
    log.info("Fetching trending keywords: size={}", size);

    Page<Keyword> keywords = keywordService.getTrendingKeywords(PageRequest.of(0, size));

    log.info("Found {} trending keywords", keywords.getNumberOfElements());

    return keywords.getContent().stream()
        .map(KeywordDto::from)
        .collect(Collectors.toList());
  }
}
