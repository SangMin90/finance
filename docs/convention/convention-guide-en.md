# Unified Backend Convention

## Ⅰ. Shared Principle (Core)

### Ⅰ.1 Architecture & Lifecycle

#### Ⅰ.1.1 Package Structure

The system follows a Feature-based top-level packaging. Inside each feature, we strictly adhere to the **4-level Layered Architecture** inspired by DDD.

##### Ⅰ.1.1.1 Domain Package Structure

| Package            | Layer              | Responsibility                                                                     |
| ------------------ | ------------------ | ---------------------------------------------------------------------------------- |
| **presentation**   | Controller         | Handles HTTP requests/responses and user commands.                                 |
| **application**    | Service, DTO       | Orchestrates business flows and manages transactions.                              |
| **domain**         | Entity, Repository | Contains business logic and rules. Must remain **POJO** (No outward dependencies). |
| **infrastructure** | Tech Details       | Technical implementations (e.g., External Clients, Message Queues).                |

##### Ⅰ.1.1.2 Global Package

- Houses cross-cutting concerns: `common`, `config`, `exception`, `filter`, `interceptor`, `resolver`, `util`.
- **Constraint**: Domain-specific logic must reside within its respective domain package, not in `global`.

#### Ⅰ.1.2 Development Principles

- **Directional Dependency**: `Presentation -> Application -> Domain`. Reverse dependencies are prohibited.
- **Pure Domain**: Domain objects must remain untainted by external frameworks or technical details.
- **No Direct Access**: Controllers are strictly forbidden from directly accessing Repositories.
- **Service Isolation**: Prevent direct coupling between Services in the Application Layer. Use **Domain Services** or shared **Components** to avoid circular dependencies.
- **CQS (Command Query Separation)**:
  - **Commands (CUD)**: Should not return values (void).
  - **Exception**: Create operations may return the **Identifier (ID)** for client-side routing.
  - **Queries (R)**: Should not modify any state.

### Ⅰ.2 Implementation Detail

#### Ⅰ.2.1 Naming Convention

##### Ⅰ.2.1.1 Method Naming by Layer

- **Controller**: `register`, `modify`, `remove` (HTTP semantics).
- **Service**: `join`, `changePassword`, `withdraw` (Business semantics).
- **Repository**: `save`, `findById`, `exists` (Data-centric).
- **Find vs. Get**:
  - `find...()`: Returns `Optional`. Result may be empty.
  - `get...()`: Returns the object. Throws `Exception` immediately if not found.
  - **Collections**: Return an empty collection instead of `null` if no data exists.

##### Ⅰ.2.1.2 Object Naming

- **Entity/VO**: Pure nouns only (e.g., Member, Address). Avoid suffixes like `...Entity`.
- **Boundary Objects**:
  - `Request/Response`: For API entry/exit (e.g., `PostCreateRequest`).
  - `Command/Query`: For Service layer inputs (e.g., `RegisterMemberCommand`).

#### Ⅰ.2.2 Exception Strategy

- **Standard First**: Prioritize Java Standard Exceptions (e.g., `IllegalArgumentException`).
- **Custom Exception Criteria**:
  - When specific business meaning is required.
  - When custom error codes or extra metadata are needed for the client.
  - When different logging/alerting strategies (e.g., Slack alerts) are required.

#### Ⅰ.2.3 Coding Style & Annotations

- **Annotation Ordering**: Sort annotations by character length in ascending order.
- **Equality**: Manually override `equals()` and `hashCode()`.
- **Parameter Immutability**: Parameter re-assignment is strictly forbidden. Use final-like behavior (Enforced via `ParameterAssignment`).

### Ⅰ.3 Quality Assurance

#### Ⅰ.3.1 Unit Testing Strategy

- **Domain Layer**: Follow the **Classical School** (State-based, avoid Mocks).
- **Application/Presentation**: Follow the **London School** (Interaction-based with Mocks) or use **E2E (RestAssured)**.

#### Ⅰ.3.2 E2E Testing (RestAssured)

- **Data Integrity**: Do not use @DirtiesContext. Perform manual data cleanup (Truncate/Delete) per test.
- **Scope**: Focus on Happy Paths to balance test speed and coverage.
- **Verification**:
  - Validate **Status Code** and **Core Payload** (e.g., Created ID, Mandatory fields).
  - Ensure CUD operations actually persist or reflect changes in the response. |

## Ⅱ. Java Implementation

### Ⅱ.1 Java 21+ Modernization & Standards

- **Record Patterns**: Use Record Patterns for deconstruction in `instanceof` (e.g., `if (obj instanceof Point(int x, int y))`) instead of manual casting.
- **Switch Pattern Matching**: Prefer `switch` expressions with pattern matching over multiple `if-else instanceof` chains.
- **Sequenced Collections**: Use `getFirst()`, `getLast()`, `addFirst()`, and `addLast()` for explicit intent.
- **Virtual Threads Awareness**: Flag `ThreadLocal` or heavy `synchronized` blocks to avoid **Thread Pinning**; suggest `ReentrantLock` for high concurrency.
- **Simplified Main**: Use unnamed classes and instance main methods for simple scripts where applicable.
- **Library Policy**: Strictly prefer **Java 21 Standard API** over Guava or Apache Commons.
- **Null Handling**: Remember that pattern matching `instanceof` safely returns `false` for `null`.

## Ⅲ. Kotlin Implementation

### Ⅲ.1 Language correctness:

- Null safety: Ensure proper use of nullable types, avoid unnecessary null assertions (!!)
- Immutability: Prefer val over var, use immutable collections where possible
- Data class usage: Use data classes for DTOs and value objects
- Sealed class usage: Use sealed classes for representing restricted hierarchies

### Ⅲ.2 Code quality:

- Complexity: Flag functions with high cyclomatic complexity (>10)
- Dead code: Identify unused functions, variables, and imports
- Naming conventions: Follow Kotlin naming conventions (camelCase for functions/properties, PascalCase for classes)

### Ⅲ.3 Coroutines and concurrency:

- Structured concurrency: Ensure proper coroutine scope usage
- Dispatcher misuse: Check for correct dispatcher usage (IO for blocking, Default for CPU-intensive)

### Ⅲ.4 Performance:

- Avoid unnecessary allocations in hot paths
- Collection usage: Use appropriate collection types and operations (prefer sequences for large collections with multiple operations)

### Ⅲ.5 API and design:

- Visibility modifiers: Use appropriate visibility (prefer private/internal over public)
- Unnecessary public API: Flag overly exposed APIs

### Ⅲ.6 Error handling:

- Proper exception handling, avoid swallowing exceptions
- Result usage: Consider using Result type for operations that can fail

### Ⅲ.7 Security:

- No hardcoded secrets or credentials
- Use SecureRandom instead of Random for security-sensitive operations
- Avoid unsafe deserialization
