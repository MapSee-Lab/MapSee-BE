package kr.suhsaechan.mapsy.sns.service;

import kr.suhsaechan.mapsy.ai.dto.AiCallbackRequest;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import kr.suhsaechan.mapsy.member.service.FcmService;

// AI 서버 Webhook Callback 처리
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
   * - SUCCESS면 Place 저장
   * - FAILED면 상태만 변경
   *
   * @param request AI Callback 요청
   * @return AI Callback 응답
   */
  @Transactional
  public AiCallbackResponse processAiServerCallback(AiCallbackRequest request) {
    // contentId 추출: 최상위 contentId 우선, 없으면 ContentInfo에서 추출
    UUID contentId = request.getContentId() != null
        ? request.getContentId()
        : (request.getContentInfo() != null && request.getContentInfo().getContentId() != null
            ? request.getContentInfo().getContentId()
            : null);

    if (contentId == null) {
      log.error("ContentInfo or contentId is null in callback request. resultStatus={}", request.getResultStatus());
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    log.info("Processing AI callback: contentId={}, resultStatus={}",
        contentId, request.getResultStatus());

    // Content 조회 - 없으면 예외 발생
    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND));

    // 결과 상태에 따라 분기 처리
    if ("SUCCESS".equals(request.getResultStatus())) {
      // 성공 - Place 데이터 저장
      processAiServerSuccessCallback(content, request);
    } else if ("FAILED".equals(request.getResultStatus())) {
      // 실패 - Content 상태만 FAILED로 변경
      processFailedCallback(content, request);
    } else {
      // 알 수 없는 상태값 - 에러 처리
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
   *
   * - Content가 COMPLETED 상태: 기존 ContentPlace 삭제 후 재생성 (업데이트 모드)
   * - Content가 PENDING/FAILED 상태: 신규 ContentPlace 생성
   * - TODO: AI 서버에서 받은 Place 정보로 직접 Place 생성 (나중에 구현 예정)
   *
   * @param content 대상 Content
   * @param request AI Callback 요청
   */
  private void processAiServerSuccessCallback(Content content, AiCallbackRequest request) {
    log.debug("Processing SUCCESS callback for contentId={}", content.getId());

    // Content가 이미 COMPLETED 상태인지 확인 (재처리 요청 판단용)
    boolean isContentAlreadyCompleted = (content.getStatus() == ContentStatus.COMPLETED);

    if (isContentAlreadyCompleted) {
      // 업데이트 모드 - 기존 ContentPlace 모두 삭제
      log.info("Content already COMPLETED. Updating existing data: contentId={}", content.getId());
      contentPlaceRepository.deleteByContentIdWithFlush(content.getId());
      log.debug("Deleted existing ContentPlaces for contentId={}", content.getId());
    }

    // Content 상태를 COMPLETED로 변경 (신규 또는 재처리 모두)
    content.setStatus(ContentStatus.COMPLETED);

    // ContentInfo로 Content 메타데이터 업데이트
    updateContentWithContentInfo(content, request);

    contentRepository.save(content);

    // AI 서버에서 받은 Place 정보로 Place 생성 및 Content 연결
    int placeCount = 0;
    if (request.getPlaces() != null && !request.getPlaces().isEmpty()) {
      List<AiCallbackRequest.PlaceInfo> placeInfos = request.getPlaces();
      log.info("Received {} places for contentId={} (update mode: {}). Starting Place creation.",
          placeInfos.size(), content.getId(), isContentAlreadyCompleted);

      // Place 생성 및 ContentPlace 연결
      List<Place> savedPlaces = new ArrayList<>();
      for (AiCallbackRequest.PlaceInfo placeInfo : placeInfos) {
        try {
          // 좌표 검증
          if (placeInfo.getLatitude() == null || placeInfo.getLongitude() == null) {
            log.error("Missing coordinates for place: {}. Skipping.", placeInfo.getName());
            continue;
          }

          // Place 생성 또는 조회
          Place place = createOrGetPlaceFromAiData(placeInfo);
          savedPlaces.add(place);

          // ContentPlace 연결 (중복 체크)
          createContentPlace(content, place);

          // 키워드 연결
          if (placeInfo.getKeywords() != null && !placeInfo.getKeywords().isEmpty()) {
            keywordService.linkKeywordsToPlace(place, placeInfo.getKeywords());
            log.debug("Linked {} keywords to place: {}", placeInfo.getKeywords().size(), place.getName());
          }

          log.debug("Successfully processed place: {} (id={})", place.getName(), place.getId());
        } catch (Exception e) {
          log.error("Failed to process place: {}. Error: {}", placeInfo.getName(), e.getMessage(), e);
          // 개별 장소 처리 실패 시 계속 진행 (다른 장소는 저장)
        }
      }

      placeCount = savedPlaces.size();
      log.info("Successfully saved {} out of {} places for contentId={}",
          placeCount, placeInfos.size(), content.getId());
    } else {
      // Place 데이터가 없는 경우 경고 로그
      log.warn("No places found in callback for contentId={}", content.getId());
    }

    // AI 분석 완료 후 모든 요청 회원에게 알림 전송
    sendContentCompleteNotification(content, request, placeCount);
  }

  /**
   * ContentInfo로 Content 메타데이터 업데이트
   *
   * null이 아닌 필드만 업데이트하여 기존 데이터 보존
   *
   * @param content 대상 Content
   * @param request AI Callback 요청
   */
  private void updateContentWithContentInfo(Content content, AiCallbackRequest request) {
    AiCallbackRequest.ContentInfo contentInfo = request.getContentInfo();

    if (contentInfo == null) {
      log.warn("ContentInfo is null for contentId={}. Skipping metadata update.", content.getId());
      return;
    }

    // title 업데이트 (null이 아닐 때만)
    if (contentInfo.getTitle() != null) {
      content.setTitle(contentInfo.getTitle());
    }

    // thumbnailUrl 업데이트 (null이 아닐 때만)
    if (contentInfo.getThumbnailUrl() != null) {
      content.setThumbnailUrl(contentInfo.getThumbnailUrl());
    }

    // contentUrl 업데이트 (null이 아닐 때만) - originalUrl에 매핑
    if (contentInfo.getContentUrl() != null) {
      String newUrl = contentInfo.getContentUrl();
      // 현재 URL과 같으면 업데이트 스킵
      if (!newUrl.equals(content.getOriginalUrl())) {
        // 다른 Content에 이미 존재하는지 확인 (unique constraint 위반 방지)
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

    // platformUploader 업데이트 (null이 아닐 때만)
    if (contentInfo.getPlatformUploader() != null) {
      content.setPlatformUploader(contentInfo.getPlatformUploader());
    }

    // summary 업데이트 (null이 아닐 때만)
    if (contentInfo.getSummary() != null) {
      content.setSummary(contentInfo.getSummary());
    }

    // platform 업데이트 (null이 아닐 때만)
    if (contentInfo.getPlatform() != null) {
      try {
        content.setPlatform(kr.suhsaechan.mapsy.sns.constant.ContentPlatform.valueOf(contentInfo.getPlatform()));
      } catch (IllegalArgumentException e) {
        log.error("Invalid platform value: {}. Keeping existing platform for contentId={}",
            contentInfo.getPlatform(), content.getId());
      }
    }

    log.debug("Updated Content with ContentInfo: contentId={}, title={}, summary={}, platformUploader={}",
        content.getId(),
        contentInfo.getTitle() != null ? contentInfo.getTitle() : "(unchanged)",
        contentInfo.getSummary() != null ? contentInfo.getSummary().substring(0, Math.min(30, contentInfo.getSummary().length())) + "..." : "(unchanged)",
        contentInfo.getPlatformUploader() != null ? contentInfo.getPlatformUploader() : "(unchanged)");
  }

  /**
   * 실패 Callback 처리
   *
   * Content 상태를 FAILED로 변경
   *
   * @param content 대상 Content
   * @param request AI Callback 요청
   */
  private void processFailedCallback(Content content, AiCallbackRequest request) {
    log.error("Processing FAILED callback for contentId={}", content.getId());

    // Content 상태를 FAILED로 변경하고 저장
    content.setStatus(ContentStatus.FAILED);
    contentRepository.save(content);
  }

  /**
   * AI 서버 응답 데이터로부터 Place 생성 또는 조회
   * <p>
   * 위도/경도 기반 중복 체크 후 기존 Place 업데이트 또는 신규 생성
   *
   * @param placeInfo AI 서버 응답 장소 정보
   * @return 생성 또는 업데이트된 Place
   */
  private Place createOrGetPlaceFromAiData(AiCallbackRequest.PlaceInfo placeInfo) {
    BigDecimal latitude = BigDecimal.valueOf(placeInfo.getLatitude());
    BigDecimal longitude = BigDecimal.valueOf(placeInfo.getLongitude());

    // 이름+좌표로 중복 체크
    Optional<Place> existing = placeRepository.findByNameAndLatitudeAndLongitude(
        placeInfo.getName(),
        latitude,
        longitude
    );

    if (existing.isPresent()) {
      // 기존 Place 업데이트
      Place place = existing.get();
      updatePlaceFromAiData(place, placeInfo);
      log.debug("Updated existing place: id={}, name={}, address={}",
          place.getId(), place.getName(), place.getAddress());
      return placeRepository.save(place);
    } else {
      // 새로 생성
      Place newPlace = Place.builder()
          .name(placeInfo.getName())
          .address(placeInfo.getAddress())
          .latitude(latitude)
          .longitude(longitude)
          .country(placeInfo.getCountry())
          .build();

      // 선택 필드 설정
      if (placeInfo.getCategory() != null) {
        newPlace.setTypes(placeInfo.getCategory());
      }
      if (placeInfo.getPhone() != null) {
        newPlace.setPhone(placeInfo.getPhone());
      }
      if (placeInfo.getOpeningHours() != null) {
        newPlace.setOpeningHours(placeInfo.getOpeningHours());
      }
      if (placeInfo.getDescription() != null) {
        newPlace.setDescription(placeInfo.getDescription());
      }

      Place savedPlace = placeRepository.save(newPlace);
      log.debug("Created new place: id={}, name={}, lat={}, lng={}",
          savedPlace.getId(), savedPlace.getName(), latitude, longitude);
      return savedPlace;
    }
  }

  /**
   * AI 응답 데이터로 기존 Place 업데이트
   * <p>
   * null이 아닌 필드만 업데이트하여 기존 데이터 보존
   *
   * @param place     업데이트할 Place
   * @param placeInfo AI 응답 장소 정보
   */
  private void updatePlaceFromAiData(Place place, AiCallbackRequest.PlaceInfo placeInfo) {
    if (placeInfo.getAddress() != null) {
      place.setAddress(placeInfo.getAddress());
    }
    if (placeInfo.getCountry() != null) {
      place.setCountry(placeInfo.getCountry());
    }
    if (placeInfo.getCategory() != null) {
      place.setTypes(placeInfo.getCategory());
    }
    if (placeInfo.getPhone() != null) {
      place.setPhone(placeInfo.getPhone());
    }
    if (placeInfo.getOpeningHours() != null) {
      place.setOpeningHours(placeInfo.getOpeningHours());
    }
    if (placeInfo.getDescription() != null) {
      place.setDescription(placeInfo.getDescription());
    }
  }

  /**
   * ContentPlace 연결 생성 (중복 체크 포함)
   * <p>
   * Content와 Place 매핑. 이미 존재하면 스킵
   *
   * @param content 대상 Content
   * @param place   대상 Place
   */
  private void createContentPlace(Content content, Place place) {
    // 중복 체크
    boolean exists = contentPlaceRepository.existsByContentAndPlace(content, place);
    if (exists) {
      log.debug("ContentPlace already exists: contentId={}, placeId={}", content.getId(), place.getId());
      return;
    }

    // ContentPlace 엔티티 생성
    ContentPlace contentPlace = ContentPlace.builder()
        .content(content)
        .place(place)
        .position(0)  // AI 서버 응답에서는 순서 정보 없음
        .build();

    // ContentPlace 저장
    contentPlaceRepository.save(contentPlace);
    log.debug("Created ContentPlace: contentId={}, placeId={}", content.getId(), place.getId());
  }

  /**
   * ContentPlace 연결 생성 (순서 포함)
   * <p>
   * Content와 Place 매핑 및 순서 저장
   *
   * @param content  대상 Content
   * @param place    대상 Place
   * @param position 순서
   */
  private void createContentPlace(Content content, Place place, int position) {
    // ContentPlace 엔티티 생성
    ContentPlace contentPlace = ContentPlace.builder()
        .content(content)
        .place(place)
        .position(position)  // Place 순서 정보
        .build();

    // ContentPlace 저장
    contentPlaceRepository.save(contentPlace);
    log.debug("Created ContentPlace: contentId={}, placeId={}, position={}", content.getId(), place.getId(), position);
  }

  /**
   * Content 분석 완료 알림 전송
   * <p>
   * 해당 Content를 요청한 모든 회원에게 FCM 알림 전송
   * notified=false인 ContentMember만 대상으로 함
   *
   * @param content    완료된 Content
   * @param request    AI Callback 요청
   * @param placeCount 추출된 장소 개수
   */
  private void sendContentCompleteNotification(Content content, AiCallbackRequest request, int placeCount) {
    log.info("Sending content complete notifications for contentId={}, placeCount={}", content.getId(), placeCount);

    // 알림 미전송된 ContentMember 조회 (Member Fetch Join으로 N+1 방지)
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
        // FCM 알림 전송
        fcmService.sendNotificationToMember(
            contentMember.getMember().getId(),
            notificationTitle,
            notificationBody,
            notificationData,
            content.getThumbnailUrl()  // 썸네일 이미지 URL (null 가능)
        );

        // 알림 전송 완료 표시
        contentMember.setNotified(true);
        succeededMembers.add(contentMember);
        successCount++;

        log.info("Notification sent successfully to memberId={} for contentId={}",
            contentMember.getMember().getId(), content.getId());
      } catch (Exception e) {
        log.error("Failed to send notification to memberId={} for contentId={}: {}",
            contentMember.getMember().getId(), content.getId(), e.getMessage());
        // 실패해도 계속 진행 (다른 회원들에게는 알림 전송)
      }
    }

    // 알림 전송 완료된 ContentMember만 업데이트
    if (!succeededMembers.isEmpty()) {
      contentMemberRepository.saveAll(succeededMembers);
    }

    log.info("Content complete notifications sent: {}/{} succeeded for contentId={}",
        successCount, unnotifiedMembers.size(), content.getId());
  }

  /**
   * Content를 요청한 모든 회원에게 MemberPlace 생성
   * - TEMPORARY 상태로 초기화
   * - sourceContentId로 Content 추적
   * - 중복 생성 방지 (이미 존재하면 스킵)
   *
   * @param content 원본 Content
   * @param place   생성된 Place
   */
  private void createMemberPlaces(Content content, Place place) {
    // 1. Content를 요청한 모든 회원 조회 (Member Fetch Join으로 N+1 방지)
    List<ContentMember> contentMembers = contentMemberRepository.findAllByContentWithMember(content);

    log.info("Creating MemberPlace for {} members (contentId={}, placeId={})",
        contentMembers.size(), content.getId(), place.getId());

    // 2. 각 회원에 대해 MemberPlace 생성
    int createdCount = 0;
    int skippedCount = 0;

    for (ContentMember contentMember : contentMembers) {
      try {
        // 3. 이미 존재하는지 확인 (중복 방지)
        Optional<MemberPlace> existing = memberPlaceRepository
            .findByMemberAndPlaceAndDeletedAtIsNull(contentMember.getMember(), place);

        if (existing.isPresent()) {
          log.debug("MemberPlace already exists: memberId={}, placeId={}",
              contentMember.getMember().getId(), place.getId());
          skippedCount++;
          continue;
        }

        // 4. MemberPlace 생성 및 저장
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
        // 실패해도 계속 진행 (다른 회원들의 MemberPlace는 생성)
      }
    }

    log.info("MemberPlace creation completed: {} created, {} skipped (contentId={}, placeId={})",
        createdCount, skippedCount, content.getId(), place.getId());
  }
}
