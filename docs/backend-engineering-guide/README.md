# Insurance Campaign Microservices — Backend mühəndislik bələdçisi (AZ)

Bu qovluq repozitoriyaya əsaslanan, Azərbaycan dilində yazılmış backend mühəndislik bələdçisinin redaktə oluna bilən
mənbələrini, build alətlərini, analiz və audit nəticələrini saxlayır. Tətbiqin mənbə kodu dəyişdirilməyib.

**Hazır PDF:** `output/Insurance-Campaign-Backend-Engineering-Guide-AZ.pdf`

## Quruluş

| Yol | Məzmun |
|---|---|
| `chapters/01-…25-*.md` | Fəsillər (Markdown, redaktə olunur) |
| `chapters/26-coverage-matrix.md` | `build/coverage.py` tərəfindən **yaradılır** — əl ilə redaktə etməyin |
| `analysis/00-repository-inventory.md` | Fayl inventarı, versiyalar, modullar, README ilə kod arasında fərqlər |
| `analysis/01-architecture-map.md` | Komponentlər, əlaqə kanalları, tapıntılar reyestri (F01–F46), uğursuzluq təsiri |
| `analysis/02-curriculum.md` | Fəsil planı və etiket konvensiyaları |
| `analysis/coverage-matrix.md` | Tam əhatə matrisi (178 fayl × 9 sütun) — yaradılır |
| `analysis/.files.txt` | Əhatə hesablamasında nəzərə alınan repozitoriya faylları |
| `diagrams/*.mmd` | Fəsillərdən çıxarılan Mermaid mənbələri (build zamanı yaradılır) |
| `diagrams/rendered/*.svg` | Render olunmuş diaqramlar (keş; mənbə dəyişəndə yenilənir) |
| `audit/audit-report.md` | Audit A/B/C hesabatı, düzəlişlər və məhdudiyyətlər |
| `audit/verify-refs.json`, `audit/pdf-audit.json` | Avtomatik yoxlamaların maşın oxunan nəticələri |
| `build/` | Build və yoxlama skriptləri, stil, şriftlər |

## Tələblər

- Node.js 20+ və Playwright + Chromium (qlobal quraşdırılmış; `require('playwright')` işləməlidir)
- `pandoc` 3.x
- Python 3.10+ və `pypdf`
- Mermaid 11.12.0: `cd build && npm install` (və ya `MERMAID_JS=/yol/mermaid.min.js` mühit dəyişəni)

## Build

```bash
cd docs/backend-engineering-guide

# 1. İstinadları yoxla (Audit B): hər fayl:sətir istinadı və kod çıxarışı mənbə ilə müqayisə olunur
python3 build/verify_refs.py

# 2. Əhatə matrisini və 26-cı fəsli yenilə
python3 build/coverage.py

# 3. PDF-i qur (Mermaid → SVG, pandoc → HTML, Chromium → PDF; mündəricat səhifələri iki keçidlə hesablanır)
NODE_PATH=$(npm root -g) node build/build.mjs

# 4. PDF bütövlüyünü yoxla (Audit C): mündəricat səhifələri, altbilgi, şriftlər, boş səhifələr, simvollar
python3 build/audit_pdf.py

# Vizual yoxlama üçün seçilmiş səhifələri PNG-yə çevirmək
python3 build/render_pages.py output/Insurance-Campaign-Backend-Engineering-Guide-AZ.pdf /tmp/pages 1-5,100 1.0
```

Alternativ kitab faylı: `BOOK_JSON=/yol/book.json node build/build.mjs` (məs. yalnız bir neçə fəsillə sürətli önizləmə).

## Yazı konvensiyaları

- Etiketlər: `[FAKT]` (koddan birbaşa yoxlanılıb), `[NƏTİCƏ]` (məntiqi nəticə, runtime-da yoxlanmayıb), `[NAMƏLUM]`,
  `[RİSK]` — fəsil 1.3.
- Mənbə istinadı: `` `Fayl.java:12-34` `` (fayl adı unikal deyilsə tam yol). Kod çıxarışından əvvəl
  `<!-- src: tam/yol:a-b -->` şərhi yazılır; `verify_refs.py` çıxarışın sətirlərini həmin aralıqda axtarır.
  Buraxılan hissələr `// ... (buraxılıb)` ilə işarələnir.
- Diaqram: ` ```mermaid ` bloku, ilk sətirdə `%% caption: …`.
- Şifrə, token və açar məzmunu heç vaxt təkrar edilmir; nümunələrdə `<...>` yer tutucusu istifadə olunur.

## Şriftlər

Noto Serif, Noto Sans (SIL Open Font License) və JetBrains Mono (SIL OFL) `build/fonts/`-da woff2 formatında saxlanılır;
lisenziya faylları yanındadır.
