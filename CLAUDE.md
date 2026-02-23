# MapSy-BE Project Conventions

## Project Structure
- Multi-module Gradle project (Java 21, Spring Boot 4.0)
- Modules: MS-Common, MS-Auth, MS-Member, MS-Place, MS-SNS, MS-AI, MS-Web

## Controller Pattern
- Controller는 `MS-Web` 모듈에 위치
- `XxxController`는 반드시 `XxxControllerDocs` 인터페이스를 implements
- `XxxControllerDocs`에 Swagger `@Operation`, `@ApiLog` 어노테이션 정의
- `@AuthenticationPrincipal CustomUserDetails userDetails`로 인증 처리
- `@RestController`, `@RequiredArgsConstructor`, `@Slf4j`, `@RequestMapping` 사용

## DTO Pattern (Request/Response)
- **각 API 메서드마다 별도의 Request/Response DTO를 생성**
- Request 네이밍: `{동작}{도메인}Request` (예: `CreateFolderRequest`, `UpdateFolderRequest`)
- Response 네이밍: `{동작}{도메인}Response` (예: `GetFoldersResponse`, `CreateFolderResponse`)
- Response는 DB Entity 필드를 그대로 DTO로 변환하여 반환 (불필요한 가공 없이)
- DTO는 각 도메인 모듈의 `dto` 패키지에 위치 (예: `MS-Place/...place/dto/`)
- Lombok: `@Getter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Swagger: `@Schema` 어노테이션으로 필드 설명
- 엔티티 → DTO 변환은 `from()` 정적 팩토리 메서드 사용

## Service Pattern
- Service는 각 도메인 모듈에 위치 (예: `MS-Place/...place/service/`)
- `@Service`, `@RequiredArgsConstructor`, `@Slf4j`
- 클래스 레벨: `@Transactional(readOnly = true)` 기본
- 변경 메서드만: `@Transactional`
- Member 파라미터와 UUID 파라미터 오버로드 메서드 패턴

## Repository Pattern
- JPA Repository, `extends JpaRepository<Entity, UUID>`
- Soft Delete 고려: `deletedAtIsNull` 조건 항상 포함
- N+1 방지: `@Query` + `JOIN FETCH` 사용
- `@Repository` 어노테이션 사용

## Entity Pattern
- `SoftDeletableBaseEntity` 상속 (isDeleted, deletedAt, deletedBy)
- UUID PK: `@GeneratedValue(strategy = GenerationType.UUID)`
- Lombok: `@Entity`, `@Builder`, `@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@AllArgsConstructor(access = PRIVATE)`

## Error Handling
- `ErrorCode` enum에 에러 코드 정의 (HttpStatus + message)
- `CustomException(ErrorCode)` throw
- `ErrorResponse` 표준 응답 형식

## Commit Message Convention
- `{이슈 제목} : {type} : {변경 사항 설명} {이슈 URL}`
- type: feat, fix, refactor, docs, chore, test
