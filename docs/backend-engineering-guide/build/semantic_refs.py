#!/usr/bin/env python3
"""Audit B (semantik hissə): istinad olunan sətir aralığı yanındakı identifikatoru ehtiva edirmi?

`verify_refs.py` yalnız faylın və aralığın mövcudluğunu yoxlayır. Bu skript isə hər `Fayl.ext:a-b` istinadından
əvvəlki eyni cümlədə backtick içində yazılmış identifikatorları (metod, sinif, xassə adları) toplayır və onlardan
heç olmasa birinin həmin sətirlərdə (±2 sətir tolerantlıqla) olub-olmadığını yoxlayır. Uyğunsuzluqlar əl ilə
nəzərdən keçirilməli "şübhəli" siyahı kimi çap olunur — bu, sübut deyil, evristikadır.
"""
import json
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import verify_refs as vr  # noqa: E402

IDENT = re.compile(r"`([^`\n]{2,80})`")
WORD = re.compile(r"[A-Za-z_][\w.-]*[A-Za-z0-9_]")
STOP = {"true", "false", "null", "public", "private", "class", "return", "final", "static", "void", "String",
        "yml", "java", "xml", "main", "src", "resources"}


def candidates(context):
    out = set()
    for token in IDENT.findall(context):
        if re.search(r"\.(java|yml|yaml|xml|proto|sql|json|md):?\d*", token) or "/" in token and ":" in token:
            continue
        for word in WORD.findall(token):
            for part in re.split(r"[.()]", word):
                if len(part) >= 4 and part not in STOP and not part.isdigit():
                    out.add(part)
    return out


def main():
    chapters_dir = os.path.join(vr.GUIDE, "chapters")
    suspects = []
    checked = 0
    for name in sorted(os.listdir(chapters_dir)):
        if not name.endswith(".md") or name.startswith("26-"):
            continue
        text = open(os.path.join(chapters_dir, name), encoding="utf-8").read()
        text = re.sub(r"```.*?```", lambda m: "\n" * m.group(0).count("\n"), text, flags=re.S)
        for m in vr.REF_RE.finditer(text):
            path, a, b = m.group(1), int(m.group(2)), int(m.group(3) or m.group(2))
            resolved, error = vr.resolve(path)
            if error or not resolved:
                continue
            lines = vr.lines_of(resolved)
            start = text.rfind("\n\n", 0, m.start())
            sentence_start = max(start, text.rfind(". ", 0, m.start() - 1), m.start() - 220)
            context = text[sentence_start:m.start()]
            cands = candidates(context)
            if not cands:
                continue
            checked += 1
            window = "\n".join(lines[max(0, a - 3):min(len(lines), b + 2)])
            if not any(c in window for c in cands):
                line_no = text.count("\n", 0, m.start()) + 1
                suspects.append({"chapter": name, "line": line_no, "ref": m.group(0),
                                 "candidates": sorted(cands)[:8]})
    out = os.path.join(vr.GUIDE, "audit", "semantic-refs.json")
    with open(out, "w", encoding="utf-8") as f:
        json.dump({"checked": checked, "suspects": suspects}, f, ensure_ascii=False, indent=1)
    for s in suspects:
        print(f"{s['chapter']}:{s['line']}: {s['ref']}  ← {', '.join(s['candidates'])}")
    print(f"Yoxlanılan istinad: {checked}, şübhəli: {len(suspects)}")


if __name__ == "__main__":
    main()
