# 1. Bu bələdçidən necə istifadə etməli

## 1.1 Kim üçün yazılıb və nəyi hədəfləyir

Bu bələdçi Java və Spring Boot ilə real backend yazmış, mikroservis anlayışlarını bilən, lakin təxminən 9–12 ay
kod yazmamış mühəndis üçündür. Belə oxucunun iki ehtiyacı olur:

1. **Unudulanı sürətlə bərpa etmək** — DI, bean həyat dövrü, `@Transactional`, JPA, validasiya, Spring Security
   filtrləri kimi mövzular uzun izahat tələb etmir, lakin "hansı annotasiya nəyi dəyişir və nə vaxt işləmir"
   suallarına dəqiq cavab tələb edir.
2. **Yeni texnologiyanı dərindən öyrənmək** — Kafka, Elasticsearch, gRPC, Spring Cloud Gateway, Resilience4j,
   OpenTelemetry kimi mövzular həm nəzəri əsas, həm də bu repozitoriyadakı konkret implementasiya ilə izah olunur.

Bələdçinin son məqsədi: oxucu repozitoriyanı bir sistem kimi başa düşsün, əsas dizayn qərarlarını izah edə
bilsin, hər əsas axını giriş nöqtəsindən nəticəyə qədər izləyə bilsin, texnologiyaları layihədən asılı olmadan
anlasın və **oxşar mikroservis backend-ini özü layihələndirib yaza bilsin**.

## 1.2 İki pəncərə ilə oxuyun: PDF və mənbə kodu

Bələdçi repozitoriya ilə yanaşı oxunmaq üçün yazılıb. Hər layihə-spesifik iddianın yanında fayl yolu və sətir
nömrəsi verilir:

```text
campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/service/CampaignService.java:54-77
```

Bu istinadı gördükdə IDE-də faylı açın (IntelliJ IDEA-da *Navigate → File* və ya `Ctrl+Shift+N`, sonra `:54`).
Kod çıxarışlarının üstündə **"Mənbə:"** sətri olur; çıxarış həmin sətir aralığından götürülüb. Bir hissə
buraxılıbsa, çıxarışda `// ... (buraxılıb)` şərhi görünür. Buraxılmış sətirlər yalnız oxunaqlılıq üçün çıxarılır;
heç bir çıxarışa repozitoriyada olmayan kod əlavə edilmir. Repozitoriyada olmayan, yalnız izah üçün yazılmış
nümunələr ayrıca **"İllüstrativ nümunə (repozitoriyada yoxdur)"** başlığı ilə verilir.

Bütün istinadlar və kod çıxarışları bələdçinin `build/verify_refs.py` skripti ilə avtomatik yoxlanılıb (bax:
fəsil 25 və `audit/audit-report.md`).

## 1.3 Etiketlər: fakt, nəticə, naməlum, risk

Mühəndis üçün ən vacib bacarıqlardan biri "kod nə deyir" ilə "mən nə düşünürəm" arasındakı fərqi saxlamaqdır.
Buna görə bələdçidə dörd etiket istifadə olunur:

| Etiket | Mənası | Nümunə |
|---|---|---|
| [FAKT] | Kodla və ya konfiqurasiya ilə birbaşa təsdiqlənir; istinad verilir | `LIFE` kampaniyası `ACTIVE` statusu ilə yaradılır (`CampaignService.java:133-138`) |
| [NƏTİCƏ] | Implementasiyadan, framework davranışından və ya bir neçə faktdan əsaslandırılmış çıxarış | Kafka göndərişi tranzaksiya commit-indən əvvəl baş verə bilər |
| [NAMƏLUM] | Repozitoriyadan müəyyən edilə bilməz | `campaign-events` mövzusunu repozitoriyadan kənarda kimin oxuduğu |
| [RİSK] | Potensial defekt, təhlükəsizlik və ya etibarlılıq problemi | İstənilən şəxsin `ROLE_ADMIN` ilə qeydiyyatdan keçə bilməsi |

Developerin niyyəti koddan oxunmursa, bələdçi bunu fakt kimi təqdim etmir: "bu qərarı əsaslandıra biləcək
mühəndis səbəbi" kimi **analiz** təqdim edir.

## 1.4 Bələdçinin quruluşu

| Hissə | Məzmun | Nə vaxt oxumalı |
|---|---|---|
| I. Sistemi anlamaq | Biznes məqsədi, komponentlər, axınlar, repozitoriya və build | Hər şeydən əvvəl |
| II. Təməllər | Java 25, Spring Boot 4 mexanizmləri, REST və təbəqəli dizayn | Unudulmuş bilikləri bərpa etmək üçün |
| III. Nüvə | Campaign Service, persistence, validasiya/istisnalar, Auth Service, security | Layihənin "ürəyi" |
| IV. Texnologiyalar | Kafka, Elasticsearch, gRPC, HTTP klientləri və Resilience4j, Redis, Spring Cloud, observability, konteynerlər, testlər | Hər biri müstəqil oxuna bilər |
| V. Axınlar | Çoxservisli uçdan-uca icra yolları | III və IV-dən sonra |
| VI. Qiymətləndirmə və dizayn | Tapıntılar, kompromislər, oxşar sistemin dizaynı | Tənqidi düşüncə və müsahibə üçün |
| VII. Production və müsahibə | Runbook, sistemin tam izahı, sual-cavablar, əlavələr | Son mərhələ və təkrar üçün |

Hər əsas texniki fəsil təxminən bu ardıcıllıqla qurulub: **problem → nəzəriyyə → daxili mexanizm → repozitoriya
implementasiyası → icra axını → konfiqurasiya → yeni layihədə tətbiq → alternativlər → xətalar və troubleshooting →
müsahibə sualları → xülasə.**

## 1.5 Tövsiyə olunan oxu yolları

**Tam yol (tövsiyə edilir, 2–3 həftə, gündə 2–3 saat):** fəsillərə ardıcıl gedin; hər fəsildən sonra
istinad olunan faylları açıb yenidən oxuyun, sonra fəsil sonundakı müsahibə suallarına kitaba baxmadan cavab verin.

**Sürətli bərpa (3–4 gün):** 2 → 3 → 4 → 6 → 8 → 10 → 20 → 21 → 24.

**Yalnız yeni texnologiyalar:** 11 (Kafka) → 12 (Elasticsearch) → 13 (gRPC) → 14 (Resilience4j) → 16 (Gateway) → 17
(Observability). Bu fəsillər əvvəlcə texnologiyanın özünü öyrədir, sonra layihədəki istifadəni göstərir.

**Müsahibəyə hazırlıq:** 24-cü fəsil (sistemin 5 dəqiqəlik izahı və sual-cavablar), sonra hər fəslin sonundakı
suallar, sonra 21-ci fəslin tapıntılar cədvəli — müsahibədə "bu sistemdə nəyi dəyişərdiniz?" sualına cavabdır.

## 1.6 Repozitoriyanı lokal işə salmaq (qısa)

Ətraflı izah 18-ci fəsildədir. Qısa xülasə [FAKT] (`README.md`, compose faylları):

```bash
# 1) Yalnız infrastruktur (PostgreSQL, Redis, Kafka, Elasticsearch, observability, SonarQube)
docker compose -f docker-compose-local.yml up -d

# 2) Servisləri bu ardıcıllıqla IDE-dən və ya Maven ilə başladın
mvn -pl discovery-server spring-boot:run
mvn -pl config-server spring-boot:run
mvn -pl auth-service spring-boot:run
mvn -pl campaign-service spring-boot:run
mvn -pl dashboard-service spring-boot:run
mvn -pl log-service spring-boot:run
mvn -pl api-gateway spring-boot:run

# Alternativ: hər şeyi konteynerdə
docker compose up -d --build
```

Tələblər: JDK 25, Maven 3.9+, Docker. Layihə Java 25 ilə kompilyasiya olunur (`pom.xml:28-29`) və
`--enable-preview` ilə işə salınır (Dockerfile-lar və surefire konfiqurasiyası). Bu bələdçi hazırlanarkən mühitdə
yalnız Java 21 olduğu üçün layihə kompilyasiya edilməyib və testlər işə salınmayıb; bütün təhlil statik olaraq mənbə
kodu, konfiqurasiya, Maven asılılıq ağacı (`mvn dependency:tree`) və istifadə olunan kitabxanaların mənbə kodu
üzərində aparılıb. Runtime davranışı haqqında iddialar buna görə [NƏTİCƏ] kimi etiketlənir, əgər kitabxana
mənbəyində təsdiqlənməyibsə.

Demo istifadəçilər [FAKT] (`auth-service/src/main/resources/db/changelog/db.changelog-master.yml:31-37`, `80-89`,
`203-216`): `demo` (`ROLE_USER,ROLE_ADMIN`), `admin` (`ROLE_ADMIN`), `user` (`ROLE_USER`). Şifrələri changelog-da
`{noop}` prefiksi ilə açıq mətndə yazılıb (F22); dəyər bu bələdçidə təkrar edilmir — lokal işə salanda onu həmin
changelog-dan götürün. Bunlar yalnız lokal demo üçündür.

## 1.7 Terminologiya qeydi

Texniki terminlərin bir çoxu Azərbaycan dilində sabitləşməyib. Bələdçidə ilk istifadədə termin həm Azərbaycan,
həm ingilis dilində verilir (məsələn, "istehlakçı qrupu (consumer group)"), sonra ən çox işlənən forma
istifadə olunur. Kod elementləri (`@Transactional`, `KafkaTemplate`) həmişə orijinal yazılışında saxlanılır.
Qısaltmaların tam siyahısı 25-ci fəsildəki lüğətdədir.

## 1.8 Xülasə

- Bələdçi real repozitoriyadan çıxarılıb; hər layihə-spesifik iddia fayl:sətir istinadı ilə verilir.
- Fakt, nəticə, naməlum və risk etiketləri "kod nə deyir" ilə "biz nə düşünürük" arasındakı sərhədi saxlayır.
- PDF-i IDE ilə yanaşı oxuyun; hər fəsildən sonra sualları kitaba baxmadan cavablandırın.
- Növbəti fəsil sistemin nə üçün mövcud olduğunu və necə qurulduğunu bütöv şəkildə göstərir.
