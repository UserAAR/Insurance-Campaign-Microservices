// Backend Engineering Guide — PDF qurma skripti.
//
// Axın: chapters/*.md -> (mermaid -> SVG, etiketlər, mənbə istinadları) -> pandoc HTML ->
//        bir HTML sənəd -> Chromium PDF (keçid 1, outline) -> səhifə nömrələri -> mündəricat ->
//        Chromium PDF (keçid 2, son nəticə).
//
// İstifadə:  MERMAID_JS=/path/to/mermaid.min.js node build/build.mjs
// Tələblər:  pandoc (3.x), Node + playwright (Chromium), python3 + pypdf.

import { createRequire } from 'node:module';
import { execFileSync } from 'node:child_process';
import crypto from 'node:crypto';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const require = createRequire(import.meta.url);
const { chromium } = require('playwright');

const BUILD_DIR = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(BUILD_DIR, '..');
const CHAPTER_DIR = path.join(ROOT, 'chapters');
const DIAGRAM_DIR = path.join(ROOT, 'diagrams');
const RENDERED_DIR = path.join(DIAGRAM_DIR, 'rendered');
const OUT_DIR = path.join(ROOT, 'output');
const WORK_DIR = path.join(BUILD_DIR, '.work');
const BOOK = JSON.parse(fs.readFileSync(process.env.BOOK_JSON || path.join(BUILD_DIR, 'book.json'), 'utf8'));
const MERMAID_JS = process.env.MERMAID_JS
  || path.join(BUILD_DIR, 'node_modules', 'mermaid', 'dist', 'mermaid.min.js');
// Mermaid konfiqurasiyası dəyişəndə keşi yeniləmək üçün versiya.
const RENDER_CONFIG_VERSION = 'v2';

fs.mkdirSync(RENDERED_DIR, { recursive: true });
fs.mkdirSync(OUT_DIR, { recursive: true });
fs.mkdirSync(WORK_DIR, { recursive: true });

const escapeHtml = (s) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

// ---------------------------------------------------------------- Mermaid
async function renderMermaid(browser, jobs) {
  const pending = jobs.filter((j) => !fs.existsSync(j.svgPath));
  if (pending.length === 0) return;
  if (!fs.existsSync(MERMAID_JS)) throw new Error(`mermaid.min.js tapılmadı: ${MERMAID_JS}`);
  const page = await browser.newPage();
  await page.setContent('<!doctype html><html><body></body></html>');
  await page.addScriptTag({ path: MERMAID_JS });
  await page.evaluate(() => {
    // eslint-disable-next-line no-undef
    mermaid.initialize({
      startOnLoad: false,
      theme: 'base',
      securityLevel: 'strict',
      fontFamily: 'DejaVu Sans, Arial, sans-serif',
      themeVariables: {
        fontFamily: 'DejaVu Sans, Arial, sans-serif',
        fontSize: '13px',
        primaryColor: '#e8f1f8',
        primaryBorderColor: '#1f5f8b',
        primaryTextColor: '#1d232b',
        lineColor: '#4a5a6a',
        secondaryColor: '#f3f6e9',
        tertiaryColor: '#fbfcfd',
        noteBkgColor: '#fff8e1',
        noteBorderColor: '#b26a00',
        edgeLabelBackground: '#ffffff',
      },
      themeCSS: '.edgeLabel .label rect.background{fill:#ffffff;opacity:0.95;} .edgeLabel .label text{fill:#1d232b;}',
      flowchart: { htmlLabels: false, curve: 'basis', useMaxWidth: true },
      sequence: { useMaxWidth: true, mirrorActors: false, wrap: true, actorFontSize: 12, messageFontSize: 12, noteFontSize: 11, width: 130, boxMargin: 6 },
      er: { useMaxWidth: true },
      state: { useMaxWidth: true },
    });
  });
  for (const job of pending) {
    const svg = await page.evaluate(async ({ id, code }) => {
      // eslint-disable-next-line no-undef
      const { svg } = await mermaid.render(id, code);
      return svg;
    }, { id: `m${job.hash.slice(0, 10)}`, code: job.code }).catch((e) => {
      throw new Error(`Mermaid render xətası (${job.name}): ${e.message}`);
    });
    fs.writeFileSync(job.svgPath, svg);
    console.log(`  diagram -> ${path.relative(ROOT, job.svgPath)}`);
  }
  await page.close();
}

// ---------------------------------------------------------------- Markdown preprocess
const TAGS = [
  ['[FAKT]', '<span class="tag tag-fact">FAKT</span>'],
  ['[NƏTİCƏ]', '<span class="tag tag-infer">NƏTİCƏ</span>'],
  ['[NAMƏLUM]', '<span class="tag tag-unknown">NAMƏLUM</span>'],
  ['[RİSK]', '<span class="tag tag-risk">RİSK</span>'],
];

function preprocess(md, chapterKey, mermaidJobs) {
  let n = 0;
  // mermaid blokları -> fiqur yer tutanları
  md = md.replace(/```mermaid\n([\s\S]*?)```/g, (_, code) => {
    n += 1;
    const captionMatch = code.match(/^%%\s*caption:\s*(.+)$/m);
    const caption = captionMatch ? captionMatch[1].trim() : '';
    const name = `${chapterKey}-${String(n).padStart(2, '0')}`;
    const hash = crypto.createHash('sha1').update(RENDER_CONFIG_VERSION + code).digest('hex');
    fs.writeFileSync(path.join(DIAGRAM_DIR, `${name}.mmd`), code);
    const svgPath = path.join(RENDERED_DIR, `${name}-${hash.slice(0, 8)}.svg`);
    mermaidJobs.push({ name, code, hash, svgPath });
    return `\n<div class="mermaid-placeholder" data-svg="${path.basename(svgPath)}" data-caption="${escapeHtml(caption)}"></div>\n`;
  });
  // mənbə istinadları: <!-- src: path:lines --> -> görünən sətir
  md = md.replace(/<!--\s*src:\s*(.+?)\s*-->/g, (_, ref) =>
    `<div class="src-ref">Mənbə: ${escapeHtml(ref)}</div>`);
  // etiketlər (kod bloklarının içində istifadə olunmur)
  for (const [k, v] of TAGS) md = md.split(k).join(v);
  return md;
}

function injectDiagrams(html) {
  return html.replace(/<div class="mermaid-placeholder" data-svg="([^"]+)" data-caption="([^"]*)">\s*<\/div>/g,
    (_, file, caption) => {
      let svg = fs.readFileSync(path.join(RENDERED_DIR, file), 'utf8');
      svg = svg.replace(/<\?xml[^>]*>/, '');
      const cap = caption ? `<figcaption>${caption}</figcaption>` : '';
      return `<figure class="diagram">${svg}${cap}</figure>`;
    });
}

function pandoc(md, idPrefix) {
  return execFileSync('pandoc', [
    '-f', 'markdown+pipe_tables+fenced_divs+raw_html+backtick_code_blocks+fenced_code_attributes+lists_without_preceding_blankline-smart',
    '-t', 'html5',
    '--highlight-style=pygments',
    `--id-prefix=${idPrefix}`,
    '--wrap=none',
  ], { input: md, maxBuffer: 64 * 1024 * 1024 }).toString('utf8');
}

// ---------------------------------------------------------------- Assemble
function stripTags(s) { return s.replace(/<[^>]+>/g, '').replace(/&amp;/g, '&').replace(/&lt;/g, '<').replace(/&gt;/g, '>').replace(/&quot;/g, '"').replace(/&#39;/g, "'").trim(); }

function buildDocument(chaptersHtml, pageMap) {
  const tocItems = [];
  let body = '';
  for (const part of BOOK.parts) {
    const partId = `part-${part.key}`;
    tocItems.push({ level: 'part', id: partId, title: part.title });
    body += `<section class="part-title" id="${partId}"><h1>${escapeHtml(part.title)}</h1>${part.subtitle ? `<p>${escapeHtml(part.subtitle)}</p>` : ''}</section>\n`;
    for (const ch of part.chapters) {
      const html = chaptersHtml.get(ch);
      body += `<section class="chapter">${html}</section>\n`;
      const re = /<h([12]) id="([^"]+)"[^>]*>([\s\S]*?)<\/h\1>/g;
      let m;
      while ((m = re.exec(html)) !== null) {
        tocItems.push({ level: m[1] === '1' ? 1 : 2, id: m[2], title: stripTags(m[3]) });
      }
    }
  }
  let pageIdx = 0;
  const toc = tocItems.map((it) => {
    const pg = pageMap ? (pageMap[pageIdx++] ?? '') : '000';
    if (it.level === 'part') {
      return `<li class="lvl-part"><a href="#${it.id}">${escapeHtml(it.title)}</a></li>`;
    }
    return `<li class="lvl${it.level}"><a class="toc-entry" href="#${it.id}"><span class="t">${escapeHtml(it.title)}</span><span class="dots"></span><span class="pg">${pg}</span></a></li>`;
  }).join('\n');

  const meta = BOOK.meta;
  const cover = `
<section class="cover">
  <div class="kicker">${escapeHtml(meta.kicker)}</div>
  <h1 class="title">${escapeHtml(meta.title)}</h1>
  <div class="subtitle">${escapeHtml(meta.subtitle)}</div>
  <div class="meta">${meta.lines.map((l) => `<p>${escapeHtml(l)}</p>`).join('')}</div>
</section>`;
  const html = `<!doctype html>
<html lang="az"><head><meta charset="utf-8">
<title>${escapeHtml(meta.title)}</title>
<link rel="stylesheet" href="style.css">
</head><body>
${cover}
<nav class="toc"><h1 id="toc">Mündəricat</h1><ol>
${toc}
</ol></nav>
${body}
</body></html>`;
  return { html, tocItems };
}

async function printPdf(browser, htmlPath, pdfPath) {
  const page = await browser.newPage();
  await page.goto(`file://${htmlPath}`, { waitUntil: 'load' });
  await page.evaluate(async () => { await document.fonts.ready; });
  const footer = `<div style="font-family: DejaVu Sans, sans-serif; font-size:7px; color:#6b7684; width:100%; padding:0 17mm 0 19mm; display:flex; justify-content:space-between;">
    <span>${escapeHtml(BOOK.meta.footer)}</span><span><span class="pageNumber"></span> / <span class="totalPages"></span></span></div>`;
  await page.pdf({
    path: pdfPath,
    format: 'A4',
    printBackground: true,
    preferCSSPageSize: true,
    displayHeaderFooter: true,
    headerTemplate: '<div></div>',
    footerTemplate: footer,
    outline: true,
    tagged: true,
  });
  await page.close();
}

function readOutline(pdfPath) {
  const out = execFileSync('python3', [path.join(BUILD_DIR, 'outline.py'), pdfPath]).toString('utf8');
  return JSON.parse(out);
}

function norm(s) {
  let t = s.replace(/\s+/g, ' ').trim();
  // Chromium bəzən tagged PDF-də başlıq mətnini iki dəfə birləşdirir: "XX" -> "X"
  if (t.length % 2 === 0 && t.slice(0, t.length / 2) === t.slice(t.length / 2)) t = t.slice(0, t.length / 2);
  return t;
}

function squash(s) { return s.replace(/\s+/g, ''); }

function fuzzyEqual(tocTitle, outlineTitle) {
  const a = squash(tocTitle);
  const b = squash(outlineTitle);
  if (a === b || norm(tocTitle) === norm(outlineTitle)) return true;
  const head = a.slice(0, Math.min(10, a.length));
  const tail = a.slice(-Math.min(8, a.length));
  return b.startsWith(head) && b.endsWith(tail);
}

function mapPages(tocItems, outline) {
  // Chromium outline-ı başlıq səviyyələrinə görə qurur: h1 → depth 0, h2 → depth 1.
  // Üz qabığı başlığı və "Mündəricat" başlığı ilk iki depth-0 elementidir; qalanları TOC sırası ilə eynidir.
  const top = outline.filter((o) => o.depth <= 1).slice(2);
  const pages = [];
  for (let i = 0; i < tocItems.length; i++) {
    const o = top[i];
    if (o && fuzzyEqual(tocItems[i].title, o.title)) {
      pages.push(String(o.page));
    } else {
      // ehtiyat: ardıcıl axtarış
      const found = outline.find((x) => fuzzyEqual(tocItems[i].title, x.title));
      pages.push(found ? String(found.page) : '?');
    }
  }
  return pages;
}

// ---------------------------------------------------------------- Main
async function main() {
  const browser = await chromium.launch();
  const mermaidJobs = [];
  const chaptersMd = new Map();
  const files = BOOK.parts.flatMap((p) => p.chapters);
  for (const f of files) {
    const md = fs.readFileSync(path.join(CHAPTER_DIR, f), 'utf8');
    const key = path.basename(f, '.md').slice(0, 2);
    chaptersMd.set(f, preprocess(md, `ch${key}`, mermaidJobs));
  }
  console.log(`Fəsillər: ${files.length}, diaqramlar: ${mermaidJobs.length}`);
  await renderMermaid(browser, mermaidJobs);

  const chaptersHtml = new Map();
  for (const f of files) {
    const key = path.basename(f, '.md').slice(0, 2);
    chaptersHtml.set(f, injectDiagrams(pandoc(chaptersMd.get(f), `c${key}-`)));
  }

  fs.copyFileSync(path.join(BUILD_DIR, 'style.css'), path.join(WORK_DIR, 'style.css'));
  fs.cpSync(path.join(BUILD_DIR, 'fonts'), path.join(WORK_DIR, 'fonts'), { recursive: true });
  const htmlPath = path.join(WORK_DIR, 'guide.html');
  const pass1Pdf = path.join(WORK_DIR, 'pass1.pdf');
  const finalPdf = path.join(OUT_DIR, BOOK.meta.pdfName);

  let { html, tocItems } = buildDocument(chaptersHtml, null);
  fs.writeFileSync(htmlPath, html);
  console.log('Keçid 1: PDF yaradılır...');
  await printPdf(browser, htmlPath, pass1Pdf);
  const outline = readOutline(pass1Pdf);
  const pageMap = mapPages(tocItems.filter((t) => true), outline);
  const missing = tocItems.filter((_, i) => pageMap[i] === '?').map((t) => t.title);
  if (missing.length) console.warn(`Xəbərdarlıq: ${missing.length} başlıq outline-da tapılmadı:`, missing.slice(0, 10));

  ({ html } = buildDocument(chaptersHtml, pageMap));
  fs.writeFileSync(htmlPath, html);
  console.log('Keçid 2: son PDF yaradılır...');
  await printPdf(browser, htmlPath, finalPdf);
  const outline2 = readOutline(finalPdf);
  const pageMap2 = mapPages(tocItems, outline2);
  const drift = pageMap2.filter((p, i) => p !== pageMap[i]).length;
  if (drift > 0) {
    console.log(`Səhifə sürüşməsi: ${drift} başlıq — keçid 3 aparılır`);
    ({ html } = buildDocument(chaptersHtml, pageMap2));
    fs.writeFileSync(htmlPath, html);
    await printPdf(browser, htmlPath, finalPdf);
  }
  await browser.close();
  fs.writeFileSync(path.join(WORK_DIR, 'toc-pages.json'), JSON.stringify(tocItems.map((t, i) => ({ ...t, page: pageMap2[i] })), null, 1));
  console.log(`Hazırdır: ${path.relative(ROOT, finalPdf)}`);
}

main().catch((e) => { console.error(e); process.exit(1); });
