package kr.suhsaechan.mapsy.sns.service;

import kr.suhsaechan.mapsy.ai.dto.AiCallbackRequest;
import kr.suhsaechan.mapsy.ai.dto.AiCallbackRequest.PlaceDetailCallback;
import kr.suhsaechan.mapsy.ai.dto.AiCallbackRequest.SnsInfoCallback;
import kr.suhsaechan.mapsy.ai.dto.AiCallbackRequest.ExtractionStatistics;
import kr.suhsaechan.mapsy.ai.dto.AiCallbackResponse;
import kr.suhsaechan.mapsy.common.constant.ContentStatus;
import kr.suhsaechan.mapsy.common.exception.CustomException;
import kr.suhsaechan.mapsy.common.exception.constant.ErrorCode;
import kr.suhsaechan.mapsy.place.constant.PlacePlatform;
import kr.suhsaechan.mapsy.place.constant.PlaceSavedStatus;
import kr.suhsaechan.mapsy.place.entity.MemberPlace;
import kr.suhsaechan.mapsy.place.entity.Place;
import kr.suhsaechan.mapsy.place.entity.PlacePlatformReference;
import kr.suhsaechan.mapsy.place.repository.MemberPlaceRepository;
import kr.suhsaechan.mapsy.place.repository.PlacePlatformReferenceRepository;
import kr.suhsaechan.mapsy.place.repository.PlaceRepository;
import kr.suhsaechan.mapsy.place.service.KeywordService;
import kr.suhsaechan.mapsy.sns.entity.Content;
import kr.suhsaechan.mapsy.sns.entity.ContentMember;
import kr.suhsaechan.mapsy.sns.entity.ContentPlace;
import kr.suhsaechan.mapsy.sns.repository.ContentMemberRepository;
import kr.suhsaechan.mapsy.sns.repository.ContentPlaceRepository;
import kr.suhsaechan.mapsy.sns.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import kr.suhsaechan.mapsy.member.service.FcmService;

/**
 * AI 서버 Webhook Callback 처리
 *
 * @see <a href="https://github.com/MapSee-Lab/MapSy-BE/issues/16">GitHub Issue #16</a>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiCallbackService {

  private final ContentRepository contentRepository;
  private final ContentMemberRepository contentMemberRepository;
  private final PlaceRepository placeRepository;
  private final ContentPlaceRepository contentPlaceRepository;
  private final PlacePlatformReferenceRepository placePlatformReferenceRepository;
  private final MemberPlaceRepository memberPlaceRepository;
  private final FcmService fcmService;
  private final KeywordService keywordService;

  /**
   * AI 서버로부터 받은 Callback 처리
   *
   * @param request AI Callback 요청
   * @return AI Callback 응답
   */
  @Transactional
  public AiCallbackResponse processAiServerCallback(AiCallbackRequest request) {
    UUID contentId = request.getContentId();

    if (contentId == null) {
      log.error("contentId is null in callback request. resultStatus={}", request.getResultStatus());
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    log.info("Processing AI callback: contentId={}, resultStatus={}",
        contentId, request.getResultStatus());

    // Content 조회
    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND));

    // 결과 상태에 따라 분기 처리
    if ("SUCCESS".equals(request.getResultStatus())) {
      processSuccessCallback(content, request);
    } else if ("FAILED".equals(request.getResultStatus())) {
      processFailedCallback(content, request);
    } else {
      log.error("Unknown resultStatus: {}", request.getResultStatus());
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    log.info("AI callback processed successfully: contentId={}", contentId);

    return AiCallbackResponse.builder()
        .received(true)
        .contentId(contentId)
        .build();
  }

  /**
   * 성공 Callback 처리
   */
  private void processSuccessCallback(Content content, AiCallbackRequest request) {
    log.debug("Processing SUCCESS callback for contentId={}", content.getId());

    // statistics 로깅 (DB 저장 안 함)
    logStatistics(content.getId(), request.getStatistics());

    // Content가 이미 COMPLETED 상태인지 확인 (재처리 요청 판단용)
    boolean isContentAlreadyCompleted = (content.getStatus() == ContentStatus.COMPLETED);

    if (isContentAlreadyCompleted) {
      log.info("Content already COMPLETED. Updating existing data: contentId={}", content.getId());
      contentPlaceRepository.deleteByContentIdWithFlush(content.getId());
      log.debug("Deleted existing ContentPlaces for contentId={}", content.getId());
    }

    // Content 상태를 COMPLETED로 변경
    content.setStatus(ContentStatus.COMPLETED);

    // SnsInfo로 Content 메타데이터 업데이트
    updateContentWithSnsInfo(content, request.getSnsInfo());

    contentRepository.save(content);

    // Place 생성 및 Content 연결
    int placeCount = 0;
    if (request.getPlaceDetails() != null && !request.getPlaceDetails().isEmpty()) {
      List<PlaceDetailCallback> placeDetails = request.getPlaceDetails();
      log.info("Received {} places for contentId={} (update mode: {}). Starting Place creation.",
          placeDetails.size(), content.getId(), isContentAlreadyCompleted);

      List<Place> savedPlaces = new ArrayList<>();
      int position = 0;
      for (PlaceDetailCallback placeDetail : placeDetails) {
        try {
          // Place 생성 또는 조회
          Place place = createOrGetPlaceFromPlaceDetail(placeDetail);
          savedPlaces.add(place);

          // ContentPlace 연결 (순서 포함)
          createContentPlace(content, place, position++);

          // 키워드 연결
          if (placeDetail.getKeywords() != null && !placeDetail.getKeywords().isEmpty()) {
            keywordService.linkKeywordsToPlace(place, placeDetail.getKeywords());
            log.debug("Linked {} keywords to place: {}", placeDetail.getKeywords().size(), place.getName());
          }

          log.debug("Successfully processed place: {} (id={})", place.getName(), place.getId());
        } catch (Exception e) {
          log.error("Failed to process place: {}. Error: {}", placeDetail.getName(), e.getMessage(), e);
        }
      }

      placeCount = savedPlaces.size();
      log.info("Successfully saved {} out of {} places for contentId={}",
          placeCount, placeDetails.size(), content.getId());
    } else {
      log.warn("No places found in callback for contentId={}", content.getId());
    }

    // 알림 전송
    sendContentCompleteNotification(content, placeCount);
  }

  /**
   * statistics 로깅
   */
  private void logStatistics(UUID contentId, ExtractionStatistics statistics) {
    if (statistics == null) {
      return;
    }

    log.info("Extraction statistics for contentId={}: totalExtracted={}, totalFound={}, failedSearches={}",
        contentId,
        statistics.getTotalExtracted(),
        statistics.getTotalFound(),
        statistics.getFailedSearches() != null ? statistics.getFailedSearches() : "[]");

    if (statistics.getExtractedPlaceNames() != null) {
      log.debug("Extracted place names for contentId={}: {}", contentId, statistics.getExtractedPlaceNames());
    }
  }

  /**
   * SnsInfo로 Content 메타데이터 업데이트
   */
  private void updateContentWithSnsInfo(Content content, SnsInfoCallback snsInfo) {
    if (snsInfo == null) {
      log.warn("SnsInfo is null for contentId={}. Skipping metadata update.", content.getId());
      return;
    }

    // platform 업데이트
    if (snsInfo.getPlatform() != null) {
      try {
        content.setPlatform(kr.suhsaechan.mapsy.sns.constant.ContentPlatform.valueOf(snsInfo.getPlatform()));
      } catch (IllegalArgumentException e) {
        log.error("Invalid platform value: {}. Keeping existing platform for contentId={}",
            snsInfo.getPlatform(), content.getId());
      }
    }

    // contentType 업데이트
    if (snsInfo.getContentType() != null) {
      content.setContentType(snsInfo.getContentType());
    }

    // url → originalUrl 업데이트
    if (snsInfo.getUrl() != null) {
      String newUrl = snsInfo.getUrl();
      if (!newUrl.equals(content.getOriginalUrl())) {
        Optional<Content> existingContent = contentRepository.findByOriginalUrl(newUrl);
        if (existingContent.isPresent() && !existingContent.get().getId().equals(content.getId())) {
          log.warn("Cannot update originalUrl: URL already exists in another Content. " +
                  "currentContentId={}, existingContentId={}, url={}",
              content.getId(), existingContent.get().getId(), newUrl);
        } else {
          content.setOriginalUrl(newUrl);
        }
      }
    }

    // author → platformUploader 업데이트
    if (snsInfo.getAuthor() != null) {
      content.setPlatformUploader(snsInfo.getAuthor());
    }

    // caption 업데이트
    if (snsInfo.getCaption() != null) {
      content.setCaption(snsInfo.getCaption());
    }

    // likesCount 업데이트
    if (snsInfo.getLikesCount() != null) {
      content.setLikesCount(snsInfo.getLikesCount());
    }

    // commentsCount 업데이트
    if (snsInfo.getCommentsCount() != null) {
      content.setCommentsCount(snsInfo.getCommentsCount());
    }

    // postedAt 업데이트 (ISO 8601 파싱)
    if (snsInfo.getPostedAt() != null) {
      try {
        OffsetDateTime odt = OffsetDateTime.parse(snsInfo.getPostedAt());
        content.setPostedAt(odt.toLocalDateTime());
      } catch (DateTimeParseException e) {
        log.warn("Failed to parse postedAt: {}. Skipping.", snsInfo.getPostedAt());
      }
    }

    // hashtags 업데이트
    if (snsInfo.getHashtags() != null) {
      content.setHashtags(snsInfo.getHashtags());
    }

    // thumbnailUrl 업데이트
    if (snsInfo.getThumbnailUrl() != null) {
      content.setThumbnailUrl(snsInfo.getThumbnailUrl());
    }

    // imageUrls 업데이트
    if (snsInfo.getImageUrls() != null) {
      content.setImageUrls(snsInfo.getImageUrls());
    }

    // authorProfileImageUrl 업데이트
    if (snsInfo.getAuthorProfileImageUrl() != null) {
      content.setAuthorProfileImageUrl(snsInfo.getAuthorProfileImageUrl());
    }

    log.debug("Updated Content with SnsInfo: contentId={}, platform={}, contentType={}, author={}",
        content.getId(),
        snsInfo.getPlatform(),
        snsInfo.getContentType(),
        snsInfo.getAuthor());
  }

  /**
   * 실패 Callback 처리
   */
  private void processFailedCallback(Content content, AiCallbackRequest request) {
    log.error("Processing FAILED callback for contentId={}, errorMessage={}",
        content.getId(), request.getErrorMessage());

    content.setStatus(ContentStatus.FAILED);
    contentRepository.save(content);
  }

  /**
   * PlaceDetail로부터 Place 생성 또는 조회
   *
   * 중복 체크 순서:
   * 1. naverPlaceId로 PlacePlatformReference 검색
   * 2. name + 좌표로 기존 Place 검색
   * 3. 없으면 신규 생성 + PlacePlatformReference 생성
   */
  private Place createOrGetPlaceFromPlaceDetail(PlaceDetailCallback placeDetail) {
    // 1. naverPlaceId로 중복 체크
    if (placeDetail.getPlaceId() != null) {
      Optional<PlacePlatformReference> existingRef = placePlatformReferenceRepository
          .findByPlacePlatformAndPlacePlatformId(PlacePlatform.NAVER, placeDetail.getPlaceId());

      if (existingRef.isPresent()) {
        Place existingPlace = existingRef.get().getPlace();
        updatePlaceFromPlaceDetail(existingPlace, placeDetail);
        log.debug("Found existing place by naverPlaceId: id={}, name={}", existingPlace.getId(), existingPlace.getName());
        return placeRepository.save(existingPlace);
      }
    }

    // 2. name + 좌표로 중복 체크 (fallback)
    if (placeDetail.getLatitude() != null && placeDetail.getLongitude() != null) {
      BigDecimal latitude = BigDecimal.valueOf(placeDetail.getLatitude());
      BigDecimal longitude = BigDecimal.valueOf(placeDetail.getLongitude());

      Optional<Place> existing = placeRepository.findByNameAndLatitudeAndLongitude(
          placeDetail.getName(),
          latitude,
          longitude
      );

      if (existing.isPresent()) {
        Place existingPlace = existing.get();
        updatePlaceFromPlaceDetail(existingPlace, placeDetail);

        // PlacePlatformReference가 없으면 생성
        createPlacePlatformReferenceIfNotExists(existingPlace, placeDetail.getPlaceId());

        log.debug("Found existing place by name+coords: id={}, name={}", existingPlace.getId(), existingPlace.getName());
        return placeRepository.save(existingPlace);
      }
    }

    // 3. 신규 Place 생성
    Place newPlace = createNewPlace(placeDetail);
    Place savedPlace = placeRepository.save(newPlace);

    // PlacePlatformReference 생성
    createPlacePlatformReferenceIfNotExists(savedPlace, placeDetail.getPlaceId());

    log.debug("Created new place: id={}, name={}, naverPlaceId={}",
        savedPlace.getId(), savedPlace.getName(), placeDetail.getPlaceId());
    return savedPlace;
  }

  /**
   * 새 Place 엔티티 생성
   */
  private Place createNewPlace(PlaceDetailCallback placeDetail) {
    BigDecimal latitude = placeDetail.getLatitude() != null
        ? BigDecimal.valueOf(placeDetail.getLatitude())
        : BigDecimal.ZERO;
    BigDecimal longitude = placeDetail.getLongitude() != null
        ? BigDecimal.valueOf(placeDetail.getLongitude())
        : BigDecimal.ZERO;

    Place place = Place.builder()
        .name(placeDetail.getName())
        .address(placeDetail.getAddress())
        .roadAddress(placeDetail.getRoadAddress())
        .latitude(latitude)
        .longitude(longitude)
        .country("KR")  // 기본값
        .build();

    // 모든 필드 설정
    updatePlaceFromPlaceDetail(place, placeDetail);

    return place;
  }

  /**
   * PlaceDetail로 기존 Place 업데이트
   */
  private void updatePlaceFromPlaceDetail(Place place, PlaceDetailCallback placeDetail) {
    // 기본 정보
    if (placeDetail.getCategory() != null) {
      place.setBusinessType(placeDetail.getCategory());
    }
    if (placeDetail.getDescription() != null) {
      place.setDescription(placeDetail.getDescription());
    }

    // 위치 정보
    if (placeDetail.getAddress() != null) {
      place.setAddress(placeDetail.getAddress());
    }
    if (placeDetail.getRoadAddress() != null) {
      place.setRoadAddress(placeDetail.getRoadAddress());
    }
    if (placeDetail.getSubwayInfo() != null) {
      place.setSubwayInfo(placeDetail.getSubwayInfo());
    }
    if (placeDetail.getDirectionsText() != null) {
      place.setDirectionsText(placeDetail.getDirectionsText());
    }

    // 평점/리뷰
    if (placeDetail.getRating() != null) {
      place.setRating(BigDecimal.valueOf(placeDetail.getRating()));
    }
    if (placeDetail.getVisitorReviewCount() != null) {
      place.setVisitorReviewCount(placeDetail.getVisitorReviewCount());
    }
    if (placeDetail.getBlogReviewCount() != null) {
      place.setBlogReviewCount(placeDetail.getBlogReviewCount());
    }

    // 영업 정보
    if (placeDetail.getBusinessStatus() != null) {
      place.setBusinessStatus(placeDetail.getBusinessStatus());
    }
    if (placeDetail.getBusinessHours() != null) {
      place.setBusinessHours(placeDetail.getBusinessHours());
    }
    if (placeDetail.getOpenHoursDetail() != null) {
      place.setOpenHoursDetail(placeDetail.getOpenHoursDetail());
    }
    if (placeDetail.getHolidayInfo() != null) {
      place.setHolidayInfo(placeDetail.getHolidayInfo());
    }

    // 연락처/링크
    if (placeDetail.getPhoneNumber() != null) {
      place.setPhone(placeDetail.getPhoneNumber());
    }
    if (placeDetail.getHomepageUrl() != null) {
      place.setHomepageUrl(placeDetail.getHomepageUrl());
    }
    if (placeDetail.getNaverMapUrl() != null) {
      place.setNaverMapUrl(placeDetail.getNaverMapUrl());
    }
    if (placeDetail.getReservationAvailable() != null) {
      place.setReservationAvailable(placeDetail.getReservationAvailable());
    }

    // 부가 정보
    if (placeDetail.getAmenities() != null) {
      place.setAmenities(placeDetail.getAmenities());
    }
    if (placeDetail.getTvAppearances() != null) {
      place.setTvAppearances(placeDetail.getTvAppearances());
    }
    if (placeDetail.getMenuInfo() != null) {
      place.setMenuInfo(placeDetail.getMenuInfo());
    }
    if (placeDetail.getImageUrl() != null) {
      place.setImageUrl(placeDetail.getImageUrl());
    }
    if (placeDetail.getImageUrls() != null) {
      place.setPhotoUrls(placeDetail.getImageUrls());
    }
  }

  /**
   * PlacePlatformReference 생성 (없으면)
   */
  private void createPlacePlatformReferenceIfNotExists(Place place, String naverPlaceId) {
    if (naverPlaceId == null) {
      return;
    }

    // 이미 존재하는지 확인
    Optional<PlacePlatformReference> existing = placePlatformReferenceRepository
        .findByPlaceAndPlacePlatform(place, PlacePlatform.NAVER);

    if (existing.isEmpty()) {
      PlacePlatformReference reference = PlacePlatformReference.builder()
          .place(place)
          .placePlatform(PlacePlatform.NAVER)
          .placePlatformId(naverPlaceId)
          .build();
      placePlatformReferenceRepository.save(reference);
      log.debug("Created PlacePlatformReference: placeId={}, naverPlaceId={}", place.getId(), naverPlaceId);
    }
  }

  /**
   * ContentPlace 연결 생성 (순서 포함)
   */
  private void createContentPlace(Content content, Place place, int position) {
    // 중복 체크
    boolean exists = contentPlaceRepository.existsByContentAndPlace(content, place);
    if (exists) {
      log.debug("ContentPlace already exists: contentId={}, placeId={}", content.getId(), place.getId());
      return;
    }

    ContentPlace contentPlace = ContentPlace.builder()
        .content(content)
        .place(place)
        .position(position)
        .build();

    contentPlaceRepository.save(contentPlace);
    log.debug("Created ContentPlace: contentId={}, placeId={}, position={}", content.getId(), place.getId(), position);
  }

  /**
   * Content 분석 완료 알림 전송
   */
  private void sendContentCompleteNotification(Content content, int placeCount) {
    log.info("Sending content complete notifications for contentId={}, placeCount={}", content.getId(), placeCount);

    List<ContentMember> unnotifiedMembers = contentMemberRepository.findUnnotifiedMembersWithMember(content.getId());

    if (unnotifiedMembers.isEmpty()) {
      log.info("No unnotified members found for contentId={}", content.getId());
      return;
    }

    log.info("Found {} unnotified members for contentId={}", unnotifiedMembers.size(), content.getId());

    // 알림 데이터 구성
    Map<String, String> notificationData = new HashMap<>();
    notificationData.put("type", "CONTENT_COMPLETE");
    notificationData.put("contentId", content.getId().toString());
    notificationData.put("placeCount", String.valueOf(placeCount));

    if (content.getTitle() != null) {
      notificationData.put("title", content.getTitle());
    }
    if (content.getThumbnailUrl() != null) {
      notificationData.put("thumbnailUrl", content.getThumbnailUrl());
    }

    // 알림 메시지 구성
    String notificationTitle = "콘텐츠 분석 완료";
    String notificationBody;
    if (placeCount > 0) {
      notificationBody = String.format("%d개의 장소가 발견되었습니다.", placeCount);
      if (content.getTitle() != null) {
        notificationBody = content.getTitle() + " - " + notificationBody;
      }
    } else {
      notificationBody = content.getTitle() != null
          ? content.getTitle() + " 분석이 완료되었습니다."
          : "콘텐츠 분석이 완료되었습니다.";
    }

    // 각 회원에게 알림 전송
    int successCount = 0;
    List<ContentMember> succeededMembers = new ArrayList<>();
    for (ContentMember contentMember : unnotifiedMembers) {
      try {
        fcmService.sendNotificationToMember(
            contentMember.getMember().getId(),
            notificationTitle,
            notificationBody,
            notificationData,
            content.getThumbnailUrl()
        );

        contentMember.setNotified(true);
        succeededMembers.add(contentMember);
        successCount++;

        log.info("Notification sent successfully to memberId={} for contentId={}",
            contentMember.getMember().getId(), content.getId());
      } catch (Exception e) {
        log.error("Failed to send notification to memberId={} for contentId={}: {}",
            contentMember.getMember().getId(), content.getId(), e.getMessage());
      }
    }

    if (!succeededMembers.isEmpty()) {
      contentMemberRepository.saveAll(succeededMembers);
    }

    log.info("Content complete notifications sent: {}/{} succeeded for contentId={}",
        successCount, unnotifiedMembers.size(), content.getId());
  }

  /**
   * Content를 요청한 모든 회원에게 MemberPlace 생성
   */
  private void createMemberPlaces(Content content, Place place) {
    List<ContentMember> contentMembers = contentMemberRepository.findAllByContentWithMember(content);

    log.info("Creating MemberPlace for {} members (contentId={}, placeId={})",
        contentMembers.size(), content.getId(), place.getId());

    int createdCount = 0;
    int skippedCount = 0;

    for (ContentMember contentMember : contentMembers) {
      try {
        Optional<MemberPlace> existing = memberPlaceRepository
            .findByMemberAndPlaceAndDeletedAtIsNull(contentMember.getMember(), place);

        if (existing.isPresent()) {
          log.debug("MemberPlace already exists: memberId={}, placeId={}",
              contentMember.getMember().getId(), place.getId());
          skippedCount++;
          continue;
        }

        MemberPlace memberPlace = MemberPlace.builder()
            .member(contentMember.getMember())
            .place(place)
            .savedStatus(PlaceSavedStatus.TEMPORARY)
            .sourceContentId(content.getId())
            .build();

        memberPlaceRepository.save(memberPlace);
        createdCount++;

        log.debug("MemberPlace created: id={}, memberId={}, placeId={}, status=TEMPORARY",
            memberPlace.getId(), contentMember.getMember().getId(), place.getId());

      } catch (Exception e) {
        log.error("Failed to create MemberPlace for memberId={}, placeId={}: {}",
            contentMember.getMember().getId(), place.getId(), e.getMessage());
      }
    }

    log.info("MemberPlace creation completed: {} created, {} skipped (contentId={}, placeId={})",
        createdCount, skippedCount, content.getId(), place.getId());
  }
}
