#!/usr/bin/env python3
"""Kitabın üz qabığını SVG kimi yaradır: cover/cover.svg.

Kompozisiya: yuxarıda tünd zolaq (texnologiyalar), ortada elmi qravüra üslubunda bal arısı (Apis mellifera)
və arxa planda pətək toru, aşağıda başlıq bloku və müəllif. Bütün qrafika koddan qurulur — xarici şəkil
istifadə olunmur. Ölçü A4 nisbətindədir (viewBox 2100 × 2970, 1 vahid = 0,1 mm).

İstifadə: python3 build/cover.py  (meta məlumatı build/book.json-dan oxunur)
"""
import json
import math
import os
import random

HERE = os.path.dirname(os.path.abspath(__file__))
GUIDE = os.path.dirname(HERE)
OUT = os.path.join(GUIDE, "cover", "cover.svg")

W, H = 2100, 2970
INK = "#1f1d1a"
PAPER = "#f3eee3"
TEAL = "#0f4650"
TEAL_DARK = "#0b363e"
AMBER = "#e2a62f"
MUTED = "#7a6c58"
CX = W / 2

rng = random.Random(25)
SHOW_CELL_LABELS = False  # servis adları anatomik göstəricilərlə toqquşur; sadə görünüş üçün söndürülüb


def az_upper(text):
    """Azərbaycan əlifbası üçün böyük hərf: i → İ, ı → I."""
    return text.replace("i", "İ").replace("ı", "I").upper()


def esc(text):
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


def mirror(path_d, group_attrs=""):
    """Sağ yarı üçün çəkilən elementi simmetrik sol yarıya da əlavə edir."""
    return (f'<g {group_attrs}>{path_d}</g>'
            f'<g transform="translate({2 * CX} 0) scale(-1 1)" {group_attrs}>{path_d}</g>')


def ellipse_points(cx, cy, rx, ry, n):
    return [(cx + rx * math.cos(2 * math.pi * i / n), cy + ry * math.sin(2 * math.pi * i / n), 2 * math.pi * i / n)
            for i in range(n)]


def hairs(cx, cy, rx, ry, n, length, width, jitter=0.35, arc=(0, 2 * math.pi)):
    """Ellips kənarında qısa tüklər — qravüranın "xəz" effekti."""
    out = []
    for i in range(n):
        a = arc[0] + (arc[1] - arc[0]) * i / n + rng.uniform(-0.02, 0.02)
        x0, y0 = cx + rx * math.cos(a), cy + ry * math.sin(a)
        nx, ny = math.cos(a) / rx, math.sin(a) / ry
        norm = math.hypot(nx, ny)
        nx, ny = nx / norm, ny / norm
        tilt = rng.uniform(-jitter, jitter)
        dx = nx * math.cos(tilt) - ny * math.sin(tilt)
        dy = nx * math.sin(tilt) + ny * math.cos(tilt)
        ln = length * rng.uniform(0.55, 1.15)
        sx, sy = x0 - nx * ln * 0.35, y0 - ny * ln * 0.35
        out.append(f'<path d="M{sx:.1f},{sy:.1f} Q{x0 + dx * ln * 0.5 + rng.uniform(-3, 3):.1f},'
                   f'{y0 + dy * ln * 0.5:.1f} {x0 + dx * ln:.1f},{y0 + dy * ln:.1f}" '
                   f'stroke="{INK}" stroke-width="{width}" fill="none" stroke-linecap="round"/>')
    return "\n".join(out)


def honeycomb():
    """Arxa planda pətək toru; bəzi hüceyrələr bal rəngində, bir neçəsi servis adları ilə."""
    r = 92
    dx, dy = 1.5 * r, math.sqrt(3) * r
    cells, labels = [], []
    named = {(-5, -3): "api-gateway", (5, -4): "auth-service", (-6, 2): "campaign-service",
             (6, 1): "dashboard", (-5, 5): "log-service", (5, 5): "kafka"}
    honey = {(-4, -3), (-5, -2), (4, -4), (6, -3), (-6, 3), (-5, 3), (6, 2), (5, 1), (-4, 5), (4, 5), (5, 6)}
    for q in range(-9, 10):
        for row in range(-7, 9):
            x = CX + q * dx
            y = 1060 + row * dy + (dy / 2 if q % 2 else 0)
            dist = math.hypot((x - CX) / 900, (y - 1060) / 760)
            if dist > 1.25:
                continue
            opacity = max(0.0, min(0.55, (dist - 0.35) * 0.9))
            if opacity <= 0.02:
                continue
            pts = " ".join(f"{x + r * math.cos(math.pi / 3 * k):.1f},{y + r * math.sin(math.pi / 3 * k):.1f}"
                           for k in range(6))
            fill = AMBER if (q, row) in honey else "none"
            fill_op = 0.22 if (q, row) in honey else 0
            cells.append(f'<polygon points="{pts}" fill="{fill}" fill-opacity="{fill_op}" stroke="#b9a988" '
                         f'stroke-width="3" opacity="{opacity:.2f}"/>')
            if SHOW_CELL_LABELS and (q, row) in named:
                labels.append(f'<text x="{x:.1f}" y="{y + 9:.1f}" text-anchor="middle" font-family="Guide Mono, '
                              f'DejaVu Sans Mono, monospace" font-size="21" fill="{MUTED}" '
                              f'opacity="{min(0.9, opacity + 0.35):.2f}">{named[(q, row)]}</text>')
    return "\n".join(cells + labels)


def wing(path, veins):
    vein_paths = "\n".join(f'<path d="{v}" stroke="{INK}" stroke-width="3.2" fill="none" opacity="0.85"/>'
                           for v in veins)
    return (f'<path d="{path}" fill="#ffffff" fill-opacity="0.55" stroke="{INK}" stroke-width="5"/>'
            f'<path d="{path}" fill="url(#hatchFine)" opacity="0.18"/>{vein_paths}')


def leg(points, width=9, hair_n=6):
    d = "M" + " L".join(f"{x},{y}" for x, y in points)
    out = [f'<path d="{d}" stroke="{INK}" stroke-width="{width}" fill="none" stroke-linejoin="round" '
           f'stroke-linecap="round"/>']
    for (x0, y0), (x1, y1) in zip(points, points[1:]):
        out.append(f'<circle cx="{x0}" cy="{y0}" r="{width * 0.75:.1f}" fill="{INK}"/>')
        for k in range(hair_n):
            t = (k + 0.5) / hair_n
            px, py = x0 + (x1 - x0) * t, y0 + (y1 - y0) * t
            ang = math.atan2(y1 - y0, x1 - x0) + math.pi / 2 * (1 if k % 2 else -1) * 0.8
            ln = rng.uniform(10, 18)
            out.append(f'<line x1="{px:.1f}" y1="{py:.1f}" x2="{px + ln * math.cos(ang):.1f}" '
                       f'y2="{py + ln * math.sin(ang):.1f}" stroke="{INK}" stroke-width="2.4"/>')
    return "\n".join(out)


def bee():
    parts = []
    # --- Qanadlar (sağ yarı; güzgü ilə sol) ---
    fore = ("M1150,835 C1300,735 1520,620 1700,585 C1800,566 1862,600 1850,660 C1838,725 1760,775 1650,808 "
            "C1480,858 1300,880 1165,872 Z")
    fore_veins = ["M1160,850 C1330,800 1560,700 1820,610",
                  "M1165,862 C1360,850 1560,800 1760,742",
                  "M1350,815 C1420,760 1470,700 1500,655", "M1500,655 C1560,690 1600,700 1660,690",
                  "M1450,775 C1530,770 1600,760 1660,690", "M1660,690 C1700,700 1740,705 1790,690",
                  "M1560,800 C1600,770 1640,745 1680,736"]
    hind = ("M1168,900 C1310,905 1500,925 1610,968 C1680,995 1690,1042 1630,1055 C1520,1078 1330,1010 1170,935 Z")
    hind_veins = ["M1172,915 C1330,940 1500,975 1640,1030", "M1380,948 C1420,980 1470,1010 1520,1030",
                  "M1480,965 C1520,975 1560,985 1610,1000"]
    parts.append(mirror(wing(hind, hind_veins)))
    parts.append(mirror(wing(fore, fore_veins)))

    # --- Ayaqlar ---
    front = [(1115, 805), (1190, 740), (1235, 655), (1268, 600), (1290, 560)]
    middle = [(1160, 900), (1290, 935), (1400, 1000), (1470, 1050), (1515, 1085)]
    hind_leg = [(1140, 975), (1260, 1060), (1330, 1190), (1385, 1310), (1420, 1395)]
    parts.append(mirror(leg(front, 9)))
    parts.append(mirror(leg(middle, 10)))
    parts.append(mirror(leg(hind_leg, 11, 7)))
    # Arxa ayaqda polen səbəti (corbicula)
    parts.append(mirror(f'<ellipse cx="1295" cy="1125" rx="30" ry="66" transform="rotate(-28 1295 1125)" '
                        f'fill="url(#hatchCross)" stroke="{INK}" stroke-width="5"/>'
                        f'<ellipse cx="1295" cy="1125" rx="18" ry="42" transform="rotate(-28 1295 1125)" '
                        f'fill="{AMBER}" opacity="0.55"/>'))

    # --- Qarın (abdomen) ---
    abdomen = ("M1050,985 C1165,985 1215,1080 1215,1185 C1215,1320 1150,1420 1050,1470 "
               "C950,1420 885,1320 885,1185 C885,1080 935,985 1050,985 Z")
    parts.append(f'<clipPath id="abdClip"><path d="{abdomen}"/></clipPath>')
    parts.append(f'<path d="{abdomen}" fill="{PAPER}"/>')
    bands = [(1000, 1062, "dark"), (1062, 1095, "light"), (1095, 1170, "dark"), (1170, 1200, "light"),
             (1200, 1270, "dark"), (1270, 1298, "light"), (1298, 1365, "dark"), (1365, 1392, "light"),
             (1392, 1480, "dark")]
    for y0, y1, kind in bands:
        fill = "url(#hatchDense)" if kind == "dark" else "url(#hatchFine)"
        parts.append(f'<path clip-path="url(#abdClip)" d="M860,{y0} Q1050,{y0 + 26} 1240,{y0} L1240,{y1} '
                     f'Q1050,{y1 + 26} 860,{y1} Z" fill="{fill}"/>')
        if kind == "light":
            parts.append(f'<path clip-path="url(#abdClip)" d="M860,{y0} Q1050,{y0 + 26} 1240,{y0} L1240,{y1} '
                         f'Q1050,{y1 + 26} 860,{y1} Z" fill="{AMBER}" opacity="0.28"/>')
    # Kölgə tərəfi (sağ)
    parts.append(f'<path clip-path="url(#abdClip)" d="M1120,980 C1240,1100 1240,1330 1120,1480 L1260,1480 '
                 f'L1260,980 Z" fill="url(#hatchCross)" opacity="0.6"/>')
    parts.append(f'<path d="{abdomen}" fill="none" stroke="{INK}" stroke-width="6"/>')
    parts.append(f'<path d="M1050,1470 L1050,1500" stroke="{INK}" stroke-width="5" stroke-linecap="round"/>')
    parts.append(hairs(1050, 1200, 168, 268, 120, 22, 2.2, arc=(-0.3, math.pi + 0.3)))

    # --- Bel (petiole) ---
    parts.append(f'<ellipse cx="1050" cy="985" rx="40" ry="24" fill="url(#hatchCross)" stroke="{INK}" '
                 f'stroke-width="5"/>')

    # --- Döş (thorax) ---
    parts.append(hairs(1050, 870, 140, 122, 170, 34, 2.4))
    parts.append(f'<ellipse cx="1050" cy="870" rx="138" ry="120" fill="url(#hatchCross)" stroke="{INK}" '
                 f'stroke-width="6"/>')
    parts.append(f'<ellipse cx="1010" cy="840" rx="70" ry="52" fill="{PAPER}" opacity="0.55"/>')
    parts.append(f'<ellipse cx="1050" cy="870" rx="138" ry="120" fill="url(#hatchFine)" opacity="0.35"/>')
    parts.append(f'<path d="M985,800 C1020,780 1080,780 1115,800" stroke="{INK}" stroke-width="4" fill="none"/>')
    parts.append(f'<path d="M965,925 C1010,955 1090,955 1135,925" stroke="{INK}" stroke-width="4" fill="none"/>')

    # --- Baş ---
    parts.append(hairs(1050, 690, 96, 80, 90, 18, 2.0, arc=(math.pi * 1.05, math.pi * 1.95)))
    parts.append(f'<ellipse cx="1050" cy="690" rx="96" ry="80" fill="url(#hatchFine)" stroke="{INK}" '
                 f'stroke-width="6"/>')
    parts.append(mirror(f'<ellipse cx="1112" cy="688" rx="40" ry="66" transform="rotate(14 1112 688)" '
                        f'fill="url(#hatchDense)" stroke="{INK}" stroke-width="5"/>'
                        f'<ellipse cx="1100" cy="665" rx="10" ry="18" transform="rotate(14 1100 665)" '
                        f'fill="{PAPER}" opacity="0.8"/>'))
    for ox, oy in ((1050, 632), (1030, 646), (1070, 646)):
        parts.append(f'<circle cx="{ox}" cy="{oy}" r="8" fill="{INK}"/>')
    parts.append(mirror(f'<path d="M1062,762 C1075,778 1072,792 1058,800" stroke="{INK}" stroke-width="7" '
                        f'fill="none" stroke-linecap="round"/>'))
    # Bığcıqlar (antenna): sap və buğumlu qamçı
    antenna = [(1080, 630), (1108, 572), (1130, 520)]
    flag = [(1130, 520), (1175, 478), (1218, 446), (1258, 420), (1296, 400), (1330, 388)]
    ant = [f'<path d="M{antenna[0][0]},{antenna[0][1]} Q1100,585 {antenna[2][0]},{antenna[2][1]}" '
           f'stroke="{INK}" stroke-width="9" fill="none" stroke-linecap="round"/>']
    d = "M" + " L".join(f"{x},{y}" for x, y in flag)
    ant.append(f'<path d="{d}" stroke="{INK}" stroke-width="7" fill="none" stroke-linecap="round" '
               f'stroke-linejoin="round"/>')
    for x, y in flag[1:]:
        ant.append(f'<circle cx="{x}" cy="{y}" r="6" fill="{INK}"/>')
    parts.append(mirror("\n".join(ant)))
    return "\n".join(parts)


def plate_labels():
    """Anatomik lövhə üslubunda göstəricilər — hər hissəyə sistemin bir qatı uyğun gəlir."""
    items = [((1290, 560), (1500, 470), "A", "gateway"), ((1180, 860), (1600, 1230), "B", "servislər"),
             ((1200, 1300), (1560, 1420), "C", "hadisələr"), ((905, 690), (560, 560), "D", "təhlükəsizlik"),
             ((900, 1300), (540, 1420), "E", "müşahidə")]
    out = []
    for (x0, y0), (x1, y1), letter, word in items:
        anchor = "start" if x1 > CX else "end"
        tx = x1 + (22 if x1 > CX else -22)
        out.append(f'<line x1="{x0}" y1="{y0}" x2="{x1}" y2="{y1}" stroke="{MUTED}" stroke-width="2.2"/>')
        out.append(f'<circle cx="{x1}" cy="{y1}" r="5" fill="{MUTED}"/>')
        out.append(f'<text x="{tx}" y="{y1 + 12}" text-anchor="{anchor}" font-family="Guide Serif, serif" '
                   f'font-style="italic" font-size="34" fill="{MUTED}">{letter}. {word}</text>')
    return "\n".join(out)


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


def build(meta):
    title_lines = meta["coverTitleLines"]
    subtitle_lines = wrap(meta["subtitle"], 46)
    band_text = esc(meta["coverBand"])
    svg = [f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="210mm" height="297mm" '
           f'role="img" aria-label="{esc(meta["title"])} — {esc(meta["author"])}">',
           f'<title>{esc(meta["title"])}</title>',
           '<defs>',
           f'<pattern id="hatchFine" width="12" height="12" patternUnits="userSpaceOnUse" '
           f'patternTransform="rotate(35)"><line x1="0" y1="0" x2="0" y2="12" stroke="{INK}" stroke-width="2"/>'
           '</pattern>',
           f'<pattern id="hatchDense" width="7" height="7" patternUnits="userSpaceOnUse" '
           f'patternTransform="rotate(35)"><line x1="0" y1="0" x2="0" y2="7" stroke="{INK}" stroke-width="3"/>'
           '</pattern>',
           f'<pattern id="hatchCross" width="9" height="9" patternUnits="userSpaceOnUse" '
           f'patternTransform="rotate(30)"><line x1="0" y1="0" x2="0" y2="9" stroke="{INK}" stroke-width="2.4"/>'
           f'<line x1="0" y1="0" x2="9" y2="0" stroke="{INK}" stroke-width="1.6"/></pattern>',
           f'<radialGradient id="vignette" cx="50%" cy="45%" r="70%"><stop offset="60%" stop-color="{PAPER}" '
           f'stop-opacity="0"/><stop offset="100%" stop-color="#d9cdb4" stop-opacity="0.55"/></radialGradient>',
           '</defs>',
           f'<rect width="{W}" height="{H}" fill="{PAPER}"/>',
           # Yuxarı zolaq
           f'<rect width="{W}" height="200" fill="{TEAL}"/>',
           f'<rect y="200" width="{W}" height="10" fill="{AMBER}"/>',
           f'<text x="{CX}" y="122" text-anchor="middle" font-family="Guide Sans, DejaVu Sans, sans-serif" '
           f'font-weight="700" font-size="40" letter-spacing="7" fill="{AMBER}">{band_text}</text>',
           # İllüstrasiya sahəsi
           '<clipPath id="artClip"><rect y="210" width="2100" height="1630"/></clipPath>',
           '<g clip-path="url(#artClip)">', honeycomb(), '</g>',
           f'<rect y="210" width="{W}" height="1620" fill="url(#vignette)"/>',
           '<g>', bee(), '</g>',
           plate_labels(),
           f'<rect x="{CX - 330}" y="1640" width="660" height="135" rx="10" fill="{PAPER}" opacity="0.92"/>',
           f'<text x="{CX}" y="1700" text-anchor="middle" font-family="Guide Serif, serif" font-style="italic" '
           f'font-size="40" fill="{MUTED}">Apis mellifera</text>',
           f'<text x="{CX}" y="1752" text-anchor="middle" font-family="Guide Sans, sans-serif" font-size="26" '
           f'letter-spacing="5" fill="{MUTED}">KOLONİYA · HÜCEYRƏ · HADİSƏ</text>',
           # Başlıq bloku
           f'<rect y="1840" width="{W}" height="780" fill="{TEAL}"/>',
           f'<rect y="1840" width="{W}" height="8" fill="{TEAL_DARK}"/>',
           f'<text x="150" y="1960" font-family="Guide Sans, sans-serif" font-weight="700" font-size="34" '
           f'letter-spacing="8" fill="{AMBER}">{esc(az_upper(meta["kicker"]))}</text>']
    y = 2115
    for line in title_lines:
        svg.append(f'<text x="142" y="{y}" font-family="Guide Serif, serif" font-weight="700" font-size="164" '
                   f'fill="#ffffff">{esc(line)}</text>')
        y += 176
    svg.append(f'<rect x="150" y="{y - 108}" width="220" height="10" fill="{AMBER}"/>')
    y += 4
    for line in subtitle_lines:
        svg.append(f'<text x="150" y="{y}" font-family="Guide Serif, serif" font-style="italic" '
                   f'font-size="56" fill="{PAPER}">{esc(line)}</text>')
        y += 72
    assert y < 2640, "altbaşlıq başlıq blokundan kənara çıxır"
    # Müəllif və nəşr
    svg += [f'<text x="{W - 150}" y="2800" text-anchor="end" font-family="Guide Sans, sans-serif" '
            f'font-weight="700" font-size="120" letter-spacing="18" fill="{TEAL}">{esc(meta["author"])}</text>',
            f'<rect x="{W - 150 - 300}" y="2830" width="300" height="8" fill="{AMBER}"/>',
            f'<text x="150" y="2800" font-family="Guide Sans, sans-serif" font-size="34" letter-spacing="4" '
            f'fill="{MUTED}">{esc(az_upper(meta["edition"]))}</text>',
            f'<text x="150" y="2848" font-family="Guide Serif, serif" font-style="italic" font-size="32" '
            f'fill="{MUTED}">{esc(meta["coverNote"])}</text>',
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
