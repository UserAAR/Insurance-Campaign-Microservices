# 1. Bu kitabı necə oxumalı

## 1.1 Kimin üçün yazmışam və nəyi hədəfləyirəm

Bu kitabı Java və Spring Boot ilə real backend yazmış, mikroservis anlayışlarını bilən, lakin təxminən 9–12 ay
kod yazmamış mühəndis üçün yazmışam — bir vaxtlar mənim özüm də məhz belə oxucu idim. Belə oxucunun iki ehtiyacı
olur:

1. **Unudulanı sürətlə bərpa etmək** — DI, bean həyat dövrü, `@Transactional`, JPA, validasiya, Spring Security
   filtrləri kimi mövzular uzun izahat tələb etmir, lakin "hansı annotasiya nəyi dəyişir və nə vaxt işləmir"
   suallarına dəqiq cavab tələb edir.
2. **Yeni texnologiyanı dərindən öyrənmək** — Kafka, Elasticsearch, gRPC, Spring Cloud Gateway, Resilience4j,
   OpenTelemetry kimi mövzuları həm nəzəri əsası ilə, həm də bu repozitoriyadakı konkret implementasiya ilə izah
   edirəm.

Kitabın son məqsədi budur: siz repozitoriyanı bir sistem kimi başa düşəsiniz, əsas dizayn qərarlarını izah edə
biləsiniz, hər əsas axını giriş nöqtəsindən nəticəyə qədər izləyə biləsiniz, texnologiyaları layihədən asılı
olmadan anlayasınız və **oxşar mikroservis backend-ini özünüz layihələndirib yaza biləsiniz**.

## 1.2 İki pəncərə ilə oxuyun: PDF və mənbə kodu

Kitabı repozitoriya ilə yanaşı oxunmaq üçün yazmışam. Layihəyə aid hər iddianın yanında fayl yolunu və sətir
nömrəsini verirəm:

```text
campaign-service/src/main/java/com/casestudy/insurancecampaign/campaign/service/CampaignService.java:54-77
```

Bu istinadı gördükdə IDE-də faylı açın (IntelliJ IDEA-da *Navigate → File* və ya `Ctrl+Shift+N`, sonra `:54`).
Kod çıxarışlarının üstündə **"Mənbə:"** sətri olur; çıxarış həmin sətir aralığından götürülüb. Bir hissəni
buraxmışamsa, çıxarışda `// ... (buraxılıb)` şərhi görünür. Sətirləri yalnız oxunaqlılıq üçün buraxıram; heç bir
çıxarışa repozitoriyada olmayan kod əlavə etmirəm. Repozitoriyada olmayan, yalnız izah üçün yazdığım nümunələri
ayrıca **"İllüstrativ nümunə (repozitoriyada yoxdur)"** başlığı ilə verirəm.

Bütün istinadları və kod çıxarışlarını kitabın `build/verify_refs.py` skripti ilə avtomatik yoxlamışam (bax:
fəsil 25 və `audit/audit-report.md`).

## 1.3 Etiketlər: fakt, nəticə, naməlum, risk

Mühəndis üçün ən vacib bacarıqlardan biri "kod nə deyir" ilə "mən nə düşünürəm" arasındakı fərqi saxlamaqdır.
Buna görə kitabda dörd etiketdən istifadə edirəm:

| Etiket | Mənası | Nümunə |
|---|---|---|
| [FAKT] | Kodla və ya konfiqurasiya ilə birbaşa təsdiqlənir; istinad verilir | `LIFE` kampaniyası `ACTIVE` statusu ilə yaradılır (`CampaignService.java:133-138`) |
| [NƏTİCƏ] | Implementasiyadan, framework davranışından və ya bir neçə faktdan əsaslandırılmış çıxarış | Kafka göndərişi tranzaksiya commit-indən əvvəl baş verə bilər |
| [NAMƏLUM] | Repozitoriyadan müəyyən edilə bilməz | `campaign-events` mövzusunu repozitoriyadan kənarda kimin oxuduğu |
| [RİSK] | Potensial defekt, təhlükəsizlik və ya etibarlılıq problemi | İstənilən şəxsin `ROLE_ADMIN` ilə qeydiyyatdan keçə bilməsi |

Developerin niyyəti koddan oxunmursa, onu fakt kimi təqdim etmirəm: "bu qərarı əsaslandıra biləcək mühəndis
səbəbi" kimi **analiz** təqdim edirəm.

## 1.4 Kitabın quruluşu

| Hissə | Məzmun | Nə vaxt oxumalı |
|---|---|---|
| I. Sistemi anlamaq | Biznes məqsədi, komponentlər, axınlar, repozitoriya və build | Hər şeydən əvvəl |
| II. Təməllər | Java 25, Spring Boot 4 mexanizmləri, REST və təbəqəli dizayn | Unudulmuş bilikləri bərpa etmək üçün |
| III. Nüvə | Campaign Service, persistence, validasiya/istisnalar, Auth Service, security | Layihənin "ürəyi" |
| IV. Texnologiyalar | Kafka, Elasticsearch, gRPC, HTTP klientləri və Resilience4j, Redis, Spring Cloud, observability, konteynerlər, testlər | Hər biri müstəqil oxuna bilər |
| V. Axınlar | Çoxservisli uçdan-uca icra yolları | III və IV-dən sonra |
| VI. Qiymətləndirmə və dizayn | Tapıntılar, kompromislər, oxşar sistemin dizaynı | Tənqidi düşüncə və müsahibə üçün |
| VII. Production və müsahibə | Runbook, sistemin tam izahı, sual-cavablar, əlavələr | Son mərhələ və təkrar üçün |

Hər əsas texniki fəsli təxminən bu ardıcıllıqla qurmuşam: **problem → nəzəriyyə → daxili mexanizm → repozitoriya
implementasiyası → icra axını → konfiqurasiya → yeni layihədə tətbiq → alternativlər → xətalar və troubleshooting →
müsahibə sualları → xülasə.**

## 1.5 Tövsiyə olunan oxu yolları

**Tam yol (tövsiyə edirəm, 2–3 həftə, gündə 2–3 saat):** fəsillərə ardıcıl gedin; hər fəsildən sonra
istinad olunan faylları açıb yenidən oxuyun, sonra fəsil sonundakı müsahibə suallarına kitaba baxmadan cavab verin.

**Sürətli bərpa (3–4 gün):** 2 → 3 → 4 → 6 → 8 → 10 → 20 → 21 → 24.

**Yalnız yeni texnologiyalar:** 11 (Kafka) → 12 (Elasticsearch) → 13 (gRPC) → 14 (Resilience4j) → 16 (Gateway) → 17
(Observability). Bu fəsillərdə əvvəlcə texnologiyanın özünü öyrədirəm, sonra layihədəki istifadəni göstərirəm.

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
`--enable-preview` ilə işə salınır (Dockerfile-lar və surefire konfiqurasiyası). Bu kitabı hazırladığım mühitdə
yalnız Java 21 olduğu üçün layihəni bu nəşr üçün kompilyasiya etməmişəm və testləri işə salmamışam; bütün təhlili
statik olaraq mənbə kodu, konfiqurasiya, Maven asılılıq ağacı (`mvn dependency:tree`) və istifadə olunan
kitabxanaların mənbə kodu üzərində aparmışam. Ona görə runtime davranışı haqqında iddiaları, kitabxana mənbəyində
təsdiqləmədiyim hallarda, [NƏTİCƏ] kimi etiketləyirəm.

Demo istifadəçilər [FAKT] (`auth-service/src/main/resources/db/changelog/db.changelog-master.yml:31-37`, `80-89`,
`203-216`): `demo` (`ROLE_USER,ROLE_ADMIN`), `admin` (`ROLE_ADMIN`), `user` (`ROLE_USER`). Şifrələri changelog-da
`{noop}` prefiksi ilə açıq mətndə yazılıb (F22); dəyəri bu kitabda təkrar etmirəm — lokal işə salanda onu həmin
changelog-dan götürün. Bunlar yalnız lokal demo üçündür.

## 1.7 Terminologiya qeydi

Texniki terminlərin bir çoxu Azərbaycan dilində sabitləşməyib. Kitabda termini ilk dəfə işlədəndə həm Azərbaycan,
həm ingilis dilində verirəm (məsələn, "istehlakçı qrupu (consumer group)"), sonra ən çox işlənən formadan
istifadə edirəm. Kod elementlərini (`@Transactional`, `KafkaTemplate`) həmişə orijinal yazılışında saxlayıram.
Qısaltmaların tam siyahısı 25-ci fəsildəki lüğətdədir.

## 1.8 Xülasə

- Kitab real repozitoriyadan çıxarılıb; layihəyə aid hər iddianı fayl:sətir istinadı ilə verirəm.
- Fakt, nəticə, naməlum və risk etiketləri "kod nə deyir" ilə "mən nə düşünürəm" arasındakı sərhədi saxlayır.
- PDF-i IDE ilə yanaşı oxuyun; hər fəsildən sonra sualları kitaba baxmadan cavablandırın.
- Növbəti fəsildə sistemin nə üçün mövcud olduğunu və necə qurulduğunu bütöv şəkildə göstərirəm.
