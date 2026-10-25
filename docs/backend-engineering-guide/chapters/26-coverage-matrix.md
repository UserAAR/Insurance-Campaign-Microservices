# 26. Əhatə matrisi və tapıntı indeksi

> Bu fəsil `build/coverage.py` tərəfindən avtomatik yaradılır; əl ilə redaktə etməyin. Tam (9 sütunlu) matris:
> `analysis/coverage-matrix.md`.

## 26.1 Necə oxumalı

- **Fəsillər** — faylın tam yolu və ya sinif/fayl adı ilə istinad olunduğu fəsillər (bu fəsil hesaba daxil deyil).
- **Yoxlama** — `OK`: həmin fayla edilən bütün `fayl:sətir` istinadları və kod çıxarışları `build/verify_refs.py`
  ilə avtomatik doğrulanıb; `—`: faylın sətir səviyyəli istinadı yoxdur (yalnız adla xatırlanır).
- Yollar qısaldılıb: `main:` = `src/main/java/com/casestudy/insurancecampaign/`, `test:` = eyni test qovluğu.

Cəmi izlənən fayl: **178**; ən azı bir fəsildə istinad olunan: **178**; sətir səviyyəsində avtomatik doğrulanan: **101**.

## 26.2 Modullar üzrə xülasə

| Modul | Fayl | İstinad olunan | Sətir səviyyəsində doğrulanan |
|---|---:|---:|---:|
| `(kök)` | 5 | 5 | 3 |
| `.github` | 2 | 2 | 1 |
| `api-gateway` | 14 | 14 | 8 |
| `auth-service` | 45 | 45 | 24 |
| `campaign-service` | 28 | 28 | 23 |
| `common` | 15 | 15 | 10 |
| `config-server` | 6 | 6 | 3 |
| `dashboard-service` | 16 | 16 | 6 |
| `discovery-server` | 5 | 5 | 1 |
| `grpc-contract` | 2 | 2 | 2 |
| `log-service` | 23 | 23 | 10 |
| `observability` | 9 | 9 | 5 |
| `postman` | 1 | 1 | 0 |
| `security` | 7 | 7 | 5 |

## 26.3 Fayl səviyyəsində matris

### (kök)

| Fayl | Məsuliyyət | Fəsillər | Yoxlama |
|------------------------------------|------------------------------|------------------------|----------|
| `.gitignore` | Git istisnaları | 25 | — |
| `README.md` | Layihə sənədi | 01, 02, 03, 05, 06, 08, 09, 10, 13, 15, 16, 17, 18, 22 | — |
| `docker-compose-local.yml` | Yalnız infrastruktur (servislər IDE-dən) | 01, 18 | OK |
| `docker-compose.yml` | Tam stek (7 tətbiq + infrastruktur) | 01, 02, 04, 10, 11, 12, 13, 15, 16, 17, 18, 20, 21, 23, 25 | OK |
| `pom.xml` | Parent POM, modul siyahısı, BOM-lar, plugin-lər | 01, 02, 03, 13, 18 | OK |

### .github

| Fayl | Məsuliyyət | Fəsillər | Yoxlama |
|------------------------------------|------------------------------|------------------------|----------|
| `workflows/ci-cd.yml` | CI/CD: verify → sonar → docker | 18 | OK |
| `workflows/mark-done-on-merge.yml` | Merge olunan PR və bağlı issue-larda `done` label | 18 | — |

### api-gateway

| Fayl | Məsuliyyət | Fəsillər | Yoxlama |
|------------------------------------|------------------------------|------------------------|----------|
| `Dockerfile` | Image | 25 | — |
| `pom.xml` | Gateway asılılıqları | 10, 16 | OK |
| `main: gateway/ApiGatewayApplication.java` | Gateway giriş nöqtəsi | 25 | — |
| `main: gateway/config/GatewayHttpServerObservationConfiguration.java` | Observation (kopya) | 25 | — |
| `main: gateway/config/GatewayLoadBalancerConfiguration.java` | @LoadBalanced WebClient.Builder | 16 | OK |
| `main: gateway/config/GatewayRateLimiterConfiguration.java` | Rate limit açar resolver-i | 10, 15, 20 | OK |
| `main: gateway/filter/GatewayJwtAuthenticationFilter.java` | Gateway JWT yoxlaması | 08, 09, 10, 15, 16, 20 | OK |
| `main: gateway/filter/GatewayRateLimitErrorResponseFilter.java` | 429 üçün JSON gövdə | 03, 04, 05, 15, 16, 20 | OK |
| `main: gateway/openapi/GatewayOpenApiController.java` | OpenAPI aqreqasiyası + fallback | 14, 16, 20, 25 | OK |
| `main: gateway/security/GatewayJwtProperties.java` | Gateway app.jwt.* | 16 | — |
| `main: gateway/security/GatewayJwtTokenService.java` | Gateway JWT doğrulama | 03, 15, 16 | — |
| `main: gateway/security/GatewayPemKeyUtils.java` | Public key oxuma | 03, 21 | OK |
| `src/main/resources/application.yml` | Marşrutlar, rate limit, springdoc | 15, 16, 20, 23 | OK |
| `src/main/resources/keys/public.pem` | RSA public key | 10 | — |

### auth-service

| Fayl | Məsuliyyət | Fəsillər | Yoxlama |
|------------------------------------|------------------------------|------------------------|----------|
| `Dockerfile` | Çoxmərhələli image build | 25 | — |
| `pom.xml` | Modul asılılıqları | 03 | OK |
| `main: auth/AuthServiceApplication.java` | AuthServiceApplication | 04, 09, 14 | OK |
| `main: auth/client/LogServiceHttpClient.java` | LogServiceHttpClient | 04, 09, 14, 20, 21 | OK |
| `main: auth/config/AuthConfiguration.java` | AuthConfiguration | 04, 09 | OK |
| `main: auth/config/LogServiceHttpClientConfiguration.java` | LogServiceHttpClientConfiguration | 02, 04, 10, 14, 20, 21 | OK |
| `main: auth/controller/AuthController.java` | AuthController | 02, 05, 08, 20, 25 | OK |
| `main: auth/model/domain/AppUser.java` | AppUser | 02, 05, 07 | — |
| `main: auth/model/domain/AuthAuditLog.java` | AuthAuditLog | 02, 14 | OK |
| `main: auth/model/domain/PasswordResetToken.java` | PasswordResetToken | 02, 05 | — |
| `main: auth/model/domain/RefreshToken.java` | RefreshToken | 02, 05 | — |
| `main: auth/model/dto/request/ForgotPasswordRequest.java` | ForgotPasswordRequest | 09 | — |
| `main: auth/model/dto/request/LoginRequest.java` | LoginRequest | 08, 09, 20 | OK |
| `main: auth/model/dto/request/LogoutRequest.java` | LogoutRequest | 09 | — |
| `main: auth/model/dto/request/RefreshTokenRequest.java` | RefreshTokenRequest | 09 | — |
| `main: auth/model/dto/request/RegisterRequest.java` | RegisterRequest | 02, 08, 09, 21 | OK |
| `main: auth/model/dto/request/ResetPasswordRequest.java` | ResetPasswordRequest | 07, 08 | OK |
| `main: auth/model/dto/response/ForgotPasswordResponse.java` | ForgotPasswordResponse | 09 | — |
| `main: auth/model/dto/response/LogoutResponse.java` | LogoutResponse | 07, 09 | — |
| `main: auth/model/dto/response/RegisteredUserResponse.java` | RegisteredUserResponse | 05, 09 | — |
| `main: auth/model/dto/response/TokenResponse.java` | TokenResponse | 05, 09, 20 | — |
| `main: auth/model/entity/AppUserEntity.java` | AppUserEntity | 07, 09 | OK |
| `main: auth/model/entity/PasswordResetTokenEntity.java` | PasswordResetTokenEntity | 07, 09 | — |
| `main: auth/model/entity/RefreshTokenEntity.java` | RefreshTokenEntity | 07, 09 | OK |
| `main: auth/model/enums/UserRole.java` | UserRole | 02, 08, 09, 10 | OK |
| `main: auth/model/enums/UserStatus.java` | UserStatus | 25 | — |
| `main: auth/model/mapper/AuthMapper.java` | AuthMapper | 03, 04, 05 | OK |
| `main: auth/repository/AppUserRepository.java` | AppUserRepository | 25 | — |
| `main: auth/repository/PasswordResetTokenRepository.java` | PasswordResetTokenRepository | 25 | — |
| `main: auth/repository/RefreshTokenRepository.java` | RefreshTokenRepository | 07 | OK |
| `main: auth/service/AuthAuditService.java` | AuthAuditService | 02, 04, 08, 09, 14, 20 | OK |
| `main: auth/service/AuthService.java` | AuthService | 04, 05, 07, 08, 09, 14, 20, 21 | OK |
| `main: auth/util/StringUtils.java` | StringUtils | 08, 09 | OK |
| `main: auth/util/TokenHashUtils.java` | TokenHashUtils | 04, 07, 09 | OK |
| `src/main/resources/application.yml` | Servis konfiqurasiyası | 02, 04, 09, 14, 20, 25 | OK |
| `src/main/resources/db/changelog/db.changelog-master.yml` | Liquibase changelog | 01, 07 | OK |
| `src/main/resources/keys/private.pem` | RSA açarı (demo) | 09, 10 | — |
| `src/main/resources/keys/public.pem` | RSA açarı (demo) | 25 | — |
| `test: auth/base/AbstractBaseServiceTest.java` | Test: AbstractBaseServiceTest | 19 | — |
| `test: auth/base/AbstractRestControllerTest.java` | Test: AbstractRestControllerTest | 19 | OK |
| `test: auth/base/AbstractTestContainerConfiguration.java` | Test: AbstractTestContainerConfiguration | 07, 13, 15, 19 | — |
| `test: auth/controller/AuthControllerTest.java` | Test: AuthControllerTest | 19 | — |
| `test: auth/model/mapper/AuthMapperTest.java` | Test: AuthMapperTest | 19 | — |
| `test: auth/service/AuthAuditServiceTest.java` | Test: AuthAuditServiceTest | 14, 19 | OK |
| `test: auth/service/AuthServiceTest.java` | Test: AuthServiceTest | 09, 19 | OK |

### campaign-service

| Fayl | Məsuliyyət | Fəsillər | Yoxlama |
|------------------------------------|------------------------------|------------------------|----------|
| `Dockerfile` | Çoxmərhələli image build | 03, 18 | OK |
| `pom.xml` | Modul asılılıqları | 03, 07, 16, 18 | OK |
| `main: campaign/CampaignServiceApplication.java` | CampaignServiceApplication | 03, 04, 06, 15, 22 | OK |
| `main: campaign/controller/CampaignController.java` | CampaignController | 02, 04, 05, 06, 08, 10, 20, 25 | OK |
| `main: campaign/domain/Campaign.java` | Campaign | 01, 02, 03, 04, 05, 06, 11, 13, 17, 19, 20, 22, 25 | OK |
| `main: campaign/domain/CampaignCategory.java` | CampaignCategory | 02, 06, 07, 08, 11 | OK |
| `main: campaign/domain/CampaignStatus.java` | CampaignStatus | 02, 06, 07, 11, 13, 21, 22 | OK |
| `main: campaign/dto/CampaignHistoryResponse.java` | CampaignHistoryResponse | 06, 13 | — |
| `main: campaign/dto/CampaignPageResponse.java` | CampaignPageResponse | 05, 06, 20, 24 | OK |
| `main: campaign/dto/CampaignResponse.java` | CampaignResponse | 05, 06, 20 | — |
| `main: campaign/dto/CampaignStatisticsResponse.java` | CampaignStatisticsResponse | 06, 13, 20 | — |
| `main: campaign/dto/CreateCampaignRequest.java` | CreateCampaignRequest | 04, 05, 06, 08, 20 | OK |
| `main: campaign/entity/CampaignEntity.java` | CampaignEntity | 03, 05, 06, 07, 19, 21, 22 | OK |
| `main: campaign/entity/CampaignStatusHistoryEntity.java` | CampaignStatusHistoryEntity | 05, 06, 07, 22 | OK |
| `main: campaign/grpc/CampaignGrpcEndpoint.java` | CampaignGrpcEndpoint | 04, 05, 06, 13, 20 | OK |
| `main: campaign/mapper/CampaignMapper.java` | CampaignMapper | 04, 05, 06 | OK |
| `main: campaign/repository/CampaignRepository.java` | CampaignRepository | 04, 05, 06, 07, 19, 20, 21 | OK |
| `main: campaign/repository/CampaignStatusHistoryRepository.java` | CampaignStatusHistoryRepository | 04, 06 | OK |
| `main: campaign/service/CampaignService.java` | CampaignService | 01, 02, 04, 05, 06, 07, 08, 11, 13, 15, 19, 20, 21, 22, 23 | OK |
| `src/main/resources/application.yml` | Servis konfiqurasiyası | 05, 06, 07, 11, 13, 15, 16, 17, 20, 21 | OK |
| `src/main/resources/db/changelog/db.changelog-master.yml` | Liquibase changelog | 06, 07, 20, 21, 23 | OK |
| `test: campaign/base/AbstractBaseServiceTest.java` | Test: AbstractBaseServiceTest | 19 | OK |
| `test: campaign/base/AbstractRestControllerTest.java` | Test: AbstractRestControllerTest | 19 | OK |
| `test: campaign/base/AbstractTestContainerConfiguration.java` | Test: AbstractTestContainerConfiguration | 07, 13, 15, 19 | OK |
| `test: campaign/controller/CampaignControllerTest.java` | Test: CampaignControllerTest | 08, 19 | OK |
| `test: campaign/service/CampaignServiceTest.java` | Test: CampaignServiceTest | 02, 04, 19 | OK |
| `src/test/resources/keys/private.pem` | Test RSA açarı | 25 | — |
| `src/test/resources/keys/public.pem` | Test RSA açarı | 25 | — |

### common

| Fayl | Məsuliyyət | Fəsillər | Yoxlama |
|------------------------------------|------------------------------|------------------------|----------|
| `pom.xml` | Ortaq modulun asılılıqları | 02, 03, 11, 18, 20 | OK |
| `main: common/api/ApiError.java` | Xəta cavab zərfi | 02, 05, 08, 10, 21, 23 | OK |
| `main: common/api/ApiResponse.java` | Uğurlu cavab zərfi | 02, 04, 05, 08, 12, 13, 20, 21 | OK |
| `main: common/config/KafkaJsonConfiguration.java` | Kafka producer (String açar, JSON dəyər) | 03, 04, 06, 11, 23 | OK |
| `main: common/config/VirtualThreadConfiguration.java` | @EnableAsync + virtual thread executor | 04, 20 | OK |
| `main: common/event/CampaignStatusChangedEvent.java` | Kampaniya status hadisəsi | 02, 06, 11 | — |
| `main: common/event/SlowRequestLogEvent.java` | Yavaş sorğu hadisəsi | 02, 04, 11, 19, 20 | — |
| `main: common/exception/BusinessException.java` | Biznes istisna bazası | 05, 07, 08, 09, 20, 21, 22 | OK |
| `main: common/exception/DuplicateCampaignException.java` | Təkrar kampaniya | 06, 07, 08, 21 | — |
| `main: common/exception/ErrorCode.java` | Xəta kodları | 05, 07, 08, 09, 10, 21 | OK |
| `main: common/exception/GlobalExceptionHandler.java` | Mərkəzi istisna xəritələnməsi | 02, 03, 04, 05, 08, 09, 10, 13, 17, 19, 20, 21 | OK |
| `main: common/exception/InvalidStatusTransitionException.java` | Yanlış status keçidi | 06, 08, 21, 22 | — |
| `main: common/exception/ResourceNotFoundException.java` | Resurs tapılmadı | 05, 06, 08, 09, 13, 20, 21, 22 | — |
| `main: common/observability/HttpServerObservationConfiguration.java` | Span adı və actuator filtri | 04, 17 | OK |
| `main: common/web/SlowRequestLoggingFilter.java` | Yavaş sorğuları Kafka-ya göndərir | 03, 04, 05, 11, 16, 19, 20, 21 | OK |

### config-server

| Fayl | Məsuliyyət | Fəsillər | Yoxlama |
|------------------------------------|------------------------------|------------------------|----------|
| `Dockerfile` | Image | 25 | — |
| `pom.xml` | Config server asılılıqları | 17 | OK |
| `main: config/ConfigServerApplication.java` | Config Server | 04 | — |
| `main: config/observability/ConfigServerHttpServerObservationConfiguration.java` | Observation (kopya) | 25 | — |
| `src/main/resources/application.yml` | native profil, search-locations | 04, 16 | OK |
| `src/main/resources/config/application.yml` | Bütün klientlərə paylanan konfiqurasiya | 04, 16 | OK |

### dashboard-service

| Fayl | Məsuliyyət | Fəsillər | Yoxlama |
|------------------------------------|------------------------------|------------------------|----------|
| `Dockerfile` | Çoxmərhələli image build | 25 | — |
| `pom.xml` | Modul asılılıqları | 25 | — |
| `main: dashboard/DashboardServiceApplication.java` | DashboardServiceApplication | 25 | — |
| `main: dashboard/client/CampaignGrpcClient.java` | CampaignGrpcClient | 04, 13, 20, 21 | OK |
| `main: dashboard/controller/DashboardController.java` | DashboardController | 02, 05, 10, 20, 21, 25 | OK |
| `main: dashboard/dto/DashboardCampaignHistoryItemResponse.java` | DashboardCampaignHistoryItemResponse | 13 | — |
| `main: dashboard/dto/DashboardCampaignHistoryResponse.java` | DashboardCampaignHistoryResponse | 13 | — |
| `main: dashboard/dto/DashboardStatisticsResponse.java` | DashboardStatisticsResponse | 13 | — |
| `src/main/resources/application.yml` | Servis konfiqurasiyası | 02, 13, 20 | OK |
| `test: dashboard/base/AbstractBaseServiceTest.java` | Test: AbstractBaseServiceTest | 19 | — |
| `test: dashboard/base/AbstractRestControllerTest.java` | Test: AbstractRestControllerTest | 19 | — |
| `test: dashboard/base/AbstractTestContainerConfiguration.java` | Test: AbstractTestContainerConfiguration | 07, 13, 15, 19 | OK |
| `test: dashboard/client/CampaignGrpcClientTest.java` | Test: CampaignGrpcClientTest | 04, 13, 19 | OK |
| `test: dashboard/controller/DashboardControllerTest.java` | Test: DashboardControllerTest | 03, 19 | OK |
| `src/test/resources/keys/private.pem` | Test RSA açarı | 25 | — |
| `src/test/resources/keys/public.pem` | Test RSA açarı | 25 | — |

### discovery-server

| Fayl | Məsuliyyət | Fəsillər | Yoxlama |
|------------------------------------|------------------------------|------------------------|----------|
| `Dockerfile` | Image | 25 | — |
| `pom.xml` | Eureka server asılılıqları | 25 | — |
| `main: discovery/DiscoveryServerApplication.java` | Eureka Server | 04 | — |
| `main: discovery/observability/DiscoveryServerHttpServerObservationConfiguration.java` | Observation (kopya) | 25 | — |
| `src/main/resources/application.yml` | Eureka konfiqurasiyası | 16 | OK |

### grpc-contract

| Fayl | Məsuliyyət | Fəsillər | Yoxlama |
|------------------------------------|------------------------------|------------------------|----------|
| `pom.xml` | protoc + grpc-java generasiyası | 03, 18 | OK |
| `src/main/proto/campaign.proto` | gRPC kontraktı | 01, 02, 03, 04, 05, 06, 07, 08, 09, 10, 11, 12, 13, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25 | OK |

### log-service

| Fayl | Məsuliyyət | Fəsillər | Yoxlama |
|------------------------------------|------------------------------|------------------------|----------|
| `Dockerfile` | Çoxmərhələli image build | 25 | — |
| `pom.xml` | Modul asılılıqları | 25 | — |
| `main: log/LogServiceApplication.java` | LogServiceApplication | 25 | — |
| `main: log/config/KafkaDlqConfiguration.java` | KafkaDlqConfiguration | 04, 11, 20, 21 | OK |
| `main: log/controller/AuthAuditLogController.java` | AuthAuditLogController | 02, 08, 10, 12, 20, 25 | OK |
| `main: log/controller/SlowRequestLogController.java` | SlowRequestLogController | 02, 05, 10, 12, 20, 21, 25 | OK |
| `main: log/model/document/AuthAuditLogDocument.java` | AuthAuditLogDocument | 12, 25 | OK |
| `main: log/model/document/SlowRequestDocument.java` | SlowRequestDocument | 05, 11, 12, 20, 23 | OK |
| `main: log/model/dto/request/AuthAuditLogRequest.java` | AuthAuditLogRequest | 08, 14, 25 | OK |
| `main: log/repository/AuthAuditLogRepository.java` | AuthAuditLogRepository | 12 | — |
| `main: log/repository/SlowRequestLogRepository.java` | SlowRequestLogRepository | 12 | — |
| `main: log/service/AuthAuditLogService.java` | AuthAuditLogService | 12, 20 | OK |
| `main: log/service/SlowRequestLogConsumer.java` | SlowRequestLogConsumer | 04, 11, 12, 20 | OK |
| `src/main/resources/application.yml` | Servis konfiqurasiyası | 11, 12 | OK |
| `test: log/base/AbstractBaseServiceTest.java` | Test: AbstractBaseServiceTest | 19 | — |
| `test: log/base/AbstractRestControllerTest.java` | Test: AbstractRestControllerTest | 19 | — |
| `test: log/base/AbstractTestContainerConfiguration.java` | Test: AbstractTestContainerConfiguration | 07, 13, 15, 19 | — |
| `test: log/controller/AuthAuditLogControllerTest.java` | Test: AuthAuditLogControllerTest | 19 | — |
| `test: log/controller/SlowRequestLogControllerTest.java` | Test: SlowRequestLogControllerTest | 05, 19 | OK |
| `test: log/service/AuthAuditLogServiceTest.java` | Test: AuthAuditLogServiceTest | 19 | — |
| `test: log/service/SlowRequestLogConsumerTest.java` | Test: SlowRequestLogConsumerTest | 19 | — |
| `src/test/resources/keys/private.pem` | Test RSA açarı | 25 | — |
| `src/test/resources/keys/public.pem` | Test RSA açarı | 25 | — |

### observability

| Fayl | Məsuliyyət | Fəsillər | Yoxlama |
|------------------------------------|------------------------------|------------------------|----------|
| `grafana/dashboards/campaign-logs-traces.json` | Observability konfiqurasiyası: campaign-logs-traces.json | 17 | — |
| `grafana/provisioning/dashboards/dashboards.yml` | Observability konfiqurasiyası: dashboards.yml | 17 | — |
| `grafana/provisioning/datasources/datasources.yml` | Observability konfiqurasiyası: datasources.yml | 17 | OK |
| `loki/loki-config.yml` | Observability konfiqurasiyası: loki-config.yml | 17 | — |
| `otel-collector/otel-collector.yml` | Observability konfiqurasiyası: otel-collector.yml | 17, 18, 23 | OK |
| `prometheus/prometheus-local.yml` | Observability konfiqurasiyası: prometheus-local.yml | 17, 18 | — |
| `prometheus/prometheus.yml` | Observability konfiqurasiyası: prometheus.yml | 02, 10, 17, 18, 23, 25 | OK |
| `promtail/promtail-config.yml` | Observability konfiqurasiyası: promtail-config.yml | 17, 23 | OK |
| `tempo/tempo.yml` | Observability konfiqurasiyası: tempo.yml | 17, 23 | OK |

### postman

| Fayl | Məsuliyyət | Fəsillər | Yoxlama |
|------------------------------------|------------------------------|------------------------|----------|
| `Insurance Campaign Microservices.postman_collection.json` | Manual API test kolleksiyası | 25 | — |

### security

| Fayl | Məsuliyyət | Fəsillər | Yoxlama |
|------------------------------------|------------------------------|------------------------|----------|
| `pom.xml` | Security modul asılılıqları | 18 | — |
| `main: security/JwtAuthenticationFilter.java` | Bearer token → SecurityContext | 04, 05, 08, 10, 13, 20 | OK |
| `main: security/JwtProperties.java` | app.jwt.* xassələri | 04, 10 | OK |
| `main: security/JwtTokenService.java` | JWT imzalama/doğrulama (RS256/HMAC) | 04, 08, 09, 10, 23 | OK |
| `main: security/PemKeyUtils.java` | PEM açar oxuma | 10, 21 | OK |
| `main: security/ServiceSecurityConfiguration.java` | SecurityFilterChain + method security | 02, 03, 04, 05, 08, 09, 10, 20, 21 | OK |
| `src/main/resources/keys/public.pem` | RSA public key (demo) | 10 | — |

## 26.4 Tapıntı indeksi

Kateqoriyalar fəsil 21.1-dədir. Tam mətn və sübut: `analysis/01-architecture-map.md` və fəsil 21.

| ID | Kateqoriya | Tapıntı (qısa) | Fəsillər |
|-----|--------------|--------------------------------------------------------|-------------------------|
| F01 | D (təhlükəsizlik) | İstənilən anonim istifadəçi `role: ROLE_ADMIN` ilə qeydiyyatdan keçə bilir | 02, 08, 09, 10, 15, 20, 21, 22, 24 |
| F02 | D (təhlükəsizlik) | `forgot-password` xam reset token-i cavabda qaytarır → istifadəçi adını bilən hər kəs şifrəni dəyişə bilər | 09, 10, 20, 21, 24, 25 |
| F03 | R | İstifadəçi enumerasiyası: login "User is not active", forgot-password `404 User not found` | 08, 09, 10, 20, 21, 22, 24, 25 |
| F04 | R | gRPC server (9090) autentifikasiyasızdır, Compose host-a açır | 10, 13, 20, 21, 23, 24 |
| F05 | R | Gateway rate limit açarı `X-Forwarded-For`-a etibar edir → anonim sorğular saxta başlıqla limiti keçə bilər | 10, 15, 21, 24 |
| F06 | R (fakt kitabxanada) | Redis əlçatmaz olduqda `RedisRateLimiter` sorğunu buraxır (fail-open) | 10, 15, 20, 21, 24 |
| F07 | D | `GlobalExceptionHandler` catch-all `Exception` handler-i Spring MVC-nin standart 4xx xətalarını (səhv JSON, yanlış enum, səhv UUID, 405, 404 … | 05, 06, 08, 10, 19, 20, 21, 22, 24, 25 |
| F08 | R | `JwtAuthenticationFilter` etibarsız/vaxtı keçmiş token üçün `JwtException`-ı tutmur; nəticə servlet error-dispatch-dən asılıdır, test yoxdur | 08, 10, 19, 21, 22 |
| F09 | R | Dual-write: DB tranzaksiyası daxilində `kafkaTemplate.send` — commit uğursuz olsa hadisə artıq göndərilmiş ola bilər; send uğursuz olsa hadisə itir … | 06, 11, 20, 21, 22, 24 |
| F10 | R | Kafka əlçatmaz olduqda `KafkaTemplate.send` metadata üçün `max.block.ms` (standart 60 s) qədər bloklaya bilər: həm HTTP filtrində, həm tranzaksiya … | 04, 06, 07, 11, 20, 21, 22, 23, 24 |
| F11 | R (fakt kitabxanada) | log-service `ErrorHandlingDeserializer` istifadə etmir: deserializasiya xətası DLQ-ya getmir, `DefaultErrorHandler.handleOtherException` … | 08, 11, 19, 20, 21, 22, 23, 24 |
| F12 | T | `campaign-events` mövzusunun repozitoriyada consumer-i yoxdur | 11, 20, 21, 22 |
| F13 | R | Cache eviction və tranzaksiya commit-inin sırası zəmanətli deyil; RedisCacheManager transaction-aware deyil → qısa müddətli köhnə statistika … | 04, 15, 20, 21, 23, 24 |
| F14 | M | Təyin olunmuş UUID id + `@Version` yoxdur → `save()`/`saveAndFlush()` `merge` edir: hər insertdən əvvəl SELECT | 06, 07, 09, 20, 21, 22, 24 |
| F15 | M (performans) | `campaign_status_histories.campaign_id` üzrə indeks yoxdur; `idx_campaign_duplicate_lookup` partial unique indekslə eyni sütunları təkrarlayır | 06, 07, 20, 21 |
| F16 | M | `refresh_tokens.token_hash` və `password_reset_tokens.token_hash` üçün həm `unique` constraint, həm ayrıca indeks | 07, 21 |
| F17 | R | Refresh token rotasiyasında optimistik kilid yoxdur → paralel iki refresh eyni token-lə iki yeni cüt ala bilər | 09, 10, 20, 21, 24 |
| F18 | D | Paralel qeydiyyatda DB unique pozuntusu (`DataIntegrityViolationException`) tutulmur → 500 + SQL mesajı | 07, 08, 09, 20, 21, 24, 25 |
| F19 | R | gRPC klientində deadline yoxdur; server tərəfində `@GrpcAdvice` yoxdur → `ResourceNotFoundException` gRPC `UNKNOWN`, dashboard-da 500 olur (404 … | 08, 13, 20, 21, 22, 24, 25 |
| F20 | M | Jackson 2 `ObjectMapper` bean-i yalnız Eureka client-in tranzitiv gətirdiyi `spring-boot-jackson2` (deprecated) sayəsində mövcuddur … | 03, 21, 23 |
| F21 | M | `JsonSerializer`/`JsonDeserializer` spring-kafka 4.0-dan `@Deprecated(forRemoval=true)` | 21 |
| F22 | R | Repozitoriyada demo RSA private key və DB/Grafana şifrələri var | 01, 09, 10, 18, 21, 23, 25 |
| F23 | M | Observation konfiqurasiyası 3 dəfə kopyalanıb (gateway, config, discovery) | 03, 17, 21, 25 |
| F24 | R (performans) | Promtail `trace_id`/`span_id`-ni Loki label-ı edir → yüksək kardinallıq | 17, 21, 23, 24 |
| F25 | T | Yavaş sorğu həddi 5 ms → filtrin aktiv olduğu servislərdə (campaign, log) praktiki olaraq hər sorğu Kafka-ya gedir | 11, 20, 21, 22 |
| F26 | M | log-service `Page<SlowRequestDocument>`-u birbaşa serializə edir (DTO-suz); campaign-service isə `CampaignPageResponse` istifadə edir | 05, 20, 21, 24 |
| F27 | R | Migrasiyalar testlərdə işləmir (`spring.liquibase.enabled=false`, `ddl-auto=create-drop`) → partial unique indeks və sxem validasiyası test edilmir | 07, 19, 21, 24 |
| F28 | M | Testcontainers image-ləri istehsal image-lərindən fərqlidir (postgres:17-alpine vs postgres:latest; cp-kafka 7.6.1 vs apache/kafka:latest) | 18, 19, 21 |
| F29 | M | Docker Compose-da bir çox `depends_on` sağlamlıq şərtisiz; campaign-service-də `restart` siyasəti yoxdur | 07, 17, 18, 21, 23 |
| F30 | H | springdoc 2.8.13 (Boot 3 xətti) Boot 4 / Framework 7 ilə rəsmi uyğunluq xəttindən kənardır; runtime-da yoxlanmayıb | 16, 21 |
| F31 | T | Dashboard → campaign gRPC və auth → log HTTP static ünvan istifadə edir; Eureka yalnız gateway marşrutları və gateway OpenAPI klienti üçün işlənir | 14, 16, 21, 22, 24 |
| F32 | R | Config Server əlçatmaz olduqda `optional:configserver:` sayəsində servislər lokal `application.yml` ilə başlayır; paylanan … | 04, 11, 16, 18, 21, 22, 23, 24 |
| F33 | M | Testlər: campaign approve/deactivate keçidləri, gRPC endpoint, gateway, filtrlər, exception mapping üçün test yoxdur | 19, 21 |
| F34 | R | `@Async` audit icrası (virtual thread) üçün trace kontekst ötürülməsi konfiqurasiya olunmayıb → audit HTTP çağırışları ayrı trace kimi görünə bilər | 04, 17, 21, 24 |
| F35 | T | Access token 120 dəq; logout yalnız refresh token-i ləğv edir, access token vaxtı bitənə qədər etibarlıdır | 09, 10, 20, 21, 22, 24 |
| F36 | D | `approve`/`deactivate` cavabında `updatedAt` köhnə dəyərlə qayıdır: `@PreUpdate` flush zamanı (commit-də) işləyir, mapper isə ondan əvvəl çağırılır | 06, 07, 19, 20, 21, 22 |
| F37 | R | `CampaignEntity`-də `@Version` yoxdur: paralel `approve` və `deactivate` hər ikisi uğurlu ola bilər (lost update), tarixçədə ziddiyyətli iki qeyd və … | 06, 08, 20, 21, 22, 24 |
| F38 | R (observability) | `GlobalExceptionHandler` heç bir istisnanı loglamır; catch-all ilə emal olunan 500 xətalarının stack trace-i log-a düşmür | 08, 17, 21, 22, 23 |
| F39 | M | `AuthAuditLogRequest` üçün heç bir validasiya yoxdur (`@Valid` və constraint-lər yoxdur); `slow-requests` `page/size` yoxlanmır (mənfi `page` → 500) | 08, 21, 25 |
| F40 | R | `/actuator/**` bütün servislərdə `permitAll`; gateway-in öz actuator-u da açıq yollardadır; Compose servis portlarını host-a açır → `prometheus` … | 10, 17, 21, 22, 23, 25 |
| F41 | M | CORS konfiqurasiyası yoxdur; başqa origin-dən brauzer klientləri bloklanır (Swagger UI gateway ilə eyni origin-dədir) | 21 |
| F42 | M | `GET /slow-requests` sıralamasız `findAll(PageRequest)` edir — nəticələr `occurredAt` üzrə deyil, ixtiyari sırada; `SlowRequestDocument` sahələrinin … | 12, 20, 21, 23, 24 |
| F43 | R (əməliyyat) | Elasticsearch indekslərində saxlanma/ILM siyasəti yoxdur; 5 ms həddi ilə `slow-request-logs` məhdudiyyətsiz böyüyür | 12, 21, 22, 23, 24 |
| F44 | T | Gateway JWT/PEM kodu `security` modulundan ayrıca yazılıb (reaktiv gateway servlet asılılıqlarını çəkməsin deyə) — eyni məntiq iki yerdə | 21 |
| F45 | M | `DUPLICATE` statusu heç bir axında təyin olunmur, lakin sorğu, yoxlama, süzgəc və partial indeksdə nəzərə alınır (ölü vəziyyət) | 21 |
| F46 | R | `@Async` audit tapşırığı tranzaksiya daxilində, commit-dən əvvəl verilir → rollback olunan əməliyyat üçün "uğurlu" audit qeydi yarana bilər | 21 |
