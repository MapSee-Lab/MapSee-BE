package kr.suhsaechan.mapsee.place.service;

import kr.suhsaechan.mapsee.common.exception.CustomException;
import kr.suhsaechan.mapsee.common.exception.constant.ErrorCode;
import kr.suhsaechan.mapsee.member.entity.Member;
import kr.suhsaechan.mapsee.member.repository.MemberRepository;
import kr.suhsaechan.mapsee.place.constant.PlaceSavedStatus;
import kr.suhsaechan.mapsee.place.dto.GetSavedPlacesResponse;
import kr.suhsaechan.mapsee.place.dto.GetTemporaryPlacesResponse;
import kr.suhsaechan.mapsee.place.dto.PlaceDto;
import kr.suhsaechan.mapsee.place.dto.SavePlaceResponse;
import kr.suhsaechan.mapsee.place.entity.MemberPlace;
import kr.suhsaechan.mapsee.place.entity.Place;
import kr.suhsaechan.mapsee.place.repository.MemberPlaceRepository;
import kr.suhsaechan.mapsee.place.repository.PlaceRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MemberPlaceService {

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
