# Repozitoriya inventarı

> İş faylı. Kitabın yazılması üçün toplanmış faktiki məlumat. Mənbə: repozitoriyanın `1500818` commit-i
> (`add complete insurance campaign microservices platform`, 2026-10-09). Bütün rəqəmlər faylların özündən
> çıxarılıb.

## 1. Ümumi göstəricilər

| Göstərici | Dəyər |
|---|---|
| Git commit | `1500818b05695b4d351c5c054260dbaef29989b8` (tək commit) |
| Faylların sayı (`.git` xaric) | 196 |
| Maven modulları | 10 (`pom.xml` `<modules>`) |
| Main Java faylları / sətir | 99 fayl / 6 228 sətir |
| Test Java faylları / sətir | 24 fayl / 3 587 sətir |
| Test metodları (`@Test`) | 93 |
| Liquibase changelog | 2 (`auth-service`, `campaign-service`) |
| `.proto` faylı | 1 (`grpc-contract/src/main/proto/campaign.proto`) |
| Docker Compose faylları | 2 (`docker-compose.yml`, `docker-compose-local.yml`) |
| GitHub Actions workflow | 2 (`ci-cd.yml`, `mark-done-on-merge.yml`) |

## 2. Versiyalar (kök `pom.xml` və asılılıq ağacı)

| Komponent | Versiya | Mənbə |
|---|---|---|
| Java | 25 (`maven.compiler.release`) | `pom.xml:28-29` |
| Spring Boot | 4.1.0 | `pom.xml:30` |
| Spring Cloud | 2025.1.2 (Gateway 5.0.2, Netflix Eureka 5.0.2) | `pom.xml:31`, dependency:tree |
| Spring Framework | 7.0.8 | dependency:tree |
| Spring Security | 7.1.0 | dependency:tree |
| Hibernate ORM | 7.4.1.Final | dependency:tree |
| Liquibase | 5.0.3 | dependency:tree |
| Spring Kafka / kafka-clients | 4.1.0 / 4.2.1 | dependency:tree |
| Spring Data Elasticsearch / elasticsearch-java | 6.1.0 / 9.4.2 | dependency:tree |
| Spring Data Redis / Lettuce | 4.1.0 / 7.5.2 | dependency:tree |
| gRPC Java | 1.80.0 (BOM) | `pom.xml:35`, `pom.xml:85-91` |
| Protobuf | 4.34.2 | `pom.xml:36` |
| net.devh grpc-spring-boot-starter | 3.1.0.RELEASE | `pom.xml:37` |
| JJWT | 0.12.6 | `pom.xml:34` |
| springdoc-openapi | 2.8.13 | `pom.xml:32` |
| Testcontainers | 1.21.3 | `pom.xml:33` |
| Resilience4j | 2.4.0 (`resilience4j-spring-boot4`) | `auth-service/pom.xml:16` |
| MapStruct / Lombok | 1.5.5.Final / Boot BOM | `auth-service/pom.xml:14` |
| Jackson | 3.1.4 (`tools.jackson`) + 2.21.4 (`com.fasterxml`) | dependency:tree |
| JaCoCo | 0.8.14 | `pom.xml:39` |

Asılılıq ağacı bu iş zamanı `mvn dependency:tree` ilə hər modul üçün ayrıca çıxarılıb (Java 21 mühitində yalnız
POM həlli aparılıb; layihə Java 25 tələb etdiyi üçün kompilyasiya edilməyib).

## 3. Modullar

| Modul | Növ | Main Java | Test Java | Əsas rol |
|---|---|---:|---:|---|
| `common` | jar (kitabxana) | 14 / 835 sətir | — | Ortaq API zərfləri, istisnalar, Kafka producer konfiqurasiyası, yavaş sorğu filtri, observation |
| `security` | jar (kitabxana) | 5 / 593 | — | Servlet əsaslı JWT doğrulama, `SecurityFilterChain`, RSA açar oxuma |
| `grpc-contract` | jar (kitabxana) | 0 (yalnız `.proto`) | — | Protobuf/gRPC kodunun generasiyası |
| `discovery-server` | Boot tətbiqi | 2 / 244 | — | Eureka Server (8761) |
| `config-server` | Boot tətbiqi | 2 / 247 | — | Spring Cloud Config Server, `native` profil (8888) |
| `api-gateway` | Boot tətbiqi (WebFlux) | 10 / 1158 | — | Spring Cloud Gateway, JWT yoxlama, Redis rate limit, OpenAPI aqreqasiyası (8080) |
| `auth-service` | Boot tətbiqi (MVC) | 32 / 1809 | 7 / 1738 | İstifadəçilər, login, refresh/reset token, JWT imzalama, audit (8081) |
| `campaign-service` | Boot tətbiqi (MVC + gRPC) | 17 / 683 | 5 / 742 | Kampaniya həyat dövrü, PostgreSQL, Redis cache, Kafka producer, gRPC server (8082/9090) |
| `dashboard-service` | Boot tətbiqi (MVC) | 6 / 213 | 5 / 567 | gRPC klienti ilə statistika və tarixçə (8083) |
| `log-service` | Boot tətbiqi (MVC + Kafka) | 11 / 446 | 7 / 540 | Kafka consumer, DLQ, Elasticsearch, audit qəbulu (8084) |

## 4. Fayl siyahısı (modullar üzrə)

### common
- `api/ApiResponse.java`, `api/ApiError.java` — cavab zərfləri.
- `config/KafkaJsonConfiguration.java` — `ProducerFactory<String,Object>` + `KafkaTemplate` (JSON).
- `config/VirtualThreadConfiguration.java` — `@EnableAsync` + virtual thread executor.
- `event/CampaignStatusChangedEvent.java`, `event/SlowRequestLogEvent.java` — Kafka mesaj modelləri.
- `exception/BusinessException.java`, `DuplicateCampaignException.java`, `InvalidStatusTransitionException.java`,
  `ResourceNotFoundException.java`, `ErrorCode.java`, `GlobalExceptionHandler.java`.
- `observability/HttpServerObservationConfiguration.java` — span adları və actuator filtri.
- `web/SlowRequestLoggingFilter.java` — 5 ms-dən uzun sorğuları Kafka-ya göndərir.

### security
- `JwtProperties.java`, `JwtTokenService.java`, `JwtAuthenticationFilter.java`, `PemKeyUtils.java`,
  `ServiceSecurityConfiguration.java`; `resources/keys/public.pem`.

### grpc-contract
- `src/main/proto/campaign.proto`; `pom.xml` (protobuf-maven-plugin + os-maven-plugin + build-helper).

### discovery-server / config-server
- `DiscoveryServerApplication.java`, `observability/DiscoveryServerHttpServerObservationConfiguration.java`, `application.yml`.
- `ConfigServerApplication.java`, `observability/ConfigServerHttpServerObservationConfiguration.java`,
  `resources/application.yml`, `resources/config/application.yml` (bütün klientlər üçün paylanan konfiqurasiya).

### api-gateway
- `ApiGatewayApplication.java`
- `config/GatewayLoadBalancerConfiguration.java`, `config/GatewayRateLimiterConfiguration.java`,
  `config/GatewayHttpServerObservationConfiguration.java`
- `filter/GatewayJwtAuthenticationFilter.java`, `filter/GatewayRateLimitErrorResponseFilter.java`
- `openapi/GatewayOpenApiController.java`
- `security/GatewayJwtProperties.java`, `GatewayJwtTokenService.java`, `GatewayPemKeyUtils.java`
- `resources/application.yml`, `resources/keys/public.pem`

### auth-service
- `AuthServiceApplication.java` (`@ImportHttpServices`)
- `client/LogServiceHttpClient.java` (`@HttpExchange` + Resilience4j)
- `config/AuthConfiguration.java` (`PasswordEncoder`), `config/LogServiceHttpClientConfiguration.java` (daxili JWT)
- `controller/AuthController.java`
- `model/domain/*` (4 record), `model/dto/request/*` (6), `model/dto/response/*` (4), `model/entity/*` (3),
  `model/enums/UserRole.java`, `UserStatus.java`, `model/mapper/AuthMapper.java` (MapStruct)
- `repository/AppUserRepository.java`, `RefreshTokenRepository.java`, `PasswordResetTokenRepository.java`
- `service/AuthService.java`, `service/AuthAuditService.java`
- `util/StringUtils.java`, `util/TokenHashUtils.java`
- `resources/application.yml`, `resources/db/changelog/db.changelog-master.yml`, `resources/keys/{private,public}.pem`
- Testlər: `AuthControllerTest`, `AuthServiceTest`, `AuthAuditServiceTest`, `AuthMapperTest`, `base/*`

### campaign-service
- `CampaignServiceApplication.java` (`@EnableCaching`)
- `controller/CampaignController.java`
- `domain/Campaign.java`, `CampaignCategory.java`, `CampaignStatus.java`
- `dto/CreateCampaignRequest.java`, `CampaignResponse.java`, `CampaignPageResponse.java`,
  `CampaignHistoryResponse.java`, `CampaignStatisticsResponse.java`
- `entity/CampaignEntity.java`, `CampaignStatusHistoryEntity.java`
- `grpc/CampaignGrpcEndpoint.java`
- `mapper/CampaignMapper.java`
- `repository/CampaignRepository.java`, `CampaignStatusHistoryRepository.java`
- `service/CampaignService.java`
- `resources/application.yml`, `resources/db/changelog/db.changelog-master.yml`
- Testlər: `CampaignControllerTest`, `CampaignServiceTest`, `base/*`, `resources/keys/*`

### dashboard-service
- `DashboardServiceApplication.java`, `client/CampaignGrpcClient.java`, `controller/DashboardController.java`,
  `dto/*` (3 record), `resources/application.yml`
- Testlər: `CampaignGrpcClientTest`, `DashboardControllerTest`, `base/*`

### log-service
- `LogServiceApplication.java` (`@EnableKafka`)
- `config/KafkaDlqConfiguration.java`
- `controller/AuthAuditLogController.java`, `controller/SlowRequestLogController.java`
- `model/document/AuthAuditLogDocument.java`, `SlowRequestDocument.java`, `model/dto/request/AuthAuditLogRequest.java`
- `repository/AuthAuditLogRepository.java`, `SlowRequestLogRepository.java`
- `service/AuthAuditLogService.java`, `service/SlowRequestLogConsumer.java`
- `resources/application.yml`
- Testlər: `AuthAuditLogControllerTest`, `SlowRequestLogControllerTest`, `AuthAuditLogServiceTest`,
  `SlowRequestLogConsumerTest`, `base/*`

### İnfrastruktur və digər
- `docker-compose.yml` (bütün servislər + infrastruktur), `docker-compose-local.yml` (yalnız infrastruktur).
- Hər tətbiq modulunda çoxmərhələli `Dockerfile`.
- `observability/`: Prometheus (2 konfiqurasiya), OpenTelemetry Collector, Tempo, Loki, Promtail, Grafana
  (datasource + dashboard provisioning, `campaign-logs-traces.json`).
- `.github/workflows/ci-cd.yml` (verify → sonar → docker matrix), `mark-done-on-merge.yml` (label avtomatlaşdırması).
- `postman/Insurance Campaign Microservices.postman_collection.json`.
- `screenshots/*.PNG` (16 ekran görüntüsü; kitabda istifadə olunmur).
- `README.md` (1 868 sətir).

## 5. README ilə kod arasında fərqlər (yoxlanılıb)

| README iddiası | Faktiki vəziyyət |
|---|---|
| Qovluq strukturunda `config-repository/` və `sonar-project.properties` | Repozitoriyada mövcud deyil. Konfiqurasiya `config-server/src/main/resources/config/` daxilindədir; Sonar parametrləri kök `pom.xml`-dadır. |
| "Kafka handles asynchronous events", `campaign-events` mövzusu | `campaign-events` yalnız produce olunur (`CampaignService.java:181-191`); repozitoriyada onu oxuyan `@KafkaListener` yoxdur. |
| "Common Module ... constants, and utilities" | `common`-da ayrıca constants/utility sinfi yoxdur. |
| Docker Compose-da API Gateway OpenAPI URL-ləri | Compose `APP_OPENAPI_*` dəyişənlərini verir; `application.yml` `AUTH_SERVICE_OPENAPI_URL` və s. yer tutanlarını gözləyir. Relaxed binding sayəsində `APP_OPENAPI_AUTH_SERVICE_URL` birbaşa `app.openapi.auth-service-url` xassəsinə bağlanır; dəyərlər eyni olduğu üçün davranış dəyişmir. |
| Testlərdə "Repository tests", "Testcontainers Kafka tests where applicable" | Ayrıca repository testi yoxdur; Kafka konteyneri campaign və log testlərində başladılır, lakin heç bir test Kafka-ya mesaj göndərib-oxumur. |
