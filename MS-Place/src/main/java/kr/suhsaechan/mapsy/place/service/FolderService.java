package kr.suhsaechan.mapsy.place.service;

import kr.suhsaechan.mapsy.common.exception.CustomException;
import kr.suhsaechan.mapsy.common.exception.constant.ErrorCode;
import kr.suhsaechan.mapsy.member.entity.Member;
import kr.suhsaechan.mapsy.member.repository.MemberRepository;
import kr.suhsaechan.mapsy.place.constant.FolderVisibility;
import kr.suhsaechan.mapsy.place.dto.AddFolderPlaceRequest;
import kr.suhsaechan.mapsy.place.dto.AddFolderPlaceResponse;
import kr.suhsaechan.mapsy.place.dto.CreateFolderRequest;
import kr.suhsaechan.mapsy.place.dto.CreateFolderResponse;
import kr.suhsaechan.mapsy.place.dto.FolderDto;
import kr.suhsaechan.mapsy.place.dto.GetFolderPlacesResponse;
import kr.suhsaechan.mapsy.place.dto.GetFoldersResponse;
import kr.suhsaechan.mapsy.place.dto.PlaceDto;
import kr.suhsaechan.mapsy.place.dto.UpdateFolderRequest;
import kr.suhsaechan.mapsy.place.dto.UpdateFolderResponse;
import kr.suhsaechan.mapsy.place.entity.Folder;
import kr.suhsaechan.mapsy.place.entity.FolderPlace;
import kr.suhsaechan.mapsy.place.entity.Place;
import kr.suhsaechan.mapsy.place.repository.FolderPlaceRepository;
import kr.suhsaechan.mapsy.place.repository.FolderRepository;
import kr.suhsaechan.mapsy.place.repository.PlaceRepository;
import java.util.List;
import java.util.Optional;
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
public class FolderService {

  private final FolderRepository folderRepository;
  private final FolderPlaceRepository folderPlaceRepository;
  private final PlaceRepository placeRepository;
  private final MemberRepository memberRepository;

  // ========== 폴더 CRUD ==========

  public GetFoldersResponse getFolders(Member member) {
    log.info("Getting folders for member: {}", member.getId());

    List<Object[]> results = folderRepository.findByOwnerWithPlaceCount(member);

    List<FolderDto> folderDtos = results.stream()
        .map(row -> {
          Folder folder = (Folder) row[0];
          int placeCount = ((Long) row[1]).intValue();
          return FolderDto.from(folder, placeCount);
        })
        .collect(Collectors.toList());

    return GetFoldersResponse.builder()
        .folders(folderDtos)
        .build();
  }

  @Transactional
  public CreateFolderResponse createFolder(Member member, CreateFolderRequest request) {
    log.info("Creating folder for member: {}, name: {}", member.getId(), request.getName());

    Folder folder = Folder.builder()
        .owner(member)
        .name(request.getName() != null ? request.getName() : "제목 없음")
        .visibility(request.getVisibility() != null ? request.getVisibility() : FolderVisibility.PRIVATE)
        .isDefault(false)
        .build();

    Folder savedFolder = folderRepository.save(folder);
    log.info("Folder created: folderId={}", savedFolder.getId());

    return CreateFolderResponse.from(savedFolder);
  }

  @Transactional
  public UpdateFolderResponse updateFolder(Member member, UUID folderId, UpdateFolderRequest request) {
    log.info("Updating folder: folderId={}, memberId={}", folderId, member.getId());

    Folder folder = getFolderWithOwnerValidation(folderId, member);

    if (request.getName() != null) {
      folder.updateName(request.getName());
    }
    if (request.getVisibility() != null) {
      folder.updateVisibility(request.getVisibility());
    }

    Folder savedFolder = folderRepository.save(folder);
    log.info("Folder updated: folderId={}", savedFolder.getId());

    return UpdateFolderResponse.from(savedFolder);
  }

  @Transactional
  public void deleteFolder(Member member, UUID folderId) {
    log.info("Deleting folder: folderId={}, memberId={}", folderId, member.getId());

    Folder folder = getFolderWithOwnerValidation(folderId, member);

    // 기본 폴더 삭제 불가
    if (folder.getIsDefault()) {
      throw new CustomException(ErrorCode.CANNOT_DELETE_DEFAULT_FOLDER);
    }

    // 하위 FolderPlace 모두 Soft Delete
    List<FolderPlace> folderPlaces = folderPlaceRepository.findByFolderAndDeletedAtIsNull(folder);
    for (FolderPlace fp : folderPlaces) {
      fp.softDelete(member.getId().toString());
    }
    folderPlaceRepository.saveAll(folderPlaces);

    // 폴더 Soft Delete
    folder.softDelete(member.getId().toString());
    folderRepository.save(folder);

    log.info("Folder deleted: folderId={}, deletedPlaces={}", folderId, folderPlaces.size());
  }

  // ========== 폴더-장소 관리 ==========

  public GetFolderPlacesResponse getFolderPlaces(Member member, UUID folderId) {
    log.info("Getting folder places: folderId={}, memberId={}", folderId, member.getId());

    Folder folder = getFolderWithOwnerValidation(folderId, member);

    List<FolderPlace> folderPlaces = folderPlaceRepository.findByFolderWithPlaceOrderByPosition(folder);

    List<PlaceDto> places = folderPlaces.stream()
        .map(FolderPlace::getPlace)
        .map(PlaceDto::from)
        .collect(Collectors.toList());

    return GetFolderPlacesResponse.builder()
        .folderId(folder.getId())
        .folderName(folder.getName())
        .places(places)
        .build();
  }

  @Transactional
  public AddFolderPlaceResponse addPlaceToFolder(Member member, UUID folderId, AddFolderPlaceRequest request) {
    log.info("Adding place to folder: folderId={}, placeId={}, memberId={}", folderId, request.getPlaceId(), member.getId());

    Folder folder = getFolderWithOwnerValidation(folderId, member);

    Place place = placeRepository.findById(request.getPlaceId())
        .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

    // 기존 레코드 조회 (Soft Delete 포함)
    Optional<FolderPlace> existingFolderPlace = folderPlaceRepository.findByFolderAndPlace(folder, place);

    if (existingFolderPlace.isPresent()) {
      FolderPlace fp = existingFolderPlace.get();
      if (fp.isActive()) {
        // 이미 활성 상태면 중복 에러
        throw new CustomException(ErrorCode.FOLDER_PLACE_ALREADY_EXISTS);
      }
      // Soft Delete된 레코드 복원
      fp.restore();
      FolderPlace restoredFolderPlace = folderPlaceRepository.save(fp);
      log.info("Place restored to folder: folderPlaceId={}", restoredFolderPlace.getId());
      return AddFolderPlaceResponse.from(restoredFolderPlace);
    }

    int maxPosition = folderPlaceRepository.findMaxPositionByFolder(folder);

    FolderPlace folderPlace = FolderPlace.builder()
        .folder(folder)
        .place(place)
        .position(maxPosition + 1)
        .build();

    FolderPlace savedFolderPlace = folderPlaceRepository.save(folderPlace);
    log.info("Place added to folder: folderPlaceId={}", savedFolderPlace.getId());

    return AddFolderPlaceResponse.from(savedFolderPlace);
  }

  @Transactional
  public void removePlaceFromFolder(Member member, UUID folderId, UUID placeId) {
    log.info("Removing place from folder: folderId={}, placeId={}, memberId={}", folderId, placeId, member.getId());

    Folder folder = getFolderWithOwnerValidation(folderId, member);

    Place place = placeRepository.findById(placeId)
        .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

    FolderPlace folderPlace = folderPlaceRepository.findByFolderAndPlaceAndDeletedAtIsNull(folder, place)
        .orElseThrow(() -> new CustomException(ErrorCode.FOLDER_PLACE_NOT_FOUND));

    folderPlace.softDelete(member.getId().toString());
    folderPlaceRepository.save(folderPlace);

    log.info("Place removed from folder: folderPlaceId={}", folderPlace.getId());
  }

  // ========== 기본 폴더 관련 ==========

  @Transactional
  public Folder createDefaultFolder(Member member) {
    log.info("Creating default folder for member: {}", member.getId());

    Folder defaultFolder = Folder.builder()
        .owner(member)
        .name("기본")
        .visibility(FolderVisibility.PRIVATE)
        .isDefault(true)
        .build();

    Folder saved = folderRepository.save(defaultFolder);
    log.info("Default folder created: folderId={}", saved.getId());
    return saved;
  }

  @Transactional
  public void addPlaceToDefaultFolder(Member member, Place place) {
    Folder defaultFolder = folderRepository.findByOwnerAndIsDefaultTrueAndDeletedAtIsNull(member)
        .orElseGet(() -> createDefaultFolder(member));

    // 기존 레코드 조회 (Soft Delete 포함)
    Optional<FolderPlace> existingFolderPlace = folderPlaceRepository.findByFolderAndPlace(defaultFolder, place);

    if (existingFolderPlace.isPresent()) {
      FolderPlace fp = existingFolderPlace.get();
      if (fp.isActive()) {
        log.debug("Place already in default folder: placeId={}", place.getId());
        return;
      }
      // Soft Delete된 레코드 복원
      fp.restore();
      folderPlaceRepository.save(fp);
      log.info("Place restored to default folder: placeId={}", place.getId());
      return;
    }

    int maxPosition = folderPlaceRepository.findMaxPositionByFolder(defaultFolder);

    FolderPlace folderPlace = FolderPlace.builder()
        .folder(defaultFolder)
        .place(place)
        .position(maxPosition + 1)
        .build();

    folderPlaceRepository.save(folderPlace);
    log.info("Place added to default folder: placeId={}", place.getId());
  }

  // ========== UUID 오버로드 메서드 ==========

  public GetFoldersResponse getFolders(UUID memberId) {
    return getFolders(getMemberById(memberId));
  }

  @Transactional
  public CreateFolderResponse createFolder(UUID memberId, CreateFolderRequest request) {
    return createFolder(getMemberById(memberId), request);
  }

  @Transactional
  public UpdateFolderResponse updateFolder(UUID memberId, UUID folderId, UpdateFolderRequest request) {
    return updateFolder(getMemberById(memberId), folderId, request);
  }

  @Transactional
  public void deleteFolder(UUID memberId, UUID folderId) {
    deleteFolder(getMemberById(memberId), folderId);
  }

  public GetFolderPlacesResponse getFolderPlaces(UUID memberId, UUID folderId) {
    return getFolderPlaces(getMemberById(memberId), folderId);
  }

  @Transactional
  public AddFolderPlaceResponse addPlaceToFolder(UUID memberId, UUID folderId, AddFolderPlaceRequest request) {
    return addPlaceToFolder(getMemberById(memberId), folderId, request);
  }

  @Transactional
  public void removePlaceFromFolder(UUID memberId, UUID folderId, UUID placeId) {
    removePlaceFromFolder(getMemberById(memberId), folderId, placeId);
  }

  // ========== Private Helper ==========

  private Member getMemberById(UUID memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> {
          log.error("Member not found: memberId={}", memberId);
          return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        });
  }

  private Folder getFolderWithOwnerValidation(UUID folderId, Member member) {
    Folder folder = folderRepository.findByIdAndDeletedAtIsNull(folderId)
        .orElseThrow(() -> {
          log.error("Folder not found: folderId={}", folderId);
          return new CustomException(ErrorCode.FOLDER_NOT_FOUND);
        });

    if (!folder.getOwner().getId().equals(member.getId())) {
      log.error("Folder access denied: folderId={}, memberId={}", folderId, member.getId());
      throw new CustomException(ErrorCode.FOLDER_ACCESS_DENIED);
    }

    return folder;
  }
}
