# Ön söz

Bu kitabı ilk növbədə özüm üçün yazmağa başladım.

Bir müddət kod yazmadan keçən aylardan sonra yenidən backend mühəndisliyinə qayıdanda gördüm ki, ümumi
dərsliklər mənə lazım olanı vermir. `@Transactional`-ın nə etdiyini, Kafka-nın nə olduğunu, JWT-nin necə
qurulduğunu bilirdim — amma "bu biliklər real sistemdə necə birləşir, harada sınır və mən onu necə izah edərəm?"
sualına sürətli və dəqiq cavab verə bilmirdim. Ona görə başqa yol seçdim: öz layihəmi — **Insurance Campaign
Microservices** repozitoriyasını — götürüb onu anatom kimi açdım.

## Niyə "anatomiya"?

Anatom orqanizmi təsvir etməklə kifayətlənmir: hər orqanın harada yerləşdiyini, nə iş gördüyünü, digərləri ilə
necə bağlandığını və xəstələnəndə nə baş verdiyini göstərir. Bu kitabda da eyni şeyi edirəm. Gateway, auth,
campaign, dashboard və log servislərini, onları birləşdirən REST, gRPC və Kafka kanallarını, PostgreSQL, Redis və
Elasticsearch qatlarını bir-bir açıram. Hər hissədə əvvəlcə texnologiyanın özünü — problem, nəzəriyyə, daxili
mexanizm — izah edirəm, sonra onun bu repozitoriyada necə qurulduğunu kodun konkret sətirləri ilə göstərirəm.

Üz qabığındakı bal arısı da bu fikrin simvoludur. Arı koloniyası mikroservis sisteminin ən yaxşı metaforalarından
biridir: hər hüceyrə kiçik və müstəqildir, amma bütöv sistem yalnız aydın kontraktlar, siqnallar və bölüşdürülmüş
məsuliyyət sayəsində yaşayır.

## Bu kitabda nəyə söz verirəm

- **Hər iddianın arxasında kod var.** Layihəyə aid hər cümlənin yanında fayl yolu və sətir nömrəsi verirəm.
  Kitabdakı bütün istinadlar və kod çıxarışları avtomatik skriptlə mənbə kodu ilə tutuşdurulub; siz də IDE-ni açıb
  eyni sətirləri yoxlaya bilərsiniz.
- **Fakt ilə fikri qarışdırmıram.** Koddan birbaşa görünən şeyləri [FAKT], məntiqlə çıxardığım nəticələri [NƏTİCƏ],
  repozitoriyadan bilinməyənləri [NAMƏLUM], təhlükəli yerləri isə [RİSK] kimi işarələyirəm.
- **Öz kodumun zəif yerlərini gizlətmirəm.** Kitabın ən dəyərli hissələrindən biri məhz budur. Admin rolunun
  qeydiyyat zamanı klientdən qəbul edilməsi, parol bərpası token-inin cavabda qaytarılması, Kafka göndərişinin DB
  tranzaksiyasının içində olması, gRPC çağırışlarında deadline-ın olmaması kimi problemləri açıq yazıram və hər biri
  üçün necə düzəldəcəyimi göstərirəm. Real mühəndislik mükəmməl kod deyil — öz qərarlarının qiymətini bilməkdir.
- **Öyrəndiyiniz hər şeyi tətbiq edə biləcəksiniz.** Kitabın sonunda eyni sistemi sıfırdan necə dizayn edərdim,
  istehsalda onu necə işlədərdim və müsahibədə necə izah edərdim — bunların hamısını ayrıca fəsillərdə yazmışam.

## Kim üçün yazmışam

Java və Spring Boot ilə işləmiş, mikroservis anlayışlarını bilən, lakin ya fasilədən sonra qayıdan, ya da Kafka,
Elasticsearch, gRPC, Spring Cloud, Resilience4j və observability kimi mövzuları real kod üzərində dərindən görmək
istəyən mühəndis üçün. Müsahibəyə hazırlaşırsınızsa, hər fəslin sonundakı suallar və 24-cü fəsil birbaşa sizin
üçündür.

## Bu kitab nə deyil

Bu, Spring-in bütün imkanlarını sadalayan arayış kitabı deyil. Burada yalnız bu sistemdə istifadə olunan və onu
başa düşmək üçün lazım olan mövzular var — amma onlar dərinliyi ilə. Kitabda gördüyünüz bütün versiyalar (Java 25,
Spring Boot 4.1, Spring Cloud 2025.1 və s.) repozitoriyanın təhlil olunan vəziyyətinə aiddir; tövsiyələri öz
mühitinizdə tətbiq etməzdən əvvəl rəsmi sənədlərlə yoxlayın.

## Necə oxumalı

Ən yaxşı nəticə üçün kitabı repozitoriya ilə yanaşı, iki pəncərədə oxuyun: bir tərəfdə bu kitab, digər tərəfdə
IDE. Oxu yollarını, etiketləri və kitabın quruluşunu 1-ci fəsildə ətraflı izah edirəm.

Uğurlar. Ümid edirəm ki, bu kitab sizə də mənə kömək etdiyi qədər kömək edəcək.

**AAR**
Oktyabr 2026
