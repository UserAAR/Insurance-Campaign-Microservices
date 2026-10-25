"""Əhatə matrisini (coverage matrix) yaradır.

Hər əhəmiyyətli repozitoriya faylı üçün: modul, funksional məsuliyyət, əsas siniflər/metodlar, runtime rolu,
bələdçidə hansı fəsillərdə (yol və ya fayl adı ilə) istinad edildiyi, əhatə statusu, açıq suallar və
verify_refs.py nəticəsinə əsasən yoxlama statusu.

İstifadə: python3 build/coverage.py   (repozitoriya kökündən və ya istənilən yerdən)
Nəticə:   analysis/coverage-matrix.md
"""
import json
import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
GUIDE = os.path.dirname(HERE)
REPO = os.path.abspath(os.path.join(GUIDE, "..", ".."))
CHAPTERS = os.path.join(GUIDE, "chapters")

# Əl ilə doldurulan məlumat: yol -> (məsuliyyət, əsas elementlər, runtime/konfiqurasiya rolu, açıq sual)
INFO = {
    # --- kök
    "pom.xml": ("Parent POM, modul siyahısı, BOM-lar, plugin-lər", "dependencyManagement, surefire/failsafe, jacoco, sonar", "Build", ""),
    "README.md": ("Layihə sənədi", "endpoint cədvəlləri, işə salma", "Sənəd", "Bəzi iddialar kodla uyğun deyil (inventar §5)"),
    ".gitignore": ("Git istisnaları", "target/, logs/, .env", "Build", ""),
    "docker-compose.yml": ("Tam stek (7 tətbiq + infrastruktur)", "servislər, env, volume, network", "Runtime", "Healthcheck şərtləri natamam (F29)"),
    "docker-compose-local.yml": ("Yalnız infrastruktur (servislər IDE-dən)", "postgres, redis, kafka, es, observability", "Runtime", ""),
    ".github/workflows/ci-cd.yml": ("CI/CD: verify → sonar → docker", "matrix build, Docker Hub push", "CI", ""),
    ".github/workflows/mark-done-on-merge.yml": ("Merge olunan PR və bağlı issue-larda `done` label", "github-script", "Repo avtomatlaşdırması", ""),
    "postman/Insurance Campaign Microservices.postman_collection.json": ("Manual API test kolleksiyası", "gateway və birbaşa sorğular", "Test aləti", ""),
    # --- common
    "common/pom.xml": ("Ortaq modulun asılılıqları", "spring-kafka optional", "Build", ""),
    "common/src/main/java/com/casestudy/insurancecampaign/common/api/ApiError.java": ("Xəta cavab zərfi", "of(), validation()", "HTTP kontrakt", ""),
    "common/src/main/java/com/casestudy/insurancecampaign/common/api/ApiResponse.java": ("Uğurlu cavab zərfi", "success()", "HTTP kontrakt", ""),
    "common/src/main/java/com/casestudy/insurancecampaign/common/config/KafkaJsonConfiguration.java": ("Kafka producer (String açar, JSON dəyər)", "producerFactory, kafkaTemplate", "Kafka şərti konfiqurasiya", ""),
    "common/src/main/java/com/casestudy/insurancecampaign/common/config/VirtualThreadConfiguration.java": ("@EnableAsync + virtual thread executor", "applicationTaskExecutor", "Asinxron icra", "Trace kontekst ötürülmür (F34)"),
    "common/src/main/java/com/casestudy/insurancecampaign/common/event/CampaignStatusChangedEvent.java": ("Kampaniya status hadisəsi", "record", "Kafka mesajı", "Consumer yoxdur (F12)"),
    "common/src/main/java/com/casestudy/insurancecampaign/common/event/SlowRequestLogEvent.java": ("Yavaş sorğu hadisəsi", "record", "Kafka mesajı", ""),
    "common/src/main/java/com/casestudy/insurancecampaign/common/exception/BusinessException.java": ("Biznes istisna bazası", "ErrorCode", "Xəta modeli", ""),
    "common/src/main/java/com/casestudy/insurancecampaign/common/exception/DuplicateCampaignException.java": ("Təkrar kampaniya", "DUPLICATE_CAMPAIGN", "Xəta modeli", ""),
    "common/src/main/java/com/casestudy/insurancecampaign/common/exception/ErrorCode.java": ("Xəta kodları", "8 dəyər", "Xəta kontraktı", ""),
    "common/src/main/java/com/casestudy/insurancecampaign/common/exception/GlobalExceptionHandler.java": ("Mərkəzi istisna xəritələnməsi", "5 @ExceptionHandler", "MVC xəta emalı", "Catch-all 4xx-ləri 500 edir (F07)"),
    "common/src/main/java/com/casestudy/insurancecampaign/common/exception/InvalidStatusTransitionException.java": ("Yanlış status keçidi", "INVALID_STATUS_TRANSITION", "Xəta modeli", ""),
    "common/src/main/java/com/casestudy/insurancecampaign/common/exception/ResourceNotFoundException.java": ("Resurs tapılmadı", "RESOURCE_NOT_FOUND", "Xəta modeli", ""),
    "common/src/main/java/com/casestudy/insurancecampaign/common/observability/HttpServerObservationConfiguration.java": ("Span adı və actuator filtri", "ObservationPredicate, ObservationFilter", "Tracing", ""),
    "common/src/main/java/com/casestudy/insurancecampaign/common/web/SlowRequestLoggingFilter.java": ("Yavaş sorğuları Kafka-ya göndərir", "doFilterInternal, shouldNotFilter", "Servlet filter", "Kafka bloklaması (F10)"),
    # --- security
    "security/pom.xml": ("Security modul asılılıqları", "jjwt", "Build", ""),
    "security/src/main/java/com/casestudy/insurancecampaign/security/JwtAuthenticationFilter.java": ("Bearer token → SecurityContext", "doFilterInternal", "Security filter", "JwtException tutulmur (F08)"),
    "security/src/main/java/com/casestudy/insurancecampaign/security/JwtProperties.java": ("app.jwt.* xassələri", "rsaEnabled()", "Konfiqurasiya", ""),
    "security/src/main/java/com/casestudy/insurancecampaign/security/JwtTokenService.java": ("JWT imzalama/doğrulama (RS256/HMAC)", "generateToken, parseClaims", "Security", ""),
    "security/src/main/java/com/casestudy/insurancecampaign/security/PemKeyUtils.java": ("PEM açar oxuma", "readPublicKey, readPrivateKey", "Security", ""),
    "security/src/main/java/com/casestudy/insurancecampaign/security/ServiceSecurityConfiguration.java": ("SecurityFilterChain + method security", "securityFilterChain, writeError", "Security", ""),
    "security/src/main/resources/keys/public.pem": ("RSA public key (demo)", "", "Konfiqurasiya", ""),
    # --- grpc-contract
    "grpc-contract/pom.xml": ("protoc + grpc-java generasiyası", "protobuf-maven-plugin", "Build", ""),
    "grpc-contract/src/main/proto/campaign.proto": ("gRPC kontraktı", "CampaignGrpcService, 6 mesaj", "Servis kontraktı", ""),
    # --- discovery
    "discovery-server/pom.xml": ("Eureka server asılılıqları", "", "Build", ""),
    "discovery-server/Dockerfile": ("Image", "", "Deploy", ""),
    "discovery-server/src/main/java/com/casestudy/insurancecampaign/discovery/DiscoveryServerApplication.java": ("Eureka Server", "@EnableEurekaServer", "Platform", ""),
    "discovery-server/src/main/java/com/casestudy/insurancecampaign/discovery/observability/DiscoveryServerHttpServerObservationConfiguration.java": ("Observation (kopya)", "", "Tracing", "Təkrar kod (F23)"),
    "discovery-server/src/main/resources/application.yml": ("Eureka konfiqurasiyası", "self-preservation off", "Konfiqurasiya", ""),
    # --- config
    "config-server/pom.xml": ("Config server asılılıqları", "", "Build", ""),
    "config-server/Dockerfile": ("Image", "", "Deploy", ""),
    "config-server/src/main/java/com/casestudy/insurancecampaign/config/ConfigServerApplication.java": ("Config Server", "@EnableConfigServer", "Platform", ""),
    "config-server/src/main/java/com/casestudy/insurancecampaign/config/observability/ConfigServerHttpServerObservationConfiguration.java": ("Observation (kopya)", "", "Tracing", "Təkrar kod (F23)"),
    "config-server/src/main/resources/application.yml": ("native profil, search-locations", "", "Konfiqurasiya", ""),
    "config-server/src/main/resources/config/application.yml": ("Bütün klientlərə paylanan konfiqurasiya", "kafka consumer, app.*", "Mərkəzi konfiqurasiya", "F32"),
    # --- gateway
    "api-gateway/pom.xml": ("Gateway asılılıqları", "gateway-server-webflux, redis-reactive", "Build", ""),
    "api-gateway/Dockerfile": ("Image", "", "Deploy", ""),
    "api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/ApiGatewayApplication.java": ("Gateway giriş nöqtəsi", "", "Platform", ""),
    "api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/config/GatewayHttpServerObservationConfiguration.java": ("Observation (kopya)", "", "Tracing", "F23"),
    "api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/config/GatewayLoadBalancerConfiguration.java": ("@LoadBalanced WebClient.Builder", "", "Kəşf", ""),
    "api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/config/GatewayRateLimiterConfiguration.java": ("Rate limit açar resolver-i", "requestRateLimiterKeyResolver", "Rate limiting", "XFF spoofing (F05)"),
    "api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/filter/GatewayJwtAuthenticationFilter.java": ("Gateway JWT yoxlaması", "filter, getOrder", "GlobalFilter", ""),
    "api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/filter/GatewayRateLimitErrorResponseFilter.java": ("429 üçün JSON gövdə", "ServerHttpResponseDecorator", "GlobalFilter", ""),
    "api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/openapi/GatewayOpenApiController.java": ("OpenAPI aqreqasiyası + fallback", "openApi", "Sənədləşmə", ""),
    "api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/security/GatewayJwtProperties.java": ("Gateway app.jwt.*", "", "Konfiqurasiya", ""),
    "api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/security/GatewayJwtTokenService.java": ("Gateway JWT doğrulama", "parseClaims", "Security", ""),
    "api-gateway/src/main/java/com/casestudy/insurancecampaign/gateway/security/GatewayPemKeyUtils.java": ("Public key oxuma", "", "Security", ""),
    "api-gateway/src/main/resources/application.yml": ("Marşrutlar, rate limit, springdoc", "", "Konfiqurasiya", ""),
    "api-gateway/src/main/resources/keys/public.pem": ("RSA public key", "", "Konfiqurasiya", ""),
}


# Bu skriptin özünün yaratdığı fəsil əhatə hesabına daxil edilmir (dairəvi istinad olmasın).
GENERATED_PREFIX = "26-"


def chapter_texts():
    out = {}
    for name in sorted(os.listdir(CHAPTERS)):
        if name.endswith(".md") and not name.startswith(GENERATED_PREFIX):
            with open(os.path.join(CHAPTERS, name), encoding="utf-8") as f:
                out[name] = f.read()
    return out


def path_mentioned(path, text):
    """Kök səviyyəli fayl (məs. `pom.xml`) `auth-service/pom.xml`-in alt sətri kimi sayılmasın."""
    if "/" in path:
        return path in text
    return re.search(r"(?<![\w/.-])" + re.escape(path) + r"(?![\w])", text) is not None


def module_of(path):
    first = path.split("/")[0]
    return first if "/" in path else "(kök)"


def default_info(path):
    base = os.path.basename(path)
    if path.endswith("Dockerfile"):
        return ("Çoxmərhələli image build", "maven → amazoncorretto:25", "Deploy", "")
    if path.endswith("pom.xml"):
        return ("Modul asılılıqları", "", "Build", "")
    if "/src/test/" in path:
        if base.endswith(".pem"):
            return ("Test RSA açarı", "", "Test", "")
        return ("Test: " + base.replace(".java", ""), "", "Test", "")
    if path.endswith(".pem"):
        return ("RSA açarı (demo)", "", "Konfiqurasiya", "Private key repozitoriyada (F22)" if "private" in base else "")
    if path.endswith("application.yml"):
        return ("Servis konfiqurasiyası", "port, datasource, kafka, tracing, app.*", "Konfiqurasiya", "")
    if path.endswith(".yml") and "changelog" in path:
        return ("Liquibase changelog", "changeSet-lər", "Sxem miqrasiyası", "")
    if path.startswith("observability/"):
        return ("Observability konfiqurasiyası: " + base, "", "Runtime (observability)", "")
    if path.endswith(".java"):
        return (base.replace(".java", ""), "", "Tətbiq kodu", "")
    return (base, "", "", "")



SHORT_PREFIXES = ("src/main/java/com/casestudy/insurancecampaign/", "src/test/java/com/casestudy/insurancecampaign/")


def short_path(path):
    mod = module_of(path)
    rest = path[len(mod) + 1:] if path.startswith(mod + "/") else path
    for pre in SHORT_PREFIXES:
        if rest.startswith(pre):
            kind = "main" if "/main/" in pre else "test"
            return f"{kind}: " + rest[len(pre):]
    return rest


def findings_register():
    rows = []
    path = os.path.join(GUIDE, "analysis", "01-architecture-map.md")
    with open(path, encoding="utf-8") as f:
        for line in f:
            m = re.match(r"\| (F\d\d) \| ([^|]+) \| ([^|]+) \|", line)
            if m:
                rows.append((m.group(1), m.group(2).strip(), m.group(3).strip()))
    return rows


def shorten(text, limit=150):
    if len(text) <= limit:
        return text
    cut = text[:limit].rsplit(" ", 1)[0]
    return cut.rstrip(",;:—-") + " …"


def write_chapter(rows, texts):
    by_module = {}
    for r in rows:
        by_module.setdefault(r[1], []).append(r)
    total = len(rows)
    covered = sum(1 for r in rows if r[6] == "Əhatə olunub")
    verified = sum(1 for r in rows if r[8] == "OK")
    out = [
        "# 26. Əhatə matrisi və tapıntı indeksi",
        "",
        "> Bu fəsil `build/coverage.py` tərəfindən avtomatik yaradılır; əl ilə redaktə etməyin. Tam (9 sütunlu) matris:",
        "> `analysis/coverage-matrix.md`.",
        "",
        "## 26.1 Necə oxumalı",
        "",
        "- **Fəsillər** — faylın tam yolu və ya sinif/fayl adı ilə istinad olunduğu fəsillər (bu fəsil hesaba daxil deyil).",
        "- **Yoxlama** — `OK`: həmin fayla edilən bütün `fayl:sətir` istinadları və kod çıxarışları `build/verify_refs.py`",
        "  ilə avtomatik doğrulanıb; `—`: faylın sətir səviyyəli istinadı yoxdur (yalnız adla xatırlanır).",
        "- Yollar qısaldılıb: `main:` = `src/main/java/com/casestudy/insurancecampaign/`, `test:` = eyni test qovluğu.",
        "",
        f"Cəmi izlənən fayl: **{total}**; ən azı bir fəsildə istinad olunan: **{covered}**; sətir səviyyəsində "
        f"avtomatik doğrulanan: **{verified}**.",
        "",
        "## 26.2 Modullar üzrə xülasə",
        "",
        "| Modul | Fayl | İstinad olunan | Sətir səviyyəsində doğrulanan |",
        "|---|---:|---:|---:|",
    ]
    for mod in sorted(by_module):
        rs = by_module[mod]
        out.append(f"| `{mod}` | {len(rs)} | {sum(1 for r in rs if r[6] == 'Əhatə olunub')} | "
                   f"{sum(1 for r in rs if r[8] == 'OK')} |")
    out += ["", "## 26.3 Fayl səviyyəsində matris", ""]
    for mod in sorted(by_module):
        out += [f"### {mod}", "", "| Fayl | Məsuliyyət | Fəsillər | Yoxlama |",
                "|------------------------------------|------------------------------|------------------------|----------|"]
        for r in by_module[mod]:
            resp = r[2].replace("|", "\\|")
            out.append(f"| `{short_path(r[0])}` | {resp} | {r[5]} | {r[8]} |")
        out.append("")
    out += [
        "## 26.4 Tapıntı indeksi",
        "",
        "Kateqoriyalar fəsil 21.1-dədir. Tam mətn və sübut: `analysis/01-architecture-map.md` və fəsil 21.",
        "",
        "| ID | Kateqoriya | Tapıntı (qısa) | Fəsillər |",
        "|-----|--------------|--------------------------------------------------------|-------------------------|",
    ]
    for fid, cat, text in findings_register():
        chs = sorted({name[:2] for name, t in texts.items() if re.search(r"\b" + fid + r"\b", t)})
        out.append(f"| {fid} | {cat} | {shorten(text).replace('|', '/')} | {', '.join(chs) or '—'} |")
    target = os.path.join(CHAPTERS, GENERATED_PREFIX + "coverage-matrix.md")
    with open(target, "w", encoding="utf-8") as f:
        f.write("\n".join(out) + "\n")
    print(f"{target}: yaradıldı")


def main():
    with open(os.path.join(REPO, "docs/backend-engineering-guide/analysis/.files.txt"), encoding="utf-8") as f:
        files = [l.strip() for l in f if l.strip()]
    texts = chapter_texts()
    verify = {}
    vpath = os.path.join(GUIDE, "audit", "verify-refs.json")
    if os.path.exists(vpath):
        with open(vpath, encoding="utf-8") as f:
            verify = json.load(f).get("per_file", {})

    rows = []
    covered = 0
    for p in files:
        resp, elems, role, question = INFO.get(p) or default_info(p)
        base = os.path.basename(p)
        stem = base.rsplit(".", 1)[0] if not base.startswith(".") else base
        refs = []
        for ch, text in texts.items():
            if path_mentioned(p, text) or (base not in ("application.yml", "pom.xml", "Dockerfile", "public.pem", "private.pem", "db.changelog-master.yml") and re.search(r"\b" + re.escape(stem) + r"\b", text)):
                refs.append(ch[:2])
        status = "Əhatə olunub" if refs else "Əhatə olunmayıb"
        if refs:
            covered += 1
        v = verify.get(p)
        vstat = "—" if v is None else ("OK" if v.get("errors", 0) == 0 else f"{v['errors']} xəta")
        rows.append((p, module_of(p), resp, elems, role, ", ".join(refs) or "—", status, question or "—", vstat))

    lines = [
        "# Əhatə matrisi (coverage matrix)",
        "",
        "> Avtomatik yaradılıb: `build/coverage.py`. Fəsil sütunu fəsil fayllarında faylın tam yolu və ya sinif/fayl adı",
        "> ilə istinadlardan hesablanır. Yoxlama sütunu `audit/verify-refs.json` (verify_refs.py) nəticəsidir:",
        "> həmin fayla edilən `fayl:sətir` istinadlarının və kod çıxarışlarının doğrulanması.",
        "",
        f"Cəmi fayl: **{len(rows)}**, ən azı bir fəsildə istinad olunan: **{covered}**.",
        "",
        "| Fayl | Modul | Məsuliyyət | Əsas elementlər | Rol | Fəsillər | Status | Açıq sual | Yoxlama |",
        "|---|---|---|---|---|---|---|---|---|",
    ]
    for r in rows:
        lines.append("| `" + r[0] + "` | " + " | ".join(x.replace("|", "\\|") for x in r[1:]) + " |")
    out = os.path.join(GUIDE, "analysis", "coverage-matrix.md")
    with open(out, "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")
    print(f"{out}: {len(rows)} fayl, {covered} əhatə olunub")
    write_chapter(rows, texts)
    missing = [r[0] for r in rows if r[6] != "Əhatə olunub"]
    if missing:
        print("Əhatə olunmayan:", *missing, sep="\n  ")


if __name__ == "__main__":
    sys.exit(main())
