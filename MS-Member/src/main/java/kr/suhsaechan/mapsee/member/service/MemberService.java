package kr.suhsaechan.mapsee.member.service;

import kr.suhsaechan.mapsee.common.exception.CustomException;
import kr.suhsaechan.mapsee.common.exception.ErrorCodeBuilder;
import kr.suhsaechan.mapsee.common.exception.constant.ErrorCode;
import kr.suhsaechan.mapsee.common.exception.constant.ErrorMessageTemplate.Subject;
import kr.suhsaechan.mapsee.common.exception.constant.ErrorMessageTemplate.BusinessStatus;
import kr.suhsaechan.mapsee.member.constant.MemberGender;
import kr.suhsaechan.mapsee.member.constant.MemberOnboardingStatus;
import kr.suhsaechan.mapsee.member.constant.OnboardingStep;
import kr.suhsaechan.mapsee.member.dto.CheckNameResponse;
import kr.suhsaechan.mapsee.member.dto.InterestDto;
import kr.suhsaechan.mapsee.member.dto.MemberDto;
import kr.suhsaechan.mapsee.member.dto.ProfileUpdateRequest;
import kr.suhsaechan.mapsee.member.dto.UpdateServiceAgreementTermsRequest;
import kr.suhsaechan.mapsee.member.dto.UpdateServiceAgreementTermsResponse;
import kr.suhsaechan.mapsee.member.dto.onboarding.request.UpdateBirthDateRequest;
import kr.suhsaechan.mapsee.member.dto.onboarding.request.UpdateGenderRequest;
import kr.suhsaechan.mapsee.member.dto.onboarding.request.UpdateInterestsRequest;
import kr.suhsaechan.mapsee.member.dto.onboarding.request.UpdateNameRequest;
import kr.suhsaechan.mapsee.member.dto.onboarding.response.OnboardingResponse;
import kr.suhsaechan.mapsee.member.entity.Interest;
import kr.suhsaechan.mapsee.member.entity.Member;
import kr.suhsaechan.mapsee.member.entity.MemberInterest;
import kr.suhsaechan.mapsee.member.repository.InterestRepository;
import kr.suhsaechan.mapsee.member.repository.MemberInterestRepository;
import kr.suhsaechan.mapsee.member.repository.MemberRepository;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

  private final MemberRepository memberRepository;
  private final MemberInterestRepository memberInterestRepository;
  private final InterestRepository interestRepository;

  // 만 14세 이상만 가입 가능
  LocalDate today = LocalDate.now();

  /**
   * 회원 생성
   *
   * @param memberDto 생성할 회원 데이터
   * @return 생성된 회원 데이터
   */
  @Transactional
  public MemberDto createMember(MemberDto memberDto) {
    // 이메일 중복 체크
    if (memberRepository.existsByEmail(memberDto.getEmail())) {
      ErrorCodeBuilder errorCode =
          ErrorCodeBuilder.businessStatus(Subject.MEMBER, BusinessStatus.DUPLICATE, HttpStatus.CONFLICT);
      throw new CustomException(errorCode);
    }

    // Entity 변환 및 저장
    Member entity = Member.builder()
        .email(memberDto.getEmail())
        .name(memberDto.getName())
        .build();

    Member savedEntity = memberRepository.save(entity);
    return MemberDto.entityToDto(savedEntity);
  }

  /**
   * 온보딩 단계 계산
   *
   * @param member 회원 엔티티
   * @return 현재 온보딩 단계
   */
  public OnboardingStep calculateOnboardingStep(Member member) {
    // COMPLETED 상태면 바로 반환 (쿼리 최적화)
    if (member.getOnboardingStatus() == MemberOnboardingStatus.COMPLETED) {
      return OnboardingStep.COMPLETED;
    }

    // 약관 동의 체크
    if (!Boolean.TRUE.equals(member.getIsServiceTermsAndPrivacyAgreed())) {
      return OnboardingStep.TERMS;
    }

    // 이름 체크 (기본값이거나 빈 문자열인 경우)
    if (member.getName() == null || member.getName().trim().isEmpty() || member.getName().equals("name")) {
      return OnboardingStep.NAME;
    }

    // 생년월일 체크
    if (member.getBirthDate() == null) {
      return OnboardingStep.BIRTH_DATE;
    }

    // 성별 체크
    if (member.getGender() == null) {
      return OnboardingStep.GENDER;
    }

    // 관심사 체크
    List<MemberInterest> interests = memberInterestRepository.findByMemberId(member.getId());
    if (interests == null || interests.isEmpty()) {
      return OnboardingStep.INTERESTS;
    }

    // 모든 단계 완료
    return OnboardingStep.COMPLETED;
  }

  /**
   * 온보딩 단계 계산 및 저장
   *
   * @param member 회원 엔티티
   * @return 계산된 온보딩 단계
   */
  @Transactional
  public OnboardingStep calculateAndSaveOnboardingStep(Member member) {
    OnboardingStep step = calculateOnboardingStep(member);
    member.setOnboardingStep(calculateOnboardingStep(member));

    if(step==OnboardingStep.COMPLETED) {
      member.setOnboardingStatus(MemberOnboardingStatus.COMPLETED);
    }

    memberRepository.save(member);
    return step;
  }

  /**
   * 약관 동의 처리
   *
   * @param request 약관 동의 요청
   * @return 온보딩 응답 (현재 단계, 상태, 회원 정보)
   */
  @Transactional
  public UpdateServiceAgreementTermsResponse agreeTerms(UpdateServiceAgreementTermsRequest request) {
    UUID memberId = request.getMemberId();

    // 회원 조회
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    if(member.getOnboardingStep() != OnboardingStep.TERMS && member.getOnboardingStep() != OnboardingStep.COMPLETED) {
      log.warn("[Onboarding] 현재 온보딩 단계가 약관 동의가 아님 - memberId={}, currentStep={}",
          memberId, member.getOnboardingStep());
      throw new CustomException(ErrorCode.INVALID_ONBOARDING_STEP);
    }

    // 필수 약관 검증
    if (!Boolean.TRUE.equals(request.getIsServiceTermsAndPrivacyAgreed())) {
      log.warn("[Onboarding] 필수 약관에 동의하지 않음 - memberId={}", memberId);
      throw new CustomException(ErrorCode.MEMBER_TERMS_REQUIRED_NOT_AGREED);
    }

    // 동의 정보 반영
    member.setIsServiceTermsAndPrivacyAgreed(true);
    member.setIsMarketingAgreed(Boolean.TRUE.equals(request.getIsMarketingAgreed()));

    // 온보딩 상태를 IN_PROGRESS로 변경
    if (member.getOnboardingStatus() == MemberOnboardingStatus.NOT_STARTED) {
      member.setOnboardingStatus(MemberOnboardingStatus.IN_PROGRESS);
    }

    // 온보딩 단계 계산 및 저장
    OnboardingStep currentStep = calculateAndSaveOnboardingStep(member);

    log.info("[Onboarding] 약관 동의 완료 - memberId={}, currentStep={}", memberId, currentStep);

    // Response 생성 및 반환
    return UpdateServiceAgreementTermsResponse.builder()
        .currentStep(currentStep)
        .onboardingStatus(member.getOnboardingStatus().name())
        .member(MemberDto.entityToDto(member))
        .build();
  }

  /**
   * 이름 업데이트
   *
   * @param request 이름 업데이트 요청
   * @return 온보딩 응답 (현재 단계, 상태, 회원 정보)
   */
  @Transactional
  public OnboardingResponse updateName(UpdateNameRequest request) {
    UUID memberId = request.getMemberId();

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    if(member.getOnboardingStep() != OnboardingStep.NAME && member.getOnboardingStep() != OnboardingStep.COMPLETED) {
      log.warn("[Onboarding] 현재 온보딩 단계가 닉네임 설정이 아님 - memberId={}, currentStep={}",
          memberId, member.getOnboardingStep());
      throw new CustomException(ErrorCode.INVALID_ONBOARDING_STEP);
    }

    if (request.getName().length() < 2 || request.getName().length() > 50) {
      log.warn("[Onboarding] 닉네임은 2자 이상 50자 이하 - memberName={}", request.getName());
      throw new CustomException(ErrorCode.INVALID_NAME_LENGTH);
    }

    boolean isDuplicateName = memberRepository.existsByNameAndIdNot(request.getName(), memberId);
    if (isDuplicateName) {
      log.warn("[Onboarding] 이미 사용 중인 닉네임 - memberName={}", request.getName());
      throw new CustomException(ErrorCode.NAME_ALREADY_EXISTS);
    }

    member.setName(request.getName());

    // 온보딩 상태를 IN_PROGRESS로 변경
    if (member.getOnboardingStatus() == MemberOnboardingStatus.NOT_STARTED) {
      member.setOnboardingStatus(MemberOnboardingStatus.IN_PROGRESS);
    }

    // 온보딩 단계 계산 및 저장
    OnboardingStep currentStep = calculateAndSaveOnboardingStep(member);

    log.info("[Onboarding] 이름 업데이트 완료 - memberId={}, name={}, currentStep={}",
        memberId, request.getName(), currentStep);

    return OnboardingResponse.builder()
        .currentStep(currentStep)
        .onboardingStatus(member.getOnboardingStatus().name())
        .member(MemberDto.entityToDto(member))
        .build();
  }

  /**
   * 생년월일 업데이트
   *
   * @param request 생년월일 업데이트 요청
   * @return 온보딩 응답 (현재 단계, 상태, 회원 정보)
   */
  @Transactional
  public OnboardingResponse updateBirthDate(UpdateBirthDateRequest request) {
    UUID memberId = request.getMemberId();

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    if(member.getOnboardingStep() != OnboardingStep.BIRTH_DATE && member.getOnboardingStep() != OnboardingStep.COMPLETED) {
      log.warn("[Onboarding] 현재 온보딩 단계가 생일 설정이 아님 - memberId={}, currentStep={}",
          memberId, member.getOnboardingStep());
      throw new CustomException(ErrorCode.INVALID_ONBOARDING_STEP);
    }

    // 유효하지 않은 생년월일 처리
    if (request.getBirthDate() == null || request.getBirthDate().isAfter(LocalDate.now())) {
      log.warn("[Onboarding] 유효하지 않은 생년월일 형식 - memberBirthDate={}", request.getBirthDate());
      throw new CustomException(ErrorCode.INVALID_BIRTH_DATE);  // 유효하지 않은 생년월일 에러 처리
    }
    // 만 14세 이상부터 가능
    LocalDate minAllowedBirthDate = today.minusYears(14);
    if (request.getBirthDate().isAfter(minAllowedBirthDate)) {
      log.warn("[Onboarding] 만 14세 미만 가입 불가 - memberBirthDate={}", request.getBirthDate());
      throw new CustomException(ErrorCode.AGE_RESTRICTION_UNDER_14);
    }

    member.setBirthDate(request.getBirthDate());

    // 온보딩 상태를 IN_PROGRESS로 변경
    if (member.getOnboardingStatus() == MemberOnboardingStatus.NOT_STARTED) {
      member.setOnboardingStatus(MemberOnboardingStatus.IN_PROGRESS);
    }

    // 온보딩 단계 계산 및 저장
    OnboardingStep currentStep = calculateAndSaveOnboardingStep(member);

    log.info("[Onboarding] 생년월일 업데이트 완료 - memberId={}, birthDate={}, currentStep={}",
        memberId, request.getBirthDate(), currentStep);

    return OnboardingResponse.builder()
        .currentStep(currentStep)
        .onboardingStatus(member.getOnboardingStatus().name())
        .member(MemberDto.entityToDto(member))
        .build();
  }

  /**
   * 성별 업데이트
   *
   * @param request 성별 업데이트 요청
   * @return 온보딩 응답 (현재 단계, 상태, 회원 정보)
   */
  @Transactional
  public OnboardingResponse updateGender(UpdateGenderRequest request) {
    UUID memberId = request.getMemberId();

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    if(member.getOnboardingStep() != OnboardingStep.GENDER && member.getOnboardingStep() != OnboardingStep.COMPLETED) {
      log.warn("[Onboarding] 현재 온보딩 단계가 성별 설정이 아님 - memberId={}, currentStep={}",
          memberId, member.getOnboardingStep());
      throw new CustomException(ErrorCode.INVALID_ONBOARDING_STEP);
    }

    // 유효한 성별인지 확인 (MALE, FEMALE, NOT_SELECTED만 허용)
    if (request.getGender() != MemberGender.MALE && request.getGender() != MemberGender.FEMALE && request.getGender() != MemberGender.NOT_SELECTED) {
      log.warn("[Onboarding] 유효하지 않은 성별 값 - memberGender={}", request.getGender());
      throw new CustomException(ErrorCode.INVALID_GENDER);  // 유효하지 않은 성별 오류 처리
    }

    member.setGender(request.getGender());

    // 온보딩 상태를 IN_PROGRESS로 변경
    if (member.getOnboardingStatus() == MemberOnboardingStatus.NOT_STARTED) {
      member.setOnboardingStatus(MemberOnboardingStatus.IN_PROGRESS);
    }

    // 온보딩 단계 계산 및 저장
    OnboardingStep currentStep = calculateAndSaveOnboardingStep(member);

    log.info("[Onboarding] 성별 업데이트 완료 - memberId={}, gender={}, currentStep={}",
        memberId, request.getGender(), currentStep);

    return OnboardingResponse.builder()
        .currentStep(currentStep)
        .onboardingStatus(member.getOnboardingStatus().name())
        .member(MemberDto.entityToDto(member))
        .build();
  }

  /**
   * 관심사 업데이트 (전체 교체)
   *
   * @param request 관심사 업데이트 요청
   * @return 온보딩 응답 (현재 단계, 상태, 회원 정보)
   */
  @Transactional
  public OnboardingResponse updateInterests(UpdateInterestsRequest request) {
    UUID memberId = request.getMemberId();

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    if(member.getOnboardingStep() != OnboardingStep.INTERESTS && member.getOnboardingStep() != OnboardingStep.COMPLETED) {
      log.warn("[Onboarding] 현재 온보딩 단계가 관심사 설정이 아님 - memberId={}, currentStep={}",
          memberId, member.getOnboardingStep());
      throw new CustomException(ErrorCode.INVALID_ONBOARDING_STEP);
    }

    List<UUID> interestIds = request.getInterestIds();
    Set<UUID> uniqueInterestIds = new HashSet<>(interestIds);
    if (uniqueInterestIds.size() != interestIds.size()) {
      log.warn("[Onboarding] 중복된 관심사 ID 포함 - memberId={}, interestIds={}",
          memberId, interestIds);
      throw new CustomException(ErrorCode.DUPLICATE_INTEREST_IDS);
    }

    // 관심사 ID 유효성 검증
    List<Interest> interests = interestRepository.findAllById(request.getInterestIds());
    if (interests.size() != request.getInterestIds().size()) {
      log.warn("[Onboarding] 유효하지 않은 관심사 ID 포함 - memberId={}", memberId);
      throw new CustomException(ErrorCode.INTEREST_NOT_FOUND);
    }

    // 관심사 최소 3개 이상 선택 검증
    if (request.getInterestIds().size() < 3) {
      log.warn("[Onboarding] 최소 3개 이상의 관심사 선택 필요 - memberId={}, selectedCount={}",
          memberId, request.getInterestIds().size());
      throw new CustomException(ErrorCode.INSUFFICIENT_INTEREST_SELECTION);
    }

    // 기존 관심사 삭제
    memberInterestRepository.deleteByMemberId(memberId);

    // 새 관심사 추가
    List<MemberInterest> memberInterests = request.getInterestIds().stream()
        .map(interestId -> MemberInterest.builder()
            .member(member)
            .interest(interests.stream()
                .filter(i -> i.getId().equals(interestId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INTEREST_NOT_FOUND)))
            .build())
        .collect(Collectors.toList());

    memberInterestRepository.saveAll(memberInterests);

    // 온보딩 상태를 IN_PROGRESS로 변경
    if (member.getOnboardingStatus() == MemberOnboardingStatus.NOT_STARTED) {
      member.setOnboardingStatus(MemberOnboardingStatus.IN_PROGRESS);
    }

    // 온보딩 단계 계산 및 저장
    OnboardingStep currentStep = calculateAndSaveOnboardingStep(member);

    log.info("[Onboarding] 관심사 업데이트 완료 - memberId={}, interestCount={}, currentStep={}",
        memberId, memberInterests.size(), currentStep);

    return OnboardingResponse.builder()
        .currentStep(currentStep)
        .onboardingStatus(member.getOnboardingStatus().name())
        .member(MemberDto.entityToDto(member))
        .build();
  }

  @Transactional
  public MemberDto updateProfile(UUID memberId, ProfileUpdateRequest request) {
    // 회원 존재 여부 확인
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    log.info("[Onboarding] 프로필 업데이트 진행(jwt 토큰으로 id 파싱 완료) - memberId={}, memberName={}", memberId, member.getName());

    if(member.getOnboardingStep() != OnboardingStep.COMPLETED) {
      log.warn("[Onboarding] 현재 온보딩 단계가 완료되지 않았음 - memberId={}, currentStep={}",
          memberId, member.getOnboardingStep());
      throw new CustomException(ErrorCode.INVALID_ONBOARDING_STEP);
    }

    // 유효한 정보들인지 확인
    // 이름 검증 (2자 이상 50자 이하, 중복 체크)
    if (request.getName().length() < 2 || request.getName().length() > 50) {
      log.warn("[Onboarding] 닉네임은 2자 이상 50자 이하 - memberName={}", request.getName());
      throw new CustomException(ErrorCode.INVALID_NAME_LENGTH);
    }
    boolean isDuplicateName = memberRepository.existsByNameAndIdNot(request.getName(), memberId);
    if (isDuplicateName) {
      log.warn("[Onboarding] 이미 사용 중인 닉네임 - memberName={}", request.getName());
      throw new CustomException(ErrorCode.NAME_ALREADY_EXISTS);
    }
    // 생년월일 검증
    if (request.getBirthDate() == null || request.getBirthDate().isAfter(LocalDate.now())) {
      log.warn("[Onboarding] 유효하지 않은 생년월일 형식 - memberBirthDate={}", request.getBirthDate());
      throw new CustomException(ErrorCode.INVALID_BIRTH_DATE);  // 유효하지 않은 생년월일 에러 처리
    }
    // 성별 검증 (MALE, FEMALE, NOT_SELECTED만 허용)
    if (request.getGender() != MemberGender.MALE && request.getGender() != MemberGender.FEMALE && request.getGender() != MemberGender.NOT_SELECTED) {
      log.warn("[Onboarding] 유효하지 않은 성별 값 - memberGender={}", request.getGender());
      throw new CustomException(ErrorCode.INVALID_GENDER);  // 유효하지 않은 성별 오류 처리
    }
    // 만 14세 이상부터 가능
    LocalDate minAllowedBirthDate = today.minusYears(14);
    if (request.getBirthDate().isAfter(minAllowedBirthDate)) {
      log.warn("[Onboarding] 만 14세 미만 가입 불가 - memberBirthDate={}", request.getBirthDate());
      throw new CustomException(ErrorCode.AGE_RESTRICTION_UNDER_14);
    }

    // 정보 업데이트
    member.setName(request.getName());
    member.setGender(request.getGender());
    member.setBirthDate(request.getBirthDate());

    updateInterests(
        UpdateInterestsRequest.builder()
            .memberId(memberId)
            .interestIds(request.getInterestIds())
            .build()
    );

    // updateInterests가 이미 member를 저장하므로, 최신 상태를 다시 조회하여 반환
    member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    log.info("[Onboarding] 프로필 업데이트 완료 - memberId={}, name={}, gender={}, birthDate={}",
        memberId, member.getName(), member.getGender(), member.getBirthDate());
    return MemberDto.builder()
        .id(member.getId())
        .email(member.getEmail())
        .name(member.getName())
        .gender(member.getGender())
        .birthDate(member.getBirthDate())
        .onboardingStatus(member.getOnboardingStatus().name())
        .isServiceTermsAndPrivacyAgreed(member.getIsServiceTermsAndPrivacyAgreed())
        .isMarketingAgreed(member.getIsMarketingAgreed())
        .build();
  }

  /**
   * 모든 회원 조회
   *
   * @return 회원 목록
   */
  public List<MemberDto> getAllMembers() {
    List<Member> entities = memberRepository.findAll();
    return entities.stream()
        .map(MemberDto::entityToDto)
        .collect(Collectors.toList());
  }

  /**
   * 회원 ID로 조회
   *
   * @param memberId 회원 ID
   * @return 회원 데이터
   */
  public MemberDto getMemberById(UUID memberId) {
    Member entity = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCodeBuilder.businessStatus(Subject.MEMBER, BusinessStatus.NOT_FOUND, HttpStatus.NOT_FOUND)));

    // 탈퇴한 회원인지 확인
    if (entity.isDeleted()) {
      log.warn("[Member] 탈퇴한 회원 조회 시도 - memberId={}", memberId);
      throw new CustomException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
    }

    return MemberDto.entityToDto(entity);
  }

  /**
   * 이메일로 회원 조회
   *
   * @param email 이메일
   * @return 회원 데이터
   */
  public MemberDto getMemberByEmail(String email) {
    Member entity = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCodeBuilder.businessStatus(Subject.MEMBER, BusinessStatus.NOT_FOUND, HttpStatus.NOT_FOUND)));

    // 탈퇴한 회원인지 확인
    if (entity.isDeleted()) {
      log.warn("[Member] 탈퇴한 회원 조회 시도 - email={}", email);
      throw new CustomException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
    }

    return MemberDto.entityToDto(entity);
  }

  /**
   * 회원 ID로 관심사 조회
   *
   *  @param memberId 회원 ID
   * @return 회원 관심사 목록
   */
  public List<InterestDto> getInterestsByMemberId(UUID memberId) {
    // 멤버가 존재하는지 확인
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 탈퇴한 회원인지 확인
    if (member.isDeleted()) {
      log.warn("[Member] 탈퇴한 회원의 관심사 조회 시도 - memberId={}", memberId);
      throw new CustomException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
    }

    if(member.getOnboardingStep() != OnboardingStep.COMPLETED) {
      log.warn("[Onboarding] 현재 온보딩 단계가 완료되지 않았음 - memberId={}, currentStep={}",
          memberId, member.getOnboardingStep());
      throw new CustomException(ErrorCode.INVALID_ONBOARDING_STEP);
    }

    // 멤버의 관심사 리스트 반환
    List<MemberInterest> memberInterests = memberInterestRepository.findByMemberId(memberId);

    // 관심사 ID를 통해 관심사 리스트 반환
    List<InterestDto> interests = memberInterests.stream()
        .map(memberInterest -> new InterestDto(
            memberInterest.getInterest().getId(),
            memberInterest.getInterest().getName()))
        .collect(Collectors.toList());

    return interests;
  }

  /**
   * 닉네임 사용 가능 여부 확인
   *
   * @param name 확인할 닉네임
   * @return 사용 가능 여부 (true: 사용 가능, false: 중복)
   */
  public CheckNameResponse checkNameAvailability(String name) {
    // 닉네임 길이 검증 (2자 이상 50자 이하)
    if (name == null || name.trim().isEmpty()) {
      log.warn("[Member] 닉네임이 비어있음");
      throw new CustomException(ErrorCode.INVALID_NAME_LENGTH);
    }

    if (name.length() < 2 || name.length() > 50) {
      log.warn("[Member] 닉네임 길이 제한 위반 - name={}", name);
      throw new CustomException(ErrorCode.INVALID_NAME_LENGTH);
    }

    // 중복 체크 (모든 회원 대상)
    boolean isDuplicate = memberRepository.existsByName(name);

    log.info("[Member] 닉네임 중복 확인 - name={}, available={}", name, !isDuplicate);

    return CheckNameResponse.builder()
        .isAvailable(!isDuplicate)
        .name(name)
        .build();
  }
}
