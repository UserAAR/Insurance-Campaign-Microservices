# 23. İstehsal mühəndisliyi: əməliyyat və troubleshooting runbook-u

Bu fəsil sistemi işlədən mühəndis üçündür: "nə baş verir və nə etməliyəm?" Repozitoriyada yalnız Docker Compose
yerləşdirməsi var [FAKT], ona görə əmrlər Compose-a uyğundur. İstehsal mühiti (Kubernetes, bulud) üçün eyni məntiq
`kubectl logs/exec`-ə çevrilir. Əmrlərdə şifrə və token **göstərilmir** — `<...>` yer tutucuları mühitinizin secret-ləri
ilə əvəz olunmalıdır.

## 23.1 Sistem xəritəsi: portlar və sağlamlıq yoxlamaları

| Komponent | Konteyner | Host portu | Sağlamlıq yoxlaması | Compose healthcheck |
|---|---|---|---|---|
| api-gateway | `api-gateway` | 8080 | `GET /actuator/health` | Yoxdur |
| auth-service | `auth-service` | 8081 | `GET /actuator/health` | Yoxdur |
| campaign-service | `campaign-service` | 8082, gRPC 9090 | `GET /actuator/health` | Yoxdur |
| dashboard-service | `dashboard-service` | 8083 | `GET /actuator/health` | Yoxdur |
| log-service | `log-service` | 8084 | `GET /actuator/health` | Yoxdur |
| discovery-server | `discovery-server` | 8761 | Eureka UI | Yoxdur |
| config-server | `config-server` | 8888 | `GET /actuator/health` | Yoxdur |
| PostgreSQL (auth / campaign) | `auth-db` / `campaign-db` | 5433 / 5434 | `pg_isready` | Var (`docker-compose.yml:103-107`, `122-126`) |
| Redis | `redis` | 6379 | `redis-cli ping` | Var (`docker-compose.yml:291-295`) |
| Kafka | `kafka` | 9092 | `kafka-topics.sh --list` | Var (`docker-compose.yml:326-331`) |
| Elasticsearch | `elasticsearch` | 9200 | `_cluster/health` | Var (`docker-compose.yml:363-368`) |
| Prometheus / Grafana | `prometheus` / `grafana` | 9091 / 3000 | UI | — |
| Tempo / Loki / OTel Collector | `tempo` / `loki` / `otel-collector` | 3200 / 3100 / 4317, 4318 | — | — |
| Kafka UI | `kafka-ui` | 8090 | UI | — |

[FAKT] Tətbiq servislərinin healthcheck-i yoxdur, ona görə `depends_on: condition: service_healthy` onlar üçün işləmir
(F29). Actuator `health,info,prometheus,metrics` açıqdır və `permitAll`-dur (F40) — istehsalda ayrı management portu və
şəbəkə izolyasiyası lazımdır.

## 23.2 Nəyi izləmək lazımdır: siqnallar

Prometheus bütün beş tətbiq servisini `/actuator/prometheus` üzərindən toplayır (`observability/prometheus/prometheus.yml:5-24`).
discovery-server və config-server scrape olunmur [FAKT].

| Siqnal | Metrik (Micrometer adı → Prometheus) | Niyə vacibdir |
|---|---|---|
| Sorğu sürəti, xəta, gecikmə (RED) | `http_server_requests_seconds_count/sum/bucket` (`uri`, `status`, `method`) | Əsas SLI |
| Gateway 429 payı | `http_server_requests_seconds_count{status="429"}` (gateway) | Rate limit həddinin düzgünlüyü |
| DB bağlantı hovuzu | `hikaricp_connections_active`, `_pending`, `_timeout_total` | Hovuz tükənməsi gecikmə kimi görünür |
| JVM | `jvm_memory_used_bytes`, `jvm_gc_pause_seconds`, `jvm_threads_live_threads` | Yaddaş sızması, GC təzyiqi |
| Circuit breaker | `resilience4j_circuitbreaker_state` (auth-service; `resilience4j-micrometer` classpath-dadır) [NƏTİCƏ] | log-service problemlərinin erkən siqnalı |
| Kafka consumer | `kafka_consumer_fetch_manager_records_lag_max` [NƏTİCƏ: Boot-un consumer factory-si Micrometer listener əlavə edir] | log-service geri qalırmı? |
| Kafka producer | **Yoxdur** — `ProducerFactory` əl ilə yaradılıb (`KafkaJsonConfiguration.java:9-17`) [NƏTİCƏ] | Producer xətaları görünmür |
| DLQ | Ayrıca metrik yoxdur; yalnız ERROR log `DLQ message received` | Alert log əsasında qurulmalıdır |
| Elasticsearch | Scrape olunmur | `_cluster/health`, disk istifadəsi əl ilə |

```yaml
# İllüstrativ Prometheus alert qaydaları (repozitoriyada yoxdur)
groups:
- name: campaign-platform
  rules:
  - alert: High5xxRate
    expr: sum by (job) (rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
          / sum by (job) (rate(http_server_requests_seconds_count[5m])) > 0.02
    for: 5m
  - alert: SlowP95
    expr: histogram_quantile(0.95, sum by (job, le) (rate(http_server_requests_seconds_bucket[5m]))) > 1
    for: 10m
  - alert: HikariPending
    expr: max by (job) (hikaricp_connections_pending) > 0
    for: 2m
  - alert: AuditCircuitOpen
    expr: max(resilience4j_circuitbreaker_state{name="logServiceHttpClient",state="open"}) == 1
    for: 1m
  - alert: LogConsumerLag
    expr: max(kafka_consumer_fetch_manager_records_lag_max{client_id=~".*log-service.*"}) > 1000
    for: 10m
```

`histogram_quantile` üçün bucket-lər lazımdır; Boot standart olaraq `http.server.requests` üçün histogram bucket-ləri
dərc etmir — `management.metrics.distribution.percentiles-histogram.http.server.requests: true` əlavə edilməlidir
[NƏTİCƏ]. Repozitoriyada bu xassə yoxdur.

## 23.3 Diaqnostika alətləri

```bash
# Konteyner vəziyyəti və loglar
docker compose ps
docker compose logs -f --since=10m campaign-service
docker compose logs campaign-service | grep -E "ERROR|WARN" | tail -50

# Servis sağlamlığı
curl -s localhost:8082/actuator/health
curl -s localhost:8082/actuator/metrics/hikaricp.connections.active

# Eureka reyestri (gateway-in görə bildiyi instanslar)
curl -s -H 'Accept: application/json' localhost:8761/eureka/apps | head

# Kafka: mövzular, consumer group lag, DLQ məzmunu
docker exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
docker exec kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --describe --group log-service
docker exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic log-events.DLQ --from-beginning --max-messages 5 --property print.headers=true

# Redis: cache və rate limiter açarları (KEYS əvəzinə SCAN — istehsalda KEYS bloklayır)
docker exec redis redis-cli --scan --pattern 'campaign-statistics*'
docker exec redis redis-cli --scan --pattern 'request_rate_limiter*' | head
docker exec redis redis-cli info memory | grep used_memory_human

# Elasticsearch: indekslər, sağlamlıq, son sənədlər
curl -s 'localhost:9200/_cat/indices?v'
curl -s 'localhost:9200/_cluster/health?pretty'
curl -s 'localhost:9200/slow-request-logs/_search?size=3&sort=occurredAt:desc&pretty'

# PostgreSQL (şifrə mühitdən daxil edilir, burada göstərilmir)
docker exec -it campaign-db psql -U <campaign_db_user> -d campaign_db \
  -c "select status, count(*) from campaigns group by status;"
docker exec -it campaign-db psql -U <campaign_db_user> -d campaign_db \
  -c "select id, filename, exectype, dateexecuted from databasechangelog order by orderexecuted;"
```

**Loki (LogQL):** `{service="campaign-service", level="ERROR"}`; traceId ilə bütün servislər üzrə:
`{trace_id="<traceId>"}`. Bu sorğular Promtail-in `service`, `level`, `trace_id`, `span_id` label-larına əsaslanır
(`observability/promtail/promtail-config.yml:15-49`); `trace_id`-nin label olması rahatdır, lakin yüksək kardinallıq
yaradır (F24) — düzəlişdən sonra `{service=~".+"} |= "<traceId>"` istifadə olunmalıdır.
**Tempo:** Grafana Explore → Tempo → TraceQL `{ resource.service.name = "api-gateway" && status = error }`.

[FAKT] `occurredAt` açıq `Date` mapping-ə malikdir (`SlowRequestDocument.java:25-26`), ona görə yuxarıdakı sıralama
işləyir; digər sahələr dinamik mapping ilə yaranır (F42) — `GET slow-request-logs/_mapping` ilə tipləri yoxlayın.

## 23.4 Hadisə playbook-ları

Hər playbook: **simptom → ehtimal olunan səbəb → yoxlama → hərəkət**.

### P1. Bütün sorğular 429 qaytarır

- **Səbəb:** `requestedTokens = burstCapacity = 5`, `replenishRate = 1` → bir açar üçün 5 saniyədə 1 sorğu (fəsil 15.6).
  Bir NAT arxasındakı bütün anonim istifadəçilər eyni `ip:` açarını paylaşır.
- **Yoxlama:** cavabda `X-RateLimit-Remaining`, `Retry-After` başlıqları; `redis-cli --scan --pattern
  'request_rate_limiter*'`.
- **Hərəkət:** `RATE_LIMITER_REQUESTED_TOKENS=1`, `RATE_LIMITER_BURST_CAPACITY` artırmaq
  (`api-gateway/src/main/resources/application.yml:24-26`) və gateway-i yenidən başlatmaq.

### P2. Bütün qorunan sorğular 401 qaytarır

- **Səbəblər:** (a) auth-service-in private key-i ilə gateway/servislərin public key-i uyğun gəlmir (açar rotasiyası
  yarımçıq); (b) `app.jwt.issuer` fərqlidir; (c) token vaxtı keçib (120 dəq); (d) saat fərqi (clock skew).
- **Yoxlama:** gateway 401-i **boş gövdə** ilə qaytarır, servis isə `ApiError` JSON-u ilə — bu, rədd edən qatı göstərir.
  Token-in payload hissəsini base64 ilə açıb `iss`, `exp` dəyərlərinə baxın (imzanı paylaşmayın).
- **Hərəkət:** açar cütünü bütün komponentlərdə eyni vaxtda yeniləmək (23.7); `kid` dəstəyi olmadığı üçün rotasiya
  qısa fasilə ilə nəticələnir.

### P3. Gateway 503 qaytarır

- **Səbəb:** `lb://` marşrutu üçün Eureka-da instans yoxdur (servis düşüb və ya qeydiyyatdan keçməyib).
- **Yoxlama:** `localhost:8761` UI; servis logunda `DiscoveryClient` xətaları; `EUREKA_INSTANCE_IP_ADDRESS`.
- **Hərəkət:** servisi yenidən başlatmaq; qeydiyyatın 30 saniyəyə qədər gecikə biləcəyini nəzərə almaq [NƏTİCƏ: Eureka
  klientinin standart yeniləmə intervalları].

### P4. campaign/log sorğuları ~60 saniyə çəkir

- **Səbəb:** Kafka əlçatmazdır; `KafkaTemplate.send()` metadata üçün `max.block.ms` (60 s) gözləyir — yavaş sorğu
  filtrində (yalnız campaign-service və log-service) və kampaniya yazılarında (F10). auth və dashboard təsirlənmir —
  bu, diaqnozda faydalı əlamətdir. Klientin gördüyü status [NƏTİCƏ] cavabın artıq commit olunub-olunmamasından asılıdır;
  kampaniya yazısında istisna tranzaksiyanı geri qaytarır və 500 verir.
- **Yoxlama:** `docker compose ps kafka`; servis logunda `TimeoutException ... not present in metadata after 60000 ms`
  [NƏTİCƏ: kafka-clients mesaj formatı]; Tempo-da uzun span-lar.
- **Hərəkət:** Kafka-nı bərpa etmək. Müvəqqəti yumşaltma: `app.slow-request-threshold-ms`-i çox böyük dəyərə qaldırmaq
  (filtr göndərmir) — kampaniya yazıları isə Kafka olmadan yenə bloklanır. Daimi həll: fəsil 22.6.

### P5. Dashboard 500 qaytarır (`UNAVAILABLE` / `UNKNOWN`)

- **Səbəb:** campaign-service gRPC (9090) əlçatmazdır və ya server istisna atıb.
- **Yoxlama:** dashboard logunda yox, **cavab gövdəsində** `StatusRuntimeException` mesajı (handler loglamır — F38);
  `docker compose logs campaign-service`; `CAMPAIGN_GRPC_ADDRESS`.
- **Hərəkət:** campaign-service-i bərpa etmək. `UNKNOWN` + mövcud olmayan id → bu, 404 ssenarisidir (fəsil 20.7),
  infrastruktur problemi deyil.

### P6. Statistika köhnədir

- **Səbəb:** evict/commit yarışı və TTL olmaması (F13).
- **Yoxlama:** `GET /api/v1/campaigns/statistics` ilə DB-dəki `group by` nəticəsini müqayisə etmək.
- **Hərəkət:** `redis-cli DEL` ilə `campaign-statistics::SimpleKey []` açarını silmək (və ya `--scan` ilə tapılan açarı);
  növbəti sorğu DB-dən doldurur. Daimi həll: TTL + transaction-aware cache.

### P7. Yavaş sorğu jurnalında yeni qeyd görünmür

Ehtimal olunan səbəblər ardıcıllıqla:

1. **Poison pill** (F11) — log-service logunda dövri `Consumer exception` / `SerializationException`, consumer group lag
   artır, DLQ boşdur. Hərəkət: problemli offseti keçmək
   (`kafka-consumer-groups.sh --reset-offsets --to-offset <N+1> --topic log-events:0 --group log-service --execute`,
   consumer dayandırılmış halda), sonra `ErrorHandlingDeserializer` əlavə etmək.
2. **Elasticsearch düşüb / yalnız oxu rejimində** — `DefaultErrorHandler` 3 cəhddən sonra qeydləri `log-events.DLQ`-a
   göndərir; logda `DLQ message received`. ES disk "flood stage" həddinə çatanda indeksə
   `index.blocks.read_only_allow_delete` qoyulur [NƏTİCƏ: ES standart davranışı]. Hərəkət: disk boşaltmaq, bloku
   götürmək, DLQ-nu yenidən emal etmək (repozitoriyada replay mexanizmi yoxdur — əl ilə).
3. **Producer tərəfi** — servis Kafka-ya yaza bilmir (P4).
4. **Mövzu yoxdur** — avtomatik yaradılma söndürülübsə (`KAFKA_AUTO_CREATE_TOPICS_ENABLE`, `docker-compose.yml:322`).

### P8. Auth audit qeydləri itir

- **Səbəb:** log-service düşüb → Retry (3×500 ms) → circuit breaker açıqdır (30 s) → çağırışlar atlanır; bulkhead dolub
  (10 eyni vaxtda).
- **Yoxlama:** auth-service logunda `Auth audit skipped because log-service circuit breaker is open`;
  `resilience4j_circuitbreaker_state`.
- **Hərəkət:** log-service-i bərpa etmək. İtmiş audit bərpa olunmur — audit hüquqi tələbdirsə dizayn dəyişməlidir
  (fəsil 22.5, ADR-4).

### P9. Servis başlamır

| Log mesajı / əlamət | Səbəb | Hərəkət |
|---|---|---|
| `Schema-validation: missing column/table` | `ddl-auto: validate` sxemlə entity arasında fərq | Liquibase changeset-in tətbiq olunduğunu yoxlamaq |
| `Validation Failed: ... checksum` (Liquibase) | Tətbiq olunmuş changeset dəyişdirilib | Changeset-i dəyişməmək; yeni changeset; son çarə `validCheckSum` |
| `Connection refused` config-server-ə | Config Server əlçatmazdır | `optional:` sayəsində başlayır, lakin paylanan xassələr itir (F32) |
| `No qualifying bean of type ObjectMapper` | Jackson 2 bean-i yoxdur (Eureka asılılığı çıxarılıb) | F20 — `JsonMapper`-ə keçid |
| Private/public key oxuna bilmir | `JWT_*_KEY_LOCATION` səhv | Fayl yolunu və formatı (PEM) yoxlamaq |
| `UnsupportedClassVersionError` / preview xətası | JVM 25 deyil və ya `--enable-preview` yoxdur | Image və işə salma parametrləri |

### P10. DB bağlantı hovuzu tükənir

- **Simptom:** gecikmə artır, sonra `Connection is not available, request timed out after 30000ms` [NƏTİCƏ: Hikari
  standart `connectionTimeout`].
- **Səbəb:** virtual thread-lər çox sayda eyni vaxtlı sorğu qəbul edir, hovuz (standart 10) isə məhduddur; tranzaksiya
  daxilində yavaş xarici çağırış (Kafka `send` bloklanması — F10) bağlantını uzun müddət tutur.
- **Hərəkət:** `hikaricp_connections_pending` metrikinə baxmaq; xarici çağırışı tranzaksiyadan çıxarmaq; hovuz ölçüsünü
  DB-nin `max_connections` limitinə görə planlaşdırmaq (servis sayı × instans × hovuz ölçüsü).

### P11. Gözlənilməz 409 `DUPLICATE_CAMPAIGN`

- **Səbəb:** eyni kateqoriya + başlıq + təsvir ilə aktiv/deaktiv kampaniya artıq var; yoxlama `DEACTIVE` statusunu da
  nəzərə alır (yalnız `DUPLICATE` istisnadır) — deaktiv kampaniyanı eyni mətnlə yenidən yaratmaq mümkün deyil [FAKT]
  (`CampaignService.java:140-150`).
- **Hərəkət:** biznes qaydasının niyyətini komanda ilə dəqiqləşdirmək (fəsil 21.11).

## 23.5 Yerləşdirmə və geri qaytarma

| Dəyişiklik növü | Risk | Prosedur |
|---|---|---|
| Kod (sxemsiz) | Aşağı | Yeni image, rolling restart; köhnə image teqi geri qaytarmaq üçün saxlanır (`latest` istifadə etməyin) |
| Liquibase changeset | Orta–yüksək | Geriyə uyğun dəyişiklik (expand → migrate → contract); rollback blokları var (`campaign-service/src/main/resources/db/changelog/db.changelog-master.yml:111-113`), lakin məlumat itkisini nəzərə alın |
| Hadisə sxemi | Yüksək | Yalnız əlavə sahələr; silmə/ad dəyişmə → yeni versiyalı mövzu |
| Proto (gRPC) | Yüksək | Sahə nömrələrini dəyişməmək, yalnız yeni sahə; əvvəl server, sonra klient |
| JWT açarı | Yüksək | 23.7 |
| Rate limit / hədd parametrləri | Aşağı | Env dəyişəni + restart |

## 23.6 Tutum və tənzimləmə

- **Virtual thread-lər + Hikari:** servlet sorğuları qeyri-məhdud paralelliklə gəlir, hovuz isə 10 bağlantıdır;
  darboğaz DB hovuzuna keçir. Hovuz ölçüsünü artırmaq əvəzinə tranzaksiyaları qısa saxlamaq daha effektivdir.
- **Kafka:** bütün mövzular 1 partisiyadır (`KAFKA_NUM_PARTITIONS: 1`) — log-service yalnız bir consumer ilə
  paralelləşə bilər. Miqyas üçün partisiya sayını əvvəlcədən planlaşdırın (artırmaq açar → partisiya xəritəsini dəyişir).
- **Elasticsearch:** tək node, `-Xms512m -Xmx512m` (`docker-compose.yml:353-362`), ILM yoxdur (F43). 5 ms həddi ilə
  `slow-request-logs` sürətlə böyüyür; rollover + saxlama müddəti mütləqdir.
- **Redis:** `maxmemory` və eviction siyasəti təyin olunmayıb [FAKT: compose-da konfiqurasiya yoxdur]; cache açarlarının
  TTL-i yoxdur — yaddaş məhdudlaşdırılmalıdır.

## 23.7 Təhlükəsizlik əməliyyatları

**JWT açar rotasiyası.** Token-lərdə `kid` başlığı və servislərdə çoxlu açar dəstəyi yoxdur [NƏTİCƏ: `JwtTokenService`
tək public key ilə yoxlayır]. Ona görə rotasiya ardıcıllığı: (1) yeni cüt yaratmaq; (2) bütün yoxlayıcılara (gateway,
4 servis) yeni public key; (3) auth-service-ə yeni private key; (4) köhnə token-lər 120 dəqiqə ərzində rədd olunur —
istifadəçilər yenidən login olur. Fasiləsiz rotasiya üçün JWKS və `kid` lazımdır (fəsil 10, 22).

**Secret-lər.** Repozitoriyadakı demo açar və şifrələr (F22) istehsalda istifadə olunmamalıdır: secret manager (Vault,
bulud KMS), konteynerə fayl və ya env kimi inyeksiya, rotasiya siyasəti. Repozitoriya tarixçəsindən secret-i silmək
kifayət deyil — kompromis sayılmalı və dəyişdirilməlidir.

**Şəbəkə.** Xaricə yalnız gateway açılmalıdır; gRPC 9090 (F04), DB, Redis, Kafka, ES, actuator portları daxili şəbəkədə
qalmalıdır.

## 23.8 Müsahibə sualları

**S1. İstehsalda bütün sorğular birdən yavaşladı. Addımlarınız?**
Cavab: RED metriklərində hansı servis və endpoint; ortaq asılılıq (DB hovuzu, Kafka, Redis); trace-də ən uzun span;
son dəyişiklik (deploy, konfiqurasiya). Bu sistemdə ilk şübhəli — Kafka (`max.block.ms`) və Hikari hovuzu.

**S2. Consumer lag artır, DLQ boşdur. Nə düşünürsünüz?**
Cavab: Consumer irəliləmir, lakin qeydlər DLQ-ya da getmir — ya consumer dayanıb, ya da poison pill
(`ErrorHandlingDeserializer` yoxdur). Logda təkrarlanan deserializasiya xətası axtarmaq, offseti keçmək, konfiqurasiyanı
düzəltmək.

**S3. Liquibase checksum xətası nə deməkdir?**
Cavab: Artıq tətbiq olunmuş changeset-in məzmunu dəyişdirilib. Düzgün həll — dəyişikliyi geri qaytarıb yeni changeset
yazmaq; `validCheckSum` yalnız şüurlu istisna üçün.

**S4. Circuit breaker açıq qalır. Necə araşdırırsınız?**
Cavab: `resilience4j_circuitbreaker_state` və çağırış nəticələri metrikləri; qorunan servisin sağlamlığı; CB-nin hansı
istisnaları uğursuzluq sayması (`record-exceptions` — burada bütün `RuntimeException`-lar, 4xx də daxil).

**S5. JWT açarını fasiləsiz necə dəyişərdiniz?**
Cavab: JWKS endpoint-i, `kid` başlığı, yoxlayıcılarda köhnə və yeni açarın paralel qəbulu, sonra köhnə açarın silinməsi.

## 23.9 Xülasə

- Compose-da tətbiq servislərinin healthcheck-i yoxdur; actuator açıqdır.
- Əsas siqnallar: RED metrikləri, Hikari, circuit breaker vəziyyəti, consumer lag; producer və ES metrikləri yoxdur.
- Ən çox ehtimal olunan insidentlər: rate limit (P1), Kafka bloklanması (P4), poison pill (P7), dashboard gRPC (P5).
- Açar rotasiyası, secret-lər və şəbəkə izolyasiyası istehsala keçidin ilkin şərtləridir.
