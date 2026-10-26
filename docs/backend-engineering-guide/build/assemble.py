#!/usr/bin/env python3
"""Üz qabığını kitabın əvvəlinə əlavə edir, səhifə etiketlərini və metadata-nı yazır.

Üz qabığı Chromium-un altbilgisi olmadan ayrıca render olunur (build/render_cover.mjs), çünki eyni PDF
daxilində altbilgi şablonu birinci səhifədən söndürülə bilmir. Əsas mətnin səhifə nömrələri üz qabığını
saymır (kitab ənənəsi): PDF oxucusunda üz qabığı "Üz qabığı", qalan səhifələr 1, 2, 3 ... kimi etiketlənir və
çap olunmuş altbilgi nömrələri ilə üst-üstə düşür. Outline (bookmark) və daxili keçidlər səhifə obyektlərinə
istinad etdiyi üçün əlavə səhifə onları pozmur.

İstifadə: python3 build/assemble.py cover.pdf body.pdf output.pdf
"""
import json
import os
import sys

from pypdf import PdfReader, PdfWriter
from pypdf.constants import PageLabelStyle

HERE = os.path.dirname(os.path.abspath(__file__))


def main():
    cover_path, body_path, out_path = sys.argv[1:4]
    with open(os.path.join(HERE, "book.json"), encoding="utf-8") as f:
        meta = json.load(f)["meta"]

    writer = PdfWriter(clone_from=body_path)
    body_pages = len(writer.pages)
    writer.insert_page(PdfReader(cover_path).pages[0], 0)
    writer.set_page_label(0, 0, prefix="Üz qabığı")
    writer.set_page_label(1, body_pages, style=PageLabelStyle.DECIMAL, start=1)
    writer.add_metadata({
        "/Title": meta["title"],
        "/Subject": meta["subtitle"],
        "/Author": meta["author"],
        "/Keywords": "Java, Spring Boot, mikroservis, Kafka, gRPC, Elasticsearch, Redis, Spring Cloud",
        "/Creator": "pandoc + Chromium (build/build.mjs)",
    })
    writer.page_mode = "/UseOutlines"
    with open(out_path, "wb") as f:
        writer.write(f)
    print(f"{os.path.relpath(out_path)}: üz qabığı + {body_pages} səhifə")


if __name__ == "__main__":
    main()
