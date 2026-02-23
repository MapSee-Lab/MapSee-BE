package kr.suhsaechan.mapsy.web.controller;

import kr.suhsaechan.mapsy.auth.dto.CustomUserDetails;
import kr.suhsaechan.mapsy.place.dto.AddFolderPlaceRequest;
import kr.suhsaechan.mapsy.place.dto.AddFolderPlaceResponse;
import kr.suhsaechan.mapsy.place.dto.CreateFolderRequest;
import kr.suhsaechan.mapsy.place.dto.CreateFolderResponse;
import kr.suhsaechan.mapsy.place.dto.GetFolderPlacesResponse;
import kr.suhsaechan.mapsy.place.dto.GetFoldersResponse;
import kr.suhsaechan.mapsy.place.dto.UpdateFolderRequest;
import kr.suhsaechan.mapsy.place.dto.UpdateFolderResponse;
import kr.suhsaechan.mapsy.place.service.FolderService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/folders")
@Tag(name = "폴더 관리 API", description = "폴더 CRUD 및 폴더-장소 관리 관련 API 제공")
public class FolderController implements FolderControllerDocs {

  private final FolderService folderService;

  @GetMapping
  @Override
  public ResponseEntity<GetFoldersResponse> getFolders(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    log.info("Get folders request from member: {}", userDetails.getMemberId());
    GetFoldersResponse response = folderService.getFolders(userDetails.getMemberId());
    return ResponseEntity.ok(response);
  }

  @PostMapping
  @Override
  public ResponseEntity<CreateFolderResponse> createFolder(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody CreateFolderRequest request
  ) {
    log.info("Create folder request from member: {}", userDetails.getMemberId());
    CreateFolderResponse response = folderService.createFolder(userDetails.getMemberId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{folderId}")
  @Override
  public ResponseEntity<UpdateFolderResponse> updateFolder(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID folderId,
      @Valid @RequestBody UpdateFolderRequest request
  ) {
    log.info("Update folder request from member: {}, folderId: {}", userDetails.getMemberId(), folderId);
    UpdateFolderResponse response = folderService.updateFolder(userDetails.getMemberId(), folderId, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{folderId}")
  @Override
  public ResponseEntity<Void> deleteFolder(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID folderId
  ) {
    log.info("Delete folder request from member: {}, folderId: {}", userDetails.getMemberId(), folderId);
    folderService.deleteFolder(userDetails.getMemberId(), folderId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{folderId}/places")
  @Override
  public ResponseEntity<GetFolderPlacesResponse> getFolderPlaces(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID folderId
  ) {
    log.info("Get folder places request from member: {}, folderId: {}", userDetails.getMemberId(), folderId);
    GetFolderPlacesResponse response = folderService.getFolderPlaces(userDetails.getMemberId(), folderId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{folderId}/places")
  @Override
  public ResponseEntity<AddFolderPlaceResponse> addPlaceToFolder(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID folderId,
      @RequestBody AddFolderPlaceRequest request
  ) {
    log.info("Add place to folder request from member: {}, folderId: {}, placeId: {}",
        userDetails.getMemberId(), folderId, request.getPlaceId());
    AddFolderPlaceResponse response = folderService.addPlaceToFolder(userDetails.getMemberId(), folderId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/{folderId}/places/{placeId}")
  @Override
  public ResponseEntity<Void> removePlaceFromFolder(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID folderId,
      @PathVariable UUID placeId
  ) {
    log.info("Remove place from folder request from member: {}, folderId: {}, placeId: {}",
        userDetails.getMemberId(), folderId, placeId);
    folderService.removePlaceFromFolder(userDetails.getMemberId(), folderId, placeId);
    return ResponseEntity.noContent().build();
  }
}
