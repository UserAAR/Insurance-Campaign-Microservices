# Arxitektura xəritəsi və tapıntılar reyestri

> İş faylı. Fəsillərdə istifadə olunan bütün arxitektura iddiaları və tapıntılar buradan götürülür ki, sənəd
> boyu ardıcıllıq qorunsun. Hər tapıntının sübut mənbəyi göstərilib.

## 1. Biznes məqsədi

Sığorta şirkəti üçün **kampaniya idarəetmə platforması** (README: "Allianz Sigorta Campaign API" case study):
- admin kampaniya yaradır (kateqoriya: `TSS`, `OSS`, `LIFE`, `OTHER`);
- `LIFE` dərhal `ACTIVE`, digərləri `PENDING_APPROVAL` olur;
- admin təsdiqləyir (`PENDING_APPROVAL → ACTIVE`) və ya deaktiv edir (`ACTIVE|PENDING_APPROVAL → DEACTIVE`);
- eyni `category+title+description` təkrarı `409` ilə rədd edilir;
- hər status dəyişikliyi tarixçəyə yazılır və Kafka-ya hadisə kimi göndərilir;
- dashboard statusa görə sayları və tarixçəni gRPC ilə alır;
- autentifikasiya: qeydiyyat, login, refresh token rotasiyası, şifrə bərpası, logout;
- audit və yavaş sorğu jurnalları Elasticsearch-də saxlanılır.

Aktorlar: `ROLE_ADMIN` (yaradır/təsdiq/deaktiv/statistika/tarixçə), `ROLE_USER` (yalnız oxuma: siyahı və detal),
daxili servis (`ROLE_INTERNAL_SERVICE`, auth-service → log-service audit), operator (Grafana, Kafka UI, Eureka).

## 2. Komponentlər və məsuliyyətlər

| Komponent | Giriş nöqtələri | Data sahibliyi | Çıxış asılılıqları |
|---|---|---|---|
| api-gateway | HTTP 8080, `/openapi/{service}` | Redis (rate limit açarları) | Eureka (lb://), Redis, auth/campaign/dashboard/log |
| auth-service | HTTP 8081 `/api/v1/auth/**` | PostgreSQL `auth_db` (app_users, refresh_tokens, password_reset_tokens) | log-service (HTTP, `@HttpExchange`) |
| campaign-service | HTTP 8082 `/api/v1/campaigns/**`, gRPC 9090 | PostgreSQL `campaign_db` (campaigns, campaign_status_histories), Redis cache `campaign-statistics` | Kafka (`campaign-events`, `log-events`) |
| dashboard-service | HTTP 8083 `/api/v1/dashboard/**` | — (state yoxdur) | campaign-service gRPC (static ünvan) |
| log-service | HTTP 8084 `/api/v1/logs/**`, Kafka listener | Elasticsearch indeksləri `auth-audit-logs`, `slow-request-logs` | Kafka (`log-events`, `log-events.DLQ`) |
| discovery-server | HTTP 8761 | Eureka reyestri (yaddaşda) | — |
| config-server | HTTP 8888 | `classpath:/config/application.yml` | Eureka (qeydiyyat) |

## 3. Kommunikasiya kanalları (təsdiqlənmiş)

| # | Mənbə → Hədəf | Protokol | Sinxron? | Kod |
|---|---|---|---|---|
| C1 | Client → api-gateway | HTTP/REST | Sinxron | `api-gateway/src/main/resources/application.yml:23-67` |
| C2 | api-gateway → servislər | HTTP (lb:// Eureka + LoadBalancer) | Sinxron | eyni |
| C3 | api-gateway → Redis | Lua script (RedisRateLimiter) | Sinxron | `application.yml:20-26` |
| C4 | auth-service → log-service | HTTP POST `/api/v1/logs/auth-audit` | `@Async` (çağırana görə asinxron) | `LogServiceHttpClient.java`, `AuthAuditService.java` |
| C5 | campaign-service → Kafka `campaign-events` | Kafka | Asinxron (fire-and-forget) | `CampaignService.java:181-191` |
| C6 | campaign/log-service → Kafka `log-events` | Kafka | Asinxron | `SlowRequestLoggingFilter.java:88-120` |
| C7 | Kafka `log-events` → log-service | Kafka consumer | Asinxron | `SlowRequestLogConsumer.java:35-47` |
| C8 | log-service → Kafka `log-events.DLQ` | DeadLetterPublishingRecoverer | Asinxron | `KafkaDlqConfiguration.java:26-33` |
| C9 | dashboard-service → campaign-service | gRPC unary (blocking stub) | Sinxron | `CampaignGrpcClient.java`, `CampaignGrpcEndpoint.java` |
| C10 | campaign-service → Redis | Spring Cache (RedisCacheManager) | Sinxron | `CampaignService.java:121-131` |
| C11 | log-service → Elasticsearch | Spring Data ES (REST) | Sinxron | repository-lər |
| C12 | bütün servislər → OTel Collector | OTLP/gRPC 4317 | Asinxron (batch export) | `application.yml` `management.opentelemetry` |
| C13 | Prometheus → servislər | HTTP scrape `/actuator/prometheus` | Pull | `observability/prometheus/prometheus.yml` |
| C14 | servislər → Eureka, Config | HTTP | Startup + periodik | `application.yml` |

Qeyd: `slow-request` filtri yalnız `spring-kafka` classpath-də olan servislərdə aktivdir: campaign-service və
log-service (dependency:tree ilə təsdiq; auth və dashboard-da `spring-kafka` yoxdur, `common`-da isə optional-dır).

## 4. Tapıntılar reyestri

Kateqoriyalar: **D** = təsdiqlənmiş defekt, **R** = potensial etibarlılıq/təhlükəsizlik riski, **T** = memarlıq
kompromisi (trade-off), **M** = baxım (maintainability) təkmilləşdirməsi, **H** = yoxlanılmamış hipotez.

| ID | Kat. | Qısa təsvir | Sübut |
|---|---|---|---|
| F01 | D (təhlükəsizlik) | İstənilən anonim istifadəçi `role: ROLE_ADMIN` ilə qeydiyyatdan keçə bilir | `RegisterRequest.java:18-24`, `UserRole.java`, `AuthService.java:103-126`, `ServiceSecurityConfiguration.java:96-104` (`/api/v1/auth/**` permitAll) |
| F02 | D (təhlükəsizlik) | `forgot-password` xam reset token-i cavabda qaytarır → istifadəçi adını bilən hər kəs şifrəni dəyişə bilər | `AuthService.java:205-230`, `ForgotPasswordResponse.java` |
| F03 | R | İstifadəçi enumerasiyası: login "User is not active", forgot-password `404 User not found` | `AuthService.java:150-153`, `207-208` |
| F04 | R | gRPC server (9090) autentifikasiyasızdır, Compose host-a açır | `docker-compose.yml:182-184`; repoda `AuthenticationManager` bean yoxdur → net.devh `GrpcServerSecurityAutoConfiguration` (`@ConditionalOnBean(AuthenticationManager.class)`) işə düşmür |
| F05 | R | Gateway rate limit açarı `X-Forwarded-For`-a etibar edir → anonim sorğular saxta başlıqla limiti keçə bilər | `GatewayRateLimiterConfiguration.java:64-92` |
| F06 | R (fakt kitabxanada) | Redis əlçatmaz olduqda `RedisRateLimiter` sorğunu buraxır (fail-open) | SCG 5.0.2 `RedisRateLimiter.isAllowed` (`onErrorResume` → `[1, -1]`, catch → `new Response(true, ...)`) |
| F07 | D | `GlobalExceptionHandler` catch-all `Exception` handler-i Spring MVC-nin standart 4xx xətalarını (səhv JSON, yanlış enum, səhv UUID, 405, 404 `NoResourceFoundException`, yanlış `sort`) 500-ə çevirir və `exception.getMessage()`-i klientə qaytarır | `GlobalExceptionHandler.java:130-139` |
| F08 | R | `JwtAuthenticationFilter` etibarsız/vaxtı keçmiş token üçün `JwtException`-ı tutmur; nəticə servlet error-dispatch-dən asılıdır, test yoxdur | `JwtAuthenticationFilter.java:67-84` |
| F09 | R | Dual-write: DB tranzaksiyası daxilində `kafkaTemplate.send` — commit uğursuz olsa hadisə artıq göndərilmiş ola bilər; send uğursuz olsa hadisə itir (yalnız log) | `CampaignService.java:54-77`, `162-169`, `181-191` |
| F10 | R | Kafka əlçatmaz olduqda `KafkaTemplate.send` metadata üçün `max.block.ms` (standart 60 s) qədər bloklaya bilər: həm HTTP filtrində, həm tranzaksiya daxilində | `SlowRequestLoggingFilter.java:113-117`, `CampaignService.java:190`; Kafka producer standartı |
| F11 | R (fakt kitabxanada) | log-service `ErrorHandlingDeserializer` istifadə etmir: deserializasiya xətası DLQ-ya getmir, `DefaultErrorHandler.handleOtherException` `IllegalStateException` atır, konteyner eyni offseti təkrar poll edir (poison pill) | `log-service/application.yml:7-15`; spring-kafka 4.1.0 `DefaultErrorHandler.handleOtherException`, `KafkaMessageListenerContainer.handleConsumerException` |
| F12 | T | `campaign-events` mövzusunun repozitoriyada consumer-i yoxdur | grep: yeganə istinad `CampaignService.java` |
| F13 | R | Cache eviction və tranzaksiya commit-inin sırası zəmanətli deyil; RedisCacheManager transaction-aware deyil → qısa müddətli köhnə statistika mümkündür; TTL yoxdur | `CampaignService.java:54-55`, `91-92`, `102-103`, `121-131`; Boot `RedisCacheConfiguration` (TTL yalnız `spring.cache.redis.time-to-live` ilə) |
| F14 | M | Təyin olunmuş UUID id + `@Version` yoxdur → `save()`/`saveAndFlush()` `merge` edir: hər insertdən əvvəl SELECT | `CampaignEntity.java:20-21`, `CampaignStatusHistoryEntity.java:40-47`, `AuthService.java:115-123` |
| F15 | M (performans) | `campaign_status_histories.campaign_id` üzrə indeks yoxdur; `idx_campaign_duplicate_lookup` partial unique indekslə eyni sütunları təkrarlayır | `campaign-service/.../db.changelog-master.yml:1-113` |
| F16 | M | `refresh_tokens.token_hash` və `password_reset_tokens.token_hash` üçün həm `unique` constraint, həm ayrıca indeks | `auth-service/.../db.changelog-master.yml:110-145`, `166-201` |
| F17 | R | Refresh token rotasiyasında optimistik kilid yoxdur → paralel iki refresh eyni token-lə iki yeni cüt ala bilər | `RefreshTokenEntity.java` (`@Version` yoxdur), `AuthService.java:175-190` |
| F18 | D | Paralel qeydiyyatda DB unique pozuntusu (`DataIntegrityViolationException`) tutulmur → 500 + SQL mesajı | `AuthService.java:103-126`, `GlobalExceptionHandler.java:130-139` |
| F19 | R | gRPC klientində deadline yoxdur; server tərəfində `@GrpcAdvice` yoxdur → `ResourceNotFoundException` gRPC `UNKNOWN`, dashboard-da 500 olur (404 əvəzinə) | `CampaignGrpcClient.java:39-68`, `CampaignGrpcEndpoint.java:34-47` |
| F20 | M | Jackson 2 `ObjectMapper` bean-i yalnız Eureka client-in tranzitiv gətirdiyi `spring-boot-jackson2` (deprecated) sayəsində mövcuddur; `ServiceSecurityConfiguration` və `GatewayRateLimitErrorResponseFilter` ona bağlıdır | dependency:tree (`spring-cloud-netflix-eureka-client → spring-boot-jackson2`), `Jackson2AutoConfiguration` `@Deprecated(since="4.0.0", forRemoval=true)` |
| F21 | M | `JsonSerializer`/`JsonDeserializer` spring-kafka 4.0-dan `@Deprecated(forRemoval=true)` | spring-kafka 4.1.0 mənbəsi |
| F22 | R | Repozitoriyada demo RSA private key və DB/Grafana şifrələri var | `auth-service/src/main/resources/keys/private.pem`, compose faylları |
| F23 | M | Observation konfiqurasiyası 3 dəfə kopyalanıb (gateway, config, discovery) | `common/.../HttpServerObservationConfiguration.java` və digər 3 fayl |
| F24 | R (performans) | Promtail `trace_id`/`span_id`-ni Loki label-ı edir → yüksək kardinallıq | `observability/promtail/promtail-config.yml:46-49` |
| F25 | T | Yavaş sorğu həddi 5 ms → filtrin aktiv olduğu servislərdə (campaign, log) praktiki olaraq hər sorğu Kafka-ya gedir | `application.yml` `app.slow-request-threshold-ms: 5` |
| F26 | M | log-service `Page<SlowRequestDocument>`-u birbaşa serializə edir (DTO-suz); campaign-service isə `CampaignPageResponse` istifadə edir | `SlowRequestLogController.java:36-43` |
| F27 | R | Migrasiyalar testlərdə işləmir (`spring.liquibase.enabled=false`, `ddl-auto=create-drop`) → partial unique indeks və sxem validasiyası test edilmir | test base sinifləri |
| F28 | M | Testcontainers image-ləri istehsal image-lərindən fərqlidir (postgres:17-alpine vs postgres:latest; cp-kafka 7.6.1 vs apache/kafka:latest) | test base sinifləri, compose |
| F29 | M | Docker Compose-da bir çox `depends_on` sağlamlıq şərtisiz; campaign-service-də `restart` siyasəti yoxdur | `docker-compose.yml` |
| F30 | H | springdoc 2.8.13 (Boot 3 xətti) Boot 4 / Framework 7 ilə rəsmi uyğunluq xəttindən kənardır; runtime-da yoxlanmayıb | `pom.xml:32` |
| F31 | T | Dashboard → campaign gRPC və auth → log HTTP static ünvan istifadə edir; Eureka yalnız gateway marşrutları və gateway OpenAPI klienti üçün işlənir | `dashboard-service/application.yml:12-15`, `auth-service/application.yml:21-24` |
| F32 | R | Config Server əlçatmaz olduqda `optional:configserver:` sayəsində servislər lokal `application.yml` ilə başlayır; paylanan `spring.kafka.consumer.auto-offset-reset: earliest` itir → yeni consumer group `latest`-dən başlayar | `config-server/src/main/resources/config/application.yml:5-9`, servislərin `spring.config.import` |
| F33 | M | Testlər: campaign approve/deactivate keçidləri, gRPC endpoint, gateway, filtrlər, exception mapping üçün test yoxdur | test qovluqları |
| F34 | R | `@Async` audit icrası (virtual thread) üçün trace kontekst ötürülməsi konfiqurasiya olunmayıb → audit HTTP çağırışları ayrı trace kimi görünə bilər | `VirtualThreadConfiguration.java:35-38` (`TaskDecorator` yoxdur) — H/NƏTİCƏ |
| F35 | T | Access token 120 dəq; logout yalnız refresh token-i ləğv edir, access token vaxtı bitənə qədər etibarlıdır | `application.yml` `app.jwt.expiration-minutes`, `AuthService.java:269-279` |
| F36 | D | `approve`/`deactivate` cavabında `updatedAt` köhnə dəyərlə qayıdır: `@PreUpdate` flush zamanı (commit-də) işləyir, mapper isə ondan əvvəl çağırılır | `CampaignService.java:162-169`, `CampaignEntity.java:65-68` |
| F37 | R | `CampaignEntity`-də `@Version` yoxdur: paralel `approve` və `deactivate` hər ikisi uğurlu ola bilər (lost update), tarixçədə ziddiyyətli iki qeyd və iki hadisə yaranır | `CampaignEntity.java`, `CampaignService.java:91-111` |
| F38 | R (observability) | `GlobalExceptionHandler` heç bir istisnanı loglamır; catch-all ilə emal olunan 500 xətalarının stack trace-i log-a düşmür | `GlobalExceptionHandler.java` (logger yoxdur), `130-139` |
| F39 | M | `AuthAuditLogRequest` üçün heç bir validasiya yoxdur (`@Valid` və constraint-lər yoxdur); `slow-requests` `page/size` yoxlanmır (mənfi `page` → 500) | `AuthAuditLogController.java:32-35`, `AuthAuditLogRequest.java`, `SlowRequestLogController.java:38-43` |
| F40 | R | `/actuator/**` bütün servislərdə `permitAll`; gateway-in öz actuator-u da açıq yollardadır; Compose servis portlarını host-a açır → `prometheus`, `metrics` endpoint-ləri autentifikasiyasız əlçatandır | `ServiceSecurityConfiguration.java:96-104`, `GatewayJwtAuthenticationFilter.java:39-50`, `application.yml` `management.endpoints.web.exposure.include` |
| F41 | M | CORS konfiqurasiyası yoxdur; başqa origin-dən brauzer klientləri bloklanır (Swagger UI gateway ilə eyni origin-dədir) | grep: `Cors` yoxdur |
| F42 | M | `GET /slow-requests` sıralamasız `findAll(PageRequest)` edir — nəticələr `occurredAt` üzrə deyil, ixtiyari sırada; `SlowRequestDocument` sahələrinin çoxu `@Field`-siz (dinamik mapping) | `SlowRequestLogController.java:38-43`, `SlowRequestDocument.java:13-26` |
| F43 | R (əməliyyat) | Elasticsearch indekslərində saxlanma/ILM siyasəti yoxdur; 5 ms həddi ilə `slow-request-logs` məhdudiyyətsiz böyüyür | `SlowRequestDocument`, `app.slow-request-threshold-ms: 5` |
| F44 | T | Gateway JWT/PEM kodu `security` modulundan ayrıca yazılıb (reaktiv gateway servlet asılılıqlarını çəkməsin deyə) — eyni məntiq iki yerdə | `GatewayPemKeyUtils.java:59`, `PemKeyUtils.java:59`, `GatewayJwtTokenService.java`, `JwtTokenService.java` |
| F45 | M | `DUPLICATE` statusu heç bir axında təyin olunmur, lakin sorğu, yoxlama, süzgəc və partial indeksdə nəzərə alınır (ölü vəziyyət) | `CampaignStatus.java:3-8`, `CampaignService.java:140-150`, `171-175`, `CampaignGrpcClient.java:25-29` |
| F46 | R | `@Async` audit tapşırığı tranzaksiya daxilində, commit-dən əvvəl verilir → rollback olunan əməliyyat üçün "uğurlu" audit qeydi yarana bilər | `AuthService.java:103-126`, `AuthAuditService.java:51-59` |

## 5. Uğursuzluq təsiri (xülasə)

| Komponent düşür | Təsir |
|---|---|
| PostgreSQL auth_db | Login/qeydiyyat 500 (catch-all); gateway digər marşrutları işləyir |
| PostgreSQL campaign_db | Kampaniya CRUD 500; gRPC statistika: cache-də varsa cache-dən, yoxdursa `UNKNOWN` → dashboard 500 |
| Redis | Gateway rate limit fail-open (F06); campaign-service `@Cacheable` xətası → 500 (Spring Cache standart `CacheErrorHandler` xətanı yenidən atır) |
| Kafka | Yavaş sorğu jurnalları və `campaign-events` itir; sorğular `max.block.ms`-ə qədər gecikə bilər (F10) |
| Elasticsearch | log-service consumer xətası → 3 təkrar (1 s) → DLQ; audit POST → 500 → auth-service Retry/CB, autentifikasiya davam edir |
| log-service | Audit itir (loglanır), login təsirlənmir |
| campaign-service | Dashboard 500 (gRPC `UNAVAILABLE`), gateway campaign marşrutu 503/500 |
| Eureka | Gateway `lb://` marşrutları yeni instans tapa bilmir (kəşf keşi müəyyən müddət işləyir) |
| Config Server | Servislər lokal konfiqurasiya ilə başlayır (optional), F32 |
