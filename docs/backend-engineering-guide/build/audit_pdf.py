#!/usr/bin/env python3
"""Audit C: yaradılmış PDF-in avtomatik bütövlük yoxlamaları.

Yoxlanılır:
  1. Səhifə sayı, outline (bookmark) sayı, PDF metadata.
  2. Mündəricatdakı hər başlığın göstərilən səhifədə həqiqətən olması (mətn çıxarışı ilə).
  3. Boş və ya demək olar ki, boş səhifələr (mətn yoxdur və şəkil/vektor yoxdur).
  4. Şriftlərin hamısının PDF-ə daxil edilməsi (FontFile*).
  5. Əvəzedici simvollar (U+FFFD) və Azərbaycan hərflərinin mətn qatında mövcudluğu.
  6. Hər səhifədə altbilgi nömrəsi ("N / CƏMİ"). Üz qabığı nömrələnmir: o, PDF-in birinci səhifəsidir və
     altbilgisi yoxdur; əsas mətnin nömrələri ondan sonra 1-dən başlayır (səhifə etiketləri ilə uyğun).

İstifadə: python3 build/audit_pdf.py [pdf] [toc-pages.json]
Nəticə: audit/pdf-audit.json; xəta olarsa çıxış kodu 1.
"""
import json
import os
import re
import sys

from pypdf import PdfReader

HERE = os.path.dirname(os.path.abspath(__file__))
GUIDE = os.path.dirname(HERE)


def squash(text):
    return re.sub(r"\s+", "", text).lower()


def walk_outline(items, out):
    for it in items:
        if isinstance(it, list):
            walk_outline(it, out)
        else:
            out.append(it)


def fonts_of(page):
    found = {}
    res = page.get("/Resources")
    if not res or "/Font" not in res:
        return found
    for font in res["/Font"].values():
        font = font.get_object()
        name = str(font.get("/BaseFont"))
        desc = font.get("/FontDescriptor")
        if desc is None and "/DescendantFonts" in font:
            desc = font["/DescendantFonts"][0].get_object().get("/FontDescriptor")
        embedded = False
        if font.get("/Subtype") == "/Type3":
            embedded = True  # Type3 qlifləri PDF daxilində (CharProcs) təsvir olunur
        elif desc is not None:
            desc = desc.get_object()
            embedded = any(k in desc for k in ("/FontFile", "/FontFile2", "/FontFile3"))
        found[name] = embedded
    return found


def main():
    with open(os.path.join(HERE, "book.json"), encoding="utf-8") as f:
        meta = json.load(f)["meta"]
    pdf = sys.argv[1] if len(sys.argv) > 1 else os.path.join(GUIDE, "output", meta["pdfName"])
    toc_path = sys.argv[2] if len(sys.argv) > 2 else os.path.join(HERE, ".work", "toc-pages.json")
    reader = PdfReader(pdf)
    total = len(reader.pages)
    texts = [page.extract_text() or "" for page in reader.pages]
    errors, warnings = [], []

    outline = []
    walk_outline(reader.outline, outline)

    # Üz qabığı: birinci səhifədə altbilgi yoxdursa, ikincidə isə "1 / N" varsa, offset = 1.
    first_footer = re.search(r"\b1\s*/\s*\d+\b", texts[1]) if total > 1 else None
    offset = 1 if first_footer and not re.search(r"\b\d+\s*/\s*\d+\b", texts[0]) else 0
    body_total = total - offset
    labels = list(reader.page_labels) if offset else []
    if offset and labels[:2] != ["Üz qabığı", "1"]:
        errors.append(f"Səhifə etiketləri gözləniləndən fərqlidir: {labels[:3]}")

    # 2. Mündəricat səhifələri
    toc_checked = 0
    with open(toc_path, encoding="utf-8") as f:
        toc = json.load(f)
    for item in toc:
        page = item.get("page")
        if not str(page).isdigit():
            errors.append(f"Mündəricat: səhifə nömrəsi yoxdur: {item['title']}")
            continue
        idx = int(page) - 1 + offset
        title = squash(item["title"])
        # Başlıq uzun olduqda sətirlərə bölünə bilər; ilk 24 simvol kifayətdir.
        needle = title[:24]
        window = squash(texts[idx]) if 0 <= idx < total else ""
        if needle not in window:
            errors.append(f"Mündəricat: '{item['title']}' {page}-ci səhifədə tapılmadı")
        toc_checked += 1

    # 3. Boş səhifələr
    blank = []
    for i, page in enumerate(reader.pages):
        body = re.sub(re.escape(meta["footer"]) + r".*?\d+\s*/\s*\d+", "", texts[i], flags=re.S).strip()
        res = page.get("/Resources") or {}
        has_xobject = "/XObject" in res
        content = page.get_contents()
        size = len(content.get_data()) if content is not None else 0
        if len(body) < 15 and not has_xobject and size < 2500:
            blank.append(i + 1)
    if blank:
        warnings.append(f"Boş və ya demək olar ki, boş səhifələr: {blank}")

    # 4. Şriftlər
    all_fonts = {}
    for page in reader.pages:
        all_fonts.update(fonts_of(page))
    not_embedded = sorted(n for n, e in all_fonts.items() if not e)
    if not_embedded:
        errors.append(f"Daxil edilməmiş şriftlər: {not_embedded}")

    # 5. Simvollar
    joined = "\n".join(texts)
    replacement = joined.count("�")
    if replacement:
        errors.append(f"Mətndə {replacement} əvəzedici simvol (U+FFFD)")
    az_letters = {ch: joined.count(ch) for ch in "əƏğĞıİöÖşŞçÇüÜ"}
    missing_letters = [ch for ch, n in az_letters.items() if n == 0 and ch not in "ĞÖÇÜ"]
    if missing_letters:
        warnings.append(f"Mətn qatında tapılmayan hərflər: {missing_letters}")

    # 6. Altbilgi
    no_footer = [i + 1 for i in range(offset, total)
                 if not re.search(rf"\b{i + 1 - offset}\s*/\s*{body_total}\b", texts[i])]
    if no_footer:
        errors.append(f"Altbilgi nömrəsi olmayan səhifələr: {no_footer[:20]}")

    result = {
        "pdf": os.path.relpath(pdf, GUIDE),
        "pages": total,
        "cover_pages": offset,
        "numbered_pages": body_total,
        "outline_entries": len(outline),
        "toc_entries_checked": toc_checked,
        "fonts": {n: ("daxil edilib" if e else "DAXİL EDİLMƏYİB") for n, e in sorted(all_fonts.items())},
        "azerbaijani_letter_counts": az_letters,
        "metadata": {k: str(v) for k, v in (reader.metadata or {}).items()},
        "errors": errors,
        "warnings": warnings,
    }
    os.makedirs(os.path.join(GUIDE, "audit"), exist_ok=True)
    with open(os.path.join(GUIDE, "audit", "pdf-audit.json"), "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=1)
    print(f"Səhifələr: {total}, outline: {len(outline)}, yoxlanılan mündəricat elementləri: {toc_checked}")
    print(f"Şriftlər: {len(all_fonts)}, daxil edilməyən: {len(not_embedded)}")
    for w in warnings:
        print("XƏBƏRDARLIQ:", w)
    for e in errors[:40]:
        print("XƏTA:", e)
    print(f"Xətalar: {len(errors)}")
    sys.exit(1 if errors else 0)


if __name__ == "__main__":
    main()
