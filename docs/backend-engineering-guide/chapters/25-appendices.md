# 25. Əlavələr

## 25.1 Terminologiya lüğəti və abbreviaturalar

| Termin | İzah | Fəsil |
|---|---|---|
| ACID | Atomiklik, ardıcıllıq, izolyasiya, davamlılıq — relational tranzaksiya zəmanətləri | 7 |
| ADR | Architecture Decision Record — qərarın konteksti, variantları və nəticəsi | 21, 22 |
| Aggregate (aqreqat) | Birlikdə dəyişən və bir tranzaksiya sərhədi olan domen obyektləri qrupu | 22 |
| At-least-once | Mesaj ən azı bir dəfə çatdırılır; dublikat mümkündür | 11 |
| Auto-configuration | Classpath və şərtlərə görə Spring Boot-un avtomatik yaratdığı bean-lər | 3, 4 |
| BFF | Backend for Frontend — klientə uyğunlaşdırılmış nazik aqreqasiya servisi (dashboard) | 13, 20 |
| BOM | Bill of Materials — Maven-də versiyaları mərkəzləşdirən `dependencyManagement` idxalı | 3 |
| Bulkhead | Bir asılılığa eyni vaxtda gedən çağırışları məhdudlaşdıran izolyasiya | 14 |
| Burst capacity | Token bucket-in maksimum tutumu | 15 |
| Cardinality (kardinallıq) | Metrik/log label dəyərlərinin unikal kombinasiyalarının sayı | 17 |
| CDC | Change Data Capture — DB jurnalından hadisə çıxarmaq (Debezium) | 22 |
| Circuit breaker | Uğursuzluq nisbəti həddi aşanda çağırışları müvəqqəti dayandıran mexanizm | 14 |
| Consumer group | Partisiyaları öz aralarında bölüşən Kafka consumer-ləri | 11 |
| CQRS | Yazı və oxu modellərinin ayrılması | 22 |
| Deadline | gRPC çağırışının mütləq bitmə vaxtı; serverə ötürülür | 13 |
| Dirty checking | Hibernate-in idarə olunan entity dəyişikliklərini flush-da avtomatik UPDATE etməsi | 7 |
| DLQ | Dead Letter Queue/Topic — emal oluna bilməyən mesajlar üçün ayrıca mövzu | 11 |
| Dual write | İki sistemə (DB və broker) atomik olmayan yazı | 11, 21 |
| Eventual consistency | Məlumatın bir müddət sonra uyğunlaşması | 22 |
| Fail-open / fail-closed | Asılılıq xətasında sorğunu buraxmaq / rədd etmək | 15 |
| Idempotency | Təkrar icranın nəticəni dəyişməməsi | 11, 14 |
| ILM | Index Lifecycle Management (Elasticsearch) | 12 |
| ISR | In-Sync Replicas — leader ilə sinxron Kafka replikaları | 11 |
| JWKS | JSON Web Key Set — public açarların paylanması üçün endpoint | 10, 22 |
| JWT | JSON Web Token — imzalanmış claim-lər dəsti | 10 |
| KRaft | ZooKeeper-siz Kafka metadata rejimi | 11, 18 |
| Lost update | Paralel yeniləmədə birinin dəyişikliyinin itməsi | 7, 21 |
| Mapping (ES) | Elasticsearch indeksinin sahə tipləri sxemi | 12 |
| MDC | Mapped Diagnostic Context — log sətirlərinə `traceId` kimi kontekst əlavə edən mexanizm | 17 |
| mTLS | Hər iki tərəfin sertifikatla autentifikasiyası | 10 |
| Observation | Micrometer-in metrik və trace yaradan vahid API-si | 17 |
| Offset | Kafka partisiyasında qeydin ardıcıl nömrəsi | 11 |
| OIDC | OpenID Connect — OAuth2 üzərində identifikasiya protokolu | 22 |
| Outbox | Hadisəni DB tranzaksiyasında cədvələ yazıb sonra broker-ə ötürmə nümunəsi | 11, 22 |
| Partial index | `WHERE` şərtli indeks (PostgreSQL) | 7 |
| Persistence context | Hibernate sessiyasında idarə olunan entity-lərin birinci səviyyə cache-i | 7 |
| Poison pill | Consumer-i sonsuz xəta döngüsünə salan mesaj | 11 |
| ProblemDetail | RFC 9457 xəta formatı; Spring-in dəstəklədiyi standart | 8, 22 |
| Proxy (Spring AOP) | Annotasiya əsaslı davranışı (tranzaksiya, cache) həyata keçirən əhatəedici obyekt | 4 |
| RED metrikləri | Rate, Errors, Duration — servis səviyyəli əsas siqnallar | 17, 23 |
| Resource server | Token-i yoxlayan, lakin buraxmayan servis (OAuth2 rolu) | 10, 22 |
| RS256 | RSA + SHA-256 asimmetrik JWT imzası | 10 |
| Shared kernel | Bir neçə servisin paylaşdığı ortaq kod modulu | 21 |
| Span / trace | Paylanmış izləmədə əməliyyat vahidi / onların ağacı | 17 |
| Testcontainers | Testlərdə real asılılıqları Docker konteynerində işə salan kitabxana | 19 |
| Token bucket | Sabit sürətlə dolan vedrə əsaslı rate limit alqoritmi | 15 |
| Virtual thread | JVM tərəfindən idarə olunan yüngül thread (Java 21+) | 4 |

## 25.2 Endpoint kataloqu

### REST (gateway `:8080` üzərindən)

| Metod | Yol | Servis | Rol | Gövdə / parametrlər | Uğur | Əsas xətalar |
|---|---|---|---|---|---|---|
| POST | `/api/v1/auth/register` | auth | Açıq | `username` 3–100, `email`, `password` 8–100, `fullName` 2–150, `role` | 200 | 400, 409 (mövcuddur), 500 (yarış, F18) |
| POST | `/api/v1/auth/login` | auth | Açıq | `usernameOrEmail`, `password` | 200 | 400, 401 |
| POST | `/api/v1/auth/refresh-token` | auth | Açıq | `refreshToken` | 200 | 400, 401 |
| POST | `/api/v1/auth/forgot-password` | auth | Açıq | `usernameOrEmail` | 200 (token cavabda — F02) | 404 (F03), 401 |
| POST | `/api/v1/auth/reset-password` | auth | Açıq | `resetToken`, `newPassword` 8–100 | 200 | 400, 401 |
| POST | `/api/v1/auth/logout` | auth | Açıq | `refreshToken` | 200 | 400, 401 |
| POST | `/api/v1/campaigns` | campaign | ADMIN | `title` 10–50, `description` 20–200, `category` | 201 | 400, 403, 409 |
| GET | `/api/v1/campaigns/{id}` | campaign | USER, ADMIN | — | 200 | 404, 500 (səhv UUID — F07) |
| GET | `/api/v1/campaigns` | campaign | USER, ADMIN | `page`, `size` (10), `sort` (`createdAt,desc`) | 200 | 500 (səhv `sort` — F07) |
| PATCH | `/api/v1/campaigns/{id}/approve` | campaign | ADMIN | — | 200 | 404, 409 |
| PATCH | `/api/v1/campaigns/{id}/deactivate` | campaign | ADMIN | — | 200 | 404, 409 |
| GET | `/api/v1/campaigns/{id}/history` | campaign | ADMIN | — | 200 | 404 |
| GET | `/api/v1/campaigns/statistics` | campaign | ADMIN | — | 200 | 500 (Redis) |
| GET | `/api/v1/dashboard/classifieds/statistics` | dashboard | ADMIN | — | 200 | 500 (gRPC — F19) |
| GET | `/api/v1/dashboard/campaigns/{campaignId}/history` | dashboard | ADMIN | — | 200 | 500 (yoxdursa da — F19) |
| GET | `/api/v1/logs/slow-requests` | log | ADMIN | `page` (0), `size` (20) | 200 | 500 (mənfi `page` — F39) |
| POST | `/api/v1/logs/auth-audit` | log | INTERNAL_SERVICE | `AuthAuditLogRequest` (validasiyasız — F39) | 200 | 401, 403 |

Mənbələr: `AuthController.java:47-117`, `CampaignController.java:42-92`, `DashboardController.java:39-57`,
`SlowRequestLogController.java:35-43`, `AuthAuditLogController.java:29-35`. Gateway səviyyəsində əlavə olaraq: 401 (boş
gövdə, token yoxdur/etibarsız), 429 (rate limit), 503 (instans yoxdur).

### Sənədləşmə və əməliyyat endpoint-ləri

| Yol | Harada | Qeyd |
|---|---|---|
| `/swagger-ui.html` | gateway | Dörd servisin sənədi seçilə bilir |
| `/openapi/{auth,campaign,dashboard,logs}` | gateway | Aqreqasiya + fallback (`GatewayOpenApiController.java:87-105`) |
| `/{auth,campaign,dashboard,logs}/v3/api-docs` | gateway | `StripPrefix=1` ilə birbaşa ötürmə |
| `/v3/api-docs`, `/swagger-ui.html` | hər servis | springdoc |
| `/actuator/{health,info,prometheus,metrics}` | hər servis | Autentifikasiyasız (F40) |

### gRPC (`campaign-service:9090`, plaintext)

| RPC | Sorğu | Cavab | İstifadəçi |
|---|---|---|---|
| `GetCampaignStatistics` | boş | `repeated CampaignStatusCount {status, count}` | dashboard |
| `GetCampaignHistory` | `campaign_id` (sətir UUID) | `repeated CampaignHistoryItem {previous_status, new_status, reason, changed_at}` | dashboard |

Kontrakt: `grpc-contract/src/main/proto/campaign.proto:8-9`.

### Kafka mövzuları və Elasticsearch indeksləri

| Ad | Növ | Yazan | Oxuyan | Açar / qeyd |
|---|---|---|---|---|
| `log-events` | Kafka | campaign-service, log-service (yavaş sorğu filtri) | log-service (`log-service`) | URI |
| `log-events.DLQ` | Kafka | `DeadLetterPublishingRecoverer` | log-service (`log-service-dlq`) — yalnız log | Orijinal açar |
| `campaign-events` | Kafka | campaign-service | — | Kampaniya id-si |
| `slow-request-logs` | ES indeksi | log-service consumer | `GET /api/v1/logs/slow-requests` | Yalnız `occurredAt` açıq mapping |
| `auth-audit-logs` | ES indeksi | `POST /api/v1/logs/auth-audit` | Repozitoriyada oxuyan API yoxdur | `AuthAuditLogDocument` |

## 25.3 Konfiqurasiya və mühit dəyişənləri arayışı

Aşağıdakı cədvəl `*/src/main/resources/**/*.yml` fayllarındakı `${DƏYİŞƏN:standart}` yer tutucularından avtomatik
çıxarılıb. Şifrələrin standart dəyərləri **redaktə olunub** — onlar repozitoriyada mövcuddur (F22), lakin kitabda təkrar
edilmir.

| Dəyişən | Servislər | Standart dəyər |
|---|---|---|
| `CONFIG_SERVER_URL` | gateway, auth, campaign, dashboard, log | `http://localhost:8888` |
| `EUREKA_SERVER_URL` | gateway, auth, campaign, config, dashboard, log | `http://localhost:8761/eureka` |
| `EUREKA_INSTANCE_IP_ADDRESS` | eyni | `127.0.0.1` |
| `REDIS_HOST` / `REDIS_PORT` | gateway, campaign | `localhost` / `6379` |
| `REDIS_TIMEOUT` | gateway | `2s` |
| `RATE_LIMITER_REPLENISH_RATE` / `_BURST_CAPACITY` / `_REQUESTED_TOKENS` | gateway | `1` / `5` / `5` |
| `AUTH_SERVICE_URI`, `CAMPAIGN_SERVICE_URI`, `DASHBOARD_SERVICE_URI`, `LOG_SERVICE_URI` | gateway | `lb://<servis>` |
| `*_SERVICE_OPENAPI_URL` (4 ədəd) | gateway | `lb://<servis>/v3/api-docs` |
| `JWT_PUBLIC_KEY_LOCATION` | gateway, auth, campaign, dashboard, log (+ config-server-in payladığı konfiqurasiya) | `classpath:keys/public.pem` |
| `JWT_PRIVATE_KEY_LOCATION` | auth | `classpath:keys/private.pem` |
| `AUTH_DB_URL` / `AUTH_DB_USERNAME` / `AUTH_DB_PASSWORD` | auth | `jdbc:postgresql://localhost:5433/auth_db` / `auth_user` / *redaktə* |
| `CAMPAIGN_DB_URL` / `CAMPAIGN_DB_USERNAME` / `CAMPAIGN_DB_PASSWORD` | campaign | `jdbc:postgresql://localhost:5434/campaign_db` / `campaign_user` / *redaktə* |
| `LOG_SERVICE_URL` | auth | `http://localhost:8084` |
| `LOG_SERVICE_CB_*` (5 ədəd) | auth | 50 / 10 / 5 / 3 / `30s` |
| `LOG_SERVICE_RETRY_MAX_ATTEMPTS` / `_WAIT_DURATION` | auth | `3` / `500ms` |
| `LOG_SERVICE_BULKHEAD_MAX_CONCURRENT_CALLS` / `_MAX_WAIT_DURATION` | auth | `10` / `0ms` |
| `KAFKA_BOOTSTRAP_SERVERS` | campaign, log (+ config-server) | `localhost:9092` |
| `CAMPAIGN_GRPC_PORT` | campaign | `9090` |
| `CAMPAIGN_GRPC_ADDRESS` | dashboard | `static://localhost:9090` |
| `ELASTICSEARCH_URIS` | log | `http://localhost:9200` |
| `SPRING_ELASTICSEARCH_CONNECTION_TIMEOUT` / `_SOCKET_TIMEOUT` | log | `10s` / `30s` |
| `MANAGEMENT_TRACING_SAMPLING_PROBABILITY` | bütün 7 tətbiq | `1.0` |
| `MANAGEMENT_TRACING_EXPORT_OTLP_ENABLED` | bütün 7 tətbiq | `true` |
| `OTEL_SERVICE_NAME` | bütün 7 tətbiq | `${spring.application.name}` |
| `OTEL_EXPORTER_OTLP_TRACES_ENDPOINT` / `_PROTOCOL` | bütün 7 tətbiq | `http://localhost:4317` / `grpc` |
| `LOGGING_FILE_NAME` | bütün 7 tətbiq | `logs/<servis>.log` |

Yer tutucu olmayan, lakin vacib sabit xassələr: `app.slow-request-threshold-ms: 5`, `app.kafka.topics.*`,
`app.jwt.expiration-minutes: 120`, `app.auth.refresh-token-expiration-days: 7`,
`app.auth.password-reset-token-expiration-minutes: 15` (`auth-service/src/main/resources/application.yml:73-78`). Compose
əlavə olaraq `APP_OPENAPI_*_URL` dəyişənlərini təyin edir (`docker-compose.yml:66-69`); Spring-in relaxed binding qaydası
ilə onlar birbaşa `app.openapi.*-url` xassələrinə bağlanır və yer tutucudakı dəyişəndən üstündür [NƏTİCƏ].

## 25.4 Fəsillərdə ayrıca müzakirə olunmayan fayllar

Əhatə matrisi (fəsil 26) hər faylın hansı fəsildə istinad olunduğunu göstərir. Aşağıdakı fayllar sadə və ya
təkrarlanan olduğu üçün fəsillərdə ayrıca təhlil edilməyib; onların məzmunu burada qeyd olunur.

| Fayl(lar) | Məzmun | Qeyd |
|----------------------------------|----------------------------------------|------------------------------|
| `api-gateway/Dockerfile`, `auth-service/Dockerfile`, `config-server/Dockerfile`, `dashboard-service/Dockerfile`, `discovery-server/Dockerfile`, `log-service/Dockerfile` | Eyni iki mərhələli şablon: `maven:3.9.11-amazoncorretto-25` ilə `mvn -pl <modul> -am clean package -DskipTests`, sonra `amazoncorretto:25` üzərində `java --enable-preview -jar`. Fərq yalnız modul adı və `EXPOSE` portudur | Şablon fəsil 3.11 və 18-də campaign-service nümunəsində izah olunub; JRE əvəzinə tam JDK image, root istifadəçi, layer cache yoxdur |
| `api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/ApiGatewayApplication.java`, `dashboard-service/src/main/java/com/casestudy/insurancecampaign/dashboard/DashboardServiceApplication.java`, `log-service/src/main/java/com/casestudy/insurancecampaign/log/LogServiceApplication.java` | `@SpringBootApplication(scanBasePackages = "com.casestudy.insurancecampaign")` + `@EnableDiscoveryClient`; log-service-də əlavə `@EnableKafka` | Geniş skan `common` və `security` bean-lərini daxil edir (fəsil 4.4) |
| `api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/config/GatewayHttpServerObservationConfiguration.java`, `config-server/src/main/java/com/casestudy/insurancecampaign/config/observability/ConfigServerHttpServerObservationConfiguration.java`, `discovery-server/src/main/java/com/casestudy/insurancecampaign/discovery/observability/DiscoveryServerHttpServerObservationConfiguration.java` | `ObservationPredicate` actuator sorğularını trace-dən çıxarır, `ObservationFilter` span adlarını `http get /yol` formatına salır | config və discovery versiyaları paket/sinif adından başqa eynidir; gateway versiyası reaktiv kontekstə uyğunlaşdırılıb (F23) |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/enums/UserStatus.java` | `ACTIVE`, `PASSIVE`, `DELETED` | Kod yalnız `ACTIVE` təyin edir; statusu dəyişən endpoint yoxdur — "User is not active" yolu yalnız DB-də əl ilə dəyişiklikdən sonra mümkündür |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/repository/AppUserRepository.java` | `findByUsername`, `findByEmail`, `existsByUsername`, `existsByEmail` | Törədilmiş sorğular (fəsil 7.9) |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/repository/PasswordResetTokenRepository.java` | `findByTokenHash` | Hash üzrə axtarış; unikal indeksə əsaslanır |
| `auth-service/src/main/resources/keys/public.pem` | auth-service-in öz public açarı (PEM) | Məzmunu kitabda təkrar edilmir |
| `campaign-service/src/test/resources/keys/private.pem`, `campaign-service/src/test/resources/keys/public.pem`, `dashboard-service/src/test/resources/keys/private.pem`, `dashboard-service/src/test/resources/keys/public.pem`, `log-service/src/test/resources/keys/private.pem`, `log-service/src/test/resources/keys/public.pem` | Testlərdə real RS256 token yaratmaq üçün açar cütləri | Yalnız test üçündür; məzmunu təkrar edilmir. İstehsal açarı ilə eyni olmamalıdır |
| `dashboard-service/pom.xml` | `common`, `security`, `grpc-contract`, gRPC kitabxanaları, `grpc-client-spring-boot-starter`, web, Eureka, actuator, OTel, springdoc; testdə `postgresql` | Kafka yoxdur → yavaş sorğu filtri aktiv deyil |
| `discovery-server/pom.xml` | `spring-cloud-starter-netflix-eureka-server`, actuator, Prometheus, OTel bridge + OTLP exporter | `common`-dan asılı deyil |
| `log-service/pom.xml` | `common`, `security`, web, `spring-kafka`, `spring-boot-kafka`, `spring-boot-starter-data-elasticsearch`, Eureka, actuator, OTel, springdoc; testdə Testcontainers `kafka`, `elasticsearch` | Fəsil 11, 12 |
| `postman/Insurance Campaign Microservices.postman_collection.json` | Postman v2.1 kolleksiyası: "Authentication" qovluğunda 10 sorğu, kökdə 16 sorğu (kampaniya, dashboard, slow-requests); dəyişənlər: `gatewayUrl`, servis URL-ləri, `token`, `adminToken`, `userToken`, `campaignId` və s.; bəzi sorğularda token-i dəyişənə yazan test skriptləri | Bəzi sorğular gateway-i keçərək birbaşa servis portlarına gedir (`authServiceUrl`, `logServiceUrl`) — bu, F40 və 10.8-dəki "gateway-dən yan keçmə" ssenarisinin praktiki nümunəsidir |
| `.gitignore` | `target/`, IDE faylları, `docker-data/`, `logs/`, `*.log`, `.env` | `keys/*.pem` istisna edilmir — açarlar commit olunub (F22) |

## 25.5 Rəsmi sənədlər və əlavə oxu

| Mövzu | Mənbə |
|---|---|
| Spring Boot | docs.spring.io/spring-boot (reference, "Upgrading to 4.0" qeydləri) |
| Spring Framework | docs.spring.io/spring-framework (HTTP Interface, transaction, AOP) |
| Spring Security | docs.spring.io/spring-security (servlet architecture, resource server) |
| Spring Cloud Gateway | docs.spring.io/spring-cloud-gateway (Server WebFlux, `RequestRateLimiter`) |
| Spring for Apache Kafka | docs.spring.io/spring-kafka (error handling, DLT, observation) |
| Spring Data Elasticsearch / JPA / Redis | docs.spring.io/spring-data/... |
| Apache Kafka | kafka.apache.org/documentation (producer/consumer configs) |
| Elasticsearch | elastic.co/guide (mapping, ILM) |
| gRPC Java | grpc.io/docs/languages/java; net.devh grpc-spring-boot-starter sənədləri |
| Resilience4j | resilience4j.readme.io |
| Liquibase | docs.liquibase.com |
| OpenTelemetry | opentelemetry.io/docs |
| Testcontainers | java.testcontainers.org |
| Transactional outbox, saga | microservices.io/patterns |
| Kitablar | Martin Kleppmann — *Designing Data-Intensive Applications*; Sam Newman — *Building Microservices*; Michael Nygard — *Release It!* |

## 25.6 Audit xülasəsi

Kitabı necə yoxladığımı `audit/audit-report.md`-də ətraflı təsvir etmişəm. Qısaca:

- **Audit A (məzmun tamlığı):** tapşırıqda tələb olunan bütün bölmələrin (texnologiya fəsilləri, iş axınları, dizayn
  fəsli, istehsal mühəndisliyi, qiymətləndirmə, əhatə matrisi, diaqramlar, müsahibə hazırlığı) mövcudluğu
  yoxlanılıb.
- **Audit B (texniki dəqiqlik):** `build/verify_refs.py` bütün `fayl:sətir` istinadlarını (faylın mövcudluğu, sətir
  aralığı) və bütün kod çıxarışlarını (sətirlərin mənbədə ardıcıl olaraq mövcudluğu) avtomatik yoxlayır; nəticə
  `audit/verify-refs.json`-dadır. Semantik yoxlama (istinad olunan sətirlərin iddianı həqiqətən dəstəkləməsi) əl ilə,
  seçmə üsulu ilə aparılıb; tapılan səhvlər düzəldilib və hesabatda sadalanıb.
- **Audit C (PDF bütövlüyü):** mündəricatın səhifə nömrələri, şriftlər, diaqramların render-i, kod bloklarının və
  cədvəllərin kəsilməməsi səhifələrin PNG render-ləri ilə vizual yoxlanılıb.

Məhdudiyyətlər: layihə kompilyasiya edilməyib və işə salınmayıb (mühitdə Java 25 yox idi); runtime davranışı haqqında
bütün iddialar [NƏTİCƏ] kimi işarələnib.
