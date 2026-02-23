package kr.suhsaechan.mapsy.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import kr.suhsaechan.mapsy.auth.dto.CustomUserDetails;
import kr.suhsaechan.mapsy.common.constant.Author;
import kr.suhsaechan.mapsy.place.dto.BookmarkDto;
import kr.suhsaechan.mapsy.place.dto.UpdateBookmarkRequest;
import kr.suhsaechan.suhapilog.annotation.ApiLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface BookmarkControllerDocs {

  @ApiLog(date = "2026.01.28", author = Author.SUHSAECHAN, issueNumber = 19, description = "북마크 목록 조회 API 추가")
  @Operation(summary = "북마크 목록 조회", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터 (Query Parameters)
              - **`page`**: 페이지 번호 (0부터 시작, 기본값: 0)
              - **`size`**: 페이지 크기 (기본값: 20)
              - **`sort`**: 정렬 기준 (예: savedAt,desc)

              ## 반환값 (Page<BookmarkDto>)
              - **`content`**: 북마크 목록
                - **`memberPlaceId`**: MemberPlace ID
                - **`place`**: 장소 정보 (PlaceDto)
                - **`folder`**: 폴더명
                - **`memo`**: 메모
                - **`rating`**: 별점 (1-5)
                - **`visited`**: 방문 여부
                - **`visitedAt`**: 방문 일시
                - **`savedAt`**: 저장 일시
              - **`totalElements`**: 전체 북마크 수
              - **`totalPages`**: 전체 페이지 수
              - **`size`**: 페이지 크기
              - **`number`**: 현재 페이지 번호

              ## 특이사항
              - 저장된 상태(SAVED)의 북마크만 조회됩니다.
              - 기본적으로 저장일 기준 최신순으로 정렬됩니다.

              ## 에러코드
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              """)
  ResponseEntity<Page<BookmarkDto>> getBookmarks(
      CustomUserDetails userDetails,
      Pageable pageable
  );

  @ApiLog(date = "2026.01.28", author = Author.SUHSAECHAN, issueNumber = 19, description = "북마크 수정 API 추가")
  @Operation(summary = "북마크 수정", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터
              - **`memberPlaceId`**: 수정할 MemberPlace ID (필수, Path Variable)

              ## 요청 바디 (UpdateBookmarkRequest)
              - **`folder`**: 폴더명 (null이면 변경하지 않음)
              - **`memo`**: 메모 (null이면 변경하지 않음)
              - **`rating`**: 별점 1-5 (null이면 변경하지 않음)
              - **`visited`**: 방문 여부 (null이면 변경하지 않음)
              - **`visitedAt`**: 방문 일시 (null이면 visited=true 시 현재 시간)

              ## 반환값 (BookmarkDto)
              - **`memberPlaceId`**: MemberPlace ID
              - **`place`**: 장소 정보 (PlaceDto)
              - **`folder`**: 폴더명
              - **`memo`**: 메모
              - **`rating`**: 별점 (1-5)
              - **`visited`**: 방문 여부
              - **`visitedAt`**: 방문 일시
              - **`savedAt`**: 저장 일시

              ## 특이사항
              - 저장된 상태(SAVED)의 북마크만 수정 가능합니다.
              - null 값인 필드는 변경되지 않습니다 (부분 업데이트).

              ## 에러코드
              - **`MEMBER_PLACE_NOT_FOUND`**: 회원의 장소 정보를 찾을 수 없습니다.
              - **`CANNOT_UPDATE_UNSAVED_PLACE`**: 저장된 장소만 수정할 수 있습니다.
              - **`INVALID_RATING`**: 별점은 1-5 사이의 값이어야 합니다.
              """)
  ResponseEntity<BookmarkDto> updateBookmark(
      CustomUserDetails userDetails,
      UUID memberPlaceId,
      UpdateBookmarkRequest request
  );
}
