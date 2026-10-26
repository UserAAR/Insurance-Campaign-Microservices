# Əhatə matrisi (coverage matrix)

> Avtomatik yaradılıb: `build/coverage.py`. Fəsil sütunu fəsil fayllarında faylın tam yolu və ya sinif/fayl adı
> ilə istinadlardan hesablanır. Yoxlama sütunu `audit/verify-refs.json` (verify_refs.py) nəticəsidir:
> həmin fayla edilən `fayl:sətir` istinadlarının və kod çıxarışlarının doğrulanması.

Cəmi fayl: **178**, ən azı bir fəsildə istinad olunan: **178**.

| Fayl | Modul | Məsuliyyət | Əsas elementlər | Rol | Fəsillər | Status | Açıq sual | Yoxlama |
|---|---|---|---|---|---|---|---|---|
| `.github/workflows/ci-cd.yml` | .github | CI/CD: verify → sonar → docker | matrix build, Docker Hub push | CI | 18 | Əhatə olunub | — | OK |
| `.github/workflows/mark-done-on-merge.yml` | .github | Merge olunan PR və bağlı issue-larda `done` label | github-script | Repo avtomatlaşdırması | 18 | Əhatə olunub | — | — |
| `.gitignore` | (kök) | Git istisnaları | target/, logs/, .env | Build | 25 | Əhatə olunub | — | — |
| `README.md` | (kök) | Layihə sənədi | endpoint cədvəlləri, işə salma | Sənəd | 01, 02, 03, 05, 06, 08, 09, 10, 13, 15, 16, 17, 18, 22 | Əhatə olunub | Bəzi iddialar kodla uyğun deyil (inventar §5) | — |
| `api-gateway/Dockerfile` | api-gateway | Image |  | Deploy | 25 | Əhatə olunub | — | — |
| `api-gateway/pom.xml` | api-gateway | Gateway asılılıqları | gateway-server-webflux, redis-reactive | Build | 10, 16 | Əhatə olunub | — | OK |
| `api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/ApiGatewayApplication.java` | api-gateway | Gateway giriş nöqtəsi |  | Platform | 25 | Əhatə olunub | — | — |
| `api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/config/GatewayHttpServerObservationConfiguration.java` | api-gateway | Observation (kopya) |  | Tracing | 25 | Əhatə olunub | F23 | — |
| `api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/config/GatewayLoadBalancerConfiguration.java` | api-gateway | @LoadBalanced WebClient.Builder |  | Kəşf | 16 | Əhatə olunub | — | OK |
| `api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/config/GatewayRateLimiterConfiguration.java` | api-gateway | Rate limit açar resolver-i | requestRateLimiterKeyResolver | Rate limiting | 10, 15, 20 | Əhatə olunub | XFF spoofing (F05) | OK |
| `api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/filter/GatewayJwtAuthenticationFilter.java` | api-gateway | Gateway JWT yoxlaması | filter, getOrder | GlobalFilter | 08, 09, 10, 15, 16, 20 | Əhatə olunub | — | OK |
| `api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/filter/GatewayRateLimitErrorResponseFilter.java` | api-gateway | 429 üçün JSON gövdə | ServerHttpResponseDecorator | GlobalFilter | 03, 04, 05, 15, 16, 20 | Əhatə olunub | — | OK |
| `api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/openapi/GatewayOpenApiController.java` | api-gateway | OpenAPI aqreqasiyası + fallback | openApi | Sənədləşmə | 14, 16, 20, 25 | Əhatə olunub | — | OK |
| `api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/security/GatewayJwtProperties.java` | api-gateway | Gateway app.jwt.* |  | Konfiqurasiya | 16 | Əhatə olunub | — | — |
| `api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/security/GatewayJwtTokenService.java` | api-gateway | Gateway JWT doğrulama | parseClaims | Security | 03, 15, 16 | Əhatə olunub | — | — |
| `api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/security/GatewayPemKeyUtils.java` | api-gateway | Public key oxuma |  | Security | 03, 21 | Əhatə olunub | — | OK |
| `api-gateway/src/main/resources/application.yml` | api-gateway | Marşrutlar, rate limit, springdoc |  | Konfiqurasiya | 15, 16, 20, 23 | Əhatə olunub | — | OK |
| `api-gateway/src/main/resources/keys/public.pem` | api-gateway | RSA public key |  | Konfiqurasiya | 10 | Əhatə olunub | — | — |
| `auth-service/Dockerfile` | auth-service | Çoxmərhələli image build | maven → amazoncorretto:25 | Deploy | 25 | Əhatə olunub | — | — |
| `auth-service/pom.xml` | auth-service | Modul asılılıqları |  | Build | 03 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/AuthServiceApplication.java` | auth-service | AuthServiceApplication |  | Tətbiq kodu | 04, 09, 14 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/client/LogServiceHttpClient.java` | auth-service | LogServiceHttpClient |  | Tətbiq kodu | 04, 09, 14, 20, 21 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/config/AuthConfiguration.java` | auth-service | AuthConfiguration |  | Tətbiq kodu | 04, 09 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/config/LogServiceHttpClientConfiguration.java` | auth-service | LogServiceHttpClientConfiguration |  | Tətbiq kodu | 02, 04, 10, 14, 20, 21 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/controller/AuthController.java` | auth-service | AuthController |  | Tətbiq kodu | 02, 05, 08, 20, 25 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/domain/AppUser.java` | auth-service | AppUser |  | Tətbiq kodu | 02, 05, 07 | Əhatə olunub | — | — |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/domain/AuthAuditLog.java` | auth-service | AuthAuditLog |  | Tətbiq kodu | 02, 14 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/domain/PasswordResetToken.java` | auth-service | PasswordResetToken |  | Tətbiq kodu | 02, 05 | Əhatə olunub | — | — |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/domain/RefreshToken.java` | auth-service | RefreshToken |  | Tətbiq kodu | 02, 05 | Əhatə olunub | — | — |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/dto/request/ForgotPasswordRequest.java` | auth-service | ForgotPasswordRequest |  | Tətbiq kodu | 09 | Əhatə olunub | — | — |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/dto/request/LoginRequest.java` | auth-service | LoginRequest |  | Tətbiq kodu | 08, 09, 20 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/dto/request/LogoutRequest.java` | auth-service | LogoutRequest |  | Tətbiq kodu | 09 | Əhatə olunub | — | — |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/dto/request/RefreshTokenRequest.java` | auth-service | RefreshTokenRequest |  | Tətbiq kodu | 09 | Əhatə olunub | — | — |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/dto/request/RegisterRequest.java` | auth-service | RegisterRequest |  | Tətbiq kodu | 02, 08, 09, 21 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/dto/request/ResetPasswordRequest.java` | auth-service | ResetPasswordRequest |  | Tətbiq kodu | 07, 08 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/dto/response/ForgotPasswordResponse.java` | auth-service | ForgotPasswordResponse |  | Tətbiq kodu | 09 | Əhatə olunub | — | — |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/dto/response/LogoutResponse.java` | auth-service | LogoutResponse |  | Tətbiq kodu | 07, 09 | Əhatə olunub | — | — |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/dto/response/RegisteredUserResponse.java` | auth-service | RegisteredUserResponse |  | Tətbiq kodu | 05, 09 | Əhatə olunub | — | — |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/dto/response/TokenResponse.java` | auth-service | TokenResponse |  | Tətbiq kodu | 05, 09, 20 | Əhatə olunub | — | — |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/entity/AppUserEntity.java` | auth-service | AppUserEntity |  | Tətbiq kodu | 07, 09 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/entity/PasswordResetTokenEntity.java` | auth-service | PasswordResetTokenEntity |  | Tətbiq kodu | 07, 09 | Əhatə olunub | — | — |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/entity/RefreshTokenEntity.java` | auth-service | RefreshTokenEntity |  | Tətbiq kodu | 07, 09 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/enums/UserRole.java` | auth-service | UserRole |  | Tətbiq kodu | 02, 08, 09, 10 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/enums/UserStatus.java` | auth-service | UserStatus |  | Tətbiq kodu | 25 | Əhatə olunub | — | — |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/model/mapper/AuthMapper.java` | auth-service | AuthMapper |  | Tətbiq kodu | 03, 04, 05 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/repository/AppUserRepository.java` | auth-service | AppUserRepository |  | Tətbiq kodu | 25 | Əhatə olunub | — | — |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/repository/PasswordResetTokenRepository.java` | auth-service | PasswordResetTokenRepository |  | Tətbiq kodu | 25 | Əhatə olunub | — | — |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/repository/RefreshTokenRepository.java` | auth-service | RefreshTokenRepository |  | Tətbiq kodu | 07 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/service/AuthAuditService.java` | auth-service | AuthAuditService |  | Tətbiq kodu | 02, 04, 08, 09, 14, 20 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/service/AuthService.java` | auth-service | AuthService |  | Tətbiq kodu | 04, 05, 07, 08, 09, 14, 20, 21 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/util/StringUtils.java` | auth-service | StringUtils |  | Tətbiq kodu | 08, 09 | Əhatə olunub | — | OK |
| `auth-service/src/main/java/com/casestudy/insurancecampaign/auth/util/TokenHashUtils.java` | auth-service | TokenHashUtils |  | Tətbiq kodu | 04, 07, 09 | Əhatə olunub | — | OK |
| `auth-service/src/main/resources/application.yml` | auth-service | Servis konfiqurasiyası | port, datasource, kafka, tracing, app.* | Konfiqurasiya | 02, 04, 09, 14, 20, 25 | Əhatə olunub | — | OK |
| `auth-service/src/main/resources/db/changelog/db.changelog-master.yml` | auth-service | Liquibase changelog | changeSet-lər | Sxem miqrasiyası | 01, 07 | Əhatə olunub | — | OK |
| `auth-service/src/main/resources/keys/private.pem` | auth-service | RSA açarı (demo) |  | Konfiqurasiya | 09, 10 | Əhatə olunub | Private key repozitoriyada (F22) | — |
| `auth-service/src/main/resources/keys/public.pem` | auth-service | RSA açarı (demo) |  | Konfiqurasiya | 25 | Əhatə olunub | — | — |
| `auth-service/src/test/java/com/casestudy/insurancecampaign/auth/base/AbstractBaseServiceTest.java` | auth-service | Test: AbstractBaseServiceTest |  | Test | 19 | Əhatə olunub | — | — |
| `auth-service/src/test/java/com/casestudy/insurancecampaign/auth/base/AbstractRestControllerTest.java` | auth-service | Test: AbstractRestControllerTest |  | Test | 19 | Əhatə olunub | — | OK |
| `auth-service/src/test/java/com/casestudy/insurancecampaign/auth/base/AbstractTestContainerConfiguration.java` | auth-service | Test: AbstractTestContainerConfiguration |  | Test | 07, 13, 15, 19 | Əhatə olunub | — | — |
| `auth-service/src/test/java/com/casestudy/insurancecampaign/auth/controller/AuthControllerTest.java` | auth-service | Test: AuthControllerTest |  | Test | 19 | Əhatə olunub | — | — |
| `auth-service/src/test/java/com/casestudy/insurancecampaign/auth/model/mapper/AuthMapperTest.java` | auth-service | Test: AuthMapperTest |  | Test | 19 | Əhatə olunub | — | — |
| `auth-service/src/test/java/com/casestudy/insurancecampaign/auth/service/AuthAuditServiceTest.java` | auth-service | Test: AuthAuditServiceTest |  | Test | 14, 19 | Əhatə olunub | — | OK |
| `auth-service/src/test/java/com/casestudy/insurancecampaign/auth/service/AuthServiceTest.java` | auth-service | Test: AuthServiceTest |  | Test | 09, 19 | Əhatə olunub | — | OK |
| `campaign-service/Dockerfile` | campaign-service | Çoxmərhələli image build | maven → amazoncorretto:25 | Deploy | 03, 18 | Əhatə olunub | — | OK |
| `campaign-service/pom.xml` | campaign-service | Modul asılılıqları |  | Build | 03, 07, 16, 18 | Əhatə olunub | — | OK |
| `campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/CampaignServiceApplication.java` | campaign-service | CampaignServiceApplication |  | Tətbiq kodu | 03, 04, 06, 15, 22 | Əhatə olunub | — | OK |
| `campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/controller/CampaignController.java` | campaign-service | CampaignController |  | Tətbiq kodu | 02, 04, 05, 06, 08, 10, 20, 25 | Əhatə olunub | — | OK |
| `campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/domain/Campaign.java` | campaign-service | Campaign |  | Tətbiq kodu | 00, 01, 02, 03, 04, 05, 06, 11, 13, 17, 19, 20, 22, 25 | Əhatə olunub | — | OK |
| `campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/domain/CampaignCategory.java` | campaign-service | CampaignCategory |  | Tətbiq kodu | 02, 06, 07, 08, 11 | Əhatə olunub | — | OK |
| `campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/domain/CampaignStatus.java` | campaign-service | CampaignStatus |  | Tətbiq kodu | 02, 05, 06, 07, 11, 13, 21, 22 | Əhatə olunub | — | OK |
| `campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/dto/CampaignHistoryResponse.java` | campaign-service | CampaignHistoryResponse |  | Tətbiq kodu | 06, 13 | Əhatə olunub | — | — |
| `campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/dto/CampaignPageResponse.java` | campaign-service | CampaignPageResponse |  | Tətbiq kodu | 05, 06, 20, 24 | Əhatə olunub | — | OK |
| `campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/dto/CampaignResponse.java` | campaign-service | CampaignResponse |  | Tətbiq kodu | 05, 06, 20 | Əhatə olunub | — | — |
| `campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/dto/CampaignStatisticsResponse.java` | campaign-service | CampaignStatisticsResponse |  | Tətbiq kodu | 06, 13, 20 | Əhatə olunub | — | — |
| `campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/dto/CreateCampaignRequest.java` | campaign-service | CreateCampaignRequest |  | Tətbiq kodu | 04, 05, 06, 08, 20 | Əhatə olunub | — | OK |
| `campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/entity/CampaignEntity.java` | campaign-service | CampaignEntity |  | Tətbiq kodu | 03, 05, 06, 07, 19, 21, 22 | Əhatə olunub | — | OK |
| `campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/entity/CampaignStatusHistoryEntity.java` | campaign-service | CampaignStatusHistoryEntity |  | Tətbiq kodu | 05, 06, 07, 22 | Əhatə olunub | — | OK |
| `campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/grpc/CampaignGrpcEndpoint.java` | campaign-service | CampaignGrpcEndpoint |  | Tətbiq kodu | 04, 05, 06, 13, 20 | Əhatə olunub | — | OK |
| `campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/mapper/CampaignMapper.java` | campaign-service | CampaignMapper |  | Tətbiq kodu | 04, 05, 06 | Əhatə olunub | — | OK |
| `campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/repository/CampaignRepository.java` | campaign-service | CampaignRepository |  | Tətbiq kodu | 04, 05, 06, 07, 19, 20, 21 | Əhatə olunub | — | OK |
| `campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/repository/CampaignStatusHistoryRepository.java` | campaign-service | CampaignStatusHistoryRepository |  | Tətbiq kodu | 04, 06 | Əhatə olunub | — | OK |
| `campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/service/CampaignService.java` | campaign-service | CampaignService |  | Tətbiq kodu | 01, 02, 04, 05, 06, 07, 08, 11, 13, 15, 19, 20, 21, 22, 23 | Əhatə olunub | — | OK |
| `campaign-service/src/main/resources/application.yml` | campaign-service | Servis konfiqurasiyası | port, datasource, kafka, tracing, app.* | Konfiqurasiya | 05, 06, 07, 11, 13, 15, 16, 17, 20, 21 | Əhatə olunub | — | OK |
| `campaign-service/src/main/resources/db/changelog/db.changelog-master.yml` | campaign-service | Liquibase changelog | changeSet-lər | Sxem miqrasiyası | 06, 07, 20, 21, 23 | Əhatə olunub | — | OK |
| `campaign-service/src/test/java/com/casestudy/insurancecampaign/campaign/base/AbstractBaseServiceTest.java` | campaign-service | Test: AbstractBaseServiceTest |  | Test | 19 | Əhatə olunub | — | OK |
| `campaign-service/src/test/java/com/casestudy/insurancecampaign/campaign/base/AbstractRestControllerTest.java` | campaign-service | Test: AbstractRestControllerTest |  | Test | 19 | Əhatə olunub | — | OK |
| `campaign-service/src/test/java/com/casestudy/insurancecampaign/campaign/base/AbstractTestContainerConfiguration.java` | campaign-service | Test: AbstractTestContainerConfiguration |  | Test | 07, 13, 15, 19 | Əhatə olunub | — | OK |
| `campaign-service/src/test/java/com/casestudy/insurancecampaign/campaign/controller/CampaignControllerTest.java` | campaign-service | Test: CampaignControllerTest |  | Test | 08, 10, 19 | Əhatə olunub | — | OK |
| `campaign-service/src/test/java/com/casestudy/insurancecampaign/campaign/service/CampaignServiceTest.java` | campaign-service | Test: CampaignServiceTest |  | Test | 02, 04, 06, 19 | Əhatə olunub | — | OK |
| `campaign-service/src/test/resources/keys/private.pem` | campaign-service | Test RSA açarı |  | Test | 25 | Əhatə olunub | — | — |
| `campaign-service/src/test/resources/keys/public.pem` | campaign-service | Test RSA açarı |  | Test | 25 | Əhatə olunub | — | — |
| `common/pom.xml` | common | Ortaq modulun asılılıqları | spring-kafka optional | Build | 02, 03, 11, 18, 20 | Əhatə olunub | — | OK |
| `common/src/main/java/com/casestudy/insurancecampaign/common/api/ApiError.java` | common | Xəta cavab zərfi | of(), validation() | HTTP kontrakt | 02, 05, 08, 10, 21, 23 | Əhatə olunub | — | OK |
| `common/src/main/java/com/casestudy/insurancecampaign/common/api/ApiResponse.java` | common | Uğurlu cavab zərfi | success() | HTTP kontrakt | 02, 04, 05, 08, 12, 13, 20, 21 | Əhatə olunub | — | OK |
| `common/src/main/java/com/casestudy/insurancecampaign/common/config/KafkaJsonConfiguration.java` | common | Kafka producer (String açar, JSON dəyər) | producerFactory, kafkaTemplate | Kafka şərti konfiqurasiya | 03, 04, 06, 11, 17, 23 | Əhatə olunub | — | OK |
| `common/src/main/java/com/casestudy/insurancecampaign/common/config/VirtualThreadConfiguration.java` | common | @EnableAsync + virtual thread executor | applicationTaskExecutor | Asinxron icra | 04, 20 | Əhatə olunub | Trace kontekst ötürülmür (F34) | OK |
| `common/src/main/java/com/casestudy/insurancecampaign/common/event/CampaignStatusChangedEvent.java` | common | Kampaniya status hadisəsi | record | Kafka mesajı | 02, 06, 11 | Əhatə olunub | Consumer yoxdur (F12) | — |
| `common/src/main/java/com/casestudy/insurancecampaign/common/event/SlowRequestLogEvent.java` | common | Yavaş sorğu hadisəsi | record | Kafka mesajı | 02, 04, 11, 19, 20 | Əhatə olunub | — | — |
| `common/src/main/java/com/casestudy/insurancecampaign/common/exception/BusinessException.java` | common | Biznes istisna bazası | ErrorCode | Xəta modeli | 05, 07, 08, 09, 20, 21, 22 | Əhatə olunub | — | OK |
| `common/src/main/java/com/casestudy/insurancecampaign/common/exception/DuplicateCampaignException.java` | common | Təkrar kampaniya | DUPLICATE_CAMPAIGN | Xəta modeli | 06, 07, 08, 21 | Əhatə olunub | — | — |
| `common/src/main/java/com/casestudy/insurancecampaign/common/exception/ErrorCode.java` | common | Xəta kodları | 8 dəyər | Xəta kontraktı | 05, 07, 08, 09, 10, 21 | Əhatə olunub | — | OK |
| `common/src/main/java/com/casestudy/insurancecampaign/common/exception/GlobalExceptionHandler.java` | common | Mərkəzi istisna xəritələnməsi | 5 @ExceptionHandler | MVC xəta emalı | 02, 03, 04, 05, 08, 09, 10, 13, 17, 19, 20, 21 | Əhatə olunub | Catch-all 4xx-ləri 500 edir (F07) | OK |
| `common/src/main/java/com/casestudy/insurancecampaign/common/exception/InvalidStatusTransitionException.java` | common | Yanlış status keçidi | INVALID_STATUS_TRANSITION | Xəta modeli | 06, 08, 21, 22 | Əhatə olunub | — | — |
| `common/src/main/java/com/casestudy/insurancecampaign/common/exception/ResourceNotFoundException.java` | common | Resurs tapılmadı | RESOURCE_NOT_FOUND | Xəta modeli | 05, 06, 08, 09, 13, 20, 21, 22 | Əhatə olunub | — | — |
| `common/src/main/java/com/casestudy/insurancecampaign/common/observability/HttpServerObservationConfiguration.java` | common | Span adı və actuator filtri | ObservationPredicate, ObservationFilter | Tracing | 04, 17 | Əhatə olunub | — | OK |
| `common/src/main/java/com/casestudy/insurancecampaign/common/web/SlowRequestLoggingFilter.java` | common | Yavaş sorğuları Kafka-ya göndərir | doFilterInternal, shouldNotFilter | Servlet filter | 03, 04, 05, 11, 16, 19, 20, 21 | Əhatə olunub | Kafka bloklaması (F10) | OK |
| `config-server/Dockerfile` | config-server | Image |  | Deploy | 25 | Əhatə olunub | — | — |
| `config-server/pom.xml` | config-server | Config server asılılıqları |  | Build | 17 | Əhatə olunub | — | OK |
| `config-server/src/main/java/com/casestudy/insurancecampaign/config/ConfigServerApplication.java` | config-server | Config Server | @EnableConfigServer | Platform | 04 | Əhatə olunub | — | — |
| `config-server/src/main/java/com/casestudy/insurancecampaign/config/observability/ConfigServerHttpServerObservationConfiguration.java` | config-server | Observation (kopya) |  | Tracing | 25 | Əhatə olunub | Təkrar kod (F23) | — |
| `config-server/src/main/resources/application.yml` | config-server | native profil, search-locations |  | Konfiqurasiya | 04, 16 | Əhatə olunub | — | OK |
| `config-server/src/main/resources/config/application.yml` | config-server | Bütün klientlərə paylanan konfiqurasiya | kafka consumer, app.* | Mərkəzi konfiqurasiya | 04, 16 | Əhatə olunub | F32 | OK |
| `dashboard-service/Dockerfile` | dashboard-service | Çoxmərhələli image build | maven → amazoncorretto:25 | Deploy | 25 | Əhatə olunub | — | — |
| `dashboard-service/pom.xml` | dashboard-service | Modul asılılıqları |  | Build | 25 | Əhatə olunub | — | — |
| `dashboard-service/src/main/java/com/casestudy/insurancecampaign/dashboard/DashboardServiceApplication.java` | dashboard-service | DashboardServiceApplication |  | Tətbiq kodu | 25 | Əhatə olunub | — | — |
| `dashboard-service/src/main/java/com/casestudy/insurancecampaign/dashboard/client/CampaignGrpcClient.java` | dashboard-service | CampaignGrpcClient |  | Tətbiq kodu | 04, 13, 20, 21 | Əhatə olunub | — | OK |
| `dashboard-service/src/main/java/com/casestudy/insurancecampaign/dashboard/controller/DashboardController.java` | dashboard-service | DashboardController |  | Tətbiq kodu | 02, 05, 10, 20, 21, 25 | Əhatə olunub | — | OK |
| `dashboard-service/src/main/java/com/casestudy/insurancecampaign/dashboard/dto/DashboardCampaignHistoryItemResponse.java` | dashboard-service | DashboardCampaignHistoryItemResponse |  | Tətbiq kodu | 13 | Əhatə olunub | — | — |
| `dashboard-service/src/main/java/com/casestudy/insurancecampaign/dashboard/dto/DashboardCampaignHistoryResponse.java` | dashboard-service | DashboardCampaignHistoryResponse |  | Tətbiq kodu | 13 | Əhatə olunub | — | — |
| `dashboard-service/src/main/java/com/casestudy/insurancecampaign/dashboard/dto/DashboardStatisticsResponse.java` | dashboard-service | DashboardStatisticsResponse |  | Tətbiq kodu | 13 | Əhatə olunub | — | — |
| `dashboard-service/src/main/resources/application.yml` | dashboard-service | Servis konfiqurasiyası | port, datasource, kafka, tracing, app.* | Konfiqurasiya | 02, 13, 20 | Əhatə olunub | — | OK |
| `dashboard-service/src/test/java/com/casestudy/insurancecampaign/dashboard/base/AbstractBaseServiceTest.java` | dashboard-service | Test: AbstractBaseServiceTest |  | Test | 19 | Əhatə olunub | — | — |
| `dashboard-service/src/test/java/com/casestudy/insurancecampaign/dashboard/base/AbstractRestControllerTest.java` | dashboard-service | Test: AbstractRestControllerTest |  | Test | 19 | Əhatə olunub | — | — |
| `dashboard-service/src/test/java/com/casestudy/insurancecampaign/dashboard/base/AbstractTestContainerConfiguration.java` | dashboard-service | Test: AbstractTestContainerConfiguration |  | Test | 07, 13, 15, 19 | Əhatə olunub | — | OK |
| `dashboard-service/src/test/java/com/casestudy/insurancecampaign/dashboard/client/CampaignGrpcClientTest.java` | dashboard-service | Test: CampaignGrpcClientTest |  | Test | 04, 13, 19 | Əhatə olunub | — | OK |
| `dashboard-service/src/test/java/com/casestudy/insurancecampaign/dashboard/controller/DashboardControllerTest.java` | dashboard-service | Test: DashboardControllerTest |  | Test | 03, 19 | Əhatə olunub | — | OK |
| `dashboard-service/src/test/resources/keys/private.pem` | dashboard-service | Test RSA açarı |  | Test | 25 | Əhatə olunub | — | — |
| `dashboard-service/src/test/resources/keys/public.pem` | dashboard-service | Test RSA açarı |  | Test | 25 | Əhatə olunub | — | — |
| `discovery-server/Dockerfile` | discovery-server | Image |  | Deploy | 25 | Əhatə olunub | — | — |
| `discovery-server/pom.xml` | discovery-server | Eureka server asılılıqları |  | Build | 25 | Əhatə olunub | — | — |
| `discovery-server/src/main/java/com/casestudy/insurancecampaign/discovery/DiscoveryServerApplication.java` | discovery-server | Eureka Server | @EnableEurekaServer | Platform | 04 | Əhatə olunub | — | — |
| `discovery-server/src/main/java/com/casestudy/insurancecampaign/discovery/observability/DiscoveryServerHttpServerObservationConfiguration.java` | discovery-server | Observation (kopya) |  | Tracing | 25 | Əhatə olunub | Təkrar kod (F23) | — |
| `discovery-server/src/main/resources/application.yml` | discovery-server | Eureka konfiqurasiyası | self-preservation off | Konfiqurasiya | 16 | Əhatə olunub | — | OK |
| `docker-compose-local.yml` | (kök) | Yalnız infrastruktur (servislər IDE-dən) | postgres, redis, kafka, es, observability | Runtime | 01, 18 | Əhatə olunub | — | OK |
| `docker-compose.yml` | (kök) | Tam stek (7 tətbiq + infrastruktur) | servislər, env, volume, network | Runtime | 01, 02, 04, 10, 11, 12, 13, 15, 16, 17, 18, 20, 21, 23, 25 | Əhatə olunub | Healthcheck şərtləri natamam (F29) | OK |
| `grpc-contract/pom.xml` | grpc-contract | protoc + grpc-java generasiyası | protobuf-maven-plugin | Build | 03, 18 | Əhatə olunub | — | OK |
| `grpc-contract/src/main/proto/campaign.proto` | grpc-contract | gRPC kontraktı | CampaignGrpcService, 6 mesaj | Servis kontraktı | 00, 01, 02, 03, 04, 05, 06, 07, 08, 09, 10, 11, 12, 13, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25 | Əhatə olunub | — | OK |
| `log-service/Dockerfile` | log-service | Çoxmərhələli image build | maven → amazoncorretto:25 | Deploy | 25 | Əhatə olunub | — | — |
| `log-service/pom.xml` | log-service | Modul asılılıqları |  | Build | 25 | Əhatə olunub | — | — |
| `log-service/src/main/java/com/casestudy/insurancecampaign/log/LogServiceApplication.java` | log-service | LogServiceApplication |  | Tətbiq kodu | 25 | Əhatə olunub | — | — |
| `log-service/src/main/java/com/casestudy/insurancecampaign/log/config/KafkaDlqConfiguration.java` | log-service | KafkaDlqConfiguration |  | Tətbiq kodu | 04, 11, 17, 20, 21 | Əhatə olunub | — | OK |
| `log-service/src/main/java/com/casestudy/insurancecampaign/log/controller/AuthAuditLogController.java` | log-service | AuthAuditLogController |  | Tətbiq kodu | 02, 08, 10, 12, 20, 25 | Əhatə olunub | — | OK |
| `log-service/src/main/java/com/casestudy/insurancecampaign/log/controller/SlowRequestLogController.java` | log-service | SlowRequestLogController |  | Tətbiq kodu | 02, 05, 10, 12, 19, 20, 21, 25 | Əhatə olunub | — | OK |
| `log-service/src/main/java/com/casestudy/insurancecampaign/log/model/document/AuthAuditLogDocument.java` | log-service | AuthAuditLogDocument |  | Tətbiq kodu | 12, 25 | Əhatə olunub | — | OK |
| `log-service/src/main/java/com/casestudy/insurancecampaign/log/model/document/SlowRequestDocument.java` | log-service | SlowRequestDocument |  | Tətbiq kodu | 05, 11, 12, 20, 23 | Əhatə olunub | — | OK |
| `log-service/src/main/java/com/casestudy/insurancecampaign/log/model/dto/request/AuthAuditLogRequest.java` | log-service | AuthAuditLogRequest |  | Tətbiq kodu | 08, 14, 25 | Əhatə olunub | — | OK |
| `log-service/src/main/java/com/casestudy/insurancecampaign/log/repository/AuthAuditLogRepository.java` | log-service | AuthAuditLogRepository |  | Tətbiq kodu | 12 | Əhatə olunub | — | — |
| `log-service/src/main/java/com/casestudy/insurancecampaign/log/repository/SlowRequestLogRepository.java` | log-service | SlowRequestLogRepository |  | Tətbiq kodu | 12 | Əhatə olunub | — | — |
| `log-service/src/main/java/com/casestudy/insurancecampaign/log/service/AuthAuditLogService.java` | log-service | AuthAuditLogService |  | Tətbiq kodu | 12, 20 | Əhatə olunub | — | OK |
| `log-service/src/main/java/com/casestudy/insurancecampaign/log/service/SlowRequestLogConsumer.java` | log-service | SlowRequestLogConsumer |  | Tətbiq kodu | 04, 11, 12, 20 | Əhatə olunub | — | OK |
| `log-service/src/main/resources/application.yml` | log-service | Servis konfiqurasiyası | port, datasource, kafka, tracing, app.* | Konfiqurasiya | 11, 12 | Əhatə olunub | — | OK |
| `log-service/src/test/java/com/casestudy/insurancecampaign/log/base/AbstractBaseServiceTest.java` | log-service | Test: AbstractBaseServiceTest |  | Test | 19 | Əhatə olunub | — | — |
| `log-service/src/test/java/com/casestudy/insurancecampaign/log/base/AbstractRestControllerTest.java` | log-service | Test: AbstractRestControllerTest |  | Test | 19 | Əhatə olunub | — | — |
| `log-service/src/test/java/com/casestudy/insurancecampaign/log/base/AbstractTestContainerConfiguration.java` | log-service | Test: AbstractTestContainerConfiguration |  | Test | 07, 13, 15, 19 | Əhatə olunub | — | — |
| `log-service/src/test/java/com/casestudy/insurancecampaign/log/controller/AuthAuditLogControllerTest.java` | log-service | Test: AuthAuditLogControllerTest |  | Test | 19 | Əhatə olunub | — | — |
| `log-service/src/test/java/com/casestudy/insurancecampaign/log/controller/SlowRequestLogControllerTest.java` | log-service | Test: SlowRequestLogControllerTest |  | Test | 05, 12, 19 | Əhatə olunub | — | OK |
| `log-service/src/test/java/com/casestudy/insurancecampaign/log/service/AuthAuditLogServiceTest.java` | log-service | Test: AuthAuditLogServiceTest |  | Test | 19 | Əhatə olunub | — | — |
| `log-service/src/test/java/com/casestudy/insurancecampaign/log/service/SlowRequestLogConsumerTest.java` | log-service | Test: SlowRequestLogConsumerTest |  | Test | 19 | Əhatə olunub | — | — |
| `log-service/src/test/resources/keys/private.pem` | log-service | Test RSA açarı |  | Test | 25 | Əhatə olunub | — | — |
| `log-service/src/test/resources/keys/public.pem` | log-service | Test RSA açarı |  | Test | 25 | Əhatə olunub | — | — |
| `observability/grafana/dashboards/campaign-logs-traces.json` | observability | Observability konfiqurasiyası: campaign-logs-traces.json |  | Runtime (observability) | 17 | Əhatə olunub | — | — |
| `observability/grafana/provisioning/dashboards/dashboards.yml` | observability | Observability konfiqurasiyası: dashboards.yml |  | Runtime (observability) | 17 | Əhatə olunub | — | — |
| `observability/grafana/provisioning/datasources/datasources.yml` | observability | Observability konfiqurasiyası: datasources.yml |  | Runtime (observability) | 17 | Əhatə olunub | — | OK |
| `observability/loki/loki-config.yml` | observability | Observability konfiqurasiyası: loki-config.yml |  | Runtime (observability) | 17 | Əhatə olunub | — | — |
| `observability/otel-collector/otel-collector.yml` | observability | Observability konfiqurasiyası: otel-collector.yml |  | Runtime (observability) | 17, 18, 23 | Əhatə olunub | — | OK |
| `observability/prometheus/prometheus-local.yml` | observability | Observability konfiqurasiyası: prometheus-local.yml |  | Runtime (observability) | 17, 18 | Əhatə olunub | — | — |
| `observability/prometheus/prometheus.yml` | observability | Observability konfiqurasiyası: prometheus.yml |  | Runtime (observability) | 02, 10, 17, 18, 23, 25 | Əhatə olunub | — | OK |
| `observability/promtail/promtail-config.yml` | observability | Observability konfiqurasiyası: promtail-config.yml |  | Runtime (observability) | 17, 23 | Əhatə olunub | — | OK |
| `observability/tempo/tempo.yml` | observability | Observability konfiqurasiyası: tempo.yml |  | Runtime (observability) | 17, 23 | Əhatə olunub | — | OK |
| `pom.xml` | (kök) | Parent POM, modul siyahısı, BOM-lar, plugin-lər | dependencyManagement, surefire/failsafe, jacoco, sonar | Build | 01, 02, 03, 13, 18 | Əhatə olunub | — | OK |
| `postman/Insurance Campaign Microservices.postman_collection.json` | postman | Manual API test kolleksiyası | gateway və birbaşa sorğular | Test aləti | 25 | Əhatə olunub | — | — |
| `security/pom.xml` | security | Security modul asılılıqları | jjwt | Build | 18 | Əhatə olunub | — | — |
| `security/src/main/java/com/casestudy/insurancecampaign/security/JwtAuthenticationFilter.java` | security | Bearer token → SecurityContext | doFilterInternal | Security filter | 04, 05, 08, 10, 13, 20 | Əhatə olunub | JwtException tutulmur (F08) | OK |
| `security/src/main/java/com/casestudy/insurancecampaign/security/JwtProperties.java` | security | app.jwt.* xassələri | rsaEnabled() | Konfiqurasiya | 04, 10 | Əhatə olunub | — | OK |
| `security/src/main/java/com/casestudy/insurancecampaign/security/JwtTokenService.java` | security | JWT imzalama/doğrulama (RS256/HMAC) | generateToken, parseClaims | Security | 04, 08, 09, 10, 23 | Əhatə olunub | — | OK |
| `security/src/main/java/com/casestudy/insurancecampaign/security/PemKeyUtils.java` | security | PEM açar oxuma | readPublicKey, readPrivateKey | Security | 10, 21 | Əhatə olunub | — | OK |
| `security/src/main/java/com/casestudy/insurancecampaign/security/ServiceSecurityConfiguration.java` | security | SecurityFilterChain + method security | securityFilterChain, writeError | Security | 02, 03, 04, 05, 08, 09, 10, 20, 21 | Əhatə olunub | — | OK |
| `security/src/main/resources/keys/public.pem` | security | RSA public key (demo) |  | Konfiqurasiya | 10 | Əhatə olunub | — | — |
