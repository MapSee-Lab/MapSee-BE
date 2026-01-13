package kr.suhsaechan.mapsee.place.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.suhsaechan.mapsee.common.exception.CustomException;
import kr.suhsaechan.mapsee.common.exception.constant.ErrorCode;
import kr.suhsaechan.mapsee.common.properties.PlaceProperties;
import kr.suhsaechan.mapsee.place.constant.PlacePlatform;
import kr.suhsaechan.mapsee.place.dto.GooglePlaceSearchDto;
import kr.suhsaechan.mapsee.place.entity.Place;
import kr.suhsaechan.mapsee.place.entity.PlacePlatformReference;
import kr.suhsaechan.mapsee.place.repository.PlacePlatformReferenceRepository;
import kr.suhsaechan.mapsee.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Google Places API를 통한 장소 검색 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GooglePlaceSearcher implements PlacePlatformSearcher {

  private final OkHttpClient okHttpClient;
  private final ObjectMapper objectMapper;
  private final PlaceProperties placeProperties;
  private final PlaceRepository placeRepository;
  private final PlacePlatformReferenceRepository placePlatformReferenceRepository;

  private static final String GOOGLE_PLACES_BASE_URL = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json";
  private static final String SEARCH_FIELDS = "place_id,name,formatted_address,geometry,types,business_status,icon,photos,rating,user_ratings_total";

  /**
   * 상호명으로 Google Place 검색
   * <p>
   * 1차: DB에서 캐싱된 Place 검색 (name + address 기반)
   * 2차: DB에 없으면 Google API 호출
   * 3차: Google API 결과를 DB에 저장하여 캐싱
   *
   * @param placeName 장소명
   * @param address   주소 (fallback용, 현재 미사용)
   * @param language  언어 코드 (ko, en, ja, zh)
   * @return Google Place 상세 정보 (place_id, 좌표 등)
   * @throws CustomException Google Places API 호출 실패 또는 장소를 찾을 수 없을 때
   */
  @Override
  public GooglePlaceSearchDto.PlaceDetail searchPlaceDetail(String placeName, String address, String language) {
    log.info("Place Search Start: name={}, address={}, language={}", placeName, address, language);

    // 1️⃣ DB에서 먼저 검색 (정규화된 키워드로 캐싱 조회)
    String normalizedName = (placeName != null) ? placeName.trim() : null;
    String normalizedAddress = (address != null && !address.isEmpty()) ? address.trim() : null;

    if (normalizedName != null && !normalizedName.isEmpty()) {
      Optional<Place> cachedPlace = placeRepository.findByNormalizedNameAndAddress(
          normalizedName, normalizedAddress);

      if (cachedPlace.isPresent()) {
        Place existing = cachedPlace.get();
        log.info("Place found in DB (cache hit): placeId={}, name={}", existing.getId(), existing.getName());

        // 좌표 변경 검증 (Google API 재호출 필요 여부)
        if (shouldUpdateCoordinates(existing)) {
          log.info("Coordinates may have changed, refreshing from Google API");
          return searchGooglePlaceApi(placeName, address, language);
        }

        // PlaceId 검증 및 재호출 처리
        String googlePlaceId = getGooglePlaceId(existing);
        if (googlePlaceId == null) {
          log.warn("PlacePlatformReference not found for Place: {}, refreshing from Google API", existing.getId());
          return searchGooglePlaceApi(placeName, address, language);
        }

        // DB 캐시 히트 - PlaceDetail 변환하여 반환
        return convertPlaceToPlaceDetail(existing, googlePlaceId);
      }

      log.info("Place not found in DB (cache miss), calling Google Places API");
    }

    // 2️⃣ DB에 없으면 Google API 호출
    return searchGooglePlaceApi(placeName, address, language);
  }

  /**
   * Google Places API 호출
   * <p>
   * DB 캐시에 없을 때만 호출됨
   *
   * @param placeName 장소명
   * @param address   주소
   * @param language  언어 코드
   * @return Google Place 상세 정보
   * @throws CustomException API 호출 실패 시
   */
  private GooglePlaceSearchDto.PlaceDetail searchGooglePlaceApi(String placeName, String address, String language) {
    String googleApiKey = placeProperties.getGoogleApiKey();

    if (googleApiKey == null || googleApiKey.isEmpty()) {
      log.error("Google Places API key not configured: placeName={}", placeName);
      throw new CustomException(ErrorCode.INVALID_API_KEY);
    }

    try {
      log.info("Calling Google Places API");
      log.info("Place Name: {}", placeName);
      log.info("Address: {}", address);
      log.info("Language: {}", language);

      // URL 생성
      String url = buildSearchUrl(placeName, language, googleApiKey);

      log.info("Request URL: {}", url);

      // OkHttp로 API 호출
      Request request = new Request.Builder()
          .url(url)
          .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
          .addHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
          .addHeader("Accept", "application/json")
          .get()
          .build();

      GooglePlaceSearchDto response;
      try (Response httpResponse = okHttpClient.newCall(request).execute()) {
        if (!httpResponse.isSuccessful()) {
          log.error("Google Places API HTTP error: code={}", httpResponse.code());
          throw new CustomException(ErrorCode.GOOGLE_PLACE_API_ERROR);
        }

        if (httpResponse.body() == null) {
          log.error("Google Places API response body is null");
          throw new CustomException(ErrorCode.GOOGLE_PLACE_API_ERROR);
        }

        String responseBody = httpResponse.body().string();
        response = objectMapper.readValue(responseBody, GooglePlaceSearchDto.class);
      }

      // 결과 파싱
      String status = response.getStatus();
      log.info("API Response Status: {}", status);
      log.info("Candidates Count: {}",
          response.getCandidates() != null ? response.getCandidates().size() : 0);

      if ("OK".equals(status)
          && response.getCandidates() != null
          && !response.getCandidates().isEmpty()) {

        GooglePlaceSearchDto.Candidate candidate = response.getCandidates().get(0);

        log.info("Selected Candidate:");
        log.info("  Place ID: {}", candidate.getPlaceId());
        log.info("  Name: {}", candidate.getName());
        log.info("  Address: {}", candidate.getFormattedAddress());

        GooglePlaceSearchDto.PlaceDetail placeDetail = GooglePlaceSearchDto.PlaceDetail.builder()
            .placeId(candidate.getPlaceId())
            .name(candidate.getName())
            .address(candidate.getFormattedAddress())
            .latitude(candidate.getGeometry().getLocation().getLat())
            .longitude(candidate.getGeometry().getLocation().getLng())
            .country(extractCountryCode(candidate.getFormattedAddress()))
            .types(candidate.getTypes())
            .businessStatus(candidate.getBusinessStatus())
            .iconUrl(candidate.getIcon())
            .rating(candidate.getRating())
            .userRatingsTotal(candidate.getUserRatingsTotal())
            .photoUrls(buildPhotoUrls(candidate.getPhotos(), googleApiKey))
            .build();

        log.info("Google Places API Search Success");
        log.info("Final Result: placeId={}, name={}, rating={}",
            placeDetail.getPlaceId(), placeDetail.getName(), placeDetail.getRating());
        return placeDetail;

      } else {
        // Google Places API 상태 코드별 에러 처리
        log.error("Google Places API Search Failed");
        log.error("Place Name: {}", placeName);
        log.error("Status: {}", status);

        if ("REQUEST_DENIED".equals(status)) {
          throw new CustomException(ErrorCode.INVALID_API_KEY);
        } else if ("INVALID_REQUEST".equals(status)) {
          throw new CustomException(ErrorCode.INVALID_REQUEST);
        } else if ("OVER_QUERY_LIMIT".equals(status)) {
          throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        } else if ("ZERO_RESULTS".equals(status)) {
          throw new CustomException(ErrorCode.GOOGLE_PLACE_NOT_FOUND);
        } else {
          throw new CustomException(ErrorCode.GOOGLE_PLACE_API_ERROR);
        }
      }

    } catch (CustomException e) {
      // NetworkUtil에서 발생한 CustomException은 그대로 전파
      log.error("Google Places API error: placeName={}, error={}", placeName, e.getMessage());
      throw e;
    } catch (Exception e) {
      // 예상치 못한 예외
      log.error("Unexpected error during Google Places API call: placeName={}", placeName, e);
      throw new CustomException(ErrorCode.GOOGLE_PLACE_API_ERROR);
    }
  }

  /**
   * Google Places API 검색 URL 생성
   */
  private String buildSearchUrl(String placeName, String language, String googleApiKey) {
    log.debug("Building search URL for: {}", placeName);

    String encodedPlaceName = URLEncoder.encode(placeName, StandardCharsets.UTF_8);

    String url = String.format("%s?input=%s&inputtype=textquery&fields=%s&language=%s&key=%s",
        GOOGLE_PLACES_BASE_URL,
        encodedPlaceName,
        SEARCH_FIELDS,
        language,
        googleApiKey
    );

    return url;
  }

  /**
   * 사진 URL 배열 생성
   * <p>
   * photo_reference를 실제 Google Photos API URL로 변환
   *
   * @param photos       사진 정보 리스트
   * @param googleApiKey API 키
   * @return 사진 URL 배열 (최대 10개)
   */
  private List<String> buildPhotoUrls(List<GooglePlaceSearchDto.Photo> photos, String googleApiKey) {
    if (photos == null || photos.isEmpty()) {
      return null;
    }

    return photos.stream()
        .limit(10)  // 최대 10개
        .map(photo -> String.format(
            "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=%s&key=%s",
            photo.getPhotoReference(),
            googleApiKey
        ))
        .collect(Collectors.toList());
  }

  /**
   * 주소에서 국가 코드 추출
   * <p>
   * 간단 구현: 주소 문자열에서 국가명 패턴 매칭
   * 추후 더 정교한 로직으로 개선 필요
   *
   * @param address Google API의 formatted_address
   * @return 국가 코드 (ISO 3166-1 alpha-2)
   */
  private String extractCountryCode(String address) {
    if (address == null || address.isEmpty()) {
      return "XX";
    }

    // 한국
    if (address.contains("South Korea") || address.contains("대한민국") || address.contains("Korea")) {
      return "KR";
    }
    // 미국
    if (address.contains("United States") || address.contains("USA") || address.contains("US")) {
      return "US";
    }
    // 일본
    if (address.contains("Japan") || address.contains("日本")) {
      return "JP";
    }
    // 중국
    if (address.contains("China") || address.contains("中国")) {
      return "CN";
    }

    // 기본값
    log.debug("Could not extract country code from address: {}", address);
    return "XX";
  }

  /**
   * Place 엔티티를 PlaceDetail DTO로 변환
   * <p>
   * DB 캐시된 Place를 API 응답 형식으로 변환
   *
   * @param place         Place 엔티티
   * @param googlePlaceId Google place_id
   * @return PlaceDetail DTO
   */
  private GooglePlaceSearchDto.PlaceDetail convertPlaceToPlaceDetail(Place place, String googlePlaceId) {
    return GooglePlaceSearchDto.PlaceDetail.builder()
        .placeId(googlePlaceId)
        .name(place.getName())
        .address(place.getAddress())
        .latitude(place.getLatitude())
        .longitude(place.getLongitude())
        .country(place.getCountry())
        .types(place.getTypes())
        .businessStatus(place.getBusinessStatus())
        .iconUrl(place.getIconUrl())
        .rating(place.getRating())
        .userRatingsTotal(place.getUserRatingsTotal())
        .photoUrls(place.getPhotoUrls())
        .build();
  }

  /**
   * Place의 Google PlaceId 조회
   * <p>
   * PlacePlatformReference 테이블에서 Google platform의 placeId를 조회
   *
   * @param place Place 엔티티
   * @return Google placeId (없으면 null)
   */
  private String getGooglePlaceId(Place place) {
    return placePlatformReferenceRepository
        .findByPlaceAndPlacePlatform(place, PlacePlatform.GOOGLE)
        .map(PlacePlatformReference::getPlacePlatformId)
        .orElse(null);
  }

  /**
   * 좌표 변경 검증
   * <p>
   * Place가 오래되었거나 좌표 정확도가 낮으면 Google API 재호출 필요
   * 현재는 단순 구현 (항상 false 반환)
   * <p>
   * 향후 개선 방향:
   * - createdAt이 1년 이상 지났으면 재조회
   * - 좌표 정확도가 낮으면 (소수점 자리수 부족) 재조회
   *
   * @param place Place 엔티티
   * @return true: 재조회 필요, false: 캐시 사용
   */
  private boolean shouldUpdateCoordinates(Place place) {
    // 현재는 항상 캐시 사용 (좌표 변경 감지 미구현)
    // 필요시 아래 로직 활성화:
    // LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
    // return place.getCreatedAt().isBefore(oneYearAgo);
    return false;
  }
}
