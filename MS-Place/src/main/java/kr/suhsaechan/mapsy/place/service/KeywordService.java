package kr.suhsaechan.mapsy.place.service;

import kr.suhsaechan.mapsy.place.entity.Keyword;
import kr.suhsaechan.mapsy.place.entity.Place;
import kr.suhsaechan.mapsy.place.entity.PlaceKeyword;
import kr.suhsaechan.mapsy.place.repository.KeywordRepository;
import kr.suhsaechan.mapsy.place.repository.PlaceKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Keyword 비즈니스 로직 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeywordService {

  private final KeywordRepository keywordRepository;
  private final PlaceKeywordRepository placeKeywordRepository;

  /**
   * 키워드 생성 또는 조회
   * - 이미 존재하는 키워드면 조회 후 사용 횟수 증가
   * - 존재하지 않으면 새로 생성
   *
   * @param keywordText 키워드 문자열
   * @return Keyword 엔티티
   */
  @Transactional
  public Keyword createOrGet(String keywordText) {
    log.info("Creating or getting keyword: {}", keywordText);

    // 기존 키워드 조회
    return keywordRepository.findByKeyword(keywordText)
        .map(existingKeyword -> {
          // 기존 키워드 사용 횟수 증가
          existingKeyword.incrementCount();
          log.info("Incremented count for keyword: {}, new count: {}",
              keywordText, existingKeyword.getCount());
          return existingKeyword;
        })
        .orElseGet(() -> {
          // 새 키워드 생성
          Keyword newKeyword = Keyword.builder()
              .keyword(keywordText)
              .build();
          Keyword saved = keywordRepository.save(newKeyword);
          log.info("Created new keyword: {}", keywordText);
          return saved;
        });
  }

  /**
   * 장소에 키워드 목록 연결
   * - 기존 연결된 키워드는 건너뛰기
   * - 새 키워드는 생성 후 연결
   *
   * @param place    장소
   * @param keywords 키워드 문자열 목록
   */
  @Transactional
  public void linkKeywordsToPlace(Place place, List<String> keywords) {
    if (keywords == null || keywords.isEmpty()) {
      log.warn("No keywords to link to place: placeId={}", place.getId());
      return;
    }

    log.info("Linking {} keywords to place: placeId={}, placeName={}",
        keywords.size(), place.getId(), place.getName());

    for (String keywordText : keywords) {
      // 해시태그 정규화 (# 제거, 소문자 변환)
      String normalized = normalizeKeyword(keywordText);

      // 키워드 생성 또는 조회
      Keyword keyword = createOrGet(normalized);

      // 이미 연결되어 있는지 확인
      boolean alreadyLinked = placeKeywordRepository.existsByPlaceAndKeyword(place, keyword);

      if (!alreadyLinked) {
        // PlaceKeyword 생성 및 연결
        PlaceKeyword placeKeyword = PlaceKeyword.of(place, keyword);
        placeKeywordRepository.save(placeKeyword);
        log.info("Linked keyword '{}' to place '{}'", normalized, place.getName());
      } else {
        log.debug("Keyword '{}' already linked to place '{}'", normalized, place.getName());
      }
    }

    log.info("Successfully linked all keywords to place: placeId={}", place.getId());
  }

  /**
   * 키워드 정규화
   * - # 제거
   * - 앞뒤 공백 제거
   * - 소문자 변환
   *
   * @param keyword 원본 키워드
   * @return 정규화된 키워드
   */
  private String normalizeKeyword(String keyword) {
    if (keyword == null) {
      return "";
    }

    // # 제거, trim, 소문자 변환
    return keyword.replace("#", "").trim().toLowerCase();
  }

  /**
   * 트렌드 키워드 조회
   * - 트렌드 점수 기준 내림차순 정렬
   *
   * @param pageable 페이징 정보
   * @return Page<Keyword>
   */
  @Transactional(readOnly = true)
  public Page<Keyword> getTrendingKeywords(Pageable pageable) {
    log.info("Fetching trending keywords: page={}, size={}",
        pageable.getPageNumber(), pageable.getPageSize());

    return keywordRepository.findTopByTrendScore(pageable);
  }

  /**
   * 인기 키워드 조회 (사용 횟수 기준)
   * - count 기준 내림차순 정렬
   *
   * @param pageable 페이징 정보
   * @return Page<Keyword>
   */
  @Transactional(readOnly = true)
  public Page<Keyword> getPopularKeywords(Pageable pageable) {
    log.info("Fetching popular keywords: page={}, size={}",
        pageable.getPageNumber(), pageable.getPageSize());

    return keywordRepository.findAllByOrderByCountDesc(pageable);
  }

  /**
   * 키워드 자동완성 검색
   * - 키워드가 prefix로 시작하는 경우
   *
   * @param prefix   검색어 시작 문자열
   * @param pageable 페이징 정보
   * @return List<Keyword>
   */
  @Transactional(readOnly = true)
  public List<Keyword> searchKeywords(String prefix, Pageable pageable) {
    String normalized = normalizeKeyword(prefix);

    log.info("Searching keywords with prefix: {}", normalized);

    return keywordRepository.findByKeywordStartingWithOrderByCountDesc(normalized, pageable);
  }

  /**
   * 키워드로 장소 검색
   * - 특정 키워드가 연결된 모든 장소 조회
   *
   * @param keywordText 키워드 문자열
   * @return List<Place>
   */
  @Transactional(readOnly = true)
  public List<Place> findPlacesByKeyword(String keywordText) {
    String normalized = normalizeKeyword(keywordText);

    log.info("Finding places by keyword: {}", normalized);

    // 키워드 조회
    return keywordRepository.findByKeyword(normalized)
        .map(keyword -> {
          List<Place> places = placeKeywordRepository.findPlacesByKeyword(keyword);
          log.info("Found {} places for keyword '{}'", places.size(), normalized);
          return places;
        })
        .orElseGet(() -> {
          log.warn("Keyword not found: {}", normalized);
          return new ArrayList<>();
        });
  }

  /**
   * 여러 키워드로 장소 검색 (OR 조건)
   * - 키워드 목록 중 하나라도 포함된 장소 조회
   *
   * @param keywordTexts 키워드 문자열 목록
   * @return List<Place>
   */
  @Transactional(readOnly = true)
  public List<Place> findPlacesByKeywords(List<String> keywordTexts) {
    if (keywordTexts == null || keywordTexts.isEmpty()) {
      log.warn("No keywords provided for search");
      return new ArrayList<>();
    }

    log.info("Finding places by {} keywords", keywordTexts.size());

    // 키워드 정규화
    List<String> normalized = keywordTexts.stream()
        .map(this::normalizeKeyword)
        .toList();

    // 키워드 엔티티 조회
    List<Keyword> keywords = keywordRepository.findByKeywordIn(normalized);

    if (keywords.isEmpty()) {
      log.warn("No keywords found in database");
      return new ArrayList<>();
    }

    // 키워드로 장소 검색
    List<Place> places = placeKeywordRepository.findPlacesByKeywords(keywords);
    log.info("Found {} places for {} keywords", places.size(), keywords.size());

    return places;
  }

  /**
   * 장소의 키워드 목록 조회
   *
   * @param place 장소
   * @return List<Keyword>
   */
  @Transactional(readOnly = true)
  public List<Keyword> getKeywordsByPlace(Place place) {
    log.info("Fetching keywords for place: placeId={}, placeName={}",
        place.getId(), place.getName());

    List<PlaceKeyword> placeKeywords = placeKeywordRepository.findByPlace(place);

    List<Keyword> keywords = placeKeywords.stream()
        .map(PlaceKeyword::getKeyword)
        .toList();

    log.info("Found {} keywords for place '{}'", keywords.size(), place.getName());

    return keywords;
  }
}
