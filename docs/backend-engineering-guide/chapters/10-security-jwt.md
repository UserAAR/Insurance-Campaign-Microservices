# 10. Spring Security və JWT — bütün sistem üzrə təhlükəsizlik

## 10.1 Problem: mikroservislərdə "bu sorğunu kim göndərib?"

Monolitdə istifadəçi sessiyası server yaddaşında saxlanılır. Mikroservislərdə sorğu bir neçə prosesdən keçir və hər
biri müstəqil qərar verməlidir: istifadəçi kimdir, hansı rolları var, token etibarlıdırmı. Sessiyanı paylaşmaq
(sticky session, mərkəzi sessiya bazası) miqyaslanmanı çətinləşdirir. **Stateless token** yanaşması bunu həll edir:
identifikasiya mərkəzi (auth-service) imzalı token verir, hər servis imzanı **lokal olaraq** yoxlayır.

## 10.2 JWT nəzəriyyəsi

**JWT** (JSON Web Token, RFC 7519) üç Base64URL hissədən ibarətdir: `header.payload.signature`.

```text
header  = {"alg":"RS256"}                                    (JJWT əlavə sahələr də yaza bilər)
payload = {"iss":"allianz-campaign-api","sub":"admin","roles":["ROLE_ADMIN"],"iat":1767000000,"exp":1767007200}
signature = RSASSA-PKCS1-v1_5-SHA256(base64url(header) + "." + base64url(payload), privateKey)
```

- Payload **şifrələnmir**, yalnız imzalanır — həssas məlumat qoyulmamalıdır. Layihədə yalnız username və rollar var.
- **Standart claim-lər**: `iss` (kim buraxıb), `sub` (kimin üçün), `iat` (nə vaxt), `exp` (nə vaxt bitir), `aud`
  (kim üçün nəzərdə tutulub — layihədə yoxdur), `jti` (unikal id — yoxdur).
- **Xüsusi claim**: `roles` — siyahı.

**İmza alqoritmləri:**

| | HMAC (HS256) | RSA (RS256) |
|---|---|---|
| Açar | Bir paylaşılan sirr | Private (imzalama) + public (yoxlama) |
| Kim token yarada bilər | Sirri bilən **hər** servis | Yalnız private key sahibi |
| Mikroservislər üçün | Bütün servislər sirri bilməlidir — bir servisin sızması hamısını təhlükəyə atır | Servislər yalnız public key bilir — sızma token yaratmağa imkan vermir |

Layihə hər iki rejimi dəstəkləyir, lakin RSA istifadə edir (README: "This replaces the old shared JWT_SECRET
approach"). [FAKT] `JwtProperties.rsaEnabled()` hər hansı açar xassəsi təyin olunubsa RSA rejimini seçir
(`JwtProperties.java:56-58`); bütün servislərin `application.yml`-ində `app.jwt.public-key-location` var.

**Doğrulama addımları** (JJWT `parseSignedClaims`): (1) formatı ayır; (2) header-dəki alqoritmin açar tipinə uyğunluğunu
yoxla; (3) imzanı public key ilə yoxla; (4) `exp`-i yoxla (`ExpiredJwtException`), `nbf` varsa onu da; (5) tələb olunan
claim-ləri yoxla — burada `requireIssuer` (`IncorrectClaimException`/`MissingClaimException`).

[NƏTİCƏ] **Alqoritm qarışdırma hücumu** (alg confusion: RS256 token-i public key-i HMAC sirri kimi istifadə edən
HS256 token ilə əvəzləmək) JJWT 0.12-də `verifyWith(PublicKey)` istifadə edildikdə mümkün deyil: açar tipi gözlənilən
alqoritm ailəsini müəyyən edir. `alg: none` da qəbul olunmur, çünki `parseSignedClaims` imza tələb edir.

## 10.3 Açarların idarə edilməsi

```mermaid
%% caption: RSA açarlarının paylanması (repozitoriyadakı faktiki fayllar)
flowchart LR
    PK[auth-service/src/main/resources/keys/private.pem]
    PUB1[auth-service/.../keys/public.pem]
    PUB2[security/src/main/resources/keys/public.pem]
    PUB3[api-gateway/src/main/resources/keys/public.pem]
    AUTH[auth-service: imzalayır və yoxlayır]
    SVC[campaign, dashboard, log: yoxlayır]
    GW[api-gateway: yoxlayır]
    PK --> AUTH
    PUB1 --> AUTH
    PUB2 -->|security jar ilə classpath-də| SVC
    PUB3 --> GW
```

[FAKT] Public key-in üç nüsxəsi var. auth-service `security` modulundan da asılı olduğu üçün classpath-də iki
`keys/public.pem` olur; [NƏTİCƏ] `classpath:keys/public.pem` birinci tapılanı qaytarır (adətən tətbiqin öz resursları
asılılıq jar-larından əvvəl gəlir). Nüsxələr eyni olduqca problem yoxdur; açar rotasiyasında hamısının eyni anda
yenilənməsi lazımdır — bu, əl ilə idarə olunan kövrək prosesdir.

`PemKeyUtils` açarı belə oxuyur [FAKT] (`PemKeyUtils.java:108-124`):
1. Inline PEM (`app.jwt.public-key`) varsa — onu istifadə et (mühit dəyişəni ilə ötürmək üçün rahatdır);
2. Yoxdursa `location`-u Spring `DefaultResourceLoader` ilə aç (`classpath:`, `file:`, URL);
3. Resurs tapılmasa — dəyəri adi fayl yolu kimi oxu;
4. PEM başlıqlarını və boşluqları sil, Base64 decode et (`:138-144`);
5. Public key — `X509EncodedKeySpec`; private key — **PKCS#8** (`-----BEGIN PRIVATE KEY-----`). PKCS#1 formatı
   (`BEGIN RSA PRIVATE KEY`) dəstəklənmir, xəta mesajı bunu açıq deyir (`:83-91`).

**Daha yaxşı alternativ — JWKS.** Auth server public açarlarını `/.well-known/jwks.json` endpoint-ində `kid` (key id)
ilə nəşr edir; resource server-lər açarı oradan yükləyib cache edir. Açar rotasiyası: yeni açar əlavə olunur, köhnə token-lər
köhnə `kid` ilə yoxlanmağa davam edir. Bu layihədə `kid` və JWKS yoxdur.

## 10.4 Servlet servislərində Spring Security arxitekturası

```mermaid
%% caption: Servlet servisində Spring Security filtr zənciri (layihə konfiqurasiyasına görə sadələşdirilmiş)
flowchart TB
    A[Tomcat] --> B[DelegatingFilterProxy springSecurityFilterChain]
    B --> C[FilterChainProxy]
    C --> D[SecurityContextHolderFilter]
    D --> E[HeaderWriterFilter: təhlükəsizlik başlıqları]
    E --> F[JwtAuthenticationFilter: Bearer → SecurityContext]
    F --> G[UsernamePasswordAuthenticationFilter: bu layihədə form login yoxdur]
    G --> H[AnonymousAuthenticationFilter]
    H --> I[ExceptionTranslationFilter: 401 / 403 handler-ləri]
    I --> J[AuthorizationFilter: permitAll və ya authenticated]
    J --> K[DispatcherServlet → @PreAuthorize → controller]
```

<!-- src: security/src/main/java/com/casestudy/insurancecampaign/security/ServiceSecurityConfiguration.java:75-109 -->
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, authException) -> writeError(
                            request,
                            response,
                            HttpStatus.UNAUTHORIZED,
                            ErrorCode.UNAUTHORIZED.name(),
                            "Authentication is required"
                    ))
                    .accessDeniedHandler((request, response, accessDeniedException) -> writeError(
                            request,
                            response,
                            HttpStatus.FORBIDDEN,
                            ErrorCode.FORBIDDEN.name(),
                            "Access denied"
                    ))
            )
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/api/v1/auth/**",
                            "/actuator/**",
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/grpc.health.v1.Health/**"
                    ).permitAll()
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
}
```

Hər qərarın izahı:

| Konfiqurasiya | Niyə | Qeyd |
|---|---|---|
| `csrf.disable()` | CSRF hücumu brauzerin cookie-ləri avtomatik göndərməsinə əsaslanır. Token `Authorization` başlığında göndərildikdə brauzer onu avtomatik əlavə etmir | Token cookie-də saxlanılsaydı, CSRF qorunması lazım olardı |
| `STATELESS` | `HttpSession` yaradılmır; hər sorğu öz token-i ilə autentifikasiya olunur | Miqyaslanma, sticky session yoxdur |
| `authenticationEntryPoint` | Autentifikasiya olmadıqda `ApiError` JSON (401) | Standart cavab `WWW-Authenticate` başlığı ilə boş gövdə olardı |
| `accessDeniedHandler` | Filtr səviyyəsində 403 üçün JSON | `@PreAuthorize` 403-ü isə `GlobalExceptionHandler` verir |
| `permitAll` siyahısı | Auth endpoint-ləri, actuator, OpenAPI, Swagger | `/grpc.health.v1.Health/**` mənasızdır: gRPC ayrı Netty server-də (9090) işləyir, HTTP filtrindən keçmir |
| `anyRequest().authenticated()` | Qalan hər şey token tələb edir | Rol yoxlaması metod səviyyəsindədir |
| `addFilterBefore(jwt, UsernamePasswordAuthenticationFilter)` | JWT filtri anonim autentifikasiya və avtorizasiyadan əvvəl işləməlidir | — |
| `@EnableMethodSecurity` | `@PreAuthorize` aktivdir | `ServiceSecurityConfiguration.java:35-38` |

[RİSK] (F40) `/actuator/**` `permitAll`-dur və `health,info,prometheus,metrics` açıqdır. Compose servis portlarını
host-a açdığı üçün metriklər autentifikasiyasız oxunur. Metriklər daxili endpoint adlarını, versiyaları, yük
nümunələrini göstərir. Tövsiyə: actuator-u ayrı idarə portuna (`management.server.port`) çıxarmaq və şəbəkə
səviyyəsində məhdudlaşdırmaq və ya yalnız `health`-i açıq saxlamaq.

### `JwtAuthenticationFilter`

<!-- src: security/src/main/java/com/casestudy/insurancecampaign/security/JwtAuthenticationFilter.java:67-84 -->
```java
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
    String authorization = request.getHeader("Authorization");
    if (authorization != null && authorization.startsWith("Bearer ")) {
        String token = authorization.substring(7);
        Claims claims = jwtTokenService.parseClaims(token);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        Collection<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                claims.getSubject(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    filterChain.doFilter(request, response);
}
```

- Başlıq yoxdursa filtr heç nə etmir; sorğu anonim davam edir və `AuthorizationFilter` qorunan yol üçün 401 verir.
- `roles` claim-indən `SimpleGrantedAuthority` yaradılır. Token-də `ROLE_ADMIN` yazıldığı üçün `hasRole('ADMIN')`
  (avtomatik `ROLE_` prefiksi əlavə edir) işləyir.
- Üç arqumentli `UsernamePasswordAuthenticationToken` konstruktoru obyekti **autentifikasiya olunmuş** kimi
  işarələyir. Principal sadə `String` username-dir.

Problemlər və incəliklər:
1. **İstisnalar tutulmur** (F08) — fəsil 8.6-ya baxın.
2. **`roles` claim-i yoxdursa** — `claims.get("roles", List.class)` `null` qaytarır və `roles.stream()`
   `NullPointerException` atır. Yalnız auth-service token verdiyi və hər token-də `roles` olduğu üçün praktikada baş
   vermir, lakin müdafiə kodu yoxdur.
3. **İkiqat qeydiyyat** [NƏTİCƏ] — filtr `@Component`-dir və `OncePerRequestFilter`-dən törəyir. Spring Boot
   kontekstdəki hər `Filter` bean-ini **servlet konteynerinə də** avtomatik qeydiyyatdan keçirir. Beləliklə filtr həm
   security zəncirində, həm də adi servlet filtri kimi mövcuddur. `OncePerRequestFilter` sorğu atributu ilə ikinci icranı
   ötürür, ona görə funksional problem yoxdur, lakin bu, gizli asılılıqdır. Təmiz variant: filtri `@Bean` kimi
   yaradıb `FilterRegistrationBean.setEnabled(false)` ilə servlet qeydiyyatını söndürmək və ya `@Component`-i çıxarıb
   yalnız security konfiqurasiyasında `new` ilə yaratmaq.
4. `"Bearer "` prefiksi böyük-kiçik hərfə həssasdır (RFC 6750 sxem adını case-insensitive sayır).

## 10.5 Metod səviyyəli avtorizasiya

`@EnableMethodSecurity` `@PreAuthorize`-ı aktivləşdirir. İfadələr SpEL-dir; `hasRole('ADMIN')` →
`ROLE_ADMIN` authority-si. Yoxlama controller bean-inin proxy-sində, metod çağırılmazdan əvvəl edilir; rədd edildikdə
Spring Security 6.3+ `AuthorizationDeniedException` (`AccessDeniedException` alt sinfi) atır.

Rol matrisi [FAKT]:

| Endpoint | ADMIN | USER | INTERNAL_SERVICE | Anonim | Mənbə |
|---|:-:|:-:|:-:|:-:|---|
| `POST /api/v1/campaigns` | ✓ | — | — | — | `CampaignController.java:43-44` |
| `GET /api/v1/campaigns`, `/{id}` | ✓ | ✓ | — | — | `:51-52`, `:58-59` |
| `PATCH /{id}/approve`, `/{id}/deactivate` | ✓ | — | — | — | `:67-68`, `:74-75` |
| `GET /{id}/history`, `/statistics` | ✓ | — | — | — | `:81-82`, `:88-89` |
| `GET /api/v1/dashboard/**` | ✓ | — | — | — | `DashboardController.java:40-41`, `:53-54` |
| `GET /api/v1/logs/slow-requests` | ✓ | — | — | — | `SlowRequestLogController.java:36-37` |
| `POST /api/v1/logs/auth-audit` | — | — | ✓ | — | `AuthAuditLogController.java:30-31` |
| `POST /api/v1/auth/**` | ✓ | ✓ | ✓ | ✓ | `ServiceSecurityConfiguration.java:97-104` |
| gRPC `CampaignGrpcService/*` (9090) | açıq | açıq | açıq | **açıq** | F04 |

[NƏTİCƏ] Avtorizasiya **son servisdə** edilir — gateway yalnız token-in etibarlılığını yoxlayır, rolu yox. Bu,
düzgün prinsipdir: servis öz qaydalarının sahibidir və gateway-dən yan keçən sorğu da (birbaşa port) eyni qaydalara
tabedir. README də bunu vurğulayır: "the final authorization decision is made inside campaign-service".

## 10.6 Gateway-də JWT yoxlaması (WebFlux)

<!-- src: api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/filter/GatewayJwtAuthenticationFilter.java:75-95 -->
```java
@Override
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getURI().getPath();
    if (OPEN_PATHS.stream().anyMatch(path::startsWith)) {
        return chain.filter(exchange);
    }

    String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authorization == null || !authorization.startsWith("Bearer ")) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    try {
        jwtTokenService.parseClaims(authorization.substring(7));
        return chain.filter(exchange);
    } catch (RuntimeException exception) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
```

- `GlobalFilter` — bütün marşrutlara tətbiq olunur. Sıra `100` (`:108-111`): marşrutun `RequestRateLimiter` filtri
  (default filter, sıra 1) ondan əvvəl işləyir, ona görə token-siz sorğular da əvvəlcə rate limit-ə düşür.
- Açıq yollar prefiks uyğunluğu ilə yoxlanılır (`OPEN_PATHS`, `:39-50`). `startsWith` ilə `/api/v1/authXYZ` də açıq
  sayılar — praktiki riski yoxdur, çünki belə marşrut yoxdur.
- Token yoxlanılır, lakin claim-lər **istifadə olunmur** və aşağı axına əlavə başlıq (məs. `X-User`) ötürülmür;
  `Authorization` başlığı olduğu kimi servisə gedir və servis onu yenidən yoxlayır (**müdafiə dərinliyi**). Bədəli:
  hər sorğuda imza iki dəfə (gateway + servis), rate limit açarı üçün isə üçüncü dəfə yoxlanılır
  (`GatewayRateLimiterConfiguration.java:46-58`). RS256 yoxlaması ucuzdur, lakin bunu bilmək lazımdır.
- Uğursuzluqda **boş gövdəli 401** — servislərin `ApiError` formatından fərqlidir.
- [NƏTİCƏ] Reaktiv kodda `parseClaims` CPU əməliyyatıdır və Netty event-loop thread-ində icra olunur; qısa olduğu
  üçün qəbuledilir. Bloklayan I/O isə event-loop-da qadağandır.

Gateway Spring Security istifadə etmir (`spring-boot-starter-security` asılılığı yoxdur — `api-gateway/pom.xml`), ona
görə təhlükəsizlik başlıqları (`X-Content-Type-Options` və s.) gateway cavablarına əlavə olunmur; servislərdən gələn
cavablarda isə Spring Security-nin standart başlıqları var.

## 10.7 Servislərarası autentifikasiya

| Kanal | Mexanizm | Qiymətləndirmə |
|---|---|---|
| Gateway → servislər | İstifadəçinin token-i ötürülür | Düzgün: servis istifadəçi kontekstində qərar verir |
| auth-service → log-service | Hər sorğu üçün yeni daxili JWT: `sub=auth-service`, `roles=[ROLE_INTERNAL_SERVICE]`, private key ilə imzalı | Yaxşı nümunə: log-service yalnız bu rolu qəbul edir |
| dashboard → campaign (gRPC) | **Autentifikasiya yoxdur** | [RİSK] F04 |

<!-- src: auth-service/src/main/java/com/casestudy/insurancecampaign/auth/config/LogServiceHttpClientConfiguration.java:32-48 -->
```java
@Bean
RestClientHttpServiceGroupConfigurer logServiceHttpServiceGroupConfigurer(
        JwtTokenService jwtTokenService
) {
    return groups -> groups.forEachClient((group, clientBuilder) -> {
        if (!LOG_SERVICE_GROUP.equals(group.name())) {
            return;
        }

        clientBuilder.defaultRequest(request -> request.headers(headers ->
                headers.setBearerAuth(jwtTokenService.generateToken(
                        SERVICE_SUBJECT,
                        List.of(INTERNAL_SERVICE_ROLE)
                ))
        ));
    });
}
```

[NƏTİCƏ] `defaultRequest` callback-i hər sorğu qurulanda çağırılır, buna görə hər audit çağırışı yeni token (RSA imza
əməliyyatı) yaradır. Token 120 dəqiqə etibarlı olduğu üçün cache-lənə bilərdi; audit yükü aşağı olduğundan bu, kiçik
optimallaşdırma imkanıdır. Daha vacib məqam: `ROLE_INTERNAL_SERVICE` token-ini yalnız private key sahibi (auth-service)
yarada bilər — `UserRole` enum-unda bu rol yoxdur, ona görə istifadəçi qeydiyyatı ilə əldə edilə bilməz.

**gRPC kanalı (F04).** [FAKT] campaign-service gRPC server-i 9090-da işləyir, `ServiceSecurityConfiguration` yalnız
HTTP-ni qoruyur. net.devh-in gRPC security auto-configuration-u (`GrpcServerSecurityAutoConfiguration`)
`@ConditionalOnBean(AuthenticationManager.class)` ilə qorunur (kitabxana mənbəsi), layihədə isə `AuthenticationManager`
bean-i yoxdur. dashboard-service stub-a token əlavə etmir. Compose 9090-ı host-a açır (`docker-compose.yml:182-184`).
[RİSK] Nəticə: istənilən şəbəkə klienti (`grpcurl -plaintext localhost:9090 ...`) bütün statistikanı və istənilən
kampaniyanın tarixçəsini autentifikasiyasız oxuya bilər; həmçinin net.devh standart olaraq reflection və health
servislərini aktiv edir, bu da API-nin kəşfini asanlaşdırır. Həllər: portu yalnız daxili şəbəkəyə açmaq, mTLS, və ya
JWT-ni gRPC metadata-da (`authorization` başlığı) ötürüb server tərəfdə interceptor ilə yoxlamaq.

## 10.8 Gateway-dən yan keçmə

[FAKT] Compose 8081–8084 portlarını da host-a açır. Birbaşa servisə gedən sorğu:
- **rate limit**-dən yan keçir (rate limit yalnız gateway-dədir);
- autentifikasiya və avtorizasiyadan yan keçə **bilmir** — servislər token-i özləri yoxlayır;
- auth endpoint-ləri birbaşa portda da açıqdır → brute-force login gateway limitsiz mümkündür.

[NƏTİCƏ] İstehsalda servislər yalnız daxili şəbəkədən əlçatan olmalı, xarici trafik yalnız gateway (və ya ingress)
vasitəsilə gəlməlidir. Lokal Compose-da portların açıq olması developer rahatlığıdır.

## 10.9 Təhdid modeli xülasəsi

| Təhdid | Mövcud müdafiə | Boşluq | Tapıntı |
|---|---|---|---|
| Saxta token | RS256, public key ilə yoxlama | Private key repozitoriyadadır | F22 |
| Oğurlanmış access token | 120 dəq ömür | Ləğv mexanizmi yoxdur, ömür uzundur | F35 |
| Oğurlanmış refresh token | Rotasiya, hash-lənmiş saxlama | Reuse detection yoxdur, yarış | F17 |
| İmtiyaz eskalasiyası | `@PreAuthorize` | Qeydiyyatda `ROLE_ADMIN` seçilir | F01 |
| Hesabın ələ keçirilməsi | Reset token tək istifadəlik, 15 dəq | Token cavabda qaytarılır | F02 |
| Brute-force login | Gateway rate limit (IP) | XFF saxtalaşdırma, birbaşa port, Redis düşəndə fail-open | F05, F06 |
| Enumerasiya | — | Fərqli mesajlar, timing, 404 | F03 |
| Daxili API-yə icazəsiz giriş | log-service rol yoxlaması | gRPC tamamilə açıq | F04 |
| Məlumat sızması | — | 500-də istisna mesajı, açıq actuator | F07, F40 |

## 10.10 Yeni layihədə tətbiq: Spring Security OAuth2 Resource Server

İllüstrativ nümunə (repozitoriyada yoxdur) — xüsusi filtr yazmadan eyni funksionallıq. Spring Security-nin
`oauth2ResourceServer` dəstəyi token çıxarmanı, xəta emalını (`WWW-Authenticate`), istisnaları və authority
çevrilməsini standart olaraq edir:

```java
@Bean
SecurityFilterChain api(HttpSecurity http, JwtDecoder decoder) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(a -> a
            .requestMatchers("/api/v1/auth/**", "/actuator/health").permitAll()
            .anyRequest().authenticated())
        .oauth2ResourceServer(o -> o.jwt(j -> j
            .decoder(decoder)
            .jwtAuthenticationConverter(rolesConverter())))
        .build();
}

@Bean
JwtDecoder jwtDecoder(@Value("${app.jwt.public-key-location}") RSAPublicKey key) {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(key).build();
    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("allianz-campaign-api"));
    return decoder;                                    // və ya NimbusJwtDecoder.withJwkSetUri(".../jwks.json")
}

private JwtAuthenticationConverter rolesConverter() {
    JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
    roles.setAuthoritiesClaimName("roles");
    roles.setAuthorityPrefix("");                     // token-də artıq ROLE_ prefiksi var
    JwtAuthenticationConverter c = new JwtAuthenticationConverter();
    c.setJwtGrantedAuthoritiesConverter(roles);
    return c;
}
```

Gateway üçün analoji: `ServerHttpSecurity.oauth2ResourceServer(o -> o.jwt(...))` və ya `TokenRelay` filtri.

## 10.11 Müsahibə sualları

**S1. Niyə mikroservislərdə RS256 HS256-dan üstündür?**
Cavab: HS256-da yoxlayan hər servis imzalaya da bilər; sirr bütün servislərdə olmalıdır. RS256-da yalnız auth-service
private key-ə malikdir; digərləri public key ilə yalnız yoxlayır. Bir servisin kompromisi token saxtalaşdırmağa imkan
vermir.

**S2. Stateless JWT ilə CSRF-i söndürmək nə vaxt təhlükəsizdir?**
Cavab: Token brauzerin avtomatik göndərmədiyi yerdə (Authorization başlığı) saxlanıldıqda. Token cookie-də olarsa,
brauzer onu cross-site sorğularda da göndərir və CSRF qorunması (SameSite, CSRF token) lazımdır.

**S3. Gateway token-i yoxlayırsa, servislər niyə yenidən yoxlayır?**
Cavab: Müdafiə dərinliyi (gateway-dən yan keçən daxili trafik, səhv konfiqurasiya), servisin öz avtorizasiya qərarı
üçün claim-lərə ehtiyacı, və "zero trust" prinsipi — şəbəkə daxilində olmaq etibar sayılmır.

**S4. `hasRole('ADMIN')` və `hasAuthority('ROLE_ADMIN')` fərqi?**
Cavab: `hasRole` avtomatik `ROLE_` prefiksi əlavə edir; nəticədə eyni yoxlamadır. Authority-lər prefikssiz saxlanılırsa
(`ADMIN`), `hasRole` işləməz, `hasAuthority('ADMIN')` lazımdır.

**S5. Filtr daxilində atılan `JwtException` niyə `@ControllerAdvice` ilə tutulmur və necə düzəltmək olar?**
Cavab: Filtr `DispatcherServlet`-dən əvvəl işləyir. Həll: filtrdə tutub `AuthenticationEntryPoint`-ə ötürmək,
`HandlerExceptionResolver`-i filtrə inject edib istifadə etmək və ya standart `BearerTokenAuthenticationFilter`-dən
(oauth2ResourceServer) istifadə etmək.

**S6. gRPC endpoint-ini necə qorumaq olar?**
Cavab: Şəbəkə izolyasiyası, mTLS (kanal səviyyəsində servis identifikasiyası), metadata-da JWT və server interceptor
(net.devh-də `GrpcAuthenticationReader` + `AuthenticationManager` və ya xüsusi `ServerInterceptor`), metod səviyyəli
icazələr.

## 10.12 Xülasə

- RS256 JWT: auth-service imzalayır, digərləri public key ilə lokal yoxlayır.
- Servlet servisləri ortaq `security` modulundan `SecurityFilterChain` + `JwtAuthenticationFilter` + method security
  alır; gateway WebFlux `GlobalFilter` ilə öz yoxlamasını edir; avtorizasiya son servisdə.
- Servislərarası: auth → log daxili rol ilə qorunur, dashboard → campaign gRPC qorunmur.
- Əsas risklər: özünü admin qeydiyyatı, reset token sızması, açıq gRPC və actuator, repozitoriyadakı private key,
  filtr istisnaları.
- IV hissə texnologiyaları müstəqil öyrədir; ilk olaraq Apache Kafka.
