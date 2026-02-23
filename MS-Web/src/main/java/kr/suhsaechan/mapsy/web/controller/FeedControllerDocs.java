package kr.suhsaechan.mapsy.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import kr.suhsaechan.mapsy.auth.dto.CustomUserDetails;
import kr.suhsaechan.mapsy.common.constant.Author;
import kr.suhsaechan.mapsy.place.dto.KeywordDto;
import kr.suhsaechan.mapsy.place.dto.PlaceDto;
import kr.suhsaechan.suhapilog.annotation.ApiLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface FeedControllerDocs {

  @ApiLog(date = "2026.01.28", author = Author.SUHSAECHAN, issueNumber = 19, description = "최신 장소 피드 API 추가")
  @Operation(summary = "최신 장소 피드 조회", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터 (Query Parameters)
              - **`page`**: 페이지 번호 (0부터 시작, 기본값: 0)
              - **`size`**: 페이지 크기 (기본값: 20)

              ## 반환값 (Page<PlaceDto>)
              - **`content`**: 장소 목록
                - **`placeId`**: 장소 ID
                - **`name`**: 장소명
                - **`address`**: 주소
                - **`rating`**: 별점 (0.0 ~ 5.0)
                - **`userRatingsTotal`**: 리뷰 수
                - **`photoUrls`**: 사진 URL 배열
                - **`description`**: 장소 요약 설명
              - **`totalElements`**: 전체 장소 수
              - **`totalPages`**: 전체 페이지 수
              - **`size`**: 페이지 크기
              - **`number`**: 현재 페이지 번호

              ## 특이사항
              - 생성일 기준 최신순으로 정렬됩니다.
              - 홈 피드에서 무한 스크롤에 사용됩니다.
              """)
  ResponseEntity<Page<PlaceDto>> getLatestPlaces(
      CustomUserDetails userDetails,
      Pageable pageable
  );

  @ApiLog(date = "2026.01.28", author = Author.SUHSAECHAN, issueNumber = 19, description = "인기 장소 피드 API 추가")
  @Operation(summary = "인기 장소 피드 조회", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터 (Query Parameters)
              - **`page`**: 페이지 번호 (0부터 시작, 기본값: 0)
              - **`size`**: 페이지 크기 (기본값: 20)

              ## 반환값 (Page<PlaceDto>)
              - **`content`**: 장소 목록
                - **`placeId`**: 장소 ID
                - **`name`**: 장소명
                - **`address`**: 주소
                - **`rating`**: 별점 (0.0 ~ 5.0)
                - **`userRatingsTotal`**: 리뷰 수
                - **`photoUrls`**: 사진 URL 배열
                - **`description`**: 장소 요약 설명
              - **`totalElements`**: 전체 장소 수
              - **`totalPages`**: 전체 페이지 수

              ## 특이사항
              - 저장 횟수 기준 내림차순으로 정렬됩니다.
              - 같은 저장 횟수의 경우 생성일 기준 최신순으로 정렬됩니다.
              """)
  ResponseEntity<Page<PlaceDto>> getPopularPlaces(
      CustomUserDetails userDetails,
      Pageable pageable
  );

  @ApiLog(date = "2026.01.28", author = Author.SUHSAECHAN, issueNumber = 19, description = "내 TOP 저장 장소 API 추가")
  @Operation(summary = "내 TOP 저장 장소 조회", description = """
              ## 인증(JWT): **필요**

              ## 반환값 (List<PlaceDto>)
              - **`placeId`**: 장소 ID
              - **`name`**: 장소명
              - **`address`**: 주소
              - **`rating`**: 별점 (0.0 ~ 5.0)
              - **`userRatingsTotal`**: 리뷰 수
              - **`photoUrls`**: 사진 URL 배열
              - **`description`**: 장소 요약 설명

              ## 특이사항
              - 최대 10개의 장소를 반환합니다.
              - 저장일 기준 최신순으로 정렬됩니다.
              - 저장된 상태(SAVED)의 장소만 포함됩니다.

              ## 에러코드
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              """)
  ResponseEntity<List<PlaceDto>> getMyTopPlaces(
      CustomUserDetails userDetails
  );

  @ApiLog(date = "2026.01.28", author = Author.SUHSAECHAN, issueNumber = 19, description = "트렌드 키워드 API 추가")
  @Operation(summary = "트렌드 키워드 조회", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터 (Query Parameters)
              - **`size`**: 조회 개수 (기본값: 20)

              ## 반환값 (List<KeywordDto>)
              - **`id`**: 키워드 ID
              - **`keyword`**: 키워드 문자열
              - **`count`**: 사용 횟수
              - **`trendScore`**: 트렌드 점수

              ## 특이사항
              - 트렌드 점수 기준 내림차순으로 정렬됩니다.
              - 홈 화면의 "떠오르는 키워드" 섹션에 사용됩니다.
              """)
  ResponseEntity<List<KeywordDto>> getTrendingKeywords(
      CustomUserDetails userDetails,
      int size
  );
}
