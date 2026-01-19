package kr.suhsaechan.mapsy.web.controller;

import kr.suhsaechan.mapsy.auth.dto.CustomUserDetails;
import kr.suhsaechan.mapsy.common.constant.Author;
import kr.suhsaechan.mapsy.member.dto.CheckNameResponse;
import kr.suhsaechan.mapsy.member.dto.MemberDto;
import kr.suhsaechan.mapsy.member.dto.ProfileUpdateRequest;
import kr.suhsaechan.mapsy.member.dto.UpdateServiceAgreementTermsRequest;
import kr.suhsaechan.mapsy.member.dto.UpdateServiceAgreementTermsResponse;
import kr.suhsaechan.mapsy.member.dto.onboarding.response.OnboardingResponse;
import kr.suhsaechan.mapsy.member.dto.onboarding.request.UpdateBirthDateRequest;
import kr.suhsaechan.mapsy.member.dto.onboarding.request.UpdateGenderRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import kr.suhsaechan.suhapilog.annotation.ApiLog;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

public interface MemberControllerDocs {

  @ApiLog(date = "2025.10.16", author = Author.SUHSAECHAN, issueNumber = 22, description = "회원 관리 API 문서화")
  @Operation(summary = "회원 생성", description = """
              ## 인증(JWT): **불필요**
              
              ## 요청 파라미터 (MemberDto)
              - **`email`**: 회원 이메일 (필수)
              - **`name`**: 회원 닉네임 (필수)
              - **`profileImageUrl`**: 프로필 이미지 URL (선택)
              - **`socialPlatform`**: 소셜 플랫폼 (KAKAO, GOOGLE)
              - **`memberRole`**: 회원 권한 (ROLE_USER, ROLE_ADMIN)
              - **`status`**: 회원 상태 (ACTIVE, INACTIVE, DELETED)
              
              ## 반환값 (MemberDto)
              - **`memberId`**: 생성된 회원 ID
              - **`email`**: 회원 이메일
              - **`name`**: 회원 닉네임
              - **`profileImageUrl`**: 프로필 이미지 URL
              - **`socialPlatform`**: 소셜 플랫폼
              - **`memberRole`**: 회원 권한
              - **`status`**: 회원 상태
              - **`createdAt`**: 생성일시
              - **`updatedAt`**: 수정일시
              
              ## 특이사항
              - 새로운 회원을 생성합니다.
              - 이메일 중복 검사가 수행됩니다.
              
              ## 에러코드
              - **`EMAIL_ALREADY_EXISTS`**: 이미 가입된 이메일입니다.
              - **`INVALID_INPUT_VALUE`**: 유효하지 않은 입력값입니다.
              """)
  ResponseEntity<MemberDto> createMember(MemberDto memberDto);

  @ApiLog(date = "2025.01.15", author = Author.SUHSAECHAN, issueNumber = 22, description = "온보딩 약관 동의 API 추가")
  @Operation(summary = "약관 동의", description = """
              ## 인증(JWT): **필요**
              
              ## 요청 파라미터 (TermAgreementRequest)
              - **`isServiceTermsAndPrivacyAgreed`**: 서비스 이용약관 및 개인정보처리방침 동의 여부 (필수, true)
              - **`isMarketingAgreed`**: 마케팅 수신 동의 여부 (선택)
              
              ## 반환값 (UpdateServiceAgreementTermsResponse)
              - **`currentStep`**: 현재 온보딩 단계 (TERMS, BIRTH_DATE, GENDER, COMPLETED)
              - **`onboardingStatus`**: 온보딩 상태 (NOT_STARTED, IN_PROGRESS, COMPLETED)
              - **`member`**: 회원 정보 (디버깅용)
              
              ## 특이사항
              - 서비스 이용약관 및 개인정보처리방침 동의는 필수입니다.
              - 마케팅 수신 동의는 선택 사항입니다.
              - 약관 동의 후 온보딩 상태가 IN_PROGRESS로 변경됩니다.
              
              ## 에러코드
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              - **`MEMBER_TERMS_REQUIRED_NOT_AGREED`**: 필수 약관에 동의하지 않았습니다.
              """)
  ResponseEntity<UpdateServiceAgreementTermsResponse> agreeMemberTerms(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid UpdateServiceAgreementTermsRequest request);

  @ApiLog(date = "2025.01.15", author = Author.SUHSAECHAN, issueNumber = 22, description = "온보딩 생년월일 설정 API 추가")
  @Operation(summary = "생년월일 설정", description = """
              ## 인증(JWT): **필요**
              
              ## 요청 파라미터 (UpdateBirthDateRequest)
              - **`birthDate`**: 생년월일 (필수, LocalDate 형식)
              
              ## 반환값 (OnboardingResponse)
              - **`currentStep`**: 현재 온보딩 단계
              - **`onboardingStatus`**: 온보딩 상태
              - **`member`**: 회원 정보 (디버깅용)
              
              ## 특이사항
              - 온보딩 단계 중 생년월일 설정 단계를 완료합니다.
              
              ## 에러코드
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              - **`INVALID_INPUT_VALUE`**: 유효하지 않은 입력값입니다.
              """)
  ResponseEntity<OnboardingResponse> updateBirthDate(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid UpdateBirthDateRequest request);

  @ApiLog(date = "2025.01.15", author = Author.SUHSAECHAN, issueNumber = 22, description = "온보딩 성별 설정 API 추가")
  @Operation(summary = "성별 설정", description = """
              ## 인증(JWT): **필요**
              
              ## 요청 파라미터 (UpdateGenderRequest)
              - **`gender`**: 성별 (필수, MALE 또는 FEMALE)
              
              ## 반환값 (OnboardingResponse)
              - **`currentStep`**: 현재 온보딩 단계
              - **`onboardingStatus`**: 온보딩 상태
              - **`member`**: 회원 정보 (디버깅용)
              
              ## 특이사항
              - 온보딩 단계 중 성별 설정 단계를 완료합니다.
              
              ## 에러코드
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              - **`INVALID_INPUT_VALUE`**: 유효하지 않은 입력값입니다.
              """)
  ResponseEntity<OnboardingResponse> updateGender(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid UpdateGenderRequest request);

  @ApiLog(date = "2025.10.16", author = Author.SUHSAECHAN, issueNumber = 22, description = "회원 관리 API 문서화")
  @Operation(summary = "전체 회원 목록 조회", description = """
              ## 인증(JWT): **불필요**
              
              ## 요청 파라미터
              - 없음
              
              ## 반환값 (List<MemberDto>)
              - 전체 회원 목록을 반환합니다.
              - 각 회원의 상세 정보가 포함됩니다.
              
              ## 특이사항
              - 모든 회원 데이터를 조회합니다.
              - 삭제되지 않은 회원만 조회됩니다.
              
              ## 에러코드
              - **`INTERNAL_SERVER_ERROR`**: 서버에 문제가 발생했습니다.
              """)
  ResponseEntity<List<MemberDto>> getAllMembers();

  @ApiLog(date = "2025.10.16", author = Author.SUHSAECHAN, issueNumber = 22, description = "회원 관리 API 문서화")
  @Operation(summary = "회원 단건 조회 (ID)", description = """
              ## 인증(JWT): **불필요**
              
              ## 요청 파라미터
              - **`memberId`**: 회원 ID (Path Variable)
              
              ## 반환값 (MemberDto)
              - **`memberId`**: 회원 ID
              - **`email`**: 회원 이메일
              - **`nickname`**: 회원 닉네임
              - **`profileImageUrl`**: 프로필 이미지 URL
              - **`socialPlatform`**: 소셜 플랫폼
              - **`memberRole`**: 회원 권한
              - **`status`**: 회원 상태
              - **`createdAt`**: 생성일시
              - **`updatedAt`**: 수정일시
              
              ## 특이사항
              - 회원 ID로 특정 회원을 조회합니다.
              - 삭제된 회원은 조회되지 않습니다.
              
              ## 에러코드
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              - **`INVALID_INPUT_VALUE`**: 유효하지 않은 입력값입니다.
              """)
  ResponseEntity<MemberDto> getMemberById(UUID memberId);

  @ApiLog(date = "2025.10.16", author = Author.SUHSAECHAN, issueNumber = 22, description = "회원 관리 API 문서화")
  @Operation(summary = "회원 단건 조회 (Email)", description = """
              ## 인증(JWT): **불필요**
              
              ## 요청 파라미터
              - **`email`**: 회원 이메일 (Path Variable)
              
              ## 반환값 (MemberDto)
              - **`memberId`**: 회원 ID
              - **`email`**: 회원 이메일
              - **`nickname`**: 회원 닉네임
              - **`profileImageUrl`**: 프로필 이미지 URL
              - **`socialPlatform`**: 소셜 플랫폼
              - **`memberRole`**: 회원 권한
              - **`status`**: 회원 상태
              - **`createdAt`**: 생성일시
              - **`updatedAt`**: 수정일시
              
              ## 특이사항
              - 이메일로 특정 회원을 조회합니다.
              - 삭제된 회원은 조회되지 않습니다.
              
              ## 에러코드
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              - **`INVALID_INPUT_VALUE`**: 유효하지 않은 입력값입니다.
              """)
  ResponseEntity<MemberDto> getMemberByEmail(String email);

  @ApiLog(date = "2025.10.16", author = Author.SUHSAECHAN, issueNumber = 22, description = "회원 관리 API 문서화")
  @Operation(summary = "회원 프로필 설정(수정)", description = """
              ## 인증(JWT): **필요**
              
              ## 요청 파라미터 (ProfileUpdateRequest)
              - **`name`**: 이름 (필수)
              - **`gender`**: 성별 (MALE, FEMALE, NONE)
              - **`birthDate`**: 생년월일 (LocalDate 형식)

              ## 반환값 (MemberDto)
              - **`memberId`**: 회원 ID
              - **`email`**: 회원 이메일
              - **`name`**: 회원 이름
              - **`gender`**: 성별
              - **`birthDate`**: 생년월일
              - **`onboardingStatus`**: 온보딩 상태
              
              ## 특이사항
              - 회원 프로필 정보를 업데이트합니다.
              - 이름 중복 검사가 수행됩니다.

              ## 에러코드
              - **`MEMBER_NOT_FOUND`**: 회원을 찾을 수 없습니다.
              - **`NAME_ALREADY_EXISTS`**: 이미 사용 중인 이름입니다.
              - **`INVALID_INPUT_VALUE`**: 유효하지 않은 입력값입니다.
              """)
  ResponseEntity<MemberDto> updateProfile(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid ProfileUpdateRequest request);

  @ApiLog(date = "2025.11.23", author = Author.SUHSAECHAN, issueNumber = 106, description = "닉네임 중복 확인 API 추가")
  @Operation(summary = "닉네임 중복 확인", description = """
              ## 인증(JWT): **불필요**

              ## 요청 파라미터
              - **`name`**: 확인할 닉네임 (Query Parameter, 필수, 2자 이상 50자 이하)

              ## 반환값 (CheckNicknameResponse)
              - **`isAvailable`**: 사용 가능 여부 (true: 사용 가능, false: 이미 사용 중)
              - **`name`**: 확인한 닉네임

              ## 특이사항
              - 회원가입 전에도 사용 가능한 API입니다.
              - 닉네임 길이는 2자 이상 50자 이하여야 합니다.
              - 탈퇴한 회원의 닉네임은 타임스탬프가 추가되어 중복 체크에서 제외됩니다.

              ## 에러코드
              - **`INVALID_NAME_LENGTH`**: 닉네임은 2자 이상 50자 이하여야 합니다.
              - **`INVALID_INPUT_VALUE`**: 유효하지 않은 입력값입니다.
              """)
  ResponseEntity<CheckNameResponse> checkName(@RequestParam String name);
}
