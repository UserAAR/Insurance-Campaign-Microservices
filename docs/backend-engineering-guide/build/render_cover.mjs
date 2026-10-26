// Üz qabığını cover/cover.svg-dən render edir:
//   build/.work/cover.pdf  — kitabın birinci səhifəsi (A4, kənar boşluqsuz, altbilgisiz)
//   output/<coverPng>      — paylaşmaq üçün yüksək keyfiyyətli PNG (2480 × 3508, A4 @ 300 dpi)
// Tək başına: NODE_PATH=$(npm root -g) node build/render_cover.mjs
import fs from 'node:fs';
import path from 'node:path';
import { createRequire } from 'node:module';
import { fileURLToPath } from 'node:url';

const require = createRequire(import.meta.url);
const BUILD_DIR = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.dirname(BUILD_DIR);
const WORK_DIR = path.join(BUILD_DIR, '.work');

export async function renderCover(browser, book) {
  const svg = fs.readFileSync(path.join(ROOT, 'cover', 'cover.svg'), 'utf8');
  fs.mkdirSync(WORK_DIR, { recursive: true });
  fs.mkdirSync(path.join(ROOT, 'output'), { recursive: true });
  const htmlPath = path.join(BUILD_DIR, 'cover.html');
  // style.css yalnız şrift elanları üçün qoşulur; səhifə tamamilə SVG-dir.
  fs.writeFileSync(htmlPath, `<!doctype html><html lang="az"><head><meta charset="utf-8">
<link rel="stylesheet" href="style.css">
<style>@page { size: A4; margin: 0; } html, body { margin: 0; padding: 0; background: #f3eee3; }
svg { display: block; width: 210mm; height: 297mm; }</style></head><body>${svg}</body></html>`);

  const page = await browser.newPage({ viewport: { width: 794, height: 1123 }, deviceScaleFactor: 3.125 });
  await page.goto(`file://${htmlPath}`, { waitUntil: 'load' });
  await page.evaluate(async () => { await document.fonts.ready; });
  const pdfPath = path.join(WORK_DIR, 'cover.pdf');
  await page.pdf({ path: pdfPath, format: 'A4', printBackground: true, preferCSSPageSize: true,
    margin: { top: 0, right: 0, bottom: 0, left: 0 } });
  const pngPath = path.join(ROOT, 'output', book.meta.coverPng);
  await page.screenshot({ path: pngPath, clip: { x: 0, y: 0, width: 794, height: 1123 } });
  await page.close();
  fs.unlinkSync(htmlPath);
  return { pdfPath, pngPath };
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  const { chromium } = require('playwright');
  const book = JSON.parse(fs.readFileSync(process.env.BOOK_JSON || path.join(BUILD_DIR, 'book.json'), 'utf8'));
  const browser = await chromium.launch();
  const out = await renderCover(browser, book);
  await browser.close();
  console.log(`Üz qabığı: ${path.relative(ROOT, out.pdfPath)}, ${path.relative(ROOT, out.pngPath)}`);
}
