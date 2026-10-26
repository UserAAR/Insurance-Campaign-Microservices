# Audit hesabatı

Bu hesabat kitabın necə yoxlandığını, hansı səhvlərin tapılıb düzəldildiyini və hansı məhdudiyyətlərin qaldığını
təsvir edir. Rəqəmlər son build-ə aiddir (bax: `audit/verify-refs.json`, `audit/semantic-refs.json`,
`audit/pdf-audit.json`).

## Yekun göstəricilər

| Göstərici | Dəyər |
|---|---|
| Kitab | *Mikroservislərin Anatomiyası* — AAR; `output/Mikroservislerin-Anatomiyasi-AAR.pdf`: üz qabığı + 245 nömrəli səhifə (cəmi 246), ~16 MB |
| Üz qabığı | `cover/cover.svg` (vektor) və `output/Mikroservislerin-Anatomiyasi-AAR-cover.png` (2480 × 3508) |
| Fəsillər | Ön söz + 26 fəsil (7 hissə), ~66 000 söz |
| Mermaid diaqramları | 33 |
| Mənbə ilə doğrulanan kod çıxarışları | 70 |
| `fayl:sətir` istinadları | 459 (101 fərqli fayl) — avtomatik yoxlamada 0 xəta |
| Müsahibə sualı (cavabla) | 187 |
| Etiketlər | `[FAKT]` 149, `[NƏTİCƏ]` 168, `[NAMƏLUM]` 12, `[RİSK]` 20 |
| Tapıntılar reyestri | F01–F46 (5 D, 21 R, 6 T, 14 M, 1 H; F06 həm R, həm T) |
| Əhatə | 178 izlənən faylın hamısı istinad olunub: 151-i fəsillərin mətnində, 27-si yalnız 25.4-cü əlavədəki fayl indeksində (Dockerfile şablonları, `*Application` sinifləri, test açarları, kopya observation konfiqurasiyaları və s.) |

## Audit A — məzmun tamlığı

Yoxlama tapşırığın tələblər siyahısı ilə fəsillərin müqayisəsi ilə aparıldı.

| Tələb | Harada | Vəziyyət |
|---|---|---|
| Ön söz, oxu yolları, etiket konvensiyası | Ön söz, fəsil 1 | Var |
| Sistem icmalı, arxitektura, data sahibliyi, uğursuzluq təsiri | Fəsil 2 | Var |
| Build, Maven, modul qrafı | Fəsil 3 | Var |
| Hər texnologiya üçün dərin fəsil (konsepsiya → problem → nəzəriyyə → daxili mexanizm → repozitoriya → icra axını → konfiqurasiya → praktika → alternativlər → uğursuzluqlar → müsahibə → xülasə) | Fəsillər 4–19 | Var; Kafka (15 bölmə), Elasticsearch, gRPC, validasiya/istisnalar, security ayrıca və ətraflı |
| Uçdan-uca axınlar | Fəsil 20 (9 axın, 7 diaqram) | Var |
| Mühəndislik qiymətləndirməsi (D/R/T/M/H) | Fəsil 21 + `analysis/01-architecture-map.md` | Var |
| Oxşar sistemin dizaynı | Fəsil 22 | Var |
| İstehsal mühəndisliyi, runbook | Fəsil 23 | Var |
| Müsahibəyə hazırlıq, 5 dəqiqəlik izah | Fəsil 24 + hər fəslin sonu | Var |
| Lüğət, endpoint kataloqu, konfiqurasiya arayışı | Fəsil 25 | Var |
| Əhatə matrisi | Fəsil 26 (yaradılır) + `analysis/coverage-matrix.md` | Var |
| Redaktə oluna bilən mənbələr və build skriptləri | `chapters/`, `build/`, `README.md` | Var |

Audit A zamanı tapılıb düzəldilən boşluqlar:

- Fəsil 12 (Elasticsearch) və 16 (Spring Cloud) diaqramsız idi — hər birinə bir diaqram əlavə olundu.
- 27 fayl heç bir fəsildə xatırlanmırdı — 25.4-cü əlavədə hər birinin məzmunu və rolu təsvir olundu (fayllar sadə və ya
  şablon təkrarı olduğu üçün ayrıca fəsil tələb etmir; bu, əhatə matrisində "25" kimi açıq göstərilir).
- `coverage.py`-da iki hesablama səhvi düzəldildi: `.gitignore` boş "stem" səbəbindən bütün fəsillərə uyğun gəlirdi;
  kök `pom.xml` `auth-service/pom.xml`-in alt sətri kimi sayılırdı.

## Audit B — texniki dəqiqlik

**Avtomatik (struktur):** `build/verify_refs.py` hər `fayl:sətir` istinadında faylın mövcudluğunu, adın birmənalılığını
və sətir aralığını, hər `<!-- src: -->` çıxarışında isə buraxılmayan bütün sətirlərin mənbədə həmin aralıqda ardıcıl
olmasını yoxlayır. Son nəticə: 459 istinad, 70 çıxarış, 0 xəta.

**Evristik (semantik):** `build/semantic_refs.py` istinaddan əvvəlki cümlədə backtick içində yazılmış identifikatorların
istinad olunan sətirlərdə olub-olmadığını yoxlayır. 290 istinad yoxlanıldı, 59 şübhəli çıxdı; hamısı əl ilə nəzərdən
keçirildi: 4-ü real səhv idi (aşağıda #11), qalanları kontekst sözlərindən yaranan yalançı siqnallardır (məs. qonşu
cədvəl xanasındakı ad, `CONFLICT` / `Conflict` hərf fərqi). Düzəlişdən sonra 55 şübhəli qalır — hamısı yoxlanılmış
yalançı siqnallardır.

**Əl ilə yoxlama zamanı tapılıb düzəldilən səhvlər** (bu sessiyada):

1. Yavaş sorğu filtrinin əhatəsi: fəsil 20, 21, 23, 24-də "bütün servislər" yazılmışdı. `common/pom.xml`-də
   `spring-kafka` `optional`-dır; Maven asılılıq ağacı ilə təsdiqləndi ki, filtr yalnız campaign-service və log-service-də
   aktivdir. Bütün yerlər düzəldildi (fəsil 2.5 əvvəldən düzgün idi).
2. `@Valid` və `@PreAuthorize` sırası: fəsil 20 və 24-də tərs göstərilmişdi. Validasiya arqument həllində, metod
   təhlükəsizliyi isə proxy çağırışında işləyir (fəsil 8.9 ilə uyğunlaşdırıldı).
3. ES indeks adı: `slow-requests` → `slow-request-logs` (`SlowRequestDocument.java:13`).
4. Poison pill davranışı fəsil 11-dəki spring-kafka 4.1.0 mənbə təhlili ilə uyğunlaşdırıldı (DLQ-ya getmir, sonsuz
   döngü).
5. Səhv UUID: "400 və ya 500" → `GlobalExceptionHandler` catch-all səbəbindən 500 (F07).
6. Validasiya sərhədləri: başlıq 10–50, təsvir 20–200 (`CreateCampaignRequest.java:10-19`).
7. Loki label adları: `service_name` → `service`, `level`, `trace_id` (`promtail-config.yml`).
8. `occurredAt` sahəsinin açıq `Date` mapping-i var — runbook-dakı əks iddia düzəldildi.
9. Cache evict və commit sırası "commit-dən sonra" kimi qəti deyil, "sıra təyin olunmayıb" kimi təqdim olundu.
10. Fəsillərarası bölmə istinadları (15.6, 13.6/13.10) düzəldildi.
11. Fəsil 3-də `pom.xml:49-55` və s. — mənada `grpc-contract/pom.xml` idi, lakin ad kök POM-a həll olunurdu; tam yolla
    yazıldı.
12. Boot 4.1 `spring.http.serviceclient.<ad>.connect-timeout/read-timeout` xassələri `spring-boot-http-client` 4.1.0
    mənbəsindən, Kafka `template/listener.observation-enabled` və `JacksonJsonDeserializer` adları spring-boot-kafka /
    spring-kafka 4.1.0 mənbəsindən yoxlanıldı.

Əvvəlki sessiyada düzəldilənlər: yml və changelog sətir istinadları, auth-service-in səhv yığılmış `forgotPassword`
çıxarışı (real kodla əvəz olundu), `aspectjweaver`-in mənbəyi (spring-data-jpa → spring-aspects).

## Audit C — PDF bütövlüyü

**Avtomatik:** `build/audit_pdf.py` — mündəricatdakı 323 başlığın hər birinin göstərilən səhifədə mətn kimi tapılması,
üz qabığından sonrakı hər səhifədə `N / 245` altbilgisi və PDF səhifə etiketlərinin (`Üz qabığı`, `1`, `2`, …) uyğunluğu,
boş səhifələrin olmaması, şriftlərin PDF-ə daxil edilməsi (26 şrift; Type3 qlifləri
daxili təsvir olunur), mətn qatında U+FFFD olmaması və Azərbaycan hərflərinin (ə, ğ, ı, İ, ö, ş, ç, ü) mövcudluğu.
Nəticə `audit/pdf-audit.json`-dadır.

**Vizual:** üz qabığı, mündəricat, hər hissədən kod, cədvəl, ER, ardıcıllıq, axın və kvadrant diaqramları olan
səhifələr PNG-yə render edilib baxıldı. Tapılıb düzəldilənlər:

| Problem | Səbəb | Düzəliş |
|---|---|---|
| ER diaqramında əlaqə etiketi tünd qutu içində oxunmurdu | Mermaid `base` mövzusunda `.edgeLabel .label` dolğusu sərhəd rənginə bərabər idi | `themeCSS` + `edgeLabelBackground`; render keşi versiyalandı (`RENDER_CONFIG_VERSION`) |
| 50 nömrəli/markerli siyahı abzas kimi birləşmişdi (məs. fəsil 10.4) | Siyahıdan əvvəl boş sətir yox idi | pandoc `+lists_without_preceding_blankline`; bir yalançı hal (`+` ilə başlayan sətir) düzəldildi |
| 9 iştirakçılı ardıcıllıq diaqramı (fəsil 20.3) oxunmurdu | Çox geniş | 7 iştirakçıya sadələşdirildi |
| ES axın diaqramı (fəsil 12) çox kiçik idi | Soldan sağa düzülüş | Yuxarıdan aşağıya düzülüş |
| Kvadrant diaqramı render olunmurdu, sonra başlıqda dırnaq görünürdü | Mermaid lexer-i dırnaqsız qeyri-ASCII mətni qəbul etmir | Ox və kvadrant etiketləri dırnaqla, başlıq dırnaqsız |
| Uzun cədvəllərdə sütunlar bərabər enli idi | Pipe cədvəl ayırıcısı | Ayırıcı uzunluqları ilə nisbi enlər (fəsil 25.4, 26) |

## Məhdudiyyətlər

- Mühitdə yalnız Java 21 olduğu üçün layihə kompilyasiya edilməyib, testlər və servislər işə salınmayıb. Bütün runtime
  davranışı haqqında iddialar `[NƏTİCƏ]` kimi işarələnib; bəziləri kitabxana mənbə kodu ilə (spring-kafka, SCG,
  spring-boot-cache, Resilience4j, net.devh, spring-boot-http-client) əsaslandırılıb.
- Semantik yoxlama evristik və seçmədir; avtomatik yoxlama yalnız istinadın mövcudluğunu və çıxarışın dəqiqliyini təmin
  edir, mətnin hər iddiasını deyil.
- Ciddilik, ehtimal və səy qiymətləndirmələri müəllifin mühakiməsidir.
- 25.5-dəki rəsmi sənəd ünvanları ümumi giriş səhifələridir; konkret URL-lər yoxlanılmayıb.
- Diaqramlar Mermaid avtomatik düzülüşü ilə render olunur; bəzi ardıcıllıq diaqramlarında özünə mesaj mətnləri dar
  sütunda bükülür.
- PDF ~16 MB-dır (daxil edilmiş şriftlər və vektor diaqramlar).
- Secret-lər (DB şifrələri, private key-lər, CI-dakı Sonar şifrəsi) kitabda təkrar edilmir; yalnız yerləri göstərilir.
