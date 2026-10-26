# Tədris kurikulumu (repozitoriya təhlilindən sonra)

Kurikulum asılılıq sırası ilə qurulub: əvvəl sistemin məqsədi, sonra ümumi Spring təməlləri, sonra nüvə
implementasiyası (campaign → persistence → validasiya → auth → security), sonra müstəqil texnologiyalar
(Kafka, Elasticsearch, gRPC, HTTP klient + resilience, Redis, Spring Cloud, observability, konteyner/CI, test),
sonra bunları birləşdirən uçdan-uca axınlar, qiymətləndirmə, dizayn və müsahibə.

| Hissə | Fəsil | Fayl | Asılı olduğu fəsillər | Əsas repozitoriya komponentləri |
|---|---|---|---|---|
| I. Sistemi anlamaq | 1. Kitabı necə oxumalı | `01-how-to-use.md` | — | README, inventar |
| | 2. Sistemin biznes və arxitektura xəritəsi | `02-system-overview.md` | 1 | bütün servislər, compose |
| | 3. Repozitoriya, Maven və build | `03-repository-and-build.md` | 2 | kök və modul `pom.xml`, Dockerfile-lar |
| II. Təməllər | 4. Java 25 və Spring Boot 4 təməlləri | `04-java-spring-foundations.md` | 3 | `common/config`, `security`, `*Application.java` |
| | 5. REST, HTTP və təbəqəli dizayn | `05-rest-layering.md` | 4 | controller-lər, `ApiResponse`, DTO/mapper-lər |
| III. Nüvə | 6. Campaign Service | `06-campaign-service.md` | 5 | `campaign-service/**` |
| | 7. PostgreSQL, JPA/Hibernate, Liquibase | `07-persistence.md` | 6 | entity-lər, repository-lər, changelog-lar |
| | 8. Validasiya və istisnalar | `08-validation-exceptions.md` | 5–7 | DTO-lar, `GlobalExceptionHandler`, security handler-lər |
| | 9. Auth Service | `09-auth-service.md` | 7, 8 | `auth-service/**` |
| | 10. Spring Security və JWT | `10-security-jwt.md` | 9 | `security/**`, gateway security |
| IV. Texnologiyalar | 11. Apache Kafka | `11-kafka.md` | 4, 6 | `KafkaJsonConfiguration`, `SlowRequestLoggingFilter`, `CampaignService`, log-service |
| | 12. Elasticsearch | `12-elasticsearch.md` | 11 | log-service document/repository |
| | 13. gRPC və Protocol Buffers | `13-grpc.md` | 6 | `grpc-contract`, `CampaignGrpcEndpoint`, `CampaignGrpcClient` |
| | 14. HTTP klientləri və Resilience4j | `14-http-clients-resilience.md` | 9 | `LogServiceHttpClient`, `AuthAuditService`, `VirtualThreadConfiguration` |
| | 15. Redis: cache və rate limiting | `15-redis.md` | 6, 16 | `@Cacheable`, gateway `RequestRateLimiter` |
| | 16. Spring Cloud: Gateway, Eureka, Config, OpenAPI | `16-spring-cloud.md` | 10 | `api-gateway/**`, discovery, config |
| | 17. Observability | `17-observability.md` | 16 | observation konfiqurasiyaları, `observability/**` |
| | 18. Konteynerlər, Compose və CI/CD | `18-containers-cicd.md` | 3 | Dockerfile-lar, compose, workflows |
| | 19. Test strategiyası | `19-testing.md` | 6–16 | bütün test qovluqları |
| V. Axınlar | 20. Uçdan-uca biznes axınları | `20-end-to-end-workflows.md` | 6–17 | çoxservisli icra yolları |
| VI. Qiymətləndirmə və dizayn | 21. Mühəndislik keyfiyyətinin qiymətləndirilməsi | `21-engineering-evaluation.md` | 20 | tapıntılar reyestri |
| | 22. Oxşar sistemi necə dizayn etmək | `22-designing-similar-system.md` | 21 | ümumiləşdirilmiş prinsiplər |
| VII. Production və müsahibə | 23. Production runbook və troubleshooting | `23-production-runbook.md` | 17, 20 | uğursuzluq ssenariləri |
| | 24. Sistemin tam izahı və müsahibə sualları | `24-interview.md` | hamısı | — |
| Əlavələr | 25. Lüğət, endpoint kataloqu, konfiqurasiya istinadı, fayl indeksi | `25-appendices.md` | — | — |

## Hər texniki fəslin quruluşu

Məqsəd və problem → nəzəriyyə → daxili mexanizm → repozitoriya implementasiyası (dəqiq fayl:sətir) → icra axını →
konfiqurasiya → praktiki tətbiq (yeni layihədə) → alternativlər və kompromislər → xətalar və troubleshooting →
müsahibə sualları (cavablı) → xülasə. Mövzu tələb etdikdə struktur uyğunlaşdırılır.

## Etiketləmə qaydası

- **[FAKT]** — kodla birbaşa təsdiqlənir (fayl:sətir göstərilir).
- **[NƏTİCƏ]** — implementasiyadan əsaslandırılmış çıxarış (framework davranışı və ya bir neçə faktın birləşməsi).
- **[NAMƏLUM]** — repozitoriyadan müəyyən edilə bilməz (məs., developerin niyyəti, istehsal mühiti).
- **[RİSK]** — potensial defekt və ya təhlükəsizlik/etibarlılıq problemi.
