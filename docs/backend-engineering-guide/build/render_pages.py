"""PDF səhifələrini PNG-yə render edir (vizual yoxlama üçün).
İstifadə: python3 build/render_pages.py file.pdf out_dir 1,5,10-12 [scale]"""
import os
import sys

import pypdfium2 as pdfium


def parse(spec, n):
    pages = []
    for part in spec.split(','):
        if '-' in part:
            a, b = part.split('-')
            pages += list(range(int(a), int(b) + 1))
        else:
            pages.append(int(part))
    return [p for p in pages if 1 <= p <= n]


def main():
    pdf = pdfium.PdfDocument(sys.argv[1])
    out = sys.argv[2]
    os.makedirs(out, exist_ok=True)
    scale = float(sys.argv[4]) if len(sys.argv) > 4 else 1.2
    for p in parse(sys.argv[3], len(pdf)):
        pdf[p - 1].render(scale=scale).to_pil().save(os.path.join(out, f"p{p:03d}.png"))
    print(len(pdf), "səhifə")


if __name__ == "__main__":
    main()
