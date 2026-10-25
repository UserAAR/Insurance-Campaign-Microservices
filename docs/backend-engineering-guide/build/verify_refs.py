"""Audit B: mənbə istinadlarının və kod çıxarışlarının texniki doğrulanması.

Yoxlanır:
  1. Fəsillərdəki bütün `fayl:sətir` və `fayl:başlanğıc-son` istinadları — fayl mövcuddur, sətir aralığı faylın
     uzunluğundan kənara çıxmır. Yalnız fayl adı (məs. `CampaignService.java:54`) verilibsə, repozitoriyada tək
     uyğunluq olmalıdır.
  2. `<!-- src: fayl:a-b -->` ilə işarələnmiş hər kod blokunun hər mənalı sətri həmin faylın a..b aralığında
     eyni ardıcıllıqla mövcuddur. Buraxılmış hissələr `...` və ya `(buraxılıb)` sətirləri ilə göstərilir və
     yoxlanmır.

İstifadə: python3 build/verify_refs.py   ->   audit/verify-refs.json + konsol xülasəsi
"""
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
GUIDE = os.path.dirname(HERE)
REPO = os.path.abspath(os.path.join(GUIDE, "..", ".."))
CHAPTERS = os.path.join(GUIDE, "chapters")

EXT = r"(?:java|ya?ml|xml|proto|json|pem|md)"
REF_RE = re.compile(r"(?<![\w/.-])((?:[\w.\-]+/)*(?:[\w.\-]+\." + EXT + r"|Dockerfile)):(\d+)(?:-(\d+))?")
SRC_RE = re.compile(r"<!--\s*src:\s*(\S+?):(\d+)(?:-(\d+))?\s*-->\s*\n```[^\n]*\n(.*?)```", re.S)


def repo_files():
    out = []
    for root, dirs, files in os.walk(REPO):
        dirs[:] = [d for d in dirs if d not in (".git", "target", "node_modules", "docs", "screenshots")]
        for f in files:
            out.append(os.path.relpath(os.path.join(root, f), REPO))
    return out


FILES = repo_files()
BY_BASENAME = {}
for f in FILES:
    BY_BASENAME.setdefault(os.path.basename(f), []).append(f)
CACHE = {}


def resolve(path):
    if path in FILES:
        return path, None
    cands = [f for f in FILES if f.endswith("/" + path)] if "/" in path else BY_BASENAME.get(path, [])
    if len(cands) == 1:
        return cands[0], None
    if not cands:
        return None, f"fayl tapılmadı: {path}"
    return None, f"qeyri-müəyyən fayl adı ({len(cands)} uyğunluq): {path}"


def lines_of(path):
    if path not in CACHE:
        with open(os.path.join(REPO, path), encoding="utf-8") as f:
            CACHE[path] = f.read().split("\n")
        if CACHE[path] and CACHE[path][-1] == "":
            CACHE[path].pop()
    return CACHE[path]


def norm(s):
    return re.sub(r"\s+", " ", s.strip())


def is_elision(line):
    t = line.strip()
    return (not t) or "(buraxılıb)" in t or t in ("...", "// ...", "# ...", "<!-- ... -->", "/* ... */") \
        or re.fullmatch(r"(//|#|--|<!--)?\s*\.\.\..*", t) is not None


def main():
    errors = []
    per_file = {}
    total_refs = 0
    total_excerpts = 0

    def bump(path, key):
        d = per_file.setdefault(path, {"refs": 0, "excerpts": 0, "errors": 0})
        d[key] += 1

    for name in sorted(os.listdir(CHAPTERS)):
        if not name.endswith(".md"):
            continue
        text = open(os.path.join(CHAPTERS, name), encoding="utf-8").read()
        # 1) istinadlar
        for m in REF_RE.finditer(text):
            raw, a, b = m.group(1), int(m.group(2)), int(m.group(3) or m.group(2))
            total_refs += 1
            path, err = resolve(raw)
            if err:
                errors.append({"chapter": name, "ref": m.group(0), "error": err})
                continue
            bump(path, "refs")
            n = len(lines_of(path))
            if a < 1 or b < a or b > n:
                errors.append({"chapter": name, "ref": m.group(0), "error": f"aralıq faylın uzunluğundan kənardır ({n} sətir)"})
                per_file[path]["errors"] += 1
        # 2) kod çıxarışları
        for m in SRC_RE.finditer(text):
            raw, a, b, code = m.group(1), int(m.group(2)), int(m.group(3) or m.group(2)), m.group(4)
            total_excerpts += 1
            path, err = resolve(raw)
            if err:
                errors.append({"chapter": name, "ref": f"src {raw}:{a}-{b}", "error": err})
                continue
            bump(path, "excerpts")
            src = [norm(x) for x in lines_of(path)[a - 1:b]]
            pos = 0
            for line in code.split("\n"):
                if is_elision(line):
                    continue
                want = norm(line)
                found = False
                while pos < len(src):
                    if src[pos] == want:
                        found = True
                        pos += 1
                        break
                    pos += 1
                if not found:
                    errors.append({"chapter": name, "ref": f"src {raw}:{a}-{b}", "error": f"sətir tapılmadı və ya sıra pozulub: {line.strip()[:90]}"})
                    per_file[path]["errors"] += 1
                    break

    report = {"total_refs": total_refs, "total_excerpts": total_excerpts, "errors": errors, "per_file": per_file}
    os.makedirs(os.path.join(GUIDE, "audit"), exist_ok=True)
    with open(os.path.join(GUIDE, "audit", "verify-refs.json"), "w", encoding="utf-8") as f:
        json.dump(report, f, ensure_ascii=False, indent=1)
    print(f"İstinadlar: {total_refs}, kod çıxarışları: {total_excerpts}, istinad olunan fayllar: {len(per_file)}, xətalar: {len(errors)}")
    for e in errors[:80]:
        print(f"  [{e['chapter']}] {e['ref']}: {e['error']}")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
