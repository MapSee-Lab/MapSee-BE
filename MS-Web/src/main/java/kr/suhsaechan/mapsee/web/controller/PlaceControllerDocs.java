package kr.suhsaechan.mapsee.web.controller;

import kr.suhsaechan.mapsee.auth.dto.CustomUserDetails;
import kr.suhsaechan.mapsee.common.constant.Author;
import kr.suhsaechan.mapsee.place.dto.GetSavedPlacesResponse;
import kr.suhsaechan.mapsee.place.dto.GetTemporaryPlacesResponse;
import kr.suhsaechan.mapsee.place.dto.PlaceDetailDto;
import kr.suhsaechan.mapsee.place.dto.SavePlaceResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import org.springframework.http.ResponseEntity;

public interface PlaceControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.10.25", author = Author.SUHSAECHAN, issueNumber = 36, description = "장소 상세 정보 조회 API 추가"),
  })
  @Operation(summary = "장소 세부정보 조회", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터
              - **`placeId`**: 조회할 장소 ID (필수, Path Variable)

              ## 반환값 (PlaceDetailDto)
              - **`id`**: 장소 ID
              - **`name`**: 장소명
              - **`address`**: 주소
              - **`country`**: 국가 코드 (ISO 3166-1 alpha-2)
              - **`latitude`**: 위도
              - **`longitude`**: 경도
              - **`businessType`**: 업종
              - **`phone`**: 전화번호
              - **`description`**: 장소 설명
              - **`types`**: 장소 유형 배열
              - **`businessStatus`**: 영업 상태
              - **`iconUrl`**: Google 아이콘 URL
              - **`rating`**: 평점 (0.0 ~ 5.0)
              - **`userRatingsTotal`**: 리뷰 수
              - **`photoUrls`**: 사진 URL 배열
              - **`platformReferences`**: 플랫폼별 참조 정보 (Google Place ID 등)
              - **`businessHours`**: 영업시간 목록
              - **`medias`**: 추가 미디어 목록

              ## 특이사항
              - Google Place ID를 포함한 플랫폼 참조 정보를 제공합니다.
              - 영업시간과 추가 미디어 정보가 포함됩니다.

              ## 에러코드
              - **`PLACE_NOT_FOUND`**: 장소를 찾을 수 없습니다.
              """)
  ResponseEntity<PlaceDetailDto> getPlaceDetail(
      CustomUserDetails userDetails,
      UUID placeId
  );

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.11.24", author = Author.SUHSAECHAN, issueNumber = 103, description = "임시 저장 장소 목록 조회 API 추가"),
  })
  @Operation(summary = "임시 저장 장소 목록 조회", description = """
              ## 인증(JWT): **필요**

              ## 반환값 (GetTemporaryPlacesResponse)
              - **`places`**: 임시 저장 장소 목록 (List<PlaceDto>)
                - **`placeId`**: 장소 ID
                - **`name`**: 장소명
                - **`address`**: 주소
                - **`rating`**: 별점 (0.0 ~ 5.0)
                - **`photoUrls`**: 사진 URL 배열
                - **`description`**: 장소 요약 설명

              ## 특이사항
              - AI 분석으로 자동 생성된 장소들을 조회합니다.
              - 사용자가 아직 저장 여부를 결정하지 않은 상태입니다.
              - 최신순으로 정렬되어 반환됩니다.

              ## 에러코드
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              """)
  ResponseEntity<GetTemporaryPlacesResponse> getTemporaryPlaces(
      CustomUserDetails userDetails
  );

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.01.15", author = Author.SUHSAECHAN, issueNumber = 129, description = "저장한 장소 목록 조회 API 응답에 userRatingsTotal 필드 추가"),
      @ApiChangeLog(date = "2025.11.24", author = Author.SUHSAECHAN, issueNumber = 103, description = "저장한 장소 목록 조회 API 추가"),
  })
  @Operation(summary = "저장한 장소 목록 조회", description = """
              ## 인증(JWT): **필요**

              ## 반환값 (GetSavedPlacesResponse)
              - **`places`**: 저장한 장소 목록 (List<PlaceDto>)
                - **`placeId`**: 장소 ID
                - **`name`**: 장소명
                - **`address`**: 주소
                - **`rating`**: 별점 (0.0 ~ 5.0)
                - **`userRatingsTotal`**: 리뷰 수
                - **`photoUrls`**: 사진 URL 배열
                - **`description`**: 장소 요약 설명

              ## 특이사항
              - 사용자가 명시적으로 저장한 장소들을 조회합니다.
              - 최신순으로 정렬되어 반환됩니다.
              - `/api/content/place/saved`와는 다른 MemberPlace 기반 조회입니다.

              ## 에러코드
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              """)
  ResponseEntity<GetSavedPlacesResponse> getSavedPlaces(
      CustomUserDetails userDetails
  );

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.11.24", author = Author.SUHSAECHAN, issueNumber = 103, description = "장소 저장 API 추가"),
  })
  @Operation(summary = "장소 저장", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터
              - **`placeId`**: 저장할 장소 ID (필수, Path Variable)

              ## 반환값 (SavePlaceResponse)
              - **`memberPlaceId`**: 회원 장소 ID
              - **`placeId`**: 장소 ID
              - **`savedStatus`**: 저장 상태 (SAVED)
              - **`savedAt`**: 저장 일시

              ## 특이사항
              - 임시 저장 상태(TEMPORARY)의 장소를 저장 상태(SAVED)로 변경합니다.
              - 저장 시점의 시간이 기록됩니다.

              ## 에러코드
              - **`PLACE_NOT_FOUND`**: 장소를 찾을 수 없습니다.
              - **`MEMBER_PLACE_NOT_FOUND`**: 회원의 장소 정보를 찾을 수 없습니다.
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              """)
  ResponseEntity<SavePlaceResponse> savePlace(
      CustomUserDetails userDetails,
      UUID placeId
  );

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.11.24", author = Author.SUHSAECHAN, issueNumber = 103, description = "임시 저장 장소 삭제 API 추가"),
  })
  @Operation(summary = "임시 저장 장소 삭제", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터
              - **`placeId`**: 삭제할 장소 ID (필수, Path Variable)

              ## 반환값
              - **204 No Content**: 삭제 성공 (반환값 없음)

              ## 특이사항
              - 임시 저장 상태(TEMPORARY)의 장소만 삭제 가능합니다.
              - 저장된 상태(SAVED)의 장소는 삭제할 수 없습니다.
              - Soft Delete 방식으로 데이터는 실제로 삭제되지 않습니다.

              ## 에러코드
              - **`PLACE_NOT_FOUND`**: 장소를 찾을 수 없습니다.
              - **`MEMBER_PLACE_NOT_FOUND`**: 회원의 장소 정보를 찾을 수 없습니다.
              - **`CANNOT_DELETE_SAVED_PLACE`**: 임시 저장된 장소만 삭제할 수 있습니다.
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              """)
  ResponseEntity<Void> deleteTemporaryPlace(
      CustomUserDetails userDetails,
      UUID placeId
  );
}
