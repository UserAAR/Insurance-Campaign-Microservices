#!/usr/bin/env python3
"""Kitabın üz qabığını SVG kimi yaradır: cover/cover.svg (3-cü versiya — ahtapot).

Kompozisiya (A4, viewBox 2100 × 2970, 1 vahid = 0,1 mm):
  1. Başlıq zolağı — "hub-and-spoke" işarəsi (mərkəz + 8 qovşaq), texnologiyalar.
  2. "Lövhə" — XIX əsr elmi atlas üslubunda çərçivə: ikiqat xətt, kəsim işarələri, millimetrlik tor, servis
     şəbəkəsini xatırladan zəif qovşaqlar. İçində qravüra üslubunda ahtapot (Octopus vulgaris, illüstrasiya
     build/cover_art.py-dadır), "Şəkil 2" vstavkası (qəlsəmələr və üç ürək), A–H anatomik göstəricilər və onların
     sistem qatlarına uyğunluğunu izah edən legenda.
  3. Başlıq paneli — iki rəngli başlıq, altbaşlıq, mövzu "çipləri", kitabın göstəriciləri və müəllif.

Bütün qrafika vektordur (filtr və rastr effekt yoxdur), PDF-də istənilən miqyasda kəskin qalır. Göstəricilər
(fəsil, diaqram, tapşırıq, istinad sayı) kitabın mənbələrindən hesablanır.

İstifadə: python3 build/cover.py
"""
import glob
import json
import math
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import cover_art  # noqa: E402

GUIDE = os.path.dirname(HERE)
OUT = os.path.join(GUIDE, "cover", "cover.svg")

W, H = 2100, 2970
CX = W / 2

# Palitra
INK = "#1c1b19"
PAPER = "#f5f0e6"
PAPER_DEEP = "#ebe2d0"
GRID = "#d9cdb5"
TEAL = "#0e4550"
TEAL_DEEP = "#082f37"
GOLD = "#dca43a"
GOLD_SOFT = "#efc773"
MUTED = "#76695a"

# Lövhənin sərhədləri
PLATE_X0, PLATE_X1 = 110, 1990
PLATE_Y0, PLATE_Y1 = 220, 1810
TITLE_Y0 = 1880
ART_DY = 50          # illüstrasiyanın şaquli sürüşməsi
LEGEND_Y = 1480      # legenda blokunun yuxarısı
INSET = (340, 566, 118)


def az_upper(text):
    """Azərbaycan əlifbası üçün böyük hərf: i → İ, ı → I."""
    return text.replace("i", "İ").replace("ı", "I").upper()


def esc(text):
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


# --------------------------------------------------------------------------- Kitabın göstəriciləri
def book_stats():
    chapters = sorted(glob.glob(os.path.join(GUIDE, "chapters", "[0-9][0-9]-*.md")))
    numbered = [c for c in chapters if not os.path.basename(c).startswith("00-")]
    text = "".join(open(c, encoding="utf-8").read() for c in chapters)
    diagrams = text.count("```mermaid")
    exercises = len(re.findall(r"^\*\*T\d+ ", text, re.M))
    refs = 0
    vr = os.path.join(GUIDE, "audit", "verify-refs.json")
    if os.path.exists(vr):
        refs = json.load(open(vr, encoding="utf-8")).get("total_refs", 0)
    return len(numbered), diagrams, exercises, refs


# --------------------------------------------------------------------------- Lövhə
def plate_background():
    out = [f'<rect x="{PLATE_X0}" y="{PLATE_Y0}" width="{PLATE_X1 - PLATE_X0}" height="{PLATE_Y1 - PLATE_Y0}" '
           f'fill="url(#plateGlow)"/>']
    for x in range(PLATE_X0 + 30, PLATE_X1, 30):
        major = (x - PLATE_X0) % 150 == 0
        out.append(f'<line x1="{x}" y1="{PLATE_Y0}" x2="{x}" y2="{PLATE_Y1}" stroke="{GRID}" '
                   f'stroke-width="{1.6 if major else 0.8}" opacity="{0.55 if major else 0.32}"/>')
    for y in range(PLATE_Y0 + 30, PLATE_Y1, 30):
        major = (y - PLATE_Y0) % 150 == 0
        out.append(f'<line x1="{PLATE_X0}" y1="{y}" x2="{PLATE_X1}" y2="{y}" stroke="{GRID}" '
                   f'stroke-width="{1.6 if major else 0.8}" opacity="{0.55 if major else 0.32}"/>')
    return "\n".join(out)


def crop_marks():
    out, L, g = [], 46, 16
    for x, y, sx, sy in ((PLATE_X0, PLATE_Y0, -1, -1), (PLATE_X1, PLATE_Y0, 1, -1),
                         (PLATE_X0, PLATE_Y1, -1, 1), (PLATE_X1, PLATE_Y1, 1, 1)):
        out.append(f'<line x1="{x + sx * g}" y1="{y}" x2="{x + sx * (g + L)}" y2="{y}" stroke="{MUTED}" '
                   f'stroke-width="2"/>')
        out.append(f'<line x1="{x}" y1="{y + sy * g}" x2="{x}" y2="{y + sy * (g + L)}" stroke="{MUTED}" '
                   f'stroke-width="2"/>')
    return "\n".join(out)


def plate_frame():
    return (f'<rect x="{PLATE_X0}" y="{PLATE_Y0}" width="{PLATE_X1 - PLATE_X0}" height="{PLATE_Y1 - PLATE_Y0}" '
            f'fill="none" stroke="{INK}" stroke-width="5"/>'
            f'<rect x="{PLATE_X0 + 14}" y="{PLATE_Y0 + 14}" width="{PLATE_X1 - PLATE_X0 - 28}" '
            f'height="{PLATE_Y1 - PLATE_Y0 - 28}" fill="none" stroke="{INK}" stroke-width="1.6"/>')


def plate_texts():
    y = PLATE_Y0 + 74
    return "\n".join([
        f'<text x="{PLATE_X0 + 50}" y="{y}" font-family="Guide Sans, sans-serif" font-weight="700" '
        f'font-size="30" letter-spacing="8" fill="{INK}">LÖVHƏ I</text>',
        f'<text x="{PLATE_X0 + 50}" y="{y + 40}" font-family="Guide Serif, serif" font-style="italic" '
        f'font-size="28" fill="{MUTED}">Paylanmış orqanizmin anatomiyası</text>',
        f'<text x="{PLATE_X1 - 50}" y="{y}" text-anchor="end" font-family="Guide Sans, sans-serif" '
        f'font-weight="700" font-size="30" letter-spacing="8" fill="{INK}">ŞƏKİL 1</text>',
        f'<text x="{PLATE_X1 - 50}" y="{y + 40}" text-anchor="end" font-family="Guide Serif, serif" '
        f'font-style="italic" font-size="28" fill="{MUTED}">dorsal görünüş</text>',
    ])


# --------------------------------------------------------------------------- Göstəricilər və legenda
LEGEND = [
    ("A", "Beyin", "API Gateway — giriş, marşrut, rate limit"),
    ("B", "Gözlər", "Təhlükəsizlik — JWT RS256, rollar"),
    ("C", "Qollar", "Servislər — auth · campaign · dashboard · log"),
    ("D", "Sinir halqası", "Hadisələr — Kafka, DLQ"),
    ("E", "Vantuzlar", "Kontraktlar — REST · gRPC · OpenAPI"),
    ("F", "Üç ürək", "Dayanıqlıq — retry · circuit breaker · bulkhead"),
    ("G", "Xromatoforlar", "Müşahidə — metrik · trace · log"),
    ("H", "Mantiya", "Data — PostgreSQL · Redis · Elasticsearch"),
]


def arm_point(arms, index, s, side=0.0):
    a = arms[index]
    i = int(a.n * s)
    x, y = a.side(i, 1, side) if side else a.pts[i]
    return x, y + ART_DY


def callouts(arms):
    """Göstərici xətləri: hədəf nöqtə (orqan) → hərfli dairə. Hədəflər illüstrasiyanın həndəsəsindən hesablanır."""
    dy = ART_DY
    right_mid = next(i for i, a in enumerate(arms) if a.pts[0][0] > CX and abs(math.degrees(a.phis[0]) - 32) < 12)
    left_side = next(i for i, a in enumerate(arms) if a.pts[0][0] < CX and 150 < math.degrees(a.phis[0]) % 360 < 190)
    ix, iy, ir = INSET
    spec = [
        ("A", (CX, 838 + dy), (CX - 300, 1340)),
        ("B", (CX - 152, 846 + dy), (700, 368)),
        ("C", arm_point(arms, right_mid, 0.42), (1880, 1100)),
        ("D", (CX, 948 + dy), (CX + 300, 1340)),
        ("E", arm_point(arms, left_side, 0.30, 0.9), (190, 900)),
        ("F", (ix + 2, iy + 40), (ix + ir + 62, iy + 70)),
        ("G", (CX + 120, 600 + dy), (1560, 560)),
        ("H", (CX + 70, 360 + dy), (1420, 340)),
    ]
    out = []
    for letter, (x0, y0), (x1, y1) in spec:
        out.append(f'<line x1="{x0:.1f}" y1="{y0:.1f}" x2="{x1}" y2="{y1}" stroke="{INK}" stroke-width="2" '
                   f'opacity="0.75"/>')
        out.append(f'<circle cx="{x0:.1f}" cy="{y0:.1f}" r="6" fill="{INK}"/>')
        out.append(f'<circle cx="{x1}" cy="{y1}" r="27" fill="{PAPER}" stroke="{INK}" stroke-width="2.6"/>')
        out.append(f'<text x="{x1}" y="{y1 + 11}" text-anchor="middle" font-family="Guide Serif, serif" '
                   f'font-style="italic" font-weight="700" font-size="32" fill="{INK}">{letter}</text>')
    return "\n".join(out)


def legend():
    y_cap = LEGEND_Y + 40
    out = [f'<rect x="{PLATE_X0 + 30}" y="{LEGEND_Y - 30}" width="{PLATE_X1 - PLATE_X0 - 60}" '
           f'height="{PLATE_Y1 - LEGEND_Y}" fill="{PAPER}" opacity="0.94"/>',
           f'<text x="{CX}" y="{y_cap}" text-anchor="middle" font-family="Guide Serif, serif" font-style="italic" '
           f'font-size="44" fill="{INK}">Octopus vulgaris <tspan font-style="normal" font-size="30" '
           f'fill="{MUTED}">Cuvier, 1797</tspan></text>',
           f'<text x="{CX}" y="{y_cap + 38}" text-anchor="middle" font-family="Guide Sans, sans-serif" '
           f'font-size="22" letter-spacing="6" fill="{MUTED}">MƏRKƏZİ BEYİN · MÜSTƏQİL QOLLAR · ÜÇ ÜRƏK</text>',
           f'<line x1="{PLATE_X0 + 60}" y1="{y_cap + 64}" x2="{PLATE_X1 - 60}" y2="{y_cap + 64}" stroke="{INK}" '
           f'stroke-width="1.4" opacity="0.5"/>']
    col_w = (PLATE_X1 - PLATE_X0 - 120) / 2
    for i, (letter, organ, meaning) in enumerate(LEGEND):
        col, row = i % 2, i // 2
        x = PLATE_X0 + 60 + col * col_w
        y = y_cap + 108 + row * 46
        out.append(f'<circle cx="{x + 18}" cy="{y - 8}" r="17" fill="none" stroke="{INK}" stroke-width="2"/>')
        out.append(f'<text x="{x + 18}" y="{y}" text-anchor="middle" font-family="Guide Serif, serif" '
                   f'font-style="italic" font-weight="700" font-size="22" fill="{INK}">{letter}</text>')
        out.append(f'<text x="{x + 50}" y="{y}" font-family="Guide Sans, sans-serif" font-weight="700" '
                   f'font-size="21" letter-spacing="1.5" fill="{INK}">{esc(az_upper(organ))}</text>')
        out.append(f'<text x="{x + 300}" y="{y}" font-family="Guide Serif, serif" font-size="23" '
                   f'fill="{MUTED}">{esc(meaning)}</text>')
    return "\n".join(out)


# --------------------------------------------------------------------------- Başlıq zolağı və paneli
def hub_icon(cx, cy, r, color):
    """Mərkəz + 8 qovşaq: gateway və servislər (eyni zamanda ahtapotun sxematik işarəsi)."""
    out = [f'<circle cx="{cx}" cy="{cy}" r="{r * 0.36:.1f}" fill="{color}"/>']
    for k in range(8):
        a = math.pi / 4 * k + math.pi / 8
        x, y = cx + r * math.cos(a), cy + r * math.sin(a)
        out.append(f'<line x1="{cx}" y1="{cy}" x2="{x:.1f}" y2="{y:.1f}" stroke="{color}" stroke-width="3.4"/>')
        out.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{r * 0.17:.1f}" fill="{TEAL_DEEP}" stroke="{color}" '
                   f'stroke-width="3"/>')
    return "\n".join(out)


def header(meta):
    left, _, right = meta["coverBand"].partition(" · KAFKA")
    right = "KAFKA" + right if right else ""
    return "\n".join([
        f'<rect width="{W}" height="160" fill="{TEAL_DEEP}"/>',
        f'<rect y="160" width="{W}" height="8" fill="{GOLD}"/>',
        hub_icon(PLATE_X0 + 40, 80, 36, GOLD),
        f'<text x="{PLATE_X0 + 104}" y="94" font-family="Guide Sans, sans-serif" font-weight="700" font-size="36" '
        f'letter-spacing="8" fill="{GOLD}">{esc(left)}</text>',
        f'<text x="{PLATE_X1}" y="94" text-anchor="end" font-family="Guide Sans, sans-serif" font-weight="700" '
        f'font-size="36" letter-spacing="8" fill="{PAPER}" opacity="0.92">{esc(right)}</text>',
    ])


def chips(topics, x0, y0, max_w, font=27):
    """Mono şriftli "çiplər": eni simvol sayından dəqiq hesablanır (JetBrains Mono, 0,6 em)."""
    out, x, y = [], x0, y0
    h, pad, gap = 54, 22, 16
    for t in topics:
        w = len(t) * font * 0.6 + 2 * pad
        if x + w > x0 + max_w:
            x, y = x0, y + h + gap
        out.append(f'<rect x="{x:.1f}" y="{y}" width="{w:.1f}" height="{h}" rx="27" fill="{TEAL_DEEP}" '
                   f'fill-opacity="0.45" stroke="{GOLD_SOFT}" stroke-opacity="0.55" stroke-width="2"/>')
        out.append(f'<text x="{x + w / 2:.1f}" y="{y + 36}" text-anchor="middle" font-family="Guide Mono, '
                   f'monospace" font-size="{font}" fill="{PAPER}">{esc(t)}</text>')
        x += w + gap
    return "\n".join(out), y + h


def wrap(text, max_chars):
    words, lines, cur = text.split(), [], ""
    for w in words:
        if len(cur) + len(w) + 1 > max_chars and cur:
            lines.append(cur)
            cur = w
        else:
            cur = f"{cur} {w}".strip()
    if cur:
        lines.append(cur)
    return lines


def title_panel(meta, stats):
    n_ch, n_dia, n_ex, n_refs = stats
    x = 150
    out = [f'<rect y="{TITLE_Y0}" width="{W}" height="{H - TITLE_Y0}" fill="url(#panel)"/>',
           f'<rect y="{TITLE_Y0}" width="{W}" height="6" fill="{GOLD}"/>']
    # Panelin sağ yuxarısında zəif "hub" naxışı
    for k in range(5):
        hx, hy = 1660 + (k % 3) * 120, TITLE_Y0 + 90 + (k // 3) * 120 + (k % 2) * 40
        out.append(f'<g opacity="{0.10 + 0.03 * (k % 2):.2f}">{hub_icon(hx, hy, 44, GOLD_SOFT)}</g>')
    out.append(f'<text x="{x}" y="{TITLE_Y0 + 104}" font-family="Guide Sans, sans-serif" font-weight="700" '
               f'font-size="32" letter-spacing="9" fill="{GOLD}">{esc(az_upper(meta["kicker"]))}</text>')
    l1, l2 = meta["coverTitleLines"]
    out.append(f'<text x="{x - 8}" y="{TITLE_Y0 + 272}" font-family="Guide Serif, serif" font-weight="700" '
               f'font-size="172" letter-spacing="-2" fill="#ffffff">{esc(l1)}</text>')
    out.append(f'<text x="{x - 8}" y="{TITLE_Y0 + 448}" font-family="Guide Serif, serif" font-weight="700" '
               f'font-size="172" letter-spacing="-2" fill="{GOLD}">{esc(l2)}</text>')
    y = TITLE_Y0 + 510
    out.append(f'<rect x="{x}" y="{y}" width="140" height="7" fill="{GOLD}"/>')
    for i, line in enumerate(wrap(meta["subtitle"], 50)):
        out.append(f'<text x="{x}" y="{y + 72 + i * 64}" font-family="Guide Serif, serif" font-style="italic" '
                   f'font-size="50" fill="{PAPER}">{esc(line)}</text>')
    chips_svg, chips_bottom = chips(meta["coverTopics"], x, TITLE_Y0 + 690, W - 2 * x, font=25)
    out.append(chips_svg)
    y_div = chips_bottom + 40
    out.append(f'<line x1="{x}" y1="{y_div}" x2="{W - x}" y2="{y_div}" stroke="{PAPER}" stroke-width="1.6" '
               f'opacity="0.28"/>')
    stats_line = (f"{n_ch} fəsil  ·  {n_dia} diaqram  ·  {n_ex} praktiki tapşırıq  ·  "
                  f"{n_refs} yoxlanılmış kod istinadı")
    out.append(f'<text x="{x}" y="{y_div + 64}" font-family="Guide Sans, sans-serif" font-size="27" '
               f'letter-spacing="1" fill="{PAPER}" opacity="0.85">{esc(stats_line)}</text>')
    out.append(f'<text x="{x}" y="{y_div + 108}" font-family="Guide Sans, sans-serif" font-weight="700" '
               f'font-size="25" letter-spacing="6" fill="{GOLD_SOFT}" opacity="0.9">'
               f'{esc(az_upper(meta["edition"]))}</text>')
    out.append(f'<text x="{W - x}" y="{y_div + 46}" text-anchor="end" font-family="Guide Sans, sans-serif" '
               f'font-size="22" letter-spacing="8" fill="{PAPER}" opacity="0.7">MÜƏLLİF</text>')
    out.append(f'<text x="{W - x + 10}" y="{y_div + 150}" text-anchor="end" font-family="Guide Sans, sans-serif" '
               f'font-weight="700" font-size="118" letter-spacing="20" fill="{GOLD}">{esc(meta["author"])}</text>')
    assert y_div + 160 < H - 40, f"başlıq paneli səhifəyə sığmır: {y_div}"
    return "\n".join(out)


# --------------------------------------------------------------------------- Yığım
def defs():
    return f"""<defs>
<radialGradient id="plateGlow" cx="50%" cy="42%" r="75%">
  <stop offset="0%" stop-color="#fbf8f1"/><stop offset="70%" stop-color="{PAPER}"/>
  <stop offset="100%" stop-color="{PAPER_DEEP}"/></radialGradient>
<linearGradient id="panel" x1="0" y1="0" x2="1" y2="1">
  <stop offset="0%" stop-color="{TEAL}"/><stop offset="100%" stop-color="{TEAL_DEEP}"/></linearGradient>
<pattern id="hatchMembrane" width="10" height="10" patternUnits="userSpaceOnUse" patternTransform="rotate(28)">
  <line x1="0" y1="0" x2="0" y2="10" stroke="{INK}" stroke-width="0.9" opacity="0.35"/></pattern>
<pattern id="hatchCross" width="8" height="8" patternUnits="userSpaceOnUse" patternTransform="rotate(30)">
  <line x1="0" y1="0" x2="0" y2="8" stroke="{INK}" stroke-width="2.2"/>
  <line x1="0" y1="0" x2="8" y2="0" stroke="{INK}" stroke-width="1.4"/></pattern>
{cover_art.DEFS}
</defs>"""


def build(meta):
    stats = book_stats()
    art, arms = cover_art.octopus()
    ix, iy, ir = INSET
    svg = [f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="210mm" height="297mm" '
           f'role="img" aria-label="{esc(meta["title"])} — {esc(meta["author"])}">',
           f'<title>{esc(meta["title"])} — {esc(meta["author"])}</title>',
           defs(),
           f'<rect width="{W}" height="{H}" fill="{PAPER}"/>',
           header(meta),
           crop_marks(),
           plate_background(),
           f'<clipPath id="plateClip"><rect x="{PLATE_X0}" y="{PLATE_Y0}" width="{PLATE_X1 - PLATE_X0}" '
           f'height="{PLATE_Y1 - PLATE_Y0}"/></clipPath>',
           '<g clip-path="url(#plateClip)">',
           cover_art.network(PLATE_X0, PLATE_Y0 + 140, PLATE_X1, LEGEND_Y - 40, (CX, 860 + ART_DY), 780),
           '</g>',
           f'<g transform="translate(0 {ART_DY})">{art}</g>',
           cover_art.hearts_inset(ix, iy, ir),
           callouts(arms),
           plate_texts(),
           legend(),
           plate_frame(),
           title_panel(meta, stats),
           '</svg>']
    return "\n".join(svg)


def main():
    with open(os.path.join(HERE, "book.json"), encoding="utf-8") as f:
        meta = json.load(f)["meta"]
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as f:
        f.write(build(meta))
    print(f"{OUT}: yaradıldı")


if __name__ == "__main__":
    main()
