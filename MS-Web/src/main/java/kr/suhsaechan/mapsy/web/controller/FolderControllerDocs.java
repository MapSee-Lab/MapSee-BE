package kr.suhsaechan.mapsy.web.controller;

import kr.suhsaechan.mapsy.auth.dto.CustomUserDetails;
import kr.suhsaechan.mapsy.common.constant.Author;
import kr.suhsaechan.mapsy.place.dto.AddFolderPlaceRequest;
import kr.suhsaechan.mapsy.place.dto.AddFolderPlaceResponse;
import kr.suhsaechan.mapsy.place.dto.CreateFolderRequest;
import kr.suhsaechan.mapsy.place.dto.CreateFolderResponse;
import kr.suhsaechan.mapsy.place.dto.GetFolderPlacesResponse;
import kr.suhsaechan.mapsy.place.dto.GetFoldersResponse;
import kr.suhsaechan.mapsy.place.dto.UpdateFolderRequest;
import kr.suhsaechan.mapsy.place.dto.UpdateFolderResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import kr.suhsaechan.suhapilog.annotation.ApiLog;
import org.springframework.http.ResponseEntity;

public interface FolderControllerDocs {

  @ApiLog(date = "2026.02.23", author = Author.SUHSAECHAN, issueNumber = 26, description = "폴더 목록 조회 API 구현")
  @Operation(summary = "내 폴더 목록 조회", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터
              - 없음

              ## 반환값 (GetFoldersResponse)
              - **`folders`**: 폴더 목록 (List<FolderDto>)
                - **`id`**: 폴더 ID
                - **`name`**: 폴더 이름
                - **`visibility`**: 공개 설정 (PRIVATE, SHARED)
                - **`thumbnailUrl`**: 썸네일 URL
                - **`isDefault`**: 기본 폴더 여부
                - **`placeCount`**: 폴더 내 장소 수
                - **`createdAt`**: 생성일시

              ## 특이사항
              - 로그인한 사용자의 모든 폴더를 조회합니다.
              - 기본 폴더를 포함하여 반환됩니다.

              ## 에러코드
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              """)
  ResponseEntity<GetFoldersResponse> getFolders(
      CustomUserDetails userDetails
  );

  @ApiLog(date = "2026.02.23", author = Author.SUHSAECHAN, issueNumber = 26, description = "폴더 생성 API 구현")
  @Operation(summary = "폴더 생성", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터
              - **`name`**: 폴더 이름 (필수, RequestBody)
              - **`visibility`**: 공개 설정 (필수, RequestBody) - PRIVATE, SHARED

              ## 반환값 (CreateFolderResponse)
              - **`id`**: 폴더 ID
              - **`name`**: 폴더 이름
              - **`visibility`**: 공개 설정
              - **`isDefault`**: 기본 폴더 여부
              - **`createdAt`**: 생성일시

              ## 특이사항
              - 새로 생성되는 폴더의 isDefault는 항상 false입니다.

              ## 에러코드
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              """)
  ResponseEntity<CreateFolderResponse> createFolder(
      CustomUserDetails userDetails,
      CreateFolderRequest request
  );

  @ApiLog(date = "2026.02.23", author = Author.SUHSAECHAN, issueNumber = 26, description = "폴더 수정 API 구현")
  @Operation(summary = "폴더 수정", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터
              - **`folderId`**: 수정할 폴더 ID (필수, Path Variable)
              - **`name`**: 폴더 이름 (선택, RequestBody)
              - **`visibility`**: 공개 설정 (선택, RequestBody) - PRIVATE, SHARED

              ## 반환값 (UpdateFolderResponse)
              - **`id`**: 폴더 ID
              - **`name`**: 폴더 이름
              - **`visibility`**: 공개 설정
              - **`isDefault`**: 기본 폴더 여부
              - **`updatedAt`**: 수정일시

              ## 특이사항
              - 본인의 폴더만 수정할 수 있습니다.

              ## 에러코드
              - **`FOLDER_NOT_FOUND`**: 폴더를 찾을 수 없습니다.
              - **`FOLDER_ACCESS_DENIED`**: 폴더에 대한 접근 권한이 없습니다.
              """)
  ResponseEntity<UpdateFolderResponse> updateFolder(
      CustomUserDetails userDetails,
      UUID folderId,
      UpdateFolderRequest request
  );

  @ApiLog(date = "2026.02.23", author = Author.SUHSAECHAN, issueNumber = 26, description = "폴더 삭제 API 구현")
  @Operation(summary = "폴더 삭제", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터
              - **`folderId`**: 삭제할 폴더 ID (필수, Path Variable)

              ## 반환값
              - **204 No Content**: 삭제 성공 (반환값 없음)

              ## 특이사항
              - 본인의 폴더만 삭제할 수 있습니다.
              - 기본 폴더는 삭제할 수 없습니다.
              - 폴더 삭제 시 폴더 내 장소 연결도 함께 삭제됩니다.

              ## 에러코드
              - **`FOLDER_NOT_FOUND`**: 폴더를 찾을 수 없습니다.
              - **`FOLDER_ACCESS_DENIED`**: 폴더에 대한 접근 권한이 없습니다.
              - **`CANNOT_DELETE_DEFAULT_FOLDER`**: 기본 폴더는 삭제할 수 없습니다.
              """)
  ResponseEntity<Void> deleteFolder(
      CustomUserDetails userDetails,
      UUID folderId
  );

  @ApiLog(date = "2026.02.23", author = Author.SUHSAECHAN, issueNumber = 26, description = "폴더 내 장소 목록 조회 API 구현")
  @Operation(summary = "폴더 내 장소 목록 조회", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터
              - **`folderId`**: 조회할 폴더 ID (필수, Path Variable)

              ## 반환값 (GetFolderPlacesResponse)
              - **`folderId`**: 폴더 ID
              - **`folderName`**: 폴더 이름
              - **`places`**: 장소 목록 (List<PlaceDto>)
                - **`placeId`**: 장소 ID
                - **`name`**: 장소명
                - **`address`**: 주소
                - **`rating`**: 별점 (0.0 ~ 5.0)
                - **`userRatingsTotal`**: 리뷰 수
                - **`photoUrls`**: 사진 URL 배열
                - **`description`**: 장소 요약 설명

              ## 특이사항
              - 본인의 폴더에 포함된 장소 목록을 조회합니다.
              - 장소는 position 순서대로 정렬되어 반환됩니다.

              ## 에러코드
              - **`FOLDER_NOT_FOUND`**: 폴더를 찾을 수 없습니다.
              - **`FOLDER_ACCESS_DENIED`**: 폴더에 대한 접근 권한이 없습니다.
              """)
  ResponseEntity<GetFolderPlacesResponse> getFolderPlaces(
      CustomUserDetails userDetails,
      UUID folderId
  );

  @ApiLog(date = "2026.02.23", author = Author.SUHSAECHAN, issueNumber = 26, description = "폴더에 장소 추가 API 구현")
  @Operation(summary = "폴더에 장소 추가", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터
              - **`folderId`**: 폴더 ID (필수, Path Variable)
              - **`placeId`**: 추가할 장소 ID (필수, RequestBody)

              ## 반환값 (AddFolderPlaceResponse)
              - **`id`**: 폴더-장소 연결 ID
              - **`folderId`**: 폴더 ID
              - **`placeId`**: 장소 ID
              - **`position`**: 정렬 순서
              - **`createdAt`**: 생성일시

              ## 특이사항
              - 본인의 폴더에만 장소를 추가할 수 있습니다.
              - 동일한 폴더에 같은 장소를 중복 추가할 수 없습니다.
              - position은 폴더 내 마지막 순서로 자동 배정됩니다.

              ## 에러코드
              - **`FOLDER_NOT_FOUND`**: 폴더를 찾을 수 없습니다.
              - **`FOLDER_ACCESS_DENIED`**: 폴더에 대한 접근 권한이 없습니다.
              - **`PLACE_NOT_FOUND`**: 장소를 찾을 수 없습니다.
              - **`FOLDER_PLACE_ALREADY_EXISTS`**: 이미 해당 폴더에 추가된 장소입니다.
              """)
  ResponseEntity<AddFolderPlaceResponse> addPlaceToFolder(
      CustomUserDetails userDetails,
      UUID folderId,
      AddFolderPlaceRequest request
  );

  @ApiLog(date = "2026.02.23", author = Author.SUHSAECHAN, issueNumber = 26, description = "폴더에서 장소 제거 API 구현")
  @Operation(summary = "폴더에서 장소 제거", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터
              - **`folderId`**: 폴더 ID (필수, Path Variable)
              - **`placeId`**: 제거할 장소 ID (필수, Path Variable)

              ## 반환값
              - **204 No Content**: 제거 성공 (반환값 없음)

              ## 특이사항
              - 본인의 폴더에서만 장소를 제거할 수 있습니다.
              - 장소 제거 시 폴더-장소 연결만 삭제되며, 장소 자체는 삭제되지 않습니다.

              ## 에러코드
              - **`FOLDER_NOT_FOUND`**: 폴더를 찾을 수 없습니다.
              - **`FOLDER_ACCESS_DENIED`**: 폴더에 대한 접근 권한이 없습니다.
              - **`PLACE_NOT_FOUND`**: 장소를 찾을 수 없습니다.
              - **`FOLDER_PLACE_NOT_FOUND`**: 폴더에 해당 장소가 존재하지 않습니다.
              """)
  ResponseEntity<Void> removePlaceFromFolder(
      CustomUserDetails userDetails,
      UUID folderId,
      UUID placeId
  );
}
