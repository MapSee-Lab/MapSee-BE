package kr.suhsaechan.mapsy.web.controller;

import kr.suhsaechan.mapsy.auth.dto.CustomUserDetails;
import kr.suhsaechan.mapsy.member.dto.CheckNameResponse;
import kr.suhsaechan.mapsy.member.dto.MemberDto;
import kr.suhsaechan.mapsy.member.dto.ProfileUpdateRequest;
import kr.suhsaechan.mapsy.member.dto.UpdateServiceAgreementTermsRequest;
import kr.suhsaechan.mapsy.member.dto.UpdateServiceAgreementTermsResponse;
import kr.suhsaechan.mapsy.member.dto.onboarding.response.OnboardingResponse;
import kr.suhsaechan.mapsy.member.dto.onboarding.request.UpdateBirthDateRequest;
import kr.suhsaechan.mapsy.member.dto.onboarding.request.UpdateGenderRequest;
import kr.suhsaechan.mapsy.member.service.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "회원 관리", description = "회원 생성, 조회 등의 기능을 제공하는 API")
public class MemberController implements MemberControllerDocs {

  private final MemberService memberService;

  @PostMapping
  public ResponseEntity<MemberDto> createMember(@Valid @RequestBody MemberDto memberDto) {
    MemberDto dto = memberService.createMember(memberDto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(dto);
  }

  @PostMapping("/onboarding/terms")
  public ResponseEntity<UpdateServiceAgreementTermsResponse> agreeMemberTerms(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody UpdateServiceAgreementTermsRequest request
  ) {
    request.setMemberId(userDetails.getMemberId());
    UpdateServiceAgreementTermsResponse response = memberService.agreeTerms(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/onboarding/birth-date")
  public ResponseEntity<OnboardingResponse> updateBirthDate(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody UpdateBirthDateRequest request
  ) {
    request.setMemberId(userDetails.getMemberId());
    OnboardingResponse response = memberService.updateBirthDate(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/onboarding/gender")
  public ResponseEntity<OnboardingResponse> updateGender(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody UpdateGenderRequest request
  ) {
    request.setMemberId(userDetails.getMemberId());
    OnboardingResponse response = memberService.updateGender(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/profile")
  @Override
  public ResponseEntity<MemberDto> updateProfile(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody ProfileUpdateRequest request
  ) {

    // 로그인한 사용자 정보에서 memberId 추출
    UUID memberId = userDetails.getMemberId();

    // 프로필 업데이트
    MemberDto updatedMember = memberService.updateProfile(memberId, request);

    return ResponseEntity.ok(updatedMember);
  }

  @GetMapping
  @Override
  public ResponseEntity<List<MemberDto>> getAllMembers() {
    List<MemberDto> dtos = memberService.getAllMembers();
    return ResponseEntity.ok(dtos);
  }

  @GetMapping("/{memberId}")
  @Override
  public ResponseEntity<MemberDto> getMemberById(@PathVariable UUID memberId) {
    MemberDto dto = memberService.getMemberById(memberId);
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/email/{email}")
  @Override
  public ResponseEntity<MemberDto> getMemberByEmail(@PathVariable String email) {
    MemberDto dto = memberService.getMemberByEmail(email);
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/check-name")
  @Override
  public ResponseEntity<CheckNameResponse> checkName(
      @RequestParam String name
  ) {
    return ResponseEntity.ok(memberService.checkNameAvailability(name));
  }

}
