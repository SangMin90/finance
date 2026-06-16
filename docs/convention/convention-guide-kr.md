# 통합 백엔드 컨벤션 (Unified Backend Convention)

## Ⅰ. 공통 원칙 (Core Principles)

### Ⅰ.1 아키텍처 및 생명주기 (Architecture & Lifecycle)

#### Ⅰ.1.1 패키지 구조

시스템은 **Feature** 기반의 패키지 구조를 따른다. 각 Feature 내부에서는 DDD에서 영감을 받은 `4-level Layered Architecture`를 유지한다.

##### Ⅰ.1.1.1 도메인 패키지 구조

| 패키지         | 계층               | 책임 및 역할                                             |
| -------------- | ------------------ | -------------------------------------------------------- |
| presentation   | Controller         | HTTP 요청/응답 처리 및 사용자 명령 수신                  |
| application    | Service, DTO       | 비즈니스 흐름 제어 및 트랜잭션 관리                      |
| domain         | Entity, Repository | 비즈니스 로직 및 규칙 포함. POJO 유지 (외부 의존성 금지) |
| infrastructure | 기술적 세부사항    | 외부 클라이언트, 메시지 큐 등 기술적 구현체              |

##### Ⅰ.1.1.2 글로벌 패키지

- 공통 관심사 분리: `common`, `config`, `exception`, `filter`, `interceptor`, `resolver`, `util`.
- 제약 사항: 특정 도메인에 종속된 로직은 `global`이 아닌 해당 Domain 패키지에 위치해야 한다.

#### Ⅰ.1.2 개발 원칙

- Layer 간 의존성은 단방향
  - Presentation -> Application -> Domain 순으로만 의존하며, 역방향 의존성은 절대 금지한다.
- Domain은 항상 순수하게 유지
  - 외부 프레임워크나 기술적인 요소에 의존하지 않는다.
- Controller -> Repository 직접 호출 금지
  - 반드시 Service를 통해 접근한다.
- Service 간 직접적인 의존 최소화
  - 순환 의존성을 방지하기 위해 Domain Service나 공통 컴포넌트를 활용한다.
- CQS (Command Query Separation) 적용
  - Commands (CUD): 상태를 변경하며, 값을 반환하지 않는다(void).
  - 예외: 생성(Create) 작업은 **식별자(ID)**를 반환 허용
  - Queries (R): 상태 변경 없이, 데이터만 반환한다.

### Ⅰ.2 구현 규칙 (Implementation Rules)

#### Ⅰ.2.1 네이밍 규칙 (Naming Convention)

##### Ⅰ.2.1.1 계층별 메서드 네이밍

- Controller: `register`, `modify`, `remove` (HTTP 의미 중심).
- Service: `join`, `changePassword`, `withdraw` (비즈니스 용어 중심).
- Repository: `save`, `findById`, `exists` (데이터 중심).
- find vs get:
  - `find...()`: `Optional` 반환한다 (결과가 없을 수 있음).
  - `get...()`: 객체 즉시 반환하며, 결과가 없으면 즉시 예외(Exception)룰 발생한다.
- **Collections 반환 규칙**: null 대신 항상 빈 컬렉션을 반환한다.

##### Ⅰ.2.1.2 객체 네이밍

- **Entity/VO**
  - 순수 명사만 사용한다(예: `Member`, `Address`).
  - `...Entity` suffix 사용을 금지한다.
- **Boundary Objects**:
  - `Request/Response`: API 입출력용 (예: PostCreateRequest).
  - `Command/Query`: Service 입력용 (예: RegisterMemberCommand).

#### Ⅰ.2.2 예외 처리 전략

- **표준 예외 우선 사용**
  - `IllegalArgumentException` 등 Java 표준 예외를 먼저 고려한다.
- **Custom Exception 사용 기준**
  - 비즈니스 의미를 명확히 표현해야 할 때
  - 클라이언트에 에러 코드나 메타데이터를 내려야 할 경우
  - 특정 로깅 또는 알림 정책이 필요한 경우

#### Ⅰ.2.3 코드 스타일

- Annotation 정렬
  - 길이 기준 오름차순으로 정렬한다.
- 동등성 비교
  - equals()와 hashCode()는 반드시 직접 구현한다.
- 파라미터 불변성
  - 메서드 파라미터 재할당은 엄격히 금지한다.

### Ⅰ.3 품질 보증 (Quality Assurance)

#### Ⅰ.3.1 단위 테스트 전략

- **Domain Layer**  
  **고전파 (Classical School)**를 따른다
  - 상태 기반 검증
  - Mock 사용 최소화
- **Application/Presentation**  
  **런던파 (London School)**를 따른다.
  - 협력 객체 Mocking 사용
  - 또는 E2E 테스트로 검증

#### Ⅰ.3.2 E2E 테스트 (RestAssured)

- 데이터 초기화
  - @DirtiesContext 사용을 지양한다.
  - 테스트마다 직접 truncate/delete를 수행한다.
- 테스트 범위
  - 테스트 속도와 커버리지의 균형을 위해 Happy Path에 집중한다.
- 검증 항목:
  - HTTP 상태 코드
  - 핵심 데이터(ID, 필수 필드)
  - 실제 DB 반영 여부 (CUD 검증).

## Ⅱ. 자바 구현 가이드 (Java Implementation)

### Ⅱ.1 Java 21+ 사용 기준

- Record Patterns
  - instanceof 사용 시 명시적 캐스팅 대신 구조 분해(Deconstruction)한다.
- Switch Pattern Matching
  - if-else instanceof 체인 대신 switch 표현식을 사용한다.
- Sequenced Collections
  - getFirst(), getLast() 등 의도가 명확한 API를 사용한다.
- Virtual Thread 주의사항
  - ThreadLocal이나 과도한 synchronized 블록 사용을 주의한다.
  - High Concurrency 환경에서는 `ReentrantLock`을 고려한다.
- 라이브러리 사용 기준
  - Guava나 Apache Commons보다 Java 21 표준 API 사용을 우선한다.
- Null 처리
  - pattern matching instanceof는 null-safe 하다.

## Ⅲ. 코틀린 구현 가이드 (Kotlin Implementation)

### Ⅲ.1 언어 사용 원칙

- Null Safety
  - Nullable 타입을 명확히 사용한다.
  - !! 사용을 금지한다.
- 불변성 우선
  - var보다는 val을,
  - mutable 컬렉션보다는 immutable 컬렉션을 우선 사용한다.
- Data Class 활용
  - DTO 및 VO에 활용한다.
  - 단, JPA Entity에는 사용 금지한다.
- Sealed Class
  - 제한된 타입 계층 표현 시 활용한다.

### Ⅲ.2 코드 품질

- 복잡도 관리
  - Cyclomatic Complexity > 10인 경우 함수를 분리한다.
- 네이밍
  - 코틀린 표준 준수
    - 함수/프로퍼티: camelCase
    - 클래스: PascalCase.

### Ⅲ.3 Coroutine 및 Concurrency

- Structured Concurrency
  - Coroutine Scope를 명확히 관리한다.
- Dispatcher 구분
  - 차단(Blocking) 작업은 IO,
  - CPU 작업은 Default

### Ⅲ.4 성능 및 디자인

- 가시성 제어
  - private, internal을 사용하여 불필요한 API 노출을 방지한다.
- 에러 처리
  - 예외 무시 금지
  - 필요 시 Result 타입 고려
- 보안
  - 하드코딩된 비밀번호를 금지
  - 난수는 SecureRandom을 사용
