# AGENTS: Guidance for AI coding agents working on Cabbie

Purpose: give AI agents the minimal, actionable knowledge to be productive immediately in this Spring Boot codebase (notifications, auth, build/run, and conventions).

Architecture (big picture)
- Spring Boot monolith with layered packages: `controller`, `service`, `repository`, `model`, `dto`, `configuration`.
- Real-time notification stack:
  - Kafka topics: `ride-notifications` and `ride-request` configured in `configuration/KafkaTopicConfig.java`.
  - Kafka producer: `service/KafkaProducerService.java` (methods `sendRideNotification`, `sendRideRequestNotification`).
  - Kafka consumer → WebSocket dispatcher: `service/NotificationDispatchConsumerService.java` listens on Kafka and forwards to WebSocket via `SimpMessagingTemplate` to `/user/queue/notifications`.
  - WebSocket STOMP endpoint: `/ws` (see `configuration/WebSocketConfig.java`) — SockJS enabled, broker prefix `/queue`, app prefix `/app`, user destination prefix `/user`.

Key integration and security notes
- Kafka bootstrap is hardcoded to `localhost:9092` in `KafkaTopicConfig.java` (agents should assume local Kafka for developer runs). Topics are auto-created by beans `new NewTopic(...)`.
- WebSocket connections must carry JWT in the CONNECT header: the project uses `JwtChannelInterceptorForWebSocketSecurity.java` which expects an `Authorization: Bearer <token>` native header on the STOMP CONNECT frame.
- Security config (`configuration/SecurityConfig.java`) permits `/ws/**` but still enforces JWT for WebSocket connections via the channel interceptor.

Developer workflows (commands)
- Build: use the maven wrapper to avoid local Maven version mismatches:
  - Windows (PowerShell): `./mvnw.cmd -DskipTests=false package`
  - Run: `./mvnw.cmd spring-boot:run` or run from your IDE.
  - Tests: `./mvnw.cmd test`
- Local runtime requirements for notification features:
  - Kafka broker available at `localhost:9092` (used by `KafkaTopicConfig`).
  - If you don't run Kafka locally, tests or runtime that expect Kafka will fail — spin up a single-node Kafka (docker-compose or Confluent quickstart).

Project-specific conventions and patterns
- DTOs use Lombok builders and Jackson-friendly types (e.g., `com.app.cabbie.dto.KafkaEventDTO` is serialized by KafkaTemplate with `JacksonJsonSerializer`). Inspect compiled DTOs under `src/main/java/com/app/cabbie/dto`.
- Repositories expose Spring Data methods like `findByPassengerId` and `findByDriverId` returning Optionals or lists — follow their naming conventions when adding new queries.
- Transactions use `jakarta.transaction.Transactional` on service methods.
- Error handling is often simple: services throw `RuntimeException` on missing resources — agents should preserve semantics when modifying code and prefer explicit exceptions only when replacing existing behavior.

How notifications flow (concrete example)
1. `RideService.requestRide(...)` builds `KafkaEventDTO` and calls `producerService.sendRideRequestNotification(event)` (see `service/RideService.java`).
2. `NotificationDispatchConsumerService` listens to `ride-request`, assigns driver, creates `newRideEvent` and forwards via `kafkaTemplate.send("ride-notifications", newRideEvent)`.
3. `NotificationDispatchConsumerService` and the other listener on `ride-notifications` call `messagingTemplate.convertAndSendToUser(email, "/queue/notifications", dto)` — client-side should subscribe to `/user/queue/notifications`.

Small code examples agents can use (copy/paste)
- KafkaEventDTO example (fields used in code):
```
KafkaEventDTO.builder()
  .userId(123L)
  .userEmail("alice@example.com")
  .rideId(456L)
  .title("Ride Requested!")
  .message("Your ride has been requested...")
  .build();
```
- Minimal JS STOMP + SockJS client (connect with JWT):
```
const socket = new SockJS('http://localhost:8080/ws');
const stomp = Stomp.over(() => socket);
stomp.connect({'Authorization': 'Bearer ' + token}, function() {
  stomp.subscribe('/user/queue/notifications', msg => console.log('notif', msg.body));
});
```

Files to inspect first (high signal)
- `src/main/java/com/app/cabbie/configuration/KafkaTopicConfig.java` — producer/consumer config and topic names
- `src/main/java/com/app/cabbie/service/KafkaProducerService.java` — where producers are called
- `src/main/java/com/app/cabbie/service/NotificationDispatchConsumerService.java` — Kafka listeners → websocket dispatch
- `src/main/java/com/app/cabbie/configuration/WebSocketConfig.java` — endpoint, prefixes, SockJS
- `src/main/java/com/app/cabbie/configuration/JwtChannelInterceptorForWebSocketSecurity.java` — how WebSocket auth is derived from JWT
- `src/main/resources/application.properties` — environment-sensitive configuration

Rules for automated agents (short)
- Prefer non-invasive changes: add new services or tests in new files; don't refactor multiple modules in one commit.
- Preserve existing exception types and method contracts (especially `@Transactional` boundaries).
- When adding runtime integrations (Kafka, WebSocket), update `AGENTS.md` and `README.md` with any new run requirements.

Where to look for tests and how to run them
- Unit tests live under `src/test/java/com/app/cabbie`.
- Run with `./mvnw.cmd test` (Windows) — CI may run plain `mvn`.

Contact points for follow-ups
- If you need to change runtime configs: `src/main/resources/application.properties` and `configuration/KafkaTopicConfig.java` are the first places to update.

End of AGENTS.md

