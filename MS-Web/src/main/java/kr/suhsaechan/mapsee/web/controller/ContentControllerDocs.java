package kr.suhsaechan.mapsee.web.controller;

import kr.suhsaechan.mapsee.auth.dto.CustomUserDetails;
import kr.suhsaechan.mapsee.sns.dto.GetContentInfoResponse;
import kr.suhsaechan.mapsee.sns.dto.GetMemberContentPageResponse;
import kr.suhsaechan.mapsee.sns.dto.RequestPlaceExtractionRequest;
import kr.suhsaechan.mapsee.common.constant.Author;
import kr.suhsaechan.mapsee.place.dto.GetSavedPlacesResponse;
import kr.suhsaechan.mapsee.sns.dto.GetRecentContentResponse;
import kr.suhsaechan.mapsee.sns.dto.RequestPlaceExtractionResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface ContentControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.11.21", author = Author.SUHSAECHAN, issueNumber = 88, description = "ContentController 리팩토링에 따른 DOCS 간소화"),
      @ApiChangeLog(date = "2025.11.02", author = Author.KANGJIYUN, issueNumber = 54, description = "콘텐츠 Docs 추가 및 리팩토링"),
      @ApiChangeLog(date = "2025.10.15", author = Author.SUHSAECHAN, issueNumber = 22, description = "온보딩 성별 설정 API 추가")
  })
  @Operation(summary = "SNS URL로 콘텐츠 생성 및 장소 추출 요청", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터
              - **`snsUrl`**: SNS URL (Instagram, YouTube Shorts 등)

              ## 반환값
              - **`contentId`**: 생성된 콘텐츠 ID
              - **`status`**: 장소 추출 상태 (PENDING, ANALYZING, COMPLETED, FAILED, DELETED)

              ## 동작 방식
              - SNS URL을 받아 콘텐츠를 생성하고 AI 서버에 장소 추출을 요청합니다.
              - 초기 상태는 `PENDING`이며, AI 서버 처리 완료 시 Webhook으로 상태가 업데이트됩니다.
              """)
  ResponseEntity<RequestPlaceExtractionResponse> requestPlaceExtraction(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody RequestPlaceExtractionRequest request);

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.11.23", author = Author.SUHSAECHAN, issueNumber = 111, description = "단일 SNS 컨텐츠 조회 API 추가")
  })
  @Operation(summary = "단일 SNS 컨텐츠 정보 조회", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터
              - **`contentId`**: 조회할 Content UUID (Path Variable)

              ## 반환값
              - **`content`**: Content 상세 정보 (ContentDto)
                - `id`: 콘텐츠 ID
                - `platform`: 플랫폼 유형 (INSTAGRAM, YOUTUBE 등)
                - `status`: 처리 상태 (PENDING, COMPLETED, FAILED 등)
                - `platformUploader`: 업로더 이름
                - `caption`: 캡션
                - `thumbnailUrl`: 썸네일 URL
                - `originalUrl`: 원본 SNS URL
                - `title`: 제목
                - `summary`: 요약 설명
                - `lastCheckedAt`: 마지막 확인 시각
              - **`places`**: 연관된 Place 목록 (List<PlaceDto>, position 순서)
                - 각 Place 정보: `id`, `name`, `address`, `latitude`, `longitude`, `rating` 등

              ## 동작 방식
              - Content ID로 콘텐츠 정보와 연관된 장소 목록을 조회합니다.
              - Place 목록은 position 순서대로 정렬되어 반환됩니다.
              - Content가 존재하지 않으면 404 에러를 반환합니다.
              - 연관된 Place가 없는 경우 빈 배열을 반환합니다.
              """)
  ResponseEntity<GetContentInfoResponse> getContentInfo(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID contentId);

  @ApiChangeLogs({
      @ApiChangeLog(date = "2025.11.23", author = Author.SUHSAECHAN, issueNumber = 112, description = "Member가 소유한 Content 목록 조회 API 추가")
  })
  @Operation(summary = "회원 콘텐츠 목록 조회", description = """
              ## 인증(JWT): **필요**

              ## 요청 파라미터
              - **`pageSize`**: 페이지 크기 (Query Parameter, 선택, 기본값 10)

              ## 반환값
              - **`contentPage`**: Page<ContentDto>
                - `content`: 콘텐츠 목록 (ContentDto 배열)
                  - `id`: 콘텐츠 ID
                  - `platform`: 플랫폼 유형 (INSTAGRAM, YOUTUBE 등)
                  - `status`: 처리 상태 (PENDING, COMPLETED, FAILED 등)
                  - `platformUploader`: 업로더 이름
                  - `caption`: 캡션
                  - `thumbnailUrl`: 썸네일 URL
                  - `originalUrl`: 원본 SNS URL
                  - `title`: 제목
                  - `summary`: 요약 설명
                  - `lastCheckedAt`: 마지막 확인 시각
                - `totalElements`: 전체 콘텐츠 개수
                - `totalPages`: 전체 페이지 수
                - `number`: 현재 페이지 번호 (0부터 시작)
                - `size`: 페이지 크기
                - `first`: 첫 페이지 여부
                - `last`: 마지막 페이지 여부

              ## 동작 방식
              - 인증된 회원이 소유한 Content 목록을 최신순(createdAt DESC)으로 조회합니다.
              - Place 정보는 제외하고 Content 정보만 반환합니다.
              - 페이지 크기를 지정하지 않으면 기본 10개가 조회됩니다.
              - 첫 페이지(0번 페이지)만 조회됩니다.
              """)
  ResponseEntity<GetMemberContentPageResponse> getMemberContentPage(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(required = false, defaultValue = "10") Integer pageSize);


    @ApiChangeLogs(
            @ApiChangeLog(date = "2025.11.16", author = Author.KANGJIYUN, issueNumber = 78, description = "최근 콘텐츠 조회 Docs 추가 및 리팩토링")
    )
    @Operation(
            summary = "최근 SNS 콘텐츠 목록 조회",
            description =
                    """
                        ## 인증(JWT): **필요**
                        
                        ## 요청 파라미터
                        - JWT 인증만 필요, 별도 파라미터 없음
          
                        ## 반환값 (List<RecentContentResponse>)
                        - **`contentId`**: 콘텐츠 UUID
                        - **`platform`**: 콘텐츠 플랫폼 (INSTAGRAM, YOUTUBE, TIKTOK 등)
                        - **`title`**: 콘텐츠 제목
                        - **`thumbnailUrl`**: 썸네일 URL
                        - **`originalUrl`**: 원본 URL
                        - **`status`**: 콘텐츠 상태 (PENDING, COMPLETED, FAILED 등)
                        - **`createdAt`**: 생성일시
          
                        ## 특이사항
                        - 인증된 사용자의 최근 10개 SNS 콘텐츠 목록을 생성일시 내림차순으로 조회합니다.
          
                        ## 에러코드
                        - **`MEMBER_NOT_FOUND`**: 해당 회원을 찾을 수 없습니다.
                        """)
    ResponseEntity<GetRecentContentResponse> getRecentContents(CustomUserDetails userDetails);

    @ApiChangeLogs({
            @ApiChangeLog(date = "2025.11.20", author = Author.KANGJIYUN, issueNumber = 80, description = "최신순으로 장소 조회")
    })
    @Operation(
            summary = "최근 장소 목록 조회",
            description =
                    """
                        ## 인증(JWT): **필요**
                        
                        ## 요청 파라미터
                        - JWT 인증만 필요, 별도 파라미터 없음
                        
                        ## 반환값 (List<>)
                        - **`placeId`**: 장소 ID
                        - **`name`**: 장소 이름
                        - **`address`**: 장소 주소
                        - **`rating`**: 장소 평점
                        - **`photoUrls`**: 장소 사진 URL 목록
                        - **`description`**: 장소 설명
                        
                        ## 특이사항
                        - 최신순으로 장소를 조회합니다.
                        - 최대 10개의 장소를 반환합니다.
                        - 장소의 사진 URL은 최대 10개까지 반환합니다.
                        
                        ## 에러코드
                        - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
                        """)
    ResponseEntity<GetSavedPlacesResponse> getSavedPlaces(CustomUserDetails userDetails);

}
