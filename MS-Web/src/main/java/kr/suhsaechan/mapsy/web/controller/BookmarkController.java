package kr.suhsaechan.mapsy.web.controller;

import java.util.UUID;
import kr.suhsaechan.mapsy.auth.dto.CustomUserDetails;
import kr.suhsaechan.mapsy.place.dto.BookmarkDto;
import kr.suhsaechan.mapsy.place.dto.UpdateBookmarkRequest;
import kr.suhsaechan.mapsy.place.service.MemberPlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/bookmarks")
public class BookmarkController implements BookmarkControllerDocs {

  private final MemberPlaceService memberPlaceService;

  /**
   * 북마크 목록 조회 (페이지네이션)
   */
  @GetMapping
  @Override
  public ResponseEntity<Page<BookmarkDto>> getBookmarks(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PageableDefault(size = 20, sort = "savedAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    log.info("Get bookmarks request from member: {}, page: {}, size: {}",
        userDetails.getMemberId(), pageable.getPageNumber(), pageable.getPageSize());

    Page<BookmarkDto> response = memberPlaceService.getBookmarks(
        userDetails.getMemberId(),
        pageable
    );

    return ResponseEntity.ok(response);
  }

  /**
   * 북마크 수정
   */
  @PatchMapping("/{memberPlaceId}")
  @Override
  public ResponseEntity<BookmarkDto> updateBookmark(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable UUID memberPlaceId,
      @RequestBody UpdateBookmarkRequest request
  ) {
    log.info("Update bookmark request from member: {}, memberPlaceId: {}",
        userDetails.getMemberId(), memberPlaceId);

    BookmarkDto response = memberPlaceService.updateBookmark(
        userDetails.getMemberId(),
        memberPlaceId,
        request
    );

    return ResponseEntity.ok(response);
  }
}
