"""PDF outline (bookmark) siyahısını sənəd sırası ilə JSON kimi çıxarır.

İstifadə: python3 outline.py guide.pdf
Nəticə:  [{"title": "...", "page": 12, "depth": 0}, ...]  (səhifə nömrələri 1-dən başlayır)
"""
import json
import sys

from pypdf import PdfReader


def walk(reader, items, depth, out):
    for item in items:
        if isinstance(item, list):
            walk(reader, item, depth + 1, out)
            continue
        try:
            page = reader.get_destination_page_number(item) + 1
        except Exception:  # noqa: BLE001 - etibarsız təyinat
            page = None
        out.append({"title": str(item.title), "page": page, "depth": depth})


def main():
    reader = PdfReader(sys.argv[1])
    out = []
    walk(reader, reader.outline, 0, out)
    json.dump(out, sys.stdout, ensure_ascii=False)


if __name__ == "__main__":
    main()
