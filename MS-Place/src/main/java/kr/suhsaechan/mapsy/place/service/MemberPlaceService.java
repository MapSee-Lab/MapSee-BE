package kr.suhsaechan.mapsy.place.service;

import kr.suhsaechan.mapsy.common.exception.CustomException;
import kr.suhsaechan.mapsy.common.exception.constant.ErrorCode;
import kr.suhsaechan.mapsy.member.entity.Member;
import kr.suhsaechan.mapsy.member.repository.MemberRepository;
import kr.suhsaechan.mapsy.place.constant.PlaceSavedStatus;
import kr.suhsaechan.mapsy.place.dto.BookmarkDto;
import kr.suhsaechan.mapsy.place.dto.GetSavedPlacesResponse;
import kr.suhsaechan.mapsy.place.dto.GetTemporaryPlacesResponse;
import kr.suhsaechan.mapsy.place.dto.PlaceDto;
import kr.suhsaechan.mapsy.place.dto.SavePlaceResponse;
import kr.suhsaechan.mapsy.place.dto.UpdateBookmarkRequest;
import kr.suhsaechan.mapsy.place.entity.MemberPlace;
import kr.suhsaechan.mapsy.place.entity.Place;
import kr.suhsaechan.mapsy.place.repository.MemberPlaceRepository;
import kr.suhsaechan.mapsy.place.repository.PlaceRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MemberPlaceService {

  private static final int DEFAULT_TOP_PLACES_LIMIT = 10;

  private final MemberPlaceRepository memberPlaceRepository;
  private final PlaceRepository placeRepository;
  private final MemberRepository memberRepository;

  /**
   * 회원의 임시 저장 장소 목록 조회
   * - AI 분석 결과로 자동 생성된 장소들
   * - 아직 사용자가 저장 여부를 결정하지 않은 상태
   *
   * @param member 조회할 회원
   * @return 임시 저장 장소 목록 응답
   */
  public GetTemporaryPlacesResponse getTemporaryPlaces(Member member) {
    log.info("Getting temporary places for member: {}", member.getId());

    List<MemberPlace> memberPlaces = memberPlaceRepository
        .findByMemberAndSavedStatusWithPlace(member, PlaceSavedStatus.TEMPORARY);

    List<PlaceDto> places = memberPlaces.stream()
        .map(MemberPlace::getPlace)
        .map(PlaceDto::from)
        .collect(Collectors.toList());

    log.info("Found {} temporary places for member: {}", places.size(), member.getId());

    return GetTemporaryPlacesResponse.builder()
        .places(places)
        .build();
  }

  /**
   * 회원의 저장한 장소 목록 조회
   * - 사용자가 명시적으로 저장한 장소들
   *
   * @param member 조회할 회원
   * @return 저장한 장소 목록 응답
   */
  public GetSavedPlacesResponse getSavedPlaces(Member member) {
    log.info("Getting saved places for member: {}", member.getId());

    List<MemberPlace> memberPlaces = memberPlaceRepository
        .findByMemberAndSavedStatusWithPlace(member, PlaceSavedStatus.SAVED);

    List<PlaceDto> places = memberPlaces.stream()
        .map(MemberPlace::getPlace)
        .map(PlaceDto::from)
        .collect(Collectors.toList());

    log.info("Found {} saved places for member: {}", places.size(), member.getId());

    return GetSavedPlacesResponse.builder()
        .places(places)
        .build();
  }

  /**
   * 임시 저장 장소를 저장 상태로 변경
   * - TEMPORARY → SAVED 상태 전환
   * - savedAt 시간 기록
   *
   * @param member 회원
   * @param placeId 저장할 장소 ID
   * @return 저장 결과 응답
   */
  @Transactional
  public SavePlaceResponse savePlace(Member member, UUID placeId) {
    log.info("Saving place for member: {}, placeId: {}", member.getId(), placeId);

    // 1. Place 조회
    Place place = placeRepository.findById(placeId)
        .orElseThrow(() -> {
          log.error("Place not found: placeId={}", placeId);
          return new CustomException(ErrorCode.PLACE_NOT_FOUND);
        });

    // 2. MemberPlace 조회
    MemberPlace memberPlace = memberPlaceRepository
        .findByMemberAndPlaceAndDeletedAtIsNull(member, place)
        .orElseThrow(() -> {
          log.error("MemberPlace not found: memberId={}, placeId={}", member.getId(), placeId);
          return new CustomException(ErrorCode.MEMBER_PLACE_NOT_FOUND);
        });

    // 3. 상태 변경 (TEMPORARY → SAVED)
    memberPlace.markAsSaved();
    MemberPlace savedMemberPlace = memberPlaceRepository.save(memberPlace);

    log.info("Place saved successfully: memberPlaceId={}", savedMemberPlace.getId());

    return SavePlaceResponse.builder()
        .memberPlaceId(savedMemberPlace.getId())
        .placeId(savedMemberPlace.getPlace().getId())
        .savedStatus(savedMemberPlace.getSavedStatus().name())
        .savedAt(savedMemberPlace.getSavedAt())
        .build();
  }

  /**
   * 임시 저장 장소 삭제 (Soft Delete)
   * - TEMPORARY 상태의 장소만 삭제 가능
   * - SAVED 상태는 삭제 불가
   *
   * @param member 회원
   * @param placeId 삭제할 장소 ID
   */
  @Transactional
  public void deleteTemporaryPlace(Member member, UUID placeId) {
    log.info("Deleting temporary place for member: {}, placeId: {}", member.getId(), placeId);

    // 1. Place 조회
    Place place = placeRepository.findById(placeId)
        .orElseThrow(() -> {
          log.error("Place not found: placeId={}", placeId);
          return new CustomException(ErrorCode.PLACE_NOT_FOUND);
        });

    // 2. MemberPlace 조회
    MemberPlace memberPlace = memberPlaceRepository
        .findByMemberAndPlaceAndDeletedAtIsNull(member, place)
        .orElseThrow(() -> {
          log.error("MemberPlace not found: memberId={}, placeId={}", member.getId(), placeId);
          return new CustomException(ErrorCode.MEMBER_PLACE_NOT_FOUND);
        });

    // 3. TEMPORARY 상태만 삭제 가능
    if (memberPlace.getSavedStatus() != PlaceSavedStatus.TEMPORARY) {
      log.error("Cannot delete saved place: memberPlaceId={}, status={}",
          memberPlace.getId(), memberPlace.getSavedStatus());
      throw new CustomException(ErrorCode.CANNOT_DELETE_SAVED_PLACE);
    }

    // 4. Soft Delete 수행
    memberPlace.softDelete(member.getId().toString());
    memberPlaceRepository.save(memberPlace);

    log.info("Temporary place deleted successfully: memberPlaceId={}", memberPlace.getId());
  }

  // ========== Controller용 오버로드 메서드 (UUID memberId 파라미터) ==========

  /**
   * 회원의 임시 저장 장소 목록 조회 (UUID memberId 버전)
   */
  public GetTemporaryPlacesResponse getTemporaryPlaces(UUID memberId) {
    Member member = getMemberById(memberId);
    return getTemporaryPlaces(member);
  }

  /**
   * 회원의 저장한 장소 목록 조회 (UUID memberId 버전)
   */
  public GetSavedPlacesResponse getSavedPlaces(UUID memberId) {
    Member member = getMemberById(memberId);
    return getSavedPlaces(member);
  }

  /**
   * 임시 저장 장소를 저장 상태로 변경 (UUID memberId 버전)
   */
  @Transactional
  public SavePlaceResponse savePlace(UUID memberId, UUID placeId) {
    Member member = getMemberById(memberId);
    return savePlace(member, placeId);
  }

  /**
   * 임시 저장 장소 삭제 (UUID memberId 버전)
   */
  @Transactional
  public void deleteTemporaryPlace(UUID memberId, UUID placeId) {
    Member member = getMemberById(memberId);
    deleteTemporaryPlace(member, placeId);
  }

  // ========== 북마크 관련 메서드 ==========

  /**
   * 북마크 목록 조회 (페이지네이션)
   *
   * @param memberId 회원 ID
   * @param pageable 페이지 정보
   * @return 북마크 Page
   */
  public Page<BookmarkDto> getBookmarks(UUID memberId, Pageable pageable) {
    log.info("Getting bookmarks for member: {}, page: {}, size: {}",
        memberId, pageable.getPageNumber(), pageable.getPageSize());

    Page<MemberPlace> memberPlaces = memberPlaceRepository.findBookmarksByMemberIdAndSavedStatus(
        memberId, PlaceSavedStatus.SAVED, pageable
    );

    log.info("Found {} bookmarks for member: {}", memberPlaces.getTotalElements(), memberId);

    return memberPlaces.map(BookmarkDto::from);
  }

  /**
   * 북마크 수정
   * - 폴더, 메모, 별점, 방문 여부 수정 가능
   * - null 값은 변경하지 않음
   *
   * @param memberId 회원 ID
   * @param memberPlaceId MemberPlace ID
   * @param request 수정 요청
   * @return 수정된 북마크 정보
   */
  @Transactional
  public BookmarkDto updateBookmark(UUID memberId, UUID memberPlaceId, UpdateBookmarkRequest request) {
    log.info("Updating bookmark: memberId={}, memberPlaceId={}", memberId, memberPlaceId);

    // 1. MemberPlace 조회 (회원 검증 포함)
    MemberPlace memberPlace = memberPlaceRepository.findByIdAndMemberIdAndDeletedAtIsNull(
        memberPlaceId, memberId
    ).orElseThrow(() -> {
      log.error("MemberPlace not found: memberPlaceId={}, memberId={}", memberPlaceId, memberId);
      return new CustomException(ErrorCode.MEMBER_PLACE_NOT_FOUND);
    });

    // 2. SAVED 상태만 수정 가능
    if (memberPlace.getSavedStatus() != PlaceSavedStatus.SAVED) {
      log.error("Cannot update unsaved place: memberPlaceId={}, status={}",
          memberPlaceId, memberPlace.getSavedStatus());
      throw new CustomException(ErrorCode.CANNOT_UPDATE_UNSAVED_PLACE);
    }

    // 3. 필드별 수정 (null이 아닌 경우만)
    if (request.getFolder() != null) {
      memberPlace.updateFolder(request.getFolder());
    }
    if (request.getMemo() != null) {
      memberPlace.updateMemo(request.getMemo());
    }
    if (request.getRating() != null) {
      memberPlace.updateRating(request.getRating());
    }
    if (request.getVisited() != null) {
      if (request.getVisited()) {
        memberPlace.markAsVisited(request.getVisitedAt());
      } else {
        memberPlace.unmarkVisited();
      }
    }

    MemberPlace savedMemberPlace = memberPlaceRepository.save(memberPlace);
    log.info("Bookmark updated successfully: memberPlaceId={}", savedMemberPlace.getId());

    return BookmarkDto.from(savedMemberPlace);
  }

  /**
   * 내 TOP 저장 장소 조회
   *
   * @param memberId 회원 ID
   * @param limit 조회 개수 (기본 10개)
   * @return 장소 목록
   */
  public List<PlaceDto> getMyTopPlaces(UUID memberId, int limit) {
    log.info("Getting top {} saved places for member: {}", limit, memberId);

    Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "savedAt"));
    List<MemberPlace> memberPlaces = memberPlaceRepository.findTopPlacesByMemberIdAndSavedStatus(
        memberId, PlaceSavedStatus.SAVED, pageable
    );

    log.info("Found {} top places for member: {}", memberPlaces.size(), memberId);

    return memberPlaces.stream()
        .map(MemberPlace::getPlace)
        .map(PlaceDto::from)
        .collect(Collectors.toList());
  }

  /**
   * 내 TOP 저장 장소 조회 (기본 개수)
   *
   * @param memberId 회원 ID
   * @return 장소 목록 (최대 10개)
   */
  public List<PlaceDto> getMyTopPlaces(UUID memberId) {
    return getMyTopPlaces(memberId, DEFAULT_TOP_PLACES_LIMIT);
  }

  // ========== Private Helper Methods ==========

  /**
   * Member ID로 Member 엔티티 조회
   */
  private Member getMemberById(UUID memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> {
          log.error("Member not found: memberId={}", memberId);
          return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        });
  }
}
